package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.common.properties.BitlinksProperties;
import com.ee.digi_doc.service.GenerateLinkService;
import com.ee.digi_doc.web.GenerateLinkResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GenerateLinkServiceImpl implements GenerateLinkService {

    private final RestTemplate restTemplate;
    private final BitlinksProperties properties;

    @Autowired
    public GenerateLinkServiceImpl(BitlinksProperties properties) {
        this.restTemplate = new RestTemplate();
        this.properties = properties;
    }

    @Override
    public ResponseEntity<GenerateLinkResponse> generate(String longLink) {
        return restTemplate.postForEntity(properties.getUrl(), createRequest(longLink), GenerateLinkResponse.class);
    }

    private HttpEntity<CreateLinkRequest> createRequest(String longLink) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getAccessToken());
        CreateLinkRequest request = CreateLinkRequest.builder().longUrl(properties.getHostName() + longLink).build();
        return new HttpEntity<>(request, headers);
    }

    @Getter
    @Builder
    private static class CreateLinkRequest {
        @JsonProperty("long_url")
        private final String longUrl;
    }
}
