package com.example.demo;

import org.apache.catalina.Session;

public class Test {
    public static void main(String[] args) {
        ThreadLocal a = new ThreadLocal();
        Session session = (Session) a.get();
    }
}
