package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.HelpTopicResponse;
import com.sunrise.dental.service.HelpService;
import com.sunrise.dental.service.impl.HelpServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HelpControllerTest {

    @Mock
    private HelpService helpService;

    @InjectMocks
    private HelpController helpController;

    @Nested
    @DisplayName("GET /api/help")
    class GetAll {

        @Test
        @DisplayName("returns all help topics")
        void getAll_success() {
            List<HelpTopicResponse> topics = List.of(
                    new HelpTopicResponse(1L, "Title", "Category", "Content", 1));
            when(helpService.getAllTopics()).thenReturn(topics);

            ResponseEntity<ApiResponse<List<HelpTopicResponse>>> result = helpController.getAll();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Help topics retrieved", result.getBody().getMessage());
            assertEquals(1, result.getBody().getData().size());
        }

        @Test
        @DisplayName("returns empty list when no topics")
        void getAll_empty() {
            when(helpService.getAllTopics()).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<HelpTopicResponse>>> result = helpController.getAll();

            assertTrue(result.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/help/search")
    class Search {

        @Test
        @DisplayName("searches help topics")
        void search_success() {
            List<HelpTopicResponse> topics = List.of(
                    new HelpTopicResponse(1L, "How to login", "Getting Started", "Content", 1));
            when(helpService.search("login")).thenReturn(topics);

            ResponseEntity<ApiResponse<List<HelpTopicResponse>>> result = helpController.search("login");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
            verify(helpService).search("login");
        }

        @Test
        @DisplayName("returns all topics when query is null")
        void search_null() {
            List<HelpTopicResponse> topics = new HelpServiceImpl().getAllTopics();
            when(helpService.search(null)).thenReturn(topics);

            ResponseEntity<ApiResponse<List<HelpTopicResponse>>> result = helpController.search(null);

            assertEquals(HttpStatus.OK, result.getStatusCode());
        }
    }
}
