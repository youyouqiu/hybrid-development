//Ajax下载
function ajax_download_file(url) {
    if (typeof (ajax_download_file.iframe) == 'undefined') {
        var iframe = document.createElement('iframe');
        ajax_download_file.iframe = iframe;
        document.body.appendChild(ajax_download_file.iframe);
    }
    ajax_download_file.iframe.src = url;
    ajax_download_file.iframe.style.display = 'none';
}

// 判断浏览器
function getBrowserType() {
    var OsObject = '';
    if (navigator.userAgent.indexOf('MSIE') > 0) {
        OsObject = 'MSIE';
    }
    if (navigator.userAgent.indexOf('Firefox') > 0) {
        OsObject = 'Firefox';
    }
    if (userAgent.indexOf('Safari') > 0 && navigator.userAgent.indexOf('Chrome') < 0) {
        OsObject = 'Safari';
    }
    if (navigator.userAgent.indexOf('Chrome') > 0) {
        OsObject = 'Chrome';
    }
    return OsObject;
}

// 封装的一个JQuery小插件
jQuery.fn.rowspan = function (colIdx) {
    return this.each(function () {
        var that;
        $('tr', this).each(function (row) {
            $('td:eq(' + colIdx + ')', this).filter(':visible').each(function (col) {
                if (that != null && $(this).html() == $(that).html()) {
                    rowspan = $(that).attr('rowSpan');
                    if (rowspan == undefined) {
                        $(that).attr('rowSpan', 1);
                        rowspan = $(that).attr('rowSpan');
                    }
                    rowspan = Number(rowspan) + 1;
                    $(that).attr('rowSpan', rowspan);
                    $(this).hide();
                } else {
                    that = this;
                }
            });
        });
    });
};

var TG_UTIL = {
    // 计算文件大小
    fileSize: function (size) {
        if (size < 1024) {
            return size + ' bytes';
        } else if (size < 1048576) {
            return (Math.round(((size * 10) / 1024)) / 10) + ' KB';
        }
        return (Math.round(((size * 10) / 1048576)) / 10) + ' MB';

    },
    // 去掉字符串头尾空格
    trim: function (str) {
        return str.replace(/(^\s*)|(\s*$)/g, '');
    }
};

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function (fmt) { // author: meizz
    var o = {
        'M+': this.getMonth() + 1, // 月份
        'd+': this.getDate(), // 日
        'h+': this.getHours(), // 小时
        'm+': this.getMinutes(), // 分
        's+': this.getSeconds(), // 秒
        'q+': Math.floor((this.getMonth() + 3) / 3), // 季度
        'S': this.getMilliseconds()
        // 毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp('(' + k + ')').test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)));
        }
    }
    return fmt;
};

/**
 * 自定义 jquery validator
 */
// 身份证编码验证
jQuery.validator.addMethod('idCardNum', function (value, element) {
    return this.optional(element) || (IdCardValidate(value));
}, '请填写有效的身份证');
// 手机号验证
jQuery.validator.addMethod('phoneNum', function (value, element) {
    return this.optional(element) || (validatemobile(value));
}, '请输入正确的手机号');

var Wi = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1]; // 加权因子
var ValideCode = [1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2]; // 身份证验证位值.10代表X
function IdCardValidate(idCard) {
    idCard = TG_UTIL.trim(idCard.replace(/ /g, '')); // 去掉字符串头尾空格
    if (idCard.length == 15) {
        return isValidityBrithBy15IdCard(idCard); // 进行15位身份证的验证
    } else if (idCard.length == 18) {
        var a_idCard = idCard.split(''); // 得到身份证数组
        if (isValidityBrithBy18IdCard(idCard) && isTrueValidateCodeBy18IdCard(a_idCard)) { // 进行18位身份证的基本验证和第18位的验证
            return true;
        }
        return false;

    }
    return false;

}

/**
 * 判断身份证号码为18位时最后的验证位是否正确
 *
 * @param a_idCard
 *            身份证号码数组
 * @return
 */
