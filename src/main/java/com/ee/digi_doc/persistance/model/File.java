package com.ee.digi_doc.persistance.model;

import com.ee.digi_doc.exception.InvalidFileNameException;
import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "content")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, updatable = false, length = 50)
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

        file.setName(getFileName(multipartFile.getOriginalFilename()));
        file.setContentType(multipartFile.getContentType());
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
