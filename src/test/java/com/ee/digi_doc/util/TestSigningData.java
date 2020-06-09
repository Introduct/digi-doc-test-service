package com.ee.digi_doc.util;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.digidoc4j.DataToSign;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.signers.PKCS12SignatureToken;
import org.springframework.core.io.ClassPathResource;

import java.security.cert.X509Certificate;

public class TestSigningData {

    @SneakyThrows
    public static String getRSASigningCertificateInHex() {
        X509Certificate certificate = getRSAToken().getCertificate();
        byte[] derEncodedCertificate = certificate.getEncoded();
        return Hex.encodeHexString(derEncodedCertificate);
    }

    public static String rsaSignData(DataToSign dataToSign, DigestAlgorithm digestAlgorithm) {
        byte[] signatureValue = getRSAToken().sign(digestAlgorithm, dataToSign.getDataToSign());
        return Hex.encodeHexString(signatureValue);
    }

    @SneakyThrows
    private static PKCS12SignatureToken getRSAToken() {
        String resolvedPath = new ClassPathResource("signout.p12").getFile().getCanonicalPath();
        return new PKCS12SignatureToken(resolvedPath, "test");
    }
}
