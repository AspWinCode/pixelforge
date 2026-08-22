package studio.pixelforge.backend.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenEventRepository extends JpaRepository<TokenEvent, Long> {

    // COALESCE защищает от NULL, если у пользователя ещё нет ни одного события —
    // без него SUM() по пустой выборке вернёт NULL, а не 0.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TokenEvent t WHERE t.user.id = :userId")
    Long sumAmountByUserId(@Param("userId") Long userId);
}
