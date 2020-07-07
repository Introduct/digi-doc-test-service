package com.ee.digi_doc.service;

import com.ee.digi_doc.web.GenerateLinkResponse;
import org.springframework.http.ResponseEntity;

public interface GenerateLinkService {

    ResponseEntity<GenerateLinkResponse> generate(String longLink);

}
