package com.portfolio.support.admin;

import com.portfolio.support.audit.TicketHistory;
import com.portfolio.support.audit.TicketHistoryRepository;
import com.portfolio.support.shared.api.PageResponse;
import com.portfolio.support.shared.exception.BusinessRuleException;
import com.portfolio.support.shared.exception.NotFoundException;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import com.portfolio.support.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {
    private final UserRepository users;
    private final TicketHistoryRepository history;

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.UserResponse> users(Pageable pageable) {
        return PageResponse.from(users.findAll(pageable).map(this::toResponse));
    }

    @Transactional
    public AdminDtos.UserResponse changeRole(UUID id, Role role, User actor) {
        User user = findUser(id);
        if (user.getId().equals(actor.getId()) && role != Role.ADMIN) {
            throw new BusinessRuleException("An admin cannot remove their own admin role");
        }
        user.setRole(role);
        return toResponse(user);
    }

    @Transactional
    public AdminDtos.UserResponse changeEnabled(UUID id, boolean enabled, User actor) {
        User user = findUser(id);
        if (user.getId().equals(actor.getId()) && !enabled) {
            throw new BusinessRuleException("An admin cannot disable their own account");
        }
        user.setEnabled(enabled);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.HistoryResponse> history(UUID ticketId, Pageable pageable) {
        return PageResponse.from(history.findByTicketId(ticketId, pageable).map(this::toResponse));
    }

    private User findUser(UUID id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private AdminDtos.UserResponse toResponse(User user) {
        return new AdminDtos.UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole(), user.isEnabled(), user.getCreatedAt());
    }

    private AdminDtos.HistoryResponse toResponse(TicketHistory item) {
        return new AdminDtos.HistoryResponse(item.getId(), item.getTicket().getId(), item.getActor().getId(),
                item.getActor().getDisplayName(), item.getAction(), item.getOldValue(), item.getNewValue(),
                item.getCreatedAt());
    }
}
