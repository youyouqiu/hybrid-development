package com.zw.platform.basic.dto;

import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Getter;
import lombok.Setter;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;

import javax.naming.Name;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

/**
 * 组织新增/更新入参
 *
 * @author Zhang Yanhui
 * @since 2021/3/4 15:17
 */

@Getter
@Setter
public final class OrganizationUpsertDTO {

    private static final long serialVersionUID = 1L;

    public OrganizationUpsertDTO() {
    }

    public OrganizationUpsertDTO(OrganizationLdap ldap) {
        BeanUtils.copyProperties(ldap, this);
    }

    public OrganizationLdap toLdapDO() {
        final OrganizationLdap ldap = new OrganizationLdap();
        BeanUtils.copyProperties(this, ldap);
        return ldap;
    }

    private List<String> extraInspectionReceivers;

    private Name id;

    private String uuid;

    private String ou;

    @Size(max = 20, message = "【组织机构代码】不能超过10个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String organizationCode;

    @Size(max = 20, message = "【经营许可证号】不能超过13个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String license;

    private String registerDate;

    /**
     * 行业类别
     */
    private String operation;

    @Size(max = 50, message = "【地址】不能超过50个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String address;

    private String description;

    @Size(max = 20, message = "【负责人】不能超过20个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String principal;

    private String phone;

    @NotEmpty(message = "【组织名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 25, message = "【组织名称】不能超过25个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String name;


    /**
     * 经营范围
     */
    private String scopeOfOperation;

    /**
     * 经营范围Id
     */
    private String scopeOfOperationIds;

    /**
     * 经营范围代码
     */
    private String scopeOfOperationCodes;

    /**
     * 发证机构
     */
    private String issuingOrgan;

    /**
     * 经营状态
     */
    private String operatingState;

    /**
     * 省
     */
    private String provinceName;

    /**
     * 县
     */
    private String countyName;

    /**
     * 市
     */
    private String cityName;
    /**
     * 行政区划代码
     */
    private String areaNumber;

    private String isArea;

    private String cityCode;

    private String countyCode;

    /**
     * 许可证有效期起
     */
    private String licenseValidityStartDate;

    /**
     * 许可证有效期止
     */
    private String licenseValidityEndDate;

    /**
     *  联系人
     */
    private String contactName;

    /**
     * 许可证有效期止
     */
    private String upOrganizationCode;

    /**
     * 管理者组织机构代码
     */
    private String managerOrganizationCode;

    /**
     * 经营许可证字别
     */
    private String businessLicenseType;
    /**
     * 创建时间
     */
    private String createTimestamp;

    /**
     * 法人电话
     */
    private String principalPhone;

    private String cid;

    private String pid;

    private String entryDN;

    public String getAreaName() {
        return StringUtil.getBlankIfNull(provinceName) + " "
                + StringUtil.getBlankIfNull(cityName) + " " + StringUtil.getBlankIfNull(countyName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrganizationUpsertDTO that = (OrganizationUpsertDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid);
    }
}
