-- ============================================
-- Section: Environment and Pollution
-- Description: Dataset and Charts
-- Dashboard Charts:
-- C_URBREATH_MADRID_SENSORS_LOCALIZATION
-- C_URBREATH_MADRID_ZONES_SENSORS_LOCALIZATION
-- C_URBREATH_MADRID_NO2_YEARLY
-- C_URBREATH_MADRID_No2_PM25_Yearly_Comparison
-- C_URBREATH_MADRID_PM25_HEATMAP_YEARLY
-- C_URBREATH_MADRID_NO2_PM25_CHANGE_RATE_YEARLY
-- ============================================


-- Dataset: D_URBREATH_MADRID_MAP_SENSORS_IN_ZONES
-- Chart: C_URBREATH_MADRID_MAP_SENSORS_IN_ZONES (used by C_URBREATH_MADRID_ZONES_SENSORS_LOCALIZATION)
-- Description: Sensor location map
select tmp.id_zone, tmp.zone_name, tmp.geometry, STRING_AGG((concat(tmp.param_name, ': ', tmp.contatore, ' sensor')), E',   ')  as sensors from (
select 
z.id_zone,
z."name" as zone_name,
z.geometry ,
p.id_param,
p."name" as param_name,
count(*) as contatore
from location l
join zone z on l.id_zone  = z.id_zone
join sensor s on s.id_location  = l.id_location 
join parameter p on s.id_param = p.id_param
join city c on l.id_city = c.id_city
join country cy on cy.id_country = c.id_country
where c.id_city = 1
group by z.id_zone, z."name", z.geometry, p.id_param, p.name) as tmp
group by tmp.id_zone, tmp.zone_name, tmp.geometry
;

-- Dataset: D_URBREATH_GENERAL_MEASUREMENT
-- Chart: C_URBREATH_MADRID_NO2_YEARLY
-- Description: No2 yearly distribution

-- Dataset: D_URBREATH_GENERAL_MEASUREMENT
-- Chart: C_URBREATH_MADRID_PM25_HEATMAP_YEARLY
-- Description: PM25 heatmap yearly

-- Dataset: D_URBREATH_GENERAL_MEASUREMENT_CHANGE_RATE
-- Chart: C_URBREATH_MADRID_NO2_PM25_CHANGE_RATE_YEARLY
-- Description: No2/PM25 yearly change rate

-- ============================================
-- Section: Climate and Resilience​
-- Description: Dataset e Grafici
-- ============================================

-- D_MADRID-AVG-PRECIPITATION-YEAR-ACTUAL