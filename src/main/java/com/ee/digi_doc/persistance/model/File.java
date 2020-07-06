package com.ee.digi_doc.persistance.model;

import com.ee.digi_doc.exception.InvalidFileNameException;
import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Entity
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "content")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String name;

    @Transient
    private byte[] content;

    @Column(nullable = false, updatable = false)
    private String contentType;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime uploadedOn;

    @PrePersist
    public void prePersist() {
        setUploadedOn(LocalDateTime.now());
    }

    @SneakyThrows
    public static File of(MultipartFile multipartFile) {
        File file = new File();

        file.setName(getFileName(ofNullable(multipartFile.getOriginalFilename()).orElseGet(multipartFile::getName)));
        file.setContentType(ofNullable(multipartFile.getContentType()).orElse(APPLICATION_OCTET_STREAM_VALUE));
        file.setContent(multipartFile.getBytes());

        return file;
    }

    private static String getFileName(@NotEmpty String fileName) {
        String cleanedFileName = StringUtils.cleanPath(fileName);

        if (cleanedFileName.contains("..")) {
            throw new InvalidFileNameException(fileName);
        }

        return cleanedFileName;
    }
}
