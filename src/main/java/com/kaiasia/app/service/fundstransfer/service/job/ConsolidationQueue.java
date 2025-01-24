package com.kaiasia.app.service.fundstransfer.service.job;

import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ConsolidationQueue {
    private final ConcurrentLinkedQueue<TransactionInfo> queue = new ConcurrentLinkedQueue<>();

    public ConsolidationQueue() {
    }

    public TransactionInfo getFromQueue() {
        return queue.poll();
    }
    public void addToQueue(TransactionInfo info) {
        queue.add(info);
    }

    public int getQueueSize() {
        return queue.size();
    }
}
