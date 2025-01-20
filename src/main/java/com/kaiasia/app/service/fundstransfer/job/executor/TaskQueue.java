package com.kaiasia.app.service.fundstransfer.job.executor;

public interface TaskQueue {
    boolean addTask(Runnable task);

    Runnable getTask();

    int size();
}
