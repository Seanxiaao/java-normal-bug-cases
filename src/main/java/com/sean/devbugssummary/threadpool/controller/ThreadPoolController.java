package com.sean.devbugssummary.threadpool.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/threadpool")
public class ThreadPoolController {

    private final static Logger logger = Logger.getLogger(ThreadPoolController.class.getName());

    private void printStats(ThreadPoolExecutor threadPoolExecutor) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            logger.info("=====");
            logger.info("Pool size" + threadPoolExecutor.getPoolSize());
            logger.info("Active Threads:" + threadPoolExecutor.getActiveCount());
            logger.info("Number of Tasks Completed: " + threadPoolExecutor.getCompletedTaskCount());
            logger.info("Number of tasks in Queue:" + threadPoolExecutor.getQueue().size());
            logger.info("=====");
        }, 0, 1, TimeUnit.SECONDS);
    }

    @GetMapping("/oom1")
    public void oom1() throws InterruptedException {
        // newFixedThreadPool is creating a linkedBlockingQueue with Integer.MAX_VALUE
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        for (int i = 0; i < 100000000; i++) {
            threadPoolExecutor.execute(() -> {
                String payload = IntStream.rangeClosed(1, 1000000).mapToObj(x -> "a")
                        .collect(Collectors.joining("")) + UUID.randomUUID().toString();
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info(payload);
            });
        }
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
    }

    @GetMapping("/oom2")
    public void oom2() throws InterruptedException {
        // newFixedThreadPool is creating a linkedBlockingQueue with Integer.MAX_VALUE
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 0; i < 100000000; i++) {
            threadPoolExecutor.execute(() -> {
                String payload = IntStream.rangeClosed(1, 1000000).mapToObj(x -> "a")
                        .collect(Collectors.joining("")) + UUID.randomUUID().toString();
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info(payload);
            });
        }
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
    }

    @GetMapping("right")
    public int right() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2, 5, 5, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadFactoryBuilder().setNameFormat("demo-threadpool-%d").build(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        printStats(threadPoolExecutor);
        IntStream.rangeClosed(1, 20).forEach(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int id = atomicInteger.incrementAndGet();
            try {
                threadPoolExecutor.submit(() -> {
                    logger.info(id + "started");
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    logger.info(id + "finished");
                });
            } catch (Exception e) {
                logger.warning("error submitting task" + id + e.getMessage());
                atomicInteger.decrementAndGet();
            }
        });
        TimeUnit.SECONDS.sleep(60);
        return atomicInteger.intValue();
    }

    // create a flexible thread pool


}
