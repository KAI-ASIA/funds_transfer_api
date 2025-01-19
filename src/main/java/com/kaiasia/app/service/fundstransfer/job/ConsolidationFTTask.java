package com.kaiasia.app.service.fundstransfer.job;

import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.t24util.T24FTExistsResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class ConsolidationFTTask implements Runnable {
    private final ITransactionInfoDAO transactionInfoDAO;
    private final T24UtilClient t24UtilClient;
    private final ConsolidationFTQueue queueJob;

    public ConsolidationFTTask(ITransactionInfoDAO transactionInfoDAO, T24UtilClient t24UtilClient, ConsolidationFTQueue queueJob) {
        this.transactionInfoDAO = transactionInfoDAO;
        this.t24UtilClient = t24UtilClient;
        this.queueJob = queueJob;
    }

    @Value("${kai.name}")
    private String requestApi;

    @Override
    public void run() {
        // query ft with status = consolidation
        String location = "ConsolidationFTTask_" + System.currentTimeMillis();
        log.info("Start Consolidation Task - {}", location);
        TransactionInfo transactionInfo = null;
        try {
            transactionInfo = queueJob.getFromQueue();
        } catch (Exception e) {
            log.error("{} : Error getting transaction from queue", location, e);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Thread.currentThread().interrupt();
        }

        if (transactionInfo == null) {
            return;
        }

        // call t24 check ft exists
        ApiHeader apiHeader = new ApiHeader();
        apiHeader.setReqType("REQUEST");
        apiHeader.setPriority(1);
        apiHeader.setChannel("API");
        apiHeader.setLocation(ApiUtils.getCurrentHostName());
        apiHeader.setRequestAPI(requestApi);
        apiHeader.setRequestNode(ApiUtils.getCurrentHostName());
        T24FTExistsResponse response = t24UtilClient.ftExists(location,
                T24Request.builder().transactionId(transactionInfo.getTransactionId()).build(),
                apiHeader);
        ApiError error = response.getError();
        if (!ApiError.OK_CODE.equals(error.getCode())) {
            log.error("{} : Call T24Api failed : {}", location, error);
            return;
        }
        log.info("{}: T24Response {}", location, response);
        String responseCode = response.getResponseCode();

        // ft exists but not revert
        if ("00".equals(responseCode)) {
            if ("300".equals(transactionInfo.getBankCode())) {
                update(location, transactionInfo.getTransactionId(), TransactionStatus.DONE.name());
                return;
            }

            // revert - call t24

            // update if success
            update(location, transactionInfo.getTransactionId(), TransactionStatus.REVERT.name());
            return;
        }

        // ft revert
        if ("01".equals(responseCode)) {
            update(location, transactionInfo.getTransactionId(), TransactionStatus.REVERT.name());
            return;
        }

        // ft not exists
        if ("02".equals(responseCode)) {
            update(location, transactionInfo.getTransactionId(), TransactionStatus.ERROR.name());
        }
    }

    private void update(String location, String transactionId, String status) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", status);
        try {
            transactionInfoDAO.update(transactionId, params);
        } catch (Exception e) {
            log.error("{}: Error updating transaction info - {}", location, transactionId, e);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Thread.currentThread().interrupt();
        }
    }
}
