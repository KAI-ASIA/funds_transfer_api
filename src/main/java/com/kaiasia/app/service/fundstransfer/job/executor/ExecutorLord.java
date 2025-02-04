package com.kaiasia.app.service.fundstransfer.job.executor;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.WAITING;
import static java.lang.Thread.State.TIMED_WAITING;

/**
 * A custom implementation of {@link Executor}, that use {@code Object.wait()} method as pool mechanism
 *
 * @author <b style="color: yellow;">lamlam</b>
 * @see Executor
 * @since 20250121
 */
@Slf4j
public class ExecutorLord implements Executor, AutoCloseable {
    private static final String RUNNING = "RUNNING";
    private TaskQueue taskQueue;
    private final Collection<InternalWorker> workers;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private int idleTimeout;
    private String executorState = RUNNING;

    // lock
    private final Object taskQueueLock = new Object();
    private final Object workerLock = new Object();

    public ExecutorLord() {
        this(5, 15, 50, 60000);
    }

    public ExecutorLord(int corePoolSize, int maxPoolSize, int queueCapacity, int idleTimeout) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        this.idleTimeout = idleTimeout;
        taskQueue = new DefaultTaskQueue();
        workers = new ConcurrentLinkedQueue<>();
        init();
    }

    /**
     * <p>Use when you want to change task queue (maybe because of reuse the executor or because of it implementation, you want to have more control over the queue), instead using default queue</p>
     * <p>This action will not immediately affect the old queue, it need to wait for remaining task in the old queue has been started processing</p>
     * <p>It implementation will be update in the future</p>
     *
     * @param newTaskQueue The new queue you want to apply to this executor
     */
    public void setTaskQueue(TaskQueue newTaskQueue) {
        if (newTaskQueue == null) {
            throw new NullPointerException("TaskQueue is null");
        }
        if (newTaskQueue.size() > queueCapacity) {
            throw new IllegalArgumentException("TaskQueue has more task than queue capacity");
        }
        while (taskQueue.size() > 0) {
        }
        taskQueue = newTaskQueue;
        log.info("Task queue has been updated");
    }

    /**
     * <p>Init worker thread to the number of core pool size, process queue thread and clean up worker thread</p>
     */
    private void init() {
        for (int i = 1; i <= corePoolSize; i++) {
            InternalWorker worker = new InternalWorker(false);
            workers.add(worker);
            worker.start();
        }
        new Thread(this::processQueue).start();
        new Thread(this::cleanUpWorker).start();
    }

    /**
     * <p>Assign task from {@code execute} method, or {@code processQueue} method, which will assign task to
     * a free thread, or will create new worker thread if num of it below core pool size</p>
     *
     * @param task The task will be assigned to a worker thread
     * @return {@code true} if the task was assigned to a worker thread<br/>
     * {@code false} otherwise
     */
    private boolean assignTask(Runnable task) {
        try {
            for (InternalWorker worker : workers) {
                if (worker.task == null && (WAITING.equals(worker.getState()) || TIMED_WAITING.equals(worker.getState()))) {
                    worker.setTask(task);
                    return true;
                }
            }
            if (workers.size() < maxPoolSize) {
                InternalWorker worker = new InternalWorker(true);
                workers.add(worker);
                worker.setTask(task);
                worker.start();
                return true;
            }
        } catch (Exception e) {
            log.error("Assign task failed due to: {}", e.getMessage(), e);
            return false;
        }
        return false;
    }

    /**
     * <p>Stat assigning task remain in the queue to worker thread</p>
     * <p>It will be slept if queue is empty</p>
     */
    private void processQueue() {
        while (RUNNING.equals(executorState)) {
            if (taskQueue.size() > 0) {
                Runnable task = taskQueue.getTask();
                boolean success = assignTask(task);
                if (!success) {
                    taskQueue.addTask(task);
                }
                continue;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Process queue thread terminated");
    }

    /**
     * <p>Start clean up worker has terminated (timeout), that can't be reused after period</p>
     * <p>Or will be run when a worker notify that it was terminated<p/>
     */
    private void cleanUpWorker() {
        while (RUNNING.equals(executorState)) {
            workers.removeIf(wk -> TERMINATED.equals(wk.getState()));
            try {
                synchronized (workerLock) {
                    workerLock.wait(idleTimeout);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Clean up thread terminated.");
    }

    /**
     * <p>Shutdown method will change <b>{@code executorState}</b> to <b>{@code TERMINATED}</b>, that will case <b>{@code processQueue}</b>thread and <b>{@code cleanUpWorker}</b> thread stop executing</p>
     * <p>Then it will interrupt all worker thread is still idle</p>
     * <p>Finally it will clear the worker queue</p>
     */
    public void shutDown() {
        executorState = TERMINATED.name();
        workers.forEach(InternalWorker::interruptWorker);
        workers.clear();
    }

    /**
     * <p>The class represent a worker thread, with <b>{@code wait()}</b> and {@code notifyAll()} mechanism</p>
     */
    private class InternalWorker extends Thread {
        private Runnable task;
        private boolean canTimeout;
        private long lastProcessTime;

        public InternalWorker() {
        }

        public InternalWorker(boolean canTimeout) {
            this.canTimeout = canTimeout;
        }

        /**
         * <p>Synchronous set the task to worker thread</p>
         * <p>It will throw <b>{@code IllegalStateException}</b> if the current task is not null, that mean current worker is running a different task</p>
         * <p>After that <b>{@code notifyAll()}</b>  will be called to awake the thread from waiting</p>
         *
         * @param task The task to assigned to this worker
         * @see #notifyAll()
         */
        public void setTask(Runnable task) {
            if (this.task != null) {
                throw new IllegalStateException("Task is already set");
            }
            synchronized (this) {
                this.task = task;
                notifyAll();
            }
        }

        /**
         * <p>Synchronous remove the task after current ask was executed by this worker</p>
         */
        private void removeTask() {
            synchronized (this) {
                this.task = null;
            }
        }

        /**
         * <p>Interrupt this worker if it state is <b>WAITING</b> or <b>TIME_WAITING</b></p>
         *
         * @see #interrupt()
         */
        public void interruptWorker() {
            if (TIMED_WAITING.equals(getState()) || WAITING.equals(getState())) {
                try {
                    interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }

        /**
         * <p>Override run method inherited from <b>{@code Thread} class</b></p>
         * <p>That simply start this worker</p>
         *
         * @see #doWork()
         */
        @Override
        public void run() {
            lastProcessTime = System.currentTimeMillis();
            doWork();
        }

        /**
         * <p>After a specific period, this worker will check if the task is available</p>
         * <p>If the task is present, it will be executed</p>
         * <p>If not, this worker will wait for a specific amount of time and recheck again</p>
         * <p>Specially, if the task <b>{@code canTimeout}</b>, it will be terminated and then be cleaned by <b>{@code cleanUpWorker}</b></p>
         */
        public void doWork() {
            while (System.currentTimeMillis() - lastProcessTime < idleTimeout || !canTimeout) {
                if (task == null) {
                    synchronized (this) {
                        try {
                            wait(idleTimeout);
                        } catch (InterruptedException e) {
                            interrupt();
                            return;
                        }
                    }
                } else {
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("Task failed due to: {}", e.getMessage(), e);
                    }
                    lastProcessTime = System.currentTimeMillis();
                    removeTask();
                }
                synchronized (workerLock) {
                    workerLock.notifyAll();
                }
            }
        }
    }

    /**
     * <p>That method will check if the executor is still alive, and then start assign task using <b>{@code assignTask}</b> method</p>
     * <p>If not, an <b>{@code IllegalStateException}</b> will be thrown</p>
     * <p>If the task can be assigned using <b>{@code assignTask}</b> method, it will be added to the task queue</p>
     * <p>If the task queue is full, an <b>{@code IllegalStateException}</b> will be thrown</p>
     *
     * @param command the runnable task
     */
    @Override
    public void execute(@Nonnull Runnable command) {
        if (executorState.equals(TERMINATED.name())) {
            throw new IllegalStateException("ExecutorLord already terminated");
        }
        if (assignTask(command)) {
            return;
        }
        if (taskQueue.size() == queueCapacity) {
            throw new IllegalStateException("Task queue is full");
        }
        taskQueue.addTask(command);
    }

    /**
     * <p>Implement close method of Autocloseable interface, it simply call <b>{@code shutDown}</b> method</p>
     */
    @Override
    public void close() {
        shutDown();
    }
}
