--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9 (Debian 16.9-1.pgdg120+1)
-- Dumped by pg_dump version 17.0

-- Started on 2025-06-03 16:20:02

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE urbreath_dev;
--
-- TOC entry 3400 (class 1262 OID 16388)
-- Name: urbreath_dev; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE urbreath_dev WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE urbreath_dev OWNER TO postgres;

\connect urbreath_dev

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 16509)
-- Name: eu_threshold; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eu_threshold (
    id_threshold bigint NOT NULL,
    name text,
    id_param bigint NOT NULL,
    value double precision
);


ALTER TABLE public.eu_threshold OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16466)
-- Name: location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location (
    id_location bigint NOT NULL,
    name text,
    locality text,
    timezone text,
    latitude double precision,
    longitude double precision
);


ALTER TABLE public.location OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16491)
-- Name: measurement; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.measurement (
    id_measure bigint NOT NULL,
    id_param bigint NOT NULL,
    id_sensor bigint NOT NULL,
    period text,
    date_from timestamp without time zone,
    date_to timestamp without time zone,
    min double precision,
    q02 double precision,
    q24 double precision,
    median double precision,
    q75 double precision,
    q98 double precision,
    max double precision,
    avg double precision,
    sd double precision
);


ALTER TABLE public.measurement OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16490)
-- Name: measurement_id_measure_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.measurement_id_measure_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.measurement_id_measure_seq OWNER TO postgres;

--
-- TOC entry 3401 (class 0 OID 0)
-- Dependencies: 220
-- Name: measurement_id_measure_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.measurement_id_measure_seq OWNED BY public.measurement.id_measure;


--
-- TOC entry 215 (class 1259 OID 16447)
-- Name: parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parameter (
    id_param bigint NOT NULL,
    name text,
    units text,
    display_name text,
    description text
);


ALTER TABLE public.parameter OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16454)
-- Name: sensor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sensor (
    id_sensor bigint NOT NULL,
    name text,
    id_param bigint NOT NULL,
    latitude double precision,
    longitude double precision
);


ALTER TABLE public.sensor OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16474)
-- Name: sensor_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sensor_location (
    id bigint NOT NULL,
    id_location bigint NOT NULL,
    id_sensor bigint NOT NULL
);


ALTER TABLE public.sensor_location OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16473)
-- Name: sensor_location_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sensor_location_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sensor_location_id_seq OWNER TO postgres;

--
-- TOC entry 3402 (class 0 OID 0)
-- Dependencies: 218
-- Name: sensor_location_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sensor_location_id_seq OWNED BY public.sensor_location.id;


--
-- TOC entry 3225 (class 2604 OID 16494)
-- Name: measurement id_measure; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.measurement ALTER COLUMN id_measure SET DEFAULT nextval('public.measurement_id_measure_seq'::regclass);


--
-- TOC entry 3224 (class 2604 OID 16477)
-- Name: sensor_location id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_location ALTER COLUMN id SET DEFAULT nextval('public.sensor_location_id_seq'::regclass);


--
-- TOC entry 3394 (class 0 OID 16509)
-- Dependencies: 222
-- Data for Name: eu_threshold; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.eu_threshold VALUES (1, 'No2 EU Threshold', 5, 40);
INSERT INTO public.eu_threshold VALUES (2, 'PM25 EU Thresold', 2, 25);


--
-- TOC entry 3389 (class 0 OID 16466)
-- Dependencies: 217
-- Data for Name: location; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.location VALUES (3223, 'PLAZA DE ESPAÑA', 'MADRID', 'Europe/Madrid', 40.424166659847046, -3.712222219623825);
INSERT INTO public.location VALUES (3265, 'ES1567A', 'MADRID', 'Europe/Madrid', 40.33971999947, -3.73583);


