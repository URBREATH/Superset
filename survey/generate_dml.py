# -*- coding: utf-8 -*-
"""
URBreath - Generate DML INSERT statements from Cluj First Survey Excel.

Structure of the Excel (98 columns):
  [0]        Timestamp
  [1]        gender
  [2]        age_group
  [3]        occupation
  [4]        nbs_familiarity
  [5]        climate_impact_perception      (1-5)
  [6]        climate_adaptation_familiarity (1-5)
  [7]        aware_of_nbs_projects
  [8]        information_sources            (multi-select)
  [9]        neighborhood
  [10]       green_space_frequency
  [11]       main_site

  [12]       problems – Site 1 "Alexandru Sahia"        (multi-select)
  [13]       desired_changes – Site 1                    (multi-select)
  [14]       problems – Site 2 "Nădășel"                 (multi-select)
  [15]       desired_changes – Site 2                    (multi-select)
  [16-41]    Satisfaction/mobility block – Zone 1&2 (26 cols)

  [42]       problems – Site 3 "Timișului"               (multi-select)
  [43]       desired_changes – Site 3                    (multi-select)
  [44-69]    Satisfaction/mobility block – Zone 3   (26 cols)

  [70]       problems – Site 4 "Barc III"                (multi-select)
  [71]       desired_changes – Site 4                    (multi-select)
  [72-97]    Satisfaction/mobility block – Zone 4   (26 cols)

The satisfaction/mobility block (26 cols, same layout each zone):
  +0   satisfaction_neighborhood
  +1   satisfaction_safety
  +2   neighbor_interaction_freq
  +3   outdoor_activities_freq
  +4   likely_turn_to_neighbor
  +5   sense_of_community
  +6   relaxation_needs_met
  +7   satisfaction_green_spaces_amount
  +8   satisfaction_public_spaces_quality
  +9   satisfaction_noise
  +10  satisfaction_air_quality
  +11  satisfaction_aesthetics
  +12  satisfaction_waste_collection
  +13  qol_factors                    (multi-select)
  +14  satisfaction_green_spaces_access
  +15  satisfaction_disability_access
  +16  main_transport
  +17  walk_frequency
  +18  bike_frequency
  +19  public_transport_frequency
  +20  car_frequency
  +21  satisfaction_public_transport
  +22  satisfaction_pedestrian_infra
  +23  satisfaction_cycling_infra
  +24  trip_purpose                   (multi-select)
  +25  short_trip_preference          (multi-select)
"""

import json
import openpyxl
from datetime import datetime

EXCEL_PATH = r"C:\Progetti\URBreath\Superset\survey\[URBREATH] Cluj First Survey.xlsx"
OUT_PATH   = r"C:\Progetti\URBreath\Superset\dml\20260603\script.sql"

# id_survey and id_city for Cluj-Napoca First Survey
# These must match actual values in the DB after the survey seed is executed.
ID_SURVEY = 1
ID_CITY   = 1   # <-- replace with actual id_city for Cluj-Napoca

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

MULTI_SELECT_KEYS = {
    'information_sources', 'qol_factors', 'trip_purpose', 'short_trip_preference',
    'problems', 'desired_changes',
}

SAT_BLOCK_OFFSETS = {
    0:  'satisfaction_neighborhood',
    1:  'satisfaction_safety',
    2:  'neighbor_interaction_freq',
    3:  'outdoor_activities_freq',
    4:  'likely_turn_to_neighbor',
    5:  'sense_of_community',
    6:  'relaxation_needs_met',
    7:  'satisfaction_green_spaces_amount',
    8:  'satisfaction_public_spaces_quality',
    9:  'satisfaction_noise',
    10: 'satisfaction_air_quality',
    11: 'satisfaction_aesthetics',
    12: 'satisfaction_waste_collection',
    13: 'qol_factors',
    14: 'satisfaction_green_spaces_access',
    15: 'satisfaction_disability_access',
    16: 'main_transport',
    17: 'walk_frequency',
    18: 'bike_frequency',
    19: 'public_transport_frequency',
    20: 'car_frequency',
    21: 'satisfaction_public_transport',
    22: 'satisfaction_pedestrian_infra',
    23: 'satisfaction_cycling_infra',
    24: 'trip_purpose',
    25: 'short_trip_preference',
}

