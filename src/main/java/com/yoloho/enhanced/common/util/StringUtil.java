package com.yoloho.enhanced.common.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 字符串工具类
 * 
 * @author jason
 *
 */
public class StringUtil {
    private final static Logger logger = LoggerFactory.getLogger(StringUtil.class.getSimpleName());
    
    final static Pattern patternUrl = Pattern.compile(
            "^[a-zA-Z]+:\\/\\/[a-zA-Z0-9-_.]+[\\/]?(\\/[a-zA-Z0-9-_.#?%=&]+?)*$",
            Pattern.CASE_INSENSITIVE);
    final static Pattern patternEmail = Pattern.compile("^[a-zA-Z0-9\\-_\\.]+@[a-zA-Z0-9\\-_\\.]+\\.[a-zA-Z0-9]+$");

    /**
     * 验证链接合法性，true为合法
     * 
     * @param str
     * @return
     */
    public static boolean validateURL(String str) {
        return patternUrl.matcher(str).matches();
    }
    
    /**
     * 验证邮件合法性，返回true为合法
     * 
     * @param str
     * @return
     */
    public static boolean validateEmail(String str) {
        return patternEmail.matcher(str).matches();
    }

    public static long strToIpLong(String ip) {
        Iterable<String> itb = JoinerSplitters.getSplitter(".").split(ip);
        Iterator<String> it = itb.iterator();
        List<Byte> arr = Lists.newArrayList();
        while (it.hasNext()) {
            arr.add(0, (byte) (Integer.valueOf(it.next()) & 0xff));
        }
        return NumberUtil.bytesToLong(arr.toArray(new Byte[] {}));
    }
    
    public static String urlEncode(String param) {
        return urlEncode(param, "UTF-8");
    }

    public static String urlDecode(String param) {
        return urlDecode(param, "UTF-8");
    }
    
    public static String urlEncode(String param, String encoding) {
        if (param != null) {
            try {
                return URLEncoder.encode(param, encoding);
            } catch (Exception ex) {
                return param;
            }
        }
        return "";
    }

    public static String urlDecode(String param, String encoding) {
        if (param != null) {
            try {
                return URLDecoder.decode(param, encoding);
            } catch (Exception ex) {
                return param;
            }
        }
        return "";
    }

    /**
     * 将数组以指定字符串分隔拼为字符串 跳过skips中指定的字符串
     * 
     * @param vals
     * @param split
     * @param skips
     * @return
     */
    public static String toString(String[] vals, String split, String skip) {
        if (vals == null) {
            return null;
        }
        if (vals.length == 1) {
            if (vals[0].equals(skip)) {
                return null;
            }
            return vals[0];
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (skip != null && skip.equals(vals[i])) {
                continue;
            }
            if (buffer.length() > 0) {
                buffer.append(split);
            }
            buffer.append(vals[i]);
        }
        return buffer.toString();
    }

    /**
     * 将数组以指定字符串分隔拼为字符串
     * 
     * @param vals
     * @param split
     * @return
     */
    public static String toString(String[] vals, String split) {
        return toString(vals, split, null);
    }

    /**
     * 将数组拼为字符串，逗号分隔
     * 
     * @param vals
     * @return
     */
    public static String toString(String[] vals) {
        return toString(vals, ",");
    }

    /**
     * 驼峰转下划线
     * 
     * @param param
     * @return
     */
    public static String toUnderline(String str) {
        if (str == null || str.trim().length() == 0) {
            return "";
        }
        str = str.trim();
        int len = str.length();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (i > 0) {
                    buffer.append('_');
                }
                buffer.append((char)(ch + 32));
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * 下划线写法转驼峰
     * 
     * @param param
     * @return
     */
    public static String toCamel(String str) {
        if (str == null || str.trim().length() == 0) {
            return "";
        }
        str = str.trim();
        int len = str.length();
        StringBuilder buffer = new StringBuilder();
        boolean found = false;
        boolean isFirst = true;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == '_') {
                found = true;
            } else {
                if (!isFirst) {
                    if (found && ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
                        found = false;
                        if (ch >= 'a' && ch <= 'z') {
                            buffer.append((char)(ch - 32));
                            continue;
                        }
                    }
                } else {
                    isFirst = false;
                    if (ch >= 'A' && ch <= 'Z') {
                        buffer.append((char)(ch + 32));
                        found = false;
                        continue;
                    }
                }
                found = false;
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
    
    private static char getSingleHexChar(int n) {
        if (n >= 0 && n < 10) {
            return (char) ('0' + n);
        } else if (n >= 10 && n < 36) {
            return (char) ('a' + n - 10);
        } else {
            return ' ';
        }
    }
    
    /**
     * 转十六进制小写字符串
     * 
     * @param src
     * @return
     */
    public static String toHex(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(src.length * 2);
        for (int i = 0; i < src.length; i++) {
            stringBuilder.append(getSingleHexChar((src[i] & 0xF0) >> 4))
                .append(getSingleHexChar(src[i] & 0x0F));
        }
        return stringBuilder.toString();
    }
    
    /**
     * 十六进制字符串转字节数组(忽略大小写)
     * 
     * @param hexString
     * @return
     */
    public static byte[] toBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        int length = hexString.length();
        byte[] buffer = new byte[length / 2 + (length % 2 == 0 ? 0 : 1)];
        char ch1, ch2;
        int val1, val2;
        int cur = 0;
        for (int i = (length % 2 == 0 ? 0 : -1); i < length; i += 2) {
            ch1 = (i < 0 ? (char) '0' : hexString.charAt(i));
            ch2 = hexString.charAt(i + 1);
            val1 = val2 = -1;
            if (ch1 >= '0' && ch1 <= '9') {
                val1 = ch1 - '0';
            }
            if (ch1 >= 'A' && ch1 <= 'F') {
                val1 = ch1 - 'A' + 10;
            }
            if (ch1 >= 'a' && ch1 <= 'f') {
                val1 = ch1 - 'a' + 10;
            }
            if (ch2 >= '0' && ch2 <= '9') {
                val2 = ch2 - '0';
            }
            if (ch2 >= 'A' && ch2 <= 'F') {
                val2 = ch2 - 'A' + 10;
            }
            if (ch2 >= 'a' && ch2 <= 'f') {
                val2 = ch2 - 'a' + 10;
            }
            if (val1 >= 0 && val2 >= 0) {
                buffer[cur ++] = ((byte) ((val1) << 4 | val2));
            } else {
                logger.error("meet unexcepted char in string: {}", hexString);
                continue;
            }
        }
        return buffer;
    }
}
