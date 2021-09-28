// # sourceURL = channelSettingLoad.js
/**
 * 右键菜单  设置通道号
 */
/*(function(window,$){*/

var channelArray;
var _thisVehicleId;
var addIndex; //新增下标值，避免重复
var physicalChannelArr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 27, 28, 29, 30, 31, 32, 33, 34, 35];//物理通道号
var logicalChannelArr = [
    "1(驾驶员)", "2(车辆正前方)", "3(车前门)", "4(车厢前部)",
    "5(车厢后部)", "6(车后门)", "7(行李舱)", "8(车辆左侧)",
    "9(车辆右侧)", "10(车辆正后方)", "11(车厢中部)", "12(车中门)",
    "13(驾驶席车门)", "14(预留)", "15(预留)", "16(预留)", "33(驾驶员)", "36(车厢前部)", "37(车厢后部)", "64（ADAS）", "65（DMS）"];//逻辑通道号
var addLogicalChannelArr = [];//逻辑通道号 用于添加时
var _defAddHtml = '';
var currentProtocol = '';// 当前监控对象协议类型
var connectYuntai = '是否连接云台';
var isConnection = '是';
var noConnection = '否';
var qj360form = '是否360全景';
var isqj360form = '是';
var noqj360form = '否';
var optionHtml = '<option value="0">音视频</option>' +
    '<option value="1">音频</option>' +
    '<option value="2">视频</option>';
var streamTypeHtml = '<label class="col-md-2 control-label">码流类型：</label>' +
    '<div class="col-md-3">' +
    '<select class="form-control" id="streamType_#">' +
    '<option value="1" selected="selected">子码流</option>' +
    '<option value="0">主码流</option>' +
    '</select>' +
    '</div>';

