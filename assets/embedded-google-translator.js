(function () {
  "use strict";

  var ELEMENT_ID = "urbreath-google-translate-element";
  var PAGE_LANGUAGE = "en";
  var INCLUDED_LANGUAGES = "en,it,es,fr,nl,ru,ro,el";
  var requestedLanguage = null;
  var widgetInitialized = false;
  var applySequence = 0;
  var dashboardObserver = null;
  var reapplyTimer = null;
  var scriptNonce = document.currentScript && document.currentScript.nonce;

  function normalizeLanguage(language) {
    if (typeof language !== "string") {
      return null;
    }

    var languageCode = language.trim().toLowerCase().split(/[-_]/)[0];
    return /^[a-z]{2}$/.test(languageCode) ? languageCode : null;
  }

  function getLanguageFromUrl() {
    return normalizeLanguage(new URLSearchParams(window.location.search).get("language"));
  }

  function isGoogleTranslateReady() {
    return (
      window.google &&
      window.google.translate &&
      typeof window.google.translate.TranslateElement === "function"
    );
  }

  function ensureElement() {
    var element = document.getElementById(ELEMENT_ID);
    if (element) {
      return element;
    }

    element = document.createElement("div");
    element.id = ELEMENT_ID;
    element.style.display = "none";
    document.body.appendChild(element);
    return element;
  }

  function getTranslateSelect() {
    var element = document.getElementById(ELEMENT_ID);
    return element ? element.querySelector("select.goog-te-combo") : null;
  }

  function hideGoogleTranslateUi() {
    var bannerSelectors = [
      "iframe.goog-te-banner-frame",
      ".goog-te-banner-frame",
      "#goog-gt-tt",
    ];

    bannerSelectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (element) {
        element.style.setProperty("display", "none", "important");
        element.style.setProperty("visibility", "hidden", "important");
        element.style.setProperty("height", "0", "important");
      });
    });

    document.querySelectorAll("iframe").forEach(function (element) {
      var bounds = element.getBoundingClientRect();
      var position = window.getComputedStyle(element).position;
      var isTopBanner =
        (position === "fixed" || position === "absolute") &&
        bounds.top <= 2 &&
        bounds.height > 0 &&
        bounds.height <= 100 &&
        bounds.width >= window.innerWidth * 0.75;

      if (isTopBanner) {
        element.style.setProperty("display", "none", "important");
        element.style.setProperty("visibility", "hidden", "important");
        element.style.setProperty("height", "0", "important");
      }
    });

    if (document.body) {
      document.body.style.setProperty("top", "0", "important");
    }
  }

  function isTranslatorNode(node) {
    var element = node && node.nodeType === Node.ELEMENT_NODE ? node : node.parentElement;
    return (
      !element ||
      element.id === ELEMENT_ID ||
      element.closest(".goog-te-banner-frame, #goog-gt-tt")
    );
  }

  function startDashboardObserver() {
    if (dashboardObserver || !document.body) {
      return;
    }

    dashboardObserver = new MutationObserver(function (mutations) {
      var hasDashboardChanges = mutations.some(function (mutation) {
        if (isTranslatorNode(mutation.target)) {
          return false;
        }

        return Array.prototype.some.call(mutation.addedNodes, function (node) {
          return !isTranslatorNode(node);
        });
      });

      if (!hasDashboardChanges || !requestedLanguage || requestedLanguage === PAGE_LANGUAGE) {
        return;
      }

      if (reapplyTimer) {
        window.clearTimeout(reapplyTimer);
      }

      reapplyTimer = window.setTimeout(function () {
        reapplyTimer = null;
        if (dashboardObserver) {
          dashboardObserver.disconnect();
          dashboardObserver = null;
        }
        applyLanguage(requestedLanguage).then(function () {
          startDashboardObserver();
        });
      }, 800);
    });

    dashboardObserver.observe(document.body, { childList: true, subtree: true });
  }

  function hasLanguageOption(select, language) {
    return Array.prototype.some.call(select.options, function (option) {
      return option.value === language;
    });
  }

  function waitForTranslateSelect(language, sequence) {
    var existingSelect = getTranslateSelect();
    if (
      existingSelect &&
      (language === PAGE_LANGUAGE || hasLanguageOption(existingSelect, language))
    ) {
      return Promise.resolve(existingSelect);
    }

    var element = ensureElement();
    return new Promise(function (resolve, reject) {
      var observer = new MutationObserver(function () {
        var select = getTranslateSelect();
        if (
          !select ||
          (language !== PAGE_LANGUAGE && !hasLanguageOption(select, language))
        ) {
          return;
        }

        window.clearTimeout(timeoutId);
        observer.disconnect();
        resolve(select);
      });

      var timeoutId = window.setTimeout(function () {
        observer.disconnect();
        reject(new Error("Google Translate language selector did not render"));
      }, 10000);

      observer.observe(element, { childList: true, subtree: true });

      if (sequence !== applySequence) {
        observer.disconnect();
        resolve(null);
      }
    });
  }

  function applyLanguage(language) {
    var normalizedLanguage = normalizeLanguage(language);
    if (!normalizedLanguage) {
      return Promise.resolve();
    }

    requestedLanguage = normalizedLanguage;
    if (!widgetInitialized) {
      return Promise.resolve();
    }

    var sequence = ++applySequence;
    return waitForTranslateSelect(normalizedLanguage, sequence)
      .then(function (select) {
        if (!select || sequence !== applySequence) {
          return;
        }

        // The source language is English. Selecting the empty option restores
        // the original text after a previous translation.
        select.value = normalizedLanguage === PAGE_LANGUAGE ? "" : normalizedLanguage;
        if (normalizedLanguage !== PAGE_LANGUAGE && select.value !== normalizedLanguage) {
          throw new Error('Google Translate does not support language "' + normalizedLanguage + '"');
        }

        select.dispatchEvent(new Event("change"));
      })
      .catch(function (error) {
        if (sequence === applySequence) {
          console.error("[Superset embedded translator] Unable to apply language", error);
        }
      });
  }

  function initializeWidget() {
    if (widgetInitialized || !isGoogleTranslateReady()) {
      return;
    }

    new window.google.translate.TranslateElement(
      {
        autoDisplay: false,
        includedLanguages: INCLUDED_LANGUAGES,
        pageLanguage: PAGE_LANGUAGE,
      },
      ELEMENT_ID
    );
    widgetInitialized = true;
    applyLanguage(requestedLanguage).then(function () {
      hideGoogleTranslateUi();
      startDashboardObserver();
    });
  }

  function loadGoogleTranslate() {
    if (isGoogleTranslateReady()) {
      initializeWidget();
      return;
    }

    window.googleTranslateElementInit = function () {
      initializeWidget();
    };

    var script = document.createElement("script");
    script.src =
      "https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit";
    script.async = true;
    if (scriptNonce) {
      script.nonce = scriptNonce;
    }
    script.onerror = function () {
      console.error("[Superset embedded translator] Google Translate script could not be loaded");
    };
    document.head.appendChild(script);
  }

  window.addEventListener("message", function (event) {
    if (event.source !== window.parent || !event.data || typeof event.data !== "object") {
      return;
    }

    applyLanguage(event.data.language);
  });

  requestedLanguage = getLanguageFromUrl();
  ensureElement();
  hideGoogleTranslateUi();
  new MutationObserver(hideGoogleTranslateUi).observe(document.documentElement, {
    childList: true,
    subtree: true,
  });
  loadGoogleTranslate();
})();
