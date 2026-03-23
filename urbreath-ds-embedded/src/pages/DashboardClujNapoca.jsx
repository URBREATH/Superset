import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardClujNapoca() {
  return <SupersetEmbed dashboardId={`${process.env.REACT_APP_DASHBOARD_CLUJ}`} />;
}

export default DashboardClujNapoca;