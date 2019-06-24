package com.yoloho.enhanced.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoloho.enhanced.common.util.DigestUtil;
import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.common.util.DigestUtil.Algorithm;

/**
 * 去掉spring-retry依赖，带重试的下载文件到本地工具
 * 
 * @author jason
 *
 */
public class DownloadUtil {
    private static final Logger logger = LoggerFactory.getLogger(DownloadUtil.class.getSimpleName());
    private static final int RETRY_TIMES = 10;
    private static final int RETRY_INTERVAL = 2000;
    
    private static <T> T doWithRetry(Callable<T> callable) throws Exception {
        int retryCount = 0;
        Exception lastException = null;
        while (retryCount < RETRY_TIMES) {
            try {
                return callable.call();
            } catch (Exception e) {
                retryCount ++;
                lastException = e;
                logger.warn("下载文件失败，重试第{}次", retryCount);
                Thread.sleep(RETRY_INTERVAL);
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new RuntimeException();
    }
    
    /**
     * 下载一个文件到本地
     * 
     * @param url
     * @param localFile
     * @return
     */
    public static boolean download(final String url, final File localFile) {
        try {
            return doWithRetry(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    URLConnection conn = new URL(url).openConnection();
                    conn.setReadTimeout(60 * 1000);
                    conn.setConnectTimeout(10 * 1000);
                    try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), 1024 * 100)) {
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile))) {
                            int cnt = 0;
                            byte[] buf = new byte[512];
                            do {
                                cnt = bis.read(buf);
                                if (cnt > 0) {
                                    bos.write(buf, 0, cnt);
                                }
                            } while (cnt > 0);
                        }
                    } catch (MalformedURLException e) {
                        logger.warn("文件地址错误", e);
                        return false;
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            logger.warn("经过重试后，依然错误", e);
            return false;
        }
    }
    
    /**
     * 下载并校验
     * 
     * @param url
     * @param localFile
     * @param hash
     * @return
     */
    public static boolean downloadAndVerify(String url, File localFile, String hash) {
        boolean succ = download(url, localFile);
        if (!succ) {
            return false;
        }
        //verify file
        String fileHash = StringUtil.toHex(DigestUtil.digest(Algorithm.MD5, localFile));
        if (!fileHash.equalsIgnoreCase(hash)) {
            logger.warn("file hash error, ignore running: file -> {} with hash {}, which should be {}", url,
                    fileHash, hash);
            return false;
        }
        return true;
    }
}
