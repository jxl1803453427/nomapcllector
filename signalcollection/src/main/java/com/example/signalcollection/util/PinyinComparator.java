package com.example.signalcollection.util;

import com.example.signalcollection.bean.CityList;

import java.util.Comparator;


public class PinyinComparator implements Comparator<CityList.DataBean> {

    public int compare(CityList.DataBean o1, CityList.DataBean o2) {
        if (o1.getSortLetter().equals("@")
                || o2.getSortLetter().equals("#")) {
            return -1;
        } else if (o1.getSortLetter().equals("#")
                || o2.getSortLetter().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetter().compareTo(o2.getSortLetter());
        }
    }

}
