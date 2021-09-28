/**
 * pagination.js 1.5.1
 * A jQuery plugin to provide simple yet fully customisable pagination.
 * @version 1.5.1
 * @author mss
 * @url https://github.com/Maxiaoxiang/jQuery-plugins
 *
 * @调用方法
 * $(selector).pagination(option, callback);
 * -此处callback是初始化调用，option里的callback是点击页码后调用
 *
 * -- example --
 * $(selector).pagination({
 *     ... // 配置参数
 *     callback: function(api) {
 *         console.log('点击页码调用该回调'); //切换页码时执行一次回调
 *     }
 * }, function(){
 *     console.log('初始化'); //插件初始化时调用该方法，比如请求第一次接口来初始化分页配置
 * });
 */
;
(function (factory) {
    if (typeof define === "function" && (define.amd || define.cmd) && !jQuery) {
        // AMD或CMD
        define(["jquery"], factory);
    } else if (typeof module === 'object' && module.exports) {
        // Node/CommonJS
        module.exports = function (root, jQuery) {
            if (jQuery === undefined) {
                if (typeof window !== 'undefined') {
                    jQuery = require('jquery');
                } else {
                    jQuery = require('jquery')(root);
                }
            }
            factory(jQuery);
            return jQuery;
        };
    } else {
        //Browser globals
        factory(jQuery);
    }
}(function ($) {

    //配置参数
    var defaults = {
        totalData: 0, //数据总条数
        showData: 0, //每页显示的条数
        pageCount: 9, //总页数,默认为9
        current: 1, //当前第几页
        prevCls: 'prev', //上一页class
        nextCls: 'next', //下一页class
        prevContent: '<', //上一页内容
        nextContent: '>', //下一页内容
        activeCls: 'active', //当前页选中状态
        coping: false, //首页和尾页
        isHide: false, //当前页数为0页或者1页时不显示分页
        homePage: '', //首页节点内容
        endPage: '', //尾页节点内容
        keepShowPN: false, //是否一直显示上一页下一页
        mode: 'unfixed', //分页模式，unfixed：不固定页码数量，fixed：固定页码数量
        count: 4, //mode为unfixed时显示当前选中页前后页数，mode为fixed显示页码总数
        jump: false, //跳转到指定页数
        jumpIptCls: 'jump-ipt', //文本框内容
        jumpBtnCls: 'jump-btn', //跳转按钮
        jumpBtn: '跳转', //跳转按钮文本
        callback: function () {} //回调
    };

    var Pagination = function (element, options) {
        //全局变量
        var opts = options, //配置
            current, //当前页
            $document = $(document),
            $obj = $(element); //容器

        /**
         * 设置总页数
         * @param {int} page 页码
         * @return opts.pageCount 总页数配置
         */
        this.setPageCount = function (page) {
            return opts.pageCount = page;
        };

        /**
         * 获取总页数
         * 如果配置了总条数和每页显示条数，将会自动计算总页数并略过总页数配置，反之
         * @return {int} 总页数
         */
        this.getPageCount = function () {
            return opts.totalData && opts.showData ? Math.ceil(parseInt(opts.totalData) / opts.showData) : opts.pageCount;
        };

        /**
         * 获取当前页
         * @return {int} 当前页码
         */
        this.getCurrent = function () {
            return current;
        };

        /**
         * 填充数据
         * @param {int} 页码
         */
        this.filling = function (index) {
            var html = '<ul class="pagination">';
            current = parseInt(index) || parseInt(opts.current); //当前页码
            var pageCount = this.getPageCount(); //获取的总页数
            switch (opts.mode) { //配置模式
                case 'fixed': //固定按钮模式
                    if (current == 1) {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    } else {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    }

//                    var start = current > opts.count - 1 ? current + opts.count - 1 > pageCount ? current - (opts.count - (pageCount - current)) : current - 2 : 1;
//                    var end = current + opts.count - 1 > pageCount ? pageCount : start + opts.count;
                    var start = 1;
                    var end = opts.count;
                    for (; start <= end; start++) {
                        if (start != current) {
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="' + start + '">' + start + '</a></li>';
                        } else {
                            html += '<li class="paginate_button active"><a href="javascript:;" class="' + opts.activeCls + '">' + start + '</a></li>';
                        }
                    }

                    if (current == end) {
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        if (opts.coping) {
                            var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                            html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        }
                    } else {
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        if (opts.coping) {
                            var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        }
                    }
                    break;
                case 'unfixed': //不固定按钮模式
                    if (current == 1) {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    } else {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    }

//                    if (opts.keepShowPN || current > 1) { //上一页
//                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
//                    } else {
//                        if (opts.keepShowPN == false) {
//                            $obj.find('.' + opts.prevCls) && $obj.find('.' + opts.prevCls).remove();
//                        }
//                    }
                    if (current >= opts.count + 2 && current != 1 && pageCount != opts.count) {
                        var home = opts.coping && opts.homePage ? opts.homePage : '1';
                        html += opts.coping ? '<li class="paginate_button"><span>...</span></li>' : '';
                    }
                    var start = (current - opts.count) <= 1 ? 1 : (current - opts.count);
                    var end = (current + opts.count) >= pageCount ? pageCount : (current + opts.count);
                    for (; start <= end; start++) {
                        if (start <= pageCount && start >= 1) {
                            if (start != current) {
                                html += '<li class="paginate_button"><a href="javascript:;" data-page="' + start + '">' + start + '</a></li>';
                            } else {
//                                html += '<li class="paginate_button active"><span class="' + opts.activeCls + '">' + start + '</span></li>';
                                html += '<li class="paginate_button active"><a href="javascript:;" class="' + opts.activeCls + '">' + start + '</a></li>';
                            }
                        }

//                        if (start != current) {
//                            html += '<li class="paginate_button"><a href="javascript:;" data-page="' + start + '">' + start + '</a></li>';
//                        } else {
//                            html += '<li class="paginate_button active"><a href="javascript:;" class="' + opts.activeCls + '">' + start + '</a></li>';
//                        }

                    }
                    if (current + opts.count < pageCount && current >= 1 && pageCount > opts.count) {
                        var end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                        html += opts.coping ? '<li class="paginate_button"><span>...</span></li>' : '';
                    }
//                    if (opts.keepShowPN || current < pageCount) { //下一页
//                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
//                    } else {
//                        if (opts.keepShowPN == false) {
//                            $obj.find('.' + opts.nextCls) && $obj.find('.' + opts.nextCls).remove();
//                        }
//                    }
                    if (current == end) {
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        if (opts.coping) {
                            var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                            html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        }
                    } else {
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        if (opts.coping) {
                            var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        }
                    }
                    break;
                case 'easy': //简单模式
                    break;
                case 'justPrevAndNext': // wjk ,只有上一页和下一页
                    if (current == 1) {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    } else {
                        if (opts.coping) {
                            var home = opts.coping && opts.homePage ? opts.homePage : '1';
                            html += '<li class="paginate_button"><a href="javascript:;" data-page="1">' + home + '</a></li>';
                        }
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.prevCls + '">' + opts.prevContent + '</a></li>';
                    }
                    if (current >= opts.count + 2 && current != 1 && pageCount != opts.count) {
                        var home = opts.coping && opts.homePage ? opts.homePage : '1';
                        html += opts.coping ? '<li class="paginate_button" style="display:none;"><span>...</span></li>' : '';
                    }
                    var start = (current - opts.count) <= 1 ? 1 : (current - opts.count);
                    var end = (current + opts.count) >= pageCount ? pageCount : (current + opts.count);
                    for (; start <= end; start++) {
                        if (start <= pageCount && start >= 1) {
                            if (start != current) {
                                html += '<li class="paginate_button" style="display:none;"><a href="javascript:;" data-page="' + start + '">' + start + '</a></li>';
                            } else {
                                html += '<li class="paginate_button active" style="display:none;"><a href="javascript:;" class="' + opts.activeCls + '">' + start + '</a></li>';
                            }
                        }

                    }
                    if (current + opts.count < pageCount && current >= 1 && pageCount > opts.count) {
                        var end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                        html += opts.coping ? '<li class="paginate_button" style="display:none;"><span>...</span></li>' : '';
                    }
                    if (current == end) {
                        html += '<li class="paginate_button disabled"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        // if (opts.coping) {
                        //     var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                        //     html += '<li class="paginate_button disabled"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        // }
                    } else {
                        html += '<li class="paginate_button"><a href="javascript:;" class="' + opts.nextCls + '">' + opts.nextContent + '</a></li>';
                        // if (opts.coping) {
                        //     var _end = opts.coping && opts.endPage ? opts.endPage : pageCount;
                        //     html += '<li class="paginate_button"><a href="javascript:;" data-page="' + pageCount + '">' + _end + '</a></li>';
                        // }
                    }
                    break;
                default:
            }
            html += opts.jump ? '<input type="text" class="' + opts.jumpIptCls + '"><a href="javascript:;" class="' + opts.jumpBtnCls + '">' + opts.jumpBtn + '</a>' : '';
            html += '</ul>';
            $obj.empty().html(html);
        };

        //绑定事件
        this.eventBind = function () {
            var that = this;
            var pageCount = that.getPageCount(); //总页数
            var index = 1;
            $obj.off().on('click', 'a', function () {
                if ($(this).parent('li').hasClass('disabled')) {
                    return;
                }
                if ($(this).hasClass(opts.nextCls)) {
                    if ($obj.find('.' + opts.activeCls + ' a').text() >= pageCount) {
                        $(this).addClass('disabled');
                        return false;
                    }
                    index = parseInt($obj.find('.' + opts.activeCls + ' a').text()) + 1;

                    riskEvidence.prevOrnext = 'next' // wjk

                } else if ($(this).hasClass(opts.prevCls)) {
                    if ($obj.find('.' + opts.activeCls + ' a').text() <= 1) {
                        $(this).addClass('disabled');
                        return false;
                    }
                    index = parseInt($obj.find('.' + opts.activeCls + ' a').text()) - 1;

                    riskEvidence.prevOrnext = 'prev' // wjk
                    var arr = riskEvidence.pageSearchAfterArr;
                    arr.pop();
                    riskEvidence.pageSearchAfterArr = arr
                    if (riskEvidence.pageSearchAfterArr.length) {
                        riskEvidence.prevSearchAfter = riskEvidence.pageSearchAfterArr[riskEvidence.pageSearchAfterArr.length - 2]
                        riskEvidence.nextSearchAfter = riskEvidence.pageSearchAfterArr[riskEvidence.pageSearchAfterArr.length - 1]
                    } else {
                        riskEvidence.prevSearchAfter = ''
                    }

                } else if ($(this).hasClass(opts.jumpBtnCls)) {
                    if ($obj.find('.' + opts.jumpIptCls).val() !== '') {
                        index = parseInt($obj.find('.' + opts.jumpIptCls).val());
                    } else {
                        return;
                    }
                } else {
                    index = parseInt($(this).data('page'));

                    // wjk
                    riskEvidence.prevOrnext = ''
                    if (index == '1') {
                        riskEvidence.prevSearchAfter = ''
                        riskEvidence.nextSearchAfter = riskEvidence.pageSearchAfterArr[0]
                        riskEvidence.pageSearchAfterArr = []
                    }
                }
                that.filling(index);
                typeof opts.callback === 'function' && opts.callback(that);
            });
            //输入跳转的页码
            $obj.on('input propertychange', '.' + opts.jumpIptCls, function () {
                var $this = $(this);
                var val = $this.val();
                var reg = /[^\d]/g;
                if (reg.test(val)) $this.val(val.replace(reg, ''));
                (parseInt(val) > pageCount) && $this.val(pageCount);
                if (parseInt(val) === 0) $this.val(1); //最小值为1
            });
            //回车跳转指定页码
            $document.keydown(function (e) {
                if (e.keyCode == 13 && $obj.find('.' + opts.jumpIptCls).val()) {
                    var index = parseInt($obj.find('.' + opts.jumpIptCls).val());
                    that.filling(index);
                    typeof opts.callback === 'function' && opts.callback(that);
                }
            });
        };

        //初始化
        this.init = function () {
            this.filling(opts.current);
            this.eventBind();
            if (opts.isHide && this.getPageCount() == '1' || this.getPageCount() == '0') {
                $obj.hide();
            } else {
                $obj.show();
            }
        };
        this.init();
    };

    $.fn.pagination = function (parameter, callback) {
        if (typeof parameter == 'function') { //重载
            callback = parameter;
            parameter = {};
        } else {
            parameter = parameter || {};
            callback = callback || function () {};
        }
        var options = $.extend({}, defaults, parameter);
        return this.each(function () {
            var pagination = new Pagination(this, options);
            callback(pagination);
        });
    };

}));

