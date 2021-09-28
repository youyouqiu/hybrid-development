/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.platform.util.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * java代码验证常见的表单数据
 * <p>Title: RegexUtils.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年11月24日下午3:09:36
 */
public class RegexUtils {

    private static final Pattern PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5A-Za-z0-9-]{2,20}$");

    /**
     * 手机号码和座机号码正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\d{7,13})?$");

    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$");

    /**
     * 车主正则
     */
    private static final Pattern VEHICLE_OWNER_REGEX = Pattern.compile("^[A-Za-z\\u4e00-\\u9fa5]{1,8}$");

    /**
     * double类型正则
     */
    private static final Pattern DOUBLE_REGEX =
        Pattern.compile("^(?:0\\.[1-9]|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\\.[1-9])$");

    /**
     * 终端编号
     */
    private static final Pattern DEVICE_NUMBER_REGEX = Pattern.compile("^[0-9a-zA-Z]{7,30}$");

    /**
     * sim卡编号
     */
    private static final Pattern SIM_CARD_NUMBER_REGEX = Pattern.compile("^\\d{7,20}$");

    /**
     * 验证车牌号
     * @param carLicense
     * @return boolean
     * @throws
     * @Title: isCarLicense
     * 京津冀晋蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云渝藏陕甘青宁新
     * @author Liubangquan
     */
    public static boolean checkCarLicense(String carLicense) {
        //标准车牌规则
        String regex =
            "^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b\u9102"
                + "\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d\u85cf"
                + "\u9655\u7518\u9752\u5b81\u65b0\u6d4b]{1}[A-Z]{1}[A-Z_0-9]{5}$";
        //香港车牌规则
        String regex1 = "^[A-Z]{2}[0-9]{4}$";
        return Pattern.matches(regex, carLicense) || Pattern.matches(regex1, carLicense);
    }

    /**
     * 根据车牌号的第一个汉字获取其所在的省份
     * @param brand
     * @return String
     * @throws
     * @Title: getAreaByBrand
     * @author Liubangquan
     */
    public static String getAreaByBrand(String brand) {
        String[] firstWord =
            { "京", "沪", "津", "渝", "黑", "吉", "辽", "冀", "豫", "鲁", "晋", "陕", "秦", "甘", "陇", "青", "琼", "黔", "贵", "鄂", "湘",
                "浙", "苏", "闽", "皖", "川", "蜀", "赣", "粤", "宁", "新", "蒙", "桂", "藏" };
        String[] citys =
            { "北京", "上海", "天津", "重庆", "黑龙江", "吉林", "辽宁", "河北", "河南", "山东", "山西", "陕西", "陕西", "甘肃", "甘肃", "青海", "海南",
                "贵州", "贵州", "湖北", "湖南", "浙江", "江苏", "福建", "安徽", "四川", "四川", "江西", "广东", "宁夏", "新疆", "内蒙古", "广西", "西藏" };
        int index = 0;
        for (int i = 0; i < firstWord.length; i++) {
            if (brand.substring(0, 1).equals(firstWord[i])) {
                index = i;
                break;
            }
        }
        return citys[index];
    }

    /**
     * 验证SIM卡(4.4.2改为数字和字母)
     * @param simcard
     * @return boolean
     * @throws
     * @Title: checkSIM
     * @author Liubangquan
     */
    public static boolean checkSIM(String simcard) {
        // String regex1 = "^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|"
        //                 + "(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\\d{8}$";
        String regex2 = "^[a-zA-Z0-9]{7,20}$";
        return Pattern.matches(regex2, simcard);
    }

    /**
     * 验证String：数字、字母、-、_
     * /^[A-Za-z0-9_-]+$/
     * @param str
     * @return boolean
     * @throws
     * @Title: checkRightfulString
     * @author Liubangquan
     */
    public static boolean checkRightfulString(String str) {
        String regex = "^[A-Za-z0-9_-]+$";
        return Pattern.matches(regex, str);
    }

    /**
     * 验证String：^[A-Za-z0-9_\(\)\（\）\*\u4e00-\u9fa5\-]+$
     * @param str
     * @return boolean
     * @throws
     * @Title: checkRightfulString
     * @author Liubangquan
     */
    public static boolean checkRightfulString1(String str) {
        String regex = "^[A-Za-z0-9_\\(\\)\\（\\）\\*\\u4e00-\\u9fa5\\-.]+$";
        return Pattern.matches(regex, str);
    }

    /**
     * 验证数字：^[1-9]([0-9]*)$|^[0-9]$
     * @param str
     * @return boolean
     * @throws
     * @Title: checkRightNumber
     * @author Liubangquan
     */
    public static boolean checkRightNumber(String str) {
        String regex = "^[1-9]([0-9]*)$|^[0-9]$";
        return Pattern.matches(regex, str);
    }

    /**
     * 获取字符串中的数字如：2.0L256KW ----->  2.0256
     * @param str
     * @return String
     * @throws
     * @Title: getNumInStr
     * @author Liubangquan
     */
    public static String getNumInStr(String str) {
        StringBuilder result = new StringBuilder();
        if (!"".equals(Converter.toBlank(str))) {
            Pattern p = Pattern.compile("[0-9\\.]+");
            Matcher m = p.matcher(str);
            while (m.find()) {
                result.append(m.group());
            }
        }
        return result.toString();
    }

    /**
     * 验证Email
     * @param email email地址，格式：zhangsan@zuidaima.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }

    /**
     * 验证身份证号码
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIdCard(String idCard) {
        String regex = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
        return Pattern.matches(regex, idCard);
    }

    /**
     * 校验身份证号（身份证号码15位或18位，最后一位可能是数字或字母 ，身份证上的前6位以及出生年月日 ）
     * @param idNum
     * @return
     */
    public static boolean checkIdentity(String idNum) {
        //定义判别用户身份证号的正则表达式（要么是15位，要么是18位，最后一位可以为字母）
        Pattern idNumPattern = Pattern.compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");
        //通过Pattern获得Matcher  
        Matcher idNumMatcher = idNumPattern.matcher(idNum);
        //判断用户输入是否为身份证号  
        if (idNumMatcher.matches()) {
            //如果是，定义正则表达式提取出身份证中的出生日期  
            Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");//身份证上的前6位以及出生年月日
            //通过Pattern获得Matcher  
            Matcher birthDateMather = birthDatePattern.matcher(idNum);
            //通过Matcher获得用户的出生年月日  
            if (birthDateMather.find()) {
                String year = birthDateMather.group(1);
                String month = birthDateMather.group(2);
                String date = birthDateMather.group(3);
                GregorianCalendar gc = new GregorianCalendar();
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    if ((gc.get(Calendar.YEAR) - Integer.parseInt(year)) > 150
                        || (gc.getTime().getTime() - s.parse(year + "-" + month + "-" + date).getTime())
                        < 0) {   // 身份证生日不在有效范围
                        return false;
                    }
                    if (Integer.parseInt(month) > 12 || Integer.parseInt(month) == 0) {   // 身份证月份无效
                        return false;
                    }
                    if (Integer.parseInt(date) > 31 || Integer.parseInt(date) == 0) {    // 身份证日期无效
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                } catch (ParseException e) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     * @param mobile 移动、联通、电信运营商的号码段
     *               <p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *               、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *               <p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *               <p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkMobile(String mobile) {
        String regex = "(\\+\\d+)?1[3458]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 验证是否是常规字符
     * 常规字符不包含 “空格、换行和英文格式的`^*;'\"|, /<>?”
     * @param str str
     */
    public static boolean checkIsRegularChar(String str) {
        String regEx = "[ `^*;'\\\\\"|,/<>?]|\n|\r|\t";
        Matcher matcher = Pattern.compile(regEx).matcher(str);
        return !matcher.find();
    }

    /**
     * 验证固定电话号码
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     *              <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     *              数字之后是空格分隔的国家（地区）代码。</p>
     *              <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     *              对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     *              <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 验证整数（正整数和负整数）
     * @param digit 一位或多位0-9之间的整数
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDigit(String digit) {
        String regex = "\\-?[1-9]\\d+";
        return Pattern.matches(regex, digit);
    }

    /**
     * 验证整数和浮点数（正负整数和正负浮点数）
     * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDecimals(String decimals) {
        String regex = "\\-?[1-9]\\d+(\\.\\d+)?";
        return Pattern.matches(regex, decimals);
    }

    /**
     * 验证空白字符
     * @param blankSpace 空白字符，包括：空格、\t、\n、\r、\f、\x0B
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkBlankSpace(String blankSpace) {
        String regex = "\\s+";
        return Pattern.matches(regex, blankSpace);
    }

    /**
     * 验证中文
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkChinese(String chinese) {
        String regex = "^[\u4E00-\u9FA5]+$";
        return Pattern.matches(regex, chinese);
    }

    /**
     * 验证日期（年月日）
     * @param date 日期，格式：1992-09-03，或1992.09.03
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDate(String date) {
        String regex = "[0-9]{4}([-./])\\d{1,2}\\1\\d{1,2}";
        return Pattern.matches(regex, date);
    }

    /**
     * 验证日期格式
     * @param date 格式 ： yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static boolean checkDateFormate(String date) {
        String regex = "^[1-9]\\d{3}\\-(0?[1-9]|1[0-2])\\-(0?[1-9]|[12]\\d|3[01])\\s*"
            + "(0?[1-9]|1\\d|2[0-3])(\\:(0?[1-9]|[1-5]\\d)){2}$";
        return Pattern.matches(regex, date);
    }

    /**
     * 验证URL地址
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkURL(String url) {
        String regex = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
        return Pattern.matches(regex, url);
    }

    /**
     * date2比date1多的天数
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /**
     * <pre>
     * 获取网址 URL 的一级域名
     * http://www.zuidaima.com/share/1550463379442688.htm ->> zuidaima.com
     * </pre>
     * @param url
     * @return
     */
    public static String getDomain(String url) {
        Pattern p =
            Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        // 获取完整的域名  
        // Pattern p=Pattern.compile("[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);  
        Matcher matcher = p.matcher(url);
        matcher.find();
        return matcher.group();
    }

    /**
     * 匹配中国邮政编码
     * @param postcode 邮政编码
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPostcode(String postcode) {
        String regex = "[1-9]\\d{5}";
        return Pattern.matches(regex, postcode);
    }

    /**
     * 匹配IP地址(简单匹配，格式，如：192.168.1.1，127.0.0.1，没有匹配IP段的大小)
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIpAddress(String ipAddress) {
        String regex = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";
        return Pattern.matches(regex, ipAddress);
    }

    /**
     * (工程机械添加)车牌号验证
     * @param plateNumber 车牌号
     * @return true: 符合规则; false: 不符合规则
     */
    public static boolean checkPlateNumber(String plateNumber) {
        Matcher matcher = PATTERN.matcher(plateNumber);
        return matcher.matches();
    }

    /**
     * (工程机械)车主手机号验证
     * @param phoneNumber 手机号
     * @return true: 符合规则; false: 不符合规则
     */
    public static boolean checkOwnerPhone(String phoneNumber) {
        Matcher matcher = PHONE_PATTERN.matcher(phoneNumber);
        return matcher.matches();
    }

    /**
     * (工程机械)车主座机验证
     * @param landline 座机号码
     * @return true: 符合规则; false: 不符合规则
     */
    public static boolean checkLandline(String landline) {
        Matcher matcher = LANDLINE_PATTERN.matcher(landline);
        return matcher.matches();
    }

    /**
     * 校验车主
     */
    public static boolean checkVehicleOwner(String vehicleOwner) {
        Matcher matcher = VEHICLE_OWNER_REGEX.matcher(vehicleOwner);
        return matcher.matches();
    }

    /**
     * 校验double类型值
     */
    public static boolean checkDouble(String doubleValueStr) {
        Matcher matcher = DOUBLE_REGEX.matcher(doubleValueStr);
        return matcher.matches();
    }

    /**
     * 校验double类型值
     */
    public static boolean checkDeviceNumber(String deviceNumber) {
        Matcher matcher = DEVICE_NUMBER_REGEX.matcher(deviceNumber);
        return matcher.matches();
    }

    /**
     * 校验double类型值
     */
    public static boolean checkSimCardNumber(String simCardNumber) {
        Matcher matcher = SIM_CARD_NUMBER_REGEX.matcher(simCardNumber);
        return matcher.matches();
    }
}

