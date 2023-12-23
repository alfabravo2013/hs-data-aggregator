package aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class DataRestController {
    @GetMapping("/aggregate")
    public List<Transaction> getData(@RequestParam String account) {
        RestTemplate template = new RestTemplate();
        var url1 = "http://localhost:8888/transactions?account=" + account;
        var url2 = "http://localhost:8889/transactions?account=" + account;
        ResponseEntity<List<Transaction>> response1 = template.exchange(
                url1,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        ResponseEntity<List<Transaction>> response2 = template.exchange(
                url2,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return Stream.of(response1.getBody(), response2.getBody())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Transaction::timestamp).reversed())
                .toList();
    }
}

record Transaction(
        String id,
        String serverId,
        String account,
        String amount,
        String timestamp
){}
