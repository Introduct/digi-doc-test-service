package com.ee.digi_doc.persistance.model;

import eu.europa.esig.dss.spi.DSSUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.digidoc4j.Container;
import org.digidoc4j.DataToSign;

import javax.persistence.*;
import javax.xml.bind.DatatypeConverter;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = "id")
public class SigningData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, updatable = false, length = 50)
    private String containerName;

    @Column(nullable = false, updatable = false, length = 50)
    private String dataToSignName;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime createdOn;

    @Transient
    private Container container;

    @Transient
    private DataToSign dataToSign;

    @PrePersist
    public void prePersist() {
        setCreatedOn(LocalDateTime.now());
    }

    public String getSignatureInHex() {
        return DatatypeConverter.printHexBinary(DSSUtils.digest(eu.europa.esig.dss.enumerations.DigestAlgorithm.SHA256,
                dataToSign.getDataToSign()));
    }
}
