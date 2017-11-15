package models.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExternalUtils {

  // parsear fecha
  Date parseDate(JsonNode jsonDate) {
    int i = 0, tries = 100;
    Date result = null;
    SimpleDateFormat df = new SimpleDateFormat();

    if (!jsonDate.isNull() && !jsonDate.asText().equals("")) {

      while (i < tries) {
        try {
          df.applyPattern("yyyy-MM-dd");
          result = df.parse(jsonDate.asText()); // fecha estreno
          break;
        } catch (Exception e) {
          i++;
          if (i != 100) {
            Logger.info("Reintentando parsear fecha de estreno");
          } else {
            Logger.error("No se ha podido parsear la fecha");
          }
        }
      }
    }

    return result;
  }

  // descargar una imagen desde una url a un directorio local
  String downloadImage(URL url, String format, String path) {
    BufferedImage image;
    try {
      // descargamos imagen
      image = ImageIO.read(url);
      File imageFile = new File(path);
      // creamos carpetas
      Boolean foldersCreated = imageFile.getParentFile().mkdirs();
      Logger.info("Ruta creada: " + foldersCreated);
      // guardamos imagen
      ImageIO.write(image, format, imageFile);
    } catch (Exception ex) {
      Logger.error("Download Image - error descargando imagen");
      return null;
    }
    return path;
  }

  // generar mitad de hashCode y positivo
  int positiveHalfHashCode(int hashCode) {
    int halfHashCode = hashCode / 2;
    return Math.abs(halfHashCode);
  }

  void deleteOldImages(String folderPath, String type, String fileName) {
    final File folder = new File(folderPath);
    final File[] files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return (name.startsWith(type.substring(0, 1)) && !name.matches(fileName));
      }
    });

    if (files != null) {
      for (final File file: files) {
        if (!file.delete()) {
          Logger.error("No se ha podido borrar el fichero " + file.getAbsolutePath());
        }
      }
    }

  }



}
