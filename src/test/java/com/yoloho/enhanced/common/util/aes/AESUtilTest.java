package com.yoloho.enhanced.common.util.aes;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.common.util.aes.AESUtil;

public class AESUtilTest {
    @Test
    public void encryptTest() {
        Assert.assertEquals("f06a6dc21b8be9d31cab476251eb375630c731455cbe771f9b5de7877cd755d96ac66d05296556d531003633b8eb41d3", AESUtil.encrypt("ts=123123123123&uid=234234324234", "pass"));
        Assert.assertEquals("ts=123123123123&uid=234234324234", AESUtil.decrypt("f06a6dc21b8be9d31cab476251eb375630c731455cbe771f9b5de7877cd755d96ac66d05296556d531003633b8eb41d3", "pass"));
    }
}
