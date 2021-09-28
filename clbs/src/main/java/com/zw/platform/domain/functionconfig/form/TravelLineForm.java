package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 行驶路线Form
 * @author tangshunyu
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TravelLineForm extends BaseFormBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty(message = "【线路名称】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    @Size(max = 20, message = "【线路名称】长度不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "name")
	private String name;	//行驶路线名称
	
	@ExcelField(title = "type")
	private String type;		//路线类型
	
	@NotEmpty(message = "【偏移量名称】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    //@Size(max = 20, message = "【偏移量名称】长度不能超过20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "excursion")
	private Integer excursion;		//路线偏移量
	
	@Size(max = 100, message = "【描述】长度不能超过100个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "description")
	private String description;		//描述
	
	@NotEmpty(message = "【起点终点经度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String startToEndLng;
	
	@NotEmpty(message = "【起点终点纬度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String startToEndLat;
	
	@ExcelField(title = "startLongitude")
	private Double startLongitude;	//开始位置经度
	
	@ExcelField(title = "startLatitude")
	private Double startLatitude;	//开始位置纬度
	
	@ExcelField(title = "endLongitude")
	private Double endLongitude;	//结束位置经度
	
	@ExcelField(title = "endLatitude")
	private Double endLatitude;		//结束位置纬度
	
	private String groupId; // 所属企业
	
	/**
	 * 新增或者修改路线标识：0-新增，1-修改
	 */
	@NotEmpty(message = "【新增或修改路线标识】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
	@Pattern(message = "【新增或修改路线标识】填值错误！",regexp = "^[0-1]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	private String addOrUpdateTravelFlag = "0";
	
	/**
	 * 被修改行驶路线的id
	 */
	private String travelLineId = "";
	
    /**
     * 途经点经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【途经点经度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String wayPointLng;
    
    /**
     * 途经点纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【途经点纬度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String wayPointLat;

    /**
     * 所有点经度集合-和相应的纬度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【途经点经度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String allPointLng;
    /**
     * 所有点纬度集合-和相应的经度一一对应(点1,点2,点3,点4......)
     */
    @NotEmpty(message = "【途经点纬度集合】不能为空！", groups = { ValidGroupAdd.class,ValidGroupUpdate.class  })
    private String allPointLat;

}
