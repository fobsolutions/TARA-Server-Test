package ee.ria.tara.banklink.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ee.ria.tara.banklink.mock")
public class BanklinkMockApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BanklinkMockApplication.class, args);
    }

}