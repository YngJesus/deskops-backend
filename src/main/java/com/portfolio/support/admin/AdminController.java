package com.portfolio.support.admin;

import com.portfolio.support.security.CurrentUser;
import com.portfolio.support.shared.api.PageResponse;
import com.portfolio.support.ticket.TicketCategory;
import com.portfolio.support.ticket.TicketDtos;
import com.portfolio.support.ticket.TicketPriority;
import com.portfolio.support.ticket.TicketService;
import com.portfolio.support.ticket.TicketStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final TicketService ticketService;
    private final CurrentUser currentUser;

    @GetMapping("/tickets")
    PageResponse<TicketDtos.TicketResponse> tickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, name = "q") String query,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        return ticketService.search(currentUser.get(authentication), status, priority, category,
                from, to, query, pageable);
    }

    @GetMapping("/users")
    PageResponse<AdminDtos.UserResponse> users(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return adminService.users(pageable);
    }

    @PatchMapping("/users/{id}/role")
    AdminDtos.UserResponse role(@PathVariable UUID id, @Valid @RequestBody AdminDtos.ChangeRoleRequest request,
                                 Authentication authentication) {
        return adminService.changeRole(id, request.role(), currentUser.get(authentication));
    }

    @PatchMapping("/users/{id}/enabled")
    AdminDtos.UserResponse enabled(@PathVariable UUID id,
                                    @Valid @RequestBody AdminDtos.ChangeEnabledRequest request,
                                    Authentication authentication) {
        return adminService.changeEnabled(id, request.enabled(), currentUser.get(authentication));
    }

    @GetMapping("/tickets/{ticketId}/history")
    PageResponse<AdminDtos.HistoryResponse> history(@PathVariable UUID ticketId,
                                                     @PageableDefault(size = 30, sort = "createdAt") Pageable pageable) {
        return adminService.history(ticketId, pageable);
    }
}
