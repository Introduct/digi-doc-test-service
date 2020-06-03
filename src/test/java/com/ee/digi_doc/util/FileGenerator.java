package com.ee.digi_doc.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotEmpty;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FileGenerator {

    private static BufferedImage generate(int width, int height, int pixSize) {
        int x, y;

        var bi = new BufferedImage(pixSize * width, pixSize * height, BufferedImage.TYPE_3BYTE_BGR);
        var g = (Graphics2D) bi.getGraphics();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                x = i * pixSize;
                y = j * pixSize;

                if ((i * j) % 6 == 0) {
                    g.setColor(Color.GRAY);
                } else if ((i + j) % 5 == 0) {
                    g.setColor(Color.BLUE);
                } else {
                    g.setColor(Color.WHITE);
                }

                g.fillRect(y, x, pixSize, pixSize);
            }
        }

        g.dispose();
        return bi;
    }

    @SneakyThrows
    public static MockMultipartFile randomMultipartJpeg(@NotEmpty String name) {
        var image = generate(500, 500, 5);

        var os = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", os);
        var is = new ByteArrayInputStream(os.toByteArray());

        String filename = StringUtils.join(new String[]{name, "jpg"}, ".");

        return new MockMultipartFile("file", filename, "image/jpeg", is);
    }

    public static MockMultipartFile randomMultipartJpeg() {
        return randomMultipartJpeg(RandomStringUtils.randomAlphabetic(10));
    }

}
