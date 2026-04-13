INSERT INTO public."attribute"
(id_attribute, attr_code, attr_desc, attr_type)
VALUES(12, 'FILE_PATH', NULL, 'STRING');

INSERT INTO public."attribute"
(id_attribute, attr_code, attr_desc, attr_type)
VALUES(13, 'ID_BATCH', NULL, 'NUMBER');



-- verifica dati inseriti
select  c.name , p.name, m.period, null as measure_type ,  count(*) from measurement m
join sensor s on m.id_sensor = s.id_sensor
join location l on s.id_location = l.id_location
join city c on l.id_city = c.id_city
join "parameter"  p on m.id_param = p.id_param
where p."name" not in ('precipitation', 'air_temperature')
group by  m.period, p.name, c.name  
union
select c.name , p.name, m.period, ma.attr_value_string , count(*) from measurement m
join measurement_attribute ma on m.id_measure = ma.id_measure  
join attribute at on ma.id_attribute = at.id_attribute and  at.attr_code ='MEASURE_TYPE' and  ma.attr_value_string ='PROJECTION' 
join sensor s on m.id_sensor = s.id_sensor
join location l on s.id_location = l.id_location
join city c on l.id_city = c.id_city
join "parameter"  p on m.id_param = p.id_param and  p."name"  in ('precipitation', 'air_temperature')  
group by c.name , p.name, m.period, ma.attr_value_string
union 
select c.name , p.name, m.period,  ma.attr_value_string , count(*) from measurement m
join measurement_attribute ma on m.id_measure = ma.id_measure  
join attribute at on ma.id_attribute = at.id_attribute and  at.attr_code ='MEASURE_TYPE' and  ma.attr_value_string ='ACTUAL'
join sensor s on m.id_sensor = s.id_sensor
join location l on s.id_location = l.id_location
join city c on l.id_city = c.id_city
join "parameter"  p on m.id_param = p.id_param  and p."name"  in ('precipitation', 'air_temperature')
group by c.name , p.name, m.period,  ma.attr_value_string order by 1,2 desc 

-- Leuven Temperature/Precipitation Location
INSERT INTO public."location"
(id_location, "name", locality, timezone, latitude, longitude, id_country, id_zone, id_city)
VALUES(9900020, '64580', 'Leuven', 'Europe/Brussels', 50.75861, 4.768333, 60, NULL, 4);

-- Leuven Temperature Sensor
INSERT INTO public.sensor
(id_sensor, "name", id_param, latitude, longitude, display_name, id_location)
VALUES(9900020, '°C', 9996, 50.75861, 4.768333, '°C', 9900020);

-- Leuven Precipitation Sensor
INSERT INTO public.sensor
(id_sensor, "name", id_param, latitude, longitude, display_name, id_location)
VALUES(9900040, 'mm', 19861, 50.75861, 4.768333, 'mm', 9900020);

-- Madrid Temperature/Precipitation Location
INSERT INTO public."location"
(id_location, "name", locality, timezone, latitude, longitude, id_country, id_zone, id_city)
VALUES(9900030, '3128C', 'Madrid', 'Europe/Brussels', 40.42111, -3.591389, 67, NULL, 1);

-- Madrid Temperature Sensor
INSERT INTO public.sensor
(id_sensor, "name", id_param, latitude, longitude, display_name, id_location)
VALUES(9900030, '°C', 9996, 40.42111, -3.591389, '°C', 9900030);

-- Madrid Precipitation Sensor
INSERT INTO public.sensor
(id_sensor, "name", id_param, latitude, longitude, display_name, id_location)
VALUES(9900050, 'mm', 19861, 40.42111, -3.591389, 'mm', 9900030);


-- Tallinn Precipitation Sensor
INSERT INTO public.sensor
(id_sensor, "name", id_param, latitude, longitude, display_name, id_location)
VALUES(9900060, 'mm', 19861, 59.3981, 24.6028, 'mm', 9900010);











