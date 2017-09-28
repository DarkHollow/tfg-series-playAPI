# Añadir a TvShow atributos score y voteCount

# --- !Ups

ALTER TABLE TvShow ADD COLUMN score FLOAT;
ALTER TABLE TvShow ADD COLUMN voteCount INTEGER;

# --- !Downs
ALTER TABLE TvShow DROP COLUMN score;
ALTER TABLE TvShow DROP COLUMN voteCount;
