package aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class DataRestController {
    @GetMapping("/data")
    public String getData() {
        RestTemplate template = new RestTemplate();
        var url = "http://localhost:8889/ping";
        return template.getForObject(url, String.class);
    }
}
