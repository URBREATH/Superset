import React from "react";
import SupersetEmbed from "../components/SupersetEmbed";

function DashboardLeuven() {
  return <SupersetEmbed dashboardId={`${process.env.REACT_APP_DASHBOARD_LEUVEN}`} />;
}

export default DashboardLeuven;