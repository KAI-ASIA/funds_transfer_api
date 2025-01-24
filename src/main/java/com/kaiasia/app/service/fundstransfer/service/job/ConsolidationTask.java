package com.kaiasia.app.service.fundstransfer.service.job;

import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.job.ConsolidationFTQueue;
import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.model.enums.TransactionStatus;
import com.kaiasia.app.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiError;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.t24util.T24FTExistsResponse;
import ms.apiclient.t24util.T24FTRevertResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class ConsolidationTask implements Runnable {
    private final ITransactionInfoDAO transactionInfoDAO;
    private final T24UtilClient t24UtilClient;
    private final ConsolidationQueue queueJob;

    public ConsolidationTask(ITransactionInfoDAO transactionInfoDAO, T24UtilClient t24UtilClient, ConsolidationQueue queueJob) {
        this.transactionInfoDAO = transactionInfoDAO;
        this.t24UtilClient = t24UtilClient;
        this.queueJob = queueJob;
    }

    @Value("${kai.name}")
    private String requestApi;

    @Override
    public void run() {

        //Check xem trong queqe co gi khong
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

        //Neu khong co thi dung thread
        if (transactionInfo == null) {
            return;
        }

        //Neu co thi truy van thong tin cua giao dich can xu li
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
        log.info("{}: T24Response {}", location, response.getResponseCode());
        HashMap<String, Object> params = new HashMap<>();

        //Neu ft tra ra khong ton tai
        if ("02".equals(response.getResponseCode())){
            log.info("{}: T24Response code 02(null)", location);
            try {
                params.put("status", TransactionStatus.ERROR.name());
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
            }
            return;
        }

        //Neu ft tra ra la revert
        if ("01".equals(response.getResponseCode())){
            log.info("{}: T24Response code 01(revert)", location);
            try {
                params.put("status", TransactionStatus.REVERT.name());
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
            }
            return;
        }

        //Kiem tra xem co la chuyen tien noi bo khong
        if ("300".equals(transactionInfo.getBankCode())){
            log.info("{}: T24Response bankCode 300", location);
            try {
                params.put("status", TransactionStatus.DONE.name());
                transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
            } catch (Exception e) {
                log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
            }
            return;
        }

        //Neu khong phai la chuyen tien noi bo thi revert no
        T24FTRevertResponse revertResponse = t24UtilClient.revertFT(location, T24Request.builder().build(), apiHeader);
        if (!ApiError.OK_CODE.equals(revertResponse.getError().getCode())) {
            log.error("{} : Call T24Api failed : {}", location, error);
            return;
        }

        // Neu revert trong T24 thanh cong thi update trong database
        try {
            params.put("status", TransactionStatus.REVERT.name());
            transactionInfoDAO.update(transactionInfo.getTransactionId(), params);
        } catch (Exception e) {
            log.error("{}: Error updating transaction info - {}", location, transactionInfo.getTransactionId(), e);
        }
    }
}
