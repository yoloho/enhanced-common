package com.yoloho.enhanced.common.util;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class NumberUtil {
    
    /**
     * 将给出的字符串以英文逗号分隔为一个数字数组
     * 
     * @param str
     * @param cls
     *          目标转换类型
     * @return
     */
    public static <T extends Number> List<T> toList(String str, Class<T> cls) {
        return toList(str, ",", cls);
    }
    
    /**
     * 将给出的字符串以给定的分隔为一个数字数组
     * 
     * @param str
     * @param sep
     * @param cls
     *          目标转换类型
     * @return
     */
    public static <T extends Number> List<T> toList(String str, String sep, Class<T> cls) {
        return toList(JoinerSplitters.getSplitter(sep).splitToList(str), cls);
    }
    
    /**
     * 将给出的字符串数字转换为一个数字数组
     * 
     * @param arr
     * @param cls
     *          目标转换类型
     * @return
     */
    public static <T extends Number> List<T> toList(List<String> arr, final Class<T> cls) {
        return Lists.newArrayList(Lists.transform(arr, new Function<String, T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T apply(String input) {
                if (cls.isAssignableFrom(Integer.class)) {
                    return (T) (Integer) NumberUtils.toInt(input, 0);
                } else if (cls.isAssignableFrom(Long.class)) {
                    return (T) (Long) NumberUtils.toLong(input, 0);
                }
                throw new RuntimeException("类型不支持");
            }
        }));
    }
    
    /**
     * 带一定精度允差的双精比较
     * @param a
     * @param b
     * @return
     */
    public static boolean equal(double a, double b) {
        double diff = a - b;
        return diff < 1E-6 && diff > -1E-6;
    }
    
    static ThreadLocal<DecimalFormat> numberFormat = new ThreadLocal<DecimalFormat>() {
        @Override
        public DecimalFormat initialValue() {
            return new DecimalFormat("###,##0.00");
        }
    };

    /**
     * 小数点后保留两位
     * 
     * @param d
     * @return
     */
    public static String formatDouble(double d) {
        DecimalFormat format = numberFormat.get();
        return format.format(d);
    }
    
    /**
     * 参数须为4元组（网络序） 转换byte[]至ip地址
     * 
     * @param arr
     * @return
     */
    public static String bytesToIp(Byte... arr) {
        if (arr == null || arr.length != 4) {
            throw new RuntimeException("bytes[] should be less than 8 elements");
        }
        StringBuilder buffer = new StringBuilder(15);
        for (int i = 0; i < arr.length; i++) {
            if (buffer.length() > 0) {
                buffer.append(".");
            }
            buffer.append(arr[i] & 0xff);
        }
        return buffer.toString();
    }
    
    /**
     * 参数须为8元组以内(小于等于8个字节)且为小端格式
     * @param arr
     * @return
     */
    public static long bytesToLong(Byte[] arr) {
        if (arr == null || arr.length > 8) {
            throw new RuntimeException("bytes[] should be less than 8 elements");
        }
        long result = 0;
        for (int i = 0; i < arr.length && i < 8; i ++) {
            result |= (((long)(arr[i] & 0xff)) << (i * 8));
        }
        return result;
    }

    /**
     * 参数须为8元组以内(小于等于8个字节)且为小端格式
     * @param arr
     * @return
     */
    public static long bytesToLong(byte... arr) {
        if (arr == null || arr.length > 8) {
            throw new RuntimeException("bytes[] should be less than 8 elements");
        }
        long result = 0;
        for (int i = 0; i < arr.length && i < 8; i ++) {
            result |= ((long)(arr[i] & 0xff) << (i * 8));
        }
        return result;
    }

    /**
     * unsigned int 转 unsigned long 时，不丢失符号位（由于java没有unsigned）
     * @param i
     * @return
     */
    public static long intToLong(int i) {
        long l = i & 0x7fffffffL;
        if (i < 0) {
            l |= 0x080000000L;
        }
        return l;
    }
    
    /**
     * 以指定进制，转换为long
     * <p>
     * 进制规则：0-9A-Za-z = 0~61
     * 
     * @param str
     * @return
     */
    public static long longFromStringWithRadix(String str, final int radix) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        if (radix > 62 || radix < 2) {
            return 0;
        }
        long id = 0;
        int len = str.length();
        //数字位数代表的级数
        long base = 0;
        char ch;
        long t = 0;
        for (int i = 0; i < len; i++) {
            ch = str.charAt(i);
            base = (long)Math.pow(radix, len - i - 1);
            if (ch >= '0' && ch <= '9') {
                t = ch - '0';
            } else if (ch >= 'A' && ch <= 'Z') {
                t = ch - 'A' + 10;
            } else if (ch >= 'a' && ch <= 'z') {
                t = ch - 'a' + 36;
            } else {
                //illegal char
                return 0;
            }
            if (t >= radix) {
                //illegal char
                return 0;
            }
            id += t * base;
        }
        return id;
    }
    
    private static char getSingleCharWithRadix(long n, int radix) {
        if (n >= radix || radix > 62 || radix < 2) {
            return '_';
        }
        if (n >= 0 && n < 10) {
            return (char) ('0' + n);
        } else if (n >= 10 && n < 36) {
            return (char) ('A' + n - 10);
        } else {
            return (char) ('a' + n - 36);
        }
    }
    
    /**
     * 转换为指定进制的字符串形式，2~62进制
     * <p>
     * 进制规则：0-9A-Za-z = 0~61
     * 
     * @param n
     *          待转换数，正整数
     * @return
     */
    public static String stringFromLongWithRadix(long n, int radix) {
        if (n < 0) {
            return "";
        }
        if (n == 0) {
            return "0";
        }
        StringBuilder buffer = new StringBuilder();
        while (n > 0) {
            buffer.append(getSingleCharWithRadix(n % radix, radix));
            n /= radix;
        }
        return buffer.reverse().toString();
    }
    
    /**
     * 62进制下数字转字符串
     * 
     * @param n
     * @return
     */
    public static String stringFromLongB62(long n) {
        return stringFromLongWithRadix(n, 62);
    }
    
    /**
     * 62进制字符串转long
     * 
     * @param str
     * @return
     */
    public static long longFromStringB62(String str) {
        return longFromStringWithRadix(str, 62);
    }
    
    /**
     * 格式化数字，左补0
     * @param n
     * @param maxLength
     * @return
     */
    public static String formatLongByPrefix(long n, int maxLength) {
    	String num = String.valueOf(n);
    	if(num.length() > maxLength) {
    		throw new RuntimeException("格式化数字出错，数值长度越界：" + num);
    	}else {
    		StringBuffer strbNum = new StringBuffer();
    		for(int i=0; i<maxLength-num.length(); i++) {
    			strbNum.append("0");
    		}
    		return strbNum.append(num).toString();
    	}
    }

    public static void main(String[] args) {
    	System.out.println(formatLongByPrefix(500, 2));
    }
    
}