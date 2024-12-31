package com.kaiasia.app.service.fundstransfer.dao;

import com.kaiasia.app.service.fundstransfer.model.TransactionInfo;

public interface ITransactionInfoDAO {
     int insert(TransactionInfo transactionInfo) throws Exception;
}
