import React from "react";
import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import DashboardMadrid from "./pages/DashboardMadrid";
import DashboardClujNapoca from "./pages/DashboardClujNapoca";
import DashboardTallinn from "./pages/DashboardTallinn";
import DashboardLeuven from "./pages/DashboardLeuven";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/madrid" element={<DashboardMadrid />} />
      <Route path="/cluj-napoca" element={<DashboardClujNapoca />} />
      <Route path="/tallinn" element={<DashboardTallinn />} />
      <Route path="/leuven" element={<DashboardLeuven />} />
    </Routes>
  );
}

export default App;
