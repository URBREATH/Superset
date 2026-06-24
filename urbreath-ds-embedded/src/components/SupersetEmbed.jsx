import React, { useEffect, useRef } from "react";
import axios from "axios";
import { embedDashboard } from "@superset-ui/embedded-sdk";
import { useEmbeddedHostConfig } from "../context/EmbeddedHostContext";

function SupersetEmbed({ dashboardId }) {
  const containerRef = useRef(null);
  const { accessToken: hostAccessToken } = useEmbeddedHostConfig();

  useEffect(() => {
    if (!dashboardId || dashboardId.startsWith("PLACEHOLDER")) return;

    const supersetUrl = process.env.REACT_APP_SUPERSET_URL;
    const supersetApiUrl = supersetUrl + "/api/v1/security";
    const technicalUsername = process.env.REACT_APP_SUPERSET_TECHNICAL_USERNAME;
    const technicalPassword = process.env.REACT_APP_SUPERSET_TECHNICAL_PASSWORD;

    // Usa il token ricevuto dal container host quando presente, altrimenti fallback locale.
    const getSupersetAccessToken = async () => {
      if (hostAccessToken) {
        return hostAccessToken;
      }

      if (!technicalUsername || !technicalPassword) {
        throw new Error(
          "Missing REACT_APP_SUPERSET_TECHNICAL_USERNAME / REACT_APP_SUPERSET_TECHNICAL_PASSWORD for embedded fallback login"
        );
      }

      const loginResp = await axios.post(
        supersetApiUrl + "/login",
        {
          username: technicalUsername,
          password: technicalPassword,
          provider: "db",
          refresh: true,
        },
        { headers: { "Content-Type": "application/json" } }
      );

      return loginResp.data["access_token"];
    };

    // fetchGuestToken deve essere async in Superset 6.x:
    // il SDK la richiama automaticamente quando il token sta per scadere
    const fetchGuestToken = async () => {
      const access_token = await getSupersetAccessToken();

      const guestResp = await axios.post(
        supersetApiUrl + "/guest_token/",
        {
          user: {
            username: "report-viewer",
            first_name: "report-viewer",
            last_name: "report-viewer",
          },
          resources: [{ type: "dashboard", id: dashboardId }],
          rls: [],
        },
        { headers: { Authorization: "Bearer " + access_token } }
      );
      return guestResp.data["token"];
    };

    async function loadDashboard() {
      if (!containerRef.current) return;

      await embedDashboard({
        id: dashboardId,
        supersetDomain: supersetUrl,
        mountPoint: containerRef.current,
        fetchGuestToken,
        dashboardUiConfig: { hideTitle: true },
      });

      // Imposta dimensioni iframe
      const iframe = containerRef.current.querySelector("iframe");
      if (iframe) {
        iframe.style.width = "100%";
        iframe.style.minHeight = "100vh";
        iframe.style.border = "none";
      }
    }

    loadDashboard().catch(console.error);
  }, [dashboardId, hostAccessToken]);

  return <div ref={containerRef} id="superset-container"></div>;
}

export default SupersetEmbed;
