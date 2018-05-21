package ee.ria.tara;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import ee.ria.tara.config.IntegrationTest;
import ee.ria.tara.config.TestConfiguration;
import ee.ria.tara.utils.OpenSAMLUtils;
import ee.ria.tara.utils.ResponseBuilderUtils;
import ee.ria.tara.utils.SystemPropertyActiveProfileResolver;
import ee.ria.tara.config.TestTaraProperties;
import ee.ria.tara.utils.XmlUtils;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.X509Support;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static ee.ria.tara.config.TaraTestStrings.*;
import static io.restassured.RestAssured.*;
import static io.restassured.config.EncoderConfig.encoderConfig;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles(profiles = {"dev"}, resolver = SystemPropertyActiveProfileResolver.class)
public abstract class TestsBase {

    @Autowired
    protected TestTaraProperties testTaraProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    protected JWKSet jwkSet;
    protected String tokenIssuer;
    protected CookieFilter cookieFilter;
    protected String state;
    protected String nonce;

    protected Credential signatureCredential;
    protected  Credential encryptionCredential;

    @Before
    public void setUp() throws IOException, InitializationException, ParseException {
        URL url = new URL(testTaraProperties.getTargetUrl());
        port = url.getPort();
        baseURI = url.getProtocol() + "://" + url.getHost();

        jwkSet = JWKSet.load(new URL(testTaraProperties.getFullJwksUrl()));

        tokenIssuer = getIssuer(testTaraProperties.getTargetUrl()+testTaraProperties.getConfigurationUrl());

        Security.addProvider(new BouncyCastleProvider());
        InitializationService.initialize();

        cookieFilter = new CookieFilter();

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            Resource resource = resourceLoader.getResource(testTaraProperties.getKeystore());
            keystore.load(resource.getInputStream(), testTaraProperties.getKeystorePass().toCharArray());
            signatureCredential = getCredential(keystore, testTaraProperties.getResponseSigningKeyId(), testTaraProperties.getResponseSigningKeyPass() );
            encryptionCredential = getEncryptionCredentialFromMetaData(getMetadataBody());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong initializing credentials:", e);
        }
    }

    private Credential getCredential(KeyStore keystore, String keyPairId, String privateKeyPass) {
        try {
            Map<String, String> passwordMap = new HashMap<>();
            passwordMap.put(keyPairId, privateKeyPass);
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            Criterion criterion = new EntityIdCriterion(keyPairId);
            CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(criterion);

            return resolver.resolveSingle(criteriaSet);
        } catch (ResolverException e) {
            throw new RuntimeException("Something went wrong reading credentials", e);
        }
    }

    protected String getMetadataBody() {
        return given()
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testTaraProperties.getFullEidasNodeMetadataUrl())
                .then()
                .log().ifError()
                .statusCode(200)
                .extract()
                .body().asString();
    }


    private String getIssuer(String url) {
        return given()
//                .log().all()
                .when()
                .get(url)
                .then()
//                .log().all()
                .extract().response().getBody().jsonPath().getString("issuer");
    }

    protected String authenticateWithMobileId(String mobileNo, String idCode, Integer pollMillis) throws InterruptedException, URISyntaxException {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        String execution2 = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "submit")
                .formParam("mobileNumber", mobileNo)
                .formParam("moblang", "et")
                .formParam("principalCode", idCode)
//                .queryParam("service", testTaraProperties.getServiceUrl())
//                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response()
                .htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");

        String location = pollForAuthentication(execution2, pollMillis);

        return getAuthorizationCode(location);
    }

    protected String authenticateWithEidas(String personCountry) throws URISyntaxException {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        Response response = getEidasSamlRequest(personCountry, execution);
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");

        //Here we need to simulate a response from foreign country eIDAS Node
        String samlResponse = getBase64SamlResponseMinimalAttributes(response.getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_LOW);

        return getAuthorizationCode(returnEidasResponse(samlResponse, relayState));
    }

    protected Response getEidasSamlRequest(String personCountry, String execution) {
        Response response = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "eidassubmit")
                .formParam("country", personCountry)
                .formParam("eidaslang", "")
                .formParam("geolocation", "") //TODO: What for is this?
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response();

        String samlRequest = response.htmlPath().getString("**.findAll { it.@name == 'SAMLRequest' }[0].@value");
        String relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");
        String country = response.htmlPath().getString("**.findAll { it.@name == 'country' }[0].@value");
        String url = response.htmlPath().getString("**.findAll { it.@method == 'post' }[0].@action");

        return given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("country", country)
                .formParam("RelayState", relayState)
                .formParam("SAMLRequest", samlRequest)
//                .log().all()
                .when()
                .post(url)
                .then()