var riskEvidence;
(function (window, $) {
    //车辆id列表
    var vehicleIds = "";
    //开始时间
    var startTime;
    //结束时间
    var endTime;
    var checkFlag = false; //判断组织节点是否是勾选操作
    var chkFlag = true; //快速删除和模糊查询不自动勾选
    var size;//当前权限监控对象数量
    var _evidenceType, _riskType, _riskEvent, _riskEventValue;
    var riskTypeValue="";
    var deleteIds = []; // 被删除的字段，点击查询置空
    //模糊查询
    var zTreeIdJson = {};
    var bflag = true;
    var crrentSubV = [];
    var ifAllCheck = true;
    var searchFlag = true;

    var ajaxDataParamFun = function (d,ifInquireClick) {
        d.vehicleIds = vehicleIds;
        d.startTime = startTime;
        d.endTime = endTime;
        d.evidenceType = $('#evidenceType').val();
        d.deleteIds = deleteIds.join(',');
        d.start = (currentPage - 1) * length;
        d.length = length;

        //下一页传参
        if (ifInquireClick == 'inquireClick') { //按钮查询时不传searchAfter
            d.searchAfter = '';
            riskEvidence.pageSearchAfterArr = [];
            riskEvidence.prevSearchAfter = '';
            riskEvidence.nextSearchAfter = '';
        } else {
            if (riskEvidence.prevOrnext == 'next') {
                d.searchAfter = riskEvidence.nextSearchAfter;
            } else if (riskEvidence.prevOrnext == 'prev') {
                d.searchAfter = riskEvidence.prevSearchAfter;
            }

        }
            console.log(d.searchAfter);

        if ($("#highsearch").css('display') == "block") {
            d.riskNumber = $("#riskNumber").val(); //模糊查询
            d.riskType = riskTypeValue;
            d.riskLevel = comLevelTreeCheckedVal;
            d.brand = $("#charSelect").val();
            d.driver = $("#driver").val();
            d.status = $('#status option:selected').text();
            d.dealUser = $("#dealUser").val();
            d.visitTime = $("#visitTime option:selected").text();
            d.riskResult = $("#riskResult").val();
            var __event = $('#riskEvent').val();
            if(__event == "null"){
                __event = null;
            }
            d.riskEvent = __event&&__event.length>0 ? __event : '';
        }
        if (currentBrand != null && currentBrand != '所    有') {
            d.brand = currentBrand;
        }
        return d;
    };
    var currentBrand = null;
    var currentPage = 1;
    var totalPage = null;
    var brands = null;
    var currentDisabledType = '';
    var length = 10;
    var zTreeIdJson = {};
    var comLevelTreeCheckedVal="";//风险事件下拉框值
    var alarmTypeSetting;
    var riskEventData={
        "所有":null,
    };
    var typeEvent = {};
    var riskTypeNodes = [
        {
            name: "所有",
            open: true,
            children: []
        }
    ];

    riskEvidence = {
        prevOrnext:'',
        pageSearchAfterArr: [],
        //点击上一页和下一页传的参数
        prevSearchAfter:'',
        nextSearchAfter:'',
        init: function () {
            var deviceDataList=[
                {"name":"所    有","value":null}
            ];
            riskEvidence.initShow({
                data:deviceDataList,
                onSetValue:function(data){
                }
            });
            riskEvidence.initSelect();

            //高级查询
            riskEvidence.getRiskTypeDatas();
            riskEvidence.setVehicleTree();
        },
        //获取风险类型事件
        getRiskTypeDatas: function(){
            var obj = Object.keys(riskEventData);
            if (obj.length > 1) {
                return;
            }
            json_ajax("get", "/clbs/adas/r/reportManagement/adasRiskEvidence/getFunctionIdAndType",
                "json", true, {}, function (data) {
                    if (data.success) {
                        var datas = data.obj.event;
                        // if(datas){
                        //     var i=0;
                        //     for(var key in datas){
                        //         i++;
                        //         var values = datas[key];
                        //         var obj = {
                        //             name: key,
                        //             value: i.toString()
                        //         };
                        //
                        //         var eventArr = [];
                        //         for(var key2 in values){
                        //             eventArr.push(key2);
                        //             typeEvent[key] = eventArr;
                        //         }
                        //
                        //         //riskTypeNodes[0].children.push(obj);
                        //         Object.assign(riskEventData, values);
                        //     }
                        //}

                        for (var key in datas) {
                            var values = datas[key];
                            Object.assign(riskEventData, values);
                            var eventArr = [];
                            for(var key2 in values){
                                eventArr.push(key2);
                                typeEvent[key] = eventArr;
                            }
                        }

                        riskEvidence.renderOptions(datas)

                        var riskTypes = data.obj.riskType;
                        for (var key in riskTypes){
                            var obj = {
                                name: key,
                                value: riskTypes[key]
                            };
                            riskTypeNodes[0].children.push(obj);
                        }
                    } else {
                        if(data.msg){
                            layer.msg(data.msg);
                        }
                    }
                    riskEvidence.setRiskTypeTree();
                });
        },
        //渲染options
        renderOptions: function(data){
            if(!data) return
            $('#riskEvent').empty()
            var riskEventHtml = '<option value="">所有</option>'
            var getHtml = function (obj) {
                for(var key in obj){
                    typeof obj[key] == 'object' ? getHtml(obj[key]) : riskEventHtml += '<option value="' + obj[key] + '">' + key + '</option>'
                }
            }
            getHtml(data)
            $('#riskEvent').html(riskEventHtml)
        },
        //风险类型
        setRiskTypeTree: function(){
            alarmTypeSetting = {
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics",
                    showIcon : false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: riskEvidence.onCheckChangeValue
                }
            };
            $.fn.zTree.init($("#alarmTypeTree"), alarmTypeSetting, riskTypeNodes);
        },
        //车辆树
        setVehicleTree: function(){
            var setting = {
                async: {
                    url: riskEvidence.getRiskDisposeTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: riskEvidence.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: riskEvidence.beforeClickVehicle,
                    onAsyncSuccess: riskEvidence.zTreeOnAsyncSuccess,
                    beforeCheck: riskEvidence.zTreeBeforeCheck,
                    onCheck: riskEvidence.onCheckVehicle,
                    onNodeCreated: riskEvidence.zTreeOnNodeCreated,
                    onExpand: riskEvidence.zTreeOnExpand
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                riskEvidence.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/m/functionconfig/fence/bindfence/vehicleTreeFuzzy",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": $('#queryType').val()},
                        dataFilter: riskEvidence.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
                    },
                    view: {
                        dblClickExpand: false,
                        nameIsHTML: true,
                        countClass: "group-number-statistics"
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: {
                        beforeClick: riskEvidence.beforeClickVehicle,
                        onCheck: riskEvidence.onCheckVehicle,
                        onExpand: riskEvidence.zTreeOnExpand,
                        onNodeCreated: riskEvidence.zTreeOnNodeCreated,
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if ($('#queryType').val() == "vehicle") {
                nodesArr = filterQueryResult(responseData, crrentSubV);
            } else {
                nodesArr = responseData;
            }
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getRiskDisposeTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));

                var data;
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                } else {
                    data = obj
                }
                for (var i = 0; i < data.length; i++) {
                    data[i].open = true;
                }
            }
            return data;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if(chkFlag){
                if(size <= TREE_MAX_CHILDREN_LENGTH){
                    treeObj.checkAllNodes(true)
                }
            }else{
                treeObj.checkAllNodes(false)
            }
            riskEvidence.getCharSelect(treeObj);
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id, "type": treeNode.type}, function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle") { //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type == "people" || nodes[i].type == "vehicle") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > TREE_MAX_CHILDREN_LENGTH) {
                    // layer.msg(maxSelectItem);
                    layer.msg('最多勾选'+TREE_MAX_CHILDREN_LENGTH+'个监控对象');
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type == "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    riskEvidence.validates();
                }, 600);
            }
            if(treeNode.type!='group'||treeNode.type!='assigment'){
                riskEvidence.getCharSelect(zTree);
            }
            riskEvidence.getCheckedNodes();
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString()
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group") {
                var url = "/clbs/m/functionconfig/fence/bindfence/getOrgAssignmentVehicle";
                json_ajax("post", url, "json", true, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "0"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    parentNode.zAsync = true;
                                    treeObj.addNodes(parentNode, 0, chNodes);
                                    riskEvidence.getCharSelect(treeObj);
                                }
                            }
                        });
                    }
                })
            }
        },
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true)
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            $("#charSelect").val("").attr("data-id","").bsSuggest("destroy");
            var veh=[];
            var vid=[];
            for(var i=0;i<nodes.length;i++){
                if(nodes[i].type=="vehicle"){
                    veh.push(nodes[i].name)
                    vid.push(nodes[i].id)
                }
            }
            var vehName = riskEvidence.unique(veh);
            var vehId = riskEvidence.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++){
                deviceDataList.value.push({
                    name: vehName[j],
                    id:vehId[j]
                });
            };

            $("#charSelect").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click",function(){
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
//            if(deviceDataList.value.length > 0){
//				$("#charSelect").val(deviceDataList.value[0].name).attr("data-id",deviceDataList.value[0].id);
//			}
            $("#groupSelect,#groupSelectSpan").bind("click",riskEvidence.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        getGroupChild: function (node, assign) { // 递归获取组织及下级组织的分组节点
            var nodes = node.children;
            if (nodes != null && nodes != undefined && nodes.length > 0) {
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.type == "assignment") {
                        assign.push(node);
                    } else if (node.type == "group" && node.children != undefined) {
                        riskEvidence.getGroupChild(node.children, assign);
                    }
                }
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true), vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    vid += nodes[i].id + ",";
                }
            }
            vehicleIds = vid;
        },
        //开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = riskEvidence.doHandleMonth(tMonth + 1);
                tDate = riskEvidence.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = riskEvidence.doHandleMonth(endMonth + 1);
                endDate = riskEvidence.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = riskEvidence.doHandleMonth(vMonth + 1);
                vDate = riskEvidence.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if (day == 1) {
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                } else {
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = riskEvidence.doHandleMonth(vendMonth + 1);
                    vendDate = riskEvidence.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
        },
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        //当前时间
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate()) + " " + "00:00:00";
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate())
                + " "
                + ("23")
                + ":"
                + ("59")
                + ":"
                + ("59");
            var atime = $("#atime").val();
            if (atime != undefined && atime != "") {
                startTime = atime;
            }
        },
        //查询
        inquireClick: function (number, isFirst) {
            if (number == 0) {
                riskEvidence.getsTheCurrentTime();
            } else if (number == -1) {
                riskEvidence.startDay(-1)
            } else if (number == -3) {
                riskEvidence.startDay(-3)
            } else if (number == -7) {
                riskEvidence.startDay(-7)
            }
            if (number != 1) {
                $('#timeInterval').val(startTime + '--' + endTime);
                startTime = startTime;
                endTime = endTime;
            } else {
                var timeInterval = $('#timeInterval').val().split('--');
                startTime = timeInterval[0];
                endTime = timeInterval[1];
            }
            if (!isFirst) {
                riskEvidence.getCheckedNodes();
            }
            deleteIds = []; // 将删除字段置空
            currentPage = 1;
            currentBrand = null;
            if (!riskEvidence.validates()) {
                return;
            }
            //查询执行代码
            riskEvidence.queryEvidence('inquireClick');
            //查询车辆信息
            riskEvidence.queryVehicle();
        },
        queryVehicle: function(){
            var ajaxDataParamFun2 = function (d) {
                d.vehicleIds = vehicleIds;
                d.startTime = startTime;
                d.endTime = endTime;
                d.evidenceType = $('#evidenceType').val();
                if ($("#highsearch").css('display') == "block") {
                    d.riskNumber = $("#riskNumber").val(); //模糊查询
                    d.riskType = riskTypeValue;
                    d.riskLevel = comLevelTreeCheckedVal;
                    d.brand = $("#charSelect").val();
                    d.driver = $("#driver").val();
                    d.status = $('#status option:selected').text();
                    d.dealUser = $("#dealUser").val();
                    d.visitTime = $("#visitTime option:selected").text();
                    d.riskResult = $("#riskResult").val();

                    var __event = $('#riskEvent').val();
                    if(__event == "null"){
                        __event = null;
                    }
                    d.riskEvent = __event&&__event.length>0 ? __event : '';
                }
                if(currentBrand != null){
                    d.brand = currentBrand;
                }
                return d;
            };
            json_ajax("POST", "/clbs/adas/r/reportManagement/adasRiskEvidence/queryBrands", 'json', true, ajaxDataParamFun2({}), riskEvidence.queryVehicleCallBack);
        },
        queryVehicleCallBack:function(data){
            console.log('车辆结果集：',data);
            if(!data.success){
                layer.msg(data.msg);
                return;
            }
            if(!data.obj){
                return;
            }
            var deviceDataList=[
                {"name":"所    有","value":null}
            ].concat(data.obj.map(function(ele){
                return {name:ele,value:ele};
            }));
            riskEvidence.initShow({
                data:deviceDataList,
                onSetValue:function(data){
                    currentBrand = data.value;
                    currentPage = 1;
                    riskEvidence.queryEvidence('inquireClick');
                }
            });
            currentBrand = null;
            brands = deviceDataList;
        },
        queryEvidence:function(ifInquireClick){

            json_ajax("POST", "/clbs/adas/r/reportManagement/adasRiskEvidence/list", 'json', true, ajaxDataParamFun({},ifInquireClick), riskEvidence.queryCallBack);
        },
        getAlarmType:function (str) {
            var riskTyepArr = riskTypeNodes[0].children;
            var strArr = str.split(',');
            var result=[];

            for(var i=0;i<riskTyepArr.length;i++){
                var item = riskTyepArr[i];
                if(str == ''){
                    result.push(item.name);
                }else if(strArr.indexOf(item.value) != -1){
                    result.push(item.name);
                }
            }

            return result.join(",");
        },
        getAlarmResult:function (result){
            if(result == "0"){
                return "事故未发生"
            }else if(result == "1"){
                return "事故已发生"
            }else if(result == ""){
                return ""
            }

        },
        getAlarmLevel:function (str) {
            var result="";
            switch (str){
                case '1':
                    result="一般(低)";
                    break;
                case '2':
                    result="一般(中)";
                    break;
                case '3':
                    result="一般(高)";
                    break;
                case '4':
                    result="较重(低)";
                    break;
                case '5':
                    result="较重(中)";
                    break;
                case '6':
                    result="较重(高)";
                    break;
                case '7':
                    result="严重(低)";
                    break;
                case '8':
                    result="严重(中)";
                    break;
                case '9':
                    result="严重(高)";
                    break;
                case '10':
                    result="特重(低)";
                    break;
                case '11':
                    result="特重(中)";
                    break;
                case '12':
                    result="特重(高)";
                    break;
            }
            return result;
        },
        getAlarmEvent:function (str) {
            var result="";
            for(var key in riskEventData){
                if(key !== '所有'){
                    var item = riskEventData[key].split(',');
                    if(item.indexOf(str) != -1){
                        result = key;
                    }
                }
            }
            return result;
        },
        queryCallBack:function(data){
            currentPage = data.page;
            totalPage = data.totalPages;

            // wjk
            if (riskEvidence.pageSearchAfterArr.indexOf(data.searchAfter.join(',')) >= 0) {
                var n = riskEvidence.pageSearchAfterArr.indexOf(data.searchAfter.join(','));
                var arr = riskEvidence.pageSearchAfterArr
                arr.splice(n + 1, arr.length)
                riskEvidence.pageSearchAfterArr = arr

                riskEvidence.nextSearchAfter = riskEvidence.pageSearchAfterArr[riskEvidence.pageSearchAfterArr.length - 1]
            } else {
                riskEvidence.nextSearchAfter = data.searchAfter.join(',');
                riskEvidence.pageSearchAfterArr.push(riskEvidence.nextSearchAfter)
            }
            // wjk end
            // console.log(riskEvidence.pageSearchAfterArr)
            // console.log(riskEvidence.nextSearchAfter)

            $('.M-box').pagination({
                coping: true,
                homePage: '首页',
                endPage: '末页',
                prevContent: '上一页',
                nextContent: '下一页',
                current:currentPage,
                pageCount:totalPage,
                count:totalPage,
                prevCls:'previous',
                nextCls:'next',
                mode:'justPrevAndNext',
                count:2,
                keepShowPN:true,
                callback:function(index){
                    currentPage = index.getCurrent();
                    riskEvidence.queryEvidence();
                }
            });
            var templateZD='<div class="own-col"><div>' +
                '	<div class="bottom-item">$media	' +
                '	</div>							' +
                '	<table class="bottom-table">	' +
                '		<tr>						' +
                '			<td>监控对象：</td>			' +
                '			<td>$brand</td>			' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>风险事件：</td>			' +
                '			<td>$event</td>			' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>预警时间：</td>			' +
                '			<td>$time</td>			' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>预警位置：</td>			' +
                '			<td>$addr</td>			' +
                '		</tr>						' +
                '	</table>						' +
                '</div></div>						';
            var templateFK='<div class="own-col"><div>' +
                '	<div class="bottom-item">$media	' +
                '	</div>							' +
                '	<table class="bottom-table">	' +
                '		<tr>						' +
                '			<td>监控对象：</td>			' +
                '			<td>$brand</td>			' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>风险类型：</td>			' +
                '			<td>$riskType</td>		' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>预警时间：</td>			' +
                '			<td>$time</td>			' +
                '		</tr>						' +
                '		<tr>						' +
                '			<td>预警位置：</td>			' +
                '			<td>$addr</td>			' +
                '		</tr>						' +
                '	</table>						' +
                '</div></div>						';
            var html=null;
            var clickCB = function(){
                var original;
                if(this.empty){
                    $('.detail-content').empty();
                    $('#preview').data('original',null);
                    $('#downloadImg').removeAttr('href');
                    return;
                }
                    var $this = $(this);
                    $this.siblings().removeClass('active');
                    $this.addClass('active');
                    original = $this.data('original');

                $('#preview').data('original',original);
                $('#downloadImg').attr('href',original.mediaName);
                $('#detailBrand').html(original.brand);
                $('#detailRiskType').html(riskEvidence.getAlarmType(original.riskType));
                $('#detailRiskLevel').html(riskEvidence.getAlarmLevel(original.riskLevel));
                $('#detailTime').html(original.warTime);
                $('#detailAddr').html(original.address);
                $('#detailCompany').html(original.groupName);
                $('#detailDriver').html(original.driver);
                $('#detailDealUser').html(original.dealUser);
                $('#detailResult').html(riskEvidence.getAlarmResult(original.riskResult));
                $('#detailweather').html(original.weather);
                $('#detailspeed').html(original.speed+'km/h');
                if(_evidenceType == '终端视频' || _evidenceType == '终端图片'){
                    $('#detailRiskEventTr').show();
                    $('#detailRiskEvent').html(riskEvidence.getAlarmEvent(original.riskEvent));
                    $('#detailNumberLabel').html('事件编号');
                    $('#deailNumber').html(original.eventNumber);
                    if(_evidenceType == '终端图片'){
                        $('#previewContent').empty().append($('<img src="$src" />'.replace('$src',original.mediaName)));
                    }else{
                        $('#previewContent').empty().append($('<video controls="controls" src="$src"></video>'.replace('$src',original.mediaName)));
                    }
                }else{
                    $('#detailRiskEventTr').hide();
                    $('#detailNumberLabel').html('风险编号')
                    $('#deailNumber').html(original.riskNumber);
                    if(_evidenceType == '风控音频'){

                        if(window.navigator.userAgent.toLowerCase().indexOf('ie') >=0 ){
                            $('#previewContent').empty().append($('<embed  src="$src" autostart=false></embed>'.replace('$src',original.mediaName)));
                        }else{
                            $('#previewContent').empty().append($('<audio controls="controls" src="$src"></audio>'.replace('$src',original.mediaName)));
                        }
                    }else{
                        $('#previewContent').empty().append($('<video controls="controls" src="$src"></video>'.replace('$src',original.mediaName)));
                    }
                }
            };
            $('.own-row').empty();
            if(_evidenceType == '终端视频' || _evidenceType == '终端图片'){
                for(var i=0; i<data.records.length;i++){
                    var item = data.records[i];
                    item.mediaName = item.mediaName ? item.mediaName.replace(/\\/g,'/') : item.mediaName;
                    html = templateZD.replace('$brand', item.brand)
                        .replace('$event',item.riskEvent?riskEvidence.getAlarmEvent(item.riskEvent):'')
                        .replace('$time',item.warTime)
                        .replace('$addr',item.address == null? "":item.address);

                    if(_evidenceType == '终端视频'){
                        html = html.replace('$media','<img src="/clbs/resources/img/fkzjk_video.svg" class="audio"/>');
                    }else{
                        html = html.replace('$media','<img src="' + item.mediaName + '" class=""/>');
                    }
                    var ele = $(html).data('original',item).click(clickCB);
                    $('.own-row').append(ele);
                }
            }else{
                for(var i=0; i<data.records.length;i++){
                    var item = data.records[i];
                    item.mediaName = item.mediaName ? item.mediaName.replace(/\\/g,'/') : item.mediaName;
                    html = templateFK.replace('$brand', item.brand)
                        .replace('$riskType',riskEvidence.getAlarmEvent(item.riskType))
                        .replace('$time',item.warTime)
                        .replace('$addr',item.address);

                    if(_evidenceType == '风控音频'){
                        html = html.replace('$media','<img src="/clbs/resources/img/fkzjk_audio.svg" class="audio"/>');
                    }else{
                        html = html.replace('$media','<img src="/clbs/resources/img/fkzjk_video.svg" class="audio"/>');
                    }
                    var ele = $(html).data('original',item).click(clickCB);
                    $('.own-row').append(ele);
                }
            }
            var firstCol = $('.own-col');
            if(firstCol.length>0){
                clickCB.apply(firstCol.get(0));
                if(currentPage==1){
                    $('#yesterdayChange').hide();
                }else{
                    $('#yesterdayChange').show();
                }
                if(currentPage==totalPage){
                    $('#tomorrowChange').hide();
                }else{
                    $('#tomorrowChange').show();
                }
            }else{
                $('#yesterdayChange').hide();
                $('#tomorrowChange').hide();
                clickCB.apply({empty:true})
            }
        },
        unique: function (arr) {
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        showMenu: function (e) {
            if ($("#menuContent").is(":hidden")) {
                var inpwidth = $("#groupSelect").outerWidth();
                $("#menuContent").css("width", inpwidth + "px");
                $(window).resize(function () {
                    var inpwidth = $("#groupSelect").outerWidth();
                    $("#menuContent").css("width", inpwidth + "px");
                })
                $("#menuContent").slideDown("fast");
            } else {
                $("#menuContent").is(":hidden");
            }
            $("body").bind("mousedown", riskEvidence.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", riskEvidence.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(
                    event.target).parents("#menuContent").length > 0)) {
                riskEvidence.hideMenu();
            }
        },
        exportAlarm: function () {
            riskEvidence.getCheckedNodes();
            if (!riskEvidence.validates()) {
                return;
            }
            var url = '/clbs/adas/r/reportManagement/adasRiskEvidence/downloadBatch';
            var parameter = ajaxDataParamFun({});
            exportExcelUseForm(url, parameter);
        },
        validates: function () {
            return $("#riskDislist").validate({
                rules: {
                    startTime: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#startTime",
                    },
                    groupSelect: {
                        regularChar: true,
                        zTreeChecked: "treeDemo"
                    }
                },
                messages: {
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    endTime: {
                        required: "请选择结束时间！",
                        compareDate: "结束日期必须大于开始日期！",
                    },
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand,
                    }
                }
            }).form();
        },
        exportCallback: function (date) {
            if (date == true) {
                var url = "/clbs/r/riskManagement/disposeReport/export";
                window.location.href = url;
            } else {
                alert("导出失败！");
            }
        },
        hightgrade: function () {
            $(".highsearch").slideToggle("slow");
            if ($(this).find("span").hasClass("fa-caret-down")) {
                $(this).find("span").addClass("fa-caret-up").removeClass("fa-caret-down")
            } else {
                $(this).find("span").removeClass("fa-caret-up").addClass("fa-caret-down");
            }
            riskEvidence.hideEventInfoWindow();
        },
        riskLevelInit: function () {
            json_ajax("POST", "/clbs/r/riskManagement/TypeLevel/riskLevelName", "json", false, null, function (data) {
                $("#riskLevel option").remove();
                $("#riskLevel").prepend("<option value=''>所有</option>")
                if (data.success) {
                    var datas = data.obj;
                    if (!!datas) {
                        for (var i = 0; i < datas.length; i++) {
                            $("#riskLevel").append("<option value='"+(i+1)+"'>" + datas[i] + "</option>")
                        }
                    }
                }
            })
        },
        riskPopoverSHFn: function (e) {
            if (!(e.target.className.indexOf('popover') != -1 || $(e.target).parents('.popover').length == 0 || $(e.target).parents('#dataTableRiskType').length > 0 || e.target.className === ' text-center')) {
                $("#riskTypePop").removeClass("in show");
            }
        },
        hideEventInfoWindow: function () {
            if ($("#dataTable tbody tr").hasClass("active-tablebg")) {
                $("#riskTypePop").removeClass("in show");
                $("#dataTable tbody").find("tr").removeClass("active-tablebg");
            }
        },
        showAlarmType : function () {
            if ($("#alarmTypeContent").is(":hidden")) {
                var inpwidth = $("#alarmTypeSelect").width();
                var spwidth = $("#alarmTypeSelectSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if(navigator.appName=="Microsoft Internet Explorer") {
                    $("#alarmTypeContent").css("width",(inpwidth+7) + "px");
                }else{
                    $("#alarmTypeContent").css("width",allWidth + "px");
                }
                $("#alarmTypeContent").slideDown("fast");
            } else {
                $("#alarmTypeContent").attr("hidden","true");
            }
            $("body").bind("mousedown", riskEvidence.onBodyDownAlarmType);
        },
        onBodyDownAlarmType : function(event){
            if (!(event.target.id == "menuBtn" || event.target.id == "alarmTypeContent" || $(event.target).parents("#alarmTypeContent").length > 0)) {
                riskEvidence.hideMenuAlarmType();
            }
        },
        onCheckChangeValue :function () {
            var zTree = $.fn.zTree.getZTreeObj("alarmTypeTree"),
                nodes = zTree.getCheckedNodes(true), alarmTypeNames = "",valueList=[];
            var $riskEvent = $('#riskEvent');
            var _items = null;

            if(nodes.length == 0 && currentDisabledType.length == 0){
                _items = [];
                for(var _item in riskEventData){
                    _items.push({
                        "name": _item,
                        "value": riskEventData[_item]
                    });
                }
            }else{
                _items = [{"name":"所有","value":null}];
                for (var i = 0, l = nodes.length; i < l; i++) {
                    var value = nodes[i].value;
                    if(nodes[i].name != "所有"){
                        if(alarmTypeNames !=""){
                            alarmTypeNames = alarmTypeNames +"、"+ nodes[i].name;
                        }else {
                            alarmTypeNames += nodes[i].name;
                        }
                        valueList.push(value);
                        if(valueList.length==5){
                            valueList=""
                        }
                        _items = _items.concat(typeEvent[nodes[i].name].map(function(ele){
                            return {
                                "name": ele,
                                "value": riskEventData[ele]
                            }
                        }));
                    }
                }
                if(currentDisabledType.length>0){
                    _items = _items.concat(typeEvent[currentDisabledType].map(function(ele){
                        return {
                            "name": ele,
                            "value": riskEventData[ele]
                        }
                    }));
                }
            }
            var options = $('#riskEvent option');
            // 删除没用的
            for(var i=0; i < options.length; i++){
                var $option = $(options[i]);
                var isFound = false;
                for(var j=0; j < _items.length; j++){
                    var _item = _items[j];
                    if($option.text() == _item.name){
                        isFound = true;
                        break;
                    }
                }
                if(!isFound){
                    $option.remove();
                }

            }
            options = $('#riskEvent option');
            // 添加没有的
            for(var i=0; i < _items.length; i++){
                var _item = _items[i];
                var isFound = false;
                for(var j=0; j < options.length; j++){
                    var $option = $(options[j]);
                    if($option.text() == _item.name){
                        isFound = true;
                        break;
                    }
                }
                if(!isFound){
                    $riskEvent.append($('<option value="$value">$name</option>'.replace('$value',riskEventData[_item.name]).replace('$name',_item.name)))
                }
            }
            var a = [currentDisabledType,alarmTypeNames].filter(function(item){
                return item.length > 0;
            }).join('、');

            $("#alarmTypeSelect").val(a);
            riskTypeValue = riskEvidence.getGroup(valueList, 0, []);//风险类型排列组合
        },
        getGroup: function (data, index, group) {
            var need_apply = new Array();
            need_apply.push(data[index]);
            for (var i = 0; i < group.length; i++) {
                need_apply.push(group[i] + ',' + data[index]);
            }
            group.push.apply(group, need_apply);

            if (index + 1 >= data.length) {
                return group.join('+');
            }
            return riskEvidence.getGroup(data, index + 1, group);

        },
        //风险等级复选下拉框
        comLevelTree:function () {
            var comChildren =[
                {name:"一般(低)",value:"1"},
                {name:"一般(中)",value:"2"},
                {name:"一般(高)",value:"3"},
                {name:"较重(低)",value:"4"},
                {name:"较重(中)",value:"5"},
                {name:"较重(高)",value:"6"},
                {name:"严重(低)",value:"7"},
                {name:"严重(中)",value:"8"},
                {name:"严重(高)",value:"9"},
                {name:"特重(低)",value:"10"},
                {name:"特重(中)",value:"11"},
                {name:"特重(高)",value:"12"}
            ];
            var zNodes = [
                {
                    name: "所有",
                    open: true,
                    children: comChildren
                }
            ];
            var alamTypeSetting = {
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics",
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onCheck: riskEvidence.comLevelValue
                }
            };
            $.fn.zTree.init($('#alarmLevelTree'),alamTypeSetting,zNodes);
        },
        comLevelValue:function () {
            var comTree = $.fn.zTree.getZTreeObj('alarmLevelTree'),
                nodes = comTree.getCheckedNodes(true),
                value = "",
                names1 = '';
            comLevelTreeCheckedVal = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                var value1 = nodes[i].value;
                var name = nodes[i].name;
                if (name != "所有") {
                    if (value != "") {
                        value = value + "," + value1;
                        names1 = names1 + "," + name;
                    } else {
                        value += value1;
                        names1 += name;
                    }
                }
            }
            if(value.length==26){
                value="";
            }
            comLevelTreeCheckedVal = value;
            $('#riskLevel').val(names1);

        },
        hideMenuAlarmType : function(){
            $("#alarmTypeContent").fadeOut("fast");
            $("body").unbind("mousedown", riskEvidence.onBodyDownAlarmType);
        },
        initShow : function(options){
            var data = options.data;
            if(!data){
                data=[
                    {name:'所    有',value:null},
                    {name:'测试0001',value:1},
                    {name:'测试0002',value:2},
                    {name:'测试0003',value:3},
                    {name:'京ZW0004',value:4},
                    {name:'京ZW0005',value:5},
                    {name:'京ZW0006',value:6},
                    {name:'京ZW0007',value:7},
                    {name:'京ZW0008',value:8}
                ];
            }

            var isIn=false;
            var close=function(){
                setTimeout(function(){
                    if(!isIn){
                        $('#showBox').addClass('hidden');
                    }
                },50);
            };
            var show=function(){
                var $show=$('#showBox'),
                    $this=$(this);
                $show.removeClass('hidden');
            };
            var clickItem = function(){
                close();
                isIn = false;
                $this = $(this);
                $this.siblings().removeClass('active');
                $this.addClass('active');
                var obj= {
                    name:$this.html(),
                    value:$this.data('value')
                };
                updateTitle(obj);
                if(options.onSetValue ){
                    options.onSetValue.call($this,obj);
                }

            };
            var updateTitle=function(data){
                $('#carName').html(data.name).data('value',data.value);
            }
            var init = function(){
                var $content = $('#showBox .show-content');
                var template = '<div class="show-item" data-value="$value">$name</div>';
                $content.empty();
                for(var i = 0; i < data.length; i++){
                    var item = $(template.replace('$value',data[i].value).replace('$name',data[i].name));
                    item.click(clickItem);
                    $content.append(item);
                }
                if(data.length>0){
                    updateTitle({
                        name:data[0].name,
                        value:data[0].value
                    });
                    $('.show-item:eq(0)').addClass('active');
                }
            }
            $('#carName').mouseover(function(){
                isIn=true;
                show.apply(this);
            }).mouseout(function(){
                isIn=false;
                close();
            });
            $('#showBox').mouseover(function(){
                isIn=true;
            }).mouseout(function(){
                isIn=false;
                close();
            });
            init();
        },
        initSelect:function(){
            _evidenceType = $('#evidenceType').find("option:selected").text();
            $('#evidenceType').change(function(){
                _evidenceType = $(this).find("option:selected").text();
                if(_evidenceType == '终端视频' || _evidenceType == '终端图片'){
                    $('#riskEvent').removeAttr('disabled');
                }else{

                    $('#riskEvent').attr('disabled','disabled');
                }
            });
            $('#riskEvent').change(function(){
                _riskEvent = $(this).find("option:selected").text();
                _riskEventValue = $(this).val();
                var riskTypeNode = riskEvidence.eventChangeFun(_riskEvent);

                var currentTypeValue = $("#alarmTypeSelect").val();
                if(_riskEvent == '所有'){
                    $("#alarmTypeSelect").val(_riskEvent);
                    var $riskEvent = $('#riskEvent');
                    $riskEvent.empty();
                    for(var n in riskEventData){
                        $riskEvent.append($('<option value="$value">$name</option>'.replace('$value',riskEventData[n]).replace('$name',n)))
                    }
                }else{
                    if(currentTypeValue.indexOf(currentDisabledType) > -1){
                        for(var k in riskTypeNode[0].children){
                            var _item = riskTypeNode[0].children[k];
                            if(currentTypeValue.indexOf(_item.name) > -1){
                                _item.checked=true;
                            }
                        }
                    }else{

                        for(var k in typeEvent){
                            for(var i=0; i< typeEvent[k].length; i++){
                                if(typeEvent[k][i] == _riskEvent){
                                    $("#alarmTypeSelect").val(k);
                                    break;
                                }
                            }
                        }
                    }
                }
                $.fn.zTree.init($("#alarmTypeTree"), alarmTypeSetting, riskTypeNode);
            });
        },
        eventChangeFun: function(_riskEvent){
            var typeNode = riskTypeNodes;
            var typeArr = [];
            currentDisabledType = '';

            for(var i=0;i<typeNode[0].children.length;i++){
                typeNode[0].children[i].checked=false;
                typeNode[0].children[i].chkDisabled=false;
            }

            for(var key in typeEvent){
                var values = typeEvent[key];
                if(values.indexOf(_riskEvent) != -1){//事件对应的报警类型
                    typeArr = typeNode[0].children;

                    for(var i=0;i<typeArr.length;i++){
                        var item = typeArr[i];

                        if(item.name == key){//勾选组织树对应的报警类型
                            typeNode[0].children[i].checked=true;
                            typeNode[0].children[i].chkDisabled=true;
                        }
                    }

                    currentDisabledType = key;
                }
            }

            return typeNode;
        },
        isJpeg:function(mediaName){
            if(mediaName){
                var arr = mediaName.split('.');
                var suffix= arr[arr.length-1];
                if(suffix=="jpeg"){
                    return true;
                }
                    return false;
                

            }
        }
    }

    $(function () {
        //初始化页面
        riskEvidence.init();
        $('#downloadImg').click(function(){
            var href = $(this).attr('href');
            var original = $('#preview').data('original');
            var isJpeg = riskEvidence.isJpeg(original.mediaName);
            if(original && original.id){
                $.post('/clbs/adas/r/reportManagement/adasRiskEvidence/canDownload',{
                    id:original.id,isJpeg:isJpeg
                },function(r){
                    if(r.success){
                        if(href && href.length>0){
                            window.location.href = href;
                        }
                    }else{
                        layer.msg('资源不存在，可能已被删除');
                    }
                },'json')
            }

            return false;
        })
        $('#preview').mouseover(function(){
            $('#previewMask').css('visibility','visible');
        }).mouseout(function(){
            $('#previewMask').css('visibility','hidden');
        });
        $('.mask-img').mouseover(function(){
            $(this).find('hr').css('transform','scaleX(1)');
        }).mouseout(function(){
            $(this).find('hr').css('transform','scaleX(0)');
        });
        $('#deleteSvg').click(function(){
            var original = $('#preview').data('original');
            if(!original || !original.id){
                return;
            }
            deleteIds.push(original.id);
            riskEvidence.queryEvidence('inquireClick');
        });
        $('#rightClickVehicle').click(function(){
            if(brands == null){
                return;
            }
            currentBrand = $('#carName').html();
            // 查找当前车辆在候选项中的位置
            for(var i = 0; i < brands.length; i++){
                if(brands[i].name == currentBrand){
                    break;
                }
            }
            if(i < brands.length - 1){
                currentBrand = brands[i+1].name;
                var currentActive = $('.show-item.active');
                var nextActive = currentActive.next();
                currentActive.removeClass('active');
                nextActive.addClass('active');
                $('#carName').html(currentBrand);
                currentPage = 1;
                riskEvidence.queryEvidence('inquireClick');
            }else{
                layer.msg('已经是最后一辆了');
            }

        });
        $('#leftClickVehicle').click(function(){
            if(brands == null){
                return;
            }
            currentBrand = $('#carName').html();
            // 查找当前车辆在候选项中的位置
            for(var i = 0; i < brands.length; i++){
                if(brands[i].name == currentBrand){
                    break;
                }
            }
            if(i > 0){
                currentBrand = brands[i-1].name;
                var currentActive = $('.show-item.active');
                var prevActive = currentActive.prev();
                currentActive.removeClass('active');
                prevActive.addClass('active');
                $('#carName').html(currentBrand);
                currentPage = 1;
                riskEvidence.queryEvidence('inquireClick');
            }else{
                layer.msg('已经是第一辆了');
            }
        });
        $('#tomorrowChange').click(function(){
            if(totalPage == null || currentPage == totalPage){
                return;
            }

            riskEvidence.prevOrnext = 'next';

            currentPage++;
            riskEvidence.queryEvidence();
        });
        $('#yesterdayChange').click(function(){
            if(currentPage > 1){

                riskEvidence.prevOrnext = 'prev'
                var arr = riskEvidence.pageSearchAfterArr;
                arr.pop();
                riskEvidence.pageSearchAfterArr = arr
                if (riskEvidence.pageSearchAfterArr.length) {
                    riskEvidence.prevSearchAfter = riskEvidence.pageSearchAfterArr[riskEvidence.pageSearchAfterArr.length - 2]
                    riskEvidence.nextSearchAfter = riskEvidence.pageSearchAfterArr[riskEvidence.pageSearchAfterArr.length - 1]
                } else {
                    riskEvidence.prevSearchAfter = ''
                }

                currentPage--;
                riskEvidence.queryEvidence();
            }
        });
        $("[data-toggle='tooltip']").tooltip();
        //添加快速删除
        $('input').inputClear().on('onClearEvent',function(e,data){
            var id=data.id;
            if(id == 'groupSelect'){
                var param=$("#groupSelect").val();
                riskEvidence.searchVehicleTree(param);
                chkFlag = false;
            }
        });
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            };
            if (!(window.ActiveXObject || "ActiveXObject" in window)){
                searchFlag = true;
            };
            inputChange = setTimeout(function () {
                if(searchFlag) {
                    var param = $("#groupSelect").val();
                    riskEvidence.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
            chkFlag = false;
        });
        //风险事件下拉
        riskEvidence.comLevelTree();
        //高级搜索风险等级初始化
        riskEvidence.riskLevelInit();
        //当前时间
        riskEvidence.getsTheCurrentTime();
        $("#timeInterval").dateRangePicker({
            dateLimit:31
        });
        $("#groupSelectSpan,#groupSelect").bind("click", riskEvidence.showMenu);
        $("#refreshTable").bind("click", riskEvidence.refreshTableClick);
        $("#highlever").on("click", riskEvidence.hightgrade);
        // //导出
        $("#exportAlarm").bind("click", riskEvidence.exportAlarm);
        $("body").on("click", riskEvidence.riskPopoverSHFn);
        $("#alarmTypeSelectSpan,#alarmTypeSelect").bind("click",riskEvidence.showAlarmType); //车辆树下拉显示
        $("#riskLevel,#alarmTypeSelectSpan1").bind("click",showMenuContent); //风险等级下拉显示
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            riskEvidence.searchVehicleTree(param);
        });
    })
}(window, $))