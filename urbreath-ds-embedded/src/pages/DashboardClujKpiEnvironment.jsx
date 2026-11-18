import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardClujKpiEnvironment() {
  return (
    <SupersetEmbed
      dashboardId={process.env.REACT_APP_DASHBOARD_CLUJ_KPI_ENVIRONMENT}
    />
  );
}

export default DashboardClujKpiEnvironment;
