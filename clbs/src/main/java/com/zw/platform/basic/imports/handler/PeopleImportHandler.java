package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.constant.RegexKey;
import com.zw.platform.basic.domain.PeopleBasicDO;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.repository.PeopleDao;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员信息导入处理
 *
 * @author zhangjuan
 */
@Slf4j
public class PeopleImportHandler extends BaseImportHandler {
    private List<PeopleDTO> importList;
    private PeopleDao peopleDao;
    private PeopleService peopleService;
    private List<PeopleDO> peopleList;
    private List<PeopleBasicDO> peopleBasicList;

    public PeopleImportHandler(List<PeopleDTO> importData, PeopleDao peopleDao, PeopleService peopleService) {
        this.importList = importData;
        this.peopleDao = peopleDao;
        this.peopleService = peopleService;
    }

    @Override
    public ImportModule module() {
        return ImportModule.PEOPLE;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[]{ImportTable.ZW_M_PEOPLE_INFO};
    }

    @Override
    public boolean uniqueValid() {
        checkParam();
        Set<String> errors = importList.stream().map(PeopleDTO::getErrorMsg).filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        boolean empty = CollectionUtils.isEmpty(errors);
        if (empty) {
            assemblyData();
            progressBar.setTotalProgress(importList.size() * 3 / 2);
        }
        return empty;
    }

    private void assemblyData() {
        peopleList = new ArrayList<>();
        peopleBasicList = new ArrayList<>();
        for (PeopleDTO people : importList) {
            PeopleDO peopleDO = new PeopleDO(people);
            peopleList.add(peopleDO);
            String id = peopleDO.getId();

            //处理人员技能
            if (StringUtils.isNotBlank(people.getSkillIds())) {
                String[] skillIds = people.getSkillIds().split(",");
                for (String skillId : skillIds) {
                    peopleBasicList.add(new PeopleBasicDO(id, skillId, 1));
                }
            }

            //处理人员驾照类别
            String driverTypeIds = people.getDriverTypeIds();
            if (StringUtils.isNotBlank(driverTypeIds)) {
                String[] driverTypeArr = driverTypeIds.split(",");
                for (String driverTypeId : driverTypeArr) {
                    peopleBasicList.add(new PeopleBasicDO(id, driverTypeId, 2));
                }
            }
        }
    }

    @Override
    public boolean addMysql() {
        partition(peopleList, peopleDao::addByBatch);
        if (CollectionUtils.isNotEmpty(peopleBasicList)) {
            partition(peopleBasicList, peopleDao::addBaseInfo);
        }
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        peopleService.addOrUpdateRedis(importList, new HashSet<>());
        progressBar.addProgress(peopleList.size() / 2);
    }

    private void checkParam() {
        //获取已经存在的人员编号和身份证号
        List<PeopleDO> existPeoples = peopleDao.getAllPeople();
        Set<String> numberSet = new HashSet<>();
        Set<String> identitySet = new HashSet<>();
        for (PeopleDO people : existPeoples) {
            numberSet.add(people.getPeopleNumber());
            if (StringUtils.isNotBlank(people.getIdentity())) {
                identitySet.add(people.getIdentity());
            }
        }

        //进行参数校验
        for (PeopleDTO people : importList) {
            if (StringUtils.isNotBlank(people.getErrorMsg())) {
                continue;
            }
            String peopleNumber = people.getName();
            if (StringUtils.isBlank(peopleNumber)) {
                people.setErrorMsg("人员编号不能为空");
                continue;
            }
            if (!peopleNumber.matches(RegexKey.PEOPLE_NUM_REG)) {
                people.setErrorMsg("人员编号不合法,请输入汉字、字母、数字或短横杠，长度2-20位");
                continue;
            }
            if (numberSet.contains(peopleNumber)) {
                people.setErrorMsg("人员编号已存在");
                continue;
            }

            String name = people.getAlias();
            if (StringUtils.isNotBlank(name) && name.length() > 8) {
                people.setErrorMsg("人员名称不合法,长度不超过8位");
                continue;
            }

            String identity = people.getIdentity();
            if (StringUtils.isNotBlank(identity)) {
                if ((!identity.matches(RegexKey.IDENTITY_REG_15)) && !identity.matches(RegexKey.IDENTITY_REG_18)) {
                    people.setErrorMsg("身份证编号不合法,请输入正确的身份证编号");
                    continue;
                }
                if (identitySet.contains(people.getIdentity())) {
                    people.setErrorMsg("身份证号已存在");
                }
            }
            if (GenderEnum.checkGender(people.getGenderStr())) {
                people.setErrorMsg("性别字段填写错误,请输入男或者女");
                continue;
            }
            people.setGender(GenderEnum.getCode(people.getGender()));

            String phone = people.getPhone();
            if (StringUtils.isNotBlank(phone) && !phone.matches(RegexKey.PHONE) && !phone.matches(RegexKey.TEL_PHONE)) {
                people.setErrorMsg("电话号码错误,请输入正确的电话号码");
            }
        }
    }
}
