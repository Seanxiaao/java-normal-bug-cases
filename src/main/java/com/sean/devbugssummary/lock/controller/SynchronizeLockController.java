package com.sean.devbugssummary.lock.controller;

import com.sean.devbugssummary.lock.model.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@RestController
@RequestMapping("/lock")
public class SynchronizeLockController {

    @GetMapping("wrong")
    public int wrong(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        // when locking on non static method, we cannot assure that multithread will not execute
        // the same method in different instance.
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new Data().wrong());
        return Data.getCounter();
    }

    @GetMapping("right")
    public int right(@RequestParam(value = "right", defaultValue = "1000000") int count) {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> Data.right());
        return Data.getCounter();
    }

}
