15143099999	9900006	mm
15120099999	9900005	mm
ROE00100902	9900004	mm
15143099999	9900003	°C
15120599999	9900002	°C
15120099999	9900001	°C
ROE00100902	9900000	°C
15120599999	9900007	mm

Cluj-Napoca/Climate Information/Observations/Temperature/15120099999_temperature.txt
Cluj-Napoca/Climate Information/Observations/Temperature/15120599999_temperature.txt
Cluj-Napoca/Climate Information/Observations/Temperature/15143099999_temperature.txt
Cluj-Napoca/Climate Information/Observations/Temperature/ROE00100902_temperature.txt

INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod) 
VALUES (2, '{"items":[{"key":"bucket","value":"urbreath-public-repo","type":"STRING"},
{"key":"objectKey","value":"Cluj-Napoca/Climate Information/Observations/Temperature/15120099999_temperature.txt","type":"STRING"},
{"key":"MEASURE_TYPE","value":"ACTUAL","type":"STRING"},
{"key":"PARAM_ID","value":"9996","type":"INTEGER"},
{"key":"SENSOR_ID","value":"9900001","type":"INTEGER"},
{"key":"PERIOD","value":"1day","type":"STRING"}]
}', 0, now(), null);

INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod) 
VALUES (2, '{"items":[{"key":"bucket","value":"urbreath-public-repo","type":"STRING"},
{"key":"objectKey","value":"Cluj-Napoca/Climate Information/Observations/Temperature/15120599999_temperature.txt","type":"STRING"},
{"key":"MEASURE_TYPE","value":"ACTUAL","type":"STRING"},
{"key":"PARAM_ID","value":"9996","type":"INTEGER"},
{"key":"SENSOR_ID","value":"9900007","type":"INTEGER"},
{"key":"PERIOD","value":"1day","type":"STRING"}]
}', 0, now(), null);

INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod) 
VALUES (2, '{"items":[{"key":"bucket","value":"urbreath-public-repo","type":"STRING"},
{"key":"objectKey","value":"Cluj-Napoca/Climate Information/Observations/Temperature/15143099999_temperature.txt","type":"STRING"},
{"key":"MEASURE_TYPE","value":"ACTUAL","type":"STRING"},
{"key":"PARAM_ID","value":"9996","type":"INTEGER"},
{"key":"SENSOR_ID","value":"9900003","type":"INTEGER"},
{"key":"PERIOD","value":"1day","type":"STRING"}]
}', 0, now(), null);

INSERT INTO public.batch_job_task_queue (id_batch, json_param, status, date_ins, date_mod) 
VALUES (2, '{"items":[{"key":"bucket","value":"urbreath-public-repo","type":"STRING"},
{"key":"objectKey","value":"Cluj-Napoca/Climate Information/Observations/Temperature/ROE00100902_temperature.txt","type":"STRING"},
{"key":"MEASURE_TYPE","value":"ACTUAL","type":"STRING"},
{"key":"PARAM_ID","value":"9996","type":"INTEGER"},
{"key":"SENSOR_ID","value":"9900000","type":"INTEGER"},
{"key":"PERIOD","value":"1day","type":"STRING"}]
}', 0, now(), null);
