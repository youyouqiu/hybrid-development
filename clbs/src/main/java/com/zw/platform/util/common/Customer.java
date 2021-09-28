package com.zw.platform.util.common;

import java.text.DecimalFormat;

/**
 * 自增序列号
 *
 * @author  Tdz
 * @create 2017-05-03 9:48
 **/
public class Customer {
    private static int totalCount = 0;
    private int customerID;
    public Customer(){
        ++totalCount;
        customerID = totalCount;
    }
    public String getCustomerID() {
        DecimalFormat decimalFormat = new DecimalFormat("0000");
        return decimalFormat.format(customerID);
    }

}
