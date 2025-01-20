package com.kaiasia.app.service.fundstransfer.service;

import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.exception.UpdateFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncTask {
    private final ITransactionInfoDAO transactionInfoDAO;

    @Async("asyncExecutor")
    public void asyncUpdateTransaction(String transactionId, Map<String, Object> params) {
        try {
            transactionInfoDAO.update(transactionId, params);
            log.warn("Transaction updated successfully: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to update transaction: {}", transactionId, e);
            throw new UpdateFailedException(e);
        }
    }
}