//                .log().all()
                .extract().response();
    }

    protected String returnEidasResponse(String samlResponse, String relayState) {
        Response response = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("RelayState", relayState)
                .formParam("SAMLResponse", samlResponse)
                //          .log().all()
                .when()
                .post(testTaraProperties.getEidasNodeUrl() + testTaraProperties.getEidasNodeResponseUrl())
                .then()
                //         .log().all()
                .extract().response();

        samlResponse = response.htmlPath().getString("**.findAll { it.@name == 'SAMLResponse' }[0].@value");
        relayState = response.htmlPath().getString("**.findAll { it.@name == 'RelayState' }[0].@value");
        String url = response.htmlPath().getString("**.findAll { it.@method == 'post' }[0].@action");

        return given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("RelayState", relayState)
                .formParam("SAMLResponse", samlResponse)
                //         .log().all()
                .when()
                .post(url)
                .then()
                //        .log().all()
                .extract().header("location");
    }

    protected String getAuthenticationMethodsPageAndGetExecution() {
        state = RandomStringUtils.random(16);
        String sha256StateBase64 = Base64.getEncoder().encodeToString(DigestUtils.sha256(state));
        nonce = RandomStringUtils.random(16);
        String sha256NonceBase64 = Base64.getEncoder().encodeToString(DigestUtils.sha256(nonce));
        String location = given()
                .filter(cookieFilter)
                .relaxedHTTPSValidation()
                .queryParam("scope", "openid")
                .queryParam("response_type", "code")
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
                .queryParam("state", sha256StateBase64)
                .queryParam("nonce", sha256NonceBase64)
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
        Response response = postToTokenEndpoint(authorizationCode);
        return response.getBody().jsonPath().getString("id_token");
    }

    protected Response postToTokenEndpoint(String authorizationCode) {
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
                .extract().response();
    }

    protected String getAuthorization(String id ,String secret) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", id, secret).getBytes(StandardCharsets.UTF_8)));
    }

    private String getCode (String url) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");

        Map<String, String> queryParams = params.stream().collect(
                Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        if (queryParams.get("state").equals(Base64.getEncoder().encodeToString(DigestUtils.sha256(state)))) {
            return queryParams.get("code");
        }
        else {
            throw new RuntimeException("State value do not match!");
        }
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
            if (signedJWT.getJWTClaimsSet().getAudience().get(0).equals(testTaraProperties.getClientId())) {
                if (signedJWT.getJWTClaimsSet().getIssuer().equals(tokenIssuer)) {
                    Date date = new Date();
                    if (date.after(signedJWT.getJWTClaimsSet().getNotBeforeTime()) && date.before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
                        if (signedJWT.getJWTClaimsSet().getClaim("nonce").equals(Base64.getEncoder().encodeToString(DigestUtils.sha256(nonce)))) {
                            return signedJWT;
                        }
                        else {
                            throw new RuntimeException("Calculated nonce do not match the received one!");
                        }
                    }
                    else {
                        throw new RuntimeException("Token validity period is not valid! current: " + date + " nbf: "+signedJWT.getJWTClaimsSet().getNotBeforeTime()+" exp: "+signedJWT.getJWTClaimsSet().getExpirationTime());
                    }
                }
                else {
                    throw new RuntimeException("Token Issuer is not valid! Expected: "+tokenIssuer+" actual: "+signedJWT.getJWTClaimsSet().getIssuer());
                }
            }
            else {
                throw new RuntimeException("Token Audience is not valid! Expected: "+testTaraProperties.getClientId()+" actual: "+signedJWT.getJWTClaimsSet().getAudience().get(0));
            }
        }
        else {
            throw new RuntimeException("Token Signature is not valid!");
        }
    }

    protected String authenticateWithMobileIdError(String mobileNo, String idCode) {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        String error =  given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "submit")
                .formParam("mobileNumber", mobileNo)
                .formParam("moblang", "et")
                .formParam("principalCode", idCode)
