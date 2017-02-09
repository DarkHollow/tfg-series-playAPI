package models.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Serie;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utils.TVDB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TvdbService {
    @Inject
    private WSClient ws;

  // buscar en tvdb y en local, marcar las que coinciden
  // buscar por campo exacto o LIKE
  public List<Serie> findOnTVDBby(String field, String value) {
    // buscamos la serie en tvdb
    JsonNode respuesta = null;
    List<Serie> series = new ArrayList<>();

    try {
      CompletionStage<JsonNode> response = ws.url("https://api.thetvdb.com/search/series")
                                                .setHeader("Authorization", "Bearer " + TVDB.getToken())
                                                .setHeader("Accept-Language", "es")
                                                .setQueryParameter(field, value)
                                                .get()
                                                .thenApply(WSResponse::asJson);

      respuesta = response.toCompletableFuture().get(5, TimeUnit.SECONDS);

      System.out.println(respuesta);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    // recorremos series encontradas en TVDB
    if (respuesta != null && respuesta.has("data")) {
      for (JsonNode jsonSerie : respuesta.withArray("data")) {
        // obtenemos datos de la serie
        Serie nuevaSerie = new Serie();
        nuevaSerie.idTVDB = Integer.parseInt(jsonSerie.get("id").asText());
        nuevaSerie.seriesName = jsonSerie.get("seriesName").asText();
        nuevaSerie.banner = jsonSerie.get("banner").asText();

        // buscamos en local para indicar si la tenemos o no
        Serie serieLocal = SerieService.findByIdTvdb(nuevaSerie.idTVDB);
        if (serieLocal != null) {
          nuevaSerie.local = true;
        }

        // finalmente la añadimos a la lista
        series.add(nuevaSerie);
      }
    }

    return series;
  }

}
