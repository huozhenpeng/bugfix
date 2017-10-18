package com.example.huozhenpeng.andfixdemo;

/**
 * Created by huozhenpeng on 2017/10/17.
 */

public class Patch {

    @Replace(clazz = "com.example.huozhenpeng.andfixdemo.MainActivity",method = "caculate")
    private int  caculate() {
        return 1/1;
    }
}
