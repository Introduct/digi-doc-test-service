package com.ee.digi_doc.persistance.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "content")
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Transient
    private byte[] content;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime createdOn;

    @Transient
    private String signatureInHex;

    @PrePersist
    public void prePersist() {
        createdOn = LocalDateTime.now();
    }

}
