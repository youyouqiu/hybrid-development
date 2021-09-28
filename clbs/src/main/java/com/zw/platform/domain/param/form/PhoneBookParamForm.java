package com.zw.platform.domain.param.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * Created by FanLu on 2017/4/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PhoneBookParamForm {
	private Integer pOperationType;//0：删除终端上所有存储的联系人；1：表示更新电话本（删除终端中已有全部联系人并追加消息中的联系人）；2：表示追加电话本；3：表示修改电话本（以联系人为索引）
	private String phoneBookId[];//电话本ID
	private String contact[];// 联系人
	private String phoneNo[];// 电话号码
	private Integer callType[];// 呼叫类型:1：呼入；2：呼出；3：呼入/呼出
}
