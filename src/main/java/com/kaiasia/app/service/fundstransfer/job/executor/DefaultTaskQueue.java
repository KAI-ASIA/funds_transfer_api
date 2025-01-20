package com.kaiasia.app.service.fundstransfer.job.executor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default implementation of {@link  TaskQueue}, that use a {@link ConcurrentLinkedQueue} as it's core queue
 * @see TaskQueue
 * @see ConcurrentLinkedQueue
 * @since 20250121
 * @author <b style="color: yellow;">lamlam</b>
 */
public class DefaultTaskQueue implements TaskQueue {
    // Core queue
    private final Queue<Runnable> taskQueue;

    public DefaultTaskQueue() {
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean addTask(Runnable task) {
        return taskQueue.add(task);
    }

    @Override
    public Runnable getTask() {
        return taskQueue.poll();
    }

    @Override
    public int size() {
        return taskQueue.size();
    }
}
