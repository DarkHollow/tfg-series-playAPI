package utils.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import models.User;
import models.service.UserService;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;

public class Auth extends Security.Authenticator {
  private UserService userService;
  private final String SECRET = "debug";
  private final String ISSUER = "TrendingSeries";

  @Inject
  public Auth(UserService userService) {
    super();
    this.userService = userService;
  }

  @Override
  public String getUsername(Http.Context context) {
    System.out.println("\nComprobando token usuario...");
    String rawToken = getTokenFromHeader(context);
    String path = context.request().path();

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

        if (!emailClaim.isNull() && !emailClaim.asString().equals("")) {
          User user = userService.findByEmail(emailClaim.asString());
          if (user != null && user.rol.equals(rolClaim.asString())) {
            // si el contenido solicitado es tipo admin
            System.out.println("Path: " + path);
            if (path.contains("/admin")) {
              System.out.println("El contenido solicitado es admin");
              // el rol debe ser admin
              if (rolClaim.asString().equals("a")) {
                return user.email;
              }
            // si no es tipo admin el contenido solicitado, el rol no importa
            } else {
              System.out.println("El contenido solicitado NO es admin");
              return user.email;
            }
          }
        }

      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt - verifier.verify(token) ha generado UnsupportedEncodingException");
        return null;
      } catch (JWTVerificationException ex) {
        Logger.debug("jwt - verifier.verify(token) ha generado JWTVerificationException");
        return null;
      }
    } else {
      System.out.println(rawToken);
      System.out.println("Sin token !");
    }

    return null;
  }

  @Override
  public Result onUnauthorized(Http.Context context) {
    String path = context.request().path();

    // si el path contiene /admin y no está indentificado como tal, enviar a login
    if (path.contains("/admin")) {
      return redirect(controllers.routes.AdminController.loginView());
    }

    return super.onUnauthorized(context);
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
                .withIssuer("TrendingSeries")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        result = true;
      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt - JWT.verify() ha generado UnsupportedEncodingException");
      } catch (JWTVerificationException ex) {
        Logger.error("jwt - JWT.verify() ha generado JWTVerificationException");
      }

    }

    return result;
  }

  private String getTokenFromHeader(Http.Context context) {
    String [] authTokenHeaderValues = context.request().headers().get("Authorization");
    if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
      return authTokenHeaderValues[0];
    }
    return null;
  }
}
