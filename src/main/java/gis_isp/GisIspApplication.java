package gis_isp;

import gis_isp.auth.LoginLockoutProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LoginLockoutProperties.class)
public class GisIspApplication {

	public static void main(String[] args) {
		SpringApplication.run(GisIspApplication.class, args);
	}

}
