package ee.ria.tara.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "test.tara")
public class TestTaraProperties {

    private String targetUrl;
    private String targetSpUrl;
    private String spMetadataUrl;
    private String spStartUrl;
    private String spReturnUrl;
    private String spProviderName;
    private Integer acceptableTimeDiffMin;
    private String keystore;
    private String keystorePass;
    private String responseSigningKeyId;
    private String responseSigningKeyPass;
    private String idpUrl;
    private String idpMetadataUrl;
    private String idpStartUrl;

    private String jwksUrl;
    private String testRedirectUri;
    private String clientId;
    private String clientSecret;
    private String authorizeUrl;
    private String tokenUrl;
    private String loginUrl;
    private String serviceUrl;
    private String configurationUrl;
    private String casClientId;

    public String getConfigurationUrl() {
        return configurationUrl;
    }

    public void setConfigurationUrl(String configurationUrl) {
        this.configurationUrl = configurationUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCasClientId() {
        return casClientId;
    }

    public void setCasClientId(String casClientId) {
        this.casClientId = casClientId;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTestRedirectUri() {
        return testRedirectUri;
    }

    public void setTestRedirectUri(String testRedirectUri) {
        this.testRedirectUri = testRedirectUri;
    }

    public String getJwksUrl() {
        return jwksUrl;
    }

    public void setJwksUrl(String jwksUrl) {
        this.jwksUrl = jwksUrl;
    }

    public String getFullJwksUrl() {
        return targetUrl+jwksUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setTargetSpUrl(String targetSpUrl) {
        this.targetSpUrl = targetSpUrl;
    }

    public void setSpMetadataUrl(String spMetadataUrl) {
        this.spMetadataUrl = spMetadataUrl;
    }

    public void setSpStartUrl(String spStartUrl) {
        this.spStartUrl = spStartUrl;
    }

    public void setSpReturnUrl(String spReturnUrl) {
        this.spReturnUrl = spReturnUrl;
    }

    public void setSpProviderName(String spProviderName) {
        this.spProviderName = spProviderName;
    }

    public void setAcceptableTimeDiffMin(Integer acceptableTimeDiffMin) {
        this.acceptableTimeDiffMin = acceptableTimeDiffMin;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public void setResponseSigningKeyId(String responseSigningKeyId) {
        this.responseSigningKeyId = responseSigningKeyId;
    }

    public void setResponseSigningKeyPass(String responseSigningKeyPass) {
        this.responseSigningKeyPass = responseSigningKeyPass;
    }

    public void setIdpUrl(String idpUrl) {
        this.idpUrl = idpUrl;
    }

    public void setIdpMetadataUrl(String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    public void setIdpStartUrl(String idpStartUrl) {
        this.idpStartUrl = idpStartUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getTargetSpUrl() {
        return targetSpUrl;
    }

    public String getSpMetadataUrl() {
        return spMetadataUrl;
    }

    public String getFullSpMetadataUrl() {
        return spMetadataUrl;
    }

    public String getSpStartUrl() {
        return spStartUrl;
    }

    public String getSpReturnUrl() {
        return spReturnUrl;
    }

    public String getSpProviderName() {
        return spProviderName;
    }

    public Integer getAcceptableTimeDiffMin() {
        return acceptableTimeDiffMin;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public String getResponseSigningKeyId() {
        return responseSigningKeyId;
    }

    public String getResponseSigningKeyPass() {
        return responseSigningKeyPass;
    }

    public String getIdpUrl() {
        return idpUrl;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public String getIdpStartUrl() {
        return idpStartUrl;
    }

    public String getFullIdpMetadataUrl() {
        return idpUrl + idpMetadataUrl;
    }

    public String getFullSpReturnUrl() {
        return targetSpUrl + spReturnUrl;
    }
}
