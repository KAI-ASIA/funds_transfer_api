package com.kaiasia.app.service.fundstransfer.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ConsolidationFTJob {
    @Autowired
    private ConsolidationFTTask task;
    @Value("${job.consolidation.numOfThread}")
    private int numOfThread;
    @Value("${job.consolidation.period}")
    private int period;

    @PostConstruct
    public void init() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(numOfThread);
        for (int i = 0; i < numOfThread; i++) {
            executorService.scheduleAtFixedRate(task, 0, period, TimeUnit.SECONDS);
        }
    }

}
