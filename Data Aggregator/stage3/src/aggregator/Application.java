package aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class DataRestController {
    private final TransactionService transactionService;

    DataRestController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/aggregate")
    public List<Transaction> getData(@RequestParam String account) {
        return transactionService.fetchTransactions(account);
    }
}

@Service
class TransactionService {
    private final RestTemplate template = new RestTemplate();

    public List<Transaction> fetchTransactions(String account) {
        var list1 = fetchWithRetry(template, "http://localhost:8888/transactions?account=" + account);
        var list2 = fetchWithRetry(template, "http://localhost:8889/transactions?account=" + account);
        return Stream.of(list1, list2)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Transaction::timestamp).reversed())
                .toList();
    }

    private List<Transaction> fetchWithRetry(RestTemplate template, String url) {
        int retries = 5;
        for (int attempt = 0; attempt < retries; attempt++) {
            try {
                ResponseEntity<List<Transaction>> response = template.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {}
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                }
            } catch (HttpServerErrorException | HttpClientErrorException e) {
                System.out.println("Server responded with error: " + e.getMessage());
            }
        }
        return List.of();
    }
}

record Transaction(
        String id,
        String serverId,
        String account,
        String amount,
        String timestamp
){}
