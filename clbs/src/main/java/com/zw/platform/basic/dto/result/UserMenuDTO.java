package com.zw.platform.basic.dto.result;

import com.zw.platform.basic.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuDTO {
    private UserDTO userInfo;
    private Set<String> menuIds;
}
