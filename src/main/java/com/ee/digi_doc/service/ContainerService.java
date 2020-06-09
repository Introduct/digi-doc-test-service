package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.web.request.CreateContainerRequest;

import javax.validation.constraints.NotNull;

public interface ContainerService {

    Container create(CreateContainerRequest request);

    Container get(@NotNull Long id);

}