function isTrueValidateCodeBy18IdCard(a_idCard) {
    var sum = 0; // 声明加权求和变量
    if (a_idCard[17].toLowerCase() == 'x') {
        a_idCard[17] = 10; // 将最后位为x的验证码替换为10方便后续操作
    }
    for (var i = 0; i < 17; i++) {
        sum += Wi[i] * a_idCard[i]; // 加权求和
    }
    valCodePosition = sum % 11; // 得到验证码所位置
    if (a_idCard[17] == ValideCode[valCodePosition]) {
        return true;
    }
    return false;

}

/**
 * 验证18位数身份证号码中的生日是否是有效生日
 *
 * @param idCard
 *            18位书身份证字符串
 * @return
 */
function isValidityBrithBy18IdCard(idCard18) {
    var year = idCard18.substring(6, 10);
    var month = idCard18.substring(10, 12);
    var day = idCard18.substring(12, 14);
    var temp_date = new Date(year, parseFloat(month) - 1, parseFloat(day));
    // 这里用getFullYear()获取年份，避免千年虫问题
    if (temp_date.getFullYear() != parseFloat(year) || temp_date.getMonth() != parseFloat(month) - 1 || temp_date.getDate() != parseFloat(day)) {
        return false;
    }
    return true;

}

/**
 * 验证15位数身份证号码中的生日是否是有效生日
 *
 * @param idCard15
 *            15位书身份证字符串
 * @return
 */
function isValidityBrithBy15IdCard(idCard15) {
    var year = idCard15.substring(6, 8);
    var month = idCard15.substring(8, 10);
    var day = idCard15.substring(10, 12);
    var temp_date = new Date(year, parseFloat(month) - 1, parseFloat(day));
    // 对于老身份证中的你年龄则不需考虑千年虫问题而使用getYear()方法
    if (temp_date.getYear() != parseFloat(year) || temp_date.getMonth() != parseFloat(month) - 1 || temp_date.getDate() != parseFloat(day)) {
        return false;
    }
    return true;

}

function validatemobile(mobile) {
    if (mobile.length == 0 || mobile.length != 11) {
        return false;
    }
    var myreg = /^(((13[0-9]{1})|145|147|(13[0-9]{1})|(18[0-9]{1}))+\d{8})$/;
    return myreg.test(mobile);
}

/**
 * 转义文本
 * @param sHtml 需要转义的文本
 * @returns 转义后的文本
 */
