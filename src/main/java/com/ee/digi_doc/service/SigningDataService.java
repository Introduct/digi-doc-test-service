package com.ee.digi_doc.service;

import com.ee.digi_doc.persistance.model.SigningData;
import com.ee.digi_doc.web.request.CreateSigningDataRequest;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface SigningDataService {

    SigningData create(CreateSigningDataRequest request);

    Optional<SigningData> getSigningData(@NotNull Long id);

}
