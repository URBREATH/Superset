import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

const EmbeddedHostContext = createContext({
  embedded: false,
  sideMenu: "expanded",
  accessToken: null,
  refreshToken: null,
  language: null,
  sourceOrigin: null,
  hasHostMessage: false,
});

function parseEmbeddedFromQuery() {
  const searchParams = new URLSearchParams(window.location.search);
  return searchParams.get("embedded") === "true";
}

function normalizeHostPayload(payload) {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const hasKnownKey =
    Object.prototype.hasOwnProperty.call(payload, "embedded") ||
    Object.prototype.hasOwnProperty.call(payload, "sideMenu") ||
    Object.prototype.hasOwnProperty.call(payload, "accessToken") ||
    Object.prototype.hasOwnProperty.call(payload, "refreshToken") ||
    Object.prototype.hasOwnProperty.call(payload, "language");

  if (!hasKnownKey) {
    return null;
  }

  return {
    embedded: typeof payload.embedded === "boolean" ? payload.embedded : undefined,
    sideMenu:
      payload.sideMenu === "expanded" || payload.sideMenu === "collapsed" || payload.sideMenu === "hidden"
        ? payload.sideMenu
        : undefined,
    accessToken: typeof payload.accessToken === "string" ? payload.accessToken : undefined,
    refreshToken: typeof payload.refreshToken === "string" ? payload.refreshToken : undefined,
    language: typeof payload.language === "string" ? payload.language : undefined,
  };
}

function maskToken(token) {
  if (typeof token !== "string" || token.length === 0) {
    return token;
  }
  if (token.length <= 12) {
    return "***";
  }
  return `${token.slice(0, 6)}...${token.slice(-4)}`;
}

function toLogSafePayload(payload) {
  if (!payload || typeof payload !== "object") {
    return payload;
  }

  return {
    ...payload,
    accessToken: maskToken(payload.accessToken),
    refreshToken: maskToken(payload.refreshToken),
  };
}

export function EmbeddedHostProvider({ children }) {
  const [hostConfig, setHostConfig] = useState(() => ({
    embedded: parseEmbeddedFromQuery(),
    sideMenu: "expanded",
    accessToken: null,
    refreshToken: null,
    language: null,
    sourceOrigin: null,
    hasHostMessage: false,
  }));

  useEffect(() => {
    const allowedOrigins = (process.env.REACT_APP_POSTMESSAGE_ALLOWED_ORIGINS || "")
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);

    const handleMessage = (event) => {
      if (allowedOrigins.length > 0 && !allowedOrigins.includes(event.origin)) {
        return;
      }

      const payload = normalizeHostPayload(event.data);
      if (!payload) {
        return;
      }

      // Logga sempre in console per troubleshooting dell'integrazione host -> iframe.
      console.info("[EmbeddedHostContext] postMessage ricevuto", {
        origin: event.origin,
        payload: toLogSafePayload(payload),
      });

      // Espone una coda log opzionale globale utile per debug automatici/e2e.
      if (!Array.isArray(window.__URBREATH_POSTMESSAGE_LOGS__)) {
        window.__URBREATH_POSTMESSAGE_LOGS__ = [];
      }
      window.__URBREATH_POSTMESSAGE_LOGS__.push({
        timestamp: new Date().toISOString(),
        origin: event.origin,
        payload: toLogSafePayload(payload),
      });

      setHostConfig((previous) => ({
        ...previous,
        ...Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined)),
        sourceOrigin: event.origin,
        hasHostMessage: true,
      }));
    };

    window.addEventListener("message", handleMessage);
    return () => {
      window.removeEventListener("message", handleMessage);
    };
  }, []);

  const contextValue = useMemo(() => hostConfig, [hostConfig]);
  return <EmbeddedHostContext.Provider value={contextValue}>{children}</EmbeddedHostContext.Provider>;
}

export function useEmbeddedHostConfig() {
  return useContext(EmbeddedHostContext);
}