function html2Escape(sHtml, jumpBr) {
    if (!sHtml) {
        return '';
    }
    if (jumpBr) {// 是否忽略<br/>标签
        sHtml = sHtml.replace(/<br\/>/g, '_#@#_');
    }
    var targetVal = sHtml.replace(/[<>&"']/g, function (c) {
        return {
            '<': '&lt;',
            '>': '&gt;',
            '&': '&amp;',
            '"': '&quot;',
            '\'': '&quot;'
        }[c];
    });
    return targetVal.replace(/_#@#_/g, '<br/>');
}


/**
 * 日期转换
 */
function formatDate(timestamp) {
    var timeType = (typeof timestamp);
    var time = new Date(timeType != 'object' ? parseInt(timestamp) : timestamp);
    var year = time.getFullYear();
    var month = time.getMonth() + 1;
    var date = time.getDate();
    /* var hours = time.getHours();
     var minutes = time.getMinutes();
     var seconds = time.getSeconds();*/
    return year + '-' + add0(month) + '-' + add0(date) /*+ ' ' + add0(hours) + ':' + add0(minutes) + ':' + add0(seconds)*/;
}

function add0(m) {
    return m < 10 ? '0' + m : m;
}

/**
 * 日期转换
 */
function formatDateAll(timestamp) {
    var timeType = (typeof timestamp);
    var time = new Date(timeType != 'object' ? parseInt(timestamp) : timestamp);
    var year = time.getFullYear();
    var month = time.getMonth() + 1;
    var date = time.getDate();
    var hours = time.getHours();
    var minutes = time.getMinutes();
    var seconds = time.getSeconds();
    return year + '-' + add0(month) + '-' + add0(date) + ' ' + add0(hours) + ':' + add0(minutes) + ':' + add0(seconds);
}

/**
 * 组织树下拉框显示隐藏
 */
function showMenuContent() {
    var _this = $(this);
    var targetMenu = _this.siblings('.menuContent');
    var targetMenuId = targetMenu.attr('id');

    if (targetMenu.is(':hidden')) {
        var width = _this.parent().width();
        targetMenu.css('width', width + 'px');
        $(window).resize(function () {
            var width = _this.parent().width();
            targetMenu.css('width', width + 'px');
        });
        targetMenu.slideDown('fast');
    } else {
        targetMenu.slideUp('fast');
    }

    $('body').off('mousedown').on('mousedown', function (e) {
        onBodyDownClick(e, targetMenuId);
    });
}

function onBodyDownClick(event, targetMenuId) {
    var inputId = $('#' + targetMenuId).siblings('input').attr('id');
    if (event.target.id == inputId || event.target.id == targetMenuId) {
        return;
    }
    if ($(event.target).parents('#' + targetMenuId).length > 0) {
        return;
    }
    hideMenuContent(targetMenuId);
}

function hideMenuContent(targetMenuId) {
    $('#' + targetMenuId).slideUp('fast');
    $('body').off('mousedown');
}

/**
 * 获取地址栏url参数
 */
(function ($) {
    $.getUrlParam = function (name) {
        var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    };
}(jQuery));

/**
 * 解析压缩时间字符串
 */
function parseDate2Str(strangeDateStr) {
    var str = strangeDateStr.toString();
    var year = '20' + str.substr(0, 2);
    var month = str.substr(2, 2);
    var date = str.substr(4, 2);
    var hour = str.substr(6, 2);
    var minute = str.substr(8, 2);
    var second = str.substr(10, 2);
    return year + '-' + month + '-' + date + ' ' + hour + ':' + minute + ':' + second;
}

/**
 * 实现输入框placeholder效果
 */
function initPlaceholder(target) {
    var showVal = $(target).attr('data-placeholder');
    if (showVal) {
        var html = '<div class="myHolder">' + showVal + '</div>';
        $(target).after(html);
        $(target).on('input propertychange', function () {
            var curVal = $(this).val();
            if (curVal != '') {
                $(this).siblings('.myHolder').hide();
            } else {
                $(this).siblings('.myHolder').show();
            }
        });
        $('.myHolder').on('click', function () {
            $($(this).prevAll('input')[0]).focus();
        });
    }
}


/**
 * 对象数组去重
 * data:对象数组; repeatFlag:数组中的对象唯一标识(默认为id)
 * */
function objArrRemoveRepeat(data, repeatFlag) {
    var idObj = {};
    var newData = data.reduce(function (initValue, curValue) {
        var attrName = repeatFlag ? curValue[repeatFlag] : curValue.id;
        idObj[attrName] ? '' : idObj[attrName] = true && initValue.push(curValue);
        return initValue;
    }, []);
    return newData;
}

/**
 * 封装一个map集合
 * */
function mapVehicle() {
    this.elements = new Array();
    //获取MAP元素个数
    this.size = function () {
        return this.elements.length;
    };
    //判断MAP是否为空
    this.isEmpty = function () {
        return (this.elements.length < 1);
    };
    //删除MAP所有元素
    this.clear = function () {
        this.elements = new Array();
    };
    //向MAP中增加元素（key, value)
    this.put = function (_key, _value) {
        this.elements.push({
            key: _key,
            value: _value
        });
    };
    //删除指定KEY的元素，成功返回True，失败返回False
    this.remove = function (_key) {
        var bln = false;
        try {
            for (var i = 0, len = this.elements.length; i < len; i++) {
                if (this.elements[i].key == _key) {
                    this.elements.splice(i, 1);
                    return true;
                }
            }
        } catch (e) {
            bln = false;
        }
        return bln;
    };
    //获取指定KEY的元素值VALUE，失败返回NULL
    this.get = function (_key) {
        try {
            for (var i = 0, len = this.elements.length; i < len; i++) {
                if (this.elements[i].key == _key) {
                    return this.elements[i].value;
                }
            }
        } catch (e) {
            return null;
        }
    };
    //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
    this.element = function (_index) {
        if (_index < 0 || _index >= this.elements.length) {
            return null;
        }
        return this.elements[_index];
    };
    //判断MAP中是否含有指定KEY的元素
    this.containsKey = function (_key) {
        var bln = false;
        try {
            for (var i = 0, len = this.elements.length; i < len; i++) {
                if (this.elements[i].key == _key) {
                    bln = true;
                }
            }
        } catch (e) {
            bln = false;
        }
        return bln;
    };
    //判断MAP中是否含有指定VALUE的元素
    this.containsValue = function (_value) {
        var bln = false;
        try {
            for (var i = 0, len = this.elements.length; i < len; i++) {
                if (this.elements[i].value == _value) {
                    bln = true;
                }
            }
        } catch (e) {
            bln = false;
        }
        return bln;
    };
    //获取MAP中所有VALUE的数组（ARRAY）
    this.values = function () {
        var arr = new Array();
        for (var i = 0, len = this.elements.length; i < len; i++) {
            arr.push(this.elements[i].value);
        }
        return arr;
    };
    //获取MAP中所有KEY的数组（ARRAY）
    this.keys = function () {
        var arr = new Array();
        for (var i = 0, len = this.elements.length; i < len; i++) {
            arr.push(this.elements[i].key);
        }
        return arr;
    };
};

/**
 * 渲染车牌颜色下拉框
 * select下拉框class必须设为:plateColorSelect
 * */
function renderPlateColorSelect() {
    var colorStr = '<option value="1">蓝色</option>' +
        '<option value="2" selected>黄色</option>' +
        '<option value="3">黑色</option>' +
        '<option value="4">白色</option>' +
        '<option value="5">绿色</option>' +
        '<option value="94">渐变绿色</option>' +
        '<option value="93">黄绿色</option>' +
        '<option value="91">农黄</option>' +
        '<option value="90">农蓝</option>' +
        '<option value="92">农绿</option>' +
        '<option value="9">其他</option>';
    $('.plateColorSelect').html(colorStr);
}

/**
 * 获取车牌颜色
 * colorNumber:车牌颜色值
 */
function getPlateColor(colorNumber) {
    if (colorNumber == '1') {
        return '蓝色';
    } else if (colorNumber == '2') {
        return '黄色';
    } else if (colorNumber == '3') {
        return '黑色';
    } else if (colorNumber == '4') {
        return '白色';
    } else if (colorNumber == '5') {
        return '绿色';
    } else if (colorNumber == '94') {
        return '渐变绿色';
    } else if (colorNumber == '93') {
        return '黄绿色';
    } else if (colorNumber == '90') {
        return '农蓝';
    } else if (colorNumber == '91') {
        return '农黄';
    } else if (colorNumber == '92') {
        return '农绿';
    } else if (colorNumber == '9') {
        return '其他';
    }
    return '';

}

/**
 * 获取昨天的日期
 */
function getYesterDay() {
    var date = new Date();
    var day = date.getDate();
    date.setDate(day - 1);
    var year = date.getFullYear(),
        month = date.getMonth(),
        day = date.getDate();

    return year + '-' + add0(month + 1) + '-' + add0(day);
}

/**
 * 根据协议值,获取协议名称
 **/
function getProtocolName(data) {
    if (typeof data == 'number') {
        data = data.toString();
    }
    switch (data) {
        case "0":
            return "交通部JT/T808-2011(扩展)";
        case "1":
            return "交通部JT/T808-2013";
        case "11":
            return "交通部JT/T808-2019";
        case "2":
            return "移为";
        case "3":
            return "天禾";
        case "6":
            return "KKS";
        case "7":
            return "KKS";
        case "5":
            return "BDTD-SM";
        case "8":
            return "BSJ-A5";
        case "9":
            return "ZYM";
        case "10":
            return "F3超长待机";
        case "12":
            return "交通部JT/T808-2013(川标)";
        case "13":
            return "交通部JT/T808-2013(冀标)";
        case "14":
            return "交通部JT/T808-2013(桂标)";
        case "15":
            return "交通部JT/T808-2013(苏标)";
        case "16":
            return "交通部JT/T808-2013(浙标)";
        case "17":
            return "交通部JT/T808-2013(吉标)";
        case "18":
            return "交通部JT/T808-2013(陕标)";
        case "19":
            return "交通部JT/T808-2013(赣标)";
        case "20":
            return "交通部JT/T808-2019(沪标)";
        case "21":
            return "交通部JT/T808-2019(中位)";
        case "22":
            return "KKS-EV25";
        case "23":
            return "JT/T808-2011(1078报批稿)";
        case "24":
            return "交通部JT/T808-2019(京标)";
        case "25":
            return "交通部JT/T808-2019(黑标)";
        case "26":
            return "交通部JT/T808-2019(鲁标)";
        case "27":
            return "交通部JT/T808-2013(湘标)";
        case "28":
            return "交通部JT/T808-2019(粤标)";
        case "97":
            return "OBD-GB-2018";
        case "99":
            return "OBD-杭州-2018";
        default:
            return "";
    }
}

/**
 * 输入框输入类型限制
 *  @param selector 输入框选择器(id/class...)
 *  @param reg 禁止输入类型的正则校验规则
 **/
function inputValueFilter(selector, reg) {
    inputFlag = true;
    var curInput = $(selector);
    // 因为中文的拼音输入也会触发input事件,需做特殊处理
    curInput.on('compositionstart', function () {
        inputFlag = false;
    });
    curInput.on('compositionend', function () {
        inputFlag = true;
    });
    curInput.on('input', function () {
        var _this = $(this);
        setTimeout(function () {
            if (inputFlag) {
                reg = reg || /[^\u4e00-\u9fa5a-zA-Z0-9-]/g;
                var newValue = _this.val().replace(reg, '');
                _this.val(newValue);
            }
        }, 0)
    });
}

/**
 * 补零
 **/
function fillZero(num) {
    var m = num;
    if (num.toString().length == 1) {
        m = "0" + num;
    }
    return m;
}

/**
 * 表单提交添加hash值，防止表单新增修改重复提交。
 * addHashCode 应用管理
 * addHashCode1 其余模块
 */
function addHashCode(formDom) {
    if($("#resubmitToken").length > 0) $("#resubmitToken").remove();

    const inputs = formDom.find('input');
    const options = formDom.find('option:selected');
    let value = '';
    let checkedVal = '';
    let isChecked = false;

    for(let i = 0; i < options.length; i++) {
        value += options[i].value;
    }
    for(let i = 0; i < inputs.length; i++) {
        if($(inputs[i]).is(':hidden')) continue;

        if($(inputs[i]).is(':checked')){
            isChecked = true;
            checkedVal += inputs[i].value;
        }

        value += inputs[i].value;
    }

    if(isChecked) value = checkedVal;

    value =  value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);

    let newInput = document.createElement("input");
    newInput.id = 'resubmitToken';
    newInput.type = "hidden";
    newInput.name = 'resubmitToken';
    newInput.value = value;
    formDom.append(newInput);
}

