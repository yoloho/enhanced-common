package com.yoloho.common.util;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.common.util.NumberUtil;

/**
 * @author jason
 *
 */
public class NumberUtilTest {
	@Test
	public void equalTest() {
	    Assert.assertTrue(NumberUtil.equal(0, 0));
	    Assert.assertTrue(NumberUtil.equal(1E-7, 0));
	    Assert.assertTrue(NumberUtil.equal(-1E-7, 0));
	    Assert.assertFalse(NumberUtil.equal(0.01, 0));
	    Assert.assertFalse(NumberUtil.equal(-0.01, 0));
	    Assert.assertFalse(NumberUtil.equal(0.000001, 0));
	    Assert.assertFalse(NumberUtil.equal(-0.000001, 0));
	    Assert.assertTrue(NumberUtil.equal(0.0000001, 0));
	    Assert.assertTrue(NumberUtil.equal(-0.0000001, 0));
	}
	
	@Test
	public void formatDoubleTest() {
	    Assert.assertEquals(NumberUtil.formatDouble(0.3223), "0.32");
	    Assert.assertEquals(NumberUtil.formatDouble(0), "0.00");
	    Assert.assertEquals(NumberUtil.formatDouble(12345), "12,345.00");
	    Assert.assertEquals(NumberUtil.formatDouble(12345.1), "12,345.10");
	    Assert.assertEquals(NumberUtil.formatDouble(12345.12345), "12,345.12");
	    Assert.assertEquals(NumberUtil.formatDouble(0.0000001), "0.00");
	    Assert.assertEquals(NumberUtil.formatDouble(0.0056789), "0.01");
	}
	
	@Test
	public void bytesToIpTest() {
	    Assert.assertEquals(NumberUtil.bytesToIp((byte)0xff, (byte)0x01, (byte)0x10, (byte)0xff), "255.1.16.255");
	    Assert.assertEquals(NumberUtil.bytesToIp(new Byte((byte) 0xff), new Byte((byte)0x01), new Byte((byte)0x10), new Byte((byte)0xff)), "255.1.16.255");
	    Assert.assertEquals(NumberUtil.bytesToIp(new Byte[]{(byte)0xff, (byte)0x01, (byte)0x10, (byte)0xff}), "255.1.16.255");
	}
	
	@Test
    public void bytesToLongTest() {
        Assert.assertEquals(NumberUtil.bytesToLong((byte)0xff, (byte)0x01, (byte)0x10, (byte)0xff), 0xff1001ffL);
        Assert.assertEquals(NumberUtil.bytesToLong(new Byte((byte) 0xff), new Byte((byte)0x01), new Byte((byte)0x10), new Byte((byte)0xff)), 0xff1001ffL);
        Assert.assertEquals(NumberUtil.bytesToLong(new Byte[]{(byte)0xff, (byte)0x01, (byte)0x10, (byte)0xff}), 0xff1001ffL);
    }
	
	@Test
    public void intToLongTest() {
	    Assert.assertEquals(NumberUtil.intToLong(1), 1L);
	    Assert.assertEquals(NumberUtil.intToLong(0), 0);
	    Assert.assertEquals(NumberUtil.intToLong(-1), 0xffffffffL);
        Assert.assertEquals(NumberUtil.intToLong(0xff1001ff), 0xff1001ffL);
    }
	
	/**
	 * 进制互转测试
	 */
	@Test
	public void radixConvertTest() {
	    for (int i = 2; i <= 62; i++) {
	        Assert.assertEquals(0L, NumberUtil.longFromStringWithRadix("0", i));
	        Assert.assertEquals(1L, NumberUtil.longFromStringWithRadix("1", i));
	        Assert.assertEquals("0", NumberUtil.stringFromLongWithRadix(0, i));
	        Assert.assertEquals("1", NumberUtil.stringFromLongWithRadix(1, i));
        }
	    Assert.assertEquals(2L, NumberUtil.longFromStringWithRadix("10", 2));
	    Assert.assertEquals("10", NumberUtil.stringFromLongWithRadix(2, 2));
	    Assert.assertEquals(255L, NumberUtil.longFromStringWithRadix("FF", 16));
	    Assert.assertEquals("FF", NumberUtil.stringFromLongWithRadix(255, 16));
	    Assert.assertEquals(0L, NumberUtil.longFromStringWithRadix("FF", 15));
	    Assert.assertEquals(0L, NumberUtil.longFromStringWithRadix(" 98w6st9789ypiu", 3));
	    Assert.assertEquals("100", NumberUtil.stringFromLongWithRadix(256, 16));
	    Assert.assertEquals(NumberUtil.stringFromLongB62(256), NumberUtil.stringFromLongWithRadix(256, 62));
	    Assert.assertEquals(NumberUtil.longFromStringB62("FF"), NumberUtil.longFromStringWithRadix("FF", 62));
	}
	
	@Test
	public void toListTest() {
	    Assert.assertArrayEquals(new Integer[]{1, 2, 3, 0}, NumberUtil.toList("1,2,3,,,,,, ,ddd", Integer.class).toArray());
	    try {
	        Assert.assertArrayEquals(new Integer[]{1, 2, 3, 0}, NumberUtil.toList("1,2,3,,,,,, ,ddd", Long.class).toArray());
	    } catch (AssertionError e) {
	        Assert.assertTrue(true);
        }
	    Assert.assertArrayEquals(new Long[]{1L, 2L, 3L, 0L}, NumberUtil.toList("1,2,3,,,,,, ,ddd", Long.class).toArray());
	}
	
}
