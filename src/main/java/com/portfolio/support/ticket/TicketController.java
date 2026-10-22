package com.portfolio.support.ticket;

import com.portfolio.support.security.CurrentUser;
import com.portfolio.support.shared.api.PageResponse;
import com.portfolio.support.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TicketDtos.TicketResponse create(@Valid @RequestBody TicketDtos.CreateTicketRequest request,
                                      Authentication authentication) {
        return ticketService.create(request, currentUser.get(authentication));
    }

    @GetMapping({"", "/my"})
    PageResponse<TicketDtos.TicketResponse> search(
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

    @GetMapping("/{id}")
    TicketDtos.TicketResponse get(@PathVariable UUID id, Authentication authentication) {
        return ticketService.get(id, currentUser.get(authentication));
    }

    @PatchMapping("/{id}/status")
    TicketDtos.TicketResponse changeStatus(@PathVariable UUID id,
                                            @Valid @RequestBody TicketDtos.ChangeStatusRequest request,
                                            Authentication authentication) {
        return ticketService.changeStatus(id, request.status(), currentUser.get(authentication));
    }

    @PatchMapping("/{id}/assign")
    TicketDtos.TicketResponse assign(@PathVariable UUID id,
                                      @Valid @RequestBody TicketDtos.AssignTicketRequest request,
                                      Authentication authentication) {
        return ticketService.assign(id, request.assigneeId(), currentUser.get(authentication));
    }

    @PatchMapping("/{id}/priority")
    TicketDtos.TicketResponse changePriority(@PathVariable UUID id,
                                              @Valid @RequestBody TicketDtos.ChangePriorityRequest request,
                                              Authentication authentication) {
        return ticketService.changePriority(id, request.priority(), currentUser.get(authentication));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    TicketDtos.CommentResponse comment(@PathVariable UUID id,
                                        @Valid @RequestBody TicketDtos.AddCommentRequest request,
                                        Authentication authentication) {
        return ticketService.addComment(id, request, currentUser.get(authentication));
    }

    @GetMapping("/{id}/comments")
    PageResponse<TicketDtos.CommentResponse> comments(@PathVariable UUID id,
                                                       @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
                                                       Authentication authentication) {
        return ticketService.comments(id, currentUser.get(authentication), pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id, Authentication authentication) {
        ticketService.delete(id, currentUser.get(authentication));
    }
}
