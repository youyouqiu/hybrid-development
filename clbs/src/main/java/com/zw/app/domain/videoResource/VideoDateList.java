package com.zw.app.domain.videoResource;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

/***
 @Author lijie
 @Date 2019/11/22 16:19
 @Description 下发920f的参数实体
 @version 1.0
 **/
@Data
public class VideoDateList extends BaseEntity {

    /* 车id */
    private String vehicleId;

    /* 资源类型 */
    private String videoType = "0";

    /* 视频的日期 */
    private String date;

}
