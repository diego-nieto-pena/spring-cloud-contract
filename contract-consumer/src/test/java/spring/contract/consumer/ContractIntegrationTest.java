package spring.contract.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import spring.contract.consumer.model.Hat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureStubRunner( ids = {"spring.contract.producer:spring-contract-producer:+:stubs:8100"},
        stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class ContractIntegrationTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void get_hat1() {
        ResponseEntity<Hat> responseEntity = restTemplate.getForEntity("http://localhost:8100/api/v1/hats/1", Hat.class);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Hat hat = responseEntity.getBody();
        assertThat(hat.getId()).isEqualTo(1);
        assertThat(hat.getName()).isEqualTo("Test Hat 1");
        assertThat(hat.getSize()).isEqualTo(10);
        assertThat(hat.getColor()).isEqualTo("striped");
    }

    @Test
    public void get_hat2() {
        ResponseEntity<Hat> responseEntity = restTemplate.getForEntity("http://localhost:8100/api/v1/hats/2", Hat.class);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Hat hat = responseEntity.getBody();
        assertThat(hat.getId()).isEqualTo(2);
        assertThat(hat.getName()).isEqualTo("Test Hat 2");
        assertThat(hat.getSize()).isEqualTo(7);
        assertThat(hat.getColor()).isEqualTo("green");
    }

    @Test
    public void get_hat3() {
        try {
            ResponseEntity<Hat> responseEntity = restTemplate.getForEntity("http://localhost:8100/api/v1/hats/3", Hat.class);
        } catch(HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