--
-- TOC entry 3393 (class 0 OID 16491)
-- Dependencies: 221
-- Data for Name: measurement; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.measurement VALUES (1, 5, 6982, '1 day', '2025-01-20 00:00:00', '2025-01-21 00:00:00', 34, NULL, 34.44, 49.5, 59, 66.5, 76.91999999999999, 58.130434782608695, 12.899275341964994);
INSERT INTO public.measurement VALUES (2, 5, 6982, '1 day', '2025-01-22 00:00:00', '2025-01-23 00:00:00', 31.32, 32.41, 37.88, 41.16, 44.44, 47.72, 51, 41.16, 6.56);
INSERT INTO public.measurement VALUES (3, 5, 6982, '1 day', '2025-01-23 00:00:00', '2025-01-24 00:00:00', 21.36, 22.22, 29.24, 33.18, 37.12, 41.06, 45, 33.18, 7.88);
INSERT INTO public.measurement VALUES (4, 5, 6982, '1 day', '2025-01-24 00:00:00', '2025-01-25 00:00:00', 26.84, 27.12, 30.28, 32, 33.72, 35.44, 37.16, 32, 3.44);
INSERT INTO public.measurement VALUES (5, 5, 6982, '1 day', '2025-01-25 00:00:00', '2025-01-26 00:00:00', 24.03, 24.2, 31.57, 35.34, 39.11, 42.88, 46.65, 35.34, 7.54);
INSERT INTO public.measurement VALUES (6, 5, 6982, '1 day', '2025-01-21 00:00:00', '2025-01-22 00:00:00', 26.31, 32.59, 45.15, 51.43, 57.71, 70.27, 76.55, 51.43, 12.56);
INSERT INTO public.measurement VALUES (7, 5, 6982, '1 day', '2025-01-22 00:00:00', '2025-01-23 00:00:00', 31.13, 37.59, 50.51, 56.97, 63.43, 76.35, 82.81, 56.97, 12.92);
INSERT INTO public.measurement VALUES (8, 5, 6982, '1 day', '2025-01-23 00:00:00', '2025-01-24 00:00:00', 36.94, 42.15, 52.57, 57.78, 62.99, 73.41, 78.62, 57.78, 10.42);
INSERT INTO public.measurement VALUES (9, 5, 6982, '1 day', '2025-01-24 00:00:00', '2025-01-25 00:00:00', 40.41, 44.51, 52.71, 56.81, 60.91, 69.11, 73.21, 56.81, 8.2);
INSERT INTO public.measurement VALUES (10, 5, 6982, '1 day', '2025-01-25 00:00:00', '2025-01-26 00:00:00', 3.04, 10.21, 24.56, 31.74, 38.91, 53.27, 60.44, 31.74, 14.35);
INSERT INTO public.measurement VALUES (11, 5, 6982, '1 day', '2025-01-26 00:00:00', '2025-01-27 00:00:00', 39.95, 44.44, 53.41, 57.89, 62.38, 71.34, 75.83, 57.89, 8.97);
INSERT INTO public.measurement VALUES (12, 5, 6982, '1 day', '2025-01-27 00:00:00', '2025-01-28 00:00:00', 8, 15.43, 30.29, 37.72, 45.15, 60.01, 67.44, 37.72, 14.86);
INSERT INTO public.measurement VALUES (13, 5, 6982, '1 day', '2025-01-28 00:00:00', '2025-01-29 00:00:00', 23.29, 29.96, 43.29, 49.95, 56.62, 69.95, 76.61, 49.95, 13.33);
INSERT INTO public.measurement VALUES (14, 5, 6982, '1 day', '2025-01-29 00:00:00', '2025-01-30 00:00:00', 33.48, 36.47, 42.45, 45.44, 48.43, 54.41, 57.4, 45.44, 5.98);
INSERT INTO public.measurement VALUES (15, 5, 6982, '1 day', '2025-01-30 00:00:00', '2025-01-31 00:00:00', 37.25, 41.39, 49.67, 53.81, 57.95, 66.23, 70.37, 53.81, 8.28);
INSERT INTO public.measurement VALUES (16, 5, 6982, '1 day', '2025-01-31 00:00:00', '2025-02-01 00:00:00', 16.11, 21.77, 33.09, 38.75, 44.41, 55.73, 61.39, 38.75, 11.32);
INSERT INTO public.measurement VALUES (17, 5, 6982, '1 day', '2025-02-01 00:00:00', '2025-02-02 00:00:00', 22.39, 29.47, 43.63, 50.71, 57.79, 71.95, 79.03, 50.71, 14.16);
INSERT INTO public.measurement VALUES (18, 5, 6982, '1 day', '2025-02-02 00:00:00', '2025-02-03 00:00:00', 19.26, 23.73, 32.67, 37.14, 41.61, 50.55, 55.02, 37.14, 8.94);
INSERT INTO public.measurement VALUES (19, 5, 6982, '1 day', '2025-02-03 00:00:00', '2025-02-04 00:00:00', 50.48, 51.98, 54.99, 56.5, 58.01, 61.02, 62.52, 56.5, 3.01);
INSERT INTO public.measurement VALUES (20, 5, 6982, '1 day', '2025-02-04 00:00:00', '2025-02-05 00:00:00', 24.49, 30.89, 43.67, 50.07, 56.47, 69.25, 75.65, 50.07, 12.79);
INSERT INTO public.measurement VALUES (21, 5, 6982, '1 day', '2025-02-05 00:00:00', '2025-02-06 00:00:00', 47.17, 49.92, 55.43, 58.19, 60.95, 66.45, 69.21, 58.19, 5.51);
INSERT INTO public.measurement VALUES (22, 5, 6982, '1 day', '2025-02-06 00:00:00', '2025-02-07 00:00:00', 26.64, 30.47, 38.14, 41.98, 45.81, 53.48, 57.32, 41.98, 7.67);
INSERT INTO public.measurement VALUES (23, 5, 6982, '1 day', '2025-02-07 00:00:00', '2025-02-08 00:00:00', 28.96, 31.48, 36.52, 39.04, 41.56, 46.6, 49.12, 39.04, 5.04);
INSERT INTO public.measurement VALUES (24, 5, 6982, '1 day', '2025-02-08 00:00:00', '2025-02-09 00:00:00', 32.11, 33.8, 37.2, 38.89, 40.59, 43.98, 45.67, 38.89, 3.39);
INSERT INTO public.measurement VALUES (25, 5, 6982, '1 day', '2025-02-09 00:00:00', '2025-02-10 00:00:00', 25.94, 28.71, 34.27, 37.04, 39.81, 45.36, 48.14, 37.04, 5.55);
INSERT INTO public.measurement VALUES (26, 5, 6982, '1 day', '2025-02-10 00:00:00', '2025-02-11 00:00:00', 37.37, 39.87, 44.86, 47.35, 49.84, 54.84, 57.33, 47.35, 4.99);
INSERT INTO public.measurement VALUES (27, 5, 6982, '1 day', '2025-02-11 00:00:00', '2025-02-12 00:00:00', 3.4, 10.52, 24.76, 31.88, 39, 53.24, 60.36, 31.88, 14.24);
INSERT INTO public.measurement VALUES (28, 5, 6982, '1 day', '2025-02-12 00:00:00', '2025-02-13 00:00:00', 23.21, 27.56, 36.26, 40.61, 44.96, 53.66, 58.01, 40.61, 8.7);
INSERT INTO public.measurement VALUES (29, 5, 6982, '1 day', '2025-02-13 00:00:00', '2025-02-14 00:00:00', 14.96, 18.98, 27, 31.02, 35.03, 43.06, 47.08, 31.02, 8.03);
INSERT INTO public.measurement VALUES (30, 5, 6982, '1 day', '2025-02-14 00:00:00', '2025-02-15 00:00:00', 35.26, 38.17, 44.01, 46.92, 49.84, 55.67, 58.58, 46.92, 5.83);
INSERT INTO public.measurement VALUES (31, 5, 6982, '1 day', '2025-02-15 00:00:00', '2025-02-16 00:00:00', 27.12, 33.94, 47.58, 54.4, 61.22, 74.86, 81.68, 54.4, 13.64);
INSERT INTO public.measurement VALUES (32, 5, 6982, '1 day', '2025-02-16 00:00:00', '2025-02-17 00:00:00', 33.17, 35.14, 39.09, 41.07, 43.05, 47, 48.97, 41.07, 3.95);
INSERT INTO public.measurement VALUES (33, 5, 6982, '1 day', '2025-02-17 00:00:00', '2025-02-18 00:00:00', 37.31, 39.8, 44.8, 47.29, 49.78, 54.77, 57.27, 47.29, 4.99);
INSERT INTO public.measurement VALUES (34, 5, 6982, '1 day', '2025-02-18 00:00:00', '2025-02-19 00:00:00', 31.2, 37.1, 48.9, 54.8, 60.7, 72.5, 78.4, 54.8, 11.8);
INSERT INTO public.measurement VALUES (35, 5, 6982, '1 day', '2025-02-19 00:00:00', '2025-02-20 00:00:00', 44.7, 48.39, 55.77, 59.46, 63.15, 70.53, 74.22, 59.46, 7.38);
INSERT INTO public.measurement VALUES (36, 5, 6982, '1 day', '2025-02-20 00:00:00', '2025-02-21 00:00:00', 33.66, 38.24, 47.4, 51.98, 56.56, 65.72, 70.3, 51.98, 9.16);
INSERT INTO public.measurement VALUES (37, 5, 6982, '1 day', '2025-02-21 00:00:00', '2025-02-22 00:00:00', 35.55, 41.03, 51.99, 57.47, 62.95, 73.91, 79.39, 57.47, 10.96);
INSERT INTO public.measurement VALUES (38, 5, 6982, '1 day', '2025-02-22 00:00:00', '2025-02-23 00:00:00', 14.82, 18.62, 26.23, 30.04, 33.84, 41.45, 45.26, 30.04, 7.61);
INSERT INTO public.measurement VALUES (39, 5, 6982, '1 day', '2025-02-23 00:00:00', '2025-02-24 00:00:00', 19.39, 23.62, 32.06, 36.29, 40.52, 48.96, 53.19, 36.29, 8.45);
INSERT INTO public.measurement VALUES (40, 5, 6982, '1 day', '2025-02-24 00:00:00', '2025-02-25 00:00:00', 38.1, 42.12, 50.18, 54.2, 58.23, 66.28, 70.3, 54.2, 8.05);
INSERT INTO public.measurement VALUES (41, 5, 6982, '1 day', '2025-02-25 00:00:00', '2025-02-26 00:00:00', 47.04, 49.92, 55.7, 58.58, 61.46, 67.23, 70.12, 58.58, 5.77);
INSERT INTO public.measurement VALUES (42, 5, 6982, '1 day', '2025-02-26 00:00:00', '2025-02-27 00:00:00', 27.45, 32.01, 41.12, 45.67, 50.23, 59.34, 63.89, 45.67, 9.11);
INSERT INTO public.measurement VALUES (43, 5, 6982, '1 day', '2025-02-27 00:00:00', '2025-02-28 00:00:00', 37.18, 41.5, 50.14, 54.46, 58.78, 67.42, 71.74, 54.46, 8.64);
INSERT INTO public.measurement VALUES (44, 5, 6982, '1 day', '2025-02-28 00:00:00', '2025-03-01 00:00:00', 28.88, 34.22, 44.91, 50.26, 55.6, 66.3, 71.64, 50.26, 10.69);
INSERT INTO public.measurement VALUES (45, 5, 6982, '1 day', '2025-03-01 00:00:00', '2025-03-02 00:00:00', 22.01, 25.95, 33.84, 37.79, 41.73, 49.62, 53.57, 37.79, 7.89);
INSERT INTO public.measurement VALUES (46, 5, 6982, '1 day', '2025-03-02 00:00:00', '2025-03-03 00:00:00', 15.87, 20.7, 30.35, 35.17, 40, 49.65, 54.47, 35.17, 9.65);
INSERT INTO public.measurement VALUES (47, 5, 6982, '1 day', '2025-03-03 00:00:00', '2025-03-04 00:00:00', 28.57, 33.72, 44.03, 49.19, 54.34, 64.66, 69.81, 49.19, 10.31);
INSERT INTO public.measurement VALUES (48, 5, 6982, '1 day', '2025-03-04 00:00:00', '2025-03-05 00:00:00', 37.69, 39.72, 43.77, 45.79, 47.81, 51.86, 53.89, 45.79, 4.05);
INSERT INTO public.measurement VALUES (49, 5, 6982, '1 day', '2025-03-05 00:00:00', '2025-03-06 00:00:00', 19.08, 25.55, 38.5, 44.98, 51.45, 64.41, 70.88, 44.98, 12.95);
INSERT INTO public.measurement VALUES (50, 5, 6982, '1 day', '2025-03-06 00:00:00', '2025-03-07 00:00:00', 27.04, 29.54, 34.54, 37.04, 39.54, 44.54, 47.04, 37.04, 5);
INSERT INTO public.measurement VALUES (51, 5, 6982, '1 day', '2025-03-07 00:00:00', '2025-03-08 00:00:00', 35.68, 37.81, 42.09, 44.22, 46.35, 50.62, 52.76, 44.22, 4.27);
INSERT INTO public.measurement VALUES (52, 5, 6982, '1 day', '2025-03-08 00:00:00', '2025-03-09 00:00:00', 25.19, 28.31, 34.55, 37.67, 40.79, 47.03, 50.15, 37.67, 6.24);
INSERT INTO public.measurement VALUES (53, 5, 6982, '1 day', '2025-03-09 00:00:00', '2025-03-10 00:00:00', 42.38, 43.94, 47.06, 48.62, 50.18, 53.3, 54.86, 48.62, 3.12);
INSERT INTO public.measurement VALUES (54, 5, 6982, '1 day', '2025-03-10 00:00:00', '2025-03-11 00:00:00', 12.15, 16.95, 26.55, 31.35, 36.15, 45.75, 50.55, 31.35, 9.6);
INSERT INTO public.measurement VALUES (55, 5, 6982, '1 day', '2025-03-11 00:00:00', '2025-03-12 00:00:00', 31.37, 38.39, 52.45, 59.47, 66.5, 80.55, 87.57, 59.47, 14.05);
INSERT INTO public.measurement VALUES (56, 5, 6982, '1 day', '2025-03-12 00:00:00', '2025-03-13 00:00:00', 15.29, 20.41, 30.65, 35.77, 40.89, 51.13, 56.25, 35.77, 10.24);
INSERT INTO public.measurement VALUES (57, 5, 6982, '1 day', '2025-03-13 00:00:00', '2025-03-14 00:00:00', 19.34, 26.76, 41.6, 49.02, 56.44, 71.28, 78.7, 49.02, 14.84);
INSERT INTO public.measurement VALUES (58, 5, 6982, '1 day', '2025-03-14 00:00:00', '2025-03-15 00:00:00', 8.55, 15.36, 28.97, 35.77, 42.58, 56.19, 62.99, 35.77, 13.61);
INSERT INTO public.measurement VALUES (59, 5, 6982, '1 day', '2025-03-15 00:00:00', '2025-03-16 00:00:00', 41.31, 45.43, 53.67, 57.79, 61.91, 70.15, 74.27, 57.79, 8.24);
INSERT INTO public.measurement VALUES (60, 5, 6982, '1 day', '2025-03-16 00:00:00', '2025-03-17 00:00:00', 48.07, 50.42, 55.12, 57.47, 59.82, 64.52, 66.87, 57.47, 4.7);
INSERT INTO public.measurement VALUES (61, 5, 6982, '1 day', '2025-03-17 00:00:00', '2025-03-18 00:00:00', 35.92, 40.1, 48.46, 52.64, 56.82, 65.18, 69.36, 52.64, 8.36);
INSERT INTO public.measurement VALUES (62, 5, 6982, '1 day', '2025-03-18 00:00:00', '2025-03-19 00:00:00', 21.25, 28.29, 42.37, 49.41, 56.45, 70.53, 77.57, 49.41, 14.08);
INSERT INTO public.measurement VALUES (63, 5, 6982, '1 day', '2025-03-19 00:00:00', '2025-03-20 00:00:00', 30.41, 33.41, 39.4, 42.39, 45.38, 51.38, 54.37, 42.39, 5.99);
INSERT INTO public.measurement VALUES (64, 5, 6982, '1 day', '2025-03-20 00:00:00', '2025-03-21 00:00:00', 28.92, 31.41, 36.38, 38.86, 41.34, 46.31, 48.8, 38.86, 4.97);
INSERT INTO public.measurement VALUES (65, 5, 6982, '1 day', '2025-03-21 00:00:00', '2025-03-22 00:00:00', 20.81, 26.88, 39.01, 45.07, 51.13, 63.27, 69.33, 45.07, 12.13);
INSERT INTO public.measurement VALUES (66, 5, 6982, '1 day', '2025-03-22 00:00:00', '2025-03-23 00:00:00', 45.74, 48.84, 55.04, 58.14, 61.24, 67.44, 70.54, 58.14, 6.2);
INSERT INTO public.measurement VALUES (67, 5, 6982, '1 day', '2025-03-23 00:00:00', '2025-03-24 00:00:00', 29.12, 34.14, 44.2, 49.22, 54.24, 64.3, 69.32, 49.22, 10.05);
INSERT INTO public.measurement VALUES (68, 5, 6982, '1 day', '2025-03-24 00:00:00', '2025-03-25 00:00:00', 22.1, 25.69, 32.88, 36.48, 40.07, 47.27, 50.86, 36.48, 7.19);
INSERT INTO public.measurement VALUES (69, 5, 6982, '1 day', '2025-03-25 00:00:00', '2025-03-26 00:00:00', 25.12, 29, 36.77, 40.66, 44.54, 52.31, 56.2, 40.66, 7.77);
INSERT INTO public.measurement VALUES (70, 5, 6982, '1 day', '2025-03-26 00:00:00', '2025-03-27 00:00:00', 37.6, 39.12, 42.16, 43.68, 45.2, 48.24, 49.76, 43.68, 3.04);
INSERT INTO public.measurement VALUES (71, 5, 6982, '1 day', '2025-03-27 00:00:00', '2025-03-28 00:00:00', 39.36, 44.25, 54.05, 58.94, 63.83, 73.62, 78.52, 58.94, 9.79);
INSERT INTO public.measurement VALUES (72, 5, 6982, '1 day', '2025-03-28 00:00:00', '2025-03-29 00:00:00', 46.63, 49.02, 53.81, 56.21, 58.61, 63.4, 65.79, 56.21, 4.79);
INSERT INTO public.measurement VALUES (73, 5, 6982, '1 day', '2025-03-29 00:00:00', '2025-03-30 00:00:00', 44.62, 46.5, 50.27, 52.16, 54.04, 57.81, 59.7, 52.16, 3.77);
INSERT INTO public.measurement VALUES (74, 5, 6982, '1 day', '2025-03-30 00:00:00', '2025-03-31 00:00:00', 24.01, 30.95, 44.84, 51.79, 58.73, 72.62, 79.57, 51.79, 13.89);
INSERT INTO public.measurement VALUES (75, 5, 6982, '1 day', '2025-03-31 00:00:00', '2025-04-01 00:00:00', 29.72, 36.84, 51.09, 58.22, 65.34, 79.59, 86.72, 58.22, 14.25);
INSERT INTO public.measurement VALUES (76, 5, 6982, '1 day', '2025-04-01 00:00:00', '2025-04-02 00:00:00', 31.8, 34.93, 41.19, 44.32, 47.45, 53.71, 56.84, 44.32, 6.26);
INSERT INTO public.measurement VALUES (77, 5, 6982, '1 day', '2025-04-02 00:00:00', '2025-04-03 00:00:00', 20.37, 24.14, 31.68, 35.45, 39.22, 46.76, 50.53, 35.45, 7.54);
INSERT INTO public.measurement VALUES (78, 5, 6982, '1 day', '2025-04-03 00:00:00', '2025-04-04 00:00:00', 20.95, 24.26, 30.87, 34.17, 37.48, 44.09, 47.39, 34.17, 6.61);
INSERT INTO public.measurement VALUES (79, 5, 6982, '1 day', '2025-04-04 00:00:00', '2025-04-05 00:00:00', 35.93, 37.8, 41.52, 43.39, 45.26, 48.98, 50.85, 43.39, 3.73);
INSERT INTO public.measurement VALUES (80, 5, 6982, '1 day', '2025-04-05 00:00:00', '2025-04-06 00:00:00', 16.03, 19.74, 27.16, 30.87, 34.58, 42, 45.71, 30.87, 7.42);
INSERT INTO public.measurement VALUES (81, 5, 6982, '1 day', '2025-04-06 00:00:00', '2025-04-07 00:00:00', 20.16, 26.03, 37.77, 43.64, 49.51, 61.25, 67.12, 43.64, 11.74);
INSERT INTO public.measurement VALUES (82, 5, 6982, '1 day', '2025-04-07 00:00:00', '2025-04-08 00:00:00', 20.48, 27, 40.02, 46.54, 53.05, 66.08, 72.6, 46.54, 13.03);
INSERT INTO public.measurement VALUES (83, 5, 6982, '1 day', '2025-04-08 00:00:00', '2025-04-09 00:00:00', 7.69, 13.57, 25.34, 31.23, 37.12, 48.89, 54.77, 31.23, 11.77);
INSERT INTO public.measurement VALUES (84, 5, 6982, '1 day', '2025-04-09 00:00:00', '2025-04-10 00:00:00', 26.62, 32.21, 43.39, 48.98, 54.57, 65.75, 71.34, 48.98, 11.18);
INSERT INTO public.measurement VALUES (85, 5, 6982, '1 day', '2025-04-10 00:00:00', '2025-04-11 00:00:00', 16.36, 20.84, 29.81, 34.3, 38.78, 47.75, 52.24, 34.3, 8.97);
INSERT INTO public.measurement VALUES (86, 5, 6982, '1 day', '2025-04-11 00:00:00', '2025-04-12 00:00:00', 32.98, 38.54, 49.65, 55.2, 60.76, 71.87, 77.42, 55.2, 11.11);
INSERT INTO public.measurement VALUES (87, 5, 6982, '1 day', '2025-04-12 00:00:00', '2025-04-13 00:00:00', 43.43, 47.04, 54.25, 57.85, 61.45, 68.67, 72.27, 57.85, 7.21);
INSERT INTO public.measurement VALUES (88, 5, 6982, '1 day', '2025-04-13 00:00:00', '2025-04-14 00:00:00', 24.33, 26.76, 31.62, 34.05, 36.48, 41.34, 43.77, 34.05, 4.86);
INSERT INTO public.measurement VALUES (89, 5, 6982, '1 day', '2025-04-14 00:00:00', '2025-04-15 00:00:00', 25.28, 26.99, 30.41, 32.12, 33.83, 37.25, 38.96, 32.12, 3.42);
INSERT INTO public.measurement VALUES (90, 5, 6982, '1 day', '2025-04-15 00:00:00', '2025-04-16 00:00:00', 12.39, 17.61, 28.05, 33.27, 38.49, 48.93, 54.15, 33.27, 10.44);
INSERT INTO public.measurement VALUES (91, 5, 6982, '1 day', '2025-04-16 00:00:00', '2025-04-17 00:00:00', 21.71, 25.45, 32.91, 36.65, 40.38, 47.85, 51.59, 36.65, 7.47);
INSERT INTO public.measurement VALUES (92, 5, 6982, '1 day', '2025-04-17 00:00:00', '2025-04-18 00:00:00', 41.24, 45.63, 54.41, 58.8, 63.19, 71.97, 76.36, 58.8, 8.78);
INSERT INTO public.measurement VALUES (93, 5, 6982, '1 day', '2025-04-18 00:00:00', '2025-04-19 00:00:00', 16.47, 22.09, 33.34, 38.97, 44.59, 55.84, 61.47, 38.97, 11.25);
INSERT INTO public.measurement VALUES (94, 5, 6982, '1 day', '2025-04-19 00:00:00', '2025-04-20 00:00:00', 28.09, 33.73, 45.03, 50.67, 56.31, 67.61, 73.25, 50.67, 11.29);
INSERT INTO public.measurement VALUES (95, 5, 6982, '1 day', '2025-04-20 00:00:00', '2025-04-21 00:00:00', 41.05, 45.26, 53.68, 57.89, 62.1, 70.52, 74.73, 57.89, 8.42);
INSERT INTO public.measurement VALUES (96, 5, 6982, '1 day', '2025-04-21 00:00:00', '2025-04-22 00:00:00', 17.18, 20.42, 26.9, 30.14, 33.38, 39.86, 43.1, 30.14, 6.48);
INSERT INTO public.measurement VALUES (97, 5, 6982, '1 day', '2025-04-22 00:00:00', '2025-04-23 00:00:00', 18.64, 24.91, 37.47, 43.74, 50.02, 62.57, 68.84, 43.74, 12.55);
INSERT INTO public.measurement VALUES (98, 5, 6982, '1 day', '2025-04-23 00:00:00', '2025-04-24 00:00:00', 33.08, 35.44, 40.15, 42.5, 44.85, 49.56, 51.92, 42.5, 4.71);
INSERT INTO public.measurement VALUES (99, 5, 6982, '1 day', '2025-04-24 00:00:00', '2025-04-25 00:00:00', 43.82, 46.36, 51.44, 53.98, 56.52, 61.6, 64.14, 53.98, 5.08);
INSERT INTO public.measurement VALUES (100, 5, 6982, '1 day', '2025-04-25 00:00:00', '2025-04-26 00:00:00', 30.41, 34.01, 41.21, 44.81, 48.41, 55.61, 59.21, 44.81, 7.2);
INSERT INTO public.measurement VALUES (101, 5, 6982, '1 day', '2025-04-26 00:00:00', '2025-04-27 00:00:00', 41.19, 45.42, 53.87, 58.09, 62.32, 70.77, 74.99, 58.09, 8.45);
INSERT INTO public.measurement VALUES (102, 5, 6982, '1 day', '2025-04-27 00:00:00', '2025-04-28 00:00:00', 22.64, 29.54, 43.33, 50.22, 57.11, 70.91, 77.8, 50.22, 13.79);
INSERT INTO public.measurement VALUES (103, 5, 6982, '1 day', '2025-04-28 00:00:00', '2025-04-29 00:00:00', 33.54, 35.84, 40.42, 42.72, 45.02, 49.6, 51.9, 42.72, 4.59);
INSERT INTO public.measurement VALUES (104, 5, 6982, '1 day', '2025-04-29 00:00:00', '2025-04-30 00:00:00', 47.33, 49.15, 52.79, 54.61, 56.43, 60.07, 61.89, 54.61, 3.64);
INSERT INTO public.measurement VALUES (105, 5, 6982, '1 day', '2025-04-30 00:00:00', '2025-05-01 00:00:00', 10.05, 15.14, 25.32, 30.41, 35.5, 45.68, 50.77, 30.41, 10.18);
INSERT INTO public.measurement VALUES (106, 5, 6982, '1 day', '2025-05-01 00:00:00', '2025-05-02 00:00:00', 17.76, 22.36, 31.55, 36.14, 40.73, 49.92, 54.52, 36.14, 9.19);
INSERT INTO public.measurement VALUES (107, 5, 6982, '1 day', '2025-05-02 00:00:00', '2025-05-03 00:00:00', 52.87, 54.45, 57.59, 59.17, 60.75, 63.9, 65.47, 59.17, 3.15);
INSERT INTO public.measurement VALUES (108, 5, 6982, '1 day', '2025-05-03 00:00:00', '2025-05-04 00:00:00', 29.38, 35.23, 46.94, 52.8, 58.66, 70.36, 76.22, 52.8, 11.71);
INSERT INTO public.measurement VALUES (109, 5, 6982, '1 day', '2025-05-04 00:00:00', '2025-05-05 00:00:00', 32.98, 38.39, 49.21, 54.62, 60.03, 70.85, 76.26, 54.62, 10.82);
INSERT INTO public.measurement VALUES (110, 5, 6982, '1 day', '2025-05-05 00:00:00', '2025-05-06 00:00:00', 33.62, 36.27, 41.57, 44.22, 46.87, 52.17, 54.82, 44.22, 5.3);
INSERT INTO public.measurement VALUES (111, 5, 6982, '1 day', '2025-05-06 00:00:00', '2025-05-07 00:00:00', 37.99, 40.8, 46.44, 49.25, 52.06, 57.7, 60.51, 49.25, 5.63);
INSERT INTO public.measurement VALUES (112, 5, 6982, '1 day', '2025-05-07 00:00:00', '2025-05-08 00:00:00', 10.05, 16.04, 28.02, 34.01, 40, 51.98, 57.97, 34.01, 11.98);
INSERT INTO public.measurement VALUES (113, 5, 6982, '1 day', '2025-05-08 00:00:00', '2025-05-09 00:00:00', 20.48, 26, 37.05, 42.58, 48.1, 59.16, 64.68, 42.58, 11.05);
INSERT INTO public.measurement VALUES (114, 5, 6982, '1 day', '2025-05-09 00:00:00', '2025-05-10 00:00:00', 12.77, 17.23, 26.13, 30.59, 35.05, 43.95, 48.41, 30.59, 8.91);
INSERT INTO public.measurement VALUES (115, 5, 6982, '1 day', '2025-05-10 00:00:00', '2025-05-11 00:00:00', 19.54, 26.46, 40.3, 47.22, 54.14, 67.98, 74.9, 47.22, 13.84);
INSERT INTO public.measurement VALUES (116, 5, 6982, '1 day', '2025-05-11 00:00:00', '2025-05-12 00:00:00', 26.47, 30.31, 37.99, 41.83, 45.67, 53.35, 57.19, 41.83, 7.68);
INSERT INTO public.measurement VALUES (117, 5, 6982, '1 day', '2025-05-12 00:00:00', '2025-05-13 00:00:00', 30.96, 33.44, 38.4, 40.88, 43.36, 48.32, 50.8, 40.88, 4.96);
INSERT INTO public.measurement VALUES (118, 5, 6982, '1 day', '2025-05-13 00:00:00', '2025-05-14 00:00:00', 21.93, 27.75, 39.4, 45.23, 51.05, 62.7, 68.53, 45.23, 11.65);
INSERT INTO public.measurement VALUES (119, 5, 6982, '1 day', '2025-05-14 00:00:00', '2025-05-15 00:00:00', 17, 21.78, 31.34, 36.12, 40.9, 50.46, 55.24, 36.12, 9.56);
INSERT INTO public.measurement VALUES (120, 5, 6982, '1 day', '2025-05-15 00:00:00', '2025-05-16 00:00:00', 27.78, 29.77, 33.75, 35.74, 37.73, 41.71, 43.7, 35.74, 3.98);
INSERT INTO public.measurement VALUES (121, 5, 6982, '1 day', '2025-05-16 00:00:00', '2025-05-17 00:00:00', 30.47, 32.89, 37.73, 40.15, 42.57, 47.41, 49.83, 40.15, 4.84);
INSERT INTO public.measurement VALUES (122, 5, 6982, '1 day', '2025-05-17 00:00:00', '2025-05-18 00:00:00', 22.64, 29.58, 43.45, 50.38, 57.32, 71.19, 78.12, 50.38, 13.87);
INSERT INTO public.measurement VALUES (123, 5, 6982, '1 day', '2025-05-18 00:00:00', '2025-05-19 00:00:00', 38.9, 44.08, 54.44, 59.62, 64.8, 75.16, 80.34, 59.62, 10.36);
INSERT INTO public.measurement VALUES (124, 5, 6982, '1 day', '2025-05-19 00:00:00', '2025-05-20 00:00:00', 19.41, 26.06, 39.36, 46.01, 52.66, 65.96, 72.61, 46.01, 13.3);
INSERT INTO public.measurement VALUES (125, 5, 6982, '1 day', '2025-05-20 00:00:00', '2025-05-21 00:00:00', 44.5, 46.63, 50.91, 53.04, 55.17, 59.45, 61.58, 53.04, 4.27);
INSERT INTO public.measurement VALUES (126, 5, 6982, '1 day', '2025-05-21 00:00:00', '2025-05-22 00:00:00', 34.08, 36.22, 40.5, 42.64, 44.78, 49.06, 51.2, 42.64, 4.28);
INSERT INTO public.measurement VALUES (127, 5, 6982, '1 day', '2025-05-22 00:00:00', '2025-05-23 00:00:00', 27.42, 31.21, 38.79, 42.58, 46.37, 53.95, 57.74, 42.58, 7.58);
INSERT INTO public.measurement VALUES (128, 5, 6982, '1 day', '2025-05-23 00:00:00', '2025-05-24 00:00:00', 31.11, 33.39, 37.97, 40.25, 42.53, 47.11, 49.39, 40.25, 4.57);
INSERT INTO public.measurement VALUES (129, 5, 6982, '1 day', '2025-05-24 00:00:00', '2025-05-25 00:00:00', 17.23, 23.43, 35.84, 42.05, 48.25, 60.66, 66.87, 42.05, 12.41);
INSERT INTO public.measurement VALUES (130, 5, 6982, '1 day', '2025-05-25 00:00:00', '2025-05-26 00:00:00', 29.9, 36.04, 48.32, 54.46, 60.6, 72.88, 79.02, 54.46, 12.28);
INSERT INTO public.measurement VALUES (131, 5, 6982, '1 day', '2025-05-26 00:00:00', '2025-05-27 00:00:00', 21.1, 25.24, 33.52, 37.66, 41.8, 50.08, 54.22, 37.66, 8.28);
INSERT INTO public.measurement VALUES (132, 5, 6982, '1 day', '2025-05-27 00:00:00', '2025-05-28 00:00:00', 33.77, 38.96, 49.34, 54.53, 59.72, 70.1, 75.29, 54.53, 10.38);
INSERT INTO public.measurement VALUES (133, 5, 6982, '1 day', '2025-05-28 00:00:00', '2025-05-29 00:00:00', 14.45, 19.29, 28.98, 33.83, 38.67, 48.36, 53.21, 33.83, 9.69);
INSERT INTO public.measurement VALUES (134, 5, 6982, '1 year', '2016-01-01 00:00:00', '2017-01-01 00:00:00', 23, 24.12, 47.5, 58.142857, 68.400002, 89.3, 90, 57.153866, 18.168678);
INSERT INTO public.measurement VALUES (135, 5, 6982, '1 year', '2017-01-01 00:00:00', '2018-01-01 00:00:00', 7, 17.718334, 36.285713, 49.782608, 61.5, 99.453716, 136.5, 50.633808, 20.903567);
INSERT INTO public.measurement VALUES (136, 5, 6982, '1 year', '2018-01-01 00:00:00', '2019-01-01 00:00:00', 1, 5, 15, 24, 44, 80, 101.777779, 30.811677, 20.4355);
INSERT INTO public.measurement VALUES (137, 5, 6982, '1 year', '2019-01-01 00:00:00', '2020-01-01 00:00:00', 0, 0, 0, 2, 37.607143, 81.205715, 102.5, 20.458205, 25.929826);
INSERT INTO public.measurement VALUES (138, 5, 6982, '1 year', '2020-01-01 00:00:00', '2021-01-01 00:00:00', 4.333333, 7.27, 21.5, 34.515408, 47.069642, 84.862857, 105, 35.570957, 19.462466);
INSERT INTO public.measurement VALUES (139, 5, 6982, '1 year', '2021-01-01 00:00:00', '2022-01-01 00:00:00', 7.8, 8.494, 18.175, 28.785714, 43.95, 93.555003, 105.199997, 33.468668, 20.041468);
INSERT INTO public.measurement VALUES (140, 5, 6982, '1 year', '2022-01-01 00:00:00', '2023-01-01 00:00:00', 2, 6.5, 16.633333, 23.5, 34.680481, 56.333332, 78, 25.790452, 13.084489);
INSERT INTO public.measurement VALUES (141, 5, 6982, '1 year', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 4.5, 7.8105, 16.249387, 24.516667, 33.723087, 54.229692, 74.599998, 26.229728, 12.801848);
INSERT INTO public.measurement VALUES (142, 5, 7101, '1 year', '2016-01-01 00:00:00', '2017-01-01 00:00:00', 18.25, 18.67, NULL, 48.11111068725586, 57.5, 104.55999999999995, 118, 49.998946814701476, 22.45364878304861);
INSERT INTO public.measurement VALUES (143, 5, 7101, '1 year', '2017-01-01 00:00:00', '2018-01-01 00:00:00', 7, 12.175, NULL, 42.09090805053711, 64.17992401123047, 103.94039093017578, 135.5, 47.57591658747474, 24.92640362818458);
INSERT INTO public.measurement VALUES (144, 5, 7101, '1 year', '2018-01-01 00:00:00', '2019-01-01 00:00:00', 3, 4.153333406448365, NULL, 18.25, 41.94999980926514, 83.78363540649424, 113, 27.834747724289443, 22.323487014292382);
INSERT INTO public.measurement VALUES (145, 5, 7101, '1 year', '2019-01-01 00:00:00', '2020-01-01 00:00:00', 0, 0, NULL, 8, 26.255493640899658, 78.59802337646485, 92, 18.028752458224325, 23.337855533922138);
INSERT INTO public.measurement VALUES (146, 5, 7101, '1 year', '2020-01-01 00:00:00', '2021-01-01 00:00:00', 2.5, 3.4400000000000004, NULL, 21.350000381469727, 45.89583396911621, 85.1433319091797, 109.5, 29.305819005050896, 22.78438343763476);
INSERT INTO public.measurement VALUES (147, 5, 7101, '1 year', '2021-01-01 00:00:00', '2022-01-01 00:00:00', 3, 5, NULL, 25.33333396911621, 46.16666793823242, 96.61999938964837, 121.375, 32.18155259065354, 23.32632557460554);
INSERT INTO public.measurement VALUES (148, 5, 7101, '1 year', '2022-01-01 00:00:00', '2023-01-01 00:00:00', 2.5, 4.42, NULL, 26.25, 44.67499923706055, 73.73230499267576, 87.33333587646484, 29.756514333775023, 20.010754163636157);
INSERT INTO public.measurement VALUES (149, 5, 7101, '1 year', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 5.5, 8.485, NULL, 25.71780300140381, 38.053977966308594, 65.7966680908203, 95, 29.205661828701313, 16.803904829144564);
INSERT INTO public.measurement VALUES (150, 2, 25161, '1 year', '2019-01-01 00:00:00', '2020-01-01 00:00:00', 0, 0, NULL, 3.485119, 9.723214, 29.131137, 40, 6.267571, 7.908327);
INSERT INTO public.measurement VALUES (151, 2, 25161, '1 year', '2020-01-01 00:00:00', '2021-01-01 00:00:00', 1, 2, NULL, 12, 18.810606, 36.926668, 53, 13.945522, 9.227043);
INSERT INTO public.measurement VALUES (152, 2, 25161, '1 year', '2021-01-01 00:00:00', '2022-01-01 00:00:00', 1, 2.614667, NULL, 10.978261, 17.03125, 46.556843, 67.13636, 13.206726, 9.665507);
INSERT INTO public.measurement VALUES (153, 2, 25161, '1 year', '2022-01-01 00:00:00', '2023-01-01 00:00:00', 1, 1.734203, NULL, 9, 14.333333, 27.0025, 82.8125, 10.559152, 7.762699);
INSERT INTO public.measurement VALUES (154, 2, 25161, '1 year', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 1, 1.916667, NULL, 7.354167, 11.799633, 28.080833, 36.900002, 9.136555, 6.489666);
INSERT INTO public.measurement VALUES (155, 2, 25161, '1 year', '2024-01-01 00:00:00', '2025-01-01 00:00:00', 2.166667, 2.376667, NULL, 11.416667, 16.142857, 29.501666, 31.041666, 12.064696, 8.112829);
INSERT INTO public.measurement VALUES (156, 9996, 1, '1 year', '2015-01-01 00:00:00', '2016-01-01 00:00:00', 9.7, NULL, NULL, NULL, NULL, NULL, 23, 16.8, NULL);
INSERT INTO public.measurement VALUES (160, 9996, 1, '1 year', '2018-01-01 00:00:00', '2019-01-01 00:00:00', 10.1, NULL, NULL, NULL, NULL, NULL, 23.8, 17.4, NULL);
INSERT INTO public.measurement VALUES (158, 9996, 1, '1 year', '2016-01-01 00:00:00', '2017-01-01 00:00:00', 9.5, NULL, NULL, NULL, NULL, NULL, 23.4, 16.5, NULL);
INSERT INTO public.measurement VALUES (159, 9996, 1, '1 year', '2017-01-01 00:00:00', '2018-01-01 00:00:00', 9.9, NULL, NULL, NULL, NULL, NULL, 23.5, 16.9, NULL);
INSERT INTO public.measurement VALUES (161, 9996, 1, '1 year', '2020-01-01 00:00:00', '2021-01-01 00:00:00', 9.6, NULL, NULL, NULL, NULL, NULL, 23.9, 16.5, NULL);
INSERT INTO public.measurement VALUES (162, 9996, 1, '1 year', '2021-01-01 00:00:00', '2022-01-01 00:00:00', 9.8, NULL, NULL, NULL, NULL, NULL, 23.2, 16.7, NULL);
INSERT INTO public.measurement VALUES (163, 9996, 1, '1 year', '2022-01-01 00:00:00', '2023-01-01 00:00:00', 10, NULL, NULL, NULL, NULL, NULL, 23.4, 16.9, NULL);
INSERT INTO public.measurement VALUES (164, 9996, 1, '1 year', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 9.7, NULL, NULL, NULL, NULL, NULL, 23.6, 16.6, NULL);
INSERT INTO public.measurement VALUES (165, 9996, 1, '1 year', '2024-01-01 00:00:00', '2025-01-01 00:00:00', 9.9, NULL, NULL, NULL, NULL, NULL, 23.7, 16.8, NULL);
INSERT INTO public.measurement VALUES (166, 9998, 1, '1 year', '2013-01-01 00:00:00', '2014-01-01 00:00:00', 52.9, NULL, NULL, NULL, NULL, NULL, 62.9, 57.9, NULL);
INSERT INTO public.measurement VALUES (167, 9998, 1, '1 year', '2014-01-01 00:00:00', '2015-01-01 00:00:00', 47.6, NULL, NULL, NULL, NULL, NULL, 57.6, 52.6, NULL);
INSERT INTO public.measurement VALUES (168, 9998, 1, '1 year', '2015-01-01 00:00:00', '2016-01-01 00:00:00', 49.2, NULL, NULL, NULL, NULL, NULL, 59.2, 54.2, NULL);
INSERT INTO public.measurement VALUES (169, 9998, 1, '1 year', '2016-01-01 00:00:00', '2017-01-01 00:00:00', 51.1, NULL, NULL, NULL, NULL, NULL, 61.1, 56.1, NULL);
INSERT INTO public.measurement VALUES (170, 9998, 1, '1 year', '2017-01-01 00:00:00', '2018-01-01 00:00:00', 53, NULL, NULL, NULL, NULL, NULL, 63, 58, NULL);
INSERT INTO public.measurement VALUES (171, 9998, 1, '1 year', '2018-01-01 00:00:00', '2019-01-01 00:00:00', 48.7, NULL, NULL, NULL, NULL, NULL, 58.7, 53.7, NULL);
INSERT INTO public.measurement VALUES (172, 9998, 1, '1 year', '2019-01-01 00:00:00', '2020-01-01 00:00:00', 49.5, NULL, NULL, NULL, NULL, NULL, 59.5, 54.5, NULL);
INSERT INTO public.measurement VALUES (173, 9998, 1, '1 year', '2020-01-01 00:00:00', '2021-01-01 00:00:00', 46.8, NULL, NULL, NULL, NULL, NULL, 56.8, 51.8, NULL);
INSERT INTO public.measurement VALUES (174, 9998, 1, '1 year', '2021-01-01 00:00:00', '2022-01-01 00:00:00', 46.5, NULL, NULL, NULL, NULL, NULL, 56.5, 51.5, NULL);
INSERT INTO public.measurement VALUES (175, 9998, 1, '1 year', '2022-01-01 00:00:00', '2023-01-01 00:00:00', 52.5, NULL, NULL, NULL, NULL, NULL, 62.5, 57.5, NULL);
INSERT INTO public.measurement VALUES (176, 9998, 1, '1 year', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 54.2, NULL, NULL, NULL, NULL, NULL, 64.2, 59.2, NULL);
INSERT INTO public.measurement VALUES (177, 9996, 1, '1 year', '2019-01-01 00:00:00', '2020-01-01 00:00:00', 10.1, NULL, NULL, NULL, NULL, NULL, 23.8, 17.4, NULL);


