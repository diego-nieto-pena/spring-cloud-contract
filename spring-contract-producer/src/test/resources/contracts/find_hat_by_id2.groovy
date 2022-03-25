package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return hat by id=2"

    request {
        url "/api/v1/hats/2"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                id: 2,
                name: "Test Hat 2",
                size: 7,
                color: "green"
        )
    }
}