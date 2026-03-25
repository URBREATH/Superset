-- Dataset: D_URBREATH_GENERAL_SENSOR_LOCALIZATION
-- Chart: C_URBREATH_MADRID_SENSORS_LOCALIZATION
select 
l.id_location,
l.name as location_name,
l.locality,
l.latitude,
l.longitude,
s.id_sensor,
s.name as sensor_name,
p.id_param,
p.name as param_name,
p.units,
c.id_country,
c.name as country_name,
ct.id_city,
ct."name" as city_name,
z.id_zone,
z."name"  as zone_name,
z.geometry
from location l 
join sensor s on l.id_location = s.id_location
join parameter p on s.id_param  = p.id_param 
join city ct on l.id_city = ct.id_city
join country c on l.id_country = c.id_country
left join "zone" z on l.id_zone = z.id_zone
;


-- Dataset: D_URBREATH_GENERAL_MEASUREMENT
-- Chart: C_URBREATH_MADRID_NO2_YEARLY, C_URBREATH_MADRID_No2_PM25_Yearly_Comparison, C_URBREATH_MADRID_PM25_HEATMAP_YEARLY
-- 
select 
m.id_measure,
m."period", 
 m.date_from,
m.date_to,
m.min,
m.q02,
m.q24,
m.median,
m.q75,
m.q98,
m.max,
m.avg,
m.sd,
s.id_sensor,
s."name" as sensor_name,
s.latitude,
s.longitude,
p.id_param ,
p."name" as param_name,
p.display_name,
p.description,
et."name" as threshold_name,
et.value as threshold_value,
cy.id_country,
cy.name as country_name,
c.id_city,
c.name as city_name,
l.id_location,
l.name as location_name,
z.id_zone,
z.name as zone_name
from measurement m 
join sensor s on m.id_sensor = s.id_sensor
join parameter p on s.id_param = p.id_param
join location l on s.id_location = l.id_location
join city c on l.id_city = c.id_city
join country cy on c.id_country = cy.id_country
left join eu_threshold et on et.id_param  = p.id_param
left join zone z on l.id_zone = z.id_zone;

-- Dataset: D_URBREATH_GENERAL_MEASUREMENT_CHANGE_RATE
-- Chart: C_URBREATH_MADRID_NO2_PM25_CHANGE_RATE_YEARLY
WITH yearly_data AS (
    SELECT 
        m.id_measure,
        m."period", 
        m.date_from,
        m.date_to,
        EXTRACT(YEAR FROM m.date_from) AS year,
        m.avg,
        s.id_sensor,
        s."name" as sensor_name,
        s.latitude,
        s.longitude,
        p.id_param,
        p."name" as param_name,
        p.display_name,
        p.description,
        et."name" as threshold_name,
        et.value as threshold_value,
        l.id_location,
        c.id_city,
        c.name as city_name,
        cy.id_country,
        cy.name as country_name,
        z.id_zone,
        z.name as zone_name
    FROM measurement m 
    JOIN sensor s ON m.id_sensor = s.id_sensor
    JOIN parameter p ON s.id_param = p.id_param
    JOIN location l on s.id_location = l.id_location
    JOIN city c on l.id_city = c.id_city
    JOIN country cy on c.id_country = cy.id_country
    left join zone z on l.id_zone = z.id_zone
    LEFT JOIN eu_threshold et ON et.id_param = p.id_param
    WHERE m."period" = '1year' 
)
SELECT 
    yd.*,
    LAG(yd.avg) OVER (PARTITION BY yd.id_sensor, yd.id_param ORDER BY yd.year) AS prev_year_avg,
    (CASE 
        WHEN LAG(yd.avg) OVER (PARTITION BY yd.id_sensor, yd.id_param ORDER BY yd.year) IS NOT NULL AND LAG(yd.avg) OVER (PARTITION BY yd.id_sensor, yd.id_param ORDER BY yd.year) <> 0
        THEN (TRUNC((
            (yd.avg - LAG(yd.avg) OVER (PARTITION BY yd.id_sensor, yd.id_param ORDER BY yd.year)) 
            / LAG(yd.avg) OVER (PARTITION BY yd.id_sensor, yd.id_param ORDER BY yd.year) 
            
        )::numeric,2))
        ELSE null
    end) AS yoy_growth_percent
FROM yearly_data yd
ORDER BY yd.id_sensor, yd.year;