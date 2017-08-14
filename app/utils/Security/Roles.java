package utils.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.service.UserService;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;

public class Roles extends Security.Authenticator {
  UserService userService;
  final String SECRET = "debug";
  final String ISSUER = "TrendingSeries";

  @Inject
  public Roles(UserService userService) {
    super();
    this.userService = userService;
  }

  @Override
  public String getUsername(Http.Context context) {
    String rawToken = getTokenFromHeader(context);

    if (rawToken != null && rawToken.length() > 6) {
      String token = rawToken.substring(7);

      try {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        Claim emailClaim = jwt.getClaim("email");
        Claim rolClaim = jwt.getClaim("rol");
        // comprobamos si los claims no son null
        if (!emailClaim.isNull() && !rolClaim.isNull()) {
            // comprobamos que el usuario exista
            User user = userService.findByEmail(emailClaim.asString());
            if (user != null) {
              return user.email;
            } else {
              Logger.warn("jwt roles - se ha detectado token de usuario que no existe");
              return null;
            }
        } else {
          Logger.warn("jwt roles - se ha detectado token sin claim email y/o rol");
          return null;
        }

      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt roles - verifier.verify(token) ha generado UnsupportedEncodingException");
        return null;
      } catch (JWTVerificationException ex) {
        Logger.debug("jwt roles - verifier.verify(token) ha generado JWTVerificationException");
        return null;
      }
    }
    Logger.warn("jwt roles - se ha deducido que el usuario no tiene autorización?");
    return null;
  }

  @Override
  public Result onUnauthorized(Http.Context context) {
    ObjectNode result = Json.newObject();
    result.put("error", "Unauthorized");
    result.put("type", "unauthorized");
    result.put("message", "No estás autorizado");
    return unauthorized(result);
  }

  public String createJWT(User user) {
    String token;

    try {
      Algorithm algorithm = Algorithm.HMAC256(SECRET);
      token = JWT.create()
              .withIssuer(ISSUER)
              .withClaim("email", user.email)
              .withClaim("rol", user.rol)
              .sign(algorithm);
    } catch (UnsupportedEncodingException ex) {
      Logger.error("jwt - JWT.create() ha generado UnsupportedEncodingException");
      return null;
    } catch (JWTCreationException ex) {
      Logger.error("jwt - JWT.create() ha generado JWTCreationException");
      return null;
    }
    Logger.info("Creado jwt para " + user.email);
    return token;
  }

  public Boolean verifyJWT(Http.Context context) {
    Boolean result = false;
    String rawToken = getTokenFromHeader(context);

    if (rawToken != null && rawToken.length() > 6) {
      String token = rawToken.substring(7);

      try {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        verifier.verify(token);
        result = true;
      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt - JWT.verify() ha generado UnsupportedEncodingException");
      } catch (JWTVerificationException ex) {
        Logger.error("jwt - JWT.verify() ha generado JWTVerificationException");
      }

    }

    return result;
  }

  String getTokenFromHeader(Http.Context context) {
    String [] authTokenHeaderValues = context.request().headers().get("Authorization");
    if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
      return authTokenHeaderValues[0];
    }
    return null;
  }
}
