import React from "react";
import { Link } from "react-router-dom";
import "./Home.css";

function Home() {
  const cards = [
    {
      id: 1,
      title: "Madrid",
      subtitle: "Spain",
      description:(
        <>Discover how Madrid is tackling <b>mobility challenges</b>, advancing <b>climate adaptation</b>, and enhancing <b>biodiversity</b> in urban areas. The dashboards highlight initiatives to improve <b>livability and social justice</b>, while promoting <b>good governance</b> and <b>participatory planning</b> as key drivers of a more sustainable and inclusive city.</>
      ),
      image:
        "https://urbreath.virtualcitymap.de/stories/madrid/datasource-data/storyExample/assets/images/main.png",
      link: process.env.REACT_APP_DASHBOARD_MADRID_LINK ,
    },
    {
      id: 2,
      title: "Cluj-Napoca",
      subtitle: "Romania",
      description:(
        <>Explore how Cluj Napoca is addressing <b>mobility</b> to create a more connected and accessible city, while fostering <b>biodiversity</b> in urban and natural spaces. The dashboards also showcase strategies for strengthening <b>climate resilience</b>, ensuring the city can adapt and thrive in the face of environmental challenges.</>
      ),
      image:
        "https://urbreath.virtualcitymap.de/stories/cluj/datasource-data/storyExample/assets/images/main.png",
      link: process.env.REACT_APP_DASHBOARD_CLUJ_LINK,
    },
    {
      id: 3,
      title: "Tallinn",
      subtitle: "Estonia",
      description:(
        <>Tallinn is advancing efforts to reduce <b>environmental impacts and pollution</b>, while building strong strategies for <b>climate resilience</b>. The dashboards also explore initiatives that enhance <b>livability and social justice</b>, ensuring a healthier, fairer, and more inclusive urban environment.</>
      ),
      image:
        "https://urbreath.virtualcitymap.de/stories/tallinn/datasource-data/storyExample/assets/images/Main.png",
      link: process.env.REACT_APP_DASHBOARD_TALLINN_LINK,
    },
    {
      id: 4,
      title: "Leuven",
      subtitle: "Belgium",
      description:(
        <>Leuven is placing <b>biodiversity</b> at the heart of its urban strategies, promoting green spaces and ecological connectivity across the city. The dashboards highlight how these efforts support <b>climate resilience</b> and enhance <b>livability and social justice</b>, creating a healthier and more inclusive environment for all residents.</>
      ),
      image:
        "https://urbreath.virtualcitymap.de/stories/leuven/datasource-data/storyExample/assets/images/main.png",
      link: process.env.REACT_APP_DASHBOARD_LEUVEN_LINK,
    },
  ];

  const kpiCards = [
    {
      id: 5,
      city: "Cluj-Napoca",
      title: "KPI Environment - Noise",
      dashboardId: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_ENVIRONMENT,
      link: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_ENVIRONMENT_LINK,
    },
    {
      id: 6,
      city: "Cluj-Napoca",
      title: "KPI City Quality & Livability",
      dashboardId: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_QUALITY,
      link: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_QUALITY_LINK,
    },
    {
      id: 7,
      city: "Leuven",
      title: "KPI Mobility - Traffic",
      dashboardId: process.env.REACT_APP_DASHBOARD_LEUVEN_KPI_MOBILITY,
      link: process.env.REACT_APP_DASHBOARD_LEUVEN_KPI_MOBILITY_LINK,
    },
    {
      id: 8,
      city: "Tallinn",
      title: "KPI City Quality & Livability",
      dashboardId: process.env.REACT_APP_DASHBOARD_TALLINN_KPI_QUALITY,
      link: process.env.REACT_APP_DASHBOARD_TALLINN_KPI_QUALITY_LINK,
    },
  ].filter((card) => card.dashboardId && card.link);

  const cityDashboardLabel = "City data dashboard";

  return (
    <div className="home-container">
      <header className="home-header">
        <h1>Explore Data & KPI Dashboard</h1>
        <p>
          A journey through interactive dashboards that showcase key performance indicators and trends. Explore data visually to uncover insights, monitor progress, and support data-driven decision-making
        </p>
      </header>

      <div className="city-directory">
        <div className="city-card-grid">
          {cards.map((card) => (
            <article className="city-card" key={card.id}>
              <img src={card.image} alt="" className="card-image" />
              <div className="card-body">
                <small className="card-subtitle">{card.subtitle}</small>
                <h3 className="card-title">{card.title}</h3>
                <p className="card-description">{card.description}</p>
              </div>
            </article>
          ))}
        </div>
        <div className="city-actions-grid">
          {cards.map((card) => (
            <div className="city-actions" key={card.id}>
              {card.link && (
                <Link to={card.link} className="dashboard-button">
                  {cityDashboardLabel}
                  <span aria-hidden="true">→</span>
                </Link>
              )}
              {kpiCards
                .filter((kpi) => kpi.city === card.title)
                .map((kpi) => (
                  <Link to={kpi.link} key={kpi.id} className="dashboard-button">
                    {kpi.title}
                    <span aria-hidden="true">→</span>
                  </Link>
                ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Home;