--
-- TOC entry 3387 (class 0 OID 16447)
-- Dependencies: 215
-- Data for Name: parameter; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.parameter VALUES (1, 'pm10', 'µg/m³', 'PM10', 'Particulate matter less than 10 micrometers in diameter mass concentration');
INSERT INTO public.parameter VALUES (2, 'pm25', 'µg/m³', 'PM2.5', 'Particulate matter less than 2.5 micrometers in diameter mass concentration');
INSERT INTO public.parameter VALUES (3, 'o3', 'µg/m³', 'O₃ mass', 'Ozone mass concentration');
INSERT INTO public.parameter VALUES (4, 'co', 'µg/m³', 'CO mass', 'Carbon Monoxide mass concentration');
INSERT INTO public.parameter VALUES (5, 'no2', 'µg/m³', 'NO₂ mass', 'Nitrogen Dioxide mass concentration');
INSERT INTO public.parameter VALUES (6, 'so2', 'µg/m³', 'SO₂ mass', 'Sulfur Dioxide mass concentration');
INSERT INTO public.parameter VALUES (7, 'no2', 'ppm', 'NO₂', 'Nitrogen Dioxide concentration');
INSERT INTO public.parameter VALUES (8, 'co', 'ppm', 'CO', 'Carbon Monoxide concentration');
INSERT INTO public.parameter VALUES (9, 'so2', 'ppm', 'SO₂', 'Sulfur Dioxide concentration');
INSERT INTO public.parameter VALUES (10, 'o3', 'ppm', 'O₃', 'Ozone concentration');
INSERT INTO public.parameter VALUES (11, 'bc', 'µg/m³', 'BC', 'Black Carbon mass concentration');
INSERT INTO public.parameter VALUES (15, 'no2', 'ppb', 'NO₂', NULL);
INSERT INTO public.parameter VALUES (19, 'pm1', 'µg/m³', 'PM1', 'Particulate matter less than 1 micrometer in diameter mass concentration');
INSERT INTO public.parameter VALUES (21, 'co2', 'ppm', 'CO₂', 'Carbon Dioxide concentration');
INSERT INTO public.parameter VALUES (22, 'wind_direction', 'deg', 'Wind direction', 'Direction that the wind originates from');
INSERT INTO public.parameter VALUES (23, 'nox', 'ppb', 'NOX', NULL);
INSERT INTO public.parameter VALUES (24, 'no', 'ppb', 'NO', NULL);
INSERT INTO public.parameter VALUES (27, 'nox', 'µg/m³', 'NOx mass', 'Nitrogen Oxides mass concentration');
INSERT INTO public.parameter VALUES (28, 'ch4', 'ppm', 'CH₄', 'Methane concentration');
INSERT INTO public.parameter VALUES (32, 'o3', 'ppb', 'O₃', NULL);
INSERT INTO public.parameter VALUES (33, 'ufp', 'particles/cm³', 'UFP count', 'Ultrafine Particles count concentration');
INSERT INTO public.parameter VALUES (34, 'wind_speed', 'm/s', 'Wind speed', 'Average wind speed in meters per second');
INSERT INTO public.parameter VALUES (35, 'no', 'ppm', 'NO', 'Nitrogen Monoxide concentration');
INSERT INTO public.parameter VALUES (95, 'pressure', 'hpa', 'Atmospheric pressure', 'Atmospheric or barometric pressure');
INSERT INTO public.parameter VALUES (97, 'pm25-old', 'ugm3', NULL, NULL);
INSERT INTO public.parameter VALUES (98, 'relativehumidity', '%', 'RH', NULL);
INSERT INTO public.parameter VALUES (100, 'temperature', 'c', 'Temperature (C)', NULL);
INSERT INTO public.parameter VALUES (101, 'so2', 'ppb', 'SO₂', NULL);
INSERT INTO public.parameter VALUES (102, 'co', 'ppb', 'CO', NULL);
INSERT INTO public.parameter VALUES (125, 'um003', 'particles/cm³', 'PM0.3 count', NULL);
INSERT INTO public.parameter VALUES (126, 'um010', 'particles/cm³', 'PM1 count', 'PM1 count');
INSERT INTO public.parameter VALUES (128, 'temperature', 'f', 'Temperature (F)', NULL);
INSERT INTO public.parameter VALUES (130, 'um025', 'particles/cm³', 'PM2.5 count', 'PM2.5 count');
INSERT INTO public.parameter VALUES (132, 'pressure', 'mb', 'Pressure', NULL);
INSERT INTO public.parameter VALUES (134, 'humidity', '%', 'H', NULL);
INSERT INTO public.parameter VALUES (135, 'um100', 'particles/cm³', 'PM10 count', 'PM10 count');
INSERT INTO public.parameter VALUES (19840, 'nox', 'ppm', 'NOx', 'Nitrogen Oxides concentration');
INSERT INTO public.parameter VALUES (19843, 'no', 'µg/m³', 'NO mass', 'Nitrogen Monoxide mass concentration');
INSERT INTO public.parameter VALUES (19844, 'pm4', 'µg/m³', 'PM4.0', 'Particulate matter less than 4 micrometers in diameter mass concentration');
INSERT INTO public.parameter VALUES (19860, 'pm25', 'ppm', NULL, NULL);
INSERT INTO public.parameter VALUES (9996, 'air_temperature', '°C', 'Air Temperature', 'Average air temperature measured in degrees Celsius');
INSERT INTO public.parameter VALUES (9997, 'radiation_load', 'W/m²', 'Radiation Load', 'Total solar and thermal radiation received by a surface');
INSERT INTO public.parameter VALUES (9998, 'humidity', '%', 'Relative Humidity', 'Percentage of water vapor in the air relative to the saturation point');
INSERT INTO public.parameter VALUES (9999, 'air_flow', 'km/h', 'Wind Speed', 'Average speed of air flow measured in kilometers per hour');


