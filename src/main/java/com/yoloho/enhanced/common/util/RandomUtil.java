package com.yoloho.enhanced.common.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

/**
 * 随机相关类
 * 
 * 增加随机字符串函数，并优化常量定义
 * 
 * @author jason
 *
 */
public class RandomUtil {
    /**
     * 获取随机长度的数字，不足加前导0
     * 
     * @param num
     * @return
     */
    public static String getRandomNumCode(int num) {
        return StringUtils.leftPad(String.valueOf(getRanDom(num)), num, '0');
    }

    /**
     * 获取指定位数的随机数
     * 
     * @param length
     * @return
     * @author wuzl
     * @deprecated 这命名有点不好
     * @see #getRandom
     */
    public static long getRanDom(int length) {
        return getRandom(length);
    }

    /**
     * 获取指定长度范围内的随机数字
     * 
     * @param length
     * @return
     */
    public static long getRandom(int length) {
        Random random = ThreadLocalRandom.current();
        return (random.nextLong() & 0x7fffffffffffffffL) % (long) Math.pow(10, length);
    }
    
    /**
     * 获取随机字符串a-zA-Z0-9
     * @param len
     * @return
     */
    public static String getRandomString(int len) {
        StringBuilder buffer = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < len; i ++) {
            int offset = random.nextInt(62);
            if (offset < 10) {
                buffer.append((char)('0' + offset));
            } else if (offset < 36) {
                buffer.append((char)('a' + offset - 10));
            } else {
                buffer.append((char)('A' + offset - 36));
            }
        }
        return buffer.toString();
    }
    
    /**
     * 获取随机字符串a-zA-Z0-9加特殊字符
     * ascii码33~126, 计93个字符
     * @param len
     * @return
     */
    public static String getRandomPrintable(int len) {
        StringBuilder buffer = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < len; i ++) {
            buffer.append((char)(33 + random.nextInt(93)));
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i ++) {
            System.out.println(getRandomNumCode(4));
        }
        for (int i = 0; i < 10; i ++) {
            System.out.println(getRandom(10));
        }
        for (int i = 0; i < 10; i ++) {
            System.out.println(getRandomString(64));
        }
        for (int i = 0; i < 10; i ++) {
            System.out.println(getRandomPrintable(32));
        }
    }
}
