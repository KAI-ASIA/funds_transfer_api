package com.kaiasia.app.service.fundstransfer.service.job;

import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.job.ConsolidationFTQueue;
import com.kaiasia.app.service.fundstransfer.job.ConsolidationFTTask;
import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ConsolidationJob {
    private final ConsolidationTask task;
    private final ITransactionInfoDAO transactionInfoDAO;
    private final ConsolidationQueue queue;


    public ConsolidationJob(ConsolidationTask task, ITransactionInfoDAO transactionInfoDAO, ConsolidationQueue queue) {
        this.task = task;
        this.transactionInfoDAO = transactionInfoDAO;
        this.queue = queue;
    }

    @Value("${job.consolidation.numOfThread}")
    private int numOfThread;
    @Value("${job.consolidation.period}")
    private int period;
    @Value("${job.consolidation.maxQueueSize}")
    private int maxQueueSize;
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(numOfThread);
        new Thread(this::fetchConsolidationFT).start();
        new Thread(this::doConsolidationFTTask).start();
    }

    private void fetchConsolidationFT() {
        log.info("Start fetching consolidation FT ");
        while (true) {
            try {
                if (queue.getQueueSize() < maxQueueSize) {
                    List<TransactionInfo> transactionInfos = transactionInfoDAO.getTransactionInfoByStatus(TransactionStatus.CONSOLIDATION.name(), maxQueueSize - queue.getQueueSize());
                    transactionInfos.forEach(queue::addToQueue);
                }
                Thread.sleep(period * 1000L);
            } catch (Exception e) {
                log.error("Failed to fetch consolidation FT", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void doConsolidationFTTask() {
        log.info("Start processing consolidation FT ");
        while (true) {
            if (queue.getQueueSize() == 0) {
                try {
                    Thread.sleep(period * 1000L);
                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
                continue;
            }
            executor.execute(task);
        }
    }
}
