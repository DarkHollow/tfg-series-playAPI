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

        if (!emailClaim.isNull() && !emailClaim.asString().equals("")) {
          User user = userService.findByEmail(emailClaim.asString());
          if (user != null) {
            return user.email;
          }
        }

      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt - verifier.verify(token) ha generado UnsupportedEncodingException");
        return null;
      } catch (JWTVerificationException ex) {
        Logger.debug("jwt - verifier.verify(token) ha generado JWTVerificationException");
        return null;
      }
    }

    return null;
  }

  @Override
  public Result onUnauthorized(Http.Context context) {
    return super.onUnauthorized(context);
  }

  public String createJWT(User user) {
    String token;

    try {
      Algorithm algorithm = Algorithm.HMAC256(SECRET);
      token = JWT.create()
              .withIssuer(ISSUER)
              .withClaim("email", user.email)
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
        Algorithm algorithm = Algorithm.HMAC256("prueba");
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
