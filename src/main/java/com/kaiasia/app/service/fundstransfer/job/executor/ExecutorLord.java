package com.kaiasia.app.service.fundstransfer.job.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.WAITING;
import static java.lang.Thread.State.TIMED_WAITING;

@Slf4j
public class ExecutorLord implements Executor {
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

    public ExecutorLord() {
        this(1, 5, 50, 60000);
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

    private void init() {
        for (int i = 1; i <= corePoolSize; i++) {
            InternalWorker worker = new InternalWorker(false);
            workers.add(worker);
            worker.start();
        }
        new Thread(this::processQueue).start();
        new Thread(this::cleanUpWorker).start();
    }

    private boolean assignTask(Runnable task) {
        try {
            if (workers.size() < maxPoolSize) {
                InternalWorker worker = new InternalWorker(true);
                workers.add(worker);
                worker.setTask(task);
                worker.start();
                return true;
            }
            for (InternalWorker worker : workers) {
                if (worker.task == null && (WAITING.equals(worker.getState()) || TIMED_WAITING.equals(worker.getState()))) {
                    worker.setTask(task);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Assign task failed due to: {}", e.getMessage(), e);
            return false;
        }
        return false;
    }

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

    private void cleanUpWorker() {
        while (RUNNING.equals(executorState)) {
            workers.removeIf(wk -> TERMINATED.equals(wk.getState()));
            try {
                synchronized (workers) {
                    workers.wait(idleTimeout);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Clean up thread terminated.");
    }

    public void shutDown() {
        executorState = TERMINATED.name();
        workers.forEach(wk -> wk.canTimeout = true);
    }

    private class InternalWorker extends Thread {
        private Runnable task;
        private boolean canTimeout;
        private long lastProcessTime;

        public InternalWorker() {
        }

        public InternalWorker(boolean canTimeout) {
            this.canTimeout = canTimeout;
        }

        public void setTask(Runnable task) {
            synchronized (this) {
                this.task = task;
                notifyAll();
            }
        }

        private void removeTask() {
            synchronized (this) {
                this.task = null;
            }
        }

        @Override
        public void run() {
            lastProcessTime = System.currentTimeMillis();
            doWork();
        }

        public void doWork() {
            while (System.currentTimeMillis() - lastProcessTime < idleTimeout || !canTimeout) {
                if (task == null) {
                    synchronized (this) {
                        try {
                            wait(idleTimeout);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    task.run();
                    lastProcessTime = System.currentTimeMillis();
                    removeTask();
                }
                synchronized (workers) {
                    workers.notifyAll();
                }
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        if (executorState.equals(TERMINATED.name())) {
            throw new IllegalStateException("ExecutorLord already terminated");
        }
        if (assignTask(command)) {
            return;
        }
        if (taskQueue.size() + 1 >= queueCapacity) {
            throw new IllegalStateException("Queue is full");
        }
        taskQueue.addTask(command);
    }
}
