package com.sunrise.dental.dto;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Nested
    @DisplayName("success(message, data)")
    class SuccessWithData {

        @Test
        @DisplayName("builds success envelope with data")
        void success() {
            ApiResponse<String> response = ApiResponse.success("Done", "data");

            assertTrue(response.isSuccess());
            assertEquals("Done", response.getMessage());
            assertEquals("data", response.getData());
            assertNotNull(response.getTimestamp());
        }
    }

    @Nested
    @DisplayName("success(message)")
    class SuccessOnly {

        @Test
        @DisplayName("builds success envelope without data")
        void success() {
            ApiResponse<String> response = ApiResponse.success("Done");

            assertTrue(response.isSuccess());
            assertEquals("Done", response.getMessage());
            assertNull(response.getData());
        }
    }

    @Nested
    @DisplayName("error(message)")
    class Error {

        @Test
        @DisplayName("builds error envelope")
        void error() {
            ApiResponse<String> response = ApiResponse.error("Failed");

            assertFalse(response.isSuccess());
            assertEquals("Failed", response.getMessage());
            assertNull(response.getData());
        }
    }
}

class PageResponseTest {

    @Test
    @DisplayName("of builds from Spring Page")
    void of() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"),
                org.springframework.data.domain.PageRequest.of(0, 2), 4);

        PageResponse<String> response = PageResponse.of(page);

        assertEquals(List.of("a", "b"), response.content());
        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(4L, response.totalElements());
        assertEquals(2, response.totalPages());
    }
}
