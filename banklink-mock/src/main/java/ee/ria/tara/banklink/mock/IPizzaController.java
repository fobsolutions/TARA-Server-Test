package ee.ria.tara.banklink.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class IPizzaController {

    private static final String IPIZZA_DEFAULTS = "ipizza.defaults.";

    private final Object defaultParameterLock = new Object();
    private final Map<String, String> defaultParameters = new HashMap<>();
    private final Map<String, Bank> banks = new HashMap<>();

    public IPizzaController(Environment environment) {
        String defaultsParams = environment.getProperty(IPIZZA_DEFAULTS + "parameters");
        if (StringUtils.isEmpty(defaultsParams)) return;

        for (String param : defaultsParams.split(",")) {
            String value = environment.getProperty(IPIZZA_DEFAULTS + param);
            if (value != null) defaultParameters.put(param, value);
        }
    }

    @RequestMapping(value = "/ipizza/auth", method = {GET, POST})
    public String authenticate(
            @RequestParam(name = IPizzaUtils.VK_SERVICE, required = true) String vkService,
            @RequestParam(name = IPizzaUtils.VK_VERSION, required = true) String vkVersion,
            @RequestParam(name = IPizzaUtils.VK_SND_ID, required = true) String vkSndId,
            @RequestParam(name = IPizzaUtils.VK_REC_ID, required = true) String vkRecId,
            @RequestParam(name = IPizzaUtils.VK_NONCE, required = true) String vkNonce,
            @RequestParam(name = IPizzaUtils.VK_RETURN, required = true) String vkReturn,
            @RequestParam(name = IPizzaUtils.VK_DATETIME, required = true) String vkDateTime,
            @RequestParam(name = IPizzaUtils.VK_RID, required = true) String vkRid,
            @RequestParam(name = IPizzaUtils.VK_MAC, required = true) String vkMac,
            @RequestParam(name = IPizzaUtils.VK_ENCODING, required = true) String vkEncoding,
            @RequestParam(name = IPizzaUtils.VK_LANG, required = true) String vkLang
    ) throws GeneralSecurityException {
        Map<String, String> returnParameters = new LinkedHashMap<>();
        Bank bank = (vkRecId != null) ? banks.get(vkRecId) : null;

        synchronized (defaultParameterLock) {
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_SERVICE, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_VERSION, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_DATETIME, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()), bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_SND_ID, vkRecId, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_REC_ID, vkSndId, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_NONCE, vkNonce, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_USER_NAME, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_USER_ID, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_COUNTRY, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_OTHER, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_TOKEN, null, bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_RID, vkRid, bank);

            Charset encoding = getPreferredEncoding(vkEncoding, bank);
            String mac = calculateMacIfPrivateKeyExists(vkRecId, returnParameters, encoding);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_MAC, mac, bank);

            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_ENCODING, encoding.name(), bank);
            putParameterIfNotNull(returnParameters, IPizzaUtils.VK_LANG, vkLang, bank);
        }

        return IPizzaUtils.buildReturnHtml(returnParameters, vkReturn,
                (bank != null) ? bank.getCallbackMethod() : "POST");
    }

    private void putParameterIfNotNull(Map<String, String> dst, String key, String value, Bank bank) {
        value = getParameterOrOverridden(key, value, bank);
        if (value != null) dst.put(key, value);
    }

    private String getParameterOrOverridden(String key, String value, Bank bank) {
        value = defaultParameters.getOrDefault(key, value);
        if (bank != null)
            return bank.getInternalDefaultParameters().getOrDefault(key, value);
        else
            return value;
    }

    private Charset getPreferredEncoding(String encoding, Bank bank) {
        encoding = getParameterOrOverridden(IPizzaUtils.VK_ENCODING, encoding, bank);
        return Charset.forName(encoding);
    }

    /* Configuration endpoints for global defaults */

    @RequestMapping(value = "/ipizza/defaults", method = PUT)
    public Map<String, String> putGlobalDefaults(HttpServletRequest request) {
        Map<String, String[]> requestParameters = request.getParameterMap();

        synchronized (defaultParameterLock) {
            for (String key : requestParameters.keySet()) {
                String[] values = requestParameters.get(key);
                String value = (values.length > 0) ? values[0] : null;
                defaultParameters.put(key, value);
            }

            return IPizzaUtils.asImmutableMap(defaultParameters);
        }
    }

    @RequestMapping(value = "/ipizza/defaults", method = GET)
    public Map<String, String> getGlobalDefaults() {
        synchronized (defaultParameterLock) {
            return IPizzaUtils.asImmutableMap(defaultParameters);
        }
    }

    @RequestMapping(value = "/ipizza/defaults", method = DELETE)
    public Map<String, String> deleteGlobalDefaults(HttpServletRequest request) {
        Map<String, String[]> requestParameters = request.getParameterMap();

        synchronized (defaultParameterLock) {
            for (String key : requestParameters.keySet()) {
                defaultParameters.remove(key);
            }

            return IPizzaUtils.asImmutableMap(defaultParameters);
        }
    }

    /* Configuration endpoints for individual banks */

    @RequestMapping(value = "/ipizza/banks", method = PUT)
    public List<Bank> insertNewBank(
            @RequestParam(name = "id", required = true) String bankIdentifier,
            @RequestParam(name = "key", required = true) String bankPrivateKey
    ) throws GeneralSecurityException {
        PrivateKey privateKey = IPizzaUtils.createPrivateKey(bankPrivateKey);
        Bank newBank = new Bank(bankIdentifier, privateKey);

        synchronized (defaultParameterLock) {
            banks.put(bankIdentifier, newBank);
            return IPizzaUtils.asImmutableList(banks.values());
        }
    }

    @RequestMapping(value = "/ipizza/banks", method = GET)
    public List<Bank> getAllBanks() {
        synchronized (defaultParameterLock) {
            return IPizzaUtils.asImmutableList(banks.values());
        }
    }

    @RequestMapping(value = "/ipizza/banks/{bankId}/defaults", method = PUT)
    public Map<String, String> putBankDefaults(@PathVariable String bankId, HttpServletRequest request) {
        Map<String, String[]> requestParameters = request.getParameterMap();

        synchronized (defaultParameterLock) {
            Map<String, String> bankDefaultParameters = getBankDefaultParameters(bankId);
            if (bankDefaultParameters == null) return null;

            for (String key : requestParameters.keySet()) {
                String[] values = requestParameters.get(key);
                String value = (values.length > 0) ? values[0] : null;
                bankDefaultParameters.put(key, value);
            }

            return IPizzaUtils.asImmutableMap(bankDefaultParameters);
        }
    }

    @RequestMapping(value = "/ipizza/banks/{bankId}/defaults", method = GET)
    public Map<String, String> getBankDefaults(@PathVariable String bankId) {
        synchronized (defaultParameterLock) {
            Map<String, String> bankDefaultParameters = getBankDefaultParameters(bankId);
            return (bankDefaultParameters == null) ? null : IPizzaUtils.asImmutableMap(bankDefaultParameters);
        }
    }

    @RequestMapping(value = "/ipizza/banks/{bankId}/defaults", method = DELETE)
    public Map<String, String> deleteBankDefaults(@PathVariable String bankId, HttpServletRequest request) {
        Map<String, String[]> requestParameters = request.getParameterMap();

        synchronized (defaultParameterLock) {
            Map<String, String> bankDefaultParameters = getBankDefaultParameters(bankId);
            if (bankDefaultParameters == null) return null;

            for (String key : requestParameters.keySet()) {
                defaultParameters.remove(key);
            }

            return IPizzaUtils.asImmutableMap(bankDefaultParameters);
        }
    }

    @RequestMapping(value = "/ipizza/banks/{bankId}/method")
    public Bank setBankCallbackMethod(@PathVariable String bankId, HttpServletRequest request) {
        synchronized (defaultParameterLock) {
            Bank bank = banks.get(bankId);
            if (bank == null) return null;

            bank.setCallbackMethod(request.getMethod());
            return bank;
        }
    }

    private final class Bank {

        private final String identifier;
        private final PrivateKey privateKey;
        private final Map<String, String> defaultParameters = new HashMap<>();
        private String callbackMethod;

        Bank(String id, PrivateKey privateKey) {
            this.identifier = id;
            this.privateKey = privateKey;
            this.callbackMethod = "POST";
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public Map<String, String> getDefaultParameters() {
            return IPizzaUtils.asImmutableMap(defaultParameters);
        }

        @JsonIgnore
        public Map<String, String> getInternalDefaultParameters() {
            return this.defaultParameters;
        }

        @JsonIgnore
        public PrivateKey getPrivateKey() {
            return this.privateKey;
        }

        public void setCallbackMethod(String callbackMethod) {
            this.callbackMethod = callbackMethod;
        }

        public String getCallbackMethod() {
            return this.callbackMethod;
        }
    }

    private String calculateMacIfPrivateKeyExists(String bankIdentifier, Map<String, String> parameters, Charset encoding) throws GeneralSecurityException {
        Bank bank = banks.get(bankIdentifier);
        if (bank != null)
            return IPizzaUtils.calculateMac(parameters, encoding, bank.getPrivateKey());
        else
            return null;
    }

    private Map<String, String> getBankDefaultParameters(String bankId) {
        Bank bank = banks.get(bankId);
        if (bank != null)
            return bank.getInternalDefaultParameters();
        else
            return null;
    }

}
