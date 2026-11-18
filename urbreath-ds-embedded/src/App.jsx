import React from "react";
import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import DashboardMadrid from "./pages/DashboardMadrid";
import DashboardClujNapoca from "./pages/DashboardClujNapoca";
import DashboardTallinn from "./pages/DashboardTallinn";
import DashboardLeuven from "./pages/DashboardLeuven";
import DashboardClujKpiEnvironment from "./pages/DashboardClujKpiEnvironment";
import DashboardClujKpiQuality from "./pages/DashboardClujKpiQuality";
import DashboardLeuvenKpiMobility from "./pages/DashboardLeuvenKpiMobility";
import DashboardTallinnKpiQuality from "./pages/DashboardTallinnKpiQuality";
import EmbeddedHostDebugBanner from "./components/EmbeddedHostDebugBanner";
import GoogleTranslator from "./components/GoogleTranslator";

function App() {
  return (
    <>
      <GoogleTranslator />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/madrid" element={<DashboardMadrid />} />
        <Route path="/cluj-napoca" element={<DashboardClujNapoca />} />
        <Route path="/tallinn" element={<DashboardTallinn />} />
        <Route path="/leuven" element={<DashboardLeuven />} />
        <Route
          path="/cluj-napoca/kpi/environment"
          element={<DashboardClujKpiEnvironment />}
        />
        <Route
          path="/cluj-napoca/kpi/quality"
          element={<DashboardClujKpiQuality />}
        />
        <Route
          path="/leuven/kpi/mobility"
          element={<DashboardLeuvenKpiMobility />}
        />
        <Route
          path="/tallinn/kpi/quality"
          element={<DashboardTallinnKpiQuality />}
        />
      </Routes>
      <EmbeddedHostDebugBanner />
    </>
  );
}

export default App;
