package com.zw.platform.util;

import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.form.TimeEnergyStatisticsForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 集合排序工具类
 * <p>Title: CollectionsSortUtil.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月19日上午10:18:47
 */
public class CollectionsSortUtil {
    private static Logger log = LogManager.getLogger(CollectionsSortUtil.class);

    /**
     * 对列表中的数据按指定字段进行排序。要求类必须有相关的方法返回字符串、整型、日期等值以进行比较。
     * @param list
     * @param method
     * @param reverseFlag
     * @return void
     * @throws
     * @Title: sortByMethod
     * @author Liubangquan
     */
    public void sortByMethod(List<TimeEnergyStatisticsForm> list, final String method, final boolean reverseFlag) {
        Collections.sort(list, new Comparator<TimeEnergyStatisticsForm>() {
            public int compare(TimeEnergyStatisticsForm arg1, TimeEnergyStatisticsForm arg2) {
                int result = 0;
                try {
                    Method m1 = ((TimeEnergyStatisticsForm) arg1).getClass().getMethod(method, null);
                    Method m2 = ((TimeEnergyStatisticsForm) arg2).getClass().getMethod(method, null);
                    Object obj1 = m1.invoke(((TimeEnergyStatisticsForm) arg1), null);
                    Object obj2 = m2.invoke(((TimeEnergyStatisticsForm) arg2), null);
                    if (obj1 instanceof String) {
                        // 字符串
                        result = obj1.toString().compareTo(obj2.toString());
                    } else if (obj1 instanceof Date) {
                        // 日期
                        long l = ((Date) obj1).getTime() - ((Date) obj2).getTime();
                        if (l > 0) {
                            result = 1;
                        } else if (l < 0) {
                            result = -1;
                        } else {
                            result = 0;
                        }
                    } else if (obj1 instanceof Integer) {
                        // 整型（Method的返回参数可以是int的，因为JDK1.5之后，Integer与int可以自动转换了）
                        result = (Integer) obj1 - (Integer) obj2;
                    } else {
                        // 目前尚不支持的对象，直接转换为String，然后比较，后果未知
                        result = obj1.toString().compareTo(obj2.toString());
                        log.error("MySortList.sortByMethod方法接受到不可识别的对象类型，转换为字符串后比较返回...");
                    }
                    if (reverseFlag) {
                        // 倒序
                        result = -result;
                    }
                } catch (NoSuchMethodException nsme) {
                    log.error("error", nsme);
                } catch (IllegalAccessException iae) {
                    log.error("error", iae);
                } catch (InvocationTargetException ite) {
                    log.error("error", ite);
                }
                return result;
            }
        });
    }

    /**
     * 对列表中的数据按指定字段进行排序。要求类必须有相关的方法返回字符串、整型、日期等值以进行比较。
     * @param list
     * @param method
     * @param reverseFlag
     * @return void
     * @throws
     * @Title: sortByMethod
     * @author Liubangquan
     */
    public void sortByMethod_m(List<MileageForm> list, final String method, final boolean reverseFlag) {
        Collections.sort(list, new Comparator<MileageForm>() {
            public int compare(MileageForm arg1, MileageForm arg2) {
                int result = 0;
                try {
                    Method m1 = ((MileageForm) arg1).getClass().getMethod(method, null);
                    Method m2 = ((MileageForm) arg2).getClass().getMethod(method, null);
                    Object obj1 = m1.invoke(((MileageForm) arg1), null);
                    Object obj2 = m2.invoke(((MileageForm) arg2), null);
                    if (obj1 instanceof String) {
                        // 字符串
                        result = obj1.toString().compareTo(obj2.toString());
                    } else if (obj1 instanceof Date) {
                        // 日期
                        long l = ((Date) obj1).getTime() - ((Date) obj2).getTime();
                        if (l > 0) {
                            result = 1;
                        } else if (l < 0) {
                            result = -1;
                        } else {
                            result = 0;
                        }
                    } else if (obj1 instanceof Integer) {
                        // 整型（Method的返回参数可以是int的，因为JDK1.5之后，Integer与int可以自动转换了）
                        result = (Integer) obj1 - (Integer) obj2;
                    } else {
                        // 目前尚不支持的对象，直接转换为String，然后比较，后果未知
                        result = obj1.toString().compareTo(obj2.toString());
                        log.error("MySortList.sortByMethod方法接受到不可识别的对象类型，转换为字符串后比较返回...");
                    }
                    if (reverseFlag) {
                        // 倒序
                        result = -result;
                    }
                } catch (NoSuchMethodException nsme) {
                    log.error("error", nsme);
                } catch (IllegalAccessException iae) {
                    log.error("error", iae);
                } catch (InvocationTargetException ite) {
                    log.error("error", ite);
                }
                return result;
            }
        });
    }

    /**
     * 对列表中的数据按指定字段进行排序。要求类必须有相关的方法返回字符串、整型、日期等值以进行比较。
     * @param list
     * @param method
     * @param reverseFlag
     * @return void
     * @throws
     * @Title: sortByMethod_mobileS
     * @author Liubangquan
     */
    public void sortByMethod_mobileS(List<MobileSourceEnergyReportForm> list, final String method,
        final boolean reverseFlag) {
        Collections.sort(list, new Comparator<MobileSourceEnergyReportForm>() {
            public int compare(MobileSourceEnergyReportForm arg1, MobileSourceEnergyReportForm arg2) {
                int result = 0;
                try {
                    Method m1 = ((MobileSourceEnergyReportForm) arg1).getClass().getMethod(method, null);
                    Method m2 = ((MobileSourceEnergyReportForm) arg2).getClass().getMethod(method, null);
                    Object obj1 = m1.invoke(((MobileSourceEnergyReportForm) arg1), null);
                    Object obj2 = m2.invoke(((MobileSourceEnergyReportForm) arg2), null);
                    if (obj1 instanceof String) {
                        // 字符串
                        result = obj1.toString().compareTo(obj2.toString());
                    } else if (obj1 instanceof Date) {
                        // 日期
                        long l = ((Date) obj1).getTime() - ((Date) obj2).getTime();
                        if (l > 0) {
                            result = 1;
                        } else if (l < 0) {
                            result = -1;
                        } else {
                            result = 0;
                        }
                    } else if (obj1 instanceof Integer) {
                        // 整型（Method的返回参数可以是int的，因为JDK1.5之后，Integer与int可以自动转换了）
                        result = (Integer) obj1 - (Integer) obj2;
                    } else {
                        // 目前尚不支持的对象，直接转换为String，然后比较，后果未知
                        result = obj1.toString().compareTo(obj2.toString());
                        log.error("MySortList.sortByMethod方法接受到不可识别的对象类型，转换为字符串后比较返回...");
                    }
                    if (reverseFlag) {
                        // 倒序
                        result = -result;
                    }
                } catch (NoSuchMethodException nsme) {
                    log.error("error", nsme);
                } catch (IllegalAccessException iae) {
                    log.error("error", iae);
                } catch (InvocationTargetException ite) {
                    log.error("error", ite);
                }
                return result;
            }
        });
    }

}
