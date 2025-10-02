import React, { useEffect } from "react";
import axios from "axios";
import { embedDashboard } from "@superset-ui/embedded-sdk";

function SupersetEmbed({ dashboardId }) {
  useEffect(() => {
    const supersetUrl = process.env.REACT_APP_SUPERSET_URL; // es. http://localhost:8088
    const supersetApiUrl = supersetUrl + "/api/v1/security";

    async function loadDashboard() {
      const login_body = {
        username: "admin",
        password: "admin",
        provider: "db",
        refresh: true,
      };

      const { data } = await axios.post(supersetApiUrl + "/login", login_body, {
        headers: { "Content-Type": "application/json" },
      });

      const access_token = data["access_token"];

      const guest_token_body = {
        user: {
          username: "report-viewer",
          first_name: "report-viewer",
          last_name: "report-viewer",
        },
        resources: [{ type: "dashboard", id: dashboardId }],
        rls: [],
      };

      const guestResp = await axios.post(
        supersetApiUrl + "/guest_token/",
        guest_token_body,
        { headers: { Authorization: "Bearer " + access_token } }
      );

      const guestToken = guestResp.data["token"];

      embedDashboard({
        id: dashboardId,
        supersetDomain: supersetUrl,
        mountPoint: document.getElementById("superset-container"),
        fetchGuestToken: () => guestToken,
        dashboardUiConfig: { hideTitle: true },
      });

            // 4️⃣ Imposta dimensioni iframe
      const iframe = document.querySelector("iframe");
      if (iframe) {
        iframe.style.width = "100%";
        iframe.style.minHeight = "100vh";
      }
    }

    loadDashboard();
  }, [dashboardId]);

  return <div id="superset-container"></div>;
}

export default SupersetEmbed;
