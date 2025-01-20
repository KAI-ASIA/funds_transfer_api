package com.kaiasia.app.service.fundstransfer.job.executor;

/**
 * Abstract a task queue, it can be a normal queue, redis queue, kafka queue, etc...
 * @since 20250121
 * @author <b style="color: yellow;">lamlam</b>
 */
public interface TaskQueue {
    /**
     * @param task <strong>Task</strong> to add to the end of the queue
     * @return {@code true} if success <br/> {@code false} otherwise
     */
    boolean addTask(Runnable task);

    /**
     * @return <strong>Task</strong> from head of the queue
     */
    Runnable getTask();

    /**
     * @return Num of element in the queue
     */
    int size();
}
