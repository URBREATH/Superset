# -*- coding: utf-8 -*-
"""
URBreath - Generate DML INSERT statements from Tallinn survey Excel.

Structure (11 columns, 17 data rows):
  [0]  Timestamp
  [1]  Gender
  [2]  Age
  [3]  Previous visits to the area
  [4]  How do you access the area?   (multi-select, separated by "; ")
  [5]  Perception of the area's appearance
  [6]  Overall satisfaction
  [7]  Feels safe                    (Yes / No / Cannot say)
  [8]  Enough leisure opportunities  (Yes / No / Cannot say)
  [9]  Comments                      (free text, optional)
  [10] Email                         (omitted for privacy)

JSON structure produced:
{
  "timestamp": "2025-06-14T11:08:40",
  "respondent": {
    "gender"    : "Female",
    "age_group" : "35-44"
  },
  "visit": {
    "previous_visits" : "Visit often",
    "access_mode"     : ["On foot", "By tram"]
  },
  "perception": {
    "appearance"           : "Neutral",
    "overall_satisfaction" : "Not very satisfied",
    "feels_safe"           : "No",
    "enough_leisure"       : "No"
  },
  "comments": "Do not build it full!"   // omitted if null/empty
}
"""

import json
import openpyxl
from datetime import datetime

EXCEL_PATH = r"C:\Progetti\URBreath\Superset\survey\URBREATH_survey_translated.xlsx"
OUT_PATH   = r"C:\Progetti\URBreath\Superset\dml\20260603\script_tallinn.sql"

ID_SURVEY = 2   # Tallinn survey — must follow Cluj (id_survey = 1)
ID_CITY   = 4   # Tallinn


def parse_semicolon_list(val):
    """Split a semicolon-separated multi-select into a list."""
    if val is None:
        return None
    s = str(val).strip()
    if not s:
        return None
    return [x.strip() for x in s.split(';') if x.strip()]


def parse_timestamp(val):
    """Parse timestamp string or datetime to ISO format."""
    if val is None:
        return None
    if isinstance(val, datetime):
        return val.isoformat()
    s = str(val).strip()
    for fmt in ('%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S'):
        try:
            return datetime.strptime(s, fmt).isoformat()
        except ValueError:
            continue
    return s


def row_to_answers(row):
    doc = {}

    ts = parse_timestamp(row[0])
    if ts:
        doc['timestamp'] = ts

    # Respondent
    respondent = {}
    if row[1]: respondent['gender']    = row[1]
    if row[2]: respondent['age_group'] = row[2]
    if respondent:
        doc['respondent'] = respondent

    # Visit behaviour
    visit = {}
    if row[3]: visit['previous_visits'] = row[3]
    access = parse_semicolon_list(row[4])
    if access: visit['access_mode'] = access
    if visit:
        doc['visit'] = visit

    # Perception & satisfaction
    perception = {}
    if row[5]: perception['appearance']           = row[5]
    if row[6]: perception['overall_satisfaction'] = row[6]
    if row[7]: perception['feels_safe']           = row[7]
    if row[8]: perception['enough_leisure']       = row[8]
    if perception:
        doc['perception'] = perception

    # Free-text comment (col 9) — email (col 10) omitted for privacy
    if row[9] and str(row[9]).strip():
        doc['comments'] = str(row[9]).strip()

    return doc


def escape_sql_string(s):
    return s.replace("'", "''")


def generate_insert(row_index, answers_dict):
    answers_json    = json.dumps(answers_dict, ensure_ascii=False)
    answers_escaped = escape_sql_string(answers_json)
    return (
        f"INSERT INTO public.survey_response (id_survey, id_city, answers)\n"
        f"VALUES ({ID_SURVEY}, {ID_CITY}, '{answers_escaped}'::jsonb); "
        f"-- row {row_index}"
    )


def main():
    wb = openpyxl.load_workbook(EXCEL_PATH, read_only=True)
    ws = wb.active
    rows = list(ws.iter_rows(values_only=True))

    data_rows = [r for r in rows[1:] if any(v is not None for v in r)]

    lines = [
        "-- ============================================================",
        "-- URBreath - Survey responses DML (Tallinn)",
        "-- Source : URBREATH_survey_translated.xlsx",
        "-- City   : Tallinn  (id_city = 4)",
        f"-- Rows   : {len(data_rows)}",
        f"-- Generated: {datetime.now().isoformat(timespec='seconds')}",
        "--",
        "-- IMPORTANT: run DDL script (ddl/20260603/script.sql) first.",
        "--",
        "-- JSON structure per row:",
        "--   timestamp, respondent{gender, age_group},",
        "--   visit{previous_visits, access_mode[]},",
        "--   perception{appearance, overall_satisfaction, feels_safe, enough_leisure},",
        "--   comments (optional)",
        "-- ============================================================",
        "",
        "-- 1. Survey campaign registry",
        "INSERT INTO public.survey (name, id_city, date_start, date_end, note)",
        "VALUES (",
        "    'URBREATH Tallinn Survey',",
        f"    {ID_CITY},",
        "    '2025-06-14',",
        "    '2025-12-03',",
        "    'Tallinn on-site survey – 17 respondents, Paljassaare area'",
        ");",
        "",
        "-- 2. Survey responses (17 rows)",
    ]

    ok = 0
    for i, row in enumerate(data_rows, start=1):
        try:
            answers = row_to_answers(row)
            lines.append(generate_insert(i, answers))
            ok += 1
        except Exception as e:
            lines.append(f"-- ERROR on row {i}: {e}")

    lines.append("")
    lines.append(f"-- Total rows inserted: {ok}")

    import os
    os.makedirs(r"C:\Progetti\URBreath\Superset\dml\20260603", exist_ok=True)
    with open(OUT_PATH, "w", encoding="utf-8-sig") as f:
        f.write("\n".join(lines))

    print(f"Generated {ok} INSERT statements -> {OUT_PATH}")


if __name__ == "__main__":
    main()

