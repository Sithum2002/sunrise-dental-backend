package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.HelpTopicResponse;
import com.sunrise.dental.service.HelpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Help section - step-by-step instructions for clinic staff.
 */
@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpService helpService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HelpTopicResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Help topics retrieved", helpService.getAllTopics()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<HelpTopicResponse>>> search(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success("Help topics retrieved", helpService.search(q)));
    }
}
