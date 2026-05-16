package pet.liro.chimi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChimiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChimiApiApplication.class, args);
    }
}
