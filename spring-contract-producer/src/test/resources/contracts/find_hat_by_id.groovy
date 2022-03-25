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