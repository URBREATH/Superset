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