function  addHashCode1(formDom) {
    if($("#resubmitToken").length > 0) $("#resubmitToken").remove();

    const data = formDom.serializeArray();
    let value = '';

    for(let i = 0; i < data.length; i++) {
        value += data[i].value
    }

    value =  value.split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0);
    let newInput = document.createElement("input");
    newInput.id = 'resubmitToken';
    newInput.type = "hidden";
    newInput.name = 'resubmitToken';
    newInput.value = value;
    formDom.append(newInput);
}
/**
 * 报表导出最大数量控制
 * @param url 请求地址
 * @param params 请求参数
 * @param max 最大数量限制 默认 60000
 * @param method 请求方法 默认POST
 * @returns {boolean}
 */
function exportMaxValidate(url, params, max, method){
    var flag = true
    var formData = new FormData();
    max = max ? max : 60000
    method = method ? method.toUpperCase() : "POST"
    for(var key in params){
        formData.append(key, params[key]);
    }
    $.ajax({
        url: url,
        type: method,
        data: formData,
        async: false,
        contentType: false,
        processData: false,
        success: function (data) {
            if(data.totalRecords > max){
                flag = false
            }
        },
        error: function (data) {
            flag = false
        }
    });
    return flag
}

/**
 * 用于导出功能--限制60000条以上数据导出
 * */
function getRecordsNum(id){
    id = id ? id : 'dataTable_info';
    var origin = $('#' + id).html();
    if(!origin) return 0;
    var match = origin.match((/共(.*)条/));
    if(!match) return 0;
    if(!match[1]) return 0;
    var res = match[1].replace(/,/g,'');
    return Number(res)
}