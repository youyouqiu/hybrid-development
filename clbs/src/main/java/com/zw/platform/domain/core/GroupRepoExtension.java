package com.zw.platform.domain.core;

import javax.naming.Name;
import java.util.List;

public interface GroupRepoExtension {
    List<String> getAllGroupNames();
    void create(Group group);
    public void addMemberToGroup(String groupName, Name user);
    public void removeMemberFromGroup(String groupName, Name user);
}
