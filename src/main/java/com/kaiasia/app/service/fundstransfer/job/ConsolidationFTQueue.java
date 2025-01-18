package com.kaiasia.app.service.fundstransfer.job;

import com.kaiasia.app.service.fundstransfer.model.entity.TransactionInfo;
import com.kaiasia.app.service.fundstransfer.utils.ObjectAndJsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsolidationFTQueue {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Value("${spring.redis.queueName}")
    private String queueName;

    public Long addToQueue(TransactionInfo transactionInfo) {
        return redisTemplate.opsForList().leftPush(queueName, ObjectAndJsonUtils.toJson(transactionInfo));
    }

    public TransactionInfo getFromQueue() {
        String s = redisTemplate.opsForList().rightPop(queueName);
        if (s == null) return null;
        return ObjectAndJsonUtils.fromJson(s, TransactionInfo.class);
    }

    public Long size() {
        return redisTemplate.opsForList().size(queueName);
    }
}