channelSettingLoad = {

    init: function () {
        $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});

        //菜单隐藏
        $('#rMenu').css({"visibility": "hidden"});

        currentProtocol = $('#settingChannelLink').attr('data-type');
        if (currentProtocol === '23') {// 报批稿协议特殊处理
            connectYuntai = '是否连接云台或启用音频输出';
            isConnection = '连接或启用';
            noConnection = '未连接或未启用';
            optionHtml = '<option value="1">音频</option>' +
                '<option value="0" selected>视频</option>';
            streamTypeHtml = '';
        }

        //模态框添加类样式
        $(".modal-body").addClass("modal-body-overflow");
        // $(".modal-dialog").css("width", "70%");

        //创建删除通道组键值对集合
        channelArray = new channelSettingLoad.mapList();

        //获取车辆ID信息
        _thisVehicleId = $("#thisVehicleId").val();
        //获取协议定义的所有通道号及名称
        addLogicalChannelArr = eval($("#allChannels").val());

        //通道类型下拉选择改变函数
        channelSettingLoad.channelTypeChangeFn();

        //初始化请求视频通道数据
        json_ajax("GET", "/clbs/realTimeVideo/videoSetting/view/" + _thisVehicleId, "json", true, null, channelSettingLoad.getInitChannelDataFn);

    },

    //封装map集合
    mapList: function () {
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
    },

    //添加初始化物理及逻辑通道号到默认节点
    addInitPhysicalAndLogicalFn: function (_index, addFlag) {
        if (_index != undefined) {
            //添加物理通道号
            var _physical = '';
            for (var i = 0; i < physicalChannelArr.length; i++) {
                _physical += '<option value="' + physicalChannelArr[i] + '">' + physicalChannelArr[i] + '</option>';
                $("#physicalChannelNumber_" + _index).html(_physical);
            }
            if (addFlag) {
                //添加逻辑通道号
                var _logical = '';
                for (var i = 0; i < addLogicalChannelArr.length; i++) {
                    /*var opt;
                  /*if(i < 1){
                        opt = 0;
                    }else{
                        var optVal = addLogicalChannelArr[i];
                        if(i < 10){
                            opt = optVal.substr(0,1);
                        }else{
                            opt = optVal.substr(0,2);
                        }
                    }*/
                    _logical += '<option value="' + addLogicalChannelArr[i] + '">' + addLogicalChannelArr[i] + '</option>';
                    $("#logicalChannelNumber_" + _index).html(_logical);
                }
            } else {
                //添加逻辑通道号
                var _logical = '';
                for (var i = 0; i < addLogicalChannelArr.length; i++) {
                    var optVal = addLogicalChannelArr[i];
                    /*var opt;
                    if (i < 1) {
                        opt = 0;
                    } else {
                        if (i < 10){
                            opt = optVal.substr(0,1);
                        } else {
                            opt = optVal.substr(0,2);
                        }
                    }*/
                    _logical += '<option value="' + optVal + '">' + optVal + '</option>';
                    $("#logicalChannelNumber_" + _index).html(_logical);
                }
            }
        } else {
            //添加物理通道号
            var _physical = '';
            for (var i = 0; i < physicalChannelArr.length; i++) {
                _physical += '<option value="' + physicalChannelArr[i] + '">' + physicalChannelArr[i] + '</option>';
                $("#physicalChannelNumber").html(_physical);
            }
            //添加逻辑通道号
            var _logical = '';
            for (var i = 0; i < addLogicalChannelArr.length; i++) {
                _logical += '<option value="' + i + '">' + addLogicalChannelArr[i] + '</option>';
                $("#logicalChannelNumber").html(_logical);
            }
        }
    },

    //通道设置组默认节点
    channelSettingGroup: function () {
        var newStreamTypeHtml = streamTypeHtml;
        _defAddHtml = '<div id="hannelSettingNode_1">' +
            '<div class="form-group">' +
            '<label class="col-md-2 control-label">物理通道号：</label>' +
            '<div class="col-md-3 physical-channel">' +
            '<select class="form-control physicalChannelNumbers" id="physicalChannelNumber_1"></select>' +
            '</div>' +
            '<label class="col-md-2 control-label">逻辑通道号：</label>' +
            '<div class="col-md-3">' +
            '<select class="form-control logicalChannelNumbers" id="logicalChannelNumber_1"></select>' +
            '</div>' +
            '<div class="col-md-1" id="channelAddBtn_1">' +
            '<button id="channel-add-btn" type="button" class="btn btn-primary addIcon"><span class="glyphicon glyphiconPlus" aria-hidden="true"></span></button>' +
            '</div>' +
            '</div>' +
            '<div class="form-group">' +
            '<label class="col-md-2 control-label">通道类型：</label>' +
            '<div class="col-md-3">' +
            '<select class="form-control" id="channelType_1" onchange="channelSettingLoad.channelTypeChangeFn(1)">' +
            '' + optionHtml + '' +
            '</select>' +
            '</div>' +
            '' + newStreamTypeHtml.replace('#', 1) + '' +
            '</div>' +
            '<div class="form-group hidden" id="connectionHaeundae_1">' +
            '<label class="col-md-2 control-label">' + connectYuntai + '</label>' +
            '<div class="col-md-3">' +
            '<label class="radio-inline radioLabel">' +
            '<input type="radio" name="haeundaeTrue_1" id="isHaeundae_1" checked="checked"> ' + isConnection + '' +
            '</label>' +
            '<label class="radio-inline radioLabel">' +
            '<input type="radio" name="haeundaeTrue_1" id="notHaeundae_1"> ' + noConnection + '' +
            '</label>' +
            '</div>' +
            '<label class="col-md-2 control-label">' + qj360form + '</label>' +
            '<div class="col-md-3">' +
            '<label class="radio-inline radioLabel">' +
            '<input type="radio" name="qj360form_1" id="isqj360form_1" checked="checked"> ' + isqj360form + '' +
            '</label>' +
            '<label class="radio-inline radioLabel">' +
            '<input type="radio" name="qj360form_1" id="notqj360form_1"> ' + noqj360form + '' +
            '</label>' +
            '</div>' +
            '</div>' +
            '</div>';
        //添加到页面
        $("#channelSettingContent").append(_defAddHtml);
    },

    //获取设置通道号基本参数信息
    getInitChannelDataFn: function (data) {
        var newStreamTypeHtml = streamTypeHtml;
        //获取通道号相关数据
        var videoChannelData = data.obj.videoChannels;
        //根据数据长度判断当前监控对象是否存在视频通道号(>0 存在)
        if (videoChannelData.length > 0) {
            var _addHtml = '';
            //遍历已设置通道号的数据
            for (var i = 0; i < videoChannelData.length; i++) {
                //第一条数据添加到默认节点
                if (i < 1) {
                    //通道设置组默认节点
                    channelSettingLoad.channelSettingGroup();
                    //添加初始化物理及逻辑通道号到默认节点
                    channelSettingLoad.addInitPhysicalAndLogicalFn(1, false);
                    //设置物理通道号选中值
                    $("#physicalChannelNumber_1>option[value=" + videoChannelData[i].physicsChannel + "]").attr("selected", "selected");
                    //设置逻辑通道号选中值
                    $("#logicalChannelNumber_1>option[value=" + videoChannelData[i].logicChannel + "]").attr("selected", "selected");
                    //设置通道类型选中值
                    $("#channelType_1>option[value=" + videoChannelData[i].channelType + "]").attr("selected", "selected");
                    //设置码流类型选中值
                    $("#streamType_1>option[value=" + videoChannelData[i].streamType + "]").attr("selected", "selected");
                    //设置云台连接是否显示
                    ($("#channelType_1").val() == 0 || $("#channelType_1").val() == 2) ? $("#connectionHaeundae_1").removeClass("hidden") : $("#connectionHaeundae_1").addClass("hidden");
                    //设置云台连接是否选中
                    if (videoChannelData[i].connectionFlag == 1) {
                        $("#notHaeundae_1").removeAttr("checked");
                        $("#isHaeundae_1").prop("checked", true);
                    } else {
                        $("#isHaeundae_1").removeAttr("checked");
                        $("#notHaeundae_1").prop("checked", true);
                    }
                    // 360全景是否选中
                    if (videoChannelData[i].panoramic === true) {
                        $("#notqj360form_1").removeAttr("checked");
                        $("#isqj360form_1").prop("checked", true);
                    } else {
                        $("#isqj360form_1").removeAttr("checked");
                        $("#notqj360form_1").prop("checked", true);
                    }
                    //判断通道类型是否可操作
                    var logicalChannel = $("#logicalChannelNumber_1").val();
                    if (logicalChannel == 33 || logicalChannel == 36 || logicalChannel == 37) {
                        $("#channelType_1").attr("disabled", "disabled");
                    }
                } else {
                    var _addIndex = i + 1;
                    //定义添加DOM节点
                    _addHtml = '<div id="hannelSettingNode_' + _addIndex + '">' +
                        '<div class="form-group">' +
                        '<label class="col-md-2 control-label">物理通道号：</label>' +
                        '<div class="col-md-3 physical-channel">' +
                        '<select class="form-control physicalChannelNumbers" id="physicalChannelNumber_' + _addIndex + '"></select>' +
                        '</div>' +
                        '<label class="col-md-2 control-label">逻辑通道号：</label>' +
                        '<div class="col-md-3">' +
                        '<select class="form-control logicalChannelNumbers" id="logicalChannelNumber_' + _addIndex + '"></select>' +
                        '</div>' +
                        '<div class="col-md-1" id="channelModule_' + _addIndex + '">' +
                        '<button type="button" class="btn btn-danger channelContentDelete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>' +
                        '</div>' +
                        '</div>' +
                        '<div class="form-group">' +
                        '<label class="col-md-2 control-label">通道类型：</label>' +
                        '<div class="col-md-3">' +
                        '<select class="form-control" id="channelType_' + _addIndex + '" onchange="channelSettingLoad.channelTypeChangeFn(' + _addIndex + ')">' +
                        '' + optionHtml + '' +
                        '</select>' +
                        '</div>' +
                        '' + newStreamTypeHtml.replace('#', _addIndex) + '' +
                        '</div>' +
                        '<div class="form-group hidden" id="connectionHaeundae_' + _addIndex + '">' +
                        '<label class="col-md-2 control-label">' + connectYuntai + '</label>' +
                        '<div class="col-md-3">' +
                        '<label class="radio-inline radioLabel">' +
                        '<input type="radio" name="haeundaeTrue_' + _addIndex + '" id="isHaeundae_' + _addIndex + '"> ' + isConnection + '' +
                        '</label>' +
                        '<label class="radio-inline radioLabel">' +
                        '<input type="radio" name="haeundaeTrue_' + _addIndex + '" id="notHaeundae_' + _addIndex + '"> ' + noConnection + '' +
                        '</label>' +
                        '</div>' +
                        '<label class="col-md-2 control-label">' + qj360form + '</label>' +
                        '<div class="col-md-3">' +
                        '<label class="radio-inline radioLabel">' +
                        '<input type="radio" name="qj360form_' + _addIndex + '" id="isqj360form_' + _addIndex + '"> ' + isqj360form + '' +
                        '</label>' +
                        '<label class="radio-inline radioLabel">' +
                        '<input type="radio" name="qj360form_' + _addIndex + '" id="notqj360form_' + _addIndex + '"> ' + noqj360form + '' +
                        '</label>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>';
                    //添加到页面
                    $("#channelSettingContent").append(_addHtml);
                    //添加物理及逻辑通道号数据
                    channelSettingLoad.addInitPhysicalAndLogicalFn(_addIndex, false);
                    //设置物理通道号选中值
                    $("#physicalChannelNumber_" + _addIndex + ">option[value=" + videoChannelData[i].physicsChannel + "]").attr("selected", "selected");
                    //设置逻辑通道号选中值
                    $("#logicalChannelNumber_" + _addIndex + ">option[value=" + videoChannelData[i].logicChannel + "]").attr("selected", "selected");
                    //设置通道类型选中值
                    $("#channelType_" + _addIndex + ">option[value=" + videoChannelData[i].channelType + "]").attr("selected", "selected");
                    //设置码流类型选中值
                    $("#streamType_" + _addIndex + ">option[value=" + videoChannelData[i].streamType + "]").attr("selected", "selected");
                    //设置云台连接是否显示
                    ($("#channelType_" + _addIndex).val() == 0 || $("#channelType_" + _addIndex).val() == 2) ? $("#connectionHaeundae_" + _addIndex).removeClass("hidden") : $("#connectionHaeundae_" + _addIndex).addClass("hidden");
                    //设置云台连接是否选中
                    if (videoChannelData[i].connectionFlag == 1) {
                        $("#notHaeundae_" + _addIndex).removeAttr("checked");
                        $("#isHaeundae_" + _addIndex).prop("checked", true);
                    } else {
                        $("#isHaeundae_" + _addIndex).removeAttr("checked");
                        $("#notHaeundae_" + _addIndex).prop("checked", true);
                    }
                    //设置360全景是否选中
                    if (videoChannelData[i].panoramic === true) {
                        $("#notqj360form_" + _addIndex).removeAttr("checked");
                        $("#isqj360form_" + _addIndex).prop("checked", true);
                    } else {
                        $("#isqj360form_" + _addIndex).removeAttr("checked");
                        $("#notqj360form_" + _addIndex).prop("checked", true);
                    }
                    //设置码流类型选中值
                    if (currentProtocol !== '23') {// 协议类型不是报批稿
                        $("#streamType_" + _addIndex + ">option[value=" + videoChannelData[i].streamType + "]").attr("selected", "selected");
                    }
                    //判断通道类型是否可操作
                    var logicalChannel = $("#logicalChannelNumber_" + _addIndex).val();
                    if (logicalChannel == 33 || logicalChannel == 36 || logicalChannel == 37) {
                        $("#channelType_" + _addIndex).attr("disabled", "disabled");
                    }
                }
            }
        }
        //数据接口中不存在通道时  添加默认DOM节点
        else {
            //通道设置组默认节点
            channelSettingLoad.channelSettingGroup();
            //添加初始化物理及逻辑通道号到默认节点
            channelSettingLoad.addInitPhysicalAndLogicalFn(1, false);
            //设置云台连接是否显示
            ($("#channelType_1").val() == 0 || $("#channelType_1").val() == 2) ? $("#connectionHaeundae_1").removeClass("hidden") : $("#connectionHaeundae_1").addClass("hidden");
            //设置云台连接选中
            $("#isHaeundae_1").attr("checked", true);
            $("#notqj360form_1").attr("checked", true);
        }

        //通道组相关添加函数
        $("#channel-add-btn").bind("click", channelSettingLoad.channelContentAddFn);

        //删除添加的通道组DOM结构
        $(".channelContentDelete").bind("click", function () {
            //获取删除时id信息
            var _btnId = $(this).parent().attr("id");
            //截取id后缀编号
            var _btnStr = _btnId.substr(_btnId.length - 1, _btnId.length);
            //将删除的通道组ID信息保存到map集合
            if (channelArray.isEmpty()) {
                channelArray.put("_this" + _btnStr, _btnStr);
            } else {
                //不为空 查找当前key 如存在  则删除
                if (channelArray.containsKey("_this" + _btnStr)) {
                    channelArray.remove("_this" + _btnStr);
                }
                channelArray.put("_this" + _btnStr, _btnStr);
            }
            //删除当前DOM节点
            $(this).parent().parent().parent().remove();
            $("#channelAddBtn").show();
        });

        $(".logicalChannelNumbers").bind("change", channelSettingLoad.checkProtocoLogicalChannel);
        //通道组相关添加初始值
        addIndex = $("#channelSettingContent>div").length;
    },

    /**
     * 检查协议逻辑通道号33,36,37
     */
    checkProtocoLogicalChannel: function () {
        var logicalChannelNumber = $(this).val();
        var _thisIdNumber = $(this).attr("id");
        _thisIdNumber = _thisIdNumber.substring(_thisIdNumber.indexOf("_") + 1);
        if (logicalChannelNumber == 33 || logicalChannelNumber == 36 || logicalChannelNumber == 37) {
            $("#channelType_" + _thisIdNumber).val(1).attr("disabled", "disabled");
            $("#connectionHaeundae_" + _thisIdNumber).attr("hidden", "hidden");
        } else {
            $("#channelType_" + _thisIdNumber).removeAttr("disabled");
            $("#connectionHaeundae_" + _thisIdNumber).removeAttr("hidden");
        }
    },

    //通道类型下拉选择改变函数
    channelTypeChangeFn: function (_thisIndex) {
        //_thisIndex 区分是默认DOM还是JS添加DOM
        if (_thisIndex != undefined) {
            if ($('#channelType_' + _thisIndex).val() == 0 || $('#channelType_' + _thisIndex).val() == 2) {
                $('#connectionHaeundae_' + _thisIndex).removeClass("hidden");
            } else {
                $('#connectionHaeundae_' + _thisIndex).addClass("hidden");
            }
        } else {
            if ($("#channelType").val() == 0 || $("#channelType").val() == 2) {
                $("#connectionHaeundae").removeClass("hidden");
            } else {
                $("#connectionHaeundae").addClass("hidden");
            }
        }
    },

    //通道组相关添加函数
    channelContentAddFn: function () {
        var newStreamTypeHtml = streamTypeHtml;
        //判断只增加19组
        if ($("#channelSettingContent>div").length < 35) {
            addIndex++;
            //定义添加通道组HTML代码
            var _thisHtml = '<div id="hannelSettingNode_' + addIndex + '">' +
                '<div class="form-group">' +
                '<label class="col-md-2 control-label">物理通道号：</label>' +
                '<div class="col-md-3 physical-channel">' +
                '<select class="form-control physicalChannelNumbers" id="physicalChannelNumber_' + addIndex + '">' +
                '</select>' +
                '</div>' +
                '<label class="col-md-2 control-label">逻辑通道号：</label>' +
                '<div class="col-md-3">' +
                '<select class="form-control logicalChannelNumbers" id="logicalChannelNumber_' + addIndex + '">' +
                '<option value="" selected="selected"> ~_~ </option>' +
                '</select>' +
                '</div>' +
                '<div class="col-md-1" id="channelModule_' + addIndex + '">' +
                '<button type="button" class="btn btn-danger channelContentDelete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>' +
                '</div>' +
                '</div>' +
                '<div class="form-group">' +
                '<label class="col-md-2 control-label">通道类型：</label>' +
                '<div class="col-md-3">' +
                '<select class="form-control" id="channelType_' + addIndex + '" onchange="channelSettingLoad.channelTypeChangeFn(\'' + addIndex + '\')">' +
                '' + optionHtml + '' +
                '</select>' +
                '</div>' +
                '' + newStreamTypeHtml.replace('#', addIndex) + '' +
                '</div>' +
                '<div class="form-group hidden" id="connectionHaeundae_' + addIndex + '">' +
                '<label class="col-md-2 control-label">' + connectYuntai + '</label>' +
                '<div class="col-md-3">' +
                '<label class="radio-inline radioLabel">' +
                '<input type="radio" name="haeundaeTrue_' + addIndex + '" id="isHaeundae_' + addIndex + '" checked="checked"> ' + isConnection + '' +
                '</label>' +
                '<label class="radio-inline radioLabel">' +
                '<input type="radio" name="haeundaeTrue_' + addIndex + '" id="notHaeundae_' + addIndex + '"> ' + noConnection + '' +
                '</label>' +
                '</div>' +
                '<label class="col-md-2 control-label">' + qj360form + '</label>' +
                '<div class="col-md-3">' +
                '<label class="radio-inline radioLabel">' +
                '<input type="radio" name="qj360form_' + addIndex + '" id="isqj360form_' + addIndex + '" checked="checked"> ' + isqj360form + '' +
                '</label>' +
                '<label class="radio-inline radioLabel">' +
                '<input type="radio" name="qj360form_' + addIndex + '" id="notqj360form_' + addIndex + '"> ' + noqj360form + '' +
                '</label>' +
                '</div>' +
                '</div>' +
                '</div>';
            //添加DOM
            $("#channelSettingContent").append(_thisHtml);
            //获取当前通道组长度
            var _groupLenth = $("#channelSettingContent>div").length;
            //新增通道号时判断物理通道号是否重复，重复则做处理，保证加号出来的一定不重复
            var existPhysicalChannelNumbers = $(".physicalChannelNumbers>option:selected");
            var physicalChannelNumber = [];
            for (var i = 0; i < existPhysicalChannelNumbers.length; i++) {
                physicalChannelNumber.push(parseInt(existPhysicalChannelNumbers[i].value));
            }
            var notExist = 1;
            while ($.inArray(notExist, physicalChannelNumber) != -1) {
                notExist++;
            }
            //添加初始化物理及逻辑通道号到默认节点
            channelSettingLoad.addInitPhysicalAndLogicalFn(addIndex, true);
            //物理通道号递增
            $("#physicalChannelNumber_" + addIndex).find("option").removeAttr("selected");
            $("#physicalChannelNumber_" + addIndex).val(notExist).attr("selected", "selected");
            $("#logicalChannelNumber_" + addIndex).find("option").removeAttr("selected");
            $("#logicalChannelNumber_" + addIndex).val(notExist).attr("selected", "selected");
            //判断当前新增的通道组是否显示连接云台
            ($('#channelType_' + addIndex).val() == 0 || $('#channelType_' + addIndex).val() == 2) ? $('#connectionHaeundae_' + addIndex).removeClass("hidden") : $('#connectionHaeundae_' + addIndex).addClass("hidden");
            //设置云台连接是否选中
            if ($("#channelType_" + addIndex + ">option:selected").val() == 0) {
                $("#notHaeundae_" + addIndex).removeAttr("checked");
                $("#isHaeundae_" + addIndex).attr("checked", true);
            } else {
                $("#isHaeundae_" + addIndex).removeAttr("checked");
                $("#notHaeundae_" + addIndex).attr("checked", true);
            }
            //设置360全景是否选中
            if ($("#channelType_" + addIndex + ">option:selected").val() == 0) {
                $("#isqj360form_" + addIndex).removeAttr("checked");
                $("#notqj360form_" + addIndex).attr("checked", true);
            } else {
                $("#notqj360form_" + addIndex).removeAttr("checked");
                $("#isqj360form_" + addIndex).attr("checked", true);
            }
            //删除添加的通道组DOM结构
            $(".channelContentDelete").bind("click", function () {
                //获取删除时id信息
                var _btnId = $(this).parent().attr("id");
                //截取id后缀编号
                var _btnStr = _btnId.substr(_btnId.length - 1, _btnId.length);
                //将删除的通道组ID信息保存到map集合
                if (channelArray.isEmpty()) {
                    channelArray.put("_this" + _btnStr, _btnStr);
                } else {
                    //不为空 查找当前key 如存在  则删除
                    if (channelArray.containsKey("_this" + _btnStr)) {
                        channelArray.remove("_this" + _btnStr);
                    }
                    channelArray.put("_this" + _btnStr, _btnStr);
                }
                //删除当前DOM节点
                $(this).parent().parent().parent().remove();
                $("#channelAddBtn").show();
            });

        } else {
            $("#channelAddBtn").hide();
            return false;
        }

        //每次新增节点重新绑定改变事件
        $(".logicalChannelNumbers").unbind("change").bind("change", channelSettingLoad.checkProtocoLogicalChannel);
    },

    //显示错误信息
    showErrorMsg: function (msg, inputId) {
        var error = $("#error_channel_setting").length;
        if (error == 0) {
            $("#channelSettingFooter").append("<label id='error_channel_setting' class='error'></label>");
        }
        $("#error_channel_setting").insertAfter($("#" + inputId));
        $("#error_channel_setting").text(msg);
        $("#error_channel_setting").removeClass("hidden");
        // if ($("#error_channel_setting").hasClass("hidden")) {
        //     $("#error_channel_setting").text(msg);
        //     $("#error_channel_setting").insertAfter($("#" + inputId));
        //     // $("#error_channel_setting").removeClass("hidden");
        // } else {
        //     $("#error_channel_setting").addClass("hidden");
        // }
    },

    //隐藏错误提示信息
    hideErrorMsg: function () {
        $("#error_channel_setting").addClass("hidden");
    },

    //通道号设置下发函数
    sendChannelSettingFn: function () {
        //获取当前通道组长度
        var _groupLenth = $("#channelSettingContent>div").length;
        //组装JSON对象
        var _jsonArr = [];
        //云台连接是否选择
        var _ifChecked;
        //360全景是否选择
        var _ifCheckedqj360form;
        //校验字符串
        var _checkPhysics = "";
        var _checkLogic = "";
        for (var i = 1; i <= _groupLenth; i++) {
            //物理及逻辑通道号取值
            var physicsChannel = $("#physicalChannelNumber_" + i + ">option:selected").val();
            if (physicsChannel == undefined) { //已被删除，重新循环取值
                _groupLenth = _groupLenth + 1;
                continue;
            }
            var logicChannel = $("#logicalChannelNumber_" + i + ">option:selected").val();
            //校验数据
            if (_checkPhysics.toString().indexOf("#" + physicsChannel + "#") > -1) {
                channelSettingLoad.showErrorMsg("物理通道号不能重复", "physicalChannelNumber_" + i);
                return false;
            } else if (logicChannel == "0") {
                channelSettingLoad.showErrorMsg("请选择逻辑通道号", "logicalChannelNumber_" + i);
                return false;
            } else if (_checkLogic.toString().indexOf("#" + logicChannel + "#") > -1) {
                channelSettingLoad.showErrorMsg("逻辑通道号不能重复", "logicalChannelNumber_" + i);
                return false;
            } 
                //隐藏错误信息
                channelSettingLoad.hideErrorMsg();
                //组装下发数据对象
                var _obj = {};
                _obj.sort = i; //排序
                _obj.physicsChannel = physicsChannel; //物理通道号
                _obj.logicChannel = logicChannel; //逻辑通道号
                _obj.streamType = $("#streamType_" + i + ">option:selected").val() || 0; //码流类型
                _obj.channelType = $("#channelType_" + i + ">option:selected").val(); //通道类型
                //验证字符串追加
                _checkPhysics += "#" + physicsChannel + "#";
                _checkLogic += "#" + logicChannel + "#";
                //云台连接
                if ($("#isHaeundae_" + i).is(":checked")) {
                    _ifChecked = 1;
                } else {
                    _ifChecked = 0;
                }
                //360全景
                if ($("#isqj360form_" + i).is(":checked")) {
                    _ifCheckedqj360form = 1;
                } else {
                    _ifCheckedqj360form = 0;
                }
                _obj.connectionFlag = _ifChecked;
                _obj.panoramic = _ifCheckedqj360form == 1 ? true : false;
                //数组添加
                _jsonArr.push(_obj);
            
        }
        //转换为json字符串
        var newJsonArr = JSON.stringify(_jsonArr);
        //定义请求接口
        var url = "/clbs/realTimeVideo/video/sendParamCommand";
        //定义下发参数
        var parameter = {
            vehicleId: _thisVehicleId,//车ID
            contrasts: newJsonArr,//参数
            orderType: 7 //下发类型
        };
        //ajax异步下发
        json_ajax("POST", url, "json", true, parameter, function (data) {
            if (data.success) {
                $("#commonWin").modal("hide");
                layer.msg("提交成功");
                //日志记录
                if (window.logFindCilck) {
                    window.logFindCilck();
                } else {
                    realTimeVideLoad.logFindCilck();
                }

                var url = '/clbs/realTimeVideo/video/getChannels';
                var data = {'vehicleId': _thisVehicleId, 'isChecked': false};
                json_ajax("POST", url, "json", true, data, function (info) {
                    var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
                    var tid = $("#tid").val();
                    var nodes = treeObj.getNodesByParam("id", _thisVehicleId);
                    for (var i = 0; i < nodes.length; i++) {
                        //清除子节点
                        treeObj.removeChildNodes(nodes[i]);
                        if (window.getGroupList) {
                            window.getGroupList(info, nodes[i]);
                        } else {
                            realTimeVideLoad.getGroupList(info, nodes[i], false);
                        }
                    }
                });
            } else {
                layer.msg(data.msg);
            }
        });
    },
}

$(function () {

    channelSettingLoad.init();
    $("#sendChannelSetting").on("click", channelSettingLoad.sendChannelSettingFn);//通道号设置下发
})

/*})(window,$)*/