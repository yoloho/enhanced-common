package com.yoloho.enhanced.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.common.util.ThreadPoolUtil;

/**
 * @author jason
 */
public class ThreadPoolUtilTest {
    @Test
    public void testWaitForDone() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100));
        {
            Future<Long> job = threadPoolExecutor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    Thread.sleep(500);
                    return 333L;
                }
            });
            long begin = System.currentTimeMillis();
            List<Future<?>> futureList = new ArrayList<>();
            futureList.add(job);
            ThreadPoolUtil.waitForDone(futureList);
            System.out.println("time: " + (System.currentTimeMillis() - begin));
            boolean got_result = false;
            try {
                System.out.println("result: " + job.get());
                got_result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Assert.assertTrue(got_result);
        }
        {
            Future<Long> job = threadPoolExecutor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    Thread.sleep(500);
                    return 333L;
                }
            });
            long begin = System.currentTimeMillis();
            List<Future<?>> futureList = new ArrayList<>();
            futureList.add(job);
            ThreadPoolUtil.waitForDone(futureList, 100);
            System.out.println("time: " + (System.currentTimeMillis() - begin));
            boolean got_result = false;
            try {
                System.out.println("result: " + job.get());
                got_result = true;
            } catch (Exception e) {
                System.out.println("result: null");
            }
            Assert.assertFalse(got_result);
        }

    }
}
