//# sourceURL=jQueryValidate.js
/**
 * Created by Administrator on 2016/7/26.
 */
$(function () {
    jQuery.validator.addMethod("email", function (value, element) {
        var tel = /^\s*$|^[\w.]+@\w+\.[a-z]+(\.[a-z]+)?/;
        return this.optional(element) || (tel.test(value));
    }, "您输入的邮箱格式不正确");

    // 重复选择校验
    jQuery.validator.addMethod("repeatSelect", function (value, element, param) {
        if (value == '' || value == '0x00') return true;
        var target = $(param);
        var len = 0;
        for (var i = 0; i < target.length; i++) {
            if ($(target[i]).val() === value) {
                len++;
            }
        }
        return len < 2;
    }, "");

    // 输入字母、数字、下划线
    jQuery.validator.addMethod("hasUnderline", function (value) {
        if (value === '') return true;
        return /^[A-Za-z0-9_]+$/.test(value);
    }, "请输入字母、数字或下划线");

    // 校验整数范围
    jQuery.validator.addMethod("integerRange", function (value, element, range) {
        if (value === '') return true;
        var res = /^[1-9]\d*$/;
        if (res.test(value) && (range instanceof Array)) {
            var value = parseInt(value);
            if (range[0] <= value && value <= range[1]) {
                return true;
            }
        }
        return false;
    }, "");

    // 校验常规字符
    jQuery.validator.addMethod("regularChar", function (value, element, range) {
        if (value === '') return true;
        var res = /^[^`\s\^\*;'"\\|,/<>\?]*$/;
        if (res.test(value)) return true;
        return false;
    }, "请不要输入空格、换行和`^*;'\\\"|, /<>?");

    // 校验带小数的数字范围
    jQuery.validator.addMethod("decimalRange", function (value, element, range) {
        if (value === '') return true;
        var res = /^[0-9\.]*$/;
        if (res.test(value) && (range instanceof Array)) {
            var value = Math.ceil(value);
            if (range[0] <= value && value <= range[1]) {
                return true;
            }
        }
        return false;
    }, "");

    jQuery.validator.addMethod("positiveInteger", function (value, element) {
        if (value === '') return true;
        var res = /^[1-9]\d*$/;
        return res.test(value);
    }, "请输入正整数");

    // 判断整数value是否等于0
    jQuery.validator.addMethod("isIntEqZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value == 0;
        } else {
            return false;
        }
    }, "整数必须为0");

    // 判断整数value是否大于0
    jQuery.validator.addMethod("isIntGtZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value > 0;
        } else {
            return false;
        }
    }, "整数必须大于0");

    //验证组织机构代码格式
    jQuery.validator.addMethod("doubles", function (value, element) {
        if (value.length == 9) {
            var reg = /^[0-9]/;
            return this.optional(element) || (reg.test(value));
        } else if (value.length == 18) {
            // var reg = /^([A-Z]*\d+[A-Z]+)|(\d*[A-Z]+\d+)$/;
            var reg = /^[A-Z\d]{18}$/;
            return this.optional(element) || (reg.test(value));
        } else if (value.length == 0) {
            return true;
        } else {
            return false;
        }
    }, "请输入正确的组织结构代码(9位数字)或者18位的统一社会信用代码(数字和大写字母的组合)");

    //验证输入的文本是否为数字和字母
    jQuery.validator.addMethod("isRegisterNumber", function (value, element) {
        var reg = /^[a-zA-Z0-9-]{13}-[a-zA-Z0-9]/;
        return this.optional(element) || (reg.test(value));
    }, "请输入正确的数字和字母以及正确的长度");

    //只能输入字母和数字
    jQuery.validator.addMethod("isABCNumber", function (value, element) {
        var reg = /^[a-zA-Z0-9]+$/;
        if (value == '') return true;
        return reg.test(value);
    }, "只能输入字母和数字");

    //只能输入字母和数字
    jQuery.validator.addMethod("isMacAddress", function (value, element) {
        var reg = /^([0-9a-fA-F]{2})(([/\s-][0-9a-fA-F]{2}){5})$/;
        if (value == '') return true;
        return reg.test(value);
    }, "只能输入数字(0-9)、字母(A-F、a-f)、每两个字符以'-'隔开，长度17位，如：09-af-EA-AE-3C-AF");


    // 判断是否是正确的数字
    jQuery.validator.addMethod("isRightNumber", function (value, element) {
        var reg = /^[1-9]([0-9]*)$|^[0-9]$/;
        return this.optional(element) || reg.test(value);
    }, "请输入正确的数字");

    // 判断整数value是否大于或等于0
    jQuery.validator.addMethod("isIntGteZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value >= 0;
        } else {
            return false;
        }
    }, "整数必须大于或等于0");

    // 判断是否介于0-999.9
    jQuery.validator.addMethod("distanceNum", function (value, element) {
        if (value) {
            var reg = /^([0-9]|[1-9][0-9]{0,2})(\.[1-9]|\.[1-9])?$/;
            return reg.test(value);
        } else {
            return true;
        }
    }, "");

    // 判断整数value是否介于1-120
    jQuery.validator.addMethod("isInt1to120", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= 120);
        } else {
            return false;
        }
    }, "介于1到120之间的整数");

    // 判断整数value是否介于1-400
    jQuery.validator.addMethod("isInt1to400", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= 400);
        } else {
            return false;
        }
    }, "介于1到400之间的整数");

    // 判断整数value是否介于1tovalue
    jQuery.validator.addMethod("isInt1tov", function (value, element, param) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= param);
        } else if (value == null || value == "") {
            return true;
        } else {
            return false;
        }
    }, "介于1到之间的整数");

    // 判断整数value是否介于1-60
    jQuery.validator.addMethod("isInt1to60", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= 60);
        } else {
            return false;
        }
    }, "介于1到60之间的整数");

    // 判断整数value是否介于10-100
    jQuery.validator.addMethod("isInt10to100", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 10 && value <= 100);
        } else {
            return false;
        }
    }, "介于10到100之间的整数");

    // 判断整数value是否介于1-10
    jQuery.validator.addMethod("isInt1to10", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= 10);
        } else {
            return false;
        }
    }, "介于1到10之间的整数");

    // 判断整数value是否介于1-24
    jQuery.validator.addMethod("isInt1to24", function (value, element) {
        if (/^[-\+]?\d/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 1 && value <= 24);
        } else {
            return false;
        }
    }, "介于1到24之间的整数");

    // 判断整数value是否介于0-59
    jQuery.validator.addMethod("isInt1to59", function (value, element) {
        if (/^[-\+]?\d/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || (value >= 0 && value <= 59);
        } else {
            return false;
        }
    }, "介于0到59之间的整数");
    /*// 判断整数value是否介于1-10
    jQuery.validator.addMethod("isInt7to15", function(value, element) {
        if(/^[-\+]?\d+$/.test(value)){
            value=parseInt(value);
            console.log(value);
            return this.optional(element) || (value >=7 && value <= 15);
        }else{
            return false;
        }
    }, "介于7到15之间的整数");
  */
    // 判断整数value是否不等于0
    jQuery.validator.addMethod("isIntNEqZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value != 0;
        } else {
            return false;
        }
    }, "整数必须不等于0");

    // 判断整数value是否小于0
    jQuery.validator.addMethod("isIntLtZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value < 0;
        } else {
            return false;
        }
    }, "整数必须小于0");

    // 判断整数value是否小于或等于0
    jQuery.validator.addMethod("isIntLteZero", function (value, element) {
        if (/^[-\+]?\d+$/.test(value)) {
            value = parseInt(value);
            return this.optional(element) || value <= 0;
        } else {
            return false;
        }
    }, "整数必须小于或等于0");

    // 判断浮点数value是否等于0
    jQuery.validator.addMethod("isFloatEqZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value == 0;
    }, "浮点数必须为0");

    // 判断浮点数value是否大于0
    jQuery.validator.addMethod("isFloatGtZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value > 0;
    }, "浮点数必须大于0");

    // 判断浮点数value是否大于或等于0
    jQuery.validator.addMethod("isFloatGteZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value >= 0;
    }, "浮点数必须大于或等于0");

    // 判断浮点数value是否不等于0
    jQuery.validator.addMethod("isFloatNEqZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value != 0;
    }, "浮点数必须不等于0");

    // 判断浮点数value是否小于0
    jQuery.validator.addMethod("isFloatLtZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value < 0;
    }, "浮点数必须小于0");

    // 判断浮点数value是否小于或等于0
    jQuery.validator.addMethod("isFloatLteZero", function (value, element) {
        value = parseFloat(value);
        return this.optional(element) || value <= 0;
    }, "浮点数必须小于或等于0");

    // 判断浮点型
    jQuery.validator.addMethod("isFloat", function (value, element) {
        return this.optional(element) || /^[-\+]?\d+(\.\d+)?$/.test(value);
    }, "只能包含数字、小数点等字符");

    // 判断小数0-1
    jQuery.validator.addMethod("isDecimal", function (value, element) {
        return this.optional(element) || /^0\.\d+$/.test(value);
    }, "只能输入0-1的小数");

    // 匹配integer
    jQuery.validator.addMethod("isInteger", function (value, element) {
        return this.optional(element) || (/^[-\+]?\d+$/.test(value) && parseInt(value) >= 0);
    }, "匹配integer");

    // 判断数值类型，包括整数和浮点数
    jQuery.validator.addMethod("isNumber", function (value, element) {
        return this.optional(element) || /^[-\+]?\d+$/.test(value) || /^[-\+]?\d+(\.\d+)?$/.test(value);
    }, "匹配数值类型，包括整数和浮点数");

    // 只能输入[0-9]数字
    jQuery.validator.addMethod("isDigits", function (value, element) {
        return this.optional(element) || /^\d+$/.test(value);
    }, "只能输入0-9数字");

    // 只能输入[0-9]数字和符号.
    jQuery.validator.addMethod("isDigitsPoint", function (value, element) {
        return this.optional(element) || /^[\d\.\n#]+$/g.test(value);
    }, "只能输入数字0-9和符号.#");

    // ip地址校验
    jQuery.validator.addMethod('ipFilter', function (value, element, param) {
        var values = value.trim().split(/\n|#/);
        var ipArr = [];
        var ipFilterMsg;
        var result = true;
        var errArr = [];
        for (var i = 0; i < values.length; i++) {
            var item = values[i];
            if (item) {
                if (ipArr.indexOf(item) === -1) {
                    ipArr.push(item);
                    var reg = /^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$/;
                    if (!reg.test(item)) {
                        errArr.push(i + 1);
                    }
                } else {
                    ipFilterMsg = '存在重复IP地址';
                    result = false;
                }
            } else {
                ipFilterMsg = '存在无效IP地址';
                result = false;
            }
        }
        if (errArr.length > 0) {
            ipFilterMsg = '第[' + errArr.join(',') + ']个IP地址格式错误';
            result = false;
        }
        if (ipArr.length > 10) {
            ipFilterMsg = '最多输入10个IP地址';
            result = false;
        }
        $.validator.messages.ipFilter = ipFilterMsg;
        return result;
    });

    // 判断中文字符
    jQuery.validator.addMethod("isChinese", function (value, element) {
        return this.optional(element) || /^[\u0391-\uFFE5]+$/.test(value);
    }, "只能包含中文字符。");

    // 判断英文字符
    jQuery.validator.addMethod("isEnglish", function (value, element) {
        return this.optional(element) || /^[A-Za-z]+$/.test(value);
    }, "只能包含英文字符。");

    // 手机号码验证
    jQuery.validator.addMethod("isMobile", function (value, element) {
        var length = value.length;
        var mobile = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
        return this.optional(element) || (length == 11 && mobile.test(value));
    }, "请正确填写您的手机号码。");

    // 电话号码验证
    jQuery.validator.addMethod("isPhone", function (value, element) {
        var tel = /^(\d{3,4}-?)?\d{7,9}$/g;
        return this.optional(element) || (tel.test(value));
    }, "请正确填写您的电话号码。");

    // 手机号码验证
    jQuery.validator.addMethod("mobilePhone", function (value, element) {
        var length = value.length;
        var mobile = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
        return this.optional(element) || (length == 11 && mobile.test(value));
    }, "看起来不像手机号码呢");

    // 联系电话(手机/电话皆可)验证
    jQuery.validator.addMethod("isTel", function (value, element) {
        var length = value.length;
        var tel = /^(\d{3}-\d{8}|\d{4}-\d{7,8}|\d{7,13})?$/;
        var mobile = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
        return this.optional(element) || tel.test(value) || (length == 11 && mobile.test(value));
    }, "请正确填写您的联系方式");

    // 联系电话(手机/电话皆可)验证
    jQuery.validator.addMethod("isNewTel", function (value, element) {
        var length = value.length;
        var tel = /^(\d{3}-\d{8}|\d{4}-\d{7,8}|\d{7,20})?$/;
        var mobile = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
        return this.optional(element) || tel.test(value) || (length == 11 && mobile.test(value));
    }, "请输入7-20位数字");

    // 终端手机号验证
    jQuery.validator.addMethod("isSim", function (value, element) {
        var tel = /^[0-9a-zA-Z]{7,20}$/g;
        /*var tel = /^((\d{3,4}-\d{7,9})|([1-9]{1}\d{6,12}))$/g;
        var mobile = /^((13[0-9]{1})|(14[5,7,9]{1})|(15[^4]{1})|(166)|(18[0-9]{1})|(19[8-9]{1})|(17[0,1,3,5,6,7,8]{1}))+\d{8}$/;
       */
        return this.optional(element) || tel.test(value);
    }, "请输入数字字母，范围：7~20位");
    // 终端手机号验证
    jQuery.validator.addMethod("isNewSim", function (value, element) {
        var tel = /^[0-9a-zA-Z]{7,20}$/g;
        return this.optional(element) || tel.test(value);
    }, "请输入数字字母，范围：7~20位");
    // 新增对讲对象终端手机号验证
    jQuery.validator.addMethod("isIntercomSim", function (value, element) {
        var length = value.length;
        if (length > 0 && value.substr(0, 1) == '0') {
            return false;
        }
        return true;
    }, "终端手机号首位不能为0");

    // 匹配qq
    jQuery.validator.addMethod("isQq", function (value, element) {
        return this.optional(element) || /^[1-9]\d{4,12}$/;
    }, "匹配QQ");

    // 邮政编码验证
    jQuery.validator.addMethod("isZipCode", function (value, element) {
        var zip = /^[0-9]{6}$/;
        return this.optional(element) || (zip.test(value));
    }, "请正确填写您的邮政编码。");

    // 匹配密码，以字母开头，长度在6-12之间，只能包含字符、数字和下划线。
    jQuery.validator.addMethod("isPwd", function (value, element) {
        return this.optional(element) || /^[a-zA-Z]\\w{6,12}$/.test(value);
    }, "以字母开头，长度在6-12之间，只能包含字符、数字和下划线。");

    // 身份证号码验证
    jQuery.validator.addMethod("isIdCardNo", function (value, element) {
        //var idCard = /^(\d{6})()?(\d{4})(\d{2})(\d{2})(\d{3})(\w)$/;
        return this.optional(element) || isIdCardNo(value);
    }, "请输入正确的身份证号码");

    // IP地址验证
    jQuery.validator.addMethod("ip", function (value, element) {
        return this.optional(element) || /^(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))$/.test(value);
    }, "请填写正确的IP地址。");

    // 多个以 # 隔开的IP地址验证
    jQuery.validator.addMethod("batchIp", function (value, element) {
        var ips = value.split("#");
        var reg = /^(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))$/;
        for (var i = 0; i < ips.length; i++) {
            if (!reg.test(ips[i])) {
                return false;
            }
        }
        return true;
    }, "");

    // 字符验证，只能包含中文、英文、数字、下划线等字符。
    jQuery.validator.addMethod("stringCheck", function (value, element) {
        return this.optional(element) || /^[a-zA-Z0-9\u4e00-\u9fa5-_]+$/.test(value);
    }, "只能包含中文、英文、数字、下划线等字符");
    // 字符验证，只能包含中文、英文、数字、# ~字符。
    jQuery.validator.addMethod("fuelType", function (value, element) {
        return this.optional(element) || /^[a-zA-Z0-9\u4e00-\u9fa5-#~]+$/.test(value);
    }, "只能包含中文、英文、数字、#、~");

    // 字符验证，只能包含中文、英文、数字字符。
    jQuery.validator.addMethod("zysCheck", function (value, element) {
        return this.optional(element) || /^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value);
    }, "只能包含中文、英文、数字");

    //字符验证，只能包含中文和英文
    jQuery.validator.addMethod("isCE", function (value, element) {
        var reg = /^[a-zA-Z\u4e00-\u9fa5]$/
        return this.optional(element) || reg.test(value);
    }, "只能包含中文、英文");
    // 匹配english
    jQuery.validator.addMethod("isEnglish", function (value, element) {
        return this.optional(element) || /^[A-Za-z]+$/.test(value);
    }, "匹配english");

    // 匹配汉字
    jQuery.validator.addMethod("isChinese", function (value, element) {
        return this.optional(element) || /^[\u4e00-\u9fa5]+$/.test(value);
    }, "匹配汉字");

    // 匹配中文(包括汉字和字符)
    jQuery.validator.addMethod("isChineseChar", function (value, element) {
        return this.optional(element) || /^[\u0391-\uFFE5]+$/.test(value);
    }, "匹配中文(包括汉字和字符) ");

    // 判断是否为合法字符(a-zA-Z0-9-_)
    jQuery.validator.addMethod("isRightfulString", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9_-]+$/.test(value);
    }, "判断是否为合法字符(a-zA-Z0-9-_)");

    // 判断是否为合法字符(油箱型号输入限制：中文、-、_、字母、数字、（）、*)
    jQuery.validator.addMethod("isRightfulString_oilBoxType", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9_.\(\)\（\）\*\u4e00-\u9fa5\-]+$/.test(value);
    }, "判断是否为合法字符(中文、-、_、字母、数字、（）、*)");

    // 判断是否为合法传感器型号(传感器型号输入限制：中文、字母、数字或特殊符号*、-、_、#)
    jQuery.validator.addMethod("isRightSensorModel", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9_#\*\u4e00-\u9fa5\-]+$/.test(value);
    }, "请输入中文、字母、数字或特殊符号*、-、_、#");

    // 判断是否为合法字符(工时传感器型号输入限制：-、字母、数字、+)
    jQuery.validator.addMethod("isRightfulString_workhourSensorType", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9-+]+$/.test(value);
    }, "判断是否为合法字符(-、字母、数字、+)");

    // 判断是否为合法字符(a-zA-Z0-9)
    jQuery.validator.addMethod("isRightfulStr", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9]+$/.test(value);
    }, "输入类型为数字和字母");
    // 判断是否为合法字符(a-zA-Z0-9)
    jQuery.validator.addMethod("devicePwd", function (value, element) {
        return this.optional(element) || /^[A-Za-z0-9]{8}$/.test(value);
    }, "输入类型为数字和字母,长度8位");

    // 判断是否包含中英文特殊字符，除英文"-_"字符外
    jQuery.validator.addMethod("isContainsSpecialChar", function (value, element) {
        var reg = RegExp(/[(\ )(\`)(\~)(\!)(\@)(\#)(\$)(\%)(\^)(\&)(\*)(\()(\))(\+)(\=)(\|)(\{)(\})(\')(\:)(\;)(\')(',)(\[)(\])(\.)(\<)(\>)(\/)(\?)(\~)(\！)(\@)(\#)(\￥)(\%)(\…)(\&)(\*)(\（)(\）)(\—)(\+)(\|)(\{)(\})(\【)(\】)(\‘)(\；)(\：)(\”)(\“)(\’)(\。)(\，)(\、)(\？)]+/);
        return this.optional(element) || !reg.test(value);
    }, "含有中英文特殊字符");

    // 主动安全速度阈值比较
    jQuery.validator.addMethod("speedLimitCompare", function (value, element) {
        var speedLimitVal = $(element).closest('.tab-pane').find('.speedLimit').val();
        if (speedLimitVal == '' && value == '') return true;
        if (speedLimitVal == '' || value == '') return false;
        if (Number(speedLimitVal) >= Number(value)) {
            return false;
        } else {
            return true;
        }
    }, "必须大于报警判断速度阈值");

    //身份证号码的验证规则
    function isIdCardNo(num) {
        /*//if (isNaN(num)) {alert("输入的不是数字！"); return false;}
            var len = num.length, re;
            if (len == 15)
                re = new RegExp(/^(\d{6})()?(\d{2})(\d{2})(\d{2})(\d{2})(\w)$/);
            else if (len == 18)
                re = new RegExp(/^(\d{6})()?(\d{4})(\d{2})(\d{2})(\d{3})(\w)$/);
            else {
                //alert("输入的数字位数不对。");
                return false;
            }
            var a = num.match(re);
            if (a != null) {
                if (len == 15) {
                    var D = new Date("19" + a[3] + "/" + a[4] + "/" + a[5]);
                    var B = D.getYear() == a[3] && (D.getMonth() + 1) == a[4] && D.getDate() == a[5];
                }
                else {
                    var D = new Date(a[3] + "/" + a[4] + "/" + a[5]);
                    var B = D.getFullYear() == a[3] && (D.getMonth() + 1) == a[4] && D.getDate() == a[5];
                }
                if (!B) {
                    //alert("输入的身份证号 "+ a[0] +" 里出生日期不对。");
                    return false;
                }
            }
            if (!re.test(num)) {
                //alert("身份证最后一位只能是数字和字母。");
                return false;
            }
         return true;*/
        if (num.length !== 15 && num.length !== 18) return false;
        var reg = new RegExp(/(^\d{6}(18|19|20)\d{2}(0[1-9]|1[012])(0[1-9]|[12]\d|3[01])\d{3}(\d|[xX])$)|(^\d{8}(0[1-9]|1[012])(0[1-9]|[12]\d|3[01])\d{3}$)/);
        return reg.test(num);
    }

});

jQuery.validator.addMethod("compareDate", function (value, element, param) {
    var assigntime = value;
    var deadlinetime = jQuery(param).val();
    var reg = new RegExp('-', 'g');
    assigntime = assigntime.replace(reg, '/'); //正则替换
    deadlinetime = deadlinetime.replace(reg, '/');
    assigntime = new Date(parseInt(Date.parse(assigntime), 10));
    deadlinetime = new Date(parseInt(Date.parse(deadlinetime), 10));
    if (deadlinetime > assigntime) {
        return false;
    } else {
        return true;
    }
}, "<font color='#E47068'>结束日期必须大于开始日期</font>");

// 校验字段是否必填(用于省市区联动勾选校验)
jQuery.validator.addMethod("isRequire", function (value, element, param) {
    var relatedArr = param.split(',');
    var status = true;
    relatedArr.forEach(function (item) {
        if ($(item).val() !== '') {
            status = false;
        }
    });
    // 没有县可选时
    if (!status && relatedArr.length === 2 && $(element).find('option').length === 1) return true;

    return value ? true : status;
}, "");

jQuery.validator.addMethod("compareDateCanNull", function (value, element, param) {
    var assigntime = value;
    var deadlinetime = jQuery(param).val();
    if (isNull(assigntime) || isNull(deadlinetime)) {
        return true;
    }
    var reg = new RegExp('-', 'g');
    assigntime = assigntime.replace(reg, '/'); //正则替换
    deadlinetime = deadlinetime.replace(reg, '/');
    assigntime = new Date(parseInt(Date.parse(assigntime), 10));
    deadlinetime = new Date(parseInt(Date.parse(deadlinetime), 10));
    if (deadlinetime > assigntime) {
        return false;
    } else {
        return true;
    }
}, "<font color='#E47068'>结束日期必须大于开始日期</font>");

jQuery.validator.addMethod("compareTime", function (value, element, param) {
    var assigntime = "2016-01-01 " + value;
    var deadlinetime = "2016-01-01 " + jQuery(param).val();
    var reg = new RegExp('-', 'g');
    assigntime = assigntime.replace(reg, '/'); //正则替换
    deadlinetime = deadlinetime.replace(reg, '/');
    assigntime = new Date(parseInt(Date.parse(assigntime), 10));
    deadlinetime = new Date(parseInt(Date.parse(deadlinetime), 10));
    if (deadlinetime > assigntime) {
        return false;
    } else {
        return true;
    }
}, "<font color='#E47068'>结束时间必须大于开始时间</font>");

jQuery.validator.addMethod("compareDateDiff", function (value, element, param) {
    var sData1 = value;
    var sData2 = jQuery(param).val();
    if (DateDiff(sData1.substring(0, 10), sData2.substring(0, 10)) >= 7) {
        return false;
    } else {
        return true;
    }
}, "<font color='#E47068'>查询的日期必须小于一周</font>");

//判断是否为合法字符(.0-9)
jQuery.validator.addMethod("isContainsNumberAndPoint", function (value, element, param) {
    return this.optional(element) || /^[0-9.]+$/.test(value);
}, "只能包含数字和.");

function DateDiff(sDate1, sDate2) { //sDate1和sDate2是yyyy-MM-dd格式

    var aDate, oDate1, oDate2, iDays;
    // aDate = sDate1.split("-");
    oDate1 = new Date(sDate1); //转换为yyyy-MM-dd格式
    // aDate = sDate2.split("-");
    oDate2 = new Date(sDate2);
    iDays = parseInt(Math.abs(oDate1 - oDate2) / 1000 / 60 / 60 / 24); //把相差的毫秒数转换为天数

    return iDays; //返回相差天数
}

/**
 * 判断选择的时间是否大于等与今天
 */
jQuery.validator.addMethod("selectDate", function (value, element) {
    return this.optional(element) || operationTime(value);
}, "授权截止时间必须大于\等于今天");

function operationTime(value) {
    var sDate = value; //字符串格式yyyy-MM-dd
    var nowDate = new Date();
    // var newDate = nowDate.toLocaleDateString();//获取当前时间的日期 年/月/日(IE浏览器获取当前时间为x年x月x日)----字符串
    var reg = new RegExp(/[-\u4E00-\u9FA5\uF900-\uFA2D]/g);
    var aDate = sDate.replace(reg, "/"); //把用户选择时间的字符串中的-替换成/
    var year = nowDate.getFullYear();
    var month = nowDate.getMonth() + 1;
    var day = nowDate.getDate();
    var nowStr = year + '/' + (month < 10 ? '0' + month : month) + '/' + day;
    var normDate = new Date(nowStr); //把当前时间字符串转换为时间格式
    var selDate = new Date(aDate); //把用户选择的日期(字符串格式)转换为日期格式 年/月/日
    var nowDateTimestamp = normDate.getTime(); //把当前时间转换为时间戳
    var selDateTimestamp = selDate.getTime(); //把用户选择的时间转换为时间戳
    if (selDateTimestamp - nowDateTimestamp >= 0) {
        return true;
    } else {
        return false;
    }
}

/**
 * 判断选择的时间是否小于等与今天
 */
jQuery.validator.addMethod("selectRegDate", function (value, element) {
    return this.optional(element) || operationRegTime(value);
}, "注册日期必须小与/等于今天");

function operationRegTime(value) {
    var sDate = value; //字符串格式yyyy-MM-dd
    var nowDate = new Date();
    // var newDate = nowDate.toLocaleDateString();//获取当前时间的日期 年/月/日(IE浏览器获取当前时间为x年x月x日)----字符串
    var reg = new RegExp(/[-\u4E00-\u9FA5\uF900-\uFA2D]/g);
    var aDate = sDate.replace(reg, "/"); //把用户选择时间的字符串中的-替换成/
    var year = nowDate.getFullYear();
    var month = nowDate.getMonth() + 1;
    var day = nowDate.getDate();
    var nowStr = year + '/' + (month < 10 ? '0' + month : month) + '/' + day;
    var normDate = new Date(nowStr); //把当前时间字符串转换为时间格式
    var selDate = new Date(aDate); //把用户选择的日期(字符串格式)转换为日期格式 年/月/日
    var nowDateTimestamp = normDate.getTime(); //把当前时间转换为时间戳
    var selDateTimestamp = selDate.getTime(); //把用户选择的时间转换为时间戳
    if (nowDateTimestamp - selDateTimestamp >= 0) {
        return true;
    } else {
        return false;
    }

}

//小数点精度两位校验
jQuery.validator.addMethod("decimalTwo", function (value, element) {
    var reg = /^[0-9]+([.]{1}[0-9]{1,2})?$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.01");

//小数点精度一位校验
jQuery.validator.addMethod("decimalOne", function (value, element) {
    if (value === '0') return true;
    var reg = /^(?:0\.\d|[1-9][0-9]{0,9}|[1-9][0-9]{0,7}\.\d)$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.1");

//大小不限 小数点精度一位校验
jQuery.validator.addMethod("decimalOneMore", function (value, element) {
    var reg = /^(?:0\.\d|[1-9][0-9]*|[1-9][0-9]*\.\d)$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.1");

// 小数点精度校验
jQuery.validator.addMethod("decimalFour", function (value, element) {
    var reg = /^(?:0\.\d|\d[0-9]{0,3}|[1-9][0-9]{0,3}\.\d)$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.1");
//小数点精度校验
jQuery.validator.addMethod("decimalThree", function (value, element) {
    var reg = /^(?:0\.\d|\d[0-9]{0,2}|[1-9][0-9]{0,2}\.\d)$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.1");
//小数点精度校验
jQuery.validator.addMethod("decimalSeven", function (value, element) {
    var reg = /^(?:0\.\d|\d[0-9]{0,6}|[1-9][0-9]{0,6}\.\d)$/;
    return this.optional(element) || reg.test(value);
}, "输入类型为非负数，精度0.1");


//座机校验
jQuery.validator.addMethod("isLandline", function (value, element) {
    var reg = /^(\d{3}-\d{8}|\d{4}-\d{7,8}|\d{7,13})?$/;
    return this.optional(element) || reg.test(value);
}, "看起来不像座机号呢");


//车牌号校验
jQuery.validator.addMethod("isBrand", function (value, element) {
    return isPlateNo(value);
}, "请输入汉字、字母、数字或短横杠，长度2-20位");

jQuery.validator.addMethod("isBrandCanNull", function (value, element) {
    return isBrandCanNull(value);
}, "请输入汉字、字母、数字或短横杠，长度2-20位");

jQuery.validator.addMethod("isJobNameCanNull", function (value, element) {
    return isBrandCanNull(value, 1, 20);
}, "请输入汉字、字母、数字或短横杠，长度1-20位");

jQuery.validator.addMethod("isfenceName", function (value, element) {
    var reg = /^[0-9a-zA-Z\u4e00-\u9fa5-]{1,20}$/;
    if (reg.test(value.trim())) {
        return true;
    }
    return false;
}, "请输入汉字、字母、数字或短横杠，长度1-20位");

function isPlateNo(plateNo) {
    var reg = /^[0-9a-zA-Z\u4e00-\u9fa5-]{2,20}$/;
    if (reg.test(plateNo.trim())) {
        return true;
    }
    return false;
}

function isBrandCanNull(plateNumber, minLength, maxLength) {
    if (isNull(plateNumber)) {
        return true;
    }
    var minLengthValue = minLength === undefined || minLength === null ? 2 : minLength;
    var maxLengthValue = maxLength === undefined || maxLength === null ? 20 : maxLength;
    // var reg = /^[0-9a-zA-Z\u4e00-\u9fa5-]{minLengthValue,maxLengthValue}$/;
    var reg = new RegExp('^[0-9a-zA-Z\u4e00-\u9fa5-]{' + minLengthValue + ',' + maxLengthValue + '}$');
    if (reg.test(plateNumber.trim())) {
        return true;
    }
    return false;
}

function isNull(param) {
    if (param == null || param == undefined || param == "") {
        return true;
    }
    return false
}

/*function isPlateNo(plateNo){
//    var re = /^[\u4e00-\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}$/;
	// 京津冀晋蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云渝藏陕甘青宁新
	var re = /^[\u4eac\u6d25\u5180\u664b\u8499\u8fbd\u5409\u9ed1\u6caa\u82cf\u6d59\u7696\u95fd\u8d63\u9c81\u8c6b\u9102\u6e58\u7ca4\u6842\u743c\u5ddd\u8d35\u4e91\u6e1d\u85cf\u9655\u7518\u9752\u5b81\u65b0\u6d4b]{1}[A-Z]{1}[A-Z_0-9]{5}$/;
	//香港车牌规则
	var reg1 = /^[A-Z]{2}[0-9]{4}$/;
	if(re.test(plateNo) || reg1.test(plateNo)){
        return true;
    }
    return false;
}*/
jQuery.validator.addMethod("isGroupRequired", function (value, element, param) {
    if (param == "true") {
        if (value) {
            return true;
        } else {
            return false;
        }
    } else {
        return true;
    }

}, "<font color='#E47068'>组织不能为空</font>");
//判断是否是大于0的合法数字 /^(?!(0[0-9]{0,}$))[0-9]{0,}[.]{0,}[0-9]{1,}$/ /^(?:[1-9]\d*|0)(?:\.\d+)?$/
jQuery.validator.addMethod("isFloatAndGtZero", function (value, element) { //
    return this.optional(element) || /^(?:[1-9]\d*|0)(?:\.\d+)?$/.test(value) && value > 0;
}, "只能包含数字、小数点等字符并且要大于零");
jQuery.validator.addMethod("minSize", function (value, element, param) { //
    var len = element.value.replace(/[\u4E00-\u9FA5]/g, 'aa').length;
    var flag = true;
    if (len < param) {
        flag = false;
    }
    return flag;
}, "");
jQuery.validator.addMethod("maxSize", function (value, element, param) { //
    var len = element.value.replace(/[\u4E00-\u9FA5]/g, 'aa').length;
    var flag = true;
    if (len > param) {
        flag = false;
    }
    return flag;
}, "");

/**
 * 分组管理现在每个组织下最多存在100个分组
 */
jQuery.validator.addMethod("assignmentLimit100", function (value, element, param) { //
    var flag = false;
    json_ajax("POST", "/clbs/m/basicinfo/enterprise/assignment/assignCountLimit", null, false, {
        "group": $(param).val()
    }, function (data) {
        if (data != null && data != undefined && data != "") {
            flag = data === 'true';
        }
    });
    return flag;
}, "");
/**
 * 判断树中是否选中车辆
 */
jQuery.validator.addMethod("zTreeChecked", function (value, element, param) {
    var check = false;
    var zTree = $.fn.zTree.getZTreeObj(param),
        nodes = zTree.getCheckedNodes(true),
        v = "";
    for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
            v += nodes[i].name + ",";
        }
    }
    if (v) {
        return true;
    }
    return check;
}, "");
/**
 * 判断树中是否选中企业
 */
jQuery.validator.addMethod("zTreeCheckGroup", function (value, element, param) {
    var zTree = $.fn.zTree.getZTreeObj(param),
        nodes = zTree.getCheckedNodes(true),
        isb = false;
    for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "group") {
            isb = true;
            break;
        }
    }
    return isb;
}, "");

jQuery.validator.addMethod("zTreePeopleChecked", function (value, element, param) {
    var check = false;
    var zTree = $.fn.zTree.getZTreeObj(param),
        nodes = zTree.getCheckedNodes(true),
        v = "";
    for (var i = 0, l = nodes.length; i < l; i++) {
        if (nodes[i].type == "user") {
            v += nodes[i].name + ",";
        }
    }
    if (v) {
        return true;
    }
    return check;
}, "");

/**
 * 开始时间和结束时间必须同时存在或者同时不存在
 */
jQuery.validator.addMethod("timeNotNull", function (value, element, param) { //
    if ($(param).val() != null && $(param).val() != "" && (value == null || value == "")) {
        return false;
    } else {
        return true;
    }
}, "");

/**
 * 根据不同监控对象类型校验终端编号
 */
jQuery.validator.addMethod("checkDeviceNumber", function (value, element, param) { //
    // var Dtype = $(param).val();//终端类型
    // if (Dtype == 5) {//判断人
    //     return this.optional(element) || /^[0-9a-zA-Z]{1,20}$/.test(value);
    // } else {//判断车
    //     if (/^[_-]+$/.test(value)) {//如果全是横杠和下划线则不通过
    //         return this.optional(element) || false;
    //     }
    //     return this.optional(element) || /^[0-9a-zA-Z_-]{7,15}$/.test(value);
    // }
    return this.optional(element) || /^[0-9a-zA-Z]{7,30}$/.test(value);
}, "");
jQuery.validator.addMethod("checkNewDeviceNumber", function (value, element, param) { //
    return this.optional(element) || /^[0-9a-zA-Z]{7,30}$/.test(value);
}, "");

/**
 * 校验人员姓名
 */
jQuery.validator.addMethod("checkPeopleName", function (value, element, param) { //
    if (/^[A-Za-z\u4e00-\u9fa5]{0,8}$/.test(value)) {
        return true;
    }
    return false;
}, "只能输入最多8位的中英文字符");

/**
 * 校验人员编号
 */
jQuery.validator.addMethod("checkRightPeopleNumber", function (value, element, param) { //
    if (/^[A-Za-z0-9\u4e00-\u9fa5_-]+$/.test(value)) {
        return true;
    }
    return false;
}, "");

//电子围栏输入的经纬度验证
jQuery.validator.addMethod("isLngLat", function (value, element, params) {
    var this_value = value;
    if (this_value.indexOf(',') != -1) {
        var this_value_array = this_value.split(',');
        if ((Number(this_value_array[0]) > 73.66 && Number(this_value_array[0]) < 135.05) && (Number(this_value_array[1]) > 3.86 && Number(this_value_array[1]) < 53.55)) {
            return true;
        } else {
            return false;
        }
        ;
    } else {
        return false;
    }
    ;
}, '请输入正确的经纬度');
//电子围栏输入的经度验证
jQuery.validator.addMethod("isLng", function (value, element, params) {
    var this_value = value;
    if (this_value != '') {
        if (Number(this_value) > 73.66 && Number(this_value) < 135.05) {
            return true;
        } else {
            return false;
        }
        ;
    } else {
        return false;
    }
    ;
}, '请输入正确的经度');
//电子围栏输入的纬度验证
jQuery.validator.addMethod("isLat", function (value, element, params) {
    var this_value = value;
    if (this_value != '') {
        if (Number(this_value) > 3.86 && Number(this_value) < 53.55) {
            return true;
        } else {
            return false;
        }
        ;
    } else {
        return false;
    }
    ;
}, '请输入正确的纬度');

//字符验证，只能包含中文和英文(全部匹配,只能包含中文和英文)
jQuery.validator.addMethod("isCN", function (value, element) {
    var reg = /^[a-zA-Z\u4e00-\u9fa5]+$/
    return this.optional(element) || reg.test(value);
}, "只能包含中文、英文");

//上报频率设置-上报起始时间校验
jQuery.validator.addMethod("checkRequiteTime", function (value, element, params) {
    if (params == 9) {
        return true;
    } else {
        return value != null && value != "";
    }
}, "");
//定点和校时-定点时间校验
jQuery.validator.addMethod("checkLocationTimes", function (value, element, params) {
    var obj = document.getElementsByName("locationTimes");
    for (i = 0; i < obj.length; i++) {
        if (obj[i].value) {
            return true;
        }
    }
    return false;
}, "");

/**
 * 如果勾选了，校验是否必填
 */
jQuery.validator.addMethod("isCheckedRequested", function (value, element, param) { //
    var checked = $(param).is(":checked"); //终端类型
    if (checked) { // 勾选
        if (value == null || value == undefined || value == "") {
            return false;
        }
    }
    return true;
}, "");

/**
 * 如果勾选了，校验是否是数字
 */
jQuery.validator.addMethod("isCheckedNumber", function (value, element, param) { //
    var paramlist = param.split(",");
    var checked = $(paramlist[0]).is(":checked"); //终端类型
    if (checked) { // 勾选
        var re = /^[0-9]+$/;
        if (!re.test(value) || Number(value) > Number(paramlist[2]) || Number(value) < Number(paramlist[1])) { //true:数字。false：非数字
            return false;
        }
    }
    return true;
}, "");

/**
 * 如果勾选了，校验是否是是在范围内
 */
jQuery.validator.addMethod("isCheckedNumber2", function (value, element, param) { //
    var paramlist = param.split(",");
    console.log("ffff" + paramlist[2] + paramlist[1])

    var checked = $(paramlist[0]).is(":checked"); //终端类型
    if (checked) { // 勾选
        var re = /^[0-9]+$/;
        if (Number(value) <= Number(paramlist[2]) && Number(value) >= Number(paramlist[1])) { //true:数字。false：非数字
            return true;
        } else {
            return false;
        }
    }
    return true;
}, "");
//校验如果启用了定时唤醒，进行时间校验不能为空
jQuery.validator.addMethod("isCheckedRequested2", function (value, element, param) { //
    var paramlist = param.split(",");
    var checked = $(paramlist[0]).is(":checked"); //终端类型
    var seleted = $(paramlist[1]).val();
    if (checked && seleted == "1") { // 勾选
        if (value == null || value == undefined || value == "") {
            return false;
        }
    }
    return true;
}, "");
//校验如果启用了定时唤醒，进行时间校验关闭时间不能大于开始时间
jQuery.validator.addMethod("isCheckedtime", function (value, element, param) {
    var paramlist = param.split(",");
    var checked = $(paramlist[0]).is(":checked"); //终端类型
    var seleted = $(paramlist[1]).val();
    var time1 = $(paramlist[2]).val();
    var time2 = $(paramlist[3]).val();
    if (checked && seleted == "1") { // 勾选
        if (compTime(time1, time2)) {
            return false;
        }
    }
    return true;
}, "");

/*
 * 校验中英文数字字符串，以传过来的参数做类型校验及输入限制(例：param = "1,1,20" 表示匹配中英文数字范围1-20)
 * 类型：1:中英文数字，2：中英文，3：中文数字，4：英文数字,5：中文,6：英文,7：数字,8:英文数字点
 */
jQuery.validator.addMethod("checkCAENumber", function (value, element, param) {
    var strs = param.split(",");
    var type = strs[0]; //类型
    var minLimit = strs[1]; //最小长度
    var maxLimit = strs[2]; //最大长度
    var typeString;
    switch (type) {
        case "1":
            typeString = "[a-zA-Z0-9\u4e00-\u9fa5]";
            break;
        case "2":
            typeString = "[a-zA-Z\u4e00-\u9fa5]";
            break;
        case "3":
            typeString = "[0-9\u4e00-\u9fa5]";
            break;
        case "4":
            typeString = "[a-zA-Z0-9]";
            break;
        case "5":
            typeString = "[\u4e00-\u9fa5]";
            break;
        case "6":
            typeString = "[a-zA-Z]";
            break;
        case "7":
            typeString = "[0-9]";
            break;
        case "8":
            typeString = "[a-zA-Z0-9.]";
            break;

        default:
            return false;
            break;
    }
    var reg = new RegExp("^" + typeString + "{" + minLimit + "," + maxLimit + "}$");
    if (reg.test(value)) {
        return true;
    } else {
        return false;
    }
}, "");

/**
 * 检查是否是正确版本号
 */
jQuery.validator.addMethod("checkVersion", function (value, element, param) {
    var reg = /^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])(\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]?[0-9])){0,2}$/;
    if (reg.test(value)) {
        return true;
    }
    return false;
}, "");


/**
 * 校验ICCID
 */
jQuery.validator.addMethod("checkICCID", function (value, element, param) { //
    if (/^[A-Z0-9]{20}$/.test(value) || value == '') {
        return true;
    }
    return false;
}, "请输入20位的数字或大写字母");
/**
 * 对讲终端号
 */
jQuery.validator.addMethod("intercomTerminalNo", function (value, element, param) {
    return /^[A-Z0-9]{7}$/.test(value);
}, "请输入7位数字或大写字母");

/**
 * 校验16进制  范围0x00000000~0xFFFFFFFF
 */
jQuery.validator.addMethod("check16", function (value, element, param) { //
    var num = parseInt(value, 16);
    if (isNaN(num) || !/^0x[A-F0-9]{8}$/.test(value)) {
        return false;
    }
    if (num >= parseInt("0x00000000", 16) && num <= parseInt("0xFFFFFFFF", 16)) {
        return true;
    }
    return false;
}, "请输入10位的0x00000000~0xFFFFFFFF");

function compTime(time1, time2) {
    var array1 = time1.split(":");
    var total1 = parseInt(array1[0]) * 3600 + parseInt(array1[1]) * 60;
    var array2 = time2.split(":");
    var total2 = parseInt(array2[0]) * 3600 + parseInt(array2[1]) * 60;
    return total1 - total2 >= 0 ? true : false;
}

/**
 * 检查是否是正确版本号
 */
jQuery.validator.addMethod("equalToDefault", function (value, element, param) {
    var reg = '插卡驾驶员';

    return value != reg;
}, "描述类型不能与驾驶员(IC卡)描述一致");

//时间段交叉判断
jQuery.validator.addMethod("repeatEndCheck", function (value, element, param) {
    var flag = true;
    var len = $('.startTime').length;
    var id = element.id;
    var inx = parseInt(id.substr(id.length - 1), 10);
    // var endDate = $('.endTime').eq(inx).val();
    var endDate = $('#endTime' + inx).val();

    for (var i = 0; i < len; i++) {
        if (i < inx) {
            var compareEndDate = $('.endTime').eq(i).val(),
                compareStartDate = $('.startTime').eq(i).val();

            if (!compareEndDate || !compareStartDate || $('.startTime').eq(i).attr('id') == element.id) continue;

            if (value >= compareEndDate || endDate <= compareStartDate) {
                flag = true;
            } else {
                flag = false;
                break;
            }
        }
    }

    return flag;
}, "此时间段与已设置好的时间段存在冲突,请重新设置");

jQuery.validator.addMethod("compareDates", function (value, element, param) {
    var id = element.id;
    var inx = parseInt(id.substr(id.length - 1), 10);
    var startDate = $('#startTime' + inx).val();

    return value > startDate;
}, "结束时间必须大于开始时间");

// 对讲平台 监控对象验证
jQuery.validator.addMethod("monitorForDispatch", function (value, element) {
    var reg = /^[a-zA-Z\u4e00-\u9fa50-9\-]{2,20}$/;
    return this.optional(element) || reg.test(value);
}, "请输入 2-20 位中文、字母、数字或短横杠");

// 对讲机型管理 原始机型配合原始机型ID一起验证是否为空
jQuery.validator.addMethod("originalModelId", function (value, element) {
    var originalModelId = $(element).parent().find('#index').val();
    if (!originalModelId) {
        $(element).val('');
    }
    return !!originalModelId;
}, "请输入 2-20 位中文、字母、数字或短横杠");

// 查询时间不可超过24小时
jQuery.validator.addMethod("isRange24", function (value, element) {
    var valueArr = value.split('--');
    var start = new Date(valueArr[0]).getTime(),
        end = new Date(valueArr[1]).getTime();

    var range = Math.abs(end - start) / 3600 / 1000;

    if (range <= 24) {
        return true;
    }
    return false;
}, "查询时间不可超过24小时");

// 结束时间必须大于开始时间
jQuery.validator.addMethod("isDateTimeCompare", function (value, element) {
    var valueArr = value.split('--');
    var start = new Date(valueArr[0]).getTime(),
        end = new Date(valueArr[1]).getTime();

    if (start < end) {
        return true;
    }
    return false;
}, "开始时间必须小于结束时间");

jQuery.validator.addMethod("checkStrength", function(password) {
    var strength = 0;
    if (password.match(/([a-zA-Z])/)) {
        strength += 1;
    }
    if (password.match(/([0-9])/)) {
        strength += 1;
    }
    if (password.match(/([~!@#$%^&*()_=+\-\\,.<>;:'"\[\]{}/?])/)) {
        strength += 1;
    }
    return strength >= 2;
}, "密码长度8-25位，且必须包含大小写字母、数字和特殊字符（不含空格）");

jQuery.validator.addMethod("checkStrengthEdit", function(password) {
    if(password == '') return true;

    var strength = 0;
    if (password.match(/([a-zA-Z])/)) {
        strength += 1;
    }
    if (password.match(/([0-9])/)) {
        strength += 1;
    }
    if (password.match(/([~!@#$%^&*()_=+\-\\,.<>;:'"\[\]{}/?])/)) {
        strength += 1;
    }
    return strength >= 2;
}, "密码长度8-25位，且必须包含大小写字母、数字和特殊字符（不含空格）");
