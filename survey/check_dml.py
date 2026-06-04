# -*- coding: utf-8 -*-
import json
with open(r'C:\Progetti\URBreath\Superset\dml\20260603\script.sql', encoding='utf-8-sig') as f:
    lines = f.readlines()

# row 1 is on line index ~22 (after header + survey insert)
for line in lines:
    if '-- row 1' in line:
        # extract json between first ' and '::jsonb
        start = line.index("'") + 1
        end   = line.rindex("'::jsonb")
        doc   = json.loads(line[start:end])
        for z in doc['zones']:
            print('zone_id:', z['zone_id'], '| zone_name:', z['zone_name'])
            print('  problems:', z.get('problems', '–'))
            print('  satisfaction keys:', list(z.get('satisfaction', {}).keys())[:3], '...')
            print()
        break


