package com.yoloho.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.common.util.DigestUtil;
import com.yoloho.common.util.StringUtil;
import com.yoloho.common.util.DigestUtil.Algorithm;

/**
 * @author jason
 *
 */
public class DigestUtilTest {
    @Test
    public void performanceTest() {
        String raw = "239efdijbp9edfocivjh";
        for (int cycle = 0; cycle < 20; cycle ++) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < 100000; i ++) {
                DigestUtil.md5(raw);
            }
            System.out.println(System.currentTimeMillis() - begin);
        }
    }
    
    @Test
    public void genericTest() {
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", DigestUtil.md5("123456"));
        Assert.assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", DigestUtil.sha1("123456"));
        Assert.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", DigestUtil.sha256("123456"));
        Assert.assertEquals("0a989ebc4a77b56a6e2bb7b19d995d185ce44090c13e2984b7ecc6d446d4b61ea9991b76a4c2f04b1b4d244841449454", DigestUtil.sha384("123456"));
        Assert.assertEquals("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413", DigestUtil.sha512("123456"));
    }
    
    @Test
    public void bytesTest() {
        byte[] input = new byte[] {'1', '2', '3', '4', '5', '6'};
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", StringUtil.toHex(DigestUtil.digest(Algorithm.MD5, input)));
        Assert.assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA1, input)));
        Assert.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA256, input)));
        Assert.assertEquals("0a989ebc4a77b56a6e2bb7b19d995d185ce44090c13e2984b7ecc6d446d4b61ea9991b76a4c2f04b1b4d244841449454", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA384, input)));
        Assert.assertEquals("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA512, input)));
    }
    
    @Test
    public void fileTest() throws IOException {
        File file = File.createTempFile("hash", "tmp");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("123456");
        }
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", StringUtil.toHex(DigestUtil.digest(Algorithm.MD5, file)));
        Assert.assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA1, file)));
        Assert.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA256, file)));
        Assert.assertEquals("0a989ebc4a77b56a6e2bb7b19d995d185ce44090c13e2984b7ecc6d446d4b61ea9991b76a4c2f04b1b4d244841449454", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA384, file)));
        Assert.assertEquals("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA512, file)));
        file.delete();
    }
    
    @Test
    public void filenameTest() throws IOException {
        File file = File.createTempFile("hash", "tmp");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("123456");
        }
        String filename = file.getPath();
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", StringUtil.toHex(DigestUtil.digestFile(Algorithm.MD5, filename)));
        Assert.assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", StringUtil.toHex(DigestUtil.digestFile(Algorithm.SHA1, filename)));
        Assert.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", StringUtil.toHex(DigestUtil.digestFile(Algorithm.SHA256, filename)));
        Assert.assertEquals("0a989ebc4a77b56a6e2bb7b19d995d185ce44090c13e2984b7ecc6d446d4b61ea9991b76a4c2f04b1b4d244841449454", StringUtil.toHex(DigestUtil.digestFile(Algorithm.SHA384, filename)));
        Assert.assertEquals("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413", StringUtil.toHex(DigestUtil.digestFile(Algorithm.SHA512, filename)));
        file.delete();
    }
    
    @Test
    public void streamTest() throws IOException {
        File file = File.createTempFile("hash", "tmp");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("123456");
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", StringUtil.toHex(DigestUtil.digest(Algorithm.MD5, fileInputStream)));
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Assert.assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA1, fileInputStream)));
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Assert.assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA256, fileInputStream)));
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Assert.assertEquals("0a989ebc4a77b56a6e2bb7b19d995d185ce44090c13e2984b7ecc6d446d4b61ea9991b76a4c2f04b1b4d244841449454", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA384, fileInputStream)));
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Assert.assertEquals("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413", StringUtil.toHex(DigestUtil.digest(Algorithm.SHA512, fileInputStream)));
        }
        file.delete();
    }
}
