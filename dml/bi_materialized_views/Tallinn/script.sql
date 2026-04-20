CREATE MATERIALIZED VIEW mv_bi_tallinn_daily_avg_precipitation_projection AS
 select
    date_part('year', date_from) as anno,
    to_char(date_from, 'MM') || '-' || trim(to_char(date_from, 'TMMonth')) as mese,
    date_trunc('day', date_from)::date as giorno,
    m.metadata->>'MEASURE_SIMULATION_COD_SCENARIO' as measure_cod_scenario,
    m.metadata->>'MEASURE_SIMULATION_SOURCE' as measure_projection_model,
    avg(m.val) as val_giorno
  from measurement m
  join sensor s on s.id_sensor = m.id_sensor
  join location l on s.id_location = l.id_location
  join city c on l.id_city = c.id_city
  join parameter p on m.id_param = p.id_param
  where
    p."name" = 'precipitation'
    and c."name" = 'Tallinn'
    and m.metadata->>'MEASURE_TYPE' ='PROJECTION'
  group by
    date_part('year', date_from),
    to_char(date_from, 'MM') || '-' || trim(to_char(date_from, 'TMMonth')),
    date_trunc('day', date_from)::date,
    m.metadata->>'MEASURE_SIMULATION_COD_SCENARIO',
    m.metadata->>'MEASURE_SIMULATION_SOURCE'
 ;
 
 CREATE MATERIALIZED VIEW mv_bi_tallinn_avg_precipitation_projection AS
select 
  x.scenario,
  case
    when anno between 1985 and 2014 then '1985–2014'
    when anno between 2021 and 2050 then '2021–2050'
    when anno between 2041 and 2070 then '2041–2070'
    when anno between 2071 and 2100 then '2071–2100'
    else 'Other'
  end as periodo,
  anno,
  sum(avg_precipitation) as tot_precipitation_year
from (
select 
  bi.measure_cod_scenario as scenario,
  bi.measure_cod_scenario as scenario1,
  anno,
  giorno,
  avg(bi.val_giorno) as avg_precipitation
from mv_bi_tallinn_daily_avg_precipitation_projection bi
group by 
  bi.measure_cod_scenario,
  giorno,
  anno
) as x 
group by 
  x.scenario,
  anno
order by periodo, scenario, anno
;



CREATE MATERIALIZED VIEW mv_bi_tallinn_daily_avg_temperature_projection AS
 select
    date_part('year', date_from) as anno,
    to_char(date_from, 'MM') || '-' || trim(to_char(date_from, 'TMMonth')) as mese,
    date_trunc('day', date_from)::date as giorno,
    m.metadata->>'MEASURE_SIMULATION_COD_SCENARIO' as measure_cod_scenario,
    m.metadata->>'MEASURE_SIMULATION_SOURCE' as measure_projection_model,
    avg(m.avg) as avg_temperature
  from measurement m
  join sensor s on s.id_sensor = m.id_sensor
  join location l on s.id_location = l.id_location
  join city c on l.id_city = c.id_city
  join parameter p on s.id_param = p.id_param
  where
    p."name" = 'air_temperature'
    and c."name" = 'Tallinn'
    and m.metadata->>'MEASURE_TYPE' ='PROJECTION'
  group by
    date_part('year', date_from),
    to_char(date_from, 'MM') || '-' || trim(to_char(date_from, 'TMMonth')),
    date_trunc('day', date_from)::date,
    m.metadata->>'MEASURE_SIMULATION_COD_SCENARIO',
    m.metadata->>'MEASURE_SIMULATION_SOURCE'
 ;
 
 CREATE MATERIALIZED VIEW mv_bi_tallinn_avg_temperature_projection AS
select 
  x.scenario,
  case
    when anno between 1985 and 2014 then '1985–2014'
    when anno between 2021 and 2050 then '2021–2050'
    when anno between 2041 and 2070 then '2041–2070'
    when anno between 2071 and 2100 then '2071–2100'
    else 'Other'
  end as periodo,
  anno,
  avg(avg_temperature) as avg_temperature_year
from (
select 
  bi.measure_cod_scenario as scenario,
  bi.measure_cod_scenario as scenario1,
  anno,
  giorno,
  avg(avg_temperature) as avg_temperature
from mv_bi_tallinn_daily_avg_temperature_projection bi
group by 
  bi.measure_cod_scenario,
  giorno,
  anno
) as x 
group by 
  x.scenario,
  anno
order by periodo, scenario, anno
;