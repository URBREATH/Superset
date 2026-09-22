import React, { useEffect } from "react";
import { useEmbeddedHostConfig } from "../context/EmbeddedHostContext";
import { normalizeLanguage } from "../utils/language";
import "./GoogleTranslator.css";

const GOOGLE_TRANSLATE_SCRIPT_ID = "urbreath-google-translate-script";
const GOOGLE_TRANSLATE_ELEMENT_ID = "urbreath-google-translate-element";
const GOOGLE_TRANSLATE_CALLBACK = "__urbreathGoogleTranslateInit";
const PAGE_LANGUAGE = "en";
const GOOGLE_TRANSLATE_BANNER_SELECTORS = [
  "iframe.goog-te-banner-frame",
  ".goog-te-banner-frame",
  "iframe[src*='translate.google']",
  "#goog-gt-tt",
];

let googleTranslateScriptPromise = null;
let googleTranslateWidgetInitialized = false;

function isGoogleTranslateReady() {
  return (
    window.google &&
    window.google.translate &&
    typeof window.google.translate.TranslateElement === "function"
  );
}

function loadGoogleTranslateScript() {
  if (isGoogleTranslateReady()) {
    return Promise.resolve();
  }

  if (googleTranslateScriptPromise) {
    return googleTranslateScriptPromise;
  }

  googleTranslateScriptPromise = new Promise((resolve, reject) => {
    const script = document.getElementById(GOOGLE_TRANSLATE_SCRIPT_ID);
    const fail = () => {
      delete window[GOOGLE_TRANSLATE_CALLBACK];
      reject(new Error("Google Translate script could not be loaded"));
    };

    window[GOOGLE_TRANSLATE_CALLBACK] = () => {
      delete window[GOOGLE_TRANSLATE_CALLBACK];
      if (isGoogleTranslateReady()) {
        resolve();
        return;
      }
      reject(new Error("Google Translate loaded without its TranslateElement API"));
    };

    if (script) {
      script.onerror = fail;
      return;
    }

    const translateScript = document.createElement("script");
    translateScript.id = GOOGLE_TRANSLATE_SCRIPT_ID;
    translateScript.src = `https://translate.google.com/translate_a/element.js?cb=${GOOGLE_TRANSLATE_CALLBACK}`;
    translateScript.async = true;
    translateScript.onerror = fail;
    document.head.appendChild(translateScript);
  }).catch((error) => {
    googleTranslateScriptPromise = null;
    throw error;
  });

  return googleTranslateScriptPromise;
}

function initializeGoogleTranslateWidget() {
  if (googleTranslateWidgetInitialized) {
    return;
  }

  if (!isGoogleTranslateReady()) {
    throw new Error("Google Translate API is not ready");
  }

  new window.google.translate.TranslateElement(
    {
      autoDisplay: false,
      pageLanguage: PAGE_LANGUAGE,
    },
    GOOGLE_TRANSLATE_ELEMENT_ID
  );
  googleTranslateWidgetInitialized = true;
}

function getTranslateSelect() {
  const container = document.getElementById(GOOGLE_TRANSLATE_ELEMENT_ID);
  return container ? container.querySelector("select.goog-te-combo") : null;
}

function hasLanguageOption(select, language) {
  return Array.from(select.options).some((option) => option.value === language);
}

function hideGoogleTranslateUi() {
  GOOGLE_TRANSLATE_BANNER_SELECTORS.forEach((selector) => {
    document.querySelectorAll(selector).forEach((element) => {
      element.style.setProperty("display", "none", "important");
      element.style.setProperty("visibility", "hidden", "important");
      element.style.setProperty("height", "0", "important");
    });
  });

  document.querySelectorAll("iframe").forEach((element) => {
    const bounds = element.getBoundingClientRect();
    const position = window.getComputedStyle(element).position;
    const isTopBanner =
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

function waitForTranslateSelect(language) {
  const existingSelect = getTranslateSelect();
  if (existingSelect && hasLanguageOption(existingSelect, language)) {
    return Promise.resolve(existingSelect);
  }

  const container = document.getElementById(GOOGLE_TRANSLATE_ELEMENT_ID);
  if (!container) {
    return Promise.reject(new Error("Google Translate container is not available"));
  }

  return new Promise((resolve, reject) => {
    const observer = new MutationObserver(() => {
      const select = getTranslateSelect();
      if (!select || !hasLanguageOption(select, language)) {
        return;
      }

      window.clearTimeout(timeoutId);
      observer.disconnect();
      resolve(select);
    });

    const timeoutId = window.setTimeout(() => {
      observer.disconnect();
      reject(new Error(`Google Translate language "${language}" did not render`));
    }, 10000);

    observer.observe(container, { childList: true, subtree: true });
  });
}

async function applyLanguage(language, isCancelled) {
  await loadGoogleTranslateScript();
  if (isCancelled()) {
    return;
  }

  initializeGoogleTranslateWidget();

  const select = await waitForTranslateSelect(language);
  if (isCancelled()) {
    return;
  }

  select.value = language;

  if (select.value !== language) {
    throw new Error(`Google Translate does not support language "${language}"`);
  }

  select.dispatchEvent(new Event("change"));
}

function GoogleTranslator() {
  const { language } = useEmbeddedHostConfig();

  useEffect(() => {
    hideGoogleTranslateUi();

    const observer = new MutationObserver(hideGoogleTranslateUi);
    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    });

    return () => {
      observer.disconnect();
    };
  }, []);

  useEffect(() => {
    const normalizedLanguage = normalizeLanguage(language);
    if (!normalizedLanguage) {
      return undefined;
    }

    document.documentElement.lang = normalizedLanguage;

    // English is the source language; only initialize the widget when a translation
    // is requested or when a previous translation must be reset.
    if (normalizedLanguage === PAGE_LANGUAGE && !googleTranslateWidgetInitialized) {
      return undefined;
    }

    let cancelled = false;
    applyLanguage(normalizedLanguage, () => cancelled).catch((error) => {
      if (!cancelled) {
        console.error("[GoogleTranslator] Unable to apply host language", error);
      }
    });

    return () => {
      cancelled = true;
    };
  }, [language]);

  return <div id={GOOGLE_TRANSLATE_ELEMENT_ID} className="urbreath-google-translate" />;
}

export default GoogleTranslator;
