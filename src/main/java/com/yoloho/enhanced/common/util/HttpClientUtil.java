package com.yoloho.enhanced.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.yoloho.enhanced.common.annotation.NonNull;

/**
 * http访问的工具类
 *
 * @author wuzl
 * @date Feb 28, 2014 9:43:40 AM
 * @comment
 */
public class HttpClientUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class.getSimpleName());
    private static String customUserAgent = "Apache-HttpClient/4.5.5 (Java/1.7.0_79)";
    /**
     * 60秒内连接可垂用
     */
    private static ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
        private DefaultConnectionKeepAliveStrategy defaultConnectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy();
        
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long defaultVal = defaultConnectionKeepAliveStrategy.getKeepAliveDuration(response, context);
            if (defaultVal < 0) {
                return 6000;
            }
            return defaultVal;
        }
    };
    private static RequestConfig REQUEST_CONFIG;
    private static HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
        
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= 2) {
                //最多只重试一次
                log.warn("Http请求重试次数达到限制，但仍有问题");
                return false;
            }
            //只针对NoHttpResponse重试
            if (exception instanceof NoHttpResponseException) {
//                log.info("针对半连接做重试");
                return true;
            }
            return false;
        }
    };

    static {
        Builder builder = RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(5000).setConnectTimeout(5000)
                //忽略cookie
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).setConnectionRequestTimeout(10000);
        try {
            if ( builder.getClass().getMethod("setContentCompressionEnabled", boolean.class) != null ) {
                builder.setContentCompressionEnabled(true);
            }
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        REQUEST_CONFIG = builder.build();
    }
    
    public static void setCustomUserAgent(String customUserAgent) {
        HttpClientUtil.customUserAgent = customUserAgent;
    }

    /**
     * 对k=v&k1=v1形式的字符串进行切割，可指定两个分隔符
     * 
     * @param queryString
     * @param segmentSeparator
     * @param keyValueSeparator
     * @return
     */
    @NonNull
    public static Map<String, String> splitKeyValuePairString(String queryString, String segmentSeparator, String keyValueSeparator) {
        Map<String, String> map = Maps.newHashMap();
        if (StringUtils.isEmpty(queryString)) {
            return map;
        }
        List<String> list = JoinerSplitters.getSplitter(segmentSeparator).splitToList(queryString);
        if ( list != null && list.size() > 0 ) {
            for (String str : list) {
                List<String> arr = JoinerSplitters.getSplitter(keyValueSeparator).splitToList(str);
                if ( arr != null && arr.size() == 2 ) {
                    if ( !map.containsKey(arr.get(0)) && StringUtils.isNotEmpty(arr.get(0)) && StringUtils.isNotEmpty(arr.get(1)) ) {
                        map.put(arr.get(0), arr.get(1));
                    }
                }
            }
        }
        return map;
    }
    
    /**
     * 对k=v&k1=v1形式的字符串进行切割
     * 
     * @param queryString
     * @return
     */
    @NonNull
    public static Map<String, String> splitKeyValuePairString(String queryString) {
        return splitKeyValuePairString(queryString, "&", "=");
    }

    /**
     * 多次访问页面
     *
     * @param url
     * @param visitCout
     * @return
     * @date Feb 28, 2014 9:47:04 AM
     * @author wuzl
     * @comment如果visitcount 小于0一直访问知道1w次
     */
    public static void visitPageByCount(String url, int visitCout) {
        try {
            /* 1.访问次数 */
            int visitNum = 0;
            /* 2.循环访问 如果visitCout小于0一直访问 */
            while ( visitCout < 0 || visitNum++ < visitCout ) {
                getRequest(url);
                if ( visitNum >= 10000 ) {
                    break;
                }
            }
        } catch (Exception e) {
//            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getRequest(String url) {
        return getRequest(url, null);
    }

    public static String getRequest(String url, int timeout) {
        return getRequest(url, null, timeout);
    }

    public static String getRequest(String url, Map<String, String> paramMap) {
        return getRequestAndHeader(url, null, paramMap, 2000);
    }

    public static String getRequest(String url, Map<String, String> paramMap, int timeout) {
        return getRequestAndHeader(url, null, paramMap, timeout);
    }
    
    public static String getRequest(String url, Map<String, String> paramMap, int timeout, String defaultCharset) {
        return getRequestAndHeader(url, null, paramMap, timeout, defaultCharset);
    }

    public static String getRequestAndHeader(String url, Map<String, String> headerMap, Map<String, String> paramMap) {
        return getRequestAndHeader(url, headerMap, paramMap, 2000);
    }

    /**
     * 发送一次get请求带header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static String getRequestAndHeader(String url, Map<String, String> headerMap, Map<String, String> paramMap, int timeout) {
        return getRequestAndHeader(url, headerMap, paramMap, timeout, "utf-8");
    }
    
    public static String getRequestAndHeader(String url, Map<String, String> headerMap, Map<String, String> paramMap, int timeout, String defaultCharset) {
        HttpGet httpget = new HttpGet(appendGetParams(url, paramMap));
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpget.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return executeRequest(httpget, timeout, defaultCharset);
    }

    public static String postRequest(String url, Map<String, ? extends Object> paramMap) {
        return postRequest(url, paramMap, 10000);
    }

    /**
     * 发送一次post请求
     *
     * @param url
     * @param paramMap
     * @param timeout  请求超时，毫秒数
     * @return
     * @comment
     */
    public static String postRequest(String url, Map<String, ? extends Object> paramMap, int timeout) {
        return postRequestAndHeader(url, null, paramMap, timeout);
    }

    /**
     * 发送一个post请求，带有header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static String postRequestAndHeader(String url, Map<String, String> headerMap, Map<String, Object> paramMap) {
        return postRequestAndHeader(url, headerMap, paramMap, 2000);
    }

    /**
     * 发送一个post请求，带有header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static String postRequestAndHeader(String url, Map<String, String> headerMap, Map<String, ? extends Object> paramMap, int timeout) {
        try {
            HttpPost httpPost = new HttpPost(url);
            if ( headerMap != null ) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            List<NameValuePair> nvps = new ArrayList<>();
            if ( paramMap != null ) {
                // 循环加入请求参数
                for (String paramName : paramMap.keySet()) {
                    if ( paramMap.get(paramName) == null ) {
                        continue;
                    }
                    nvps.add(new BasicNameValuePair(paramName, paramMap.get(paramName).toString()));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            return executeRequest(httpPost, timeout);
        } catch (UnsupportedEncodingException e) {
//            log.error("Encoding Error: url={}, error={}", url, e.getMessage());
            throw new RuntimeException("Encoding Error", e);
        }
    }

    public static String postRequestHaveFile(String url, Map<String, Object> paramMap, Map<String, File> fileParamMap) {
        return postRequestHaveFile(url, paramMap, fileParamMap, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static String postRequestHaveFile(String url, Map<String, Object> paramMap, Map<String, File> fileParamMap, int timeout) {
        return postRequestAndHeaderHaveFile(url, paramMap, null, fileParamMap, timeout);
    }

    public static String postRequestAndHeaderHaveFile(String url, Map<String, Object> paramMap, Map<String, String> headerMap, Map<String, File> fileParamMap) {
        return postRequestAndHeaderHaveFile(url, paramMap, headerMap, fileParamMap, 60000);
    }

    public static String postRequestAndHeaderHaveFile(String url, Map<String, Object> paramMap, Map<String, String> headerMap, Map<String, File> fileParamMap, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        if ( paramMap != null ) {
            // 循环加入请求参数
            for (String paramName : paramMap.keySet()) {
                multipartEntityBuilder.addPart(paramName, new StringBody(paramMap.get(paramName).toString(), ContentType.TEXT_PLAIN.withCharset("utf-8")));
            }
        }
        if ( fileParamMap != null ) {
            // 循环加入请求文件参数
            for (String fileName : fileParamMap.keySet()) {
                multipartEntityBuilder.addPart(fileName, new FileBody(fileParamMap.get(fileName)));
            }
        }
        //header
        httpPost.setEntity(multipartEntityBuilder.build());
        return executeRequest(httpPost, timeout);
    }

    public static String postRequestHaveFileInputStream(String url, Map<String, String> paramMap, Map<String, InputStream> fileParamMap) {
        return postRequestHaveFileInputStream(url, paramMap, fileParamMap, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static String postRequestHaveFileInputStream(String url, Map<String, String> paramMap, Map<String, InputStream> fileParamMap, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if ( paramMap != null ) {
            // 循环加入请求参数
            for (String paramName : paramMap.keySet()) {
                multipartEntityBuilder.addPart(paramName, new StringBody(paramMap.get(paramName).toString(), ContentType.TEXT_PLAIN.withCharset("utf-8")));
            }
        }
        if ( fileParamMap != null ) {
            // 循环加入请求文件参数
            for (String fileName : fileParamMap.keySet()) {
                multipartEntityBuilder.addPart(fileName, new InputStreamBody(fileParamMap.get(fileName), fileName));
            }
        }
        httpPost.setEntity(multipartEntityBuilder.build());
        return executeRequest(httpPost, timeout);
    }

    public static String postRequestHaveFileInputStream(String url, Map<String, String> headerMap, FileInputStream uoloadFile) {
        return postRequestHaveFileInputStream(url, headerMap, uoloadFile, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static String postRequestHaveFileInputStream(String url, Map<String, String> headerMap, FileInputStream uoloadFile, int timeout) {
        try {
            HttpPost httpPost = new HttpPost(url);
            if ( headerMap != null ) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPost.setEntity(new ByteArrayEntity(getFileBytes(uoloadFile)));
            return executeRequest(httpPost, timeout);
        } catch (IOException e) {
            throw new RuntimeException("io Error", e);
        }
    }

    /**
     * 发送一个post xml请求
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static String postRequestWithXml(String url, String xmlString) {
        return postRequestWithXmlAndHeader(url, null, xmlString, 3000);
    }

    /**
     * 发送一个post json请求
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static String postRequestWithJson(String url, String jsonString) {
        return postRequestWithJsonAndHeader(url, null, jsonString, 3000);
    }

    public static String postRequestWithXml(String url, String jsonString, int timeout) {
        return postRequestWithXmlAndHeader(url, null, jsonString, timeout);
    }

    public static String postRequestWithJson(String url, String jsonString, int timeout) {
        return postRequestWithJsonAndHeader(url, null, jsonString, timeout);
    }

    /**
     * 发送一个post xml请求，带有header
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static String postRequestWithXmlAndHeader(String url, Map<String, String> headerMap, String xmlString) {
        return postRequestWithXmlAndHeader(url, headerMap, xmlString, 3000);
    }

    /**
     * 发送一个post json请求，带有header
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static String postRequestWithJsonAndHeader(String url, Map<String, String> headerMap, String jsonString) {
        return postRequestWithJsonAndHeader(url, headerMap, jsonString, 3000);
    }

    /**
     * 发送一个post xml请求，带有header
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static String postRequestWithXmlAndHeader(String url, Map<String, String> headerMap, String xmlString, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity entity = new StringEntity(xmlString, "utf-8");// 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/xml");
        httpPost.setEntity(entity);
        return executeRequest(httpPost, timeout);
    }

    /**
     * 发送一个post json请求，带有header
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static String postRequestWithJsonAndHeader(String url, Map<String, String> headerMap, String jsonString, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity entity = new StringEntity(jsonString, "utf-8");// 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        return executeRequest(httpPost, timeout);
    }

    private static byte[] getFileBytes(FileInputStream upLoadFile) throws IOException {
        ByteBuffer byteBuf = ByteBuffer.allocate((int) upLoadFile.available());
        FileChannel fileChannel = upLoadFile.getChannel();

        fileChannel.read(byteBuf, 0);
        byteBuf.position(0);

        fileChannel.close();
        upLoadFile.close();
        return byteBuf.array();
    }

    /**
     * 通用的同步形式http执行调用
     * 
     * @param request
     * @return
     */
    public static String executeRequest(HttpUriRequest request) {
        return executeRequest(request, 2000);
    }
    
    /**
     * 通用的同步形式http执行调用
     * 
     * @param request
     * @param timeout
     * @return
     */
    public static String executeRequest(HttpUriRequest request, int timeout) {
        return executeRequest(request, timeout, "utf-8");
    }
    
    /**
     * 通用的同步形式http执行调用
     * 
     * @param request
     * @param timeout
     * @param defaultCharset
     * @return
     */
    public static String executeRequest(HttpUriRequest request, int timeout, String defaultCharset) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        if ( timeout > 500000 ) {
            timeout = 500000;
        }
        try {
            RequestConfig config = RequestConfig.copy(REQUEST_CONFIG).setSocketTimeout(timeout).build();
            httpClient = HttpClients.custom()
                    .setKeepAliveStrategy(connectionKeepAliveStrategy)
                    .setConnectionManagerShared(true)
                    .setUserAgent(customUserAgent)
                    .setRetryHandler(httpRequestRetryHandler)
                    .setConnectionManager(ConnectionManager.getConnectionManager())
                    .setDefaultRequestConfig(config)
                    .build();
            response = httpClient.execute(request);
            if ( !(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) ) {
                if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR ) {
                    return EntityUtils.toString(response.getEntity());
                }
                if (response.getEntity() == null) {
                    throw new RuntimeException("服务器异常稍后再试:" + request.getURI().toString() + "; httpStatus: "+response.getStatusLine().getStatusCode() + " ;response.getEntity()  is null ");
                }
                throw new RuntimeException("服务器异常稍后再试:" + request.getURI().toString() + "; httpStatus:"+response.getStatusLine().getStatusCode()+" ; " + EntityUtils.toString(response.getEntity()));
            }
            return EntityUtils.toString(response.getEntity(), defaultCharset);
        } catch (Exception e) {
            if (e instanceof IOException) {
//                log.error("Error while execute request: {} \t {}", e.getMessage(), request.getURI().toString());
            }
            request.abort();
            throw new RuntimeException(e);
        } finally {
            if ( response != null ) {
                try {
                    response.close();
                    //HttpClientUtils.closeQuietly(response);
                } catch (Exception ex) {
                    log.error("Error while close client {}", ex.getMessage());
                }
            }
            if ( httpClient != null ) {
                try {
                    httpClient.close();
                    //HttpClientUtils.closeQuietly(httpClient);
                } catch (Exception ex) {
                    log.error("Error while close client, {}", ex.getMessage());
                }
            }
        }
    }
    
    public static abstract class Callback {
        public abstract void completed(HttpUriRequest request, String result);
        public void failed(HttpUriRequest request, Exception ex) {
            
        }
        public void cancelled(HttpUriRequest request) {
            log.warn("http request canceled");
        }
    }
    
    public static Future<HttpResponse> asyncGetRequest(String url, Callback callback) {
        return asyncGetRequest(url, null, callback);
    }

    public static Future<HttpResponse> asyncGetRequest(String url, Callback callback, int timeout) {
        return asyncGetRequest(url, null, callback, timeout);
    }

    public static Future<HttpResponse> asyncGetRequest(String url, Map<String, String> paramMap, Callback callback) {
        return asyncGetRequestAndHeader(url, null, paramMap, callback, 2000);
    }

    public static Future<HttpResponse> asyncGetRequest(String url, Map<String, String> paramMap, Callback callback, int timeout) {
        return asyncGetRequestAndHeader(url, null, paramMap, callback, timeout);
    }

    public static Future<HttpResponse> asyncGetRequestAndHeader(String url, Map<String, String> headerMap, Map<String, String> paramMap, Callback callback) {
        return asyncGetRequestAndHeader(url, headerMap, paramMap, callback, 2000);
    }

    /**
     * 发送一次get请求带header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static Future<HttpResponse> asyncGetRequestAndHeader(String url, Map<String, String> headerMap, Map<String, String> paramMap, Callback callback, int timeout) {
        HttpGet httpget = new HttpGet(appendGetParams(url, paramMap));
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpget.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return executeRequest(httpget, callback, timeout);
    }
    
    public static Future<HttpResponse> asyncPostRequest(String url, Map<String, ? extends Object> paramMap, Callback callback) {
        return asyncPostRequest(url, paramMap, callback, 10000);
    }

    /**
     * 发送一次post请求
     *
     * @param url
     * @param paramMap
     * @param timeout  请求超时，毫秒数
     * @return
     * @comment
     */
    public static Future<HttpResponse> asyncPostRequest(String url, Map<String, ? extends Object> paramMap, Callback callback, int timeout) {
        return asyncPostRequestAndHeader(url, null, paramMap, callback, timeout);
    }

    /**
     * 发送一个post请求，带有header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestAndHeader(String url, Map<String, String> headerMap, Map<String, Object> paramMap, Callback callback) {
        return asyncPostRequestAndHeader(url, headerMap, paramMap, callback, 2000);
    }

    /**
     * 发送一个post请求，带有header
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestAndHeader(String url, Map<String, String> headerMap, Map<String, ? extends Object> paramMap, Callback callback, int timeout) {
        try {
            HttpPost httpPost = new HttpPost(url);
            if ( headerMap != null ) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            List<NameValuePair> nvps = new ArrayList<>();
            if ( paramMap != null ) {
                // 循环加入请求参数
                for (String paramName : paramMap.keySet()) {
                    if ( paramMap.get(paramName) == null ) {
                        continue;
                    }
                    nvps.add(new BasicNameValuePair(paramName, paramMap.get(paramName).toString()));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            return executeRequest(httpPost, callback, timeout);
        } catch (UnsupportedEncodingException e) {
//            log.error("Encoding Error: url={}, error={}", url, e.getMessage());
            throw new RuntimeException("Encoding Error", e);
        }
    }

    public static Future<HttpResponse> asyncPostRequestHaveFile(String url, Map<String, Object> paramMap, Map<String, File> fileParamMap, Callback callback) {
        return asyncPostRequestHaveFile(url, paramMap, fileParamMap, callback, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestHaveFile(String url, Map<String, Object> paramMap, Map<String, File> fileParamMap, Callback callback, int timeout) {
        return asyncPostRequestAndHeaderHaveFile(url, paramMap, null, fileParamMap, callback, timeout);
    }

    public static Future<HttpResponse> asyncPostRequestAndHeaderHaveFile(String url, Map<String, Object> paramMap, Map<String, String> headerMap, Map<String, File> fileParamMap, Callback callback) {
        return asyncPostRequestAndHeaderHaveFile(url, paramMap, headerMap, fileParamMap, callback, 60000);
    }

    public static Future<HttpResponse> asyncPostRequestAndHeaderHaveFile(String url, Map<String, Object> paramMap, Map<String, String> headerMap, Map<String, File> fileParamMap, Callback callback, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        if ( paramMap != null ) {
            // 循环加入请求参数
            for (String paramName : paramMap.keySet()) {
                multipartEntityBuilder.addPart(paramName, new StringBody(paramMap.get(paramName).toString(), ContentType.TEXT_PLAIN.withCharset("utf-8")));
            }
        }
        if ( fileParamMap != null ) {
            // 循环加入请求文件参数
            for (String fileName : fileParamMap.keySet()) {
                multipartEntityBuilder.addPart(fileName, new FileBody(fileParamMap.get(fileName)));
            }
        }
        //header
        httpPost.setEntity(multipartEntityBuilder.build());
        return executeRequest(httpPost, callback, timeout);
    }

    public static Future<HttpResponse> asyncPostRequestHaveFileInputStream(String url, Map<String, String> paramMap, Map<String, InputStream> fileParamMap, Callback callback) {
        return asyncPostRequestHaveFileInputStream(url, paramMap, fileParamMap, callback, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestHaveFileInputStream(String url, Map<String, String> paramMap, Map<String, InputStream> fileParamMap, Callback callback, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if ( paramMap != null ) {
            // 循环加入请求参数
            for (String paramName : paramMap.keySet()) {
                multipartEntityBuilder.addPart(paramName, new StringBody(paramMap.get(paramName).toString(), ContentType.TEXT_PLAIN.withCharset("utf-8")));
            }
        }
        if ( fileParamMap != null ) {
            // 循环加入请求文件参数
            for (String fileName : fileParamMap.keySet()) {
                multipartEntityBuilder.addPart(fileName, new InputStreamBody(fileParamMap.get(fileName), fileName));
            }
        }
        httpPost.setEntity(multipartEntityBuilder.build());
        return executeRequest(httpPost, callback, timeout);
    }

    public static String asyncPostRequestHaveFileInputStream(String url, Map<String, String> headerMap, FileInputStream uoloadFile) {
        return postRequestHaveFileInputStream(url, headerMap, uoloadFile, 10000);
    }

    /**
     * 发送一个带文件的post请求
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @param fileParamMap
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestHaveFileInputStream(String url, Map<String, String> headerMap, FileInputStream uoloadFile, Callback callback, int timeout) {
        try {
            HttpPost httpPost = new HttpPost(url);
            if ( headerMap != null ) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPost.setEntity(new ByteArrayEntity(getFileBytes(uoloadFile)));
            return executeRequest(httpPost, callback, timeout);
        } catch (IOException e) {
            throw new RuntimeException("io Error", e);
        }
    }

    /**
     * 发送一个post xml请求
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithXml(String url, String xmlString, Callback callback) {
        return asyncPostRequestWithXmlAndHeader(url, null, xmlString, callback, 3000);
    }

    /**
     * 发送一个post json请求
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithJson(String url, String jsonString, Callback callback) {
        return asyncPostRequestWithJsonAndHeader(url, null, jsonString, callback, 3000);
    }

    public static Future<HttpResponse> asyncPostRequestWithXml(String url, String jsonString, Callback callback, int timeout) {
        return asyncPostRequestWithXmlAndHeader(url, null, jsonString, callback, timeout);
    }

    public static Future<HttpResponse> asyncPostRequestWithJson(String url, String jsonString, Callback callback, int timeout) {
        return asyncPostRequestWithJsonAndHeader(url, null, jsonString, callback, timeout);
    }

    /**
     * 发送一个post xml请求，带有header
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithXmlAndHeader(String url, Map<String, String> headerMap, String xmlString, Callback callback) {
        return asyncPostRequestWithXmlAndHeader(url, headerMap, xmlString, callback, 3000);
    }

    /**
     * 发送一个post json请求，带有header
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithJsonAndHeader(String url, Map<String, String> headerMap, String jsonString, Callback callback) {
        return asyncPostRequestWithJsonAndHeader(url, headerMap, jsonString, callback, 3000);
    }

    /**
     * 发送一个post xml请求，带有header
     *
     * @param url
     * @param headerMap
     * @param xmlString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithXmlAndHeader(String url, Map<String, String> headerMap, String xmlString, Callback callback, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity entity = new StringEntity(xmlString, "utf-8");// 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/xml");
        httpPost.setEntity(entity);
        return executeRequest(httpPost, callback, timeout);
    }

    /**
     * 发送一个post json请求，带有header
     *
     * @param url
     * @param headerMap
     * @param jsonString 请求参数
     * @return
     */
    public static Future<HttpResponse> asyncPostRequestWithJsonAndHeader(String url, Map<String, String> headerMap, String jsonString, Callback callback, int timeout) {
        HttpPost httpPost = new HttpPost(url);
        if ( headerMap != null ) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity entity = new StringEntity(jsonString, "utf-8");// 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        return executeRequest(httpPost, callback, timeout);
    }
    
    public static Future<HttpResponse> asyncDeleteRequest(String url, Callback callback) {
        return asyncDeleteRequest(url, callback, 10000);
    }
    
    public static Future<HttpResponse> asyncDeleteRequest(String url, Callback callback, int timeout) {
        HttpDelete httpDelete = new HttpDelete(url);
        return executeRequest(httpDelete, callback, timeout);
    }
    
    private volatile static CloseableHttpAsyncClient httpAsyncClient = null;
    
    private static CloseableHttpAsyncClient getAsyncClient() {
        if (httpAsyncClient == null) {
            synchronized (HttpClientUtil.class) {
                if (httpAsyncClient == null) {
                    RequestConfig config = RequestConfig.copy(REQUEST_CONFIG).build();
                    httpAsyncClient = HttpAsyncClients.custom()
                            .setKeepAliveStrategy(connectionKeepAliveStrategy)
                            .setConnectionManager(AsyncConnectionManager.getConnectionManager())
                            .setDefaultRequestConfig(config)
                            .build();
                    httpAsyncClient.start();
                }
            }
        }
        return httpAsyncClient;
    }
    
    /**
     * 通用的异步http请求执行器
     * 
     * @param request
     * @param callback
     * @param timeout
     * @return
     */
    public static Future<HttpResponse> executeRequest(final HttpUriRequest request, final Callback callback, int timeout) {
        CloseableHttpAsyncClient httpClient = getAsyncClient();
        if ( timeout > 500000 ) {
            timeout = 500000;
        }
        try {
            // set UA
            request.setHeader("User-Agent", customUserAgent);
            //这里目前不做共享，慎重考虑未来是否做threadLocal
            HttpClientContext context = HttpClientContext.create();
            RequestConfig config = RequestConfig.copy(REQUEST_CONFIG).setSocketTimeout(timeout).build();
            context.setRequestConfig(config);
            FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>() {

                @Override
                public void completed(HttpResponse response) {
                    try {
                        if ( !(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) ) {
                            if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR ) {
                                callback.completed(request, EntityUtils.toString(response.getEntity()));
                            }
                            if ( response.getEntity() == null ) {
                                throw new RuntimeException(
                                        String.format("服务器异常稍后再试: %s; httpStatus: %d; response.getEntity()  is null",
                                                request.getURI().toString(), response.getStatusLine().getStatusCode()));
                            }
                            throw new RuntimeException(String.format("服务器异常稍后再试: %s; httpStatus: %d; %s",
                                    request.getURI().toString(), response.getStatusLine().getStatusCode(),
                                    EntityUtils.toString(response.getEntity())));
                        }
                        callback.completed(request, EntityUtils.toString(response.getEntity(), "utf-8"));
                    } catch (Exception e) {
                        log.warn("async complete error", e);
                        request.abort();
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    callback.failed(request, ex);
                }

                @Override
                public void cancelled() {
                    callback.cancelled(request);
                }
            };
            return httpClient.execute(request, context, futureCallback);
        } catch (Exception e) {
            log.warn("async error", e);
            request.abort();
            throw new RuntimeException(e);
        } finally {
            if ( httpClient != null ) {
                try {
                    //HttpClientUtils.closeQuietly(httpClient);
                } catch (Exception ex) {
                    log.error("Error while close client, {}", ex.getMessage());
                }
            }
        }
    }

    public static String appendGetParams(String url, Map<String, String> paramMap) {
        if ( paramMap == null || paramMap.size() == 0 ) {
            return url;
        }
        StringBuilder url_target = new StringBuilder();
        url_target.append(url);

        if ( url.indexOf("?") > 0 ) {
            if ( url_target.indexOf("?") != url_target.length() - 1 ) {
                url_target.append("&");
            }
        } else {
            url_target.append("?");
        }
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if ( entry.getKey() == null ) {
                continue;
            }
            if ( isFirst ) {
                isFirst = false;
            } else {
                url_target.append("&");
            }
            try {
                url_target.append(URLEncoder.encode(entry.getKey(), "utf-8")).append("=");
                if ( entry.getValue() != null ) {
                    url_target.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                }
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }
        return url_target.toString();
    }
}

