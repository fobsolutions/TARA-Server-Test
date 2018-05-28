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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

@SpringBootTest(classes = OpenIdConnectTest.class)
@Category(IntegrationTest.class)
public class OpenIdConnectTest extends TestsBase {

    @Test
    public void oidc1_authenticationWithMidShouldSucceed() throws InterruptedException, URISyntaxException, ParseException, JOSEException {
        String authorizationCode = authenticateWithMobileId("00000766", "60001019906", 3000, OIDC_DEF_SCOPE);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals(OIDC_AMR_MID, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals("EE60001019906", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000766", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }

    @Ignore
    @Test
    public void oidc1_authenticationWithEidasShouldSucceed() throws InterruptedException, URISyntaxException, ParseException, JOSEException {

    }

    @Test
    public void oidc2_requestTokenTwiceShouldFail() throws InterruptedException, URISyntaxException {
        String authorizationCode = authenticateWithMobileId("00000766", "60001019906", 3000, OIDC_DEF_SCOPE);
        getIdToken(authorizationCode);
        Response response = postToTokenEndpoint(authorizationCode);
        assertEquals(400, response.statusCode());
        assertEquals("invalid_grant", response.body().jsonPath().getString("error"));
    }

    @Test //TODO: Error handling is changed with AUT-57
    public void oidc2_mandatoryScopeMissingErrorMustBeReturned() {
        Response response = getAuthenticationMethodsPage("eidasonly");
        assertThat("openid scope must be present error", response.body().asString(), startsWith("RESPONSE ERROR: invalid_scope - Required scope <openid> not provided.") );
    }

    @Test //TODO: Error handling is changed with AUT-57
    public void oidc2_emptyScopeErrorMustBeReturned() {
        Response response = getAuthenticationMethodsPage(null);
        assertThat("scope missing error", response.body().asString(), startsWith("RESPONSE ERROR: invalid_scope - No value found in the request for <scope> parameter") );
    }

    @Test //TODO: Error handling is changed with AUT-57
    public void oidc2_notKnownScopeErrorMustBeReturned() {
        Response response = getAuthenticationMethodsPage("openid newscope");
        assertThat("scope missing error", response.body().asString(), startsWith("RESPONSE ERROR: invalid_scope - One or some of the provided scopes are not allowed by TARA, only <openid, eidasonly> are permitted.") );
    }

    @Test
    public void oidc3_eidasOnlyScopeShouldShowOnlyEidas() {
        Response response = getAuthenticationMethodsPage(OIDC_EIDAS_ONLY_SCOPE);

        assertEquals("Only eIDAS must be present", true, isEidasPresent(response));
        assertEquals("Only eIDAS must be present", false, isMidPresent(response));
        assertEquals("Only eIDAS must be present", false, isIdCardPresent(response));
    }

    @Test
    public void oidc3_allAuthenticationMethodsShouldBePresent() {
        Response response = getAuthenticationMethodsPage(OIDC_DEF_SCOPE);

        assertEquals("eIDAS must be present", true, isEidasPresent(response));
        assertEquals("MID must be present", true, isMidPresent(response));
        assertEquals("ID-Card must be present", true, isIdCardPresent(response));
    }
}
