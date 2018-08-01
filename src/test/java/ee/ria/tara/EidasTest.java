package ee.ria.tara;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static ee.ria.tara.config.TaraTestStrings.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = EidasTest.class)
@Category(IntegrationTest.class)
public class EidasTest extends TestsBase {

    @Test
    public void eidas1_eidasAuthenticationMinAttrSuccess() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, "low");
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_SUBSTANTIAL);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
    }

    @Test
    public void eidas1_eidasAuthenticationMaxAttrSuccess() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseDefaultMaximalAttributes(response.getBody().asString());

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
    }

    @Test
    public void eidas1_eidasAuthenticationMaxLegalAttrSuccess() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseDefaultMaximalAttributes(response.getBody().asString());

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
    }

    @Test
    public void eidas2_eidasAuthenticationFailure() {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "AuthFailed");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Autentimine eIDAS-ga ebaõnnestus.", error);
    }

    @Test
    public void eidas2_eidasConsentFailure() {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "ConsentNotGiven");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Autentimine eIDAS-ga ebaõnnestus.", error);
    }

    @Test
    public void eidas2_eidasRandomFailure() {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "RandomFailure");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Üldine viga", error);
    }

    @Test
    public void eidas3_eidasAcrValueLowShouldReturnSuccess() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");
        formParams.put("acr_values", OIDC_ACR_VALUES_LOW);

        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        String loa = getDecodedSamlRequestBodyXml(response.getBody().asString()).getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, loa);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals(OIDC_ACR_VALUES_LOW, signedJWT.getJWTClaimsSet().getClaim("acr"));
    }

    @Test
    public void eidas3_eidasAcrValueSubstantialShouldReturnSuccess() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");
        formParams.put("acr_values", OIDC_ACR_VALUES_SUBSTANTIAL);

        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        String loa = getDecodedSamlRequestBodyXml(response.getBody().asString()).getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, loa);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals(OIDC_ACR_VALUES_SUBSTANTIAL, signedJWT.getJWTClaimsSet().getClaim("acr"));
    }

    @Test
    public void eidas3_eidasAcrValueHighShouldReturnSuccess() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");
        formParams.put("acr_values", OIDC_ACR_VALUES_HIGH);

        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        String loa = getDecodedSamlRequestBodyXml(response.getBody().asString()).getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, loa);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals(OIDC_ACR_VALUES_HIGH, signedJWT.getJWTClaimsSet().getClaim("acr"));
    }

    @Test
    public void eidas3_eidasAcrValueDefaultShouldReturnSuccess() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");

        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        String loa = getDecodedSamlRequestBodyXml(response.getBody().asString()).getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, loa);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals("Default loa is substantial", OIDC_ACR_VALUES_SUBSTANTIAL, signedJWT.getJWTClaimsSet().getClaim("acr"));
    }

    @Test
    public void eidas3_eidasAcrValueHigherLoaReturnedThanAskedShouldReturnSuccess() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");
        formParams.put("acr_values", OIDC_ACR_VALUES_LOW);


        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");
        //Here we need to simulate a response from foreign country eIDAS Node

        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_HIGH);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals(OIDC_ACR_VALUES_HIGH, signedJWT.getJWTClaimsSet().getClaim("acr"));
    }

    @Test
    public void eidas3_eidasAcrValueLowerLoaReturnedThanAskedShouldReturnError() throws URISyntaxException, ParseException, JOSEException {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("scope", "openid");
        formParams.put("response_type", "code");
        formParams.put("client_id", testTaraProperties.getClientId());
        formParams.put("redirect_uri", testTaraProperties.getTestRedirectUri());
        formParams.put("lang", "et");
        formParams.put("acr_values", OIDC_ACR_VALUES_SUBSTANTIAL);

        String execution = getAuthenticationMethodsPageWithParams(formParams).getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
        Response response = getEidasSamlRequest(DEF_COUNTRY, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_LOW);

        Response errorResponse = returnEidasFailureResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='sub-title' }");

        assertEquals("An unexpected error has occurred", error);
    }

    @Ignore
    @Test //TODO: eIDAS Node do not forward the relayState!
    public void eidas4_eidasWrongRelayState() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication(DEF_COUNTRY, OIDC_DEF_SCOPE, null);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        String loa = getDecodedSamlRequestBodyXml(response.getBody().asString()).getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, loa);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, "a"+relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
    }
}
