package com.yoloho.enhanced.common.thread;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.yoloho.enhanced.common.annotation.NonNull;

/**
 * A thread driven by blocking queue
 * 
 * @author jason
 *
 */
public abstract class QueueDrivenThread<T> extends GracefullyShutdownThread {
    private final Logger logger = LoggerFactory.getLogger(getJobClass().getSimpleName());
    
    protected ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<>(getQueueCapacity());
    
    @Override
    protected boolean shouldNotShutdown() {
        return queue.size() > 0;
    }
    
    public void add(T item) {
        if (shutdown) {
            throw new RuntimeException("Thread is already shutdown");
        }
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            logger.warn("Add to queue failed: {}", item);
        }
    }
    
    /**
     * Pop out items from the queue
     * 
     * @param size Maximum number of items one time popped out
     * @return
     */
    @NonNull
    protected List<T> fetchData(int size) {
        List<T> list = Lists.newArrayList();
        queue.drainTo(list, size);
        return list;
    }
    
    public int getQueueSize() {
        return queue.size();
    }
    
    /**
     * Capacity of the queue containing unprocessed items
     * 
     * @return
     */
    protected abstract int getQueueCapacity();
}
