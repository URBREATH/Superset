import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardTallinn() {
  return <SupersetEmbed dashboardId={`${process.env.REACT_APP_DASHBOARD_TALLINN}`} />;
}

export default DashboardTallinn;