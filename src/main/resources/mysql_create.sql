/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  Dave
 * Created: May 30, 2017
 */

CREATE DATABASE sensors;

CREATE TABLE sensors
(
  id BIGINT NOT NULL AUTO_INCREMENT,
  name text,
  serial text,
  type smallint,
  PRIMARY KEY (id)
);

CREATE TABLE events
(
  id BIGINT NOT NULL AUTO_INCREMENT,
  sensorId bigint NOT NULL,
  temperature float,
  timestamp DATETIME NOT NULL,
  PRIMARY KEY (id)
);