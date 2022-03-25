package spring.contract.producer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import spring.contract.producer.model.Hat;
import spring.contract.producer.service.HatService;

@RequestMapping("/api/v1/hats/")
@RequiredArgsConstructor
@RestController
public class HatController {

    private final HatService hatService;

    @GetMapping("{id}")
    public Hat findHatById(@PathVariable Long id) {
        Hat hat = hatService.findHatById(id);

        if(hat == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        return hat;
    }
}
