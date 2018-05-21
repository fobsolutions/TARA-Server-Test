package ee.ria.tara;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.URISyntaxException;
import java.text.ParseException;

import static ee.ria.tara.config.TaraTestStrings.OIDC_AMR_MID;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = OpenIdConnectTest.class)
@Category(IntegrationTest.class)
public class OpenIdConnectTest extends TestsBase {

    @Test
    public void happyPathTest() throws InterruptedException, URISyntaxException, ParseException, JOSEException {
        String authorizationCode = authenticateWithMobileId("00000766", "60001019906", 3000);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals(OIDC_AMR_MID, signedJWT.getJWTClaimsSet().getStringArrayClaim("amr")[0]);
        assertEquals("EE60001019906", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000766", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }


    @Test
    public void requestTokenTwiceShouldFail() throws InterruptedException, URISyntaxException {
        String authorizationCode = authenticateWithMobileId("00000766", "60001019906", 3000);
        getIdToken(authorizationCode);
        Response response = postToTokenEndpoint(authorizationCode);
        assertEquals(400, response.statusCode());
        assertEquals("invalid_grant", response.body().jsonPath().getString("error"));
    }

}
