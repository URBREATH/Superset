import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardLeuvenKpiMobility() {
  return (
    <SupersetEmbed dashboardId={process.env.REACT_APP_DASHBOARD_LEUVEN_KPI_MOBILITY} />
  );
}

export default DashboardLeuvenKpiMobility;
