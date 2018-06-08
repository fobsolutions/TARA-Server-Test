package ee.ria.tara.banklink.mock;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public final class IPizzaUtils {

    public static final String VK_COUNTRY = "VK_COUNTRY";
    public static final String VK_DATETIME = "VK_DATETIME";
    public static final String VK_ENCODING = "VK_ENCODING";
    public static final String VK_LANG = "VK_LANG";
    public static final String VK_MAC = "VK_MAC";
    public static final String VK_NONCE = "VK_NONCE";
    public static final String VK_OTHER = "VK_OTHER";
    public static final String VK_REC_ID = "VK_REC_ID";
    public static final String VK_RETURN = "VK_RETURN";
    public static final String VK_RID = "VK_RID";
    public static final String VK_SERVICE = "VK_SERVICE";
    public static final String VK_SND_ID = "VK_SND_ID";
    public static final String VK_TOKEN = "VK_TOKEN";
    public static final String VK_USER_ID = "VK_USER_ID";
    public static final String VK_USER_NAME = "VK_USER_NAME";
    public static final String VK_VERSION = "VK_VERSION";

    private IPizzaUtils() {}

    private static final String SUBMIT_FORM_ID = "submitForm";

    public static String buildReturnHtml(Map<String, String> parameters, String returnUrl, String method) {
        StringBuilder body = new StringBuilder();
        body.append(String.format(
                "<body onload=\"document.getElementById('%s').submit();\">",
                SUBMIT_FORM_ID
        ));
        body.append(String.format(
                "<form id=\"%s\" action=\"%s\" method=\"%s\">",
                SUBMIT_FORM_ID, returnUrl, method
        ));

        for (String key : parameters.keySet()) {
            body.append(String.format(
                    "<input type=\"hidden\" name=\"%s\" value=\"%s\">",
                    key, parameters.get(key)
            ));
        }

        body.append("</form></body>");

        String encoding = parameters.get(VK_ENCODING);
        return buildHtml(body.toString(), encoding);
    }

    private static String buildHtml(String htmlBody, String charset) {
        return String.format("<html><head>" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=%s\">" +
                        "</head>%s</html>",
                (charset == null ? StandardCharsets.UTF_8.name() : charset),
                htmlBody
        );
    }


    public static PrivateKey createPrivateKey(String keyBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static String calculateMac(Map<String, String> parameters, Charset encoding, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        byte[] bytes = flattenParameters(parameters).getBytes(encoding);

        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(bytes);

        bytes = signature.sign();
        return new String(Base64.getEncoder().encode(bytes), encoding);
    }

    private static String flattenParameters(Map<String, String> parameters) {
        StringBuilder flattened = new StringBuilder();

        for (String key : parameters.keySet()) {
            String value = parameters.get(key);
            flattened.append(StringUtils.leftPad(Integer.toString(value.length()), 3, '0'));
            flattened.append(value);
        }

        return flattened.toString();
    }


    public static <K, V> Map<K, V> asImmutableMap(Map<K, V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static <E> List<E> asImmutableList(Collection<E> collection) {
        return Collections.unmodifiableList(new LinkedList<>(collection));
    }

}
