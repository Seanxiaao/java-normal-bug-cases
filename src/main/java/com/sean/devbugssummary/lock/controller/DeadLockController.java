package com.sean.devbugssummary.lock.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/deadlock")
public class DeadLockController {

    private final static Logger logger = Logger.getLogger(DeadLockController.class.getName());

    private final Map<String, Item> items = IntStream.rangeClosed(0, 10).boxed().collect(
            Collectors.toMap(i -> "item" + i, val -> new Item("item" + val),
                    (i, val) -> i, HashMap::new)
    );

    static class Item {
        ReentrantLock reentrantLock = new ReentrantLock();
        final String name;
        int remaining = 1000;

        Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", remaining=" + remaining +
                    '}';
        }
    }


    private List<Item> createCart() {
        return IntStream.rangeClosed(1, 3)
                .mapToObj(i -> "item" + ThreadLocalRandom.current().nextInt(items.size()))
                .map(items::get).collect(Collectors.toList());
    }

    private boolean createOrder(List<Item> order) {
        List<ReentrantLock> locks = new ArrayList<>();

        for (Item item: order) {
            try {
                // try to acquire lock in 10 seconds;
                if (item.reentrantLock.tryLock(10, TimeUnit.SECONDS)) {
                    locks.add(item.reentrantLock);
                } else {
                    // this is the logic for tiemout (release all the item lock and cancel the order)
                    locks.forEach(ReentrantLock::unlock);
                    return false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // after acquiring all the lock, modify the store
        try {
            order.forEach(i -> i.remaining --);
        } finally {
            locks.forEach(ReentrantLock::unlock);
        }
        return true;
    }

    @GetMapping("wrong")
    public long wrong() {
        long begin = System.currentTimeMillis();
        logger.info("cart" + items.toString());
        long success = IntStream.rangeClosed(1, 100).parallel().mapToObj(i -> {
            List<Item> cart = createCart();
            // since the order of checking lock is unsorted, this will raise a deadlock problem.
            return createOrder(cart);
        }).filter(result -> result).count();
        logger.info("success:" + success +
                "total Remaining:" + items.entrySet().stream()
                .map(item -> item.getValue().remaining)
                .reduce(0, Integer::sum) +
                "took:" + (System.currentTimeMillis() - begin) +
                "items:" + items);
        return success;
    }

    @GetMapping("right")
    public long right() {
        long begin = System.currentTimeMillis();
        logger.info("cart" + items.toString());
        long success = IntStream.rangeClosed(1, 100).parallel().mapToObj(i -> {
            List<Item> cart = createCart().stream()
                    .sorted(Comparator.comparing(Item::getName))
                    .collect(Collectors.toList());
            return createOrder(cart);
        }).filter(result -> result).count();
        logger.info("success:" + success +
                "total Remaining:" + items.entrySet().stream()
                .map(item -> item.getValue().remaining)
                .reduce(0, Integer::sum) +
                "took:" + (System.currentTimeMillis() - begin) +
                "items:" + items);
        return success;
    }

}
