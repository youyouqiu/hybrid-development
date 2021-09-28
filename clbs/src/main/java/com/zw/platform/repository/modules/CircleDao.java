package com.zw.platform.repository.modules;

import com.zw.platform.domain.functionconfig.Circle;
import com.zw.platform.domain.functionconfig.form.CircleForm;
import com.zw.platform.domain.functionconfig.form.ManageFenceFrom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Tdz on 2016/8/8.
 */
public interface CircleDao {

    /**
     * 新增圆
     */
    boolean circles(final CircleForm form);
    
    /**
    * 更新圆形区域
    * @return void
    * @author Liubangquan
     */
    boolean updateCircle(final CircleForm form);

    boolean fenceInfo(final ManageFenceFrom fenceForm);
    
    Circle getCircleById(@Param("id") final String id);
    
    /**
     * 根据id list 查询圆形
     * @author wangying
     */
    List<Circle> getCircleByIds(@Param("ids") final List<String> ids);

    
    /**
    * 获取所有的圆形区域信息
    * @return List<Circle>
    * @author Liubangquan
     */
    List<Circle> findAllCircles();

    /**
     * 删除圆id
     * @param fenceId 圆id
     * @return boolean
     */
    boolean deleteCircle(String fenceId);
}
