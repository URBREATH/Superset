-- ============================================================
-- URBreath – Survey response table
-- Date   : 2026-06-03
-- Notes  : Flat / non-relational design.
--          All answers for a single questionnaire submission
--          are stored as a JSONB document in the `answers` column.
--          Rows belonging to the same survey run share the same
--          id_survey value (e.g. Cluj First Survey ? id_survey = 1).
--
-- JSON STRUCTURE (semantic keys, nested by section):
-- {
--   "timestamp"  : "2025-10-09T12:04:12.226000",
--
--   "respondent": {
--     "gender"       : "Female",      -- Female|Male|other|prefer_not_to_say
--     "age_group"    : "25-34",       -- 16-24|25-34|35-44|45-54|55-64|65+
--     "occupation"   : "Employee",
--     "neighborhood" : "Some?eni"
--   },
--
--   "general": {
--     "nbs_familiarity"                : "Yes, I have a basic understanding",
--     "climate_impact_perception"      : 4,    -- scale 1-5
--     "climate_adaptation_familiarity" : 3,    -- scale 1-5
--     "aware_of_nbs_projects"          : "Yes",
--     "information_sources"            : ["Social media (Facebook, Instagram, etc.)", ...],
--     "green_space_frequency"          : "A few times a week",
--     "main_site"                      : "Site 4: ..."
--   },
--
--   "zones": [
--     -- Zone 1 ? present only for respondents who engaged with Site 1
--     {
--       "zone_id"   : "1",
--       "zone_name" : "Site 1 – Alexandru Sahia",
--       "problems"        : ["Pollution (air, noise) caused by traffic", ...],
--       "desired_changes" : ["Implementing nature-based solutions ...", ...],
--       "satisfaction": {
--         "satisfaction_neighborhood"         : 3,   -- scale 1-5
--         "satisfaction_safety"               : 3,
--         "relaxation_needs_met"              : 3,
--         "satisfaction_green_spaces_amount"  : 4,
--         "satisfaction_public_spaces_quality": 4,
--         "satisfaction_noise"                : 4,
--         "satisfaction_air_quality"          : 4,
--         "satisfaction_aesthetics"           : 4,
--         "satisfaction_waste_collection"     : 3,
--         "satisfaction_green_spaces_access"  : 3,
--         "satisfaction_disability_access"    : 3,
--         "satisfaction_public_transport"     : 4,
--         "satisfaction_pedestrian_infra"     : 4,
--         "satisfaction_cycling_infra"        : 3
--       },
--       "community": {
--         "neighbor_interaction_freq" : "Rarely/Never",
--         "outdoor_activities_freq"   : "Rarely",
--         "likely_turn_to_neighbor"   : "Very unlikely",
--         "sense_of_community"        : "No sense of community"
--       },
--       "mobility": {
--         "main_transport"             : "Public transport (bus, tram, metro, train)",
--         "walk_frequency"             : "Monthly",
--         "bike_frequency"             : "Monthly",
--         "public_transport_frequency" : "Monthly",
--         "car_frequency"              : "Rarely/Never",
--         "trip_purpose"               : ["Work/commute", "Leisure/recreation"],
--         "short_trip_preference"      : ["Bicycle", "Public transport"]
--       },
--       "qol_factors": ["The existence of green and recreational spaces", ...]
--     },
--     -- Zone 2 ? present only for respondents who engaged with Site 2
--     --          NOTE: satisfaction/community/mobility blocks are shared with
--     --          Zone 1 (single form section for the whole Site 1&2 area) and
--     --          are therefore duplicated here for query uniformity.
--     {
--       "zone_id"   : "2",
--       "zone_name" : "Site 2 – N?d??el",
--       "problems"        : ["Lack of landscaped green spaces", ...],
--       "desired_changes" : ["Creating green spaces and planting trees", ...],
--       "satisfaction"    : { ... },   -- same 14 keys as Zone 1
--       "community"       : { ... },
--       "mobility"        : { ... },
--       "qol_factors"     : [...]
--     },
--     -- Zone 3 ? present only for respondents who engaged with Site 3
--     {
--       "zone_id"   : "3",
--       "zone_name" : "Site 3 – Timi?ului",
--       "problems"        : ["Lack of an adequate pedestrian connection", ...],
--       "desired_changes" : ["Creating a new pedestrian connection", ...],
--       "satisfaction"    : { ... },
--       "community"       : { ... },
--       "mobility"        : { ... },
--       "qol_factors"     : [...]
--     },
--     -- Zone 4 ? present for ALL 67 respondents (Barc III – Some?ul Mic)
--     {
--       "zone_id"   : "4",
--       "zone_name" : "Site 4 – Barc III",
--       "problems"        : ["Lack of facilities for recreational activities", ...],
--       "desired_changes" : ["Creating community co-managed green spaces", ...],
--       "satisfaction"    : { ... },
--       "community"       : { ... },
--       "mobility"        : { ... },
--       "qol_factors"     : [...]
--     }
--   ]
-- }
--
-- USEFUL QUERY PATTERNS:
--
-- 1. Extract respondent demographics:
--    SELECT answers->'respondent'->>'age_group' AS age_group,
--           answers->'respondent'->>'gender'     AS gender
--    FROM survey_response WHERE id_survey = 1;
--
-- 2. Explode zone-level answers (one row per respondent × zone):
--    SELECT sr.id,
--           z->>'zone_id'                                        AS zone_id,
--           (z->'satisfaction'->>'satisfaction_air_quality')::int AS air_quality,
--           (z->'satisfaction'->>'satisfaction_noise')::int       AS noise
--    FROM survey_response sr,
--         jsonb_array_elements(sr.answers->'zones') AS z
--    WHERE sr.id_survey = 1;
--
-- 3. Count problems per zone:
--    SELECT z->>'zone_id'  AS zone_id,
--           p.problem,
--           COUNT(*)        AS mentions
--    FROM survey_response sr,
--         jsonb_array_elements(sr.answers->'zones')       AS z,
--         jsonb_array_elements_text(z->'problems')        AS p(problem)
--    WHERE sr.id_survey = 1
--    GROUP BY 1, 2
--    ORDER BY 1, 3 DESC;
--
-- 4. Average satisfaction per indicator per zone:
--    SELECT z->>'zone_id'                                         AS zone_id,
--           AVG((z->'satisfaction'->>'satisfaction_air_quality')::numeric) AS avg_air,
--           AVG((z->'satisfaction'->>'satisfaction_noise')::numeric)       AS avg_noise,
--           AVG((z->'satisfaction'->>'satisfaction_safety')::numeric)      AS avg_safety
--    FROM survey_response sr,
--         jsonb_array_elements(sr.answers->'zones') AS z
--    WHERE sr.id_survey = 1
--    GROUP BY 1;
--
-- 5. NBS familiarity by age group:
--    SELECT answers->'respondent'->>'age_group'  AS age_group,
--           answers->'general'->>'nbs_familiarity' AS nbs_familiarity,
--           COUNT(*) AS n
--    FROM survey_response WHERE id_survey = 1
--    GROUP BY 1, 2
--    ORDER BY 1, 3 DESC;
-- ============================================================

