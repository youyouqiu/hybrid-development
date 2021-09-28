package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.DictionaryType;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.repository.ThingDao;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 物品导入逻辑处理部分
 *
 * @author zhangjuan
 */
public class ThingImportHandler extends BaseImportHandler {
    private List<ThingDTO> importData;
    private ThingService thingService;
    private ThingDao thingDao;
    private TypeCacheManger cacheManger;
    private List<ThingDO> thingList;

    public ThingImportHandler(List<ThingDTO> importData, ThingService thingService, ThingDao thingDao) {
        this.importData = importData;
        this.thingService = thingService;
        this.thingDao = thingDao;
        cacheManger = TypeCacheManger.getInstance();
    }

    @Override
    public ImportModule module() {
        return ImportModule.THING;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[]{ImportTable.ZW_M_THING_INFO};
    }

    @Override
    public boolean uniqueValid() {
        checkParam();
        Set<String> thingNumSet = thingDao.getAll().stream().map(ThingDTO::getName).collect(Collectors.toSet());
        for (ThingDTO thing : importData) {
            if (StringUtils.isNotBlank(thing.getErrorMsg())) {
                continue;
            }
            if (thingNumSet.contains(thing.getName())) {
                thing.setErrorMsg("物品编号已存在");
            }
        }
        boolean isCheckPass = true;
        for (ThingDTO thing : importData) {
            if (StringUtils.isNotBlank(thing.getErrorMsg())) {
                isCheckPass = false;
                break;
            }
        }

        if (isCheckPass) {
            buildMysqlData();
            progressBar.setTotalProgress(importData.size() * 3 / 2);
        }
        return isCheckPass;
    }

    private void buildMysqlData() {
        thingList = new ArrayList<>();
        for (ThingDTO thing : importData) {
            thingList.add(new ThingDO(thing));
        }
    }

    @Override
    public boolean addMysql() {
        partition(thingList, thingDao::addThingInfoByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        thingService.addOrUpdateRedis(importData, null);
        progressBar.addProgress(importData.size() / 2);
    }

    private void checkParam() {
        Map<String, String> categoryMap = cacheManger.getDictValueCodeMap(DictionaryType.THING_CATEGORY);
        Map<String, String> typeMap = cacheManger.getDictValueCodeMap(DictionaryType.THING_TYPE);

        for (ThingDTO thing : importData) {
            if (StringUtils.isNotBlank(thing.getErrorMsg())) {
                continue;
            }

            if (StringUtils.isNotEmpty(thing.getAlias()) && thing.getAlias().length() > 20) {
                thing.setErrorMsg("物品名称超出最大位数，最大为20位");
                continue;
            }

            if (!Pattern.matches("^[\u4e00-\u9fa5-a-zA-Z0-9]{2,20}$", thing.getName())) {
                thing.setErrorMsg("物品编号只能输入汉字、字母、数字或短横杠，长度2-20位");
                continue;
            }

            if (StringUtils.isNotEmpty(thing.getLabel()) && thing.getLabel().length() > 20) {
                thing.setErrorMsg("品牌超出最大位数，最大为20位");
                continue;
            }
            if (StringUtils.isNotEmpty(thing.getModel()) && thing.getModel().length() > 20) {
                thing.setErrorMsg("型号超出最大位数，最大为20位");
                continue;
            }
            if (StringUtils.isNotEmpty(thing.getMaterial()) && thing.getMaterial().length() > 20) {
                thing.setErrorMsg("材料超出最大位数，最大为20位");
                continue;
            }

            if (StringUtils.isNotEmpty(thing.getSpec()) && thing.getSpec().length() > 20) {
                thing.setErrorMsg("规格超出最大位数，最大为20位");
                continue;
            }
            if (StringUtils.isNotEmpty(thing.getManufacture()) && thing.getManufacture().length() > 20) {
                thing.setErrorMsg("制造商超出最大位数，最大为20位");
                continue;
            }

            if (StringUtils.isNotEmpty(thing.getDealer()) && thing.getDealer().length() > 20) {
                thing.setErrorMsg("经销商超出最大位数，最大为20位");
                continue;
            }
            if (StringUtils.isNotEmpty(thing.getPlace()) && thing.getPlace().length() > 10) {
                thing.setErrorMsg("产地超出最大位数，最大为10位");
                continue;
            }

            if (StringUtils.isNotBlank(thing.getProductDateStr())) {
                Date productDate = DateUtil.getStringToDate(thing.getProductDateStr(), DateFormatKey.YYYY_MM_DD);
                if (productDate == null) {
                    thing.setErrorMsg("生产日期合适不正确，请以[yyyy-MM-dd]格式填写");
                    continue;
                }
                thing.setProductDate(productDate);
            }

            if (thing.getWeight() != null && (thing.getWeight() > 999999999 || thing.getWeight() < 0
                    || thing.getWeight().toString().contains("."))) {
                thing.setErrorMsg("重量只能输入0-999999999的正整数");
                continue;
            }
            if (StringUtils.isNotEmpty(thing.getRemark()) && thing.getRemark().length() > 50) {
                thing.setErrorMsg("备注超出最大位数，最大为50位");
                continue;
            }

            if (!categoryMap.containsKey(thing.getCategoryName())) {
                thing.setErrorMsg("物品类别不存在");
                continue;
            }
            thing.setCategory(categoryMap.get(thing.getCategoryName()));

            if (!typeMap.containsKey(thing.getTypeName())) {
                thing.setErrorMsg("物品类型不存在");
                continue;
            }
            thing.setType(typeMap.get(thing.getTypeName()));
        }
    }
}
