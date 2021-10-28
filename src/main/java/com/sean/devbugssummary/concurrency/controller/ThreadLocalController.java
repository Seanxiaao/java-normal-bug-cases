package com.sean.devbugssummary.concurrency.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/threadlocal")
public class ThreadLocalController {

    // tomcat itself is also a thread, if did not remove, it may store the past data

    private static final ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);

    @GetMapping("/wrong")
    public String wrong(@RequestParam("userId") Integer usrId) {
        String before = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(usrId);
        String after = Thread.currentThread().getName() + ":" + currentUser.get();
        Map<String, String> map = new HashMap<>();
        map.put("before", before);
        map.put("after", after);
        return map.toString();
    }

    @GetMapping("/right")
    public String right(@RequestParam("userId") Integer usrId) {
        String before = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(usrId);
        try {
            String after = Thread.currentThread().getName() + ":" + currentUser.get();
            Map<String, String> map = new HashMap<>();
            map.put("before", before);
            map.put("after", after);
            return map.toString();
        } finally {
            currentUser.remove();
        }
    }
}
