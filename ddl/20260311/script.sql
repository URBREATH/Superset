CREATE TABLE public.batch_job_task_queue (
	id bigserial NOT NULL,
	id_batch int4 NULL,
	json_param json NULL,
	status int4 NULL,
	date_ins timestamp NOT NULL,
	date_mod timestamp NULL,
	note text NULL,
	CONSTRAINT batch_job_task_queue_pkey PRIMARY KEY (id)
);



CREATE TABLE public.traffic_measurement (
	id_traffic_measure int8 NOT NULL,
	segment_id varchar(50) NOT NULL,
	"date" timestamp NOT NULL,
	"interval" varchar(20) NULL,
	uptime numeric NULL,
	direction numeric NULL,
	timezone varchar(50) NULL,
	heavy numeric NULL,
	car numeric NULL,
	bike numeric NULL,
	pedestrian numeric NULL,
	night numeric NULL,
	heavy_lft numeric NULL,
	heavy_rgt numeric NULL,
	car_lft numeric NULL,
	car_rgt numeric NULL,
	bike_lft numeric NULL,
	bike_rgt numeric NULL,
	pedestrian_lft numeric NULL,
	pedestrian_rgt numeric NULL,
	night_lft numeric NULL,
	night_rgt numeric NULL,
	v85 numeric NULL,
	CONSTRAINT traffic_measurement_pkey PRIMARY KEY (id_traffic_measure)
);



CREATE TABLE public.traffic_speed_histogram (
	id bigserial NOT NULL,
	id_traffic_measure int8 NULL,
	histogram_type varchar(50) NULL,
	bucket_index int4 NULL,
	value numeric NULL,
	CONSTRAINT traffic_speed_histogram_pkey PRIMARY KEY (id),
	CONSTRAINT traffic_speed_histogram_id_traffic_measure_fkey FOREIGN KEY (id_traffic_measure) REFERENCES public.traffic_measurement(id_traffic_measure)
);


CREATE TABLE public.batch_job_execution_params (
	job_execution_id int8 NOT NULL,
	parameter_name varchar(100) NOT NULL,
	parameter_type varchar(100) NOT NULL,
	parameter_value varchar(2500) NULL,
	identifying bpchar(1) NOT NULL,
	CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);

-- public.batch_job_execution_context definition

-- Drop table

-- DROP TABLE public.batch_job_execution_context;

CREATE TABLE public.batch_job_execution_context (
	job_execution_id int8 NOT NULL,
	short_context varchar(2500) NOT NULL,
	serialized_context text NULL,
	CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id),
	CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);

CREATE TABLE public.batch_job_execution (
	job_execution_id int8 NOT NULL,
	"version" int8 NULL,
	job_instance_id int8 NOT NULL,
	create_time timestamp NOT NULL,
	start_time timestamp NULL,
	end_time timestamp NULL,
	status varchar(10) NULL,
	exit_code varchar(2500) NULL,
	exit_message varchar(2500) NULL,
	last_updated timestamp NULL,
	CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id),
	CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES public.batch_job_instance(job_instance_id)
);

CREATE TABLE public.batch_job_instance (
	job_instance_id int8 NOT NULL,
	"version" int8 NULL,
	job_name varchar(100) NOT NULL,
	job_key varchar(32) NOT NULL,
	CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
	CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE public.batch_step_execution (
	step_execution_id int8 NOT NULL,
	"version" int8 NOT NULL,
	step_name varchar(100) NOT NULL,
	job_execution_id int8 NOT NULL,
	create_time timestamp NOT NULL,
	start_time timestamp NULL,
	end_time timestamp NULL,
	status varchar(10) NULL,
	commit_count int8 NULL,
	read_count int8 NULL,
	filter_count int8 NULL,
	write_count int8 NULL,
	read_skip_count int8 NULL,
	write_skip_count int8 NULL,
	process_skip_count int8 NULL,
	rollback_count int8 NULL,
	exit_code varchar(2500) NULL,
	exit_message varchar(2500) NULL,
	last_updated timestamp NULL,
	CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id),
	CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);


CREATE TABLE public.batch_step_execution_context (
	step_execution_id int8 NOT NULL,
	short_context varchar(2500) NOT NULL,
	serialized_context text NULL,
	CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id),
	CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES public.batch_step_execution(step_execution_id)
);

CREATE SEQUENCE public.batch_job_execution_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

	CREATE SEQUENCE public.batch_job_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

	CREATE SEQUENCE public.batch_job_task_queue_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

	CREATE SEQUENCE public.batch_step_execution_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;