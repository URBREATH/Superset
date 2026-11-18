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
      title: "KPI Environment",
      dashboardId: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_ENVIRONMENT,
      link: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_ENVIRONMENT_LINK,
    },
    {
      id: 6,
      city: "Cluj-Napoca",
      title: "KPI Quality",
      dashboardId: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_QUALITY,
      link: process.env.REACT_APP_DASHBOARD_CLUJ_KPI_QUALITY_LINK,
    },
    {
      id: 7,
      city: "Leuven",
      title: "KPI Mobility",
      dashboardId: process.env.REACT_APP_DASHBOARD_LEUVEN_KPI_MOBILITY,
      link: process.env.REACT_APP_DASHBOARD_LEUVEN_KPI_MOBILITY_LINK,
    },
    {
      id: 8,
      city: "Tallinn",
      title: "KPI Quality",
      dashboardId: process.env.REACT_APP_DASHBOARD_TALLINN_KPI_QUALITY,
      link: process.env.REACT_APP_DASHBOARD_TALLINN_KPI_QUALITY_LINK,
    },
  ].filter((card) => card.dashboardId && card.link);

  return (
    <div className="home-container">
      <header className="home-header">
        <h1>Explore Data & KPI Dashboard</h1>
        <p>
          A journey through interactive dashboards that showcase key performance indicators and trends. Explore data visually to uncover insights, monitor progress, and support data-driven decision-making
        </p>
      </header>

      <div className="card-grid">
        {cards.map((card) => {
          const cityKpiCards = kpiCards.filter((kpi) => kpi.city === card.title);

          return (
            <div className="city-column" key={card.id}>
              <Link to={card.link} className="card">
                <img src={card.image} alt={card.title} className="card-image" />
                <div className="card-body">
                  <small className="card-subtitle">{card.subtitle}</small>
                  <h3 className="card-title">{card.title}</h3>
                  <p className="card-description">{card.description}</p>
                </div>
              </Link>
              {cityKpiCards.length > 0 && (
                <div className="kpi-links" aria-label={`${card.title} KPI dashboards`}>
                  {cityKpiCards.map((kpi) => (
                    <Link to={kpi.link} key={kpi.id} className="kpi-link">
                      {kpi.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Home;
