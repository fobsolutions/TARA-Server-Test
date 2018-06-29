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

@SpringBootTest(classes = BanklinkTest.class)
@Category(IntegrationTest.class)
public class BanklinkTest extends TestsBase {
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @Feature("TPL-8")
    public void bank_example_seb() throws Exception {
        createBank("EYP", "seb_priv");
        setBankDefault("EYP", "VK_OTHER", "ISIK:60001019896;NIMI:Test-Surname,Given-Name1 Givenname2");
        setBankDefault("EYP", "VK_USER_NAME", "Test-Surname,Given-Name1 Givenname2");
        setBankDefault("EYP", "VK_USER_ID", "60001019896");
        setBankDefault("EYP", "VK_COUNTRY", "EE");

        Map bankRequestParams = startBankAuthentication("seb", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = banklinkCallbackPOST(bankResponseParams);
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(token);

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("GIVEN-NAME1", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("TEST-SURNAME", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("2000-01-01", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    @Test
    @Feature("TPL-8")
    public void bank_example_swedbank() throws Exception {
        createBank("HP", "swedbank_priv");
        setBankDefault("HP", "VK_OTHER", "");
        setBankDefault("HP", "VK_USER_NAME", "Test-Surname,Given-Name1 Givenname2");
        setBankDefault("HP", "VK_USER_ID", "60001019896");
        setBankDefault("HP", "VK_COUNTRY", "EE");

        Map bankRequestParams = startBankAuthentication("swedbank", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = banklinkCallbackPOST(bankResponseParams);
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(token);

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("GIVEN-NAME1", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("TEST-SURNAME", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("2000-01-01", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    @Test
    @Feature("TPL-8")
    public void bank_example_coop() throws Exception {
        createBank("KREP", "coop_priv");
        setBankDefault("KREP", "VK_OTHER", "");
        setBankDefault("KREP", "VK_USER_NAME", "Test-Surname,Given-Name1 Givenname2");
        setBankDefault("KREP", "VK_USER_ID", "60001019896");
        setBankDefault("KREP", "VK_COUNTRY", "EE");

        Map bankRequestParams = startBankAuthentication("coop", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = banklinkCallbackGET(bankResponseParams);

        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(token);

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("GIVEN-NAME1", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("TEST-SURNAME", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("2000-01-01", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    @Test
    @Feature("TPL-8")
    public void bank_example_lhv() throws Exception {
        createBank("LHV", "lhv_priv");

        setBankDefault("LHV", "VK_OTHER", "");
        setBankDefault("LHV", "VK_USER_NAME", "Given-Name1 Given-Name2 Test-Surname");
        setBankDefault("LHV", "VK_USER_ID", "60001019896");
        setBankDefault("LHV", "VK_COUNTRY", "EE");

        Map bankRequestParams = startBankAuthentication("lhv", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = banklinkCallbackPOST(bankResponseParams);
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(token);

        assertEquals("EE60001019896", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("GIVEN-NAME1 GIVEN-NAME2", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("TEST-SURNAME", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals("2000-01-01", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    @Test
    @Feature("TPL-2")
    public void bank1_bankRequestParams() throws Exception {
        createBank("DANSKE", "danske_priv");

        Map<String, String> bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "et");
        Map<String, String> expectedBankRequestParams = new HashMap<>();
        expectedBankRequestParams.put("VK_SERVICE", "4012");
        expectedBankRequestParams.put("VK_VERSION", "008");
        expectedBankRequestParams.put("VK_SND_ID", "TARA_DANSKE");
        expectedBankRequestParams.put("VK_REC_ID", "DANSKE");
        expectedBankRequestParams.put("VK_RETURN", "https://koogelmoogel.net/login");
        expectedBankRequestParams.put("VK_RID", "");
        expectedBankRequestParams.put("VK_ENCODING", "UTF-8");
        expectedBankRequestParams.put("VK_LANG", "EST");

        assertThat(new HashSet<Object>(bankRequestParams.entrySet()), hasItems(expectedBankRequestParams.entrySet().toArray()));
        assertThat("VK_NONCE", bankRequestParams.get("VK_NONCE"), not(isEmptyString()));
        assertThat("VK_DATETIME", bankRequestParams.get("VK_DATETIME"), not(isEmptyString()));
        assertThat("VK_MAC", bankRequestParams.get("VK_MAC"), not(isEmptyString()));
    }

    @Test
    @Feature("TPL-2")
    public void bank2_bankRequestParams_langEN() throws Exception {
        createBank("DANSKE", "danske_priv");

        Map bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "en");

        Map<String, String> expectedBankRequestParams = new HashMap<String, String>();
        expectedBankRequestParams.put("VK_LANG", "ENG");
        assertThat(new HashSet<Object>(bankRequestParams.entrySet()), hasItems(expectedBankRequestParams.entrySet().toArray()));
    }

    @Test
    @Feature("TPL-2")
    public void bank2_bankRequestParams_langRU() throws Exception {
        createBank("DANSKE", "danske_priv");

        Map bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "ru");

        Map<String, String> expectedBankRequestParams = new HashMap<String, String>();
        expectedBankRequestParams.put("VK_LANG", "RUS");
        assertThat(new HashSet<Object>(bankRequestParams.entrySet()), hasItems(expectedBankRequestParams.entrySet().toArray()));
    }

    @Test
    @Feature("TPL-6")
    public void bank_VK_NONCE_reuseForbidden() throws Exception {
        createBank("DANSKE", "danske_priv");

        Map bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = given().filter(cookieFilter).relaxedHTTPSValidation().log().all().formParams(bankResponseParams).post("https://koogelmoogel.net/login").then().log().all().extract().response()
                .getHeader("location");
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        verifyTokenAndReturnSignedJwtObject(token);

        String responseBody = given().filter(cookieFilter).relaxedHTTPSValidation().log().all().formParams(bankResponseParams).post("https://koogelmoogel.net/login").then().statusCode(500).log().all().extract().response().body().asString();
        assertThat(responseBody, StringContains.containsString("Kasutaja tuvastamine ebaõnnestus"));
    }

    @Test
    @Feature("TPL-6")
    public void bank_single_VK_NONCE_in_session() throws Exception {
        createBank("DANSKE", "danske_priv");

        Map bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "et");
        Map bankRequestParams2 = startBankAuthentication("danske", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");
        String location = given().filter(cookieFilter).relaxedHTTPSValidation().log().all().formParams(bankResponseParams).post("https://koogelmoogel.net/login").then().log().all().extract().response()
                .getHeader("location");
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        verifyTokenAndReturnSignedJwtObject(token);

        String responseBody = given().filter(cookieFilter).relaxedHTTPSValidation().log().all().formParams(bankResponseParams).post("https://koogelmoogel.net/login").then().statusCode(500).log().all().extract().response().body().asString();
        assertThat(responseBody, StringContains.containsString("Kasutaja tuvastamine ebaõnnestus"));
    }

    @Test
    @Feature("TPL-8")
    public void bank_latvian_example() throws Exception {
        createBank("DANSKE", "danske_priv");

        setBankDefault("DANSKE", "VK_USER_NAME", "Test-Surname,Given-Name1 Givenname2");
        setBankDefault("DANSKE", "VK_USER_ID", "320000-00000");
        setBankDefault("DANSKE", "VK_COUNTRY", "LV");
        Map bankRequestParams = startBankAuthentication("danske", OIDC_DEF_SCOPE, "et");
        Map bankResponseParams = getBankResponse(bankRequestParams, "");

        String location = banklinkCallbackPOST(bankResponseParams);
        String authorizationCode = getAuthorizationCode(location);
        String token = getIdToken(authorizationCode);
        SignedJWT signedJWT = verifyTokenAndReturnSignedJwtObject(token);

        assertEquals("LV320000-00000", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("GIVEN-NAME1", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("given_name"));
        assertEquals("TEST-SURNAME", signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("family_name"));
        assertEquals(null, signedJWT.getJWTClaimsSet().getJSONObjectClaim("profile_attributes").getAsString("date_of_birth"));
    }

    void createBank(String id, String privatekeyAlias) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:bank.p12");
        KeyStore keystore = KeyStore.getInstance("PKCS12" == null ? KeyStore.getDefaultType() : "PKCS12");

        keystore.load(resource.getInputStream(), "s3cr3t".toCharArray());
        PrivateKey key = (PrivateKey) keystore.getKey(privatekeyAlias, "s3cr3t".toCharArray());
        String privateKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.print(privateKey);
        given().log().all().when().put(testTaraProperties.getBanklinkMockUrl() + "/banks?id=" + id + "&key=" + privateKey).then().log().all();
    }

    void setBankDefault(String bank, String parameter, String value) {
        given().log().all().when().put(testTaraProperties.getBanklinkMockUrl() + "/banks/" + bank + "/defaults?" + parameter + "=" + value).then().log().all();
    }
}
