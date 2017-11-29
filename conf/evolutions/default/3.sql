# Añadir tabla season y clave foranea a tvShow

# --- !Ups

CREATE TABLE episode (
  id int(11) NOT NULL AUTO_INCREMENT,
  episodeNumber int(11),
  firstAired datetime,
  name varchar(255),
  overview text,
  screenshot varchar(255),
  seasonId int(11),
  PRIMARY KEY (id),
  FOREIGN KEY (seasonId) REFERENCES season(id)
);

# --- !Downs
DROP TABLE episode;
