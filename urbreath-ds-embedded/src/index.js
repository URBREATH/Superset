import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { BrowserRouter } from "react-router-dom";
import { EmbeddedHostProvider } from "./context/EmbeddedHostContext";

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <EmbeddedHostProvider>
    <BrowserRouter basename="/app">
      <App />
    </BrowserRouter>
  </EmbeddedHostProvider>
);