--
-- TOC entry 3388 (class 0 OID 16454)
-- Dependencies: 216
-- Data for Name: sensor; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sensor VALUES (6982, 'no2 µg/m³', 5, 40.424166659847046, -3.712222219623825);
INSERT INTO public.sensor VALUES (7101, 'no2 µg/m³', 5, 40.33971999947, -3.73583);
INSERT INTO public.sensor VALUES (25161, 'pm25 µg/m³', 2, 40.33971999947, -3.73583);
INSERT INTO public.sensor VALUES (1, '°C', 9996, 40.33961999947, -3.73583);


--
-- TOC entry 3391 (class 0 OID 16474)
-- Dependencies: 219
-- Data for Name: sensor_location; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.sensor_location VALUES (1, 3223, 6982);
INSERT INTO public.sensor_location VALUES (2, 3265, 7101);
INSERT INTO public.sensor_location VALUES (3, 3265, 25161);
INSERT INTO public.sensor_location VALUES (4, 3265, 1);


--
-- TOC entry 3403 (class 0 OID 0)
-- Dependencies: 220
-- Name: measurement_id_measure_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.measurement_id_measure_seq', 177, true);


--
-- TOC entry 3404 (class 0 OID 0)
-- Dependencies: 218
-- Name: sensor_location_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sensor_location_id_seq', 4, true);


