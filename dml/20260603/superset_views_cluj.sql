-- ============================================================
-- URBreath – Superset Virtual Datasets / Views
-- Survey: Cluj-Napoca First Survey  (id_survey = 1)
-- Date  : 2026-06-03
--
-- Strategia: 5 VIEW che "appiattiscono" il JSONB in righe relazionali.
-- Ogni VIEW viene registrata come Dataset in Superset e alimenta uno
-- o più grafici. Nessun dato viene duplicato nella tabella sorgente.
--
-- INDICE GRAFICI
--   Chart 1  – Big Numbers hero (clima + NBS awareness)
--   Chart 2  – Heatmap soddisfazione  (zone x indicatore)
--   Chart 3  – Bar orizzontale problemi per zona
--   Chart 4  – Bar orizzontale cambiamenti desiderati per zona
--   Chart 5  – 100% Stacked Bar  NBS familiarity per fascia d'età
--   Chart 6  – Bar fattori QoL più citati
--   Chart 7  – Pie distribuzione quartieri / mezzo di trasporto
-- ============================================================

-- ============================================================
-- DROP views (ordine inverso rispetto alle dipendenze)
-- ============================================================
DROP VIEW IF EXISTS public.v_survey_cluj_satisfaction_unpivot;
DROP VIEW IF EXISTS public.v_survey_cluj_mobility;
DROP VIEW IF EXISTS public.v_survey_cluj_qol_factors;
DROP VIEW IF EXISTS public.v_survey_cluj_desired_changes;
DROP VIEW IF EXISTS public.v_survey_cluj_problems;
DROP VIEW IF EXISTS public.v_survey_cluj_satisfaction;
DROP VIEW IF EXISTS public.v_survey_cluj_respondent;
DROP VIEW IF EXISTS public.v_survey_cluj_mobility_modes
DROP VIEW IF EXISTS public.v_survey_cluj_community;

-- ============================================================
-- VIEW 1: v_survey_cluj_respondent
-- Una riga per rispondente – dati demografici + generali (flat)
-- Alimenta: Chart 1, 5, 7
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_respondent AS
SELECT
    sr.id                                                                        AS respondent_id,
    (sr.answers ->> 'timestamp')                                                 AS ts,
    sr.answers -> 'respondent' ->> 'gender'                                      AS gender,
    sr.answers -> 'respondent' ->> 'age_group'                                   AS age_group,
    sr.answers -> 'respondent' ->> 'occupation'                                  AS occupation,
    sr.answers -> 'respondent' ->> 'neighborhood'                                AS neighborhood,
    sr.answers -> 'general'   ->> 'nbs_familiarity'                              AS nbs_familiarity,
    (sr.answers -> 'general'  ->> 'climate_impact_perception')::int              AS climate_impact_perception,
    (sr.answers -> 'general'  ->> 'climate_adaptation_familiarity')::int         AS climate_adaptation_familiarity,
    sr.answers -> 'general'   ->> 'aware_of_nbs_projects'                        AS aware_of_nbs_projects,
    sr.answers -> 'general'   ->> 'green_space_frequency'                        AS green_space_frequency,
    sr.answers -> 'general'   ->> 'main_site'                                    AS main_site
FROM public.survey_response sr
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 2: v_survey_cluj_satisfaction
-- Una riga per rispondente × zona – 14 punteggi flat (scala 1-5)
-- Alimenta: Chart 2
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_satisfaction AS
SELECT
    sr.id                                                                                AS respondent_id,
    z ->> 'zone_id'                                                                      AS zone_id,
    z ->> 'zone_name'                                                                    AS zone_name,
    (z -> 'satisfaction' ->> 'satisfaction_air_quality')::int                            AS air_quality,
    (z -> 'satisfaction' ->> 'satisfaction_noise')::int                                  AS noise,
    (z -> 'satisfaction' ->> 'satisfaction_safety')::int                                 AS safety,
    (z -> 'satisfaction' ->> 'relaxation_needs_met')::int                                AS relaxation,
    (z -> 'satisfaction' ->> 'satisfaction_neighborhood')::int                           AS neighborhood,
    (z -> 'satisfaction' ->> 'satisfaction_green_spaces_amount')::int                    AS green_spaces_amount,
    (z -> 'satisfaction' ->> 'satisfaction_public_spaces_quality')::int                  AS public_spaces_quality,
    (z -> 'satisfaction' ->> 'satisfaction_aesthetics')::int                             AS aesthetics,
    (z -> 'satisfaction' ->> 'satisfaction_waste_collection')::int                       AS waste_collection,
    (z -> 'satisfaction' ->> 'satisfaction_green_spaces_access')::int                    AS green_spaces_access,
    (z -> 'satisfaction' ->> 'satisfaction_disability_access')::int                      AS disability_access,
    (z -> 'satisfaction' ->> 'satisfaction_public_transport')::int                       AS public_transport,
    (z -> 'satisfaction' ->> 'satisfaction_pedestrian_infra')::int                       AS pedestrian_infra,
    (z -> 'satisfaction' ->> 'satisfaction_cycling_infra')::int                          AS cycling_infra
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones') AS z
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 3: v_survey_cluj_satisfaction_unpivot
-- Versione "tall" di v_survey_cluj_satisfaction:
-- una riga per rispondente × zona × indicatore.
-- Usata direttamente come source per il Heatmap di Superset
-- (richiede X=zone_id, Y=indicator_label, Value=AVG(score)).
-- Alimenta: Chart 2
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_satisfaction_unpivot AS
SELECT
    s.respondent_id,
    s.zone_id,
    CASE s.zone_id
        WHEN '1' THEN 'Alexandru Sahia'
        WHEN '2' THEN 'N?d??el'
        WHEN '3' THEN 'Timi?ului'
        WHEN '4' THEN 'Barc III'
        ELSE s.zone_name
    END                  AS zone_label,
    t.indicator_label,
    t.score
