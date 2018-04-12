package ee.ria.tara;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import ee.ria.tara.config.TestConfiguration;
import ee.ria.tara.utils.SystemPropertyActiveProfileResolver;
import ee.ria.tara.config.TestTaraProperties;
import io.restassured.filter.cookie.CookieFilter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles(profiles = {"dev"}, resolver = SystemPropertyActiveProfileResolver.class)
public abstract class TestsBase {

    @Autowired
    protected TestTaraProperties testTaraProperties;

    protected JWKSet jwkSet;
    protected CookieFilter cookieFilter;

    @Before
    public void setUp() throws IOException, InitializationException, ParseException {
        URL url = new URL(testTaraProperties.getTargetUrl());
        port = url.getPort();
        baseURI = url.getProtocol() + "://" + url.getHost();

        jwkSet = JWKSet.load(new URL(testTaraProperties.getJwksUrl()));

        Security.addProvider(new BouncyCastleProvider());
        InitializationService.initialize();
        cookieFilter = new CookieFilter();
    }

    protected String authenticateWithMobileId(String mobileNo, String idCode) throws InterruptedException, URISyntaxException {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        String execution2 = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "submit")
                .formParam("mobileNumber", mobileNo)
                .formParam("moblang", "et")
                .formParam("principalCode", idCode)
                .queryParam("service", testTaraProperties.getServiceUrl())
                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response()
                .htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");

        Thread.sleep(7500);

        String location = given()
                .filter(cookieFilter)
                .relaxedHTTPSValidation()
                .redirects().follow(false)
                .formParam("execution", execution2)
                .formParam("_eventId", "check")
                .queryParam("service", testTaraProperties.getServiceUrl())
                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response()
                .getHeader("location");

        return getAuthorizationCode(location);
    }

    protected String getAuthenticationMethodsPageAndGetExecution() {
        String location = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .queryParam("scope", "openid")
                .queryParam("response_type", "code")
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
                .queryParam("state", "abcdefghijklmnop")
                .queryParam("nonce", "qrstuvwxyzabcdef")
                .queryParam("lang", "et")
                .when()
                .redirects().follow(false)
//                .log().all()
                .get(testTaraProperties.getAuthorizeUrl())
                .then()
//                .log().all()
                .extract().response()
                .getHeader("location");

        return  given()
                .filter(cookieFilter)
                .relaxedHTTPSValidation()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(false)
//                .log().all()
                .get(location)
                .then()
//                .log().all()
                .extract().response()
                .getBody().htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");
    }

    protected String getAuthorizationCode(String url) throws URISyntaxException {
        String location = given()
                .filter(cookieFilter)
                .relaxedHTTPSValidation()
                .redirects().follow(false)
//                .log().all()
                .when()
                .urlEncodingEnabled(false)
                .get(url)
                .then()
//                .log().all()
                .extract().response()
                .getHeader("location");

        String location2 = given()
                .filter(cookieFilter)
                .relaxedHTTPSValidation()
//                .log().all()
                .when()
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .get(location)
                .then()
//                .log().all()
                .extract().response()
                .getHeader("Location");

        return getCode(location2);
    }

    protected String getIdToken(String authorizationCode) {
        return  given()
                .relaxedHTTPSValidation()
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", authorizationCode)
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .header("Authorization", getAuthorization(testTaraProperties.getClientId(), testTaraProperties.getClientSecret()))
                .urlEncodingEnabled(true)
                .post(testTaraProperties.getTokenUrl())
                .then()
//                .log().all()
                .extract().response().getBody().jsonPath().getString("id_token");
    }

    protected String getAuthorization(String id ,String secret) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", id, secret).getBytes(StandardCharsets.UTF_8)));
    }

    private String getCode (String url) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");

        Map<String, String> queryParams = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        return queryParams.get("code");
    }

    protected Boolean isTokenSignatureValid(SignedJWT signedJWT) throws JOSEException {
        List<JWK> matches = new JWKSelector(new JWKMatcher.Builder()
                .keyType(KeyType.RSA)
                .build())
                .select(jwkSet);

        RSAKey rsaKey = (RSAKey) matches.get(0);

        JWSVerifier verifier = new RSASSAVerifier(rsaKey);
        return signedJWT.verify(verifier);
    }

    protected SignedJWT verifyTokenAndReturnSignedJwtObject(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT =  SignedJWT.parse(token);
        if (isTokenSignatureValid(signedJWT)) {
            return signedJWT;
        }
        else {
            throw new RuntimeException("Token Signature is not valid!");
        }
    }

    protected String authenticateWithMobileIdError(String mobileNo, String idCode) {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        return  given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "submit")
                .formParam("mobileNumber", mobileNo)
                .formParam("moblang", "et")
                .formParam("principalCode", idCode)
                .queryParam("service", testTaraProperties.getServiceUrl())
                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
                .log().all()
                .extract().response()
                .htmlPath().getString("**.findAll { it.@class=='error-box' }");
    }
}
