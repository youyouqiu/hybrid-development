package com.zw.platform.domain.leaderboard;

import lombok.Data;


@Data
public class EnterpriseLdapInfo {
    private String gid;//企业id

    private String provinceCode;//省code

    private String cityCode;//市code

    private String countyCode;//县code

    private String ldapAddress;//省市区县的名称

    private String name;//企业名称
}
