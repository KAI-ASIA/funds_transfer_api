package com.kaiasia.app.service.fundstransfer.dao;

import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;

import java.util.List;
import java.util.Map;

public interface ITransactionInfoDAO {
     int insert(TransactionInfo transactionInfo) throws Exception;

     int update(String transactionId, Map<String,Object> param) throws Exception;

     boolean checkExistTransactionId(String transactionId) throws Exception;

     List<TransactionInfo> getTransactionInfoByStatus(String status, int limit) throws Exception;

}
