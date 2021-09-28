package com.zw.ws.entity.t808.oil;

import lombok.Data;

@Data
public class TransmissionSettingParam {
	 private static final long serialVersionUID = 1L;
//	 /**
//	  * 外设id
//	  */
//	 private Integer sensorId;
//	 
//	 /**
//	  *  数据长度
//	  */
//	 private Integer dataLen;
	 
	 /**
	  * 外设地址
	  */
	 private Integer sensorAddress;
	 
	/**
	 * 波特率
	 */
	private Integer baudRate = 3;
	
	/**
	 * 奇偶校验,奇偶校验位：1-奇校验；2-偶校验；3-无校验（缺省值）；
	 */
	private Integer parity = 3;
	
	/**
	 *  保留项
	 */
	private byte[] reservedItem = new byte[2];
}
