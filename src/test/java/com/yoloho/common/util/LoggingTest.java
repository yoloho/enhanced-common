package com.yoloho.common.util;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoloho.common.util.Logging;
import com.yoloho.common.util.RandomUtil;

/**
 * 日志测试类，目前作为单元测试先禁用
 * 
 * @author jason
 *
 */
@Ignore
public class LoggingTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingTest.class.getSimpleName());
    @Test
    public void loggingTest() {
        Logging.setMaxFileSize("100KB");
        Logging.initLogging(false, true);
        while(true) {
            logger.info(RandomUtil.getRandomString(50));
            logger.error(RandomUtil.getRandomString(50));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
}
