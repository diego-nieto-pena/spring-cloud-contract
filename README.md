# spring-cloud-contract

Testing microservices is a challenging task, we have two options for testing the service **v1** in the following scenario:

![scenario](https://github.com/diego-nieto-pena/spring-cloud-contract/blob/main/img/img_1.png)

### Option 1 - Deploy all microservices and perform end-to-end tests.

#### Pros
- Simulates production
- Tests real communiction between services
#### Cons
- Testing one microservice requires the deployment for 6 microservices, databases and others.
- Running the tests will lock the environment (no one else can run the tests at the same time).
- Time consuming
- Hard to debug

### Option 2 - Mock all other services for unitary and integration tests

#### Pros
- Fast feedback.
- No infrastructure requirements.
#### Cons
- The implementor of the service creates stubs that might have nothing to do with reality.
- Tests can pass locally but fail in production.

## Spring Cloud Contract
Focused on give a very fast feedback, avoding to setup all the involved microservices, the unique applications needed are those that your application directly uses.

![stubs](https://github.com/diego-nieto-pena/spring-cloud-contract/blob/main/img/img_2.png)

Spring Cloud Contract gives you the certainty that the stubs that you use were created by the service that you are calling. Also, if you can use them it means that were also tested against the producer side.

## The Contract

Consumer servives needs to define exactly what to achieve, a contract is an agreement on how the API communication should look.

Assume that you want to send a request that contains the ID of a client company and the amount it wants to borrow. The request will be send to the URL **/fraudcheck** by using the **PUT** method.

```
/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package contracts

org.springframework.cloud.contract.spec.Contract.make {
    request { // (1)
        method 'PUT' // (2)
        url '/fraudcheck' // (3)
        body([ // (4)
               "client.id": $(regex('[0-9]{10}')),
               loanAmount : 99999
        ])
        headers { // (5)
            contentType('application/json')
        }
    }
    response { // (6)
        status OK() // (7)
        body([ // (8)
               fraudCheckStatus  : "FRAUD",
               "rejection.reason": "Amount too high"
        ])
        headers { // (9)
            contentType('application/json')
        }
    }
}

/*
From the Consumer perspective, when shooting a request in the integration test:

(1) - If the consumer sends a request
(2) - With the "PUT" method
(3) - to the URL "/fraudcheck"
(4) - with the JSON body that
 * has a field `client.id` that matches a regular expression `[0-9]{10}`
 * has a field `loanAmount` that is equal to `99999`
(5) - with header `Content-Type` equal to `application/json`
(6) - then the response will be sent with
(7) - status equal `200`
(8) - and JSON body equal to
 { "fraudCheckStatus": "FRAUD", "rejectionReason": "Amount too high" }
(9) - with header `Content-Type` equal to `application/json`

From the Producer perspective, in the autogenerated producer-side test:

(1) - A request will be sent to the producer
(2) - With the "PUT" method
(3) - to the URL "/fraudcheck"
(4) - with the JSON body that
 * has a field `client.id` that will have a generated value that matches a regular expression `[0-9]{10}`
 * has a field `loanAmount` that is equal to `99999`
(5) - with header `Content-Type` equal to `application/json`
(6) - then the test will assert if the response has been sent with
(7) - status equal `200`
(8) - and JSON body equal to
 { "fraudCheckStatus": "FRAUD", "rejectionReason": "Amount too high" }
(9) - with header `Content-Type` matching `application/json.*`
 */
```

## The Producer

By default the contracts directory is **$rootDir/src/test/resources/contracts**.

1- Adding the dependency:
```
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-contract-verifier</artifactId>
      <scope>test</scope>
    </dependency>
    
    <build>
        <plugins>
          <plugin>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-maven-plugin</artifactId>
            <version>3.1.1</version>
            <extensions>true</extensions>
            <configuration>
              <testFramework>JUNIT5</testFramework>
              <baseClassForTests>spring.contract.producer.BaseClass</baseClassForTests>
            </configuration>
          </plugin>
        </plugins>
    </build>
  ```
  A base class must be added, this class will be extended by all the auto-generated tests, it should contain all the setup information:
  
  ```
  <baseClassForTests>spring.contract.producer.BaseClass</baseClassForTests>
  
  @SpringBootTest(classes = SpringContractProducerApplication.class)
  public class BaseClass {

      @Autowired
      HatController hatController;

      @MockBean
      HatService hatService;

      @BeforeEach
      public void setup() {
          RestAssuredMockMvc.standaloneSetup(hatController);
          Mockito.when(hatService.findHatById(1L)).thenReturn(new Hat(1L, "Test Hat 1", 10L, "striped"));
          Mockito.when(hatService.findHatById(2L)).thenReturn(new Hat(2L, "Test Hat 2", 7L, "green"));
      }
  }
  ```
  ### Adding the contracts
  
  Contracts can be written in YAML or Groovy **src/test/resources/contracts/**:
  ```
    package contracts

    import org.springframework.cloud.contract.spec.Contract

    Contract.make {
        description "Should return hat by id=1"

        request {
            url "/api/v1/hats/1"
            method GET()
        }

        response {
            status OK()
            headers {
                contentType applicationJson()
            }
            body(
                    id: 1,
                    name: "Test Hat 1",
                    size: 10,
                    color: "striped"
            )
        }
    }
  ```
  
  Generate ths stubs by executing ```./mvnw install```, this will generate and install the stubs in the local ```.m2``` repository.
  
  ```
    [INFO] --- spring-cloud-contract-maven-plugin:3.1.1:generateTests (default-generateTests) @ spring-contract-producer ---
    [INFO] Generating server tests source code for Spring Cloud Contract Verifier contract verification
    [INFO] Will use contracts provided in the folder [/Users/diego/spring-contract-producer/src/test/resources/contracts]
    [INFO] Directory with contract is present at [/Users/diego/spring-contract/spring-contract-producer/src/test/resources/contracts]
    [INFO] Test Source directory: /Users/diego/spring-contract/spring-contract-producer/target/generated-test-sources/contracts added.
    [INFO] Using [spring.contract.producer.BaseClass] as base class for test classes, [null] as base package for tests, [null] as package with base classes, base class mappings []
    [INFO] Creating new class file [/Users/diego/spring-contract/spring-contract-producer/target/generated-test-sources/contracts/spring/contract/producer/ContractVerifierTest.java]
    [INFO] Generated 1 test classes.
  ```
  
Spring found the contracts in the ```/src/test/resources/contracts``` folder, used the base test class defined in the ```pom.xml```, and generated integration tests based on those contracts.
  
The generated integration tests will look like this:

```
    package spring.contract.producer;

    import spring.contract.producer.BaseClass;
    import com.jayway.jsonpath.DocumentContext;
    import com.jayway.jsonpath.JsonPath;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
    import io.restassured.response.ResponseOptions;

    import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
    import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
    import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
    import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;

    @SuppressWarnings("rawtypes")
    public class ContractVerifierTest extends BaseClass {

      @Test
      public void validate_find_hat_by_id() throws Exception {
        // given:
          MockMvcRequestSpecification request = given();


        // when:
          ResponseOptions response = given().spec(request)
              .get("/api/v1/hats/1");

        // then:
          assertThat(response.statusCode()).isEqualTo(200);
          assertThat(response.header("Content-Type")).matches("application/json.*");

        // and:
          DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
          assertThatJson(parsedJson).field("['id']").isEqualTo(1);
          assertThatJson(parsedJson).field("['name']").isEqualTo("Test Hat 1");
          assertThatJson(parsedJson).field("['size']").isEqualTo(10);
          assertThatJson(parsedJson).field("['color']").isEqualTo("striped");
      }

      @Test
      public void validate_find_hat_by_id2() throws Exception {
        // given:
          MockMvcRequestSpecification request = given();


        // when:
          ResponseOptions response = given().spec(request)
              .get("/api/v1/hats/2");

        // then:
          assertThat(response.statusCode()).isEqualTo(200);
          assertThat(response.header("Content-Type")).matches("application/json.*");

        // and:
          DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
          assertThatJson(parsedJson).field("['id']").isEqualTo(2);
          assertThatJson(parsedJson).field("['name']").isEqualTo("Test Hat 2");
          assertThatJson(parsedJson).field("['size']").isEqualTo(7);
          assertThatJson(parsedJson).field("['color']").isEqualTo("green");
      }

      @Test
      public void validate_find_hat_by_id3() throws Exception {
        // given:
          MockMvcRequestSpecification request = given();


        // when:
          ResponseOptions response = given().spec(request)
              .get("/api/v1/hats/3");

        // then:
          assertThat(response.statusCode()).isEqualTo(404);
      }

    }

```
**The Groovy contracts has been turned into testable assertions. The producer contract is tested against the actual behavior of the producer.**

The stubs jar also was installed in the local Maven repository:

```
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ spring-contract-producer ---
[INFO] Installing /Users/diego/spring-contract/spring-contract-producer/target/spring-contract-producer-0.0.1-SNAPSHOT.jar to /Users/diego/.m2/repository/spring/contract/producer/spring-contract-producer/0.0.1-SNAPSHOT/spring-contract-producer-0.0.1-SNAPSHOT.jar
```

The stubs behavior is defined to match the contract, so its integration testing is guaranteed.

## The consumer

Add the following dependency 

```
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
      <scope>test</scope>
    </dependency>
```

This service contains a simple endpoint it will be consuming the producer service:
```
		@RequestMapping("/api/v1/wearhat/{hatId}")
		String getMessage(@PathVariable Long hatId) {

			ResponseEntity<Hat> response = restTemplate.exchange("http://localhost:" + producerPort   + "/api/v1/hats/{hatId}",
																 HttpMethod.GET, null, Hat.class, hatId);
			Hat hat = response.getBody();

			return "Enjoy your new " + hat.getName();
		}
```

The request/response will look like:

```
http :8080/api/v1/hats/1 

HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 23 Mar 2022 16:35:55 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "color": "red",
    "id": 1,
    "name": "Sombrero",
    "size": 30
}
```

The stubbed producer will run in the port defined (**8100**) in the ```src/test/resources/application.properties```


A basic integration test will look like:

```
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
    }
```

Importing the producer stubs is done by specifying the group and artyfact IDs
```ids = {"spring.contract.producer:spring-contract-producer:+:stubs:8100"}```

```stubsMode = StubRunnerProperties.StubsMode.LOCAL``` The stub runner in LOCAL mode means that it will look in the local Maven repo instead of looking online.

```
    @SpringBootTest
    @AutoConfigureMockMvc
    @AutoConfigureStubRunner(ids = {"spring.contract.producer:spring-contract-producer:+:stubs:8100"},
            stubsMode = StubRunnerProperties.StubsMode.LOCAL)
    public class ConsumerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        public void shouldReturnDefaultMessage() throws Exception {
            this.mockMvc.perform(get("/api/v1/wearhat/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Enjoy your new Test Hat 1")));
        }
    }
```
As the stubbed producer its running in the port 8100, the request are done against it instead of the "real" producer on port 8080.

## References

- https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/getting-started.html#getting-started
- https://developer.okta.com/blog/2022/02/01/spring-cloud-contract
