package com.yoloho.enhanced.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.yoloho.enhanced.common.util.HttpClientUtil;
import com.yoloho.enhanced.common.util.HttpClientUtil.Callback;

public class HttpClientUtilTest {
    @SuppressWarnings("unused")
    private static class Msg {
        private int errno = 0;
        private String errdesc;
        public int getErrno() {
            return errno;
        }
        public String getErrdesc() {
            return errdesc;
        }
        public void setErrno(int errno) {
            this.errno = errno;
        }
        public void setErrdesc(String errdesc) {
            this.errdesc = errdesc;
        }
    }
    
    @Test
    public void splitTest() {
        String queryString = null;
        Map<String, String> map = HttpClientUtil.splitKeyValuePairString(queryString);
        assertNotNull(map);
        assertEquals(0, map.size());
        
        queryString = "";
        map = HttpClientUtil.splitKeyValuePairString(queryString);
        assertNotNull(map);
        assertEquals(0, map.size());
        
        queryString = "k=v&k1=v1";
        map = HttpClientUtil.splitKeyValuePairString(queryString);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("v", map.get("k"));
        
        queryString = "k=v&=v2&k1=v1";
        map = HttpClientUtil.splitKeyValuePairString(queryString);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
        
        queryString = "k=v&k2=&k1=v1";
        map = HttpClientUtil.splitKeyValuePairString(queryString);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));
    }

    @Test
    public void getRequestTest() {
        String url = "http://uicapi.test.yoloho.com/user/login";
        Msg msg = JSON.parseObject(HttpClientUtil.getRequest(url), Msg.class);
        Assert.assertEquals(21100, msg.getErrno());
        msg = JSON.parseObject(HttpClientUtil.getRequest(url, 1000), Msg.class);
        Assert.assertEquals(21100, msg.getErrno());
        msg = JSON.parseObject(HttpClientUtil.getRequest(url, null, 1000), Msg.class);
        Assert.assertEquals(21100, msg.getErrno());
        msg = JSON.parseObject(HttpClientUtil.postRequest(url, null), Msg.class);
        Assert.assertEquals(21100, msg.getErrno());
        msg = JSON.parseObject(HttpClientUtil.postRequest(url, null, 1000), Msg.class);
        Assert.assertEquals(21100, msg.getErrno());
    }
    
    @Test
    public void asyncGetRequestTest() {
        String url = "http://uicapi.test.yoloho.com/user/login";
        List<Future<HttpResponse>> futures = Lists.newArrayList();
        futures.add(HttpClientUtil.asyncGetRequest(url, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }));
        futures.add(HttpClientUtil.asyncGetRequest(url, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }, 1000));
        futures.add(HttpClientUtil.asyncGetRequest(url, null, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }, 1000));
        futures.add(HttpClientUtil.asyncGetRequest(url, null, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }));
        futures.add(HttpClientUtil.asyncPostRequest(url, null, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }));
        futures.add(HttpClientUtil.asyncPostRequest(url, null, new Callback() {

            @Override
            public void completed(HttpUriRequest request, String result) {
                Msg msg = JSON.parseObject(result, Msg.class);
                Assert.assertEquals(21100, msg.getErrno());
            }

            @Override
            public void cancelled(HttpUriRequest request) {
                Assert.assertTrue(false);
            }

            @Override
            public void failed(HttpUriRequest request, Exception ex) {
                Assert.assertTrue(false);
            }
        }, 1000));
        for (Future<HttpResponse> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                Assert.assertTrue(false);
            }
        }
    }

}
