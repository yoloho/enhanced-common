package com.yoloho.common.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.yoloho.common.util.DownloadUtil;

public class DownloadUtilTest {
    @Test
    public void testDownloadAndVerify() {
        File file = null;
        try {
            file = File.createTempFile("testDownload", "tmp");
            assertTrue(DownloadUtil.downloadAndVerify("http://a.dayima.com/a.jpg", file, "a105bb2f9958f46de79063aab06ead10"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }
}
