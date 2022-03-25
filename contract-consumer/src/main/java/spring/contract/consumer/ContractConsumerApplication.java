package spring.contract.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import spring.contract.consumer.model.Hat;

@SpringBootApplication
public class ContractConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContractConsumerApplication.class, args);
	}

	@RestController
	public class HatConsumerResource {

		@Value( "${producer.port}" )
		private Integer producerPort;

		private final RestTemplate restTemplate;

		HatConsumerResource(RestTemplateBuilder restTemplateBuilder) {
			this.restTemplate = restTemplateBuilder.build();
		}

		@RequestMapping("/api/v1/wearhat/{hatId}")
		String getMessage(@PathVariable Long hatId) {

			ResponseEntity<Hat> response = restTemplate.exchange("http://localhost:"+producerPort+"/api/v1/hats/{hatId}",
																 HttpMethod.GET, null, Hat.class, hatId);
			Hat hat = response.getBody();

			return "Enjoy your new " + hat.getName();
		}
	}
}
