package com.zw.talkback.service.baseinfo.impl;

import com.zw.talkback.repository.mysql.CallNumberDao;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.util.CallNumberUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/***
 * 组呼和个呼号码
 * @author zhengjc
 * @version 1.0
 **/
@Service
public class IntercomCallNumberServiceImpl implements IntercomCallNumberService {
    @Autowired
    private CallNumberDao callNumberDao;

    @Override
    public String updateAndReturnPersonCallNumber() {
        String personCallNumber = CallNumberUtil.popPersonCallNumber();
        callNumberDao.updatePersonCallNumber(personCallNumber, (byte) 0);
        return personCallNumber;
    }

    @Override
    public String updateAndReturnGroupCallNumber() {
        String groupCallNumber = CallNumberUtil.popGroupCallNumber();
        callNumberDao.updateGroupCallNumber(groupCallNumber, (byte) 0);
        return groupCallNumber;

    }

    @Override
    public void updateAndRecyclePersonCallNumber(String callNumber) {
        CallNumberUtil.recyclePersonCallNumber(callNumber);
        callNumberDao.updatePersonCallNumber(callNumber, (byte) 1);
    }

    @Override
    public void updateAndRecycleGroupCallNumber(String callNumber) {
        CallNumberUtil.recycleGroupCallNumber(callNumber);
        callNumberDao.updateGroupCallNumber(callNumber, (byte) 1);
    }

    @Override
    public void addAndInitCallNumberToRedis() {
        long totalCallNumber = callNumberDao.checkCallNumber();
        if (totalCallNumber <= 0) {
            List<Integer> callNumbers = new ArrayList<>(90);
            for (int i = 10000; i < 100000; i++) {
                callNumbers.add(i);
            }
            callNumberDao.addCallNumbers(callNumbers);
        }
        List<String> personCallNumber = callNumberDao.getAllAvailablePersonCallNumber();
        List<String> groupCallNumber = callNumberDao.getAllAvailableGroupCallNumber();
        if (CollectionUtils.isNotEmpty(personCallNumber)) {
            CallNumberUtil.recyclePersonCallNumber(personCallNumber.toArray(new String[] {}));
        }
        if (CollectionUtils.isNotEmpty(groupCallNumber)) {
            CallNumberUtil.recycleGroupCallNumber(groupCallNumber.toArray(new String[] {}));
        }
    }

    @Override
    public void updateAndRecyclePersonCallNumberBatch(Collection<String> personNumbers) {
        if (CollectionUtils.isEmpty(personNumbers)) {
            return;
        }
        String[] personNumberArr = new String[personNumbers.size()];
        personNumbers.toArray(personNumberArr);
        CallNumberUtil.recycleGroupCallNumber(personNumberArr);
        callNumberDao.updatePersonCallNumberBatch(personNumbers, (byte) 1);
    }

    @Override
    public List<String> updateAndReturnPersonCallNumbers(int length) {
        Set<String> personCallNumbers = CallNumberUtil.popLengthPersonCallNumber(length);
        callNumberDao.updatePersonCallNumberBatch(personCallNumbers, (byte) 0);
        return new ArrayList<>(personCallNumbers);
    }
}
