package com.roger.urbanlifestyle.utils;

public interface Ilock {

    boolean trylock(long timeoutSec);

    void unlock();
}
