package com.zw.platform.repository.modules;

import com.zw.platform.domain.param.form.CommandParametersForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommandParametersDao {

    List<CommandParametersForm> findByVehicleIds(@Param("ids") List<String> ids, @Param("commandType") String commandType);
}
