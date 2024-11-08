package org.resoft.keycloak.endpoints.token_inspect;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class TokenInspectProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public TokenInspectProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new TokenResource(session);
    }

    @Override
    public void close() {
    }

}