//                .queryParam("service", testTaraProperties.getServiceUrl())
//                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response()
                .htmlPath().getString("**.findAll { it.@class=='error-box' }");
        error = error.substring(4);
        return error;
    }

    protected String authenticateWithMobileIdPollError(String mobileNo, String idCode, Integer pollMillis) throws InterruptedException, URISyntaxException {
        String execution = getAuthenticationMethodsPageAndGetExecution();
        String execution2 = given()
                .filter(cookieFilter).relaxedHTTPSValidation()
                .formParam("execution", execution)
                .formParam("_eventId", "submit")
                .formParam("mobileNumber", mobileNo)
                .formParam("moblang", "et")
                .formParam("principalCode", idCode)
//                .queryParam("service", testTaraProperties.getServiceUrl())
//                .queryParam("client_name", testTaraProperties.getCasClientId())
                .queryParam("client_id", testTaraProperties.getClientId())
                .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                .when()
                .post(testTaraProperties.getLoginUrl())
                .then()
//                .log().all()
                .extract().response()
                .htmlPath().getString("**.findAll { it.@name == 'execution' }[0].@value");

        String error = pollForError(execution2, pollMillis).htmlPath().getString("**.findAll { it.@class=='error-box' }");
        error = error.substring(4);
        return error;
    }

    protected String pollForAuthentication(String execution, Integer intervalMillis) throws InterruptedException {
        DateTime endTime = new DateTime().plusMillis(intervalMillis*3 + 200);
        while(new DateTime().isBefore(endTime)) {
            Thread.sleep(intervalMillis);
            Response response = given()
                    .filter(cookieFilter)
                    .relaxedHTTPSValidation()
                    .redirects().follow(false)
                    .formParam("execution", execution)
                    .formParam("_eventId", "check")
//                    .queryParam("service", testTaraProperties.getServiceUrl())
//                    .queryParam("client_name", testTaraProperties.getCasClientId())
                    .queryParam("client_id", testTaraProperties.getClientId())
                    .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                    .when()
                    .post(testTaraProperties.getLoginUrl())
                    .then()
//                .log().all()
                    .extract().response();
            if (response.statusCode() == 302) {
                return response.getHeader("location");
            }
        }
        throw new RuntimeException("No MID response in: "+ (intervalMillis*3 + 200) +" millis");
    }

    protected Response pollForError(String execution, Integer intervalMillis) throws InterruptedException {
        DateTime endTime = new DateTime().plusMillis(intervalMillis*3 + 200);
        while(new DateTime().isBefore(endTime)) {
            Thread.sleep(intervalMillis);
            Response response = given()
                    .filter(cookieFilter)
                    .relaxedHTTPSValidation()
                    .redirects().follow(false)
                    .formParam("execution", execution)
                    .formParam("_eventId", "check")
//                    .queryParam("service", testTaraProperties.getServiceUrl())
//                    .queryParam("client_name", testTaraProperties.getCasClientId())
                    .queryParam("client_id", testTaraProperties.getClientId())
                    .queryParam("redirect_uri", testTaraProperties.getTestRedirectUri())
//                .log().all()
                    .when()
                    .post(testTaraProperties.getLoginUrl())
                    .then()
//                .log().all()
                    .extract().response();
            if ((response.statusCode() != 302) & (response.statusCode() != 200)) {
                return response;
            }
        }
        throw new RuntimeException("No MID error response in: "+ (intervalMillis*3 + 200) +" millis");
    }

    protected String getBase64SamlResponseMinimalAttributes(String requestBody, String givenName, String familyName, String personIdentifier, String dateOfBirth, String loa) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        if(loa == null) {
            loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        }
        org.opensaml.saml.saml2.core.Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                "http://eidas-tomcat-1a.arendus.kit:8080/EidasNode/ColleagueResponse", loa, givenName, familyName, personIdentifier, dateOfBirth, "http://eidas-tomcat-1a.arendus.kit:8080/EidasNode/ServiceMetadata", 5, "http://eidas-tomcat-1a.arendus.kit:8080/EidasNode/ConnectorMetadata");
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected XmlPath getDecodedSamlRequestBodyXml(String body) {
        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, body);
        String SAMLRequestString = html.getString("**.findAll { it.@name == 'SAMLRequest' }[0].@value");
        String decodedRequest = new String(Base64.getDecoder().decode(SAMLRequestString), StandardCharsets.UTF_8);
        XmlPath decodedSAMLrequest = new XmlPath(decodedRequest);
        return decodedSAMLrequest;
    }

    protected Credential getEncryptionCredentialFromMetaData (String body) throws CertificateException {
        java.security.cert.X509Certificate x509Certificate = getEncryptionCertificate(body);
        BasicX509Credential encryptionCredential = new BasicX509Credential(x509Certificate);
        return encryptionCredential;
    }

    protected java.security.cert.X509Certificate getEncryptionCertificate(String body) throws CertificateException {
        XmlPath metadataXml = new XmlPath(body);
        java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("**.findAll {it.@use == 'encryption'}.KeyInfo.X509Data.X509Certificate"));
        return x509;
    }

    protected Boolean validateSamlResponseSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("Response.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body,x509);
            return true;
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected void validateSignature(String body, java.security.cert.X509Certificate x509) {
        try {
            x509.checkValidity();
            SignableSAMLObject signableObj = XmlUtils.unmarshallElement(body);
            X509Credential credential = CredentialSupport.getSimpleCredential(x509,null);
            SignatureValidator.validate(signableObj.getSignature(), credential);
        } catch (SignatureException e) {
            throw new RuntimeException("Signature validation in validateSignature() failed: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid: " + e.getMessage(), e);
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate is expired: " + e.getMessage(), e);
        }
    }

}
