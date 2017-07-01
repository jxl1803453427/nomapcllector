package com.example.signalcollection.bean;

import java.io.Serializable;

/**
 * 时间总线的消息
 * <p>
 * Created by Konmin on 2017/3/22.
 */

public class EventBusMessage implements Serializable {


    public int what;
    public String tag;
    public Object data;


    public EventBusMessage() {
    }

    public EventBusMessage(int what, Object data) {
        this.what = what;
        this.data = data;
    }


    public EventBusMessage(int what, String tag, Object data) {
        this.what = what;
        this.tag = tag;
        this.data = data;
    }


}
