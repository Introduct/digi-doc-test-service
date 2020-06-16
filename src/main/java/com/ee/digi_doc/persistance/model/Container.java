package com.ee.digi_doc.persistance.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = "id")
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime signedOn;

    @Transient
    private org.digidoc4j.Container bdDocContainer;

    @PrePersist
    public void prePersist() {
        setSignedOn(LocalDateTime.now());
    }

    @SneakyThrows
    public byte[] getContent() {
        return bdDocContainer.saveAsStream().readAllBytes();
    }
}
