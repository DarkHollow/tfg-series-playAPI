package models.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Serie;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utils.TVDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TvdbService {

  private final WSClient ws;
  private final SimpleDateFormat df;
  private final SerieService serieService;

  @Inject
  public TvdbService(WSClient ws, SimpleDateFormat df, SerieService serieService) {
    this.ws = ws;
    this.df = df;
    this.serieService = serieService;
  }

  // buscar en tvdb y en local, marcar las que coinciden
  // buscar por campo exacto o LIKE
  public List<Serie> findOnTVDBby(String field, String value) {
    // buscamos la serie en tvdb
    JsonNode respuesta = null;
    List<Serie> series = new ArrayList<>();

    try {
      CompletionStage<JsonNode> stage = ws.url("https://api.thetvdb.com/search/series")
                                                .setHeader("Authorization", "Bearer " + TVDB.getToken())
                                                .setHeader("Accept-Language", "es")
                                                .setQueryParameter(field, value)
                                                .get()
                                                .thenApply(WSResponse::asJson);

      respuesta = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);

      System.out.println(respuesta);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    // recorremos series encontradas en TVDB
    if (respuesta != null && respuesta.has("data")) {
      for (JsonNode jsonSerie : respuesta.withArray("data")) {
        // obtenemos datos de la serie
        Serie nuevaSerie = new Serie();
        nuevaSerie.idTVDB = Integer.parseInt(jsonSerie.get("id").asText()); // id de tvdb
        nuevaSerie.seriesName = jsonSerie.get("seriesName").asText();       // nombre de la serie
        nuevaSerie.banner = jsonSerie.get("banner").asText();               // banner de la serie
        nuevaSerie.local = false;                                           // iniciamos por defecto a false
        try {
          df.applyPattern("yyyy-MM-dd");
          nuevaSerie.firstAired = df.parse(jsonSerie.get("firstAired").asText()); // fecha estreno
        } catch (ParseException e) {
          e.printStackTrace();
        }

        // buscamos en local para indicar si la tenemos o no
        Serie serieLocal = serieService.findByIdTvdb(nuevaSerie.idTVDB);
        if (serieLocal != null) {
          nuevaSerie.local = true;
        }

        // finalmente la añadimos a la lista
        series.add(nuevaSerie);
      }
    }

    return series;
  }

  // POST petición serie
  public Boolean requestSeries(Integer idTVDB) {
    Boolean result = false;

    // comprobamos que no esté en nuetra base de datos ya
    if (serieService.findByIdTvdb(idTVDB) == null) {
      // hacer peticion de la serie
      // TODO:

    }

    return result;
  }

}
