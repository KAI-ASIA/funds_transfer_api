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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class ConsolidationFTTask implements Runnable {
    @Autowired
    private ITransactionInfoDAO transactionInfoDAO;
    @Autowired
    private T24UtilClient t24UtilClient;
    @Autowired
    private ConsolidationFTQueue queueJob;
    @Value("${kai.name}")
    private String requestApi;

    @Override
    public void run() {
        // query ft with status = consolidation
        String location = "ConsolidationFTTask_" + System.currentTimeMillis();
        log.info("Start Consolidation Job - {}", location);
        TransactionInfo transactionInfo;
        try {
            transactionInfo = queueJob.getFromQueue();
        } catch (Exception e) {
            log.error("{} : Error getting transaction info", location, e);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
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

        HashMap<String, Object> params = new HashMap<>();
        // ft exists but not revert
        if ("00".equals(responseCode)) {
            if ("300".equals(transactionInfo.getBankCode())) {
                params.put("status", TransactionStatus.DONE.name());
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
                return;
            }
            try {
                // TODO: revert - call t24


                // update if success
                params.put("status", TransactionStatus.REVERT.name());
                try {
                    transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
                } catch (Exception e) {
                    log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
            return;
        }

        // ft revert
        if ("01".equals(responseCode)) {
            params.put("status", TransactionStatus.REVERT.name());
            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
            return;
        }

        // ft not exists
        if ("02".equals(responseCode)) {
            params.put("status", TransactionStatus.ERROR.name());
            try {
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
        }
    }
}
