package com.sean.devbugssummary.lock.model;


public class Data {

    private static int counter = 0;

    private static Object lock = new Object();

    public static int reset() {
        counter = 0;
        return counter;
    }
    public static int getCounter() {
        return counter;
    }

    public synchronized void wrong() {
        counter ++;
    }

    public static void right() {
        synchronized (lock) {
            counter ++;
        }
    }
}
