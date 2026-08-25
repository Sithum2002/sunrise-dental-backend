package com.sunrise.dental.service;

import com.sunrise.dental.dto.response.HelpTopicResponse;

import java.util.List;

public interface HelpService {

    List<HelpTopicResponse> getAllTopics();

    HelpTopicResponse getByCategory(String category);

    List<HelpTopicResponse> search(String query);
}
