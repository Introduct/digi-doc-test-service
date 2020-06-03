package com.ee.digi_doc.persistance.model;

import com.ee.digi_doc.exception.InvalidFileNameException;
import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@Entity
@ToString(exclude = "content")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false, length = 20)
    private String fileName;

    @Column(nullable = false, length = 20)
    private String contentType;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime uploadedOn;

    @Transient
    private byte[] content;

    @PrePersist
    public void prePersist() {
        uploadedOn = LocalDateTime.now();
    }


    @SneakyThrows
    public static File of(MultipartFile multipartFile) {
        File file = new File();

        file.setFileName(getFileName(multipartFile.getOriginalFilename()));
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
