# Añadir tabla season y clave foranea a tvShow

# --- !Ups

ALTER TABLE TvShow ADD COLUMN tmdbId integer;

CREATE TABLE season (
  id int(11) NOT NULL AUTO_INCREMENT,
  firstAired datetime,
  name varchar(255),
  overview text,
  poster varchar(255),
  seasonNumber int(11),
  tvShowId int(11),
  PRIMARY KEY (id),
  FOREIGN KEY (tvShowId) REFERENCES tvShow(id)
);

# --- !Downs
ALTER TABLE TvShow DROP COLUMN tmdbId;
DROP TABLE season;
