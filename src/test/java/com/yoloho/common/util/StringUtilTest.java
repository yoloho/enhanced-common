package com.yoloho.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.common.util.StringUtil;

/**
 * @author jason
 *
 */
public class StringUtilTest {
    @Test
    public void toHexPerformanceTest() {
        byte[] raw = "239efdijbp9e".getBytes();
        for (int cycle = 0; cycle < 20; cycle ++) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < 100000; i ++) {
                StringUtil.toHex(raw);
            }
            System.out.println(System.currentTimeMillis() - begin);
        }
    }
    
	@Test
	public void validateURLTest() {
	    assertTrue(StringUtil.validateURL("http://a.dayima.com"));
	    assertTrue(StringUtil.validateURL("http://a.dayima.com/"));
	    assertTrue(StringUtil.validateURL("https://a.dayima.com/a/a/b/d/d-a"));
	    assertTrue(StringUtil.validateURL("https://a.dayima.com/a/a/b/d/d-a/a?topicId=33333&new=1"));
	    assertTrue(StringUtil.validateURL("https://a.dayima.com/a/a/b/d/d-a/a?topicId=33333&new=1#32322"));
	    assertTrue(StringUtil.validateURL("https://a.dayima.com/a/a/b/d/d-a/a?topicId=33333&c=%2f&new=1#32322"));
	    assertTrue(StringUtil.validateURL("dayima://topic/a/a/b/d/d-a/a"));
	    assertTrue(StringUtil.validateURL("dayima://topic/a/a/b/d/d-a/a?topicId=33333&new=1"));
	    assertTrue(StringUtil.validateURL("dayima://topic/a/a/b/d/d-a/a?topicId=33333&new=1&c=%2F"));
	    assertTrue(StringUtil.validateURL("ftp://a.dayima.com"));
	    assertFalse(StringUtil.validateURL("http3://a.dayima.com/"));
	    assertTrue(StringUtil.validateURL("https://a/a/a/b/d/d-a"));
	    assertFalse(StringUtil.validateURL("https:/a.dayima.com/a/a/b/d/d-a/a?topicId=33333&new=1"));
        assertFalse(StringUtil.validateURL("://a.dayima.com/a/a/b/d/d-a/a?topicId=33333&new=1#32322"));
        assertFalse(StringUtil.validateURL("dayima:/topic/a/a/b/d/d-a/a"));
        assertFalse(StringUtil.validateURL("dayima//topic/a/a/b/d/d-a/a?topicId=33333&new=1"));
	}
	
	@Test
	public void validateEmailTest() {
	    assertFalse(StringUtil.validateEmail(""));
	    assertFalse(StringUtil.validateEmail("test"));
	    assertFalse(StringUtil.validateEmail("test.com"));
	    assertFalse(StringUtil.validateEmail("www.test.com"));
	    assertTrue(StringUtil.validateEmail("test@test.com"));
	    assertTrue(StringUtil.validateEmail("test@www.test.com"));
	    assertTrue(StringUtil.validateEmail("test901@www.test.com"));
	    assertTrue(StringUtil.validateEmail("test901.god@www.test.com"));
	    assertTrue(StringUtil.validateEmail("test901.god-top@www.test.com"));
	    assertTrue(StringUtil.validateEmail("test901.god-top@www.test-first.com"));
	    
	}
	
	@Test
	public void strToIpLongTest() {
	    Assert.assertEquals(StringUtil.strToIpLong("127.0.0.1"), 0x7f000001L);
	    Assert.assertEquals(StringUtil.strToIpLong("255.255.255.255"), 0xffffffffL);
	    Assert.assertEquals(StringUtil.strToIpLong("0.255.255.255"), 0x00ffffffL);
	}
	
	@Test
	public void urlEncodeDecodeTest() {
	    Assert.assertEquals("", StringUtil.urlDecode(""));
	    Assert.assertEquals(" 0的", StringUtil.urlDecode("+%30%E7%9A%84"));
	    Assert.assertEquals("", StringUtil.urlEncode(""));
	    Assert.assertEquals("+0%E7%9A%84", StringUtil.urlEncode(" 0的"));
	}
	
	@Test
	public void toStringTest() {
	    Assert.assertEquals(null, StringUtil.toString(null, ",", ""));
	    Assert.assertEquals("", StringUtil.toString(new String[] {}, ",", ""));
        Assert.assertEquals("a,b,d,c", StringUtil.toString(new String[] { "a", "b", "d", "c" }, ",", ""));
        Assert.assertEquals("a,d,c", StringUtil.toString(new String[] { "a", "b", "d", "c" }, ",", "b"));
	}
	
	@Test
	public void toUnderlineTest() {
	    Assert.assertEquals("", StringUtil.toUnderline(""));
	    Assert.assertEquals("", StringUtil.toUnderline(" "));
	    Assert.assertEquals("_", StringUtil.toUnderline(" _"));
	    Assert.assertEquals("a_apple_banana_b", StringUtil.toUnderline(" aAppleBanana_b "));
	    Assert.assertEquals("a_apple_banana_b_", StringUtil.toUnderline("aAppleBanana_b_"));
	    Assert.assertEquals("_a_apple_banana_b_", StringUtil.toUnderline("_aAppleBanana_b_"));
	    Assert.assertEquals("apple_banana", StringUtil.toUnderline("AppleBanana"));
	    Assert.assertEquals("___a_apple_____banana____b____", StringUtil.toUnderline("__AApple____Banana____b____"));
	}
	
	@Test
	public void toCamelTest() {
	    Assert.assertEquals("", StringUtil.toCamel(""));
        Assert.assertEquals("", StringUtil.toCamel(" "));
        Assert.assertEquals("", StringUtil.toCamel(" _"));
        Assert.assertEquals("aBCD", StringUtil.toCamel(" _a_b_c_d"));
        Assert.assertEquals("appleBanana", StringUtil.toCamel("AppleBanana"));
        Assert.assertEquals("appleBanana", StringUtil.toCamel("apple_banana"));
        Assert.assertEquals("appleBanana", StringUtil.toCamel("_apple_banana"));
        Assert.assertEquals("appleBanana", StringUtil.toCamel("apple_banana"));
        Assert.assertEquals("appleBanana", StringUtil.toCamel("_Apple_Banana"));
	}
	
	@Test
	public void toHex() {
	    Assert.assertNull(StringUtil.toHex(null));
	    Assert.assertNull(StringUtil.toHex(new byte[] {}));
	    Assert.assertEquals("0123456789abcdef", StringUtil.toHex(new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}));
	    Assert.assertNull(StringUtil.toBytes(null));
        Assert.assertNull(StringUtil.toBytes(""));
        Assert.assertArrayEquals(new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, StringUtil.toBytes("0123456789abcdef"));
        Assert.assertArrayEquals(new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, StringUtil.toBytes("0123456789ABCdef"));
        Assert.assertArrayEquals(new byte[] {0x01, 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, StringUtil.toBytes("10123456789ABCdef"));
	}
	
}
