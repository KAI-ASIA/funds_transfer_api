package com.kaiasia.app.service.fundstransfer.dao.impl;

import com.kaiasia.app.core.dao.CommonDAO;
import com.kaiasia.app.core.dao.PosgrestDAOHelper;
import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.model.TransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TransactionInfoDAO implements ITransactionInfoDAO {
    @Value("${dbinfo.transactionInfo}")
    private String tableName;
    @Autowired
    private PosgrestDAOHelper posgrestDAOHelper;

    @Override
    public int insert(TransactionInfo transactionInfo) throws Exception {
        String sql = "insert into " + tableName + "(transaction_id,customer_id,approval_method,otp,response_code,response_str,status,bank_trans_id,insert_time) " +
                "values(:transaction_id, :customer_id, :approval_method, :otp, :response_code, :response_str, :status,:bank_trans_id, :insert_time)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transaction_id", transactionInfo.getTransactionId());
        params.put("customer_id", transactionInfo.getCustomerId());
        params.put("approval_method", transactionInfo.getApprovalMethod());
        params.put("otp", transactionInfo.getOtp());
        params.put("response_code", transactionInfo.getResponseCode());
        params.put("response_str", transactionInfo.getResponseStr());
        params.put("status", transactionInfo.getStatus());
        params.put("bank_trans_id", transactionInfo.getBankTransId());
        params.put("insert_time", transactionInfo.getInsertTime());
        return posgrestDAOHelper.update(sql, params);
    }

    @Override
    public int update(String transactionId, Map<String, Object> param) throws Exception {
        Set<String> keys = param.keySet();
        StringBuilder sql = new StringBuilder("update " + tableName + " set ");
        sql.append(keys.stream().map(k -> k + "=" + ":" + k).collect(Collectors.joining()));
        sql.append(" where transaction_id = ").append(transactionId);
        return posgrestDAOHelper.update(sql.toString(), param);
    }
}
