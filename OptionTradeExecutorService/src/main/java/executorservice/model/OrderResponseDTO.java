package executorservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class OrderResponseDTO {

    private String status;
    private String orderId;
}
