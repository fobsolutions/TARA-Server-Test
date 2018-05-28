package ee.ria.tara;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

import static ee.ria.tara.config.TaraTestStrings.OIDC_DEF_SCOPE;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = MobileIdTest.class)
@Category(IntegrationTest.class)
public class MobileIdTest extends TestsBase {

    @Ignore
    @Test
    public void mob1_mobileIdAuthenticationSuccess() throws InterruptedException, URISyntaxException, ParseException, JOSEException {
        String authorizationCode = authenticateWithMobileId("00000266", "60001019896", 2000, OIDC_DEF_SCOPE);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000266", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }

    @Test
    public void mob1_mobileIdAuthenticationSuccessWithRealLifeDelay() throws InterruptedException, URISyntaxException, ParseException, JOSEException {
        String authorizationCode = authenticateWithMobileId("00000766", "60001019906", 7000, OIDC_DEF_SCOPE);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(getIdToken(authorizationCode));

        assertEquals("EE60001019906", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("MARY ÄNN", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("O’CONNEŽ-ŠUSLIK TESTNUMBER", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("+37200000766", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("mobile_number"));
    }

    @Test
    public void mob2_mobileIdAuthenticationMidNotActivated() {
        String errorMessage = authenticateWithMobileIdError("00000366","60001019928");
        assertEquals("Mobiil-ID teenuses esinevad tehnilised tõrked. Palun proovige mõne aja pärast uuesti", errorMessage);
    }

    @Test
    public void mob2_mobileIdAuthenticationUserCertificatesRevoked() {
        String errorMessage = authenticateWithMobileIdError("00000266","60001019939");
        assertEquals("Autentimine Mobiil-ID-ga ei õnnestunud. Testi oma Mobiil-ID toimimist haldusvahendis: http://www.id.ee/index.php?id=35636", errorMessage);
    }

    @Test
    public void mob2_mobileIdAuthenticationRequestToPhoneFailed() throws URISyntaxException, InterruptedException {
        String errorMessage = authenticateWithMobileIdPollError("07110066","60001019947", 500);
        assertEquals("Teie mobiiltelefoni ei saa Mobiil-ID autentimise sõnumeid saata", errorMessage);
    }

    @Test
    public void mob2_mobileIdAuthenticationTechnicalError() throws URISyntaxException, InterruptedException {
        String errorMessage = authenticateWithMobileIdPollError("00000666","60001019961", 3000);
        assertEquals("Autentimine Mobiil-ID-ga ei õnnestunud. Testi oma Mobiil-ID toimimist haldusvahendis: http://www.id.ee/index.php?id=35636", errorMessage);
    }

    @Test
    public void mob2_mobileIdAuthenticationSimApplicationError() throws URISyntaxException, InterruptedException {
        String errorMessage = authenticateWithMobileIdPollError("01200266","60001019972", 1000);
        assertEquals("Teie mobiiltelefoni SIM kaardiga tekkis tõrge", errorMessage);
    }

    @Test
    public void mob2_mobileIdAuthenticationPhoneNotInNetwork() throws URISyntaxException, InterruptedException {
        String errorMessage = authenticateWithMobileIdPollError("13100266","60001019983", 1000);
        assertEquals("Teie mobiiltelefon on levialast väljas", errorMessage);
    }

    @Test
    public void mob3_mobileIdAuthenticationUserCancels() throws URISyntaxException, InterruptedException {
        String errorMessage = authenticateWithMobileIdPollError("01100266","60001019950", 1000);
        assertEquals("Autentimine on katkestatud", errorMessage);
    }
}
