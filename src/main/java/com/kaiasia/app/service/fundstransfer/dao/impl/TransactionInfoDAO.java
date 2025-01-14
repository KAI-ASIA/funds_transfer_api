package com.kaiasia.app.service.fundstransfer.dao.impl;

import com.kaiasia.app.core.dao.CommonDAO;
import com.kaiasia.app.core.dao.PosgrestDAOHelper;
import com.kaiasia.app.service.fundstransfer.dao.ITransactionInfoDAO;
import com.kaiasia.app.service.fundstransfer.model.TransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        Set<String> keys = param.keySet();
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName)
                                                        .append(" SET ")
                                                        .append(keys.stream()
                                                                    .map(k -> k + " = :" + k)
                                                                    .collect(Collectors.joining(", ")))
                                                        .append(" WHERE transaction_id = :transactionId");
        param.put("transactionId", transactionId);

        return posgrestDAOHelper.update(sql.toString(), param);
    }

    @Override
    public boolean checkExistTransactionId(String transactionId) throws Exception {
        String sql = "SELECT 1 FROM " + tableName + " WHERE transaction_id = :transaction_id LIMIT 1";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("transaction_id", transactionId);

        Integer result =posgrestDAOHelper.querySingle(sql, paramMap, new BeanPropertyRowMapper<>(Integer.class));

        return result != null;
    }


}
