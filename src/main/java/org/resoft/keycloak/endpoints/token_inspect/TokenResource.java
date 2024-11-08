package org.resoft.keycloak.endpoints.token_inspect;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

public class TokenResource {
    private final AuthenticationManager.AuthResult auth;

    public TokenResource(KeycloakSession session) {
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUsersByAttr() {
        checkRealmAccess();
        AccessToken token = auth.getToken();
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(token)
                .build();
    }

    private void checkRealmAccess() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }
    }

}
