package studio.pixelforge.backend.token;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/users/{userId}/balance")
    public BalanceResponse balance(@PathVariable Long userId) {
        long balance = tokenService.balance(userId);
        Rank rank = Rank.fromBalance(balance);
        return new BalanceResponse(userId, balance, rank, rank.getDisplayName());
    }
}
