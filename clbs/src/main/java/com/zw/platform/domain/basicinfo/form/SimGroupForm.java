package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Title: 设备组织关联表</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Fan Lu
 * @date 2016年9月01日下午16：56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SimGroupForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String simcardId;
    private String groupId;

    public static SimGroupForm of(String simCardId, String orgId, String userName) {
        SimGroupForm form = new SimGroupForm();
        form.setSimcardId(simCardId);
        form.setGroupId(orgId);
        form.setFlag(1);
        form.setCreateDataUsername(userName);
        form.setCreateDataTime(new Date());
        return form;
    }

}