package com.kaiasia.app.service.fundstransfer.job.executor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DefaultTaskQueue implements TaskQueue {
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
