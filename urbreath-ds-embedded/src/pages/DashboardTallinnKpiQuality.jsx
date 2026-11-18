import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardTallinnKpiQuality() {
  return (
    <SupersetEmbed dashboardId={process.env.REACT_APP_DASHBOARD_TALLINN_KPI_QUALITY} />
  );
}

export default DashboardTallinnKpiQuality;
