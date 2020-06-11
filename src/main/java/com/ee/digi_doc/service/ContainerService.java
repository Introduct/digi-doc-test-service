package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.request.SignContainerRequest;

import java.util.Optional;

public interface ContainerService {

    Container signContainer(SignContainerRequest request);

    Optional<Container> get(Long id);

}