--
-- TOC entry 3237 (class 2606 OID 16515)
-- Name: eu_threshold eu_threshold_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eu_threshold
    ADD CONSTRAINT eu_threshold_pkey PRIMARY KEY (id_threshold);


--
-- TOC entry 3231 (class 2606 OID 16472)
-- Name: location location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id_location);


--
-- TOC entry 3235 (class 2606 OID 16498)
-- Name: measurement measurement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.measurement
    ADD CONSTRAINT measurement_pkey PRIMARY KEY (id_measure);


--
-- TOC entry 3227 (class 2606 OID 16453)
-- Name: parameter parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter
    ADD CONSTRAINT parameter_pkey PRIMARY KEY (id_param);


--
-- TOC entry 3233 (class 2606 OID 16479)
-- Name: sensor_location sensor_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_location
    ADD CONSTRAINT sensor_location_pkey PRIMARY KEY (id);


--
-- TOC entry 3229 (class 2606 OID 16460)
-- Name: sensor sensor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor
    ADD CONSTRAINT sensor_pkey PRIMARY KEY (id_sensor);


--
-- TOC entry 3241 (class 2606 OID 16499)
-- Name: measurement fk_measurement_param; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.measurement
    ADD CONSTRAINT fk_measurement_param FOREIGN KEY (id_param) REFERENCES public.parameter(id_param);