# Satisfaction keys (numeric scores)
SAT_SCORE_KEYS = {
    'satisfaction_neighborhood', 'satisfaction_safety', 'relaxation_needs_met',
    'satisfaction_green_spaces_amount', 'satisfaction_public_spaces_quality',
    'satisfaction_noise', 'satisfaction_air_quality', 'satisfaction_aesthetics',
    'satisfaction_waste_collection', 'satisfaction_green_spaces_access',
    'satisfaction_disability_access', 'satisfaction_public_transport',
    'satisfaction_pedestrian_infra', 'satisfaction_cycling_infra',
}

# Community/perception keys (string)
COMMUNITY_KEYS = {
    'neighbor_interaction_freq', 'outdoor_activities_freq',
    'likely_turn_to_neighbor', 'sense_of_community',
}

# Mobility keys
MOBILITY_KEYS = {
    'main_transport', 'walk_frequency', 'bike_frequency',
    'public_transport_frequency', 'car_frequency',
    'trip_purpose', 'short_trip_preference',
}


def parse_multiselect(val):
    """
    Split a comma-separated multi-select string into a list.
    Commas inside parentheses (e.g. "Public institutions (city hall, etc.)")
    are NOT treated as separators.
    Returns None if the value is empty.
    """
    if val is None:
        return None
    s = str(val).strip()
    if not s:
        return None

    parts = []
    current = []
    depth = 0
    i = 0
    while i < len(s):
        c = s[i]
        if c == '(':
            depth += 1
            current.append(c)
        elif c == ')':
            depth -= 1
            current.append(c)
        elif c == ',' and depth == 0:
            part = ''.join(current).strip()
            if part:
                parts.append(part)
            current = []
            # skip the space that follows the comma separator
            if i + 1 < len(s) and s[i + 1] == ' ':
                i += 1
        else:
            current.append(c)
        i += 1

    last = ''.join(current).strip()
    if last:
        parts.append(last)

    return parts if parts else None


def parse_sat_block(row, start_col):
    """Parse a 26-column satisfaction/mobility block starting at start_col."""
    sat = {}
    community = {}
    mobility = {}
    qol = None

    for offset, key in SAT_BLOCK_OFFSETS.items():
        idx = start_col + offset
        if idx >= len(row):
            continue
        val = row[idx]
        if val is None:
            continue

        if key in MULTI_SELECT_KEYS:
            parsed = parse_multiselect(val)
            if parsed:
                if key == 'qol_factors':
                    qol = parsed
                elif key in MOBILITY_KEYS:
                    mobility[key] = parsed
        elif key in SAT_SCORE_KEYS:
            sat[key] = val
        elif key in COMMUNITY_KEYS:
            community[key] = val
        elif key in MOBILITY_KEYS:
            mobility[key] = val

    result = {}
    if sat:
        result['satisfaction'] = sat
    if community:
        result['community'] = community
    if mobility:
        result['mobility'] = mobility
    if qol:
        result['qol_factors'] = qol

    return result if result else None


def build_zone(zone_id, zone_name, problems_val, changes_val, sat_block):
    """Build a zone dict with problems, desired_changes, and the sat block merged in."""
    zone = {'zone_id': zone_id, 'zone_name': zone_name}

    problems = parse_multiselect(problems_val)
    changes  = parse_multiselect(changes_val)
    if problems:
        zone['problems'] = problems
    if changes:
        zone['desired_changes'] = changes

    if sat_block:
        zone.update(sat_block)

    # Return zone only if it has meaningful data beyond zone_id/zone_name
    useful_keys = set(zone.keys()) - {'zone_id', 'zone_name'}
    return zone if useful_keys else None


