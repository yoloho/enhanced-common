package com.yoloho.common.util.aes;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoloho.common.util.StringUtil;

public class AESUtil {
    private final static Logger logger = LoggerFactory.getLogger(AESUtil.class.getSimpleName());

    /**
     * 加密
     * 
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static byte[] encrypt(byte[] content, byte[] password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password);
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(content);
        } catch (Exception e) {
            logger.warn("aes 加密出错", e);
        }
        return null;
    }
    
    /**
     * @param content
     * @param password
     * @return
     */
    public static String encrypt(String content, String password) {
        return StringUtil.toHex(encrypt(content.getBytes(), password.getBytes()));
    }
    
    /**
     * @param content
     * @param password
     * @return
     */
    public static String decrypt(String content, String password) {
        byte[] bytes = decrypt(StringUtil.toBytes(content), password.getBytes());
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    /**
     * 解密
     * 
     * @param content
     *            待解密内容
     * @param password
     *            解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] content, byte[] password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(password);
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(content);
        } catch (Exception e) {
            logger.warn("aes 解密出错", e);
        }
        return null;
    }

}
