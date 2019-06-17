package com.yoloho.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class.getSimpleName());
    private final static String resouceFilePrefixOld = "/USER_DIR";
    private final static String resouceFilePrefixNew = "${user_dir}";
    private final static Pattern filenamePattern = Pattern.compile("[0-9a-zA-Z.\\u4e00-\\u9fa5_\\-|\\[\\]=+()@!~`'\":;><,]+$");

	/**
	 * 获取文件的扩展名
	 * @param fileName
	 * @return
	 */
	public static String getExtension(String fileName) {
		return fileName.indexOf(".") != -1 ? fileName.substring(fileName.lastIndexOf(".") + 1) : null;
	}
	
	/**
	 * 获取路径中所指的文件名，如果没取到文件名，返回空字符串（不是null）
	 * @param filenameWithPath
	 * @return
	 */
	public static String getFilename(String filenameWithPath) {
	    Matcher matcher = filenamePattern.matcher(filenameWithPath);
	    if (matcher.find()) {
	        return matcher.group();
	    }
	    return "";
	}
	
	/**
	 * 根据当前线程获取默认的classloader，
	 * 因为暂时不明确应该放在哪里，这里暂时先私有
	 * @return
	 */
	private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = FileUtil.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
	
	/**
	 * 基于上述的道理暂先私有
	 * @param resourceUrl
	 * @param description
	 * @return
	 * @throws FileNotFoundException
	 */
	private static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        if (resourceUrl == null) {
            return null;
        }
        if (!"file".equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                    description + " cannot be resolved to absolute file path " +
                    "because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        }
        catch (URISyntaxException ex) {
            // Fallback for URLs that are not valid URIs (should hardly ever happen).
            return new File(resourceUrl.getFile());
        }
    }
	
	/**
	 * 转换url中的空格
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	private static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    /**
     * 转换url中的空格
     * @param location
     * @return
     * @throws URISyntaxException
     */
    private static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }
    
	public static File getFileFromVariousPlace(String path) {
	    if (StringUtils.isEmpty(path)) {
	        return null;
	    }
	    boolean loaded = false;
	    logger.info("开始重新定位资源 {}", path);
	    String filename = getFilename(path);
	    String pathReal = null;
	    String userDir = System.getProperty("user.dir");
	    String userHome = System.getProperty("user.home");
	    String tmpDir = System.getProperty("java.io.tmpdir"); //这个不清楚有无安全隐患
	    String catalinaBase = System.getProperty("catalina.base");
	    String[] candidateBases = new String[] {userDir, userHome, tmpDir, catalinaBase};
	    File file = null;
        if (path.startsWith(resouceFilePrefixOld)) {
            pathReal = path.substring(resouceFilePrefixOld.length());
        } else if (path.startsWith(resouceFilePrefixNew)) {
            pathReal = path.substring(resouceFilePrefixNew.length());
        } else {
            pathReal = path;
        }
        int cur = 0;
        while (cur < candidateBases.length) {
            if (!loaded) {
                try {
                    String baseDir = candidateBases[cur ++];
                    if (StringUtils.isEmpty(baseDir)) {
                        continue;
                    }
                    String fullpath = String.format("%s%s%s", baseDir, File.separatorChar, pathReal);
                    file = new File(fullpath);
                    if (file.exists()) {
                        logger.info("重新定位资源 {} 成功", fullpath);
                        loaded = true;
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }
        ClassLoader cl = getDefaultClassLoader();
        if (!loaded) {
            //try to load from classpath
            logger.info("重定位失败，尝试从classpath中加载资源: {}", pathReal);
            try {
                URL url = (cl != null ? cl.getResource(pathReal) : ClassLoader.getSystemResource(pathReal));
                if (url != null) {
                    file = getFile(url, filename);
                    if (file != null) {
                        loaded = true;
                        logger.info("尝试从classpath中加载资源成功: {}", pathReal);
                    }
                }
            } catch (Exception e) {
            }
        }
        if (!loaded) {
            try {
                logger.info("重定位失败，尝试从classpath中加载资源: {}", filename);
                URL url = (cl != null ? cl.getResource(filename) : ClassLoader.getSystemResource(filename));
                if (url != null) {
                    file = getFile(url, filename);
                    if (file != null) {
                        loaded = true;
                        logger.info("尝试从classpath中加载资源成功: {}", filename);
                    }
                }
            } catch (Exception e) {
            }
        }
        if (loaded) {
            return file;
        }
        return null;
	}

	/**
	 * 基于毫秒的唯一ID
	 * @return
	 */
	public static String uniqId() {
		return System.nanoTime() + "";
	}
}
