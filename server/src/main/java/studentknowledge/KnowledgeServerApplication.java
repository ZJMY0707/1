package studentknowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KnowledgeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServerApplication.class, args);
    }
}
