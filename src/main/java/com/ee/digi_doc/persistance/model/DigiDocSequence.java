package com.ee.digi_doc.persistance.model;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "digi_doc_sequence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(staticName = "of")
public class DigiDocSequence {

    @Id
    @NonNull
    @Column(updatable = false)
    private Class<?> name;

    private Integer nextValue = 0;

    private Integer incrementValue = 1;

}
