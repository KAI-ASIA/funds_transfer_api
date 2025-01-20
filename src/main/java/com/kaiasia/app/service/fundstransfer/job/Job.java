package com.kaiasia.app.service.fundstransfer.job;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@AllArgsConstructor
//@Component
public class Job implements InitializingBean {
    private int numberOfThread = 5;

    @Override
    public void afterPropertiesSet() throws Exception {
       ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfThread);

       for (int i = 0; i < numberOfThread; i++) {
           executor.scheduleAtFixedRate(new Task(), 0, 10, TimeUnit.MILLISECONDS);
       }
    }
}