FROM public.v_survey_cluj_satisfaction s
CROSS JOIN LATERAL (VALUES
    ('Air quality',              s.air_quality),
    ('Noise',                    s.noise),
    ('Safety',                   s.safety),
    ('Relaxation needs met',     s.relaxation),
    ('Neighborhood overall',     s.neighborhood),
    ('Green spaces – quantity',  s.green_spaces_amount),
    ('Public spaces quality',    s.public_spaces_quality),
    ('Aesthetics',               s.aesthetics),
    ('Waste collection',         s.waste_collection),
    ('Green spaces – access',    s.green_spaces_access),
    ('Disability access',        s.disability_access),
    ('Public transport',         s.public_transport),
    ('Pedestrian infra',         s.pedestrian_infra),
    ('Cycling infra',            s.cycling_infra)
) AS t(indicator_label, score)
WHERE t.score IS NOT NULL;


-- ============================================================
-- VIEW 4: v_survey_cluj_problems
-- Una riga per rispondente × zona × problema (multi-select esploso)
-- Alimenta: Chart 3
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_problems AS
SELECT
    sr.id       AS respondent_id,
    z ->> 'zone_id'   AS zone_id,
    z ->> 'zone_name' AS zone_name,
    p.problem
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones')  AS z,
     jsonb_array_elements_text(z -> 'problems')   AS p(problem)
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 5: v_survey_cluj_desired_changes
-- Una riga per rispondente × zona × cambiamento (multi-select esploso)
-- Alimenta: Chart 4
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_desired_changes AS
SELECT
    sr.id       AS respondent_id,
    z ->> 'zone_id'   AS zone_id,
    z ->> 'zone_name' AS zone_name,
    c.change
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones')         AS z,
     jsonb_array_elements_text(z -> 'desired_changes')   AS c(change)
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 6: v_survey_cluj_qol_factors
-- Una riga per rispondente × zona × fattore QoL citato
-- Alimenta: Chart 6
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_qol_factors AS
SELECT
    sr.id       AS respondent_id,
    z ->> 'zone_id'   AS zone_id,
    z ->> 'zone_name' AS zone_name,
    f.factor
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones')         AS z,
     jsonb_array_elements_text(z -> 'qol_factors')       AS f(factor)
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 7: v_survey_cluj_mobility
-- Una riga per rispondente × zona – dati di mobilità flat
-- Alimenta: Chart 7 (mezzo principale per zona)
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_mobility AS
SELECT
    sr.id                                                                   AS respondent_id,
    z ->> 'zone_id'                                                         AS zone_id,
    z ->> 'zone_name'                                                       AS zone_name,
    z -> 'mobility' ->> 'main_transport'                                    AS main_transport,
    z -> 'mobility' ->> 'walk_frequency'                                    AS walk_frequency,
    z -> 'mobility' ->> 'bike_frequency'                                    AS bike_frequency,
    z -> 'mobility' ->> 'public_transport_frequency'                        AS public_transport_frequency,
    z -> 'mobility' ->> 'car_frequency'                                     AS car_frequency
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones') AS z
WHERE sr.id_survey = 1;


-- ============================================================
-- VIEW 7: v_survey_cluj_mobility
-- Una riga per rispondente × zona – dati di mobilità NON FLAT, ma esplosi per mezzo di trasporto (multi-select)
-- Alimenta: Chart 7 (mezzo principale per zona)
-- ============================================================
CREATE OR REPLACE VIEW public.v_survey_cluj_mobility_modes AS
SELECT
    respondent_id,
    zone_id,
    zone_name,
    TRIM(mode) AS transport_mode
