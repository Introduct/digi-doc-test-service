package com.ee.digi_doc.service;

import org.springframework.http.ResponseEntity;

public interface GenerateLinkService {

    ResponseEntity<Object> generate(String longLink);

}
