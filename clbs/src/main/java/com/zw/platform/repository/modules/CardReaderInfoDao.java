package com.zw.platform.repository.modules;

import com.zw.platform.domain.basicinfo.CardReaderInfo;
import com.zw.platform.domain.basicinfo.form.CardReaderInfoForm;
import com.zw.platform.domain.basicinfo.query.CardReaderInfoQuery;

import java.util.List;


/**
 * 读卡器管理dao层接口
 * <p>Title: CardReaderInfoDao.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年7月21日下午5:53:54
 * @version 1.0
 * 
 */
public interface CardReaderInfoDao {

	/**
	* 查询读卡器数据
	* @Title: find
	* @return List<CardReaderInfo>
	* @throws
	* @author Liubangquan
	 */
    List<CardReaderInfo> find(final CardReaderInfoQuery query);
    
    /**
    * 通过id得到一个 CardReaderInfo
    * @Title: get
    * @return Personnel
    * @throws
    * @author Liubangquan
     */
    CardReaderInfo get(final String id);
    
    /**
    * 根据读卡器编号读取CardReaderInfo
    * @Title: findByCardReaderNumber
    * @return CardReaderInfo
    * @throws
    * @author Liubangquan
     */
    CardReaderInfo findByCardReaderNumber(final String cardReaderNumber);
    
    /**
    * 新增CardReaderInfo
    * @Title: add
    * @return void
    * @throws
    * @author Liubangquan
     */
    void add(final CardReaderInfoForm form);
    
    /**
    * 批量新增
    * @Title: addByBatch
    * @return boolean
    * @throws
    * @author Liubangquan
     */
    boolean addByBatch(List<CardReaderInfoForm> cardReaderInfoForm);

    /**
    * 根据id删除读卡器信息
    * @Title: delete
    * @return int
    * @throws
    * @author Liubangquan
     */
    int delete(final String id);

    /**
    * 修改读卡器信息
    * @Title: update
    * @return int
    * @throws
    * @author Liubangquan
     */
    int update(final CardReaderInfoForm form);

	CardReaderInfo findByCardReaderInfo(String cardReaderNumber);
}
