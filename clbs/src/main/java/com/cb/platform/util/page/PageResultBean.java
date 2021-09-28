package com.cb.platform.util.page;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.talkback.util.common.AssembleFunction;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页表格bean
 * @author Administrator
 */
@Data
public class PageResultBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<?> items;

    private ApiPageInfo pageInfo;

    public PageResultBean(List<?> resultList) {
        this.pageInfo = new ApiPageInfo(resultList);
        this.items = resultList;
    }

    public PageResultBean() {
    }

    public static PageResultBean getInstance(JSONObject jsonObject, AssembleFunction<List<?>> assembleFunction) {
        PageResultBean pageResultBean = new PageResultBean();
        ApiPageInfo pageInfo = JSONObject.parseObject(jsonObject.getString("pageInfo"), ApiPageInfo.class);
        pageResultBean.pageInfo = pageInfo;
        if (assembleFunction != null) {
            pageResultBean.items = assembleFunction.asssemble(jsonObject.getString("items"));
        } else {
            pageResultBean.items = jsonObject.getJSONArray("items");
        }
        return pageResultBean;
    }

    public static PageResultBean getInstanceSingle(JSONObject jsonObject, AssembleFunction assembleFunction) {
        PageResultBean pageResultBean = new PageResultBean();
        ApiPageInfo pageInfo = JSONObject.parseObject(jsonObject.getString("pageInfo"), ApiPageInfo.class);
        pageResultBean.pageInfo = pageInfo;
        JSONArray items = jsonObject.getJSONArray("items");
        if (assembleFunction != null) {
            for (int i = 0, len = items.size(); i < len; i++) {
                items.set(i, assembleFunction.asssemble(items.getString(i)));
            }
        }
        pageResultBean.items = jsonObject.getJSONArray("items");

        return pageResultBean;
    }

    public PageResultBean(List<?> pageResult, ApiPageInfo pageInfo) {
        this.items = pageResult;
        this.pageInfo = pageInfo;
    }

}