class ConnectionManager {
    private static ConnectionManager cm = new ConnectionManager();
    private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    private ConnectionManager() {
        connectionManager.setMaxTotal(8000);
        connectionManager.setDefaultMaxPerRoute(500);
        //连接非活动半关闭状态(CLOSE_WAIT)检测
        connectionManager.setValidateAfterInactivity(1000);

        IdleConnectionMonitorThread idleConnectionMonitor = new IdleConnectionMonitorThread(connectionManager);
        idleConnectionMonitor.start();
    }

    public static PoolingHttpClientConnectionManager getConnectionManager() {
        return cm.connectionManager;
    }
}

class AsyncConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConnectionManager.class.getSimpleName());
    private static AsyncConnectionManager cm = new AsyncConnectionManager();
    private PoolingNHttpClientConnectionManager connectionManager;
    {
        DefaultConnectingIOReactor reactor;
        try {
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setSoTimeout(15000).build();
            reactor = new DefaultConnectingIOReactor(ioReactorConfig);
            connectionManager = new PoolingNHttpClientConnectionManager(reactor);
        } catch (IOReactorException e) {
            logger.warn("create async connection manager failed", e);
        }
    }
    private AsyncConnectionManager() {
        connectionManager.setMaxTotal(8000);
        connectionManager.setDefaultMaxPerRoute(500);

        NIdleConnectionMonitorThread idleConnectionMonitor = new NIdleConnectionMonitorThread(connectionManager);
        idleConnectionMonitor.start();
    }

    public static PoolingNHttpClientConnectionManager getConnectionManager() {
        return cm.connectionManager;
    }
}

/**
 * Idel连接监控
 */
class IdleConnectionMonitorThread extends Thread {
    private final PoolingHttpClientConnectionManager connMgr;
    private volatile boolean shutdown;

    public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
        super();
        setDaemon(true);
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while ( !shutdown ) {
                synchronized (this) {
                    wait(10000);
                    // 关闭失效的连接
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            // terminate
        }
    }

    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}

class NIdleConnectionMonitorThread extends Thread {
    private final PoolingNHttpClientConnectionManager connMgr;
    private volatile boolean shutdown;

    public NIdleConnectionMonitorThread(PoolingNHttpClientConnectionManager connMgr) {
        super();
        setDaemon(true);
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while ( !shutdown ) {
                synchronized (this) {
                    wait(10000);
                    // 关闭失效的连接
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            // terminate
        }
    }

    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}
