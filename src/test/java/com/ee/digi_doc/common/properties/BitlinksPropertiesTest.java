package com.ee.digi_doc.common.properties;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
class BitlinksPropertiesTest {

    @Value("${bitlinks.host-name}")
    private String hostName;
    @Value("${bitlinks.url}")
    private String url;
    @Value("${bitlinks.access-token}")
    private String accessToken;

    @Autowired
    private BitlinksProperties properties;

    @Test
    void testUploadProperties() {
        assertNotNull(properties);
        assertNotNull(properties.getHostName());
        assertNotNull(properties.getUrl());
        assertNotNull(properties.getAccessToken());

        assertEquals(hostName, properties.getHostName());
        assertEquals(accessToken, properties.getAccessToken());
        assertEquals(url, properties.getUrl());
    }

}