def row_to_answers(row):
    """Convert one Excel data row (tuple) to the answers JSONB dict."""
    doc = {}

    # --- Timestamp ---
    ts = row[0]
    if isinstance(ts, datetime):
        doc['timestamp'] = ts.isoformat()

    # --- Respondent ---
    respondent = {}
    if row[1]:  respondent['gender']       = row[1]
    if row[2]:  respondent['age_group']    = row[2]
    if row[3]:  respondent['occupation']   = row[3]
    if row[9]:  respondent['neighborhood'] = row[9]
    if respondent:
        doc['respondent'] = respondent

    # --- General ---
    general = {}
    if row[4]:  general['nbs_familiarity']                = row[4]
    if row[5] is not None: general['climate_impact_perception']      = row[5]
    if row[6] is not None: general['climate_adaptation_familiarity'] = row[6]
    if row[7]:  general['aware_of_nbs_projects']          = row[7]
    sources = parse_multiselect(row[8])
    if sources: general['information_sources']            = sources
    if row[10]: general['green_space_frequency']          = row[10]
    if row[11]: general['main_site']                      = row[11]
    if general:
        doc['general'] = general

    # --- Zone 1 (Site 1 Alexandru Sahia) and Zone 2 (Site 2 Nădășel) ---
    # Both share the same satisfaction block (cols 16-41 in the Excel form).
    # The satisfaction data is duplicated on both zone objects so that every
    # zone has a uniform structure and can be queried identically.
    sat_12 = parse_sat_block(row, 16)
    p1 = parse_multiselect(row[12])
    c1 = parse_multiselect(row[13])
    p2 = parse_multiselect(row[14])
    c2 = parse_multiselect(row[15])

    zone_1_data = {}
    zone_2_data = {}

    if any([p1, c1, sat_12]):
        zone1 = {'zone_id': '1', 'zone_name': 'Site 1 – Alexandru Sahia'}
        if p1: zone1['problems'] = p1
        if c1: zone1['desired_changes'] = c1
        if sat_12:
            zone1.update(sat_12)
        zone_1_data = zone1

    if any([p2, c2, sat_12]):
        zone2 = {'zone_id': '2', 'zone_name': 'Site 2 – Nădășel'}
        if p2: zone2['problems'] = p2
        if c2: zone2['desired_changes'] = c2
        if sat_12:
            zone2.update(sat_12)
        zone_2_data = zone2

    # --- Zone 3 (Site 3 Timișului) ---
    sat_3 = parse_sat_block(row, 44)
    zone3  = build_zone('3', 'Site 3 – Timișului', row[42], row[43], sat_3)

    # --- Zone 4 (Site 4 Barc III) ---
    sat_4 = parse_sat_block(row, 72)
    zone4  = build_zone('4', 'Site 4 – Barc III', row[70], row[71], sat_4)

    zones = [z for z in [zone_1_data if zone_1_data else None,
                         zone_2_data if zone_2_data else None,
                         zone3, zone4] if z]
    if zones:
        doc['zones'] = zones

    return doc


def escape_sql_string(s):
    """Escape a string for use inside a PostgreSQL single-quoted literal."""
    return s.replace("'", "''")


def generate_insert(row_index, answers_dict):
    """Generate a single INSERT statement."""
    answers_json = json.dumps(answers_dict, ensure_ascii=False)
    answers_escaped = escape_sql_string(answers_json)
    return (
        f"INSERT INTO public.survey_response (id_survey, id_city, answers)\n"
        f"VALUES ({ID_SURVEY}, {ID_CITY}, '{answers_escaped}'::jsonb); "
        f"-- row {row_index}"
    )


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    wb = openpyxl.load_workbook(EXCEL_PATH, read_only=True)
    ws = wb.active
    rows = list(ws.iter_rows(values_only=True))

    header = rows[0]
    data_rows = rows[1:]  # skip header

    lines = [
        "-- ============================================================",
        "-- URBreath – Survey responses DML",
        "-- Source : [URBREATH] Cluj First Survey.xlsx",
        "-- City   : Cluj-Napoca",
        f"-- Rows   : {len(data_rows)}",
        f"-- Generated: {datetime.now().isoformat(timespec='seconds')}",
        "--",
        "-- IMPORTANT: run the DDL script (ddl/20260603/script.sql) first.",
        "-- Replace id_city value if different from the seed.",
        "-- ============================================================",
        "",
        "-- 1. Survey campaign registry",
        "INSERT INTO public.survey (name, id_city, date_start, date_end, note)",
        "VALUES (",
        "    '[URBREATH] Cluj First Survey',",
        f"    {ID_CITY},",
        "    '2025-10-09',",
        "    '2025-11-30',",
        "    'First citizen survey for URBreath project – 67 respondents, 4 sites in Iris/Someșeni districts'",
        ");",
        "",
        "-- 2. Survey responses (67 rows)",
    ]

    ok = 0
    for i, row in enumerate(data_rows, start=1):
        if all(v is None for v in row):
            continue
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

    print(f"Generated {ok} INSERT statements → {OUT_PATH}")


if __name__ == "__main__":
    main()

