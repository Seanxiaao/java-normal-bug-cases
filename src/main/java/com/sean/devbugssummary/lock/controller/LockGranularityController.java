package com.sean.devbugssummary.lock.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/granularity")
public class LockGranularityController {

    private List<Integer> data = new ArrayList<>();

    private static Logger logger = Logger.getLogger(LockGranularityController.class.getName());

    private void slow() {
        try {
            TimeUnit.MICROSECONDS.sleep(10);
        } catch (InterruptedException ignored) {

        }
    }

    @GetMapping("/wrong")
    public int wrong() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            synchronized (this) {
                slow();
                data.add(i);
            }
        });
        logger.info("took time:" + (System.currentTimeMillis() - begin));
        return data.size();
    }

    @GetMapping("/right")
    public int right() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            synchronized (data) {
                slow();
                data.add(i);
            }
        });
        logger.info("took time:" + (System.currentTimeMillis() - begin));
        return data.size();
    }


}
