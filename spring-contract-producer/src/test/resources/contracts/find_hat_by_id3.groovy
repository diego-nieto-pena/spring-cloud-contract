package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return 404  for hat id=2"

    request {
        url "/api/v1/hats/3"
        method GET()
    }

    response {
        status 404
    }
}