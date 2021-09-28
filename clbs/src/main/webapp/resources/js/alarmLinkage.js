/**
 * author: wushengsong
 **/
/*********** 全局变量 **************/
var alarmLinkageVehicleId = null;				// 当前车辆ID
var alarmTypeList = [];				// 包含需展示的报警类型，参考报警查询的报警类型列表
var alarmTypeUl = null; 			// 页面的报警类型列表
var refersData = [];				// 参考对象数据
var referSettingList = null;				// 某个参考对象的设置
var settingList = [];				// 当前车之前的设置
var currentPos = null; 			// 当前报警类型pos
var defaultSetting = null; 			// 表单默认值
var tempSettingList = [];				// 当前用户编辑时临时保存的设置，比方说她选中了一个报警类型，编辑了一阵，又切换到另一个报警类型，那么之前编辑的报警类型设置就要保存在这里
var onlyNeedFields = null;				// 我们需要的字段。从服务器返回的数据中包含需要我们不需要的字段，留着这些不需要的字段会带来祸端，去除掉
var protocolType = $('input[name="deviceCheck"]:checked').val();
var alarmLinkage = {
    init: function () {
        console.log('协议类型', protocolType);
        if (protocolType === '-1' || protocolType === '11') {
            $('#ioBox').show();
        }
        // 初始化车辆ID
        alarmLinkageVehicleId = $('#alarmLinkageVehicleId').val();
        // 初始化我们需要的字段
        onlyNeedFields = ['wayId', 'command', 'time', 'saveSign',
            'resolution', 'quality', 'luminance', 'contrast',
            'saturability', 'chroma', 'msgContent', 'marks', 'deviceType',
            'textType', 'messageTypeOne', 'messageTypeTwo',
            'peripheralId', 'controlTime', 'controlSubtype',
            'outletSet', 'analogOutputRatio',
        ];
        // 初始化报警类型
        alarmLinkage.initAlarmType();
        // 初始化这个车之前的设置
        alarmLinkage.initSettingList();
        // 初始化左右侧
        alarmLinkage.renderLeftAndRight();
        // 初始化参考对象
        alarmLinkage.initRefer();
        // 初始化显示更多功能
        alarmLinkage.initShowMore();
        // 初始化tooltip
        $('[data-toggle="tooltip"]').tooltip();
        // 初始化保存按钮点击事件
        $('#saveAlarmLinkage').click(alarmLinkage.saveCB);
        $(".alternativeTts").click(alarmLinkage.alternativeTts);
        $(".alternative").click(alarmLinkage.alternative);
        alarmLinkage.loadManagerTree()
    },
    // 初始化这个车之前的设置
    initSettingList: function () {
        var _settingList = JSON.parse($('#settingList').val());
        // 删除不用的字段
        settingList = alarmLinkage.deleteUnusefulField(_settingList);
    },
    // 初始化表单默认值
    initDefaultSetting: function () {
        var photoDefault = {
            wayId: '请选择通道号',			//通道ID
            time: 0,			//拍照间隔/录像时间; 单位：秒，0 表示按最小间隔拍照或一直录像
            command: 1,		//拍摄命令，0-停止拍摄；0xFFFF-录像；其它表示拍照张数
            saveSign: 0,		//保存标志: 1：保存；0：实时上传
            resolution: 1,	//分辨率
            quality: 5,		//图像/视频质量；范围1-10，1代表质量损失最小，10表示压缩比最大
            luminance: 125,	//亮度 0-255
            contrast: 60,		//对比度 0-127
            saturability: 60,	//饱和度0-127
            chroma: 125		//色度 0-255

        };
        var recordingDefault = {
            wayId: 1,			//通道ID
            time: 3,			//拍照间隔/录像时间; 单位：秒，0 表示按最小间隔拍照或一直录像
            saveSign: 0,		//保存标志: 1：保存；0：实时上传
            resolution: 1,	//分辨率
            quality: 5,		//图像/视频质量；范围1-10，1代表质量损失最小，10表示压缩比最大
            luminance: 125,	//亮度 0-255
            contrast: 60,		//对比度 0-127
            saturability: 60,	//饱和度0-127
            chroma: 125		//色度 0-255

        };
        // I/O输出控制
        var outputControlDefault = {
            peripheralId: '144',		//外设ID
            controlTime: '',		//控制时长
            controlSubtype: 1,		//控制子类型
            outletSet: 0,		// 输出口设置
            analogOutputRatio: ''		//模拟量输出比例
        };
        var msgDefault = {
            msgContent: "",				//短信内容
            marks: "4", //默认勾选框
        }
        defaultSetting = {
            alarmHandleLinkageCheck:0,
            handleUsername: $("#userName").html(),
            photoFlag: 0,
            photo: photoDefault,
            recordingFlag: 0,
            recording: recordingDefault,
            videoFlag: 0,
            uploadAudioResourcesFlag: 0,
            msg: msgDefault,
            outputControl: outputControlDefault,
            msgFlag: 0,
            outputControlFlag: 0,
        };
        alarmLinkage.renderSetting(defaultSetting);
    },
    // 初始化显示更多
    initShowMore: function () {
        $(".show-more-title").on("click", function () {
            var $this = $(this);
            var target = $(this).data('target');
            if (!($(target).is(":hidden"))) {
                $(target).slideUp();
                $this.children("font").text("显示更多");
                $this.children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $(target).slideDown();
                $this.children("font").text("隐藏参数");
                $this.children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        });
        $(".lower-checkbox").on("click", function () {
            var $this = $(this);
            var $font = $this.closest('.form-group').find('font');
            var $span = $font.next();
            var target = $(this).data('target');
            if ($this.is(':checked')) {
                $(target).slideDown();
                $font.text("隐藏参数");
                $span.removeAttr("class").addClass("fa fa-chevron-up");
            }
        });
    },
    // 初始化参考对象
    initRefer: function () {
        refersData = JSON.parse($('#referVehicleList').val());
        $("#refers").bsSuggest({
            idField: "id",			// data-id 对应的值
            keyField: "brand",		// input框中的内容
            effectiveFields: ["brand"],
            searchFields: ["id"],
            data: {value: refersData}
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
            $('#refersUl').hide();
            alarmLinkage.selectReferCB(keyword);

        }).on('onClearSelectValue', function () {
            alarmLinkage.clearReferCB();
        })
    },
    // 初始化左侧报警类型
    initAlarmType: function () {
        alarmTypeUl = $('#alarmTypeUl');
        var rawAlarmTypeList = JSON.parse($('#alarmTypeList').val());
        var alarmTypeChinese = {
            alert: '预警',
            driverAlarm: '驾驶员引起报警',
            vehicleAlarm: '车辆报警',
            faultAlarm: '故障报警',
            sensorAlarm: 'F3传感器报警',
            platAlarm: '平台报警',
            videoAlarm: '视频报警',
            ioAlarm: 'I/O报警'
        };
        if (!(rawAlarmTypeList instanceof Array)) {
            console.error('后台返回报警类型格式错误，应为数组');
            return;
        }
        // 将报警类型组装到对应的标题中
        alarmTypeList = [];
        for (var k in alarmTypeChinese) {
            var _type = {
                typeValue: k,
                typeName: alarmTypeChinese[k],
                alarmTypes: rawAlarmTypeList.filter(function (ele) {
                    return ele.type === k;
                })
            };
            alarmTypeList.push(_type);
        }
        // 将数据渲染到 html 中
        for (var i = 0; i < alarmTypeList.length; i++) {
            alarmTypeUl.append($('<li>').addClass('title').html(alarmTypeList[i].typeName).data('value', alarmTypeList[i].typeValue));
            for (var k = 0; k < alarmTypeList[i].alarmTypes.length; k++) {
                var _item = alarmTypeList[i].alarmTypes[k];
                alarmTypeUl.append($('<li><input type="checkbox" class="lower-checkbox"/><span>' + _item.name + '</span></li>')
                    .data('id', _item.id).data('pos', _item.pos));
            }
        }
        // 绑定报警类型点击事件
        alarmTypeUl.find('span').click(alarmLinkage.clickAlarmTypeCB)
        alarmTypeUl.find('input').on('change',function(){
            if($(this).prop("checked")){
                $(this).next().click()
            }
            if(!$(this).next().hasClass('chosen')){
                $(this).prop("checked", false)
            }
        })
    },
    // 点击报警类型回调
    // 这里主要做两件事情
    // 第一保存上一个报警类型对应的参数，保存到临时数组里：tempSettingList
    // 第二显示当前报警类型对应的参数
    clickAlarmTypeCB: function () {
        var $preChosen = alarmTypeUl.find('span.chosen');
        var $this = $(this);
        var $li = $this.parent('li');
        var pos = $li.data('pos');
        // 如果之前有选中另一个，暂存设置

        if ($preChosen.length > 0) {
            var prePos = $preChosen.parent('li').data('pos');
            // 如果当前和之前的是同一个，不做任何操作
            if (pos === prePos) {
                return;
            }
            // 如果返回false，说明保存出错，有可能是用户输入数据格式不对，中断操作
            var saveR = alarmLinkage.tempSaveAlarmTypeSetting(prePos);
            if (!saveR) {
                return;
            }
            $preChosen.removeClass('chosen');
        }

        // 初始化新的报警类型对应的设置
        $this.addClass('chosen');
        $this.prev().prop("checked", true)

        var setting = alarmLinkage.getAlarmTypeSetting(pos);
        alarmLinkage.renderSetting(setting);
        // 重置显示更多
        //alarmLinkage.resetShowMore();
        currentPos = pos;
    },
    // 清除参考对象回调
    // 将referSettingList 设为 null,重新渲染左右侧
    // 如有选中报警类型，获取该报警类型对应的设置渲染
    clearReferCB: function () {
        referSettingList = null;
        alarmLinkage.renderLeftAndRight();
    },
    // 选中参考对象回调
    // 将referSettingList 设为 返回的对象,重新渲染左右侧
    // 如有选中报警类型，获取该报警类型对应的设置渲染
    selectReferCB: function (keyword) {
        var toPost = {
            vehicleId: keyword.id
        }
        var url = '/clbs/a/alarmSetting/referSetting';
        // 上传
        json_ajax("POST", url, "json", true, toPost, alarmLinkage.getReferSettingCB);
    },
    // 远程获取参考对象设置回调
    getReferSettingCB: function (r) {
        if (r.success == false) {
            layer.msg(publicErrorTip);
            return;
        }
        // 清除选中
        alarmLinkage.clearChose();
        // 删除不用的字段
        referSettingList = alarmLinkage.deleteUnusefulField(r.obj);
        alarmLinkage.renderLeftAndRight();
    },
    // 删除不用的字段
    deleteUnusefulField: function (list) {
        return list.map(function (item) {
            var obj = {
                pos: item.pos,
                videoFlag: item.videoFlag,
                uploadAudioResourcesFlag: item.uploadAudioResourcesFlag,
                alarmHandleLinkageCheck: item.alarmHandleLinkageCheck,
                alarmHandleResult: item.alarmHandleResult,
                alarmHandleType: item.alarmHandleType,
                handleUsername: item.handleUsername,
            };
            if (item.photo == null) {
                obj.photo = null;
                obj.photoFlag = 0;
            } else {
                obj.photo = {};
                obj.photoFlag = 1;
                for (var k in item.photo) {
                    if (onlyNeedFields.indexOf(k) > -1) {
                        obj.photo[k] = item.photo[k];
                    }
                }
            }

            if (item.recording == null) {
                obj.recording = null;
                obj.recordingFlag = 0;
            } else {
                obj.recording = {};
                obj.recordingFlag = 1;
                for (var k in item.recording) {
                    if (onlyNeedFields.indexOf(k) > -1) {
                        obj.recording[k] = item.recording[k];
                    }
                }
            }

            if (item.msg == null) {
                obj.msg = null;
                obj.msgFlag = 0;
            } else {
                obj.msg = {};
                obj.msgFlag = 1;
                for (var k in item.msg) {
                    if (onlyNeedFields.indexOf(k) > -1) {
                        obj.msg[k] = item.msg[k];
                    }
                }
            }

            // I/O输出控制
            if (item.outputControl == null) {
                obj.outputControl = null;
                obj.outputControlFlag = 0;
            } else {
                obj.outputControl = {};
                obj.outputControlFlag = 1;
                for (var k in item.outputControl) {
                    if (onlyNeedFields.indexOf(k) > -1) {
                        obj.outputControl[k] = item.outputControl[k];
                    }
                }
            }
            return obj;
        });
    },
    clearChose: function () {
        currentPos = null;
        var $preChosen = alarmTypeUl.find('span.chosen');
        if ($preChosen.length > 0) {
            $preChosen.removeClass('chosen');
        }
    },
    // 重置显示更多，将两个都展开
    resetShowMore: function () {
        var pageIds = ['#photoShowMore-content', '#recordingShowMore-content', '#sendMsgShowMore-content'];		// 页面dom元素的ID
        for (var i = 0; i < pageIds.length; i++) {
            var $target = $(pageIds[i]);
            if ($target.is(":hidden")) {
                $target.slideDown();
                $this = $('span[data-target="' + pageIds[i] + '"]');
                $this.children("font").text("隐藏参数");
                $this.children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        }
    },
    // 暂存当前报警类型设置到临时数组中:tempSettingList
    // 如果数据不符合规范，不保存，返回false
    // 如果保存成功，返回 true
    tempSaveAlarmTypeSetting: function (pos) {
        // 校验当前输入的数据是否符合规范，如果不符合，则给出提示，并终端后续操作
        if (!alarmLinkage.photoValidate() || !alarmLinkage.recordingValidate() || !alarmLinkage.msgValidate() || !alarmLinkage.ioValidate() || !alarmLinkage.alarmLinkageValidate()) {
            return false;
        }
        var pageIds = ['#photoShowMore-content', '#recordingShowMore-content', '#sendMsgShowMore-content', '#outputControl-content'];					// 页面dom元素的ID
        var flagPageIds = ['#photoAreaCheckbox', '#recordingAreaCheckbox', '#videoAreaCheckbox', '#msgAreaCheckbox', '#uploadAudioResourcesFlag', '#outputControl'];	// 每个类型前面的checkbox 的id
        var flagObjectKeys = ['photoFlag', 'recordingFlag', 'videoFlag', 'msgFlag', 'uploadAudioResourcesFlag', 'outputControlFlag'];							// 设置对象的三个flag
        var objectKeys = ['photo', 'recording', 'msg', 'outputControl'];													// 设置对象的key

        // 要保存的设置

        var setting = {
            pos: pos
        }
        for (var i = 0; i < pageIds.length; i++) {
            var $area = $(pageIds[i]);
            if (setting[objectKeys[i]] === undefined) {
                setting[objectKeys[i]] = {};
            }
            for (var k in defaultSetting[objectKeys[i]]) {
                var $input = $area.find('input[name="' + k + '"],select[name="' + k + '"],textarea[name="' + k + '"]');
                // 理论上讲这个判断是不必须的，只要不存在变量名写错了的情况。但是也有可能存在多余的变量名
                if ($input.length > 0) {
                    // 这段比较难理解，以i=0为例，
                    // objectKeys[i]=>photo; setting[objectKeys[i]] => 拍照的设置 ; setting[objectKeys[i]][k]=>拍照某一个表单的值
                    if ($input.attr('type') != 'checkbox') {
                        setting[objectKeys[i]][k] = $input.val();
                    } else {
                        var marks = "";
                        for (var j = 0; j < $input.length; j++) {
                            if ($($input[j]).prop('checked')) {
                                marks += $($input[j]).val();
                                if (j != $input.length - 1) {
                                    marks += ",";
                                }
                            }
                        }
                        setting[objectKeys[i]][k] = marks;
                    }
                }
            }
        }

        // 根据checkbox勾选状态 设置flag
        for (var i = 0; i < flagPageIds.length; i++) {
            if ($(flagPageIds[i]).prop('checked')) {
                setting[flagObjectKeys[i]] = 1;
            } else {
                setting[flagObjectKeys[i]] = 0;
            }
        }
        // 报警参数临时保存
        setting.alarmHandleLinkageCheck = Number($("#alarmLinkage").prop('checked'))
        if(setting.alarmHandleLinkageCheck){
            setting.alarmHandleResult = $("#alarmLinkageMore input[name='handleResult']:checked").val()
            setting.alarmHandleType = $("#alarmLinkageMore input[name='handleMethod']:checked").val()
            var treeObj = $.fn.zTree.getZTreeObj("managerTree");
            var checkedNodes = treeObj.getCheckedNodes()
            if(checkedNodes && checkedNodes.length > 0){
                setting.handleUsername = checkedNodes[0].name
            }
        }
        for (var i = 0; i < tempSettingList.length; i++) {
            if (tempSettingList[i].pos === pos) {
                tempSettingList[i] = setting;
                break;
            }
        }
        // 说明查找完毕 tempSettingList 都没有找到，需要新增
        if (i === tempSettingList.length) {
            tempSettingList.push(setting);
        }
        return true;
    },
    // 获取报警类型所属的分类
    // 找得到就直接返回值，找不到返回false
    getCategory4AlarmType: function (pos) {
        for (var i = 0; i < alarmTypeList.length; i++) {
            var item = alarmTypeList[i];
            for (var j = 0; j < item.alarmTypes.length; j++) {
                if (item.alarmTypes[j].pos === pos) {
                    return item.typeValue;
                }
            }
        }
        return false;
    },
    // 获取一个报警类型对应的设置
    // 获取的顺序为 "默认值" -> "原有设置" -> "当前临时保存的设置"
    getAlarmTypeSetting: function (pos) {
        var _originalSetting = null, _tempSetting = null, _defaultSetting = defaultSetting;
        var referArray = [_originalSetting, _tempSetting];						// 存储上面两个变量的引用，在下面for循环中，方便直接用下标的方式访问
        var objects = [alarmLinkage.getSettingList(), tempSettingList];			// 存储原有设置和当前临时变量的引用
        for (var k = 0; k < objects.length; k++) {
            var _setting = objects[k];
            for (var i = 0; i < _setting.length; i++) {
                if (_setting[i].pos === pos) {
                    referArray[k] = _setting[i];
                    break;
                }
            }
        }
        // 复位对数组的引用
        _originalSetting = referArray[0], _tempSetting = referArray[1];

        // 如果没有那一个类型的参数，对应的flag设置为0
        // _originalSetting 不为空才参与判断
        if (_originalSetting) {
            if (!_originalSetting.photo) {
                _originalSetting.photoFlag = 0
                delete _originalSetting.photo
            } else {
                _originalSetting.photoFlag = 1
            }
            ;
            if (!_originalSetting.recording) {
                _originalSetting.recordingFlag = 0
                delete _originalSetting.recording
            } else {
                _originalSetting.recordingFlag = 1
            }
            ;
            if (!_originalSetting.msg) {
                _originalSetting.msgFlg = 0
                delete _originalSetting.msg
            } else {
                _originalSetting.msgFlag = 1
            }
            if (!_originalSetting.outputControl) {
                _originalSetting.outputControlFlag = 0
                delete _originalSetting.outputControl
            } else {
                _originalSetting.outputControlFlag = 1
            }
        }

        // 音视频下的报警类型，默认勾选实时视频
        /*var alarmTypeCategory = alarmLinkage.getCategory4AlarmType(pos);
        if(alarmTypeCategory && alarmTypeCategory === 'videoAlarm'){
            _defaultSetting = $.extend({}, _defaultSetting, {videoFlag:1});
        }*/

        //特殊报警类型,默认勾选实时视频
        var specialAlarms = ['0', '1', '2', '29', '30']; // 特殊报警类型
        var index = $.inArray(pos, specialAlarms);
        if (index >= 0) {
            _defaultSetting = $.extend({}, _defaultSetting, {videoFlag: 1});
        }

        // 此处需要进行深拷贝，所以要传入第一个true参数
        return $.extend(true, {}, _defaultSetting, _originalSetting, _tempSetting)
    },
    // 获取设置列表，如果参考对象设置有数据，就返回参考对象的，不然就返回自身的
    getSettingList: function () {
        if (referSettingList !== null) {
            return referSettingList;
        }
        return settingList;

    },
    // 渲染左右侧数据
    renderLeftAndRight: function () {
        alarmLinkage.renderSettingList();
        if (currentPos === null) {
            alarmLinkage.initDefaultSetting();
        } else {
            var setting = alarmLinkage.getAlarmTypeSetting(currentPos);
            alarmLinkage.renderSetting(setting);
        }
        // 默认点击被勾选的第一个报警类型
        $("#alarmTypeUl").find(".lower-checkbox:checked:first").each(function () {
            alarmLinkage.clickAlarmTypeCB.call($(this).next().get(0));
        });
    },
    // 渲染左侧报警类型的勾选状态，页面初始化时和选中参考对象时调用
    renderSettingList: function () {
        var _settingList = alarmLinkage.getSettingList();
        // 勾选左侧报警类型
        var checkBox = alarmTypeUl.find('input[type="checkbox"]');
        checkBox.each(function (index, ele) {
            $(ele).prop('checked', false)
        });
        checkBox.each(function (index, ele) {
            for (var i = 0; i < _settingList.length; i++) {
                var item = _settingList[i];
                if ($(ele).parent('li').data('pos') === item.pos) {
                    $(ele).prop('checked', true);
                }
            }
        });
    },
    // 从原有设置集合中读取对应设置，并渲染到右侧表单中
    renderSetting: function (setting) {
        console.log(setting)
        var pageIds = ['#photoShowMore-content', '#recordingShowMore-content', '#sendMsgShowMore-content', '#outputControl-content','#alarmLinkageMore'];					// 页面dom元素的ID
        var flagPageIds = ['#photoAreaCheckbox', '#recordingAreaCheckbox', '#videoAreaCheckbox', '#msgAreaCheckbox', '#uploadAudioResourcesFlag', '#outputControl','#alarmLinkage'];	// 每个类型前面的checkbox 的id
        var flagObjectKeys = ['photoFlag', 'recordingFlag', 'videoFlag', 'msgFlag', 'uploadAudioResourcesFlag', 'outputControlFlag','alarmHandleLinkageCheck'];    // 设置对象的三个flag
        var objectKeys = ['photo', 'recording', 'msg', 'outputControl'];													// 设置对象的ke

        // 清除所有错误信息
        $('label.error').remove();
        $('input.error').removeClass('error');
        for (var i = 0; i < pageIds.length; i++) {
            var $area = $(pageIds[i]);
            for (var k in setting[objectKeys[i]]) {
                if (objectKeys[i] === 'outputControl') {
                    if (setting[objectKeys[i]]['controlSubtype'] == '3') {
                        $('.analogInfo').show();
                    } else {
                        $('.analogInfo').hide();
                    }
                }
                var $input = $area.find('input[name="' + k + '"],select[name="' + k + '"],textarea[name="' + k + '"]');
                // 理论上讲这个判断是不必须的，只要不存在变量名写错了的情况。但是也有可能存在多余的变量名
                if ($input.length > 0) {
                    // 这段比较难理解，以i=0为例，
                    // objectKeys[i]=>photo; setting[objectKeys[i]] => 拍照的设置 ; setting[objectKeys[i]][k]=>拍照某一个表单的值
                    if ($input.attr('type') != 'checkbox') {
                        $input.val(setting[objectKeys[i]][k]);
                    } else if(objectKeys[i] === 'msg'){
                        //下发短信中的复选框
                        var marks = setting.msg.marks;
                        var mark = marks.split(',');
                        for (var j = 0; j < $input.length; j++) {
                            //先全部取消勾选
                            $($input[j]).prop('checked', false);
                        }
                        if (mark.length > 0) {
                            for (var m = 0; m < mark.length; m++) {
                                for (var j = 0; j < $input.length; j++) {
                                    if (mark[m] == $($input[j]).val()) {
                                        $($input[j]).prop('checked', true);
                                    }
                                }
                            }
                        }
                    } else if(objectKeys[i] === 'photo'){
                        //拍照的通道id处理
                        var wayIds = setting.photo.wayId + '';
                        var wayIdArr = wayIds.split(',').filter(function (item) {
                            return item !== ''
                        });
                        //取消全部勾选
                        $input.prop('checked', false);
                        if(wayIdArr.length > 0){
                            $("#multiInput").html(wayIdArr.join('、'))
                        }else {
                            $("#multiInput").html('请选择通道号')
                        }
                        for (var ii = 0; ii < wayIdArr.length; ii++) {
                            for (var jj = 0; jj < $input.length; jj++) {
                                if (wayIdArr[ii] == $input.eq(jj).val()) {
                                    $input.eq(jj).prop('checked', true);
                                }
                            }
                        }
                    }
                }
            }
        }
        //报警联动flag为0时 禁用处理方式和处理结果单选框
        if(setting.alarmHandleLinkageCheck == 0){
            if($("#alarmTypeUl input[type='checkbox']:checked").length >1){
                $area.find("input[type='radio']").each(function () {
                    $(this).prop('disabled',true)
                    $(this).prop('checked',false)
                })
            }
        }
        //报警处理结果
        if(setting.alarmHandleResult && setting.alarmHandleLinkageCheck){
            $area.find("input[name='handleResult']").each(function () {
                $(this).prop('disabled',false)
                if($(this).val() == setting.alarmHandleResult){
                    $(this).prop('checked',true)
                }
            })
        }
        //报警处理方式
        if(setting.alarmHandleType && setting.alarmHandleLinkageCheck){
            $area.find("input[name='handleMethod']").each(function () {
                $(this).prop('disabled',false)
                if($(this).val() == setting.alarmHandleType){
                    $(this).prop('checked',true)
                }
            })
        }
        //报警处理人
        if(setting.handleUsername && referSettingList == null){
            setTimeout(function () {
                $("#managerTreeInput").val(setting.handleUsername)
                var treeObj = $.fn.zTree.getZTreeObj("managerTree");
                var rootNode = treeObj.getNodes();
                var nodes = treeObj.transformToArray(rootNode);
                for (var i = 0, l = nodes.length; i < l; i++) {
                    if(nodes[i].name == setting.handleUsername){
                        treeObj.checkNode(nodes[i], true, false);
                    }else {
                        treeObj.checkNode(nodes[i], false, false);
                    }
                }
            },300)
        }
        // 设置 checkbox 勾选状态
        for(var i=0; i<flagObjectKeys.length; i++){
            var $target = $(flagPageIds[i]).parent().parent().siblings();
            var $this = $(flagPageIds[i]).parent().siblings('.text-right');
            if(setting[flagObjectKeys[i]] === 0) {
                $(flagPageIds[i]).prop('checked', false);
                if(!$target.is(':hidden')){
                    $target.slideDown();
                    $this.find('font').text('显示更多');
                    $this.children().find('span').removeAttr("class").addClass("fa fa-chevron-down");
                }

            }else if(setting[flagObjectKeys[i]] === 1){
                $(flagPageIds[i]).prop('checked', true);
                if($target.is(':hidden')){
                    $target.slideDown();
                    $this.find('font').text('隐藏参数');
                    $this.children().find('span').removeAttr("class").addClass("fa fa-chevron-up");
                }
            }
        }
    },
    // 获取保存时需要的数据
    // 如果数据格式错误，中断操作，返回false
    // 如果一切正确，返回设置数组
    getToSaveSetting: function () {
        // 首先，暂存当前报警类型的设置
        if (currentPos !== null) {
            // 如果数据格式错误，中断操作，返回false
            var saveR = alarmLinkage.tempSaveAlarmTypeSetting(currentPos);
            if (!saveR) {
                return false;
            }
        }
        // 然后，获取左侧报警类型 checkbox 已勾选的对应的 pos
        var posArray = [];
        var checkedBox = alarmTypeUl.find('input[type="checkbox"]:checked');
        checkedBox.each(function (index, ele) {
            posArray.push($(ele).parent('li').data('pos'));
        });
        if (posArray.length == 0) {
            layer.msg("请至少选择一个报警类型");
            return false;
        }
        // 最后，根据pos，获取设置
        var settingArray = [];
        for (var i = 0; i < posArray.length; i++) {
            var _setting = alarmLinkage.getAlarmTypeSetting(posArray[i]);
            // 处理数据，如果没有pos，加上
            // 如果 photoFlag = 0, photo 设为 null, 删除 photoFlag
            // 如果 recordingFlag = 0, recording 设为 null, 删除 recordingFlag
            if (_setting.pos === undefined || _setting.pos === null) {
                _setting.pos = posArray[i];
            }
            if (_setting.photoFlag === 0) {
                _setting.photo = null;
            }
            delete _setting.photoFlag;

            if (_setting.recordingFlag === 0) {
                _setting.recording = null;
            }
            delete _setting.recordingFlag;

            if (_setting.msgFlag === 0) {
                _setting.msg = null;
            }
            delete _setting.msgFlag;

            if (_setting.outputControlFlag === 0) {
                _setting.outputControl = null;
            }
            delete _setting.outputControlFlag;

            //报警联动处理
            if(_setting.alarmHandleLinkageCheck == 0){
                delete _setting.alarmHandleType
                delete _setting.alarmHandleResult
                delete _setting.handleUsername
            }
            _setting.deviceType = $("#Ul-menu-text-v input[type='radio']:checked").val()
            settingArray.push(_setting);
        }
        return settingArray;
    },
    // 保存按钮点击回调
    saveCB: function () {
        if(!alarmLinkage.alarmLinkageValidate()){
            return
        }
        // 先组装需要上传的数据
        var linkageParam = alarmLinkage.getToSaveSetting();
        // 如果返回false，说明获取数据出错，有可能是用户输入数据格式错误，弹出提示，中断操作
        if (!linkageParam) {
            return;
        }
        if (linkageParam[0].msg) {
            linkageParam[0].msg.deviceType = $('#deviceTypeTxt').attr('value');
            linkageParam[0].msg.textType = $('input[name="textType"]:checked').val();
            linkageParam[0].msg.messageTypeOne = $('#messageTypeOne').val();
            linkageParam[0].msg.messageTypeTwo = $('#messageTypeTwo').val();
        }

        for (var i = 0; i < linkageParam.length; i++) {
            if (linkageParam[i].msg != null) {
                linkageParam[i].msg.deviceType = $('#deviceTypeTxt').attr('value');
            }
            // 组装I/O输出控制提交参数
            if (linkageParam[i].outputControl != null) {
                if (linkageParam[i].outputControl.controlSubtype != '3') {
                    linkageParam[i].outputControl.analogOutputRatio = '';
                }
                linkageParam[i].outputControl.protocolType = protocolType;
                if (!alarmLinkage.ioValidate()) return;
            }
        }
        console.log(linkageParam);
        linkageParam = JSON.stringify(linkageParam);

        var linkageFlag = $("#linkageFlag").val();
        if (linkageFlag == 0) {
            var toPost = {
                vehicleId: alarmLinkageVehicleId,
                linkageParam: linkageParam
            };
            var url = '/clbs/a/alarmSetting/savePhotoSetting';

            // 上传
            json_ajax("POST", url, "json", true, toPost, function (r) {
                if (r.success == true) {
                    layer.msg(saveSuccessTip);
                    $("#commonWin").modal("hide");
                    myTable.refresh();
                } else {
                    layer.msg(publicErrorTip);
                }
            });
        } else {
            var paramData = {
                linkageParam: linkageParam,
                vehicleId: $("#alarmLinkageVehicleId").val()
            }
            var url = '/clbs/a/alarmSetting/saveMorePhotoSetting';
            json_ajax('post', url, 'json', true, paramData, function (data) {
                if (data.success) {
                    layer.msg(saveSuccessTip)
                    $("#commonWin").modal("hide");
                    myTable.refresh();
                } else {
                    layer.msg(publicErrorTip);
                }
            });
        }

    },
    // 验证拍照参数是否正确
    photoValidate: function () {
        return $("#takePhoto").validate({
            rules: {
                // wayId: {
                //     required: true
                // },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [0, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayId: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },
    //验证下发内容长度
    msgValidate: function () {
        return $("#sendText").validate({
            rules: {
                msgContent: {
                    maxlength: 512
                }
            },
            messages: {
                msgContent: {
                    maxlength: "下发内容长度最大为512"
                }
            }
        }).form();
    },
    // 验证I/O输出控制表单
    ioValidate: function () {
        return $("#outputControlForm").validate({
            rules: {
                controlTime: {
                    integerRange: [1, 65534],
                },
                analogOutputRatio: {
                    required: true,
                    decimalOne: true,
                    decimalRange: [0, 100],
                },
            },
            messages: {
                controlTime: {
                    integerRange: "请输入1~65534间的正整数"
                },
                analogOutputRatio: {
                    required: '请输入模拟量输出比例',
                    decimalOne: '请输入0~100之间的数字，最多保留一位小数',
                    decimalRange: '请输入0~100之间的数字，最多保留一位小数',
                },
            }
        }).form();
    },
    // 验证录像参数是否正确
    recordingValidate: function () {
        return $("#getRecording").validate({
            rules: {
                wayId: {
                    required: true
                },
                time: {
                    required: true,
                    digits: true,
                    range: [0, 65535]
                },
                command: {
                    range: [0, 10],
                    required: true
                },
                saveSign: {
                    required: true
                },
                distinguishability: {
                    required: true
                },
                quality: {
                    range: [1, 10],
                    required: true
                },
                luminance: {
                    range: [0, 255],
                    required: true
                },
                contrast: {
                    range: [0, 127],
                    required: true
                },
                saturability: {
                    range: [0, 127],
                    required: true
                },
                chroma: {
                    range: [0, 255],
                    required: true
                },
            },
            messages: {
                wayId: {
                    required: alarmSearchChannelID
                },
                time: {
                    required: alarmSearchIntervalTime,
                    digits: alarmSearchIntervalError,
                    range: alarmSearchIntervalSize
                },
                command: {
                    range: alarmSearchPhotoSize,
                    required: alarmSearchPhotoNull
                },
                saveSign: {
                    required: alarmSearchSaveNull
                },
                distinguishability: {
                    required: alarmSearchResolutionNull
                },
                quality: {
                    range: alarmSearchMovieSize,
                    required: alarmSearchMovieNull
                },
                luminance: {
                    range: alarmSearchBrightnessSize,
                    required: alarmSearchBrightnessNull
                },
                contrast: {
                    range: alarmSearchContrastSize,
                    required: alarmSearchContrastNull
                },
                saturability: {
                    range: alarmSearchSaturatedSize,
                    required: alarmSearchSaturatedNull
                },
                chroma: {
                    range: alarmSearchColorSize,
                    required: alarmSearchColorNull
                }
            }
        }).form();
    },
    //终端TTS读播与终端TTS读播并处理二选一
    alternativeTts: function () {
        var isChecked = $(this).is(":checked");
        if (isChecked) {
            $(".alternative").prop('checked', false);
        }
    },
    alternative: function () {
        var isChecked = $(this).is(":checked");
        if (isChecked) {
            $(".alternativeTts").prop('checked', false);
        }
    },
    //报警处理联动
    alarmLinkageValidate: function () {
        var handleMethod = $("#alarmLinkageMore input[name='handleMethod']:checked").val()
        //终端TTS语音是否选中
        var isTerminalSelected = function(){
            var flag = false
            $("#sendText input[name='marks']:checked").each(function(){
                if($(this).val() == 4){
                    flag = true
                    return false
                }
            })
            return flag
        }
        //通道ID是否选中
        var isTunnelIdSelected = function(){
            return $("#multiOptions input[name='wayId']:checked").length !== 0
        }
        //语音下发验证
        if(handleMethod == 1){
            if(!$("#msgAreaCheckbox").is(':checked')){
                layer.msg("保存失败，请完成下发短信的设置！")
                return false
            }else if(!isTerminalSelected()) {
                layer.msg("保存失败，下发短信时TSS读播为必选项！")
                return false
            }
        }else if(handleMethod == 2){//快速拍照验证
            if(!$("#photoAreaCheckbox").is(':checked')){
                layer.msg("保存失败，请完成拍照的设置！")
                return false
            }
        }
        //拍照通道验证
        if($("#photoAreaCheckbox").prop("checked")){
            if(!isTunnelIdSelected()) {
                layer.msg("保存失败，拍照时通道ID为必选项！")
                return false
            }
        }
        // 处理人验证  注意特殊情况：处理人可能通过上级设置，而当前用户权限下查不到该用户
        var isPeopleSelected = function(){
            var treeObj = $.fn.zTree.getZTreeObj("managerTree");
            var nodes = treeObj.getCheckedNodes();
            var peopleSelected = nodes.length > 0
            var currentSeting = settingList.find(function (item) {
                return item.pos == currentPos
            })
            var inputName = $("#managerTreeInput").val()
            currentSeting = currentSeting ? currentSeting : {}
            // 是否是通过上级设定的名字
            var isOldname = currentSeting.handleUsername == inputName
            return peopleSelected || isOldname || inputName == 'admin'
        }
        if($("#alarmLinkage").prop("checked")){
            if(!isPeopleSelected()){
                layer.msg("保存失败，请选择处理人！");
                return false
            }
        }
        return true
    },
    loadManagerTree: function () {
        var zTreeOnAsyncSuccess = function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true); // 展开所有节点
        }
        var zTreeOnCheck = function(event, treeId, treeNode) {;
            $("#managerTreeInput").val(treeNode.name)
        };
        function zTreeBeforeCheck(treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("managerTree");
            var nodes = treeObj.getCheckedNodes();
            for (var i=0, l=nodes.length; i < l; i++) {
                treeObj.checkNode(nodes[i], false, true);
            }
            return true;
        };
        //原始数据处理
        var handleRowData = function (treeId, parentNode, responseData) {
            var userName = $("#userName").html()
            var newData = responseData.obj.map(function (item) {
                if(item.name == userName){
                    // $("#managerTreeInput").prop('placeholder',item.name)
                    $("#managerTreeInput").val(item.name)
                    item.checked = true
                }
                item.type = 'group'
                return item
            })
            return newData;
        };
        var setting = {
            async: {
                url: "/clbs/c/user/dropdown",
                type: "get",
                enable: true,
                dataType: "json",
                dataFilter: handleRowData
            },
            view: {
                dblClickExpand: false,
                nameIsHTML: true,
                countClass: "group-number-statistics",
                showIcon: false,
            },
            data: {
                simpleData: {
                    enable: true
                },
            },
            callback: {
                onAsyncSuccess: zTreeOnAsyncSuccess,
                onCheck: zTreeOnCheck,
                beforeCheck: zTreeBeforeCheck
            },
            check: {
                enable: true,
                chkStyle: "radio",
            }
        };

        $.fn.zTree.init($("#managerTree"), setting, null);
    },
}
$(function () {
    alarmLinkage.init();
    $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'managerTreeInput') {
            var treeObj = $.fn.zTree.getZTreeObj("managerTree");
            var nodes = treeObj.getCheckedNodes();
            for (var i=0, l=nodes.length; i < l; i++) {
                treeObj.checkNode(nodes[i], false, true);
            }
            var allNodes = treeObj.getNodes();
            treeObj.showNodes(allNodes);
        }
        if(id == 'fastMsgQueryParam'){
            $("#toolTip").hide()
        }
    });
    if ($('#deviceTypeTxt').val() == '11') {// 2019协议
        $('.defaultValue').attr('selected', false);
    }
    $('#controlSubtype').on('change', function () {
        var curVal = $(this).val();
        if (curVal === '3') {
            $('.analogInfo').show();
        } else {
            $('.analogInfo').hide();
        }
    });
    $('#alarmLinkage').on('change', function () {
        var checked = $(this).is(':checked');
        if (checked) {
            $("#alarmLinkageMore").find('input').each(function (index) {
                if(index == 0 || index == 4){
                    $(this).attr({"disabled":false})
                    $(this).click()
                }else {
                    $(this).attr("disabled",false)
                }
            })
        } else {
            $("#alarmLinkageMore").find('input').each(function () {
                $(this).attr({"checked":false,"disabled":true})
            })
        }
    });
    $(".modal-body").addClass("modal-body-overflow");
    $(".modal-body").css({"height": "auto", "max-height": ($(window).height() - 194) + "px"});
    $("#managerTreeInput").parent().on("click",function (e) {
        // $("#managerTreeBox").toggle()
        e.stopPropagation()
        var $box = $("#managerTreeBox")
        if($box.is(':hidden')){
            $box.show()
            $(document).one("click", function(e){
                if(!$("#managerTreeBox").parent().get(0).contains(e.target)){
                    $("#managerTreeBox").hide()
                }
            });
        }
    })
    $("#managerTreeInput").on("input oninput", function () {
        search_ztree('managerTree', 'managerTreeInput', 'group');
    });
    // $("#alarmTypeUl").on('click',function (e) {
    //     if(e.target.nodeName.toLowerCase() == 'input'){
    //         var checkedLength = alarmTypeUl.find("input[type='checkBox']:checked").length
    //         if(checkedLength == 1){
    //             currentPos = $(e.target).parent().data('pos');
    //             var $preChosen = alarmTypeUl.find('span.chosen');
    //             $preChosen.removeClass('chosen');
    //             $(e.target).next().addClass('chosen');
    //         }
    //     }
    // })
    $("#alarmLinkage, #msgAreaCheckbox, #photoAreaCheckbox, #recordingAreaCheckbox, #outputControl, #videoAreaCheckbox, #uploadAudioResourcesFlag").on('change',function (e) {
        if($(this).prop('checked')){
            alarmTypeUl.find("input[type='checkBox']")
            if(alarmTypeUl.find('span.chosen').length == 0){
                e.preventDefault()
                layer.msg("请先选择一个报警类型");
                $(this).click()
            }
        }
    })
    $("#alarmLinkage, #msgAreaCheckbox, #photoAreaCheckbox, #recordingAreaCheckbox, #outputControl, #videoAreaCheckbox, #uploadAudioResourcesFlag").next().on('click',function (e) {
        var checkBox = $(this).prev()
        if(!checkBox.prop('checked')){
            if(alarmTypeUl.find('span.chosen').length == 0){
                e.preventDefault()
                e.stopPropagation()
                layer.msg("请先选择一个报警类型");
                setTimeout(checkBox.click,0)
            }
        }
    })
    alarmTypeUl.find("input[type='checkBox']").on('change',function (e) {
        if(alarmTypeUl.find("input[type='checkBox']:checked").length == 0){
            layer.msg("至少需要一个报警类型");
            $(this).click()
        }
        var clickPos = $(e.target).parent().data('pos')
        if(!$(this).prop('checked') && currentPos == clickPos){
            alarmLinkage.renderSetting(defaultSetting)
            $("#alarmLinkageMore input[type='radio']").prop({disabled:true,checked:false})
        }else {
            var index = tempSettingList.findIndex(function (item) {
                return item.pos == clickPos
            })
            tempSettingList.splice(index,1)
            var index = settingList.findIndex(function (item) {
                return item.pos == clickPos
            })
            settingList.splice(index,1)
        }
    })
})
