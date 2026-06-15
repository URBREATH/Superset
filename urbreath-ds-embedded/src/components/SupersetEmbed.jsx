import React, { useEffect, useRef } from "react";
import axios from "axios";
import { embedDashboard } from "@superset-ui/embedded-sdk";

function SupersetEmbed({ dashboardId }) {
  const containerRef = useRef(null);

  useEffect(() => {
    if (!dashboardId || dashboardId.startsWith("PLACEHOLDER")) return;

    const supersetUrl = process.env.REACT_APP_SUPERSET_URL;
    const supersetApiUrl = supersetUrl + "/api/v1/security";

    // fetchGuestToken deve essere async in Superset 6.x:
    // il SDK la richiama automaticamente quando il token sta per scadere
    const fetchGuestToken = async () => {
      const loginResp = await axios.post(
        supersetApiUrl + "/login",
        {
          username: "admin",
          password: "admin",
          provider: "db",
          refresh: true,
        },
        { headers: { "Content-Type": "application/json" } }
      );
      const access_token = loginResp.data["access_token"];

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
  }, [dashboardId]);

  return <div ref={containerRef} id="superset-container"></div>;
}

export default SupersetEmbed;
