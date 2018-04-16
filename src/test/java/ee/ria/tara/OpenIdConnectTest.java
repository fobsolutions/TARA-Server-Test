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

@SpringBootTest(classes = OpenIdConnectTest.class)
@Category(IntegrationTest.class)
public class OpenIdConnectTest extends TestsBase {



}
