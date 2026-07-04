package ifba.engsoft.vidaplena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class VidaplenaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VidaplenaApplication.class, args);
	}

}