FROM public.v_survey_cluj_mobility,
     LATERAL regexp_split_to_table(main_transport, '\s*,\s*') AS mode
WHERE main_transport IS NOT NULL;

-- ============================================================
-- VIEW 8: v_survey_cluj_community
-- Una riga per rispondente × zona – dati comunità flat
-- Alimenta: Chart community (senso di comunità, interazione vicini, ecc.)
-- ============================================================
DROP VIEW IF EXISTS public.v_survey_cluj_community;
CREATE OR REPLACE VIEW public.v_survey_cluj_community AS
SELECT
    sr.id                                                       AS respondent_id,
    z ->> 'zone_id'                                             AS zone_id,
    CASE z ->> 'zone_id'
    WHEN '1' THEN 'Alexandru Sahia'
    WHEN '2' THEN 'N?d??el'
    WHEN '3' THEN 'Timi?ului'
    WHEN '4' THEN 'Barc III'
    ELSE z ->> 'zone_name'
END                                                         AS zone_label,
    z -> 'community' ->> 'sense_of_community'                  AS sense_of_community,
    z -> 'community' ->> 'likely_turn_to_neighbor'             AS likely_turn_to_neighbor,
    z -> 'community' ->> 'outdoor_activities_freq'             AS outdoor_activities_freq,
    z -> 'community' ->> 'neighbor_interaction_freq'           AS neighbor_interaction_freq
FROM public.survey_response sr,
     jsonb_array_elements(sr.answers -> 'zones') AS z
WHERE sr.id_survey = 1
  AND z -> 'community' IS NOT NULL;


-- ============================================================
-- ============================================================
-- QUERY DI RIFERIMENTO PER OGNI GRAFICO
-- (da usare come "Custom SQL" o per validare i dataset in Superset)
-- ============================================================
-- ============================================================


-- ------------------------------------------------------------
-- CHART 1a – Big Number: Average perceived climate impact
-- Dataset: v_survey_cluj_respondent
-- Superset type: Big Number
--   Metric        : AVG(climate_impact_perception)
--   Number Format : ,.1f          ?  shows  3.7
--   Subheader     : "/ 5  ·  Average perceived climate impact"
--
--   NOTE: Superset Big Number requires a numeric metric (applies f(x)).
--   There is no native way to embed "/ 5" inline with the number without
--   custom CSS. The subheader is the only supported way to add context text.
-- ------------------------------------------------------------
/*
SELECT climate_impact_perception
FROM public.v_survey_cluj_respondent;
-- Superset metric: AVG(climate_impact_perception)
-- Result: 3.7   ?  Number Format ",.1f"  ?  displays  3.7
--                   Subheader             ?  "/ 5  ·  Average perceived climate impact"
*/


-- ------------------------------------------------------------
-- CHART 1b – Big Number: % of respondents aware of NBS projects
-- Dataset: v_survey_cluj_respondent
-- Superset type: Big Number
--   Number Format (D3): ",.1%"
--   ? D3 ",.1%" multiplies the value ×100 automatically:
--     the query MUST return a fraction 0-1, not a percentage 0-100.
--   Subheader: "Aware of local NBS projects"
-- ------------------------------------------------------------
/*
SELECT
    ROUND(
        COUNT(*) FILTER (WHERE aware_of_nbs_projects = 'Yes')::numeric
        / NULLIF(COUNT(*) FILTER (WHERE aware_of_nbs_projects IS NOT NULL), 0),
    3) AS pct_nbs_aware
FROM public.v_survey_cluj_respondent;
-- Returns: 0.149   ?  format ",.1%"  ?  Big Number shows  14.9%
*/


-- ------------------------------------------------------------
-- CHART 2 – Heatmap: Soddisfazione media per zona × indicatore
-- Dataset: v_survey_cluj_satisfaction_unpivot
-- Superset type: Heatmap
--   X Axis  : zone_id
--   Y Axis  : indicator_label
--   Metric  : AVG(score)
--   Sort Y by value per leggibilità
-- ------------------------------------------------------------
/*
SELECT
    zone_id,
    zone_label,
    indicator_label,
    ROUND(AVG(score)::numeric, 2)  AS avg_score,
    COUNT(*)                        AS n_responses
FROM public.v_survey_cluj_satisfaction_unpivot
GROUP BY zone_id, zone_label, indicator_label
ORDER BY zone_id, avg_score;
-- X Axis: zone_label  (shows "Alexandru Sahia", "N?d??el", "Timi?ului", "Barc III")
*/


