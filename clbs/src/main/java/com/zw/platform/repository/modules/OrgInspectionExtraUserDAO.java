package com.zw.platform.repository.modules;

import com.zw.platform.domain.connectionparamsset_809.OrgInspectionExtraUserDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 企业查岗额外接收人DAO
 */
public interface OrgInspectionExtraUserDAO {

    void saveAll(Collection<OrgInspectionExtraUserDO> entities);

    Set<String> listUsernameByOrgId(String orgId);

    List<OrgInspectionExtraUserDO> listByUsernameIn(Collection<String> usernames);

    int deleteByOrgId(String orgId);

    int deleteByUsernameIn(Collection<String> usernames);
}
