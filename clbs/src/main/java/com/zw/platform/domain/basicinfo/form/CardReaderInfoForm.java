package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 读卡器信息form
 * <p>Title: CardReaderInfoForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年7月25日上午10:37:27
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CardReaderInfoForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 读卡器编号
     */
    @NotEmpty(message = "【读卡器编号】不能为空！", groups = { ValidGroupAdd.class })
    @Size(min = 5, max = 30, message = "【姓名】最少2个字符，最大30个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "读卡器编号")
    private String cardReaderNumber;
    /**
     * 读卡器类型
     */
    @ExcelField(title = "读卡器类型")
	private String cardReaderType = "";
    /**
	 * 启停状态
	 */
	@ExcelField(title = "启停状态")
	private String isStart;
	/**
	 * 设备厂商
	 */
    @ExcelField(title = "读卡器厂商")
	private String manuFacturer = "";
	/**
	 * 出厂时间
	 */
	@ExcelField(title = "出厂时间")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	private String factoryDate;
	
	/**
	 * 描述
	 */
	@ExcelField(title = "描述")
	private String description = "";
}
