package com.yoloho.common.thread;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 优雅关闭的任务超类
 * 
 * @author jason
 *
 */
public abstract class GracefullyShutdownThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(getJobClass().getSimpleName());
    
    protected boolean shutdown = false;
    protected boolean shutdownComplete = false;
    
    /**
     * 等待指定毫秒数
     * 
     * @param millis
     */
    protected void delayMillis(long millis) {
        try {
            while (millis > 0 && !shutdown) {
                sleep(500);
                millis -= 500;
            }
        } catch (InterruptedException e) {
        }
    }

    @PostConstruct
    public void init() {
        setName(getClass().getSimpleName());
        setDaemon(false);
        start();
    }
    
    @PreDestroy
    public void deinit() {
        shutdown = true;
        int cnt = 0;
        while (!shutdownComplete && cnt < 120) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
            }
            cnt ++;
        }
    }
    
    protected abstract boolean shouldNotShutdown();
    
    @Override
    public final void run() {
        logger.info("启动任务");
        while (!shutdown || shouldNotShutdown()) {
            try {
                call();
            } catch (Exception e) {
                logger.error("任务执行失败", e);
                delayMillis(200);
            }
        }
        logger.info("任务结束");
        shutdownComplete = true;
    }
    
    /**
     * 任务定义的实际子类
     * 
     * @return
     */
    protected abstract Class<?> getJobClass();
    
    /**
     * 任务实际的执行体，只需要考虑单次调度调用
     */
    protected abstract void call();
}
