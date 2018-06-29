package ee.ria.tara;


import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import ee.ria.tara.utils.Feature;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static ee.ria.tara.config.TaraTestStrings.OIDC_DEF_SCOPE;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = SmartIdTest.class)
@Category(IntegrationTest.class)
public class SmartIdTest extends TestsBase {
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @Feature("TSID-12, TSID-13")
    public void smartidSuccess() throws Exception {
        String authorizationCode = authenticateWithSmartId("10101010005", 2000, OIDC_DEF_SCOPE);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE10101010005", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("DEMO", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("SMART-ID", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("1801-01-01", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    @Test
    @Feature("TSID-11")
    public void smartid_UserRefuses() throws Exception {
        String errorMessage = authenticateWithSmartIdPollError("10101010016", 2000);
        assertEquals("Autentimine katkestati kasutaja poolt.", errorMessage);
    }
}
