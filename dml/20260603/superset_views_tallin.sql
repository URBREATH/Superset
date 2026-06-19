-- ============================================================
-- Survey Tallinn (id_city = 4) - Viste per grafici Superset
-- Regole:
-- 1) valori dal JSON
-- 2) se non presenti, includi tutte le opzioni del questionario
-- 3) includi sempre i vuoti come "No response"
-- ============================================================

-- -----------------------------
-- BASE: estrazione da JSON
-- -----------------------------
CREATE OR REPLACE VIEW public.v_tallinn_survey_base AS
SELECT
    sr.id,
    sr.id_city,
    (sr.answers->>'timestamp')::timestamp AS response_ts,
    NULLIF(BTRIM(sr.answers#>>'{respondent,gender}'), '') AS gender_raw,
    NULLIF(BTRIM(sr.answers#>>'{respondent,age_group}'), '') AS age_group_raw,
    NULLIF(BTRIM(sr.answers#>>'{visit,previous_visits}'), '') AS previous_visits_raw,
    NULLIF(BTRIM(sr.answers#>>'{perception,appearance}'), '') AS appearance_raw,
    NULLIF(BTRIM(sr.answers#>>'{perception,overall_satisfaction}'), '') AS overall_satisfaction_raw,
    NULLIF(BTRIM(sr.answers#>>'{perception,feels_safe}'), '') AS feels_safe_raw,
    NULLIF(BTRIM(sr.answers#>>'{perception,enough_leisure}'), '') AS enough_leisure_raw
FROM public.survey_response sr
WHERE sr.id_city = 4;


-- -----------------------------
-- 1) Gender distribution
-- Grafico: Bar chart / Pie
-- -----------------------------
CREATE OR REPLACE VIEW public.v_tallinn_chart_gender AS
WITH options AS (
    SELECT * FROM (VALUES
        ('Female', 1),
        ('Male', 2),
        ('Other', 3),
        ('Prefer not to say', 4),
        ('No response', 5)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN gender_raw IS NULL THEN 'No response'
            WHEN gender_raw IN ('Female', 'Male', 'Other', 'Prefer not to say') THEN gender_raw
            ELSE 'Other'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a ON a.option_label = o.option_label
ORDER BY o.option_order;


-- -----------------------------
-- 2) Previous visits
-- Grafico: Bar chart ordinato
-- -----------------------------
CREATE OR REPLACE VIEW public.v_tallinn_chart_previous_visits AS
WITH options AS (
    SELECT * FROM (VALUES
        ('I have never visited it', 1),
        ('I have visited it a few times', 2),
        ('I visit it frequently', 3),
        ('No response', 4)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN previous_visits_raw IS NULL THEN 'No response'
            WHEN previous_visits_raw = 'Never visited' THEN 'I have never visited it'
            WHEN previous_visits_raw = 'Visited occasionally' THEN 'I have visited it a few times'
            WHEN previous_visits_raw = 'Visit often' THEN 'I visit it frequently'
            WHEN previous_visits_raw IN (
                'I have never visited it',
                'I have visited it a few times',
                'I visit it frequently'
            ) THEN previous_visits_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a ON a.option_label = o.option_label
ORDER BY o.option_order;


-- -----------------------------
-- 3) Overall satisfaction
-- Grafico: Bar chart Likert
-- -----------------------------
CREATE OR REPLACE VIEW public.v_tallinn_chart_overall_satisfaction AS
WITH options AS (
    SELECT * FROM (VALUES
        ('Very satisfied', 1),
        ('Satisfied', 2),
        ('No opinion / Not sure', 3),
        ('Not very satisfied', 4),
        ('Not satisfied at all', 5),
        ('No response', 6)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN overall_satisfaction_raw IS NULL THEN 'No response'
            WHEN overall_satisfaction_raw = 'Cannot say' THEN 'No opinion / Not sure'
            WHEN overall_satisfaction_raw IN (
                'Very satisfied',
                'Satisfied',
                'No opinion / Not sure',
                'Not very satisfied',
                'Not satisfied at all'
            ) THEN overall_satisfaction_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a ON a.option_label = o.option_label
ORDER BY o.option_order;


-- -----------------------------
-- 4) Leisure opportunities
-- Grafico: Bar chart
-- -----------------------------
CREATE OR REPLACE VIEW public.v_tallinn_chart_enough_leisure AS
WITH options AS (
    SELECT * FROM (VALUES
        ('Yes', 1),
        ('No', 2),
        ('Not sure', 3),
        ('No response', 4)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN enough_leisure_raw IS NULL THEN 'No response'
            WHEN enough_leisure_raw = 'Cannot say' THEN 'Not sure'
            WHEN enough_leisure_raw IN ('Yes', 'No', 'Not sure') THEN enough_leisure_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a ON a.option_label = o.option_label
ORDER BY o.option_order;


CREATE OR REPLACE VIEW public.v_tallinn_chart_access_mode AS
WITH options AS (
    SELECT * FROM (
        VALUES
            ('On foot', 1),
            ('By bike', 2),
            ('By tram', 3),
            ('By car', 4),
            ('Scooter', 5),
            ('No response', 6)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        access_mode AS option_label,
        COUNT(*)::int AS responses
    FROM (
        SELECT
            jsonb_array_elements_text(
                COALESCE(
                    sr.answers#>'{visit,access_mode}',
                    '[]'::jsonb
                )
            ) AS access_mode
        FROM public.survey_response sr
        WHERE sr.id_city = 4
    ) x
    GROUP BY access_mode

    UNION ALL

    SELECT
        'No response',
        COUNT(*)::int
    FROM public.survey_response sr
    WHERE sr.id_city = 4
      AND (
            sr.answers#>'{visit,access_mode}' IS NULL
            OR jsonb_array_length(sr.answers#>'{visit,access_mode}') = 0
      )
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a
                   ON a.option_label = o.option_label
ORDER BY o.option_order;



CREATE OR REPLACE VIEW public.v_tallinn_chart_appearance AS
WITH options AS (
    SELECT * FROM (VALUES
        ('Very pleasant', 1),
        ('Pleasant', 2),
        ('Neutral', 3),
        ('Unpleasant', 4),
        ('Very unpleasant', 5),
        ('No response', 6)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN appearance_raw IS NULL THEN 'No response'
            WHEN appearance_raw IN (
                'Very pleasant',
                'Pleasant',
                'Neutral',
                'Unpleasant',
                'Very unpleasant'
            ) THEN appearance_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a
                   ON a.option_label = o.option_label
ORDER BY o.option_order;



CREATE OR REPLACE VIEW public.v_tallinn_chart_feels_safe AS
WITH options AS (
    SELECT * FROM (VALUES
        ('Yes', 1),
        ('No', 2),
        ('Not sure', 3),
        ('No response', 4)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN feels_safe_raw IS NULL THEN 'No response'
            WHEN feels_safe_raw = 'Cannot say' THEN 'Not sure'
            WHEN feels_safe_raw IN ('Yes', 'No', 'Not sure') THEN feels_safe_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a
                   ON a.option_label = o.option_label
ORDER BY o.option_order;



CREATE OR REPLACE VIEW public.v_tallinn_chart_age_group AS
WITH options AS (
    SELECT * FROM (VALUES
        ('15-24', 1),
        ('25-34', 2),
        ('35-44', 3),
        ('45-54', 4),
        ('55-64', 5),
        ('65+', 6),
        ('No response', 7)
    ) AS t(option_label, option_order)
),
agg AS (
    SELECT
        CASE
            WHEN age_group_raw IS NULL THEN 'No response'
            WHEN age_group_raw IN (
                '15-24',
                '25-34',
                '35-44',
                '45-54',
                '55-64',
                '65+'
            ) THEN age_group_raw
            ELSE 'No response'
        END AS option_label,
        COUNT(*)::int AS responses
    FROM public.v_tallinn_survey_base
    GROUP BY 1
)
SELECT
    o.option_label,
    o.option_order,
    COALESCE(a.responses, 0) AS responses
FROM options o
         LEFT JOIN agg a
                   ON a.option_label = o.option_label
ORDER BY o.option_order;