package com.ee.digi_doc.service.impl;

import com.ee.digi_doc.common.properties.Digidoc4jProperties;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.service.FileSigner;
import lombok.SneakyThrows;
import org.digidoc4j.*;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileSignerImpl implements FileSigner {

    private final DigestAlgorithm algorithm;
    private final Configuration configuration;

    public FileSignerImpl(Digidoc4jProperties properties) {
        this.algorithm = properties.getAlgorithm();
        this.configuration = new Configuration(properties.getMode());
    }

    @Override
    public SigningData generateDataToSign(Collection<File> files, String certificateInHex) {
        List<DataFile> filesToSign = files.stream()
                .map(file -> new DataFile(file.getContent(), file.getName(), file.getContentType()))
                .collect(Collectors.toList());

        org.digidoc4j.Container container = createContainer(filesToSign);

        DataToSign dataToSign = createDataToSign(container, certificateInHex);

        return new SigningData(container, dataToSign);
    }

    @Override
    public Container signContainer(SigningData signingData, String signatureInHex) {
        Signature signature = signingData.getDataToSign().finalize(DatatypeConverter.parseHexBinary(signatureInHex));
        signingData.getContainer().addSignature(signature);
        return new Container(signingData.getContainer(), getContentType());
    }

    private String getContentType() {
        return "application/vnd.etsi.asic-e+zip";
    }

    private org.digidoc4j.Container createContainer(Collection<DataFile> dataFiles) {
        var builder = BDocContainerBuilder
                .aContainer()
                .withConfiguration(configuration);
        dataFiles.forEach(builder::withDataFile);
        return builder.build();
    }

    private DataToSign createDataToSign(org.digidoc4j.Container container, String certificateInHex) {
        return SignatureBuilder
                .aSignature(container)
                .withSigningCertificate(getCertificate(certificateInHex))
                .withSignatureDigestAlgorithm(algorithm)
                .withSignatureProfile(SignatureProfile.LT_TM)
                .buildDataToSign();
    }

    @SneakyThrows
    private X509Certificate getCertificate(String certificateInHex) {
        byte[] certificateBytes = DatatypeConverter.parseHexBinary(certificateInHex);
        try (InputStream inStream = new ByteArrayInputStream(certificateBytes)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(inStream);
        }
    }
}
