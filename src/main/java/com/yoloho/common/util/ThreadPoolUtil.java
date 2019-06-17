package com.yoloho.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class ThreadPoolUtil {

    /**
     * Timeout default to 10000 millis to wait
     *
     * @param futureList
     */
    public static void waitForDone(List<Future<?>> futureList) {
        waitForDone(futureList, 10000);
    }

    /**
     * Wait for all the futures to be done or timeout to kill all the uncomplete futures
     *
     * @param futureList
     * @param milliseconds
     */
    public static void waitForDone(List<Future<?>> futureList, int milliseconds) {
        if ( futureList != null && futureList.size() > 0 ) {
            while ( true ) {
                Iterator<Future<?>> it = futureList.iterator();
                Future<?> future = null;
                while ( it.hasNext() ) {
                    future = it.next();
                    if ( future.isDone() || future.isCancelled() ) {
                        it.remove();
                    } else if ( milliseconds <= 1 ) {
                        future.cancel(true);
                        it.remove();
                        continue;
                    }
                }
                if ( futureList.isEmpty() )
                    break;
                try {
                    Thread.sleep(10);
                } catch (Exception ex) {
                }
                milliseconds -= 10;
            }
        }
    }


}
