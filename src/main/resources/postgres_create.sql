/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  Dave
 * Created: May 30, 2017
 */

CREATE DATABASE sensors
  WITH ENCODING='UTF8'
       CONNECTION LIMIT=-1;

CREATE TABLE sensors
(
  id serial NOT NULL,
  name text,
  serial text,
  type smallint,
  min real,
  max real,
  CONSTRAINT sensors_pkey PRIMARY KEY (id)
);

CREATE TABLE events
(
  id serial NOT NULL,
  "sensorid" bigint NOT NULL,
  "devicetype" smallint NOT NULL,
  "temperature" float,
  "timestamp" timestamp with time zone NOT NULL,
  CONSTRAINT events_pkey PRIMARY KEY (id)
);

GRANT ALL ON SEQUENCE sensors_id_seq TO public;
GRANT ALL ON SEQUENCE events_id_seq TO public;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE sensors TO public;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE events TO public;
