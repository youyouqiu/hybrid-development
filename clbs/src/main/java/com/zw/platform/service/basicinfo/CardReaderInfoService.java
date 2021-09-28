package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.CardReaderInfo;
import com.zw.platform.domain.basicinfo.form.CardReaderInfoForm;
import com.zw.platform.domain.basicinfo.query.CardReaderInfoQuery;
import com.zw.platform.util.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface CardReaderInfoService {

	/**
	* 分页查询读卡器数据
	* @Title: findByPage
	* @return Page<CardReaderInfo>
	* @throws
	* @author Liubangquan
	 */
    Page<CardReaderInfo> findByPage(final CardReaderInfoQuery query);
    
    /**
    * 通过id得到一个 CardReaderInfo
    * @Title: get
    * @return CardReaderInfo
    * @throws
    * @author Liubangquan
     */
    CardReaderInfo get(final String id);
    
    /**
    * 新增
    * @Title: add
    * @return void
    * @throws
    * @author Liubangquan
     */
    void add(final CardReaderInfoForm form) throws BusinessException;
   
    /**
    * 根据id删除一个 CardReaderInfo
    * @Title: delete
    * @return int
    * @throws
    * @author Liubangquan
     */
    int delete(final String id) throws BusinessException;

    /**
    * 修改 CardReaderInfo
    * @Title: update
    * @return int
    * @throws
    * @author Liubangquan
     */
    int update(final CardReaderInfoForm form) throws BusinessException;
    
    /**
    * 导出
    * @Title: exportInfo
    * @return boolean
    * @throws
    * @author Liubangquan
     */
    boolean exportCardReaderInfo(String title, int type, HttpServletResponse response);
    
    /**
     * 生成导入模板
     * @Title: generateTemplate
     * @param response
     * @return boolean
     * @author Liubangquan
     */
    boolean generateTemplate(HttpServletResponse response);
     
    /**
    * 导入读卡器列表
    * @Title: importConfig
    * @param file
    * @return Map
    * @author Liubangquan
     */
  	Map importCardReaderInfo(MultipartFile file);
  	
  	/**
  	* 校验读卡器编号是否重复
  	* @Title: checkCardReaderNumber
  	* @param cardReaderNumber
  	* @throws BusinessException
  	* @return boolean
  	* @author Liubangquan
  	 */
  	boolean checkCardReaderNumber(final String cardReaderNumber) throws BusinessException;

	CardReaderInfo findByCardReaderInfo(String cardReaderNumber);
}
