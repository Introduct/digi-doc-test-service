package com.ee.digi_doc.storage.local.util;

import com.ee.digi_doc.persistance.model.Container;
import com.ee.digi_doc.persistance.model.File;
import com.ee.digi_doc.persistance.model.SigningData;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class HexUtils {

    public String getFileHex(File file) {
        return getMd5Hex(StringUtils.join(file.getId(), file.getName()));
    }

    public String getSigningDataHex(SigningData signingData) {
        return getMd5Hex(StringUtils.join(signingData.getId(), signingData.getContainerName(), signingData.getDataToSignName()));
    }

    public String getContainerHex(Container container) {
        return getMd5Hex(StringUtils.join(container.getId(), container.getName()));
    }

    private String getMd5Hex(String valueToHex) {
        return DigestUtils.md5Hex(valueToHex);
    }

}
