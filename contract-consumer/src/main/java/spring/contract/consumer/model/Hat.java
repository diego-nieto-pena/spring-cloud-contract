package spring.contract.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hat {
    private Long id;
    private String name;
    private Long size;
    private String color;
}
