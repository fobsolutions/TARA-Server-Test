package ee.ria.tara;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = MobileIdTest.class)
@Category(IntegrationTest.class)
public class MobileIdTest extends TestsBase {

    @Test
    public void mobileIdAuthenticationSuccess() throws InterruptedException, URISyntaxException, ParseException, JOSEException, BadJOSEException, MalformedURLException {
        String authorizationCode = authenticateWithMobileId("+37200000266", "60001019896");
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000266", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }

    @Test
    public void mobileIdAuthenticationUserCertificatesRevoked() {
        String errorMessage = authenticateWithMobileIdError("00000266","60001019939");
        assertEquals("nullAutentimine Teie Mobiil-ID-ga ei õnnestunud. Testi oma Mobiil-ID toimimist haldusvahendis: http://www.id.ee/index.php?id=35636", errorMessage);
    }


}