-- ------------------------------------------------------------
-- CHART 3 – Bar orizzontale: Top problemi per zona
-- Dataset: v_survey_cluj_problems
-- Superset type: Bar Chart (horizontal)
--   Dimension : problem
--   Breakdowns: zone_id   (oppure usare Native Filter)
--   Metric    : COUNT(*)
--   Limit     : TOP 10 per zona
-- ------------------------------------------------------------
/*
SELECT
    zone_id,
    zone_name,
    problem,
    COUNT(*)                                            AS mentions,
    COUNT(*) OVER (PARTITION BY zone_id)                AS total_zone_mentions,
    ROUND(100.0 * COUNT(*) / COUNT(DISTINCT respondent_id)
          OVER (PARTITION BY zone_id), 1)               AS pct_respondents
FROM public.v_survey_cluj_problems
GROUP BY zone_id, zone_name, problem
ORDER BY zone_id, mentions DESC;
*/


-- ------------------------------------------------------------
-- CHART 4 – Bar orizzontale: Cambiamenti desiderati per zona
-- Dataset: v_survey_cluj_desired_changes
-- Superset type: Bar Chart (horizontal)
--   Stessa configurazione di Chart 3
-- ------------------------------------------------------------
/*
SELECT
    zone_id,
    zone_name,
    change,
    COUNT(*)  AS mentions,
    ROUND(100.0 * COUNT(*) / COUNT(DISTINCT respondent_id)
          OVER (PARTITION BY zone_id), 1) AS pct_respondents
FROM public.v_survey_cluj_desired_changes
GROUP BY zone_id, zone_name, change
ORDER BY zone_id, mentions DESC;
*/


-- ------------------------------------------------------------
-- CHART 5 – 100% Stacked Bar: NBS familiarity per fascia d'età
-- Dataset: v_survey_cluj_respondent
-- Superset type: Bar Chart (stacked, percentage mode)
--   X Axis    : age_group
--   Breakdowns: nbs_familiarity
--   Metric    : COUNT(*)
--   Sort X    : 16-24 | 25-34 | 35-44 | 45-54 | 55-64 | 65+
-- ------------------------------------------------------------
/*
SELECT
    age_group,
    nbs_familiarity,
    COUNT(*)                                        AS n,
    ROUND(100.0 * COUNT(*)
          / SUM(COUNT(*)) OVER (PARTITION BY age_group), 1) AS pct_age_group
FROM public.v_survey_cluj_respondent
WHERE age_group IS NOT NULL
GROUP BY age_group, nbs_familiarity
ORDER BY age_group, n DESC;
*/


-- ------------------------------------------------------------
-- CHART 6 – Bar orizzontale: Fattori QoL più citati (globale)
-- Dataset: v_survey_cluj_qol_factors
-- Superset type: Bar Chart (horizontal) oppure Word Cloud
--   Dimension: factor
--   Metric   : COUNT(DISTINCT respondent_id)  ? conta persone, non coppie zona×persona
--   Limit    : TOP 10
-- ------------------------------------------------------------
/*
SELECT
    factor,
    COUNT(DISTINCT respondent_id)  AS respondents_citing,
    ROUND(100.0 * COUNT(DISTINCT respondent_id)
          / (SELECT COUNT(*) FROM public.v_survey_cluj_respondent), 1) AS pct_total
FROM public.v_survey_cluj_qol_factors
GROUP BY factor
ORDER BY respondents_citing DESC
LIMIT 15;
*/


-- ------------------------------------------------------------
-- CHART 7a – Pie: Distribuzione rispondenti per quartiere
-- Dataset: v_survey_cluj_respondent
-- Superset type: Pie Chart
--   Dimension: neighborhood
--   Metric   : COUNT(*)
-- ------------------------------------------------------------
/*
SELECT
    COALESCE(TRIM(neighborhood), 'Unknown') AS neighborhood,
    COUNT(*) AS n
FROM public.v_survey_cluj_respondent
GROUP BY 1
ORDER BY 2 DESC;
*/


-- ------------------------------------------------------------
-- CHART 7b – Bar: Mezzo principale per zona
-- Dataset: v_survey_cluj_mobility
-- Superset type: Bar Chart (stacked)
--   X Axis    : zone_id
--   Breakdowns: main_transport
--   Metric    : COUNT(DISTINCT respondent_id)
-- ------------------------------------------------------------
/*
SELECT
    zone_id,
    zone_name,
    main_transport,
    COUNT(DISTINCT respondent_id) AS n
FROM public.v_survey_cluj_mobility
WHERE main_transport IS NOT NULL
GROUP BY zone_id, zone_name, main_transport
ORDER BY zone_id, n DESC;
*/

