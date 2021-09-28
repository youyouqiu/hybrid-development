package com.zw.platform.service.oilsubsidy.impl;

import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oilsubsidy.station.StationDO;
import com.zw.platform.domain.oilsubsidy.station.StationDTO;
import com.zw.platform.domain.oilsubsidy.station.StationQuery;
import com.zw.platform.repository.oilsubsidy.StationManageDao;
import com.zw.platform.service.oilsubsidy.StationManageService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 站点管理逻辑实现类
 *
 * @author zhangjuan
 */
@Service
public class StationManageServiceImpl implements StationManageService {

    @Autowired
    private StationManageDao stationManageDao;

    @Autowired
    private LogSearchService logSearchService;

    @Override
    public boolean add(StationDTO stationDTO) throws BusinessException {
        if (checkNumRepeat(null, stationDTO.getNumber())) {
            throw new BusinessException("", "站点编号已经存在!");
        }

        //封装站点插入数据库实体
        stationDTO.setId(UUID.randomUUID().toString());
        StationDO stationDO = new StationDO();
        BeanUtils.copyProperties(stationDTO, stationDO);
        stationDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        stationDO.setFlag(1);

        //插入数据库
        boolean isSuccess = stationManageDao.insert(stationDO);
        if (!isSuccess) {
            throw new BusinessException("", "添加失败!");
        }
        //记录操作日志
        String message = "站点管理：新增站点（" + stationDTO.getName() + "）";
        logSearchService.addLog(getIpAddress(), message, "3", "站点管理");

        return true;
    }

    @Override
    public boolean update(StationDTO stationDTO) throws BusinessException {
        String id = stationDTO.getId();
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("", "修改时id不能为空!");
        }

        StationDO oldStation = stationManageDao.getById(id);
        if (Objects.isNull(oldStation)) {
            throw new BusinessException("", "站点信息不存在!");
        }
        //校验站点编号是否重复
        if (checkNumRepeat(stationDTO.getId(), stationDTO.getNumber())) {
            throw new BusinessException("", "站点编号已经存在!");
        }

        //封装站点插入数据库实体
        StationDO stationDO = new StationDO();
        BeanUtils.copyProperties(stationDTO, stationDO);
        stationDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        stationDO.setUpdateDataTime(new Date());

        //更新站点信息
        boolean isSuccess = stationManageDao.update(stationDO);
        if (!isSuccess) {
            throw new BusinessException("", "更新失败!");
        }

        //记录操作日志
        String message = "站点管理：修改站点（" + oldStation.getName() + "）";
        if (!Objects.equals(oldStation.getName(), stationDO.getName())) {
            message = message + "为（" + stationDO.getName() + ")";
        }
        logSearchService.addLog(getIpAddress(), message, "3", "站点管理");

        return true;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("", "删除时id不能为空!");
        }

        StationDO station = stationManageDao.getById(id);
        if (Objects.isNull(station)) {
            throw new BusinessException("", "站点信息不存在!");
        }

        //站点是否被使用
        List<StationDO> usedStations = stationManageDao.getUsedByIds(Collections.singletonList(id));
        if (!usedStations.isEmpty()) {
            throw new BusinessException("", station.getName() + "站，已经使用，不能删除哦！");
        }

        StationDO stationDO = new StationDO();
        stationDO.setId(id);
        stationDO.setFlag(0);
        stationDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());


        //删除站点信息
        boolean isSuccess = stationManageDao.update(stationDO);
        if (!isSuccess) {
            throw new BusinessException("", "删除失败!");
        }


        //记录操作日志
        String message = "站点管理：删除站点（" + station.getName() + "）";
        logSearchService.addLog(getIpAddress(), message, "3", "站点管理");

        return true;
    }

    @Override
    public int delBatch(Collection<String> ids) throws BusinessException {
        if (ids.isEmpty()) {
            return 0;
        }
        //待删除的站点信息
        List<StationDO> stations = stationManageDao.getByIds(ids);
        if (stations.isEmpty()) {
            return 0;
        }

        int count = stationManageDao.deleteBatch(new ArrayList<>(ids));

        //记录操作日志
        Object[] stationNames = stations.stream().map(StationDO::getName).toArray();
        String message = "站点管理：批量删除站点（" + StringUtils.join(stationNames, ",") + "）";
        logSearchService.addLog(getIpAddress(), message, "3", "站点管理");

        return count;
    }

    @Override
    public Page<StationDTO> getListByKeyword(StationQuery query) throws BusinessException {
        return PageHelperUtil.doSelect(query, () -> stationManageDao.getByKeyword(query.getSimpleQueryParam()));
    }

    @Override
    public StationDTO getById(String id) throws BusinessException {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("", "id不能为空!");
        }
        StationDO stationDO = stationManageDao.getById(id);
        if (Objects.isNull(stationDO)) {
            throw new BusinessException("", "站点信息不存在!");
        }

        StationDTO stationDTO = new StationDTO();
        BeanUtils.copyProperties(stationDO, stationDTO);

        return stationDTO;
    }

    @Override
    public boolean checkNumRepeat(String id, String number) {
        StationDO stationDO = stationManageDao.getByNumber(number);
        return !(Objects.isNull(stationDO) || Objects.equals(id, stationDO.getId()));
    }

    @Override
    public List<String> getUsedName(Collection<String> ids) {
        List<StationDO> stations = stationManageDao.getUsedByIds(ids);

        List<String> usedNames = new ArrayList<>();
        for (StationDO stationDO : stations) {
            usedNames.add(stationDO.getName());
            ids.remove(stationDO.getId());
        }
        return usedNames;
    }

    @Override
    public List<StationDTO> getAll() {
        return stationManageDao.getByKeyword(null);
    }
}