CREATE TABLE public.survey_response (
    id            bigserial    NOT NULL,
    id_survey     int8         NOT NULL,
    id_city       int8         NOT NULL,
    answers       jsonb        NOT NULL,
    date_ins      timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT survey_response_pkey
        PRIMARY KEY (id),
    CONSTRAINT survey_response_city_fk
        FOREIGN KEY (id_city) REFERENCES public.city(id_city)
);

-- Fast lookup by survey run
CREATE INDEX survey_response_id_survey_idx
    ON public.survey_response USING btree (id_survey);

-- Fast lookup by city
CREATE INDEX survey_response_id_city_idx
    ON public.survey_response USING btree (id_city);

-- GIN index on the JSON document – enables efficient querying
-- of individual answer keys (e.g. answers->>'age_group')
CREATE INDEX survey_response_answers_gin_idx
    ON public.survey_response USING gin (answers);

-- ============================================================
-- Survey registry – one row per survey campaign
-- ============================================================

CREATE TABLE public.survey (
    id_survey     bigserial    NOT NULL,
    name          text         NOT NULL,
    id_city       int8         NOT NULL,
    date_start    date         NULL,
    date_end      date         NULL,
    note          text         NULL,
    CONSTRAINT survey_pkey
        PRIMARY KEY (id_survey),
    CONSTRAINT survey_city_fk
        FOREIGN KEY (id_city) REFERENCES public.city(id_city)
);

-- ============================================================
-- Seed – Cluj-Napoca First Survey (2025)
-- ============================================================
-- INSERT INTO public.survey (name, id_city, date_start, date_end, note)
-- VALUES (
--     '[URBREATH] Cluj First Survey',
--     <id_city for Cluj-Napoca>,   -- replace with actual id_city
--     '2025-10-09',
--     '2025-11-06',
--     'First citizen survey for URBreath project – 67 respondents, 4 sites in Iris/Some?eni districts'
-- );

