-- ============================================================
-- URBreath - Survey responses DML (Tallinn)
-- Source : URBREATH_survey_translated.xlsx
-- City   : Tallinn  (id_city = 4)
-- Rows   : 17
-- Generated: 2026-06-03T11:37:33
--
-- IMPORTANT: run DDL script (ddl/20260603/script.sql) first.
--
-- JSON structure per row:
--   timestamp, respondent{gender, age_group},
--   visit{previous_visits, access_mode[]},
--   perception{appearance, overall_satisfaction, feels_safe, enough_leisure},
--   comments (optional)
-- ============================================================

-- 1. Survey campaign registry
INSERT INTO public.survey (name, id_city, date_start, date_end, note)
VALUES (
    'URBREATH Tallinn Survey',
    4,
    '2025-06-14',
    '2025-12-03',
    'Tallinn on-site survey – 17 respondents, Paljassaare area'
);

-- 2. Survey responses (17 rows)
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-14T11:08:40", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Visit often", "access_mode": ["By car"]}, "perception": {"appearance": "Neutral", "overall_satisfaction": "Not very satisfied", "feels_safe": "No", "enough_leisure": "No"}}'::jsonb); -- row 1
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-14T13:08:36", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Never visited", "access_mode": ["By tram"]}, "perception": {"appearance": "Unpleasant", "overall_satisfaction": "Not very satisfied", "feels_safe": "No", "enough_leisure": "No"}}'::jsonb); -- row 2
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-14T13:30:13", "respondent": {"gender": "Male", "age_group": "25-34"}, "visit": {"previous_visits": "Visit often", "access_mode": ["On foot", "By tram"]}, "perception": {"appearance": "Neutral", "overall_satisfaction": "Satisfied", "feels_safe": "No", "enough_leisure": "No"}}'::jsonb); -- row 3
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-15T13:27:53", "respondent": {"gender": "Female", "age_group": "15-24"}, "visit": {"previous_visits": "Never visited", "access_mode": ["On foot"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Cannot say", "feels_safe": "No", "enough_leisure": "No"}}'::jsonb); -- row 4
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-15T13:28:24", "respondent": {"gender": "Male", "age_group": "35-44"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["By tram", "By bike"]}, "perception": {"appearance": "Unpleasant", "overall_satisfaction": "Not very satisfied", "feels_safe": "No", "enough_leisure": "No"}}'::jsonb); -- row 5
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-06-17T21:30:33", "respondent": {"gender": "Female", "age_group": "45-54"}, "visit": {"previous_visits": "Visit often", "access_mode": ["On foot", "By tram"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Satisfied", "feels_safe": "No", "enough_leisure": "No"}, "comments": "Do not build it full!"}'::jsonb); -- row 6
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:45:52", "respondent": {"gender": "Other", "age_group": "45-54"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot", "By bike"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Satisfied", "feels_safe": "Yes", "enough_leisure": "No"}, "comments": "Do not build the area full"}'::jsonb); -- row 7
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:47:05", "respondent": {"gender": "Other", "age_group": "45-54"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot", "By bike"]}, "perception": {"appearance": "Very pleasant", "overall_satisfaction": "Very satisfied", "feels_safe": "Yes", "enough_leisure": "No"}, "comments": "I hope no lighting will be added"}'::jsonb); -- row 8
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:48:08", "respondent": {"gender": "Other", "age_group": "25-34"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot", "By car", "Scooter"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Very satisfied", "feels_safe": "Cannot say", "enough_leisure": "No"}}'::jsonb); -- row 9
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:49:13", "respondent": {"gender": "Other", "age_group": "35-44"}, "visit": {"previous_visits": "Visit often", "access_mode": ["On foot"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Satisfied", "feels_safe": "Yes", "enough_leisure": "No"}, "comments": "Beer vending machine"}'::jsonb); -- row 10
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:50:19", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot"]}, "perception": {"appearance": "Unpleasant", "overall_satisfaction": "Not very satisfied", "feels_safe": "No", "enough_leisure": "No"}, "comments": "It would be nice to preserve the area''s wild nature"}'::jsonb); -- row 11
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:51:04", "respondent": {"gender": "Female", "age_group": "45-54"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot"]}, "perception": {"appearance": "Unpleasant", "overall_satisfaction": "Cannot say", "feels_safe": "No", "enough_leisure": "No"}, "comments": "Do not build houses"}'::jsonb); -- row 12
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:52:00", "respondent": {"gender": "Other", "age_group": "35-44"}, "visit": {"previous_visits": "Never visited", "access_mode": ["On foot", "By tram"]}, "perception": {"appearance": "Neutral", "overall_satisfaction": "Very satisfied", "feels_safe": "Yes", "enough_leisure": "Cannot say"}, "comments": "Not participating"}'::jsonb); -- row 13
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:52:48", "respondent": {"gender": "Female", "age_group": "55-64"}, "visit": {"previous_visits": "Visit often", "access_mode": ["On foot"]}, "perception": {"appearance": "Very pleasant", "overall_satisfaction": "Satisfied", "feels_safe": "Yes", "enough_leisure": "Yes"}, "comments": "Preserve the wild coastal area!"}'::jsonb); -- row 14
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:53:26", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Visited occasionally", "access_mode": ["On foot", "By tram", "By bike"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Satisfied", "feels_safe": "Yes", "enough_leisure": "No"}}'::jsonb); -- row 15
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:54:34", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Never visited", "access_mode": ["By bike"]}, "perception": {"appearance": "Pleasant", "overall_satisfaction": "Very satisfied", "feels_safe": "Yes", "enough_leisure": "Cannot say"}, "comments": "Not yet familiar but happy with the initiative"}'::jsonb); -- row 16
INSERT INTO public.survey_response (id_survey, id_city, answers)
VALUES (2, 4, '{"timestamp": "2025-12-03T13:55:56", "respondent": {"gender": "Female", "age_group": "35-44"}, "visit": {"previous_visits": "Visit often", "access_mode": ["On foot", "By tram", "By car"]}, "perception": {"overall_satisfaction": "Cannot say", "feels_safe": "No"}, "comments": "Preserve urban nature and public access to the sea!"}'::jsonb); -- row 17

-- Total rows inserted: 17