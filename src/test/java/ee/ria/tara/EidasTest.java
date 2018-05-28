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

import static ee.ria.tara.config.TaraTestStrings.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = EidasTest.class)
@Category(IntegrationTest.class)
public class EidasTest extends TestsBase {

    @Test
    public void eidas1_eidasAuthenticationMinAttrSuccess() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_LOW);

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
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
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
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
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
    public void eidas2_eidasAuthenticationFailure() throws URISyntaxException {
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "AuthFailed");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Autentimine eIDAS-ga ebaõnnestus", error);
    }

    @Test
    public void eidas2_eidasConsentFailure() throws URISyntaxException {
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "ConsentNotGiven");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Autentimine eIDAS-ga ebaõnnestus", error);
    }

    @Test
    public void eidas2_eidasRandomFailure() throws URISyntaxException {
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseWithErrors(response.getBody().asString(), "RandomFailure");

        Response errorResponse = returnEidasErrorResponse(samlResponse, relayState);

        String error = errorResponse.htmlPath().getString("**.findAll { it.@class=='error-box' }").substring(4);

        assertEquals("Üldine viga", error);
    }

    @Ignore
    @Test //TODO: eIDAS Node do not relay the relayState!
    public void eidas2_eidasWrongRelayState() throws URISyntaxException, ParseException, JOSEException {
        Response response = initiateEidasAuthentication("EE", OIDC_DEF_SCOPE);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_LOW);

        String authorizationCode = getAuthorizationCode(returnEidasResponse(samlResponse, "a"+relayState));
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE30011092212", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals(DEFATTR_FIRST, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals(DEFATTR_FAMILY, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(DEFATTR_DATE, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
        assertEquals(OIDC_AMR_EIDAS, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
    }
}
