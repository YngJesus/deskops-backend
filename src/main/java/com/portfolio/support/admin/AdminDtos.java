package com.portfolio.support.admin;

import com.portfolio.support.user.Role;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class AdminDtos {
    private AdminDtos() {}

    public record UserResponse(UUID id, String email, String displayName, Role role,
                               boolean enabled, Instant createdAt) {}
    public record ChangeRoleRequest(@NotNull Role role) {}
    public record ChangeEnabledRequest(boolean enabled) {}
    public record HistoryResponse(UUID id, UUID ticketId, UUID actorId, String actorName,
                                  String action, String oldValue, String newValue, Instant createdAt) {}
}