--
-- TOC entry 3242 (class 2606 OID 16504)
-- Name: measurement fk_measurement_sensor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.measurement
    ADD CONSTRAINT fk_measurement_sensor FOREIGN KEY (id_sensor) REFERENCES public.sensor(id_sensor);


--
-- TOC entry 3239 (class 2606 OID 16480)
-- Name: sensor_location fk_sensor_location_location; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_location
    ADD CONSTRAINT fk_sensor_location_location FOREIGN KEY (id_location) REFERENCES public.location(id_location);


--
-- TOC entry 3240 (class 2606 OID 16485)
-- Name: sensor_location fk_sensor_location_sensor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_location
    ADD CONSTRAINT fk_sensor_location_sensor FOREIGN KEY (id_sensor) REFERENCES public.sensor(id_sensor);


--
-- TOC entry 3238 (class 2606 OID 16461)
-- Name: sensor fk_sensor_param; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor
    ADD CONSTRAINT fk_sensor_param FOREIGN KEY (id_param) REFERENCES public.parameter(id_param);


--
-- TOC entry 3243 (class 2606 OID 16516)
-- Name: eu_threshold fk_sensor_param; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eu_threshold
    ADD CONSTRAINT fk_sensor_param FOREIGN KEY (id_param) REFERENCES public.parameter(id_param);


-- Completed on 2025-06-03 16:20:02

--
-- PostgreSQL database dump complete
--

