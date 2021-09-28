package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.dto.platformInspection.Zw809MessageDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

public interface InspectionAndSupervisionService {

    List<Zw809MessageDTO> getList(String groupIds, String type, String startTime, String endTime, Integer status);

    void exportList(String title, HttpServletResponse res) throws Exception;

    /**
     * 查询查岗信息额外接收人（指被查岗企业的上级企业用户）
     * @since 4.4.0
     */
    List<String> getExtraInspectionReceivers(OrganizationLdap org);

    /**
     * 设置查岗信息额外接收人
     * @since 4.4.0
     */
    void setExtraInspectionReceivers(OrganizationLdap org, Collection<String> usernames, OpType opType);

    /**
     * 把额外接收sourceOrg查岗的用户复制给newOrg
     * <p>查询username ↔ orgId时，sourceOrgDn决定<b>右</b>边
     * @param newOrgIds   uuid
     * @param sourceOrgDn orgDn
     * @since 4.4.0
     */
    void batchCopySuperiorReceivers(List<String> newOrgIds, String sourceOrgDn);

    /**
     * 把sourceOrg的查岗额外接收用户复制给newOrg
     * <p>查询username ↔ orgId时，sourceOrgDn决定<b>左</b>边
     * @param newOrgIds   uuid
     * @param sourceOrgDn orgDn
     * @since 4.4.0
     */
    void batchCopySubordinateReceivers(List<String> newOrgIds, String sourceOrgDn);

    void deleteByUsername(Collection<String> usernames);

    /**
     * 组织操作类型
     */
    enum OpType {
        /**
         * 插入和新增分开处理
         */
        INSERT, ADD, DELETE, UPDATE
    }
}
