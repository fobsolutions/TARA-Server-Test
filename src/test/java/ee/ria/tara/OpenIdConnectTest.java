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

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = OpenIdConnectTest.class)
@Category(IntegrationTest.class)
public class OpenIdConnectTest extends TestsBase {

    @Test
    public void happyPathTest() throws InterruptedException, URISyntaxException, ParseException, JOSEException {
        String authorizationCode = authenticateWithMobileId("00000266", "60001019896", 2000);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000266", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }


    @Test
    public void requestTokenTwiceShouldFail() throws InterruptedException, URISyntaxException {
        String authorizationCode = authenticateWithMobileId("+37200000266", "60001019896", 2000);
        getIdToken(authorizationCode);
        Response response = postToTokenEndpoint(authorizationCode);
        assertEquals(400, response.statusCode());
        assertEquals("invalid_grant", response.body().jsonPath().getString("error"));
    }

}
