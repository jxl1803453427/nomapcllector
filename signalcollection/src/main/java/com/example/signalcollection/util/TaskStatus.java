package com.example.signalcollection.util;

import java.util.HashMap;

/**
 * 任务状态
 * Created by Konmin on 2016/10/14.
 */

public class TaskStatus {

    private HashMap<Integer, String> statusMap;


    public TaskStatus() {
        statusMap = new HashMap<>();
        statusMap.put(0, "未开始");
        statusMap.put(1, "进行中");
        statusMap.put(2, "待审核");
        //statusMap.put(3, "审核通过");

        //statusMap.put(4, "审核被驳回并重新做任务");
        //statusMap.put(5, "审核不通过并结束任务");
        statusMap.put(21, "审核不通过");
        statusMap.put(103, "待审批");
        statusMap.put(1031, "审批不通过");
        statusMap.put(104, "待结算");
        statusMap.put(105, "待结算");
        statusMap.put(1045, "待结算");
        statusMap.put(106, "已结算");
        statusMap.put(107, "已结算");
        statusMap.put(1067, "已结算");


    }


    public String getStatusMsg(Integer statusCode) {


        return statusMap.get(statusCode);
    }


}
