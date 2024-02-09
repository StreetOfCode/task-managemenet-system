DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(45) NOT NULL,
  email varchar(45) NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS project;
CREATE TABLE project (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  name varchar(45) NOT NULL,
  description varchar(160),
  created_at datetime NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT project_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

DROP TABLE IF EXISTS task;
CREATE TABLE task (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  project_id bigint,
  name varchar(45) NOT NULL,
  description varchar(160),
  status varchar(10) NOT NULL,
  created_at datetime NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT task_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT task_project_id_fk FOREIGN KEY (project_id) REFERENCES project (id)
);
