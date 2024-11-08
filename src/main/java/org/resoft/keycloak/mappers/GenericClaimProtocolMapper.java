package org.resoft.keycloak.mappers;

import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GenericClaimProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger log = LoggerFactory.getLogger(GenericClaimProtocolMapper.class);

    public static final String PROVIDER_ID = "generic-claim-mapper";
    private static final String CLAIM_NAME = "claim.name";
    private static final String ATTRIBUTE_SOURCE = "attribute.source";

    @Override
    public String getDisplayCategory() {
        return "Claim Mappers";
    }

    @Override
    public String getDisplayType() {
        return "Generic Claim Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds a custom claim to the token based on a specified attribute source (e.g., User Session or Request Parameter).";
    }


    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        configProperties.add(getProviderConfigProperty(
                CLAIM_NAME,
                "Claim Nane",
                "Name of the claim to be added to the token.",
                ProviderConfigProperty.STRING_TYPE,
                List.of("")
        ));

        configProperties.add(getProviderConfigProperty(
                ATTRIBUTE_SOURCE,
                "Attribute Source",
                "Source of the attribute value. " +
                        "Choose 'session' to retrieve from user attributes. " +
                        "or 'request parameters' to retrieve from the request parameters. " +
                        "or 'headers' to retrieve from the headers parameters.",
                ProviderConfigProperty.LIST_TYPE,
                List.of("session", "request_parameters", "headers")
        ));


        ProviderConfigProperty accessTokenClaimProperty = new ProviderConfigProperty();
        accessTokenClaimProperty.setName("access.token.claim");
        accessTokenClaimProperty.setLabel("Add to Access Token");
        accessTokenClaimProperty.setHelpText("Include this claim in the access token.");
        accessTokenClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        accessTokenClaimProperty.setDefaultValue("true"); // "On"
        configProperties.add(accessTokenClaimProperty);

        ProviderConfigProperty idTokenClaimProperty = new ProviderConfigProperty();
        idTokenClaimProperty.setName("id.token.claim");
        idTokenClaimProperty.setLabel("Add to ID Token");
        idTokenClaimProperty.setHelpText("Include this claim in the ID token.");
        idTokenClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        idTokenClaimProperty.setDefaultValue("true"); // "On"
        configProperties.add(idTokenClaimProperty);

        ProviderConfigProperty lightweightClaimProperty = new ProviderConfigProperty();
        lightweightClaimProperty.setName("lightweight.claim");
        lightweightClaimProperty.setLabel("Add to Lightweight Access Token");
        lightweightClaimProperty.setHelpText("Specify if this claim is lightweight.");
        lightweightClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        lightweightClaimProperty.setDefaultValue("false"); // "Off"
        configProperties.add(lightweightClaimProperty);

        ProviderConfigProperty userInfoTokenClaimProperty = new ProviderConfigProperty();
        userInfoTokenClaimProperty.setName("userinfo.token.claim");
        userInfoTokenClaimProperty.setLabel("Add to User Info");
        userInfoTokenClaimProperty.setHelpText("Include this claim in the user info token.");
        userInfoTokenClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        userInfoTokenClaimProperty.setDefaultValue("false"); // "Off"
        configProperties.add(userInfoTokenClaimProperty);

        ProviderConfigProperty accessTokenResponseClaimProperty = new ProviderConfigProperty();
        accessTokenResponseClaimProperty.setName("access.tokenResponse.claim");
        accessTokenResponseClaimProperty.setLabel("Add to Access Token Response");
        accessTokenResponseClaimProperty.setHelpText("Include this claim in the token response.");
        accessTokenResponseClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        accessTokenResponseClaimProperty.setDefaultValue("true"); // "On"
        configProperties.add(accessTokenResponseClaimProperty);

        ProviderConfigProperty introspectionTokenClaimProperty = new ProviderConfigProperty();
        introspectionTokenClaimProperty.setName("introspection.token.claim");
        introspectionTokenClaimProperty.setLabel("Add to Token Introspection");
        introspectionTokenClaimProperty.setHelpText("Include this claim in the introspection response.");
        introspectionTokenClaimProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        introspectionTokenClaimProperty.setDefaultValue("true"); // "On"
        configProperties.add(introspectionTokenClaimProperty);


        return configProperties;
    }

    private static ProviderConfigProperty getProviderConfigProperty(String name, String label, String helpText, String type, List<String> options) {
        ProviderConfigProperty attributeSourceProperty = new ProviderConfigProperty();
        attributeSourceProperty.setName(name);
        attributeSourceProperty.setLabel(label);
        attributeSourceProperty.setHelpText(helpText);
        attributeSourceProperty.setType(type);
        if (!options.isEmpty()) {
            attributeSourceProperty.setOptions(options);
        }

        return attributeSourceProperty;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        String claimName = mappingModel.getConfig().get(CLAIM_NAME);
        String attributeSource = mappingModel.getConfig().get(ATTRIBUTE_SOURCE);
        String claimValue = null;

        if ("session".equals(attributeSource)) {
            claimValue = userSession.getUser().getFirstAttribute(claimName);
        } else if ("request_parameters".equals(attributeSource)) {
            MultivaluedMap<String, String> parameters = session.getContext().getHttpRequest().getDecodedFormParameters();
            claimValue = parameters.getFirst(claimName);
        } else if ("headers".equals(attributeSource)) {
            claimValue = session.getContext().getHttpRequest().getHttpHeaders().getHeaderString(claimValue);
        }

        if (claimValue != null && !claimValue.isEmpty()) {
            log.info("Adding claim '{}' with value '{}' to the token.", claimName, claimValue);
            token.getOtherClaims().put(claimName, claimValue);
        } else {
            log.warn("Claim '{}' not found in the specified source: '{}'.", claimName, attributeSource);
        }

        return token;
    }

}