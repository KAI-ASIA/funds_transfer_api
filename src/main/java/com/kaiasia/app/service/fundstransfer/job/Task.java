package com.kaiasia.app.service.fundstransfer.job;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Task implements Runnable {
    @Override
    public void run() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd:HH:mm:ss");
            log.error("{}:{}", "Start task: ", sdf.format(new Date()));
            log.error("{}:{}", "End task: ", sdf.format(new Date()));
        } catch (Exception e) {
            log.error("{}:{}", "Task exception", e);
        }
    }
}
