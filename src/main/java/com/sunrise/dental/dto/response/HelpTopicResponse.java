package com.sunrise.dental.dto.response;

public record HelpTopicResponse(
        Long id,
        String title,
        String category,
        String content,
        int orderIndex
) {
}
