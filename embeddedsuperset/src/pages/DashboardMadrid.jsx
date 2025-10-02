import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardMadrid() {
  return <SupersetEmbed dashboardId={`${process.env.REACT_APP_DASHBOARD_MADRID}`} />;
}

export default DashboardMadrid;