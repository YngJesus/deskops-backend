package com.portfolio.support.attachment;

import com.portfolio.support.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AttachmentController {
    private final AttachmentService attachmentService;
    private final CurrentUser currentUser;

    @PostMapping(value = "/tickets/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AttachmentDtos.AttachmentResponse> upload(@PathVariable UUID ticketId,
                                                              @RequestPart("file") MultipartFile file,
                                                              Authentication authentication) {
        return ResponseEntity.status(201).body(
                attachmentService.upload(ticketId, file, currentUser.get(authentication)));
    }

    @GetMapping("/attachments/{id}/download")
    ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable UUID id,
                                                                  Authentication authentication) {
        AttachmentService.DownloadedAttachment download =
                attachmentService.download(id, currentUser.get(authentication));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.resource());
    }
}
