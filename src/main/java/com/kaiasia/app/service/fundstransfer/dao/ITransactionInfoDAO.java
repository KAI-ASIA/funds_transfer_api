package com.kaiasia.app.service.fundstransfer.dao;

import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;

import java.util.Map;

public interface ITransactionInfoDAO {
     int insert(TransactionInfo transactionInfo) throws Exception;

     int update(String transactionId, Map<String,Object> param) throws Exception;

     boolean checkExistTransactionId(String transactionId) throws Exception;

     TransactionInfo getTransactionInfo(String status) throws Exception;

}
