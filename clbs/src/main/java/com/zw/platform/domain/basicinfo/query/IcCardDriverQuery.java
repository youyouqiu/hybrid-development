package com.zw.platform.domain.basicinfo.query;

import lombok.Data;

import java.util.Objects;

/***
 @Author zhengjc
 @Date 2019/12/26 10:28
 @Description 司机名称和驾驶员ic卡
 @version 1.0
 **/
@Data
public class IcCardDriverQuery {
    private String name;
    /**
     * 4.4.0调整（名字和身份证号是唯一标识）
     * 身份证号
     */
    private String cardNumber;

    public static IcCardDriverQuery getInstance(String cardNumber, String name) {
        IcCardDriverQuery query = new IcCardDriverQuery();
        query.setName(name);
        query.setCardNumber(cardNumber);
        return query;
    }

    /**
     * 获取ic卡和从业人员名称查询实体
     * @param driverNameCardNumber
     * @return
     */
    public static IcCardDriverQuery getInstance(String driverNameCardNumber) {
        String[] cardNumberAndName = driverNameCardNumber.split("_");
        IcCardDriverQuery icCardDriverQuery = new IcCardDriverQuery();
        icCardDriverQuery.setCardNumber(cardNumberAndName[0]);
        icCardDriverQuery.setName(cardNumberAndName[1]);
        return icCardDriverQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IcCardDriverQuery query = (IcCardDriverQuery) o;
        return name.equals(query.name) && cardNumber.equals(query.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cardNumber);
    }
}
