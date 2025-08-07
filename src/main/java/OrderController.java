import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altradar.service.UpbitOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upbit")
@RequiredArgsConstructor
public class OrderController {

    private final UpbitOrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestParam String market, @RequestParam String price) {
        String response = orderService.placeOrder(market, price, "bid");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sell(@RequestParam String market, @RequestParam String price) {
        String response = orderService.placeOrder(market, price, "ask");
        return ResponseEntity.ok(response);
    }
}