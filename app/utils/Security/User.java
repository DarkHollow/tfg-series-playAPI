package utils.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import models.service.UserService;
import play.Logger;
import play.mvc.Http;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;

public class User extends Roles {
  private final String ROL = "u";

  @Inject
  public User(UserService userService) {
    super(userService);
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
          // comprobamos si el claim rol es usuario
          if (rolClaim.asString().equals(ROL)) {
            // comprobamos que el usuario exista
            models.User user = userService.findByEmail(emailClaim.asString());
            if (user != null) {
              // comprobamos que el rol del usuario es de usuario
              if (user.rol.equals(ROL)) {
                return user.email;
              } else {
                Logger.warn("jwt user - se ha detectado token de usuario que no es user");
                return null;
              }
            } else {
              Logger.warn("jwt user - se ha detectado token de usuario que no existe");
              return null;
            }
          } else {
            Logger.warn("jwt user - se ha detectado token con claim rol = " + rolClaim.asString());
            return null;
          }
        } else {
          Logger.warn("jwt user - se ha detectado token sin claim email y/o rol");
          return null;
        }

      } catch (UnsupportedEncodingException ex) {
        Logger.error("jwt user - verifier.verify(token) ha generado UnsupportedEncodingException");
        return null;
      } catch (JWTVerificationException ex) {
        Logger.debug("jwt user - verifier.verify(token) ha generado JWTVerificationException");
        return null;
      }
    }

    return null;
  }

}
