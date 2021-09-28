package com.cb.platform.util;

import java.util.ArrayList;
import java.util.List;

public class StringToList {

    public static final List<String> stringToList(String str){

        List<String> list = new ArrayList<>(20);
        if(str.contains(",")){
            String ss[] = str.split(",");
            for(String s : ss){
                list.add(s);
            }
        }else{
            list.add(str);
        }
        return list;
    }

    public static final String[] listToStringArray(List<String> list){
        if(list==null||list.size()==0){
            return null;
        }
        String [] str = new String[list.size()];
        for(int i = 0;i<list.size();i++){
            str[i] = list.get(i);
        }
        return str;
    }

}
