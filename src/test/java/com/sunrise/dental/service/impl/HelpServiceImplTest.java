package com.sunrise.dental.service.impl;

import com.sunrise.dental.dto.response.HelpTopicResponse;
import com.sunrise.dental.service.HelpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HelpServiceImplTest {

    private final HelpService helpService = new HelpServiceImpl();

    @Nested
    @DisplayName("getAllTopics()")
    class GetAllTopics {

        @Test
        @DisplayName("returns all help topics")
        void getAllTopics_returnsAll() {
            List<HelpTopicResponse> topics = helpService.getAllTopics();

            assertNotNull(topics);
            assertEquals(12, topics.size());
        }

        @Test
        @DisplayName("topics have unique ids")
        void getAllTopics_uniqueIds() {
            List<HelpTopicResponse> topics = helpService.getAllTopics();

            long uniqueIds = topics.stream().map(HelpTopicResponse::id).distinct().count();
            assertEquals(topics.size(), uniqueIds);
        }

        @Test
        @DisplayName("all topics have non-empty titles and content")
        void getAllTopics_content() {
            List<HelpTopicResponse> topics = helpService.getAllTopics();

            assertTrue(topics.stream().allMatch(t -> t.title() != null && !t.title().isBlank()));
            assertTrue(topics.stream().allMatch(t -> t.content() != null && !t.content().isBlank()));
            assertTrue(topics.stream().allMatch(t -> t.category() != null && !t.category().isBlank()));
        }
    }

    @Nested
    @DisplayName("getByCategory()")
    class GetByCategory {

        @Test
        @DisplayName("returns topic for matching category")
        void getByCategory_match() {
            HelpTopicResponse result = helpService.getByCategory("Appointments");

            assertNotNull(result);
            assertEquals("Appointments", result.category());
        }

        @Test
        @DisplayName("category matching is case-insensitive")
        void getByCategory_caseInsensitive() {
            HelpTopicResponse result = helpService.getByCategory("appointments");

            assertEquals("Appointments", result.category());
        }

        @Test
        @DisplayName("returns first topic for unknown category")
        void getByCategory_unknown() {
            HelpTopicResponse result = helpService.getByCategory("DoesNotExist");

            assertNotNull(result);
            assertEquals(1L, result.id());
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("returns all topics for null query")
        void search_nullQuery() {
            List<HelpTopicResponse> result = helpService.search(null);

            assertEquals(12, result.size());
        }

        @Test
        @DisplayName("returns all topics for blank query")
        void search_blankQuery() {
            List<HelpTopicResponse> result = helpService.search("   ");

            assertEquals(12, result.size());
        }

        @Test
        @DisplayName("filters topics matching title")
        void search_byTitle() {
            List<HelpTopicResponse> result = helpService.search("log in");

            assertFalse(result.isEmpty());
            assertTrue(result.stream().allMatch(t ->
                    t.title().toLowerCase().contains("log in")
                            || t.category().toLowerCase().contains("log in")
                            || t.content().toLowerCase().contains("log in")));
        }

        @Test
        @DisplayName("filters topics matching category")
        void search_byCategory() {
            List<HelpTopicResponse> result = helpService.search("Billing");

            assertFalse(result.isEmpty());
            assertTrue(result.stream().anyMatch(t -> t.category().contains("Billing")));
        }

        @Test
        @DisplayName("search is case-insensitive")
        void search_caseInsensitive() {
            List<HelpTopicResponse> result = helpService.search("BILLING");

            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for unmatched query")
        void search_noMatch() {
            List<HelpTopicResponse> result = helpService.search("xyzzy_nothing");

            assertTrue(result.isEmpty());
        }
    }
}
