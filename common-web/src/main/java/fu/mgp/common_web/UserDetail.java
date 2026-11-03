package fu.mgp.common_web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail {
    private UUID accountId;
    private String email;
    private List<String> roles;

    private static final ThreadLocal<UserDetail> CONTEXT = new ThreadLocal<>();

    public static void set(UserDetail detail) {
        CONTEXT.set(detail);
    }

    public static UserDetail get() {
        return CONTEXT.get();
    }

    public static UUID getAccountId() {
        UserDetail detail = get();
        return detail != null ? detail.getAccountId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

