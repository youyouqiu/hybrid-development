package com.zw.platform.util.common;

import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.MagicNumbers;
import com.zw.platform.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 基本查询属性
 */
public abstract class BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * dataTable用,保证异步请求的返回值是同一次请求的
     */
    @Getter
    @Setter
    private Integer draw = 1;

    @Getter
    @Setter
    private String queryType;

    @Getter
    @Setter
    private Integer exportType;

    @Getter
    @Setter
    private String simpleQueryParam;

    @Getter
    @Setter
    private Boolean queryByPage = false;

    /**
     * 开始记录
     */
    private Long start = 0L;

    /**
     * 数量, datatTable用
     */
    @Getter
    private Long length = MagicNumbers.LONG_TWENTY;

    /**
     * 数量
     */
    @Getter
    @Setter
    private Long limit = MagicNumbers.LONG_TWENTY;

    /**
     * 结束记录
     */
    @Getter
    @Setter
    private Long end;

    /**
     * 当前页数
     */
    @Getter
    @Setter
    private Long page = 1L;

    /**
     * 当前请求路径
     */
    @Getter
    @Setter
    private String requestUrl;

    @Getter
    private List<SqlSortBean> sorters;

    /**
     * 拼接好的排序sql
     */
    @Getter
    private String sortCondition;

    @Getter
    private List<SqlGroupBean> groupers;

    /**
     * 拼接好的分组sql
     */
    @Getter
    private String groupCondition;

    @Getter
    private List<SqlFilterBean> filters;

    /**
     * 拼接好的过滤条件sql
     */
    @Getter
    private String filterCondition;

    /**
     * 设置开始页
     * @param startParm 开始页
     */
    public void setStart(Long startParm) {
        if (startParm != null && startParm > 0) {
            this.start = startParm;
            this.page = start / limit + 1;
        }
    }

    /**
     * 设置单页条数
     * @param lengthParm 单页条数
     */
    public void setLength(Long lengthParm) {
        if (lengthParm != null && lengthParm > 0) {
            this.length = lengthParm;
            this.limit = lengthParm;
        }
    }

    /**
     * 设置起始页
     * @return 起始页码
     */
    public Long getStart() {
        if (start != null && start > 0) {
            return start;
        } else if (page != null) {
            return (page - 1) * limit;
        } else {
            return start;
        }
    }

    /**
     * 设置排序字段
     * @param sortStr 排序字段
     */
    public final void setSort(final Object sortStr) {
        sorters = JsonUtil.json2List(sortStr.toString(), SqlSortBean.class);
        SqlSortBean sort;
        if (sorters != null && sorters.size() > 0) {
            sortCondition = "";
            for (int i = 0, length = sorters.size(); i < length; i++) {
                sort = sorters.get(length - i - 1);
                if (i == 0) {
                    sortCondition += sort.getProperty() + " " + sort.getDirection() + " ";
                } else {
                    sortCondition += ", " + sort.getProperty() + " " + sort.getDirection() + " ";
                }
            }
        }
    }

    /**
     * 设置分组字段
     * @param groupStr 分组字段
     */
    public void setGroup(final Object groupStr) {
        groupers = JsonUtil.json2List(groupStr.toString(), SqlGroupBean.class);
    }

    /**
     * 设置过滤条件
     * @param filterStr 过滤条件
     */
    public void setFilter(final Object filterStr) {
        filters = JsonUtil.json2List(filterStr.toString(), SqlFilterBean.class);
        SqlFilterBean filter;
        if (filters != null && filters.size() > 0) {
            filterCondition = "";
            for (int i = 0, length = filters.size(); i < length; i++) {
                filter = filters.get(length - i - 1);
                String operator = filter.getOperator();
                String value = filter.getValue().toString();
                if ("in".equals(filter.getOperator())) {
                    if (!StringUtil.isNullOrBlank(value)) {
                        value = "(" + value.substring(1, value.length() - 1) + ")";
                    }
                } else if ("lt".equals(filter.getOperator())) {
                    operator = "<";
                    value = "'" + value + "'";
                } else if ("gt".equals(filter.getOperator())) {
                    operator = ">";
                    value = "'" + value + "'";
                } else if ("eq".equals(filter.getOperator())) {
                    operator = "=";
                    value = "'" + value + "'";
                } else if ("like".equals(filter.getOperator())) {
                    if (value != null) {
                        value = "'%" + value + "%'";
                    }
                }
                if (i == 0) {
                    filterCondition += filter.getProperty() + " " + operator + " " + value + " ";
                } else {
                    filterCondition += " AND " + filter.getProperty() + " " + operator + " " + value + " ";
                }
            }
        }
    }

    @Getter
    @Setter
    private long maxRecorder = 5000L;

    public void setSorters(final List<SqlSortBean> sortersParm) {
        this.sorters = sortersParm;
    }

    public void setGroupers(final List<SqlGroupBean> groupersParm) {
        this.groupers = groupersParm;
    }

    public void setFilters(List<SqlFilterBean> filters) {
        this.filters = filters;
    }

}
