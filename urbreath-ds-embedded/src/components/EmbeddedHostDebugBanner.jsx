import React from "react";
import { useEmbeddedHostConfig } from "../context/EmbeddedHostContext";

function EmbeddedHostDebugBanner() {
  const { hasHostMessage, sourceOrigin, embedded, sideMenu, language, accessToken, refreshToken } =
    useEmbeddedHostConfig();

  if (process.env.NODE_ENV !== "development") {
    return null;
  }

  const style = {
    position: "fixed",
    right: "12px",
    bottom: "12px",
    zIndex: 9999,
    maxWidth: "360px",
    background: "rgba(17, 24, 39, 0.92)",
    color: "#f9fafb",
    border: "1px solid #374151",
    borderRadius: "8px",
    padding: "10px 12px",
    fontFamily: "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace",
    fontSize: "12px",
    lineHeight: 1.4,
  };

  const valueStyle = { color: "#93c5fd" };

  return (
    <div style={style}>
      <div>
        postMessage: <span style={valueStyle}>{hasHostMessage ? "received" : "waiting"}</span>
      </div>
      <div>
        origin: <span style={valueStyle}>{sourceOrigin || "n/a"}</span>
      </div>
      <div>
        embedded: <span style={valueStyle}>{String(embedded)}</span>
      </div>
      <div>
        sideMenu: <span style={valueStyle}>{sideMenu || "n/a"}</span>
      </div>
      <div>
        language: <span style={valueStyle}>{language || "n/a"}</span>
      </div>
      <div>
        accessToken: <span style={valueStyle}>{accessToken ? "yes" : "no"}</span>
      </div>
      <div>
        refreshToken: <span style={valueStyle}>{refreshToken ? "yes" : "no"}</span>
      </div>
    </div>
  );
}

export default EmbeddedHostDebugBanner;
