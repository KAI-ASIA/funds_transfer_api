package com.kaiasia.app.service.fundstransfer.job;

import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class ConsolidationFTJob {
    private final ConsolidationFTTask task;
    private final ITransactionInfoDAO transactionInfoDAO;
    private final ConsolidationFTQueue queue;

    public ConsolidationFTJob(ConsolidationFTTask task, ConsolidationFTQueue queue, ITransactionInfoDAO transactionInfoDAO) {
        this.task = task;
        this.queue = queue;
        this.transactionInfoDAO = transactionInfoDAO;
    }

    @Value("${job.consolidation.numOfThread}")
    private int numOfThread;
    @Value("${job.consolidation.period}")
    private int period;
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
                List<TransactionInfo> transactionInfos = transactionInfoDAO.getTransactionInfoByStatus(TransactionStatus.CONSOLIDATION.name(), numOfThread);
                transactionInfos.forEach(queue::addToQueue);
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
            if (queue.size() == 0) {
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
