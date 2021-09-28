package com.zw.platform.domain.param.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * Created by FanLu on 2017/4/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class EventSetParamForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer eOperationType;//0：删除终端现有所有事件，该命令后不带后继字节；1：更新事件；2：追加事件；3：修改事件；4：删除特定几项事件，之后事件项中无需带事件内容
	private Integer[] eventId;//事件id
	private String[] eventContent;//事件内容
}
