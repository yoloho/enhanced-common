package com.yoloho.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DigestUtil {
    private final static Logger logger = LoggerFactory.getLogger(DigestUtil.class.getSimpleName());
    public static enum Algorithm {
        SHA1("SHA-1"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512"), MD5("MD5");
        
        String name;
        
        private Algorithm(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private static final LoadingCache<Algorithm, ThreadLocal<MessageDigest>> digestCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Algorithm, ThreadLocal<MessageDigest>>(){

                @Override
                public ThreadLocal<MessageDigest> load(final Algorithm algorithm) throws Exception {
                    return new ThreadLocal<MessageDigest>() {
                        protected MessageDigest initialValue() {
                            try {
                                return MessageDigest.getInstance(algorithm.getName());
                            } catch (NoSuchAlgorithmException e) {
                                logger.error("Digest algorithm connot be found: {}", algorithm);
                            }
                            return null;
                        }
                    };
                }
                
            });
    
    private static MessageDigest getMessageDigest(Algorithm algorithm) {
        try {
            return digestCache.get(algorithm).get();
        } catch (ExecutionException e) {
        }
        return null;
    }
    
    /**
     * 对指定字节数组做指定算法的hash，返回bytes，如需要转换成十六进制字符串，@see {@link StringUtil#toHex(byte[])}
     * 错误情况下返回null
     * 
     * @param algorithm
     * @param input
     * @return
     */
    public static byte[] digest(Algorithm algorithm, byte[] input) {
        MessageDigest instance = getMessageDigest(algorithm);
        if (instance == null) {
            return null;
        }
        instance.reset();
        instance.update(input);
        return instance.digest();
    }
    
    /**
     * 对指定字符串做指定算法的hash，返回bytes，如需要转换成十六进制字符串，@see {@link StringUtil#toHex(byte[])}
     * 错误情况下返回null
     * 
     * @param algorithm
     * @param s
     * @return
     */
    public static byte[] digest(Algorithm algorithm, String s) {
        return digest(algorithm, s.getBytes());
    }

    /**
     * 对指定流做指定算法的hash，返回bytes，如需要转换成十六进制字符串，@see {@link StringUtil#toHex(byte[])}
     * 错误情况下返回null
     * 
     * @param algorithm
     * @param inputStream
     * @return
     */
    public static byte[] digest(Algorithm algorithm, InputStream inputStream) {
        byte[] data = new byte[256];
        int len = 0;
        MessageDigest instance = getMessageDigest(algorithm);
        if (instance == null) {
            return null;
        }
        instance.reset();
        try {
            while (true) {
                len = inputStream.read(data);
                if (len > 0) {
                    instance.update(data, 0, len);
                } else {
                    break;
                }
            };
            return instance.digest();
        } catch (IOException e) {
            logger.error("error occured when reading", e);
        }
        return null;
    }
    
    /**
     * 对指定文件名做指定算法的hash，返回bytes，如需要转换成十六进制字符串，@see {@link StringUtil#toHex(byte[])}
     * 错误情况下返回null
     * 
     * @param algorithm
     * @param filename
     * @return
     */
    public static byte[] digestFile(Algorithm algorithm, String filename) {
        return digest(algorithm, new File(filename));
    }
    
    /**
     * 对指定文件对象做指定算法的hash，返回bytes，如需要转换成十六进制字符串，@see {@link StringUtil#toHex(byte[])}
     * 错误情况下返回null
     * 
     * @param algorithm
     * @param file
     * @return
     */
    public static byte[] digest(Algorithm algorithm, File file) {
        try {
            return digest(algorithm, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            logger.error("file not found: {}", file, e);
        }
        return null;
    }
    
    /**
     * 对指定字符串做hash，返回小写的hash串
     * 错误情况下返回null
     * 
     * @param s
     * @return
     */
    public static String md5(String s) {
        return StringUtil.toHex(digest(Algorithm.MD5, s));
    }
    
    /**
     * 对指定字符串做hash，返回小写的hash串
     * 错误情况下返回null
     * 
     * @param s
     * @return
     */
    public static String sha1(String s) {
        return StringUtil.toHex(digest(Algorithm.SHA1, s));
    }
    
    /**
     * 对指定字符串做hash，返回小写的hash串
     * 错误情况下返回null
     * 
     * @param s
     * @return
     */
    public static String sha256(String s) {
        return StringUtil.toHex(digest(Algorithm.SHA256, s));
    }
    
    /**
     * 对指定字符串做hash，返回小写的hash串
     * 错误情况下返回null
     * 
     * @param s
     * @return
     */
    public static String sha384(String s) {
        return StringUtil.toHex(digest(Algorithm.SHA384, s));
    }
    
    /**
     * 对指定字符串做hash，返回小写的hash串
     * 错误情况下返回null
     * 
     * @param s
     * @return
     */
    public static String sha512(String s) {
        return StringUtil.toHex(digest(Algorithm.SHA512, s));
    }
}
