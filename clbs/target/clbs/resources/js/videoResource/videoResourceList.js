(function (window, $) {
    var vehicleList = [];
    var channelNumVideoList = [];
    var vehicleTreeId = "";
    var vehicleTreeName = "";
    var alarmBinary = [];
    var alarmTen = "";
    var zTreeIdJson = {};
    var vico = "";
    var size;
    var param = [];
    var broadVideoCastSocket = [];
    var videoPlayLists; //重新加载树之后,重置集合
    var FtpResourcesLists; //查询FTP获得的资源列表集合
    var FtpAdvancedResourcesLists; //高级查询FTP获得的资源列表集合
    var deviceResourcesLists; //查询终端，获得资源列表集合
    var videoPlayDeviceLists; //查询终端，监控对象，播放数据集合存放
    var videoChannelStopTimeList; //创建存储通道号及播放暂停时间集合
    var onlineType = 'all';
    var coordMap;
    var checkFlag = null;
    var treePid = null; //树选中的id
    var oldVehicleId, oldType, oldChannlNumer, mobile, vehcleBrand, deviceNumber;
    var videoName = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen"]; //视频分隔类样式
    var headerHeight = $("#header").height(); //header高度
    var lineArr = []; //轨迹线坐标数组
    var resultful; //行驶数据
    var marker; //车辆位置
    var msgSNAck;
    var alarmTypeLists; //FTP 报警类型集合
    var deviceQueryType = 0; // 终端查询类型类型 ： 0 点击查询       1 点击日历
    var _checkChannelList; //通道号保存集合
    var channelDate; //通道号视频数据
    var channelLists; //通道号视频数据2
    var channelDates = {};
    var channelDatesNoSelect = []; //非音视频通道
    var channelAllList = []; //获取到的所有通道号
    var deviceQueryType_start = 0;
    var videoDataList; //通道号及视频集合
    var videoPlayFlag = true; //视频播放暂停时Flag
    var fileUploadList; //文件上传集合
    var resourceVideoGoingIndex = 1; //资源列表快进初始值
    var resourceVideoBackIndex = 1; //资源列表快退初始值
    var playListVideoGoingIndex = 1; //播放列表快进初始值
    var playListVideoBackIndex = 1; //播放列表快退初始值

    var pluginClickPlayFlag = false; //插件点击位置是否有数据
    var pluginSendData = []; //插件传输数据
    var pluginClickStartTime; //插件点击时间 用于地图轨迹点
    var pluginStopTime; //日历点击年月日获取
    var polylinesNew; //轨迹覆盖线路
    var playPauseMapTrackFlag = null; //视频播放暂停按钮点击时地图描绘线路
    var resourceListVideoPauseTime = 0; //记录资源列表视频暂停时间节点
    var terminalVideoPlayStopFlag = false; //记录播放列表终端视频资源播放暂停后再次播放flag
    var ftpVideoPlayStopFlag = false; //记录播放列表FTP视频资源播放暂停后再次播放flag
    var pluginClickBlankChangeTabFlag = false; //记录点击插件点击空白后 切换TAB flag
    var pluginClickSendPlayEntrance = false; //记录视频播放后 点击暂停后重新选择插件时间点数据 再次播放Flag

    var videoPlayState = false;


    var searchDate; // 查询数据日期
    var resourceVideoPlayIndex = []; // 资源视频正在播放数据
    var terminalVideoPlayList; // 终端视频播放状态列表
    var playListVideoPlayWay = 0; // 播放列表回放方式
    var objAllNum; // 监控对象数量

    var mseVideoLists; // 初始化视频插件对象集合

    var videoControlPlugin; // 视频播放控制插件

    var videoPluginSelect = false; // 视频播放插件选中状态
    var videoDragState = false; // 是否是拖拽状态
    var createChannelVoice = null; // 打开的通道声音Id

    var startTimeGlobal = null;
    var endTimeGlobal = null;

    var inquiryType = null; //查询类型

    var ifadvanced = false; //是否是高级查询
    var getResFlag = true; // 是否查询资源数据

    var terminalTimer = null; //终端资源请求定时器
    var terminalDateTimer = null; //终端日历请求定时器

    var socketConnectNum = 0; // 视频通道socket连接数量
    var isPlayFlag = true; // 判断是否是播放后,视频通道才全关闭的
    var subscribeSingleVideo = false;

    var deviceTypeArr = ['1', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28'];
    var currentProtocol = ''; // 当前选择监控对象的协议类型

    var timerIframePlayer = null;

    var videoMap;
    var resourceTimeout = null; // 获取资源列表超时定时器
    var getMothDataCallBackTime = null //定时器
    var selectSearchType = 1
    var ftpVideoPlay = false

    var showValueData = true ///用于高级查询时不显示  ‘重试或请用高级查询’  提示语使用

    var groupSelect = $("#groupSelect");
    resourceList = {
        getOnlineArray: null,
        videoIsFlag: false,
        init: function () {
            //日历高亮
            $('.calendar3').calendar();
            layer.msg('请选择监控对象', {
                time: -1
            });
            //创建地图
            videoMap = new AMap.Map("resource-map-module", {
                resizeEnable: true, //是否监控地图容器尺寸变化
                zoom: 18, //地图显示的缩放级别
            });
            //文件上传消息ID集合
            fileUploadList = new resourceList.maps();

            //创建通道号保存集合 用于勾选及取消时与数据表格联动问题
            _checkChannelList = new resourceList.maps();

            //保存监控对象时间及视频文件集合(长度大于0表示已经查询到数据)
            videoPlayLists = new resourceList.maps();

            //FTP查询后得到的资源列表集合
            FtpResourcesLists = new resourceList.maps();

            //高级FTP查询后得到的资源列表集合
            FtpAdvancedResourcesLists = new resourceList.maps();

            //终端查询后得到的资源列表集合
            deviceResourcesLists = new resourceList.maps();

            //保存监控对象时间，资源类型，码流类型，存储类型集合
            videoPlayDeviceLists = new resourceList.maps();

            //通道号及视频集合
            videoDataList = new resourceList.maps();

            //创建存储通道号及播放暂停时间集合
            videoChannelStopTimeList = new resourceList.maps();

            // 终端视频各通道号播放状态集合
            terminalVideoPlayList = new resourceList.maps();

            //创建原始坐标集合
            coordMap = new resourceList.maps();

            // 视频插件
            mseVideoLists = new resourceList.maps();

            resourceList.setTreeHeight();
            // 监听窗口大小变化
            $(window).resize(function () {
                resourceList.setTreeHeight();
            });
            // 视频socket接口订阅
            resourceList.videoSocketSubscribe();
            resourceList.videoDateSocketSubscribe();

            // 初始化树判断监控对象数量
            json_ajax("POST", "/clbs/m/functionconfig/fence/bindfence/getStatistical", "json", true, {
                webType: 2
            }, resourceList.getMonitoringNumber);
        },

        /**
         * 日历插件切换年月回调
         * author wanjikun
         */

        changeYMcallback: function () {
            if (vehicleTreeId !== "") {
                ifadvanced = false;
                resourceList.videoIsFlag = true
                resourceList.inquiry(1);
            } else {
                resourceList.videoIsFlag = false
            }
        },
        /**
         * 设置监控对象树高度
         * @author lichuan
         */
        setTreeHeight: function () {
            var resizeWidth = $(window).width();
            if (resizeWidth < 1200) {
                $("body").css("overflow", "auto");
            } else {
                $("body").css("overflow", "hidden");
            }
            var windowHeight = $(window).height();
            headerHeight = $("#header").height(); //顶部的高度
            var panelHead = $(".panel-heading").height() + 20; //标题栏高度
            var citySelHght = $(".select-condition").height() + 10; //输入框高度

            var trLength = $(".calendar3 tbody tr").length;
            if (trLength === 5) {
                var calHeight = 295;
            } else if (trLength === 4) {
                calHeight = 350;
            } else if (trLength === 6) {
                calHeight = 340
            }
            var zTreeHeight = windowHeight - headerHeight - panelHead - calHeight - citySelHght - 26 - 34;
            var treeObj = $("#treeDemo");
            treeObj.css("height", zTreeHeight + "px");
            if (windowHeight <= 667) {
                treeObj.css("height", 150 + "px");
            }
        },
        /**
         * 封装map集合
         * @author wangjianyu
         */
        maps: function () {
            this.elements = [];
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
                this.elements = [];
            };
            //向MAP中增加元素（key, value)
            this.put = function (_key, _value, _newKey) {
                this.elements.push({
                    key: _key,
                    value: _value,
                    newKey: _newKey
                });
            };
            //删除指定KEY的元素，成功返回True，失败返回False
            this.remove = function (_key) {
                var bln = false;
                try {
                    for (var i = 0, len = this.elements.length; i < len; i++) {
                        if (this.elements[i].key === _key) {
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
                        if (this.elements[i].key === _key) {
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
                        if (this.elements[i].key === _key) {
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
                        if (this.elements[i].value === _value) {
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
                var arr = [];
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].value);
                }
                return arr;
            };
            //获取MAP中所有KEY的数组（ARRAY）
            this.keys = function () {
                var arr = [];
                for (var i = 0, len = this.elements.length; i < len; i++) {
                    arr.push(this.elements[i].key);
                }
                return arr;
            };
        },
        /**
         * 查询时间初始化
         * @author angbike
         */
        searchTimeLoad: function () {
            //开始时间
            var firstDate = new Date();
            var searchStartTime = new XDate(firstDate).toString('yyyy-MM-dd');
            $("#searchStartTime").val(searchStartTime + " 00:00:00");
            //结束时间
            var searchEndTime = new XDate(firstDate).toString('yyyy-MM-dd');
            $("#searchEndTime").val(searchEndTime + " 23:59:59");
            //时间初始化
            laydate.render({
                elem: '#searchStartTime',
                theme: '#6dcff6',
                type: 'datetime'
            });
            laydate.render({
                elem: '#searchEndTime',
                theme: '#6dcff6',
                type: 'datetime'
            });
        },
        ///////////////////////////////////////////左侧车辆树加载的相关函数 start/////////////////////////////////////////////////////////
        /**
         * 车辆树加载并组装
         * @author yangyi
         */
        resourceListTree: function () {
            //车辆树Setting
            var setChar = {
                async: {
                    url: resourceList.getIcoTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {
                        "type": "resource"
                    },
                    dataFilter: resourceList.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_sphf_ztree,
                    countClass: "group-number-statistics",
                    addDiyDom: resourceList.addDiyDom
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: resourceList.beforeClickVehicle,
                    beforeCheck: resourceList.zTreeBeforeCheck,
                    onCheck: resourceList.onCheckVehicle,
                    onExpand: resourceList.zTreeOnExpand,
                    onAsyncSuccess: resourceList.zTreeOnAsyncSuccess,
                    onNodeCreated: resourceList.zTreeOnNodeCreated,
                    onAsyncError: resourceList.ZtreeonAsyncError
                }
            };

            //车辆树初始化
            $.fn.zTree.init($("#treeDemo"), setChar, null);
        },
        getIcoTreeUrl: function (treeId, treeNode) {
            var url;
            if (treeNode == null) {
                if (onlineType === 'all') {
                    if (objAllNum <= 5000) {
                        url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTree?webType=2';
                    } else {
                        url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
                    }
                } else if (onlineType === 'online') {
                    url = "/clbs/m/basicinfo/monitoring/vehicle/new/treeStateInfo?webType=2&type=1";
                } else {
                    if (objAllNum <= 5000) {
                        url = "/clbs/m/basicinfo/monitoring/vehicle/treeStateInfo?webType=2&type=7";
                    } else {
                        url = "/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree";
                    }
                }
                return url;
            } else if (treeNode.type === "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/new/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor" + "&webType=2";
            }
        },
        zTreeBeforeAsync: function () {
            return bflag;
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            if (!treeNode.checked) {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            return false;
        },
        ZtreeonAsyncError: function () {
            $("#treeLoading i").css('visibility', 'hidden');
            $("#treeLoading span").text('加载失败');
        },
        fuzzySeacchTreeonAsyncError: function () {
            $("#treeLoading i").css('visibility', 'hidden');
            $("#treeLoading span").text('查询失败');
        },
        zTreeOnAsyncSuccess: function () {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000) {
                treeObj.checkAllNodes(true);
            }
            $('#treeLoading').hide();
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var obj;
            if (responseData.msg == undefined) {
                // obj = JSON.parse(ungzip(responseData));
                if (onlineType === 'online') {
                    obj = responseData;
                } else {
                    obj = JSON.parse(ungzip(responseData));
                }
            } else {
                obj = JSON.parse(ungzip(responseData.msg));
            }
            var data;
            if (obj.tree != null && obj.tree != undefined) {
                data = obj.tree;
                size = obj.size;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                if (data[i].type == "group" || data[i].type == "assignment") {
                    data[i].open = true;
                    data[i].nocheck = true;
                }
                // if (deviceTypeArr.indexOf(data[i].deviceType) != '-1') { // 808协议的车丶人丶物
                //     data[i].isParent = false;
                // }
                if (data[i].type == 'people' || data[i].type == 'vehicle' || data[i].type == 'thing') { // 车丶人丶物
                    data[i].isParent = false;
                }
            }
            return data;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                    nodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                var ns = [];
                //节点id
                var nodeId;
                if (treeNode.type === "group" || treeNode.type === "assignment") { //若勾选的为组织或分组
                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {
                            "parentId": treeNode.id,
                            "type": treeNode.type
                        },
                        function (data) {
                            if (data.success) {
                                nodesLength += data.obj;
                            } else {
                                if (data.msg != null && data.msg != '') {
                                    layer.msg(data.msg);
                                }
                            }
                        });

                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type === "people" || nodes[i].type === "vehicle") {
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                            if (nd == null && $.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                } else if (treeNode.type === "people" || treeNode.type === "vehicle" || treeNode.type === "thing") { //若勾选的为监控对象
                    for (var i = 0; i < nodes.length; i++) {
                        nodeId = nodes[i].id;
                        if (nodes[i].type === "people" || nodes[i].type === "vehicle" || nodes[i].type === "thing") {
                            if ($.inArray(nodeId, ns) == -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if (flag) {
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if (treeNode.type === "group" && !treeNode.checked) {
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function (e, treeId, treeNode) {
            broadVideoCastSocket = []
            layer.closeAll();
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            if (!treeNode.checked) {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            currentProtocol = treeNode.deviceType;
            if (currentProtocol === '23') { // 报批稿协议特殊处理
                $('#type').val('0');
                $('#allNum').hide();
                $('#ftpServer').hide();
            } else {
                $('#allNum').show();
                $('#ftpServer').show();
            }
            treePid = treeNode.id
            resourceList.vehicleListId(treeNode); // 记录勾选的节点
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
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {
                    "groupId": treeNode.id,
                    "isChecked": treeNode.checked,
                    "monitorType": "vehicle"
                }, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined) {
                        $.each(result, function (i) {
                            var pid = i; //获取键值
                            var chNodes = result[i] //获取对应的value
                            var parentTid = zTreeIdJson[pid][0];
                            var parentNode = treeObj.getNodeByTId(parentTid);
                            if (parentNode.children === undefined) {
                                treeObj.addNodes(parentNode, []);
                            }
                        });
                    }
                })
            }
        },
        ///////////////////////////////////////////左侧车辆树加载的相关函数 end/////////////////////////////////////////////////////////


        //////////////////////////////////////////左侧车辆树搜索相关函数 start/////////////////////////////////////////////////////////
        /**
         * 车辆树查询数据组装
         * @author hujun
         * @param treeId
         * @param parentNode
         * @param responseData
         * @returns {*}
         */
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
            var list = [];
            if (vehicleList !== null && vehicleList !== undefined && vehicleList.length > 0) {
                var str = (vehicleList.slice(vehicleList.length - 1) === ',') ? vehicleList.slice(0, -1) : vehicleList;
                list = str.split(",");
            }

            for (var i = 0; i < responseData.length; i++) {
                if (responseData[i].type == "group" || responseData[i].type == "assignment") {
                    responseData[i].open = true;
                    responseData[i].nocheck = true;
                }
                if (deviceTypeArr.indexOf(responseData[i].deviceType) != '-1') {
                    responseData[i].isParent = false;
                }
            }
            return filterQueryResult(responseData, list);
        },

        /**
         * 模糊搜索车辆树
         * @author yangyi
         * @param param
         */
        searchVehicleTree: function (param) {
            $("#treeLoading i").css('visibility', 'visible');
            $("#treeLoading span").text('正在查询，请稍候...');
            $('#treeLoading').show();
            var setQueryChar = {
                async: {
                    url: "/clbs/a/search/new/monitorTreeFuzzy",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    otherParam: {
                        "type": "single",
                        "queryParam": param,
                        "queryType": "name",
                        "deviceType": "1",
                        "webType": "2"
                    },
                    dataFilter: resourceList.ajaxQueryDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_sphf_ztree,
                    countClass: "group-number-statistics",
                    addDiyDom: resourceList.addDiyDom
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: resourceList.beforeClickVehicle,
                    onAsyncSuccess: resourceList.fuzzyZTreeOnAsyncSuccess,
                    onCheck: resourceList.fuzzyOnCheckVehicle,
                    onAsyncError: resourceList.fuzzySeacchTreeonAsyncError,
                }
            };
            $.fn.zTree.init($("#treeDemo"), setQueryChar, null);
        },
        /**
         * 在节点上固定显示自定义控件
         */
        addDiyDom: function (treeId, treeNode) {
            if (['vehicle', 'people', 'thing'].indexOf(treeNode.type) !== -1 && treeNode.status && treeNode.status !== 3) {
                var src = treeNode.acc === 1 ? "../../resources/img/videoPrompt/accgreen.svg" : "../../resources/img/videoPrompt/accgray.svg";
                var str = '<img src="' + src + '"style="height:19px;width:43px;"/>';
                $("#" + treeNode.tId + "_a").append(str);
            }
        },
        /**
         * 模糊搜索车辆树点击事件
         * @author yangyi
         * @param e
         * @param treeId
         * @param treeNode
         */
        fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
            broadVideoCastSocket = []
            //获取树结构
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            if (!treeNode.checked) {
                zTree.checkNode(treeNode, !treeNode.checked, true, true);
            }
            currentProtocol = treeNode.deviceType;
            if (currentProtocol === '23') { // 报批稿协议特殊处理
                $('#type').val('0');
                $('#ftpServer').hide();
            } else {
                $('#ftpServer').show();
            }
            //获取勾选状态改变的节点
            var changeNodes = zTree.getChangeCheckedNodes();
            if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
                resourceList.vehicleListId(treeNode); // 记录勾选的节点
                var checkedNodes = zTree.getCheckedNodes(true);
                var nodesLength = 0;
                for (var i = 0; i < checkedNodes.length; i++) {
                    if (checkedNodes[i].type === "people" || checkedNodes[i].type === "vehicle" || checkedNodes[i].type === "thing") {
                        nodesLength += 1;
                    }
                }
                if (nodesLength > 5000) {
                    layer.msg("最多勾选5000个监控对象！");
                    for (i = 0; i < changeNodes.length; i++) {
                        changeNodes[i].checked = false;
                        zTree.updateNode(changeNodes[i]);
                    }
                }
            }
            //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
            for (i = 0; i < changeNodes.length; i++) {
                changeNodes[i].checkedOld = changeNodes[i].checked;
            }
            treePid = treeNode.id
            groupSelect.val(treeNode.name.replace('ACC', ''));
        },
        /**
         * 模糊搜索车辆树加载成功方法
         * @author yangyi
         * @param event
         * @param treeId
         */
        fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            allNodes = treeObj.getNodes();
            if (allNodes.length == 0) {
                $("#treeLoading i").css('visibility', 'hidden');
                $("#treeLoading span").text('未找到查询对象');
            } else {
                $("#treeLoading").hide();
            }
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.expandAll(true);
            // $('#treeLoading').hide();
        },
        //////////////////////////////////////////左侧车辆树搜索相关函数 end/////////////////////////////////////////////////////////

        /**
         * 获取车辆树节点，点击方法
         * @author yangyi
         */
        vehicleListId: function (data) {
            if (navigator.appName === "Microsoft Internet Explorer") {
                $('.input-placeholder').remove();
            }
            groupSelect.val(data.name.replace('ACC', ''));
            vehicleTreeName = data.name.replace('ACC', '');
            vehicleTreeId = data.id;

            ifadvanced = false;

            if ($("#resourceListActive").hasClass("active")) {
                // 点击日期时，如果是在资源列表播放的，停止播放，并下发9202
                resourceList.resourceVideoStopFn();
                $("#resourceListActive,#resourceList,#ftpList").removeClass("active");
                $("#playListActive,#playList").addClass("active");
                $(".video-back-module,.video-play-module").show();
                $(".video-play-select,.video-resource-module").hide();
            }
            resourceList.inquiry(1);
            resourceList.getVideoChannel(vehicleTreeId);
        },

        /**
         * 点击车辆获取通道号
         * @author yangyi
         * @param vehicleId
         */
        getVideoChannel: function (vehicleId) {
            var url = "/clbs/realTimeVideo/resource/getVideoChannel";
            var parameter = {
                "vehicleId": vehicleId
            };
            json_ajax("POST", url, "json", false, parameter, resourceList.getVideoChannelCallack);
        },

        /**
         * 通道号组装，排序
         * @author
         * @param data
         */
        getVideoChannelCallack: function (data) {
            if (data.success) {
                var channelList = data.obj;
                channelLists = channelList;
                var str;
                channelDate = [];
                var logicChannelDate = [];
                channelDates = {};
                channelDatesNoSelect = []
                for (var i = 0; i < channelList.length; i++) {
                    if (channelList[i].channelType === 0 || channelList[i].channelType === 2) {
                        channelDate.push(channelList[i].logicChannel);
                        logicChannelDate.push(channelList[i].logicChannel);
                        channelDates[channelList[i].logicChannel] = channelList[i].logicChannel;
                    } else {
                        channelDates[channelList[i].logicChannel] = channelList[i].logicChannel;
                    }
                }
                channelAllList.forEach(it => {
                    if (!channelDate.some(item => item === it)) {
                        channelDatesNoSelect.push(it)
                        logicChannelDate.push(it)
                        channelDate.push(it)
                    }
                })
                str = '<option value="0">全部</option>';
                if (currentProtocol === '23') {
                    str = '';
                }
                for (var j = 0; j < logicChannelDate.length; j++) {
                    var logicChannal = logicChannelDate[j];
                    if (oldChannlNumer === channelDate[j]) {
                        str += '<option selected value=' + logicChannal + '>' + logicChannal + '</option>';
                    } else {
                        str += '<option value=' + logicChannal + '>' + logicChannal + '</option>';
                    }
                }
                $("#channelNum").html(str);
            }
        },

        /**
         * 简单数组排序规则
         * @author yangyi
         * @param a
         * @param b
         * @returns {number}
         */
        easySort: function (a, b) {
            return a - b
        },

        /**
         * 车辆树下拉菜单显示
         * @author yangbike
         */
        showMenu: function () {
            var menuContent = $("#menuContent");
            if (menuContent.is(":hidden")) {
                var inpwidth = groupSelect.width();
                var spwidth = $("#groupSelectSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName === "Microsoft Internet Explorer") {
                    menuContent.css("width", (inpwidth + 7) + "px");
                } else {
                    menuContent.css("width", allWidth + "px");
                }
                $(window).resize(function () {
                    var inpwidth = groupSelect.width();
                    var spwidth = $("#groupSelectSpan").width();
                    var allWidth = inpwidth + spwidth + 21;
                    if (navigator.appName === "Microsoft Internet Explorer") {
                        menuContent.css("width", (inpwidth + 7) + "px");
                    } else {
                        menuContent.css("width", allWidth + "px");
                    }
                })
                menuContent.slideDown("fast");
            } else {
                menuContent.is(":hidden");
            }
            $("body").on("mousedown", resourceList.onBodyDown);
        },

        /**
         * 移除鼠标按下事件
         * @author yangbike
         */
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").off("mousedown", resourceList.onBodyDown);
        },

        /**
         * 车辆树的显示或隐藏
         * @author yangbike
         * @param event
         */
        onBodyDown: function (event) {
            if (!(event.target.id === "menuBtn" || event.target.id === "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                resourceList.hideMenu();
            }
        },

        /**
         * 报警树菜单显示
         * @author yangyi
         */
        showMenuAlarm: function () {
            var alarmContent = $("#alarmContent");
            if (alarmContent.is(":hidden")) {
                var inpwidth = $("#alarmSelect").width();
                var spwidth = $("#alarmSelectSpan").width();
                var allWidth = inpwidth + spwidth + 21;
                if (navigator.appName === "Microsoft Internet Explorer") {
                    alarmContent.css("width", (inpwidth + 7) + "px");
                } else {
                    alarmContent.css("width", allWidth + "px");
                }
                $(window).resize(function () {
                    var inpwidth = groupSelect.width();
                    var spwidth = $("#groupSelectSpan").width();
                    var allWidth = inpwidth + spwidth + 21;
                    if (navigator.appName === "Microsoft Internet Explorer") {
                        alarmContent.css("width", (inpwidth + 7) + "px");
                    } else {
                        alarmContent.css("width", allWidth + "px");
                    }
                })
                alarmContent.slideDown("fast");
            } else {
                alarmContent.is(":hidden");
            }
            $("body").on("mousedown", resourceList.onBodyDownAlarm);
        },

        /**
         * 移除鼠标按下事件
         * @author yangyi
         */
        hideMenuAlarm: function () {
            $("#alarmContent").fadeOut("fast");
            $("body").off("mousedown", resourceList.onBodyDownAlarm);
        },

        /**
         * 鼠标按下事件，隐藏或显示报警树菜单
         * @author yangyi
         * @param event
         */
        onBodyDownAlarm: function (event) {
            if (!(event.target.id === "menuBtn" || event.target.id === "alarmContent" || $(event.target).parents("#alarmContent").length > 0)) {
                resourceList.hideMenuAlarm();
            }
        },

        /**
         * 页面右侧地图模块大小显示
         * @author yangbike
         */
        mapAllShowFn: function () {
            var mapModule = $("#resource-map-module");
            if ($(this).children().hasClass("fa fa-chevron-left")) {
                $(this).children().removeClass("fa fa-chevron-left");
                $(this).children().addClass("fa fa-chevron-right");
                mapModule.css("float", "right");
                mapModule.removeClass("col-md-3");
                mapModule.addClass("col-md-12");
                $("#resource-video-module").hide();
            } else {
                $(this).children().removeClass("fa fa-chevron-right");
                $(this).children().addClass("fa fa-chevron-left");
                mapModule.removeClass("col-md-12");
                mapModule.addClass("col-md-3");
                setTimeout(function () {
                    $("#resource-video-module").show();
                }, 300)
            }
        },

        /**
         * 日历点击时执行函数 用于清空页面相关历史数据 重新加载
         * @author yangbike
         */
        pageLoadsEmpty: function ($checkbox) {
            //清空视频
            var resource = $("#resource-video-module div:nth-child(2)");
            resource.nextAll().remove();
            //显示播放列表添加的视频面板
            resource.removeClass('hidden-video').css({
                "width": "100%",
                "height": "100%"
            });
            var playListVideoPlay = $("#playListVideoPlay");
            var isPlaying = playListVideoPlay.hasClass("video-play");
            if (!isPlaying) {
                resourceList.stop($checkbox);
            } else {
                if ($("#resourceListActive").hasClass("active")) {
                    // 点击日期时，如果是在资源列表播放的，停止播放，并下发9202
                    if ($('#resourceListVideoPlay').hasClass('video-resource-play-check')) {
                        resourceList.resourceVideoStopFn();
                    }
                }
            }
            //播放列表 - 播放按钮恢复默认
            playListVideoPlay.removeClass("video-play-check").addClass("video-play");
            playListVideoPlay.prop("title", "播放");
            //播放列表-视频后退
            $("#playListVideoBack").off("click");
            //播放列表-视频停止
            $("#playListVideoStop").off("click");
            //播放列表-视频单帧播放
            $("#playListVideoFrame").off("click");
            //播放列表-视频前进
            $("#playListVideoGoing").off("click");
            //清空插件通道号
            $("#stationContainer").html("");
            //清空插件通道号对应值
            $("#timeBody").html("");
            //清空通道号DOM节点
            $("#videoChannelSelection").html("");
            //重置播放列表快进快退
            playListVideoGoingIndex = 1;
            playListVideoBackIndex = 1;
            $("#playLeftGripVal").text(1);
            $("#rightGripValue").css("left", "0px");
            //播放列表 - 还原查询终端资源类型时快进快退初始值
            $("#playListVideoBack,#playListVideoGoing").attr("data", "0");
            //重置地图轨迹描绘flag
            playPauseMapTrackFlag = null;
            //初始化之后显示播放列表默认视频
            if ($("#playListActive").hasClass("active")) {
                $("#playListVideoDefault").removeClass("hidden-video");
            }

            //播放插件重置
            // playListVideoStop();
            //jwVideoClear
            resourceList.jwVideoClear();
            //清空插件传输数组;
            pluginSendData = [];
            //清空FTP播放文件集合
            videoDataList.clear();
            //清空通道号查询原始数组
            channelDate = [];
            //清空通道号集合
            _checkChannelList.clear();
            //清空监控对象时间及视频文件集合
            videoPlayLists.clear();
            //清空监控对象时间，资源类型，码流类型，存储类型集合
            videoPlayDeviceLists.clear();
            //清空通道号及视频集合
            videoDataList.clear();

            //重置资源列表快进快退
            resourceVideoGoingIndex = 1;
            resourceVideoBackIndex = 1;
            $("#resourceLeftGripVal").text(1);
            $("#leftGripValue").css("left", "0px");
            //资源列表 - 播放按钮恢复默认
            $("#resourceListVideoPlay").removeClass("video-resource-play-check").addClass("video-resource-play");
            //资源列表 - 清空默认播放器内容
            document.getElementById('videoSource').removeAttribute("src");
            //资源列表 - 清空暂停视频时间记录
            resourceListVideoPauseTime = 0;

        },

        /**
         * 初始化报警类型数据
         * @author lichuan
         */
        setAlarm: function () {
            var setQueryChar = {
                async: {
                    url: "/clbs/realTimeVideo/resource/alarmTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    sync: false,
                    dataFilter: resourceList.ajaxAlarmDataFilter
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
                    fontCss: setFontCss_sphf_ztree,
                    countClass: "group-number-statistics",
                    showIcon: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: resourceList.beforeClickAlarm,
                    onAsyncSuccess: resourceList.AlarmTreeAsyncSuccess,
                    onCheck: resourceList.fuzzyOnCheckAlarm,
                }
            };
            $.fn.zTree.init($("#alarmTree"), setQueryChar, null);
        },

        /**
         * 点击高级查询按钮显示查询条件弹窗
         * @author lichuan
         */
        showQueryInfo: function () {
            if (vehicleTreeId === "") {
                layer.msg("请选择监控对象");
                return false;
            }
            // 报批稿协议显示字段处理
            if (currentProtocol === '23') {
                $('#codeSchema').val('0');
                $('#storageType').val('0');
                $('#videoType').val('0');
                $('#allVideoType').text('全部');
                $('.approvalHide').hide();
            } else {
                $('#allVideoType').text('音视频');
                $('.approvalHide').show();
            }

            resourceList.resetadvancedInfo();
            $("#advancedQuery").modal('show');
        },

        /**
         * 还原高级查询弹窗内容
         * @author lichuan
         */
        resetadvancedInfo: function () {
            resourceList.searchTimeLoad();
            resourceList.setAlarm();
            alarmTen = '';
            $('#alarmSelect').val('');
            // 监控对象协议类型不为报批稿,则通道默认选择全部
            if (currentProtocol !== '23') {
                $('#channelNum').val(0);
            }
            $('#videoType').val(0);
            $('#codeSchema').val(0);
            $('#storageType').val(0);
        },

        /**
         * 查询资源列表初始方法，先获取流水号，再执行查询，解决下发后websoket返回数据过快导致前端未接收到问题
         * type=0,为查询终端类型，其他为服务器
         * @author yangyi
         */
        inquiry: function (index, valueData) { //index 是否为高级查询  2为高级查询   //valueData 为是否需要更新日历，用于切换选项时使用
            var scalingBtn = $('#scalingBtn');
            if (scalingBtn.hasClass('fa-chevron-down')) {
                scalingBtn.click();
            }
            if (terminalDateTimer) clearTimeout(terminalDateTimer);
            $('#containers').hide();
            var $checkbox = $('.channel-checkbox');
            $('#videoChannelSelection').html('');
            $('#resourceListDataTable tbody').html('');
            $('#resourceListDataTables tbody').html('');
            //查询时插件重置
            // reset();
            //时间维度模块显示重置
            $("#stationContainer").css({
                "padding-top": "30px"
            });
            $("#timeLine").css({
                "height": "0px"
            });

            //高级查询不清空
            if (index !== 2) {
                //执行清空函数  清空相对应集合 数组 dom
                resourceList.pageLoadsEmpty($checkbox);
                //清空 FTP资源列表数据
                FtpResourcesLists.clear();
                //清空 资源列表数据
                deviceResourcesLists.clear();
            } else {
                // 高级查询清空高级集合
                FtpAdvancedResourcesLists.clear();
            }

            $("#resourceListDataTable tbody").html(""); // 	清空资源列表数据
            $("#resourceListDataTables tbody").html("") //清空终端 FTP列表数据

            var yearMonth = $('.calendar.calendar3 table caption span').html();
            var date = yearMonth.replace(/[^0-9]/ig, "-");
            var year = date.split('-')[0];
            var month = date.split('-')[1];

            if (valueData) { //等于2是执行高级查询不清空日历
                resourceList.buildDate([], [year, month, 1]);
            }

            videoMap.clearMap(); // 清空地图数据
            deviceQueryType = 0; // 点击查询
            deviceQueryType_start = 0;
            oldType = $("#type").val();
            layer.msg('正在努力加载数据,请耐心等待', {
                icon: 16,
                time: 19000,
                shade: [0.1, true],
                skin: "layui-layer-border layui-layer-hui"
            });
            resourceList.searchCalendar(index); // 后台查询日历数据（组装日历的显示）
        },


        /**
         * 查询数据，查询条件组装，保存查询条件以OLd开头
         * @author yangyi
         * @param index
         */
        searchCalendar: function (index) { //index 2为高级查询
            //再次查询之前数据清空
            param = [];

            var obj = {};
            if (vehicleTreeId === "") {
                layer.msg("请选择监控对象");
                return false;
            }
            obj.vehicleID = vehicleTreeId; //获取车辆id
            param.push(obj);
            //以下数据作为当前查询数据条件
            var type = $("#type").val();
            var ftpName = type;
            var startTime = $("#searchStartTime").val();
            var endTime = $("#searchEndTime").val();
            //若选择了开始日期和结束日期，则校验其合法性
            if (startTime !== "" && endTime !== "") {
                var startCheckTime = startTime;
                var endCheckTime = endTime;
                var reg = new RegExp('-', 'g');
                startCheckTime = startCheckTime.replace(reg, '/'); //正则替换
                endCheckTime = endCheckTime.replace(reg, '/');
                startCheckTime = new Date(parseInt(Date.parse(startCheckTime), 10)).getTime();
                endCheckTime = new Date(parseInt(Date.parse(endCheckTime), 10)).getTime();
                if (startCheckTime > endCheckTime) {
                    layer.msg("开始时间应在结束时间之前");
                    return false;
                }

                if (type !== 0) { //ftp
                    if ((endCheckTime - startCheckTime) > 2678400000) {
                        layer.msg("查询FTP视频信息时间范围不能大于一个月");
                        return false;
                    }
                } else { //终端
                    terminalDateTimer = setTimeout(function () {
                        layer.msg('终端无反馈,请重试');
                    }, 60000)
                }
            }
            if (startTime === "") {
                if (type !== 0) {
                    layer.msg("查询FTP资源开始时间不能为空");
                    return false;
                }
                startTime = "0000-00-00 00:00:00";

            }
            if (endTime === "") {
                if (type !== 0) {
                    layer.msg("查询FTP资源结束时间不能为空");
                    return false;
                }
                endTime = "0000-00-00 00:00:00";

            }
            var vehicleId = vehicleTreeId;
            var brand = vehicleTreeName;
            var alarmType = alarmTen;
            if (alarmType === "") {
                alarmType = 0;
            }
            var channlNumer = $("#channelNum").val();
            oldChannlNumer = channlNumer;
            var videoType = $("#videoType").val();
            var streamType = $("#codeSchema").val();
            var storageType = $("#storageType").val();
            oldVehicleId = vehicleId;

            var yearMonth = $('.calendar.calendar3 table caption span').html();
            var date = yearMonth.replace(/[^0-9]/ig, "-");
            var year = date.split('-')[0].substring(2, 4);
            var month = date.split('-')[1];
            if (month.length < 2) {
                month = '0' + month;
            }
            var dateParma = year + month

            if (index === 1) {
                var y = parseInt(date.split('-')[0]);
                var m = parseInt(date.split('-')[1]) - 1;
                var firstDay = new Date(y, m, 1);
                var lastDay = new Date(y, m + 1, 0);

                var searchStartTime = new XDate(firstDay).toString('yyyy-MM-dd');
                var searchEndTime = new XDate(lastDay).toString('yyyy-MM-dd');
                startTime = searchStartTime + " 00:00:00";
                endTime = searchEndTime + " 23:59:59";
            }
            // 终端资源
            if (type == 0) {
                if (index !== 2) {// 高级查询不执行如下操作

                    const requestStr = {
                        "vehicleId": vehicleId,
                        "videoType": videoType,
                        "date": dateParma
                    };
                    webSocket.send("/app/video/history/month", headers, requestStr) //回调函数 订阅函数 videoDateSocketSubscribe
                }
            } else {
                var url = "/clbs/realTimeVideo/resource/getResource";
                var parameter = {
                    "vehicleId": vehicleId,
                    "brand": brand,
                    "alarmType": alarmType,
                    "channlNumer": channlNumer,
                    "startTime": startTime,
                    "endTime": endTime,
                    "type": type,
                    "msgSN": msgSNAck,
                    "videoType": videoType,
                    "streamType": streamType,
                    "storageType": storageType,
                    "ftpName": ftpName,
                    "date": dateParma
                };
                json_ajax_p("POST", url, "json", true, parameter, resourceList.inquiryBack);
            }
            resourceList.getIco();
        },


        /**
         * 切换选项卡
         */
        changeTab: function () {
            if ($("#resourceListActive").hasClass("active")) {
                $("#resourceListActive,#resourceList,#ftpList,#FTPListActive").removeClass("active");
                $("#playListActive,#playList").addClass("active");
                $(".video-back-module,.video-play-module").show();
                $(".video-play-select,.video-resource-module").hide();
            }
            if ($("#FTPListActive").hasClass("active")) {
                $("#resourceListActive,#resourceList,#ftpList,#FTPListActive").removeClass("active");
                $("#playListActive,#playList").addClass("active");
                $(".video-back-module,.video-play-module").show();
                $(".video-play-select,.video-resource-module").hide();
            }
        },

        /**
         * 查询资源列表回调函数，主要涉及FTP查询后数据组装，
         * @author yangyi
         * @param data
         */
        inquiryBack: function (data) {
            var yearMonth = $('.calendar.calendar3 table caption span').html();
            var date = yearMonth.replace(/[^0-9]/ig, "-");
            var year = date.split('-')[0];
            var month = date.split('-')[1];
            //判断回调函数值不为空
            if (data.success) {
                //不是高级查询
                if (!ifadvanced) {
                    $("#advancedQuery").modal('hide');
                    resourceList.changeTab();
                    vehcleBrand = data.obj.brand;
                    deviceNumber = data.obj.deviceNumber;
                    mobile = data.obj.mobile;

                    inquiryType = data.obj.type;

                    if (data.obj.type !== "0") {
                        layer.closeAll();
                        var ftp = data.obj.ftpResource;
                        if (ftp != null) {
                            var calendarSet = ftp.calendarSet;
                            //日期解析
                            resourceList.buildDate(calendarSet, [year, month, 1]);
                            FtpResourcesLists.clear();
                            var ftpresourceListBeans = ftp.resourceList;
                            for (var j = 0; j < ftpresourceListBeans.length; j++) {
                                var alarmType = ftpresourceListBeans[j].alarmType; //将报警类型数字转换成名字
                                var startTime = ftpresourceListBeans[j].startTime;
                                var endTime = ftpresourceListBeans[j].endTime;
                                var name = ftpresourceListBeans[j].name;
                                var url = ftpresourceListBeans[j].url;
                                var fileSize = ftpresourceListBeans[j].fileSize;
                                var channlNumer = ftpresourceListBeans[j].channelNumber;
                                var uploadTime = ftpresourceListBeans[j].uploadTime;
                                var tempUrl = ftpresourceListBeans[j].tempUrl;
                                var downUrl = ftpresourceListBeans[j].downUrl;

                                var timeSplit = startTime.substr(0, 10);
                                var FTPList = [channlNumer, startTime, endTime, alarmType, fileSize, url, name, channlNumer, uploadTime, downUrl, tempUrl];
                                resourceList.publicMapData(timeSplit, FTPList, FtpResourcesLists);
                            }
                        } else {
                            layer.msg("获取资源列表数据失败");
                        }
                    }
                } else {
                    //高级查询
                    $("#advancedQuery").modal('hide');
                    resourceList.changeTab();
                    vehcleBrand = data.obj.brand;
                    deviceNumber = data.obj.deviceNumber;
                    mobile = data.obj.mobile;

                    inquiryType = data.obj.type;

                    if (data.obj.type !== "0") {
                        layer.closeAll();
                        ftp = data.obj.ftpResource;
                        if (ftp != null) {
                            //日期解析
                            ftpresourceListBeans = ftp.resourceList;
                            FtpAdvancedResourcesLists.clear();
                            for (j = 0; j < ftpresourceListBeans.length; j++) {
                                var alarmType = ftpresourceListBeans[j].alarmType; //将报警类型数字转换成名字
                                var startTime = ftpresourceListBeans[j].startTime;
                                var endTime = ftpresourceListBeans[j].endTime;
                                var name = ftpresourceListBeans[j].name;
                                var url = ftpresourceListBeans[j].url;
                                var fileSize = ftpresourceListBeans[j].fileSize;
                                var channlNumer = ftpresourceListBeans[j].channelNumber;
                                var timeSplit = startTime.substr(0, 10);
                                var uploadTime = ftpresourceListBeans[j].uploadTime;
                                var tempUrl = ftpresourceListBeans[j].tempUrl;
                                var downUrl = ftpresourceListBeans[j].downUrl;
                                FTPList = [channlNumer, startTime, endTime, alarmType, fileSize, url, name, channlNumer, uploadTime, downUrl, tempUrl];
                                resourceList.publicMapData(timeSplit, FTPList, FtpAdvancedResourcesLists);
                            }
                            var searchStartTimess = $('#searchStartTime').val();
                            var searchEndTimess = $('#searchEndTime').val();
                            resourceList.oneDayData(searchStartTimess, searchEndTimess);
                        } else {
                            layer.msg("获取资源列表数据失败");
                        }
                    }
                }
            } else {
                layer.closeAll();
                if (data.msg != null && data.msg !== '') {
                    $('.video-loading-name-all').text(ata.msg)
                }
            }
        },


        /**
         * 查询资源列表回调函数，主要涉及FTP查询后数据组装，用于查询终端FTP列表的数据，不进行日期组装
         * @author lkh
         * @param data
         */
        inquiryNewBack: function (data) {
            //判断回调函数值不为空
            if (data.success) {
                //不是高级查询
                if (!ifadvanced) {
                    $("#advancedQuery").modal('hide');
                    vehcleBrand = data.obj.brand;
                    deviceNumber = data.obj.deviceNumber;
                    mobile = data.obj.mobile;
                    inquiryType = data.obj.type;
                    layer.closeAll();
                    var ftp = data.obj.ftpResource;
                    if (ftp != null) {
                        //日期解析
                        FtpResourcesLists.clear();
                        var ftpresourceListBeans = ftp.resourceList;
                        for (var j = 0; j < ftpresourceListBeans.length; j++) {
                            var alarmType = ftpresourceListBeans[j].alarmType; //将报警类型数字转换成名字
                            var startTime = ftpresourceListBeans[j].startTime;
                            var endTime = ftpresourceListBeans[j].endTime;
                            var name = ftpresourceListBeans[j].name;
                            var url = ftpresourceListBeans[j].url;
                            var fileSize = ftpresourceListBeans[j].fileSize;
                            var channlNumer = ftpresourceListBeans[j].channelNumber;
                            var uploadTime = ftpresourceListBeans[j].uploadTime;
                            var downUrl = ftpresourceListBeans[j].downUrl;
                            var tempUrl = ftpresourceListBeans[j].tempUrl;
                            var timeSplit = startTime.substr(0, 10);
                            var FTPList = [channlNumer, startTime, endTime, alarmType, fileSize, url, name, channlNumer, uploadTime, downUrl, tempUrl];
                            resourceList.publicMapData(timeSplit, FTPList, FtpResourcesLists);
                        }

                        var listss = ifadvanced ? FtpAdvancedResourcesLists.get(timeSplit) : FtpResourcesLists.get(timeSplit);
                        resourceList.generateFtpNewList(listss, timeSplit);
                    } else {
                        layer.msg("获取资源列表数据失败");
                    }
                } else {
                    //高级查询
                    $("#advancedQuery").modal('hide');
                    vehcleBrand = data.obj.brand;
                    deviceNumber = data.obj.deviceNumber;
                    mobile = data.obj.mobile;
                    inquiryType = data.obj.type;
                    if (data.obj.type !== "0") {
                        layer.closeAll();
                        ftp = data.obj.ftpResource;
                        if (ftp != null) {
                            ftpresourceListBeans = ftp.resourceList;
                            FtpAdvancedResourcesLists.clear();
                            for (j = 0; j < ftpresourceListBeans.length; j++) {
                                alarmType = ftpresourceListBeans[j].alarmType; //将报警类型数字转换成名字
                                startTime = ftpresourceListBeans[j].startTime;
                                endTime = ftpresourceListBeans[j].endTime;
                                name = ftpresourceListBeans[j].name;
                                url = ftpresourceListBeans[j].url;
                                fileSize = ftpresourceListBeans[j].fileSize;
                                channlNumer = ftpresourceListBeans[j].channelNumber;
                                var uploadTime = ftpresourceListBeans[j].uploadTime;
                                var tempUrl = ftpresourceListBeans[j].tempUrl;
                                var downUrl = ftpresourceListBeans[j].downUrl;
                                timeSplit = startTime.substr(0, 10);
                                FTPList = [channlNumer, startTime, endTime, alarmType, fileSize, url, name, channlNumer, uploadTime, downUrl, tempUrl];
                                resourceList.publicMapData(timeSplit, FTPList, FtpAdvancedResourcesLists);
                            }
                            var listss = ifadvanced ? FtpAdvancedResourcesLists.get(timeSplit) : FtpResourcesLists.get(timeSplit);
                            resourceList.generateFtpNewList(listss, timeSplit);
                        } else {
                            layer.msg("获取资源列表数据失败");
                        }
                    }
                }
            } else {
                layer.closeAll();
                if (data.msg != null && data.msg !== '') {
                    $('.video-loading-name-all').text(ata.msg)
                }
            }
            resourceList.setMainRightLayout();
        },

        /**
         * @author yangyi
         * 查询一天的数据
         */
        oneDayData: function (startTime, endTime, flag) {
            selectSearchType = 1
            if (flag) {
                ifadvanced = false;
            }
            inquiryType = $("#type").val();
            // reset();
            searchDate = startTime.split(' ')[0];
            //执行清空函数  清空相对应集合 数组 dom
            resourceList.pageLoadsEmpty();
            //日历点击查询时 重置为true 进入播放分屏相关
            videoPlayFlag = true;
            deviceQueryType_start = 1;
            var timeSplit = startTime.substr(0, 10);
            $(".heads-date-module").html(timeSplit);
            pluginStopTime = timeSplit; //日历点击年月日 用于轨迹结束点时间组装
            $("#resourceListDataTable tbody").html(""); // 	清空资源列表数据
            $("#resourceListDataTables tbody").html("")
            layer.msg('正在努力加载数据,请耐心等待', {
                icon: 16,
                time: 18000,
                shade: [0.1, true],
                skin: "layui-layer-border layui-layer-hui"
            });
            if (inquiryType != '0') {
                resourceList.getHistoryCoord(timeSplit, startTime, endTime)
            } else {
                getResFlag = true;
                //获取资源列表信息
                resourceList.getResourceData(startTime, endTime);
                startTimeGlobal = startTime;
                endTimeGlobal = endTime;
                resourceList.getHistoryCoord(timeSplit, startTime, endTime) //新加的
            }
        },
        /**
         * @author wanjikun
         * 获取资源列表信息
         */
        getResourceData: function (startTime, endTime) {
            getResFlag = true;
            inquiryType = 0;
            //再次查询之前数据清空
            $('#resourceListDataTable tbody').html('');
            $('#resourceListDataTables tbody').html('');

            if (terminalTimer) clearTimeout(terminalTimer);

            if (vehicleTreeId === "") {
                layer.msg("请选择监控对象");
                return false;
            }
            //以下数据作为当前查询数据条件
            var type = $("#type").val();
            //若选择了开始日期和结束日期，则校验其合法性
            var dateTimeRegex = /(\d+-\d+-\d+)\s(\d+:\d+:\d+)/;
            if (startTime !== "" && endTime !== "") {
                var startCheckTime = startTime.replace(dateTimeRegex, '$1T$2'); //正则替换
                var endCheckTime = endTime.replace(dateTimeRegex, '$1T$2');
                startCheckTime = Date.parse(startCheckTime);
                endCheckTime = Date.parse(endCheckTime);
                if (startCheckTime > endCheckTime) {
                    layer.msg("开始时间应在结束时间之前");
                    return false;
                }
                if (type !== 0) {
                    if ((endCheckTime - startCheckTime) > 2678400000) {
                        layer.msg("查询FTP视频信息时间范围不能大于一个月");
                        return false;
                    }
                } else {
                    terminalTimer = setTimeout(function () {
                        layer.msg('终端无反馈,请重试');
                    }, 60000)
                }
            }
            if (startTime === "") {
                if (type !== 0) {
                    layer.msg("查询FTP资源开始时间不能为空");
                    return false;
                }
                startTime = "0000-00-00 00:00:00";

            }
            if (endTime === "") {
                if (type !== 0) {
                    layer.msg("查询FTP资源结束时间不能为空");
                    return false;
                }
                endTime = "0000-00-00 00:00:00";

            }
            layer.msg('正在努力加载数据,请耐心等待', {
                icon: 16,
                time: 18000,
                shade: [0.1, true],
                skin: "layui-layer-border layui-layer-hui"
            });
            var vehicleId = vehicleTreeId;
            var brand = vehicleTreeName;
            var alarmType = alarmTen;
            if (alarmType === "") {
                alarmType = 0;
            }
            var channelNumber = $("#channelNum").val();
            oldChannlNumer = channelNumber;
            var videoType = $("#videoType").val();
            var streamType = $("#codeSchema").val();
            var storageType = $("#storageType").val();
            oldVehicleId = vehicleId;

            var yearMonth = $('.calendar.calendar3 table caption span').html();
            var date = yearMonth.replace(/[^0-9]/ig, "-");
            var year = date.split('-')[0].substring(2, 4);
            var month = date.split('-')[1];
            if (month.length < 2) {
                month = '0' + month;
            }
            var dateParma = year + month;
            layer.msg('正在努力加载数据,请耐心等待', {
                icon: 16,
                time: 18000,
                shade: [0.1, true],
                skin: "layui-layer-border layui-layer-hui"
            });
            // var url = "/clbs/realTimeVideo/resource/getResourceList";
            var parameter = {
                "vehicleId": vehicleId,
                "brand": brand,
                "alarmType": alarmType,
                "channlNumer": channelNumber,
                "startTime": startTime,
                "endTime": endTime,
                // "type": type,
                "msgSN": msgSNAck,
                "videoType": videoType,
                "streamType": streamType,
                "storageType": storageType,
                // "ftpName": ftpName,
                "date": dateParma,
                "deviceType": currentProtocol
            };
            webSocket.send("/app/video/history/day", headers, parameter)
            subscribeSingleVideo = false;
            $("#advancedQuery").modal('hide');
            resourceList.changeTab();
            // json_ajax_p("POST", url, "json", true, parameter, resourceList.getResourceDataCallBack);
        },
        /**
         * @author wanjikun
         * 获取资源列表信息回调
         */
        getResourceDataCallBack: function () {
            subscribeSingleVideo = false;
            $("#advancedQuery").modal('hide');
            resourceList.changeTab();
        },
        /**
         * @author yangyi
         * 查询历史数据
         */
        getHistoryCoord: function (timeSplit, startTime, endTime) {
            // 组装历史数据
            videoMap.clearMap(); // 清空地图数据
            pluginStopTime = timeSplit; //日历点击年月日 用于轨迹结束点时间组装
            var url = "/clbs/realTimeVideo/resource/getHistory";
            var parameter = {
                "vehicleId": oldVehicleId,
                "startTime": startTime,
                "endTime": endTime
            };
            json_ajax_p("POST", url, "json", true, parameter, function (data) {
                if (data.success) {
                    layer.closeAll();
                    if (data.msg !== null && data.msg !== undefined) {
                        var positionalData = ungzip(data.msg);
                        var positionals = $.parseJSON(positionalData);
                        resourceList.history(positionals);
                    }
                    //查询通道号方法 日历点击时调用
                    resourceList.getVideoChannel(oldVehicleId);
                    // var newChannelDate = []
                    if (oldChannlNumer !== "0") {
                        channelDate = [];
                        channelDate.push(channelDates[oldChannlNumer]);
                    }
                    if (oldType === "0") { //终端
                        deviceQueryType = 1;
                        var list = deviceResourcesLists.get(timeSplit);
                        resourceList.generateDeviceList(list, timeSplit);
                        $('.video-play-select').css('opacity', 1);
                    } else {
                        list = ifadvanced ? FtpAdvancedResourcesLists.get(timeSplit) : FtpResourcesLists.get(timeSplit);
                        resourceList.generateFtpList(list, timeSplit);
                        $('.video-play-select').css('opacity', 0);
                    }
                    //监控对象通道号前端数据及DOM节点添加
                    resourceList.getMonitorObjeChannelNumber(channelDate);
                } else {
                    layer.closeAll();
                    layer.msg("数据查询失败！请重试！");
                }
            });
        },


        /**
         * webSocket数据接收，数据来源于查询终端，获取资源列表
         * @author yangyi
         * @param data
         */
        getLastOilDataCallBack: function (data) {
            var str = $.parseJSON(data.body);
            if (!str.success) {
                layer.msg(str.msg);
                $('.video-loading-name-all').text(str.msg)
                return;
            }
            if (resourceTimeout) clearTimeout(resourceTimeout);
            str = str.obj;
            if (str === undefined || str === null || str === '') {
                resourceTimeout = setTimeout(function () {
                    layer.msg('获取终端视频数据失败,请稍后再试!');
                }, 60000);
                return;
            }
            if (!subscribeSingleVideo) {
                var yearMonth = $('.calendar.calendar3 table caption span').html();
                var date = yearMonth.replace(/[^0-9]/ig, "-");
                var year = date.split('-')[0];
                var month = date.split('-')[1];

                layer.closeAll();
                if (terminalTimer) clearTimeout(terminalTimer);
                var resourcesList = str.data.msgBody.resourcesList;
                var allchannelNum = []
                resourcesList.forEach(it => {
                    allchannelNum.push(it.channelNum)
                })
                channelAllList = [...new Set(allchannelNum)]
                vehcleBrand = str.desc.monitorName;
                deviceNumber = str.desc.deviceNumber;
                mobile = str.data.msgHead.mobile;
                inquiryType = 0;
                var dateList = [];
                deviceResourcesLists.clear();
                for (var i = 0; i < resourcesList.length; i++) {
                    // 日历组装
                    var startTime = resourcesList[i].startTime;
                    var endTime = resourcesList[i].endTime;
                    var stime = startTime.substring(0, 6);
                    // var etime = endTime.substring(0, 6);
                    // 资源列表组装
                    var channelNum = resourcesList[i].channelNum;
                    var fileSize = resourcesList[i].fileSize;
                    var alarm = resourcesList[i].alarm;
                    var videoType = resourcesList[i].videoType;
                    var streamType = resourcesList[i].streamType;
                    var storageType = resourcesList[i].storageType;
                    //时间解析
                    startTime = resourceList.dateFormat("20" + startTime);
                    endTime = resourceList.dateFormat("20" + endTime);
                    // var physicsChannel = resourcesList[i].channelNum;//物理通道号
                    if (resourceList.filterAlarmType(alarm)) {
                        dateList.push(stime);
                        var timeSplit = startTime.substr(0, 10);
                        var FTPList = [channelNum, startTime, endTime, alarm, fileSize, videoType, streamType, storageType, resourcesList[i].startTime, resourcesList[i].endTime, channelNum];
                        resourceList.publicMapData(timeSplit, FTPList, deviceResourcesLists);

                    }

                }

                if (inquiryType != '0') {
                    var dateArray = resourceList.unique(dateList);
                    if (deviceQueryType_start == 0) {
                        resourceList.buildDate(dateArray, [year, month, 1]);
                    }
                } else if (getResFlag) {
                    //wjk
                    var timeSplitParma = startTimeGlobal.substr(0, 10);
                    resourceList.getHistoryCoord(timeSplitParma, startTimeGlobal, endTimeGlobal)
                }

            }
        },

        /**
         * @author yangyi
         * 组装ftp的资源列表
         */
        generateFtpList: function (data, thisDate) {
            ///查询FTP数据，获取资源列表数据组装
            var str = "";
            videoPlayLists.clear();
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    var channelNumer = parseInt(data[i][0]);
                    var physicsChannel = parseInt(data[i][7]);
                    var stime = data[i][1];
                    var etime = data[i][2];
                    var startTime = resourceList.timestamp(stime);
                    var endTime = resourceList.timestamp(etime);
                    var alarmName = data[i][3]; //将报警类型数字转换成名字
                    alarmName = resourceList.parsingAlarmType(alarmName);
                    var alarmSplit = resourceList.alarmNameSplit(alarmName);
                    if (alarmName !== "" && alarmName.length > 20) {
                        alarmName = alarmName.substring(0, 20) + "...";
                    }
                    alarmName = (alarmName.substring(alarmName.length - 1) === ',') ? alarmName.substring(0, alarmName.length - 1) : alarmName;
                    var fileSize = data[i][4];

                    var resourceName = data[i][5];
                    var fileName = data[i][6];

                    var uploadTime = data[i][8] ? data[i][8] : '-';

                    var tempUrl = data[i][10];
                    var mpfourUrl = data[i][9];

                    //FTP视频资源集合组装
                    var mpfourUrlBtm = '<button href="' + mpfourUrl + '"  type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileHrefDownload(\'' + mpfourUrl + '\')">MP4文件下载</button> ';
                    if (!mpfourUrl) {
                        mpfourUrlBtm = '<button href=""  type="button" class="btn btn-primary fileUpload" disabled>MP4文件下载</button> ';
                    }
                    var tempUrlBtm = '<button href="' + tempUrl + '"  type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileHrefDownload(\'' + tempUrl + '\')">源文件下载</button> ';
                    if (!tempUrl) {
                        tempUrlBtm = '<button disabled  type="button" class="btn btn-primary fileUpload">源文件下载</button> ';
                    }
                    var videoNumber = [startTime, endTime, resourceName, channelNumer];
                    resourceList.publicMapData(physicsChannel, videoNumber, videoPlayLists, channelNumer);
                    str += ' <tr data-num="' + channelNumer + '" data-fileName="' + resourceName + '" data-ftpName="' + oldType + '">' +
                        '<td>' + (i + 1) + '</td>' +
                        '<td>' + tempUrlBtm + mpfourUrlBtm + '</td>' +
                        '<td>-</td>' +
                        '<td>' + uploadTime + '</td>' +
                        '<td>' + vehcleBrand + '</td>' +
                        '<td>' + deviceNumber + '</td>' +
                        '<td>' + mobile + '</td>' +
                        '<td>' + channelNumer + '</td>' +
                        '<td>' + stime + '</td>' +
                        '<td>' + etime + '</td>' +
                        '<td class="demo demoUp" data-alarm="' + alarmSplit + '" >' + alarmName + '</td>' +
                        '<td>' + (fileSize || "-") + '</td>' +
                        '<td>-</td>' +
                        '<td>子码流</td>' +
                        '<td>-</td>' +
                        '</tr>';
                }
            }
            var tableData = '<table width="100%" cellspacing="0"  class="table table-striped table-bordered table-hover" id="resourceListDataTable">' +
                '<thead> <tr><th width="50px">序号</th>' +
                '<th>操作设置</th>' +
                '<th>状态</th>' +
                '<th>上传时间</th>' +
                '<th>监控对象</th>' +
                '<th>终端号</th>' +
                '<th>终端手机号</th>' +
                '<th>通道号</th>' +
                '<th>开始时间</th>' +
                '<th>结束时间</th>' +
                '<th>报警类型</th>' +
                '<th>文件大小(M)</th>' +
                '<th>资源类型</th>' +
                '<th>码流类型</th>' +
                '<th>存储器类型</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody></tbody></table >'
            $('#resourceList').html(tableData);
            var $resourceListDataTableTbody = $("#resourceListDataTable tbody");
            $resourceListDataTableTbody.append(str);
            setTimeout(function () {
                $(".demoUp").mouseover(function () {
                    var _this = $(this);
                    if (_this.attr("data-alarm") != "undefined") {
                        _this.justToolsTip({
                            animation: "moveInTop",
                            width: "auto",
                            contents: _this.attr("data-alarm"),
                            gravity: 'top'
                        });
                    }
                })
            }, 1000);
            var resourceListRow = $("#resourceListDataTable tbody tr");
            resourceListRow.click(function (e) {
                resourceList.onclickVideo($(this)[0]);
            });
            resourceListRow.dblclick(function (e) {
                resourceList.ondblclickVideo($(this)[0]);
            });
            resourceList.videoStatusInit(channelDate, videoPlayLists, thisDate);
        },
        /**
         * @author lkh
         * 组装终端资源ftp的资源列表
         */
        generateFtpNewList: function (data, thisDate) {
            ///查询FTP数据，获取资源列表数据组装
            var str = "";
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    var channelNumer = parseInt(data[i][0]);
                    var physicsChannel = parseInt(data[i][7]);
                    var stime = data[i][1];
                    var etime = data[i][2];
                    var startTime = resourceList.timestamp(stime);
                    var endTime = resourceList.timestamp(etime);
                    var alarmName = data[i][3]; //将报警类型数字转换成名字
                    alarmName = resourceList.parsingAlarmType(alarmName);
                    var alarmSplit = resourceList.alarmNameSplit(alarmName);
                    if (alarmName !== "" && alarmName.length > 20) {
                        alarmName = alarmName.substring(0, 20) + "...";
                    }
                    alarmName = (alarmName.substring(alarmName.length - 1) === ',') ? alarmName.substring(0, alarmName.length - 1) : alarmName;
                    var fileSize = data[i][4];
                    var resourceName = data[i][5];
                    var fileName = data[i][6];

                    var uploadTime = data[i][8] ? data[i][8] : '-';

                    var tempUrl = data[i][10];
                    var mpfourUrl = data[i][9];
                    //FTP视频资源集合组装
                    var videoNumber = [startTime, endTime, resourceName, channelNumer];
                    resourceList.publicMapData(physicsChannel, videoNumber, videoPlayLists, channelNumer);

                    var mpfourUrlBtm = '<button href="' + mpfourUrl + '"  type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileHrefDownload(\'' + mpfourUrl + '\')">MP4文件下载</button> ';
                    if (!mpfourUrl) {
                        mpfourUrlBtm = '<button disabled type="button" class="btn btn-primary fileUpload">MP4文件下载</button> ';
                    }
                    var tempUrlBtm = '<button href="' + tempUrl + '"  type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileHrefDownload(\'' + tempUrl + '\')">源文件下载</button> ';
                    if (!tempUrl) {
                        tempUrlBtm = '<button disabled  type="button" class="btn btn-primary fileUpload">源文件下载</button> ';
                    }

                    str += ' <tr data-num="' + channelNumer + '" data-fileName="' + resourceName + '" data-ftpName="' + 'FTP服务器' + '">' +
                        '<td>' + (i + 1) + '</td>' +
                        '<td>' + tempUrlBtm + mpfourUrlBtm + '</td>' +
                        '<td>-</td>' +
                        '<td>' + uploadTime + '</td>' +
                        '<td>' + vehcleBrand + '</td>' +
                        '<td>' + deviceNumber + '</td>' +
                        '<td>' + mobile + '</td>' +
                        '<td>' + channelNumer + '</td>' +
                        '<td>' + stime + '</td>' +
                        '<td>' + etime + '</td>' +
                        '<td class="demo demoUp" data-alarm="' + alarmSplit + '" >' + alarmName + '</td>' +
                        '<td>' + fileSize + '</td>' +
                        '<td>-</td>' +
                        '<td>子码流</td>' +
                        '<td>-</td>' +
                        '</tr>';
                }
            }
            var tableData = '<table width="100%" cellspacing="0"  class="table table-striped table-bordered table-hover" id="resourceListDataTables">' +
                '<thead> <tr><th width="50px">序号</th>' +
                '<th>操作设置</th>' +
                '<th>状态</th>' +
                '<th>上传时间</th>' +
                '<th>监控对象</th>' +
                '<th>终端号</th>' +
                '<th>终端手机号</th>' +
                '<th>通道号</th>' +
                '<th>开始时间</th>' +
                '<th>结束时间</th>' +
                '<th>报警类型</th>' +
                '<th>文件大小(M)</th>' +
                '<th>资源类型</th>' +
                '<th>码流类型</th>' +
                '<th>存储器类型</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody></tbody></table >'
            $('#ftpList').html(tableData);
            var $resourceListDataTableTbody = $("#resourceListDataTables tbody");
            $resourceListDataTableTbody.append(str);
            setTimeout(function () {
                $(".demoUp").mouseover(function () {
                    var _this = $(this);
                    if (_this.attr("data-alarm") != "undefined") {
                        _this.justToolsTip({
                            animation: "moveInTop",
                            width: "auto",
                            contents: _this.attr("data-alarm"),
                            gravity: 'top'
                        });
                    }
                })
            }, 1000);
            var resourceListRow = $("#resourceListDataTables tbody tr");
            resourceListRow.click(function (e) {
                resourceList.onclickVideo($(this)[0]);
            });
            resourceListRow.dblclick(function (e) {
                resourceList.ondblclickFTPVideo($(this)[0]);
            });
            // resourceList.videoStatusInit(channelDate, videoPlayLists, thisDate);
        },
        /**
         * 点击日历查询终端数据
         * @author yangyi
         * @param data
         * @param thisDate
         */
        generateDeviceList: function (data, thisDate) {
            videoPlayDeviceLists.clear();
            var resourcesTableList = []
            if (data) {
                for (let i = 0; i < data.length; i++) {
                    var channelNum = data[i][0];
                    var physicsChannel = data[i][10];
                    var startTime = data[i][1];
                    var endTime = data[i][2];
                    var alarm = data[i][3]; //将报警类型数字转换成名字
                    var alarmName = resourceList.parsingAlarmType(alarm);
                    var alarmSplit = resourceList.alarmNameSplit(alarmName);
                    if (alarmName !== "" && alarmName.length > 20) {
                        alarmName = alarmName.substring(0, 20) + "...";
                    }
                    alarmName = (alarmName.substring(alarmName.length - 1) === ',') ? alarmName.substring(0, alarmName.length - 1) : alarmName;
                    var fileSize = data[i][4];
                    //报警类型解析
                    var codeVideoType = data[i][5];
                    var codeStreamType = data[i][6];
                    var codeStorageType = data[i][7];
                    // 视频播放时间段
                    var videoStartTime = data[i][8];
                    var videoEndTime = data[i][9];
                    var videoType = resourceList.videoType(codeVideoType) || '-';
                    var streamType = resourceList.streamType(codeStreamType) || '-';
                    var storageType = resourceList.storageType(codeStorageType) || '-';
                    var startTimeMap = resourceList.timestamp(startTime) || '-';
                    var endTimeMap = resourceList.timestamp(endTime) || '-';
                    // 终端播放数据集合
                    var videoNumer = [startTimeMap, endTimeMap, codeVideoType, codeStreamType, codeStorageType, false, channelNum];
                    resourceList.publicMapData(physicsChannel, videoNumer, videoPlayDeviceLists, channelNum);
                    //音视频，码流，存储解析
                    fileSize = (fileSize / (1024 * 1024)).toFixed(1);
                    //文件上传所需要的参数组装
                    var state = "state" + (i + 1);
                    var stop = "stop" + (i + 1);
                    var cancel = "cancel" + (i + 1);
                    var upload = "upload" + (i + 1);
                    var resourceListStr = "" + vehcleBrand + "," + oldVehicleId + "," + channelNum + "," + videoStartTime + "," + videoEndTime + "," + alarm + "," + codeVideoType + "," + codeStreamType + "," + codeStorageType + "," + state + "," + stop + "," + cancel + "," + data[i][4] + "," + upload + "";
                    // //终端查询后的资源列表组装
                    resourcesTableList.push({
                        0: i + 1,
                        1: resourceListStr,
                        2: state,
                        3: vehcleBrand,
                        4: deviceNumber,
                        5: mobile,
                        6: channelNum,
                        7: startTime,
                        8: endTime,
                        9: alarmName,
                        10: fileSize,
                        11: videoType,
                        12: streamType,
                        13: storageType,
                        14: videoStartTime,
                        15: videoEndTime,
                        16: alarmSplit
                    })
                }
            }
            var defaultDate = null;
            if (data) {
                if (data.length > 0) {
                    defaultDate = data[0][1];
                }
            } else if (thisDate) {
                defaultDate = thisDate;
            }

            $('#resourceList').html('<table width="100%" cellspacing="0"  class="table table-striped table-bordered table-hover" id="resourceListDataTable"></table>');
            $('#resourceListDataTable').dataTable({
                "data": resourcesTableList,
                "paging": false,
                searching: false,
                "language": {
                    "zeroRecords": "暂无数据"
                },
                "columns": [{
                    "title": "序号",
                    orderable: false
                },
                    {
                        "title": "操作设置",
                        "class": "center",
                        orderable: false,
                        render: function (data, type, row, meta) {
                            // var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                            var result = '';

                            if (currentProtocol === '23') {
                                result = '<button disabled href="/clbs/realTimeVideo/resource/upload?resourceListStr=' + data + '" id="' + "upload" + row['0'] + '" type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileUpload(\'' + data + '\')">文件上传</button> ';
                            } else {
                                result += '<button style="margin-right: 10px;" href="/clbs/realTimeVideo/resource/upload?resourceListStr=' + data + '" id="' + "upload" + row['0'] + '" type="button" class="btn btn-primary fileUpload" onclick="resourceList.fileUpload(\'' + data + '\')">文件上传</button>';
                            }
                            result += '<button style="margin-right: 10px;" type="button" class="btn btn-primary fileStop" id="' + "stop" + row['0'] + '"  onclick="resourceList.status(this,0,' + (row['0']) + ')" disabled >暂停</button>';

                            result += '<button type="button" class="btn btn-primary fileCancel" id="' + "cancel" + row['0'] + '"   onclick="resourceList.status(this,1,' + (row['0']) + ')" disabled >取消</button>';

                            return result;
                        }
                    },
                    {
                        "title": "状态",
                        orderable: false,
                        render: function (data, type, row, meta) {
                            var result = '';
                            result += `<span id=${row['2']}></span>`;
                            return result;
                        }
                    },
                    {
                        "title": "监控对象",
                        orderable: false,

                    },
                    {
                        "title": "终端号",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "终端手机号",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "通道号",
                        "class": "center"
                    },
                    {
                        "title": "开始时间",
                        "class": "center videoStartTime",
                        render: function (data, type, row, meta) {
                            // var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                            var result = '';
                            result += `<span videoStartTime=${row['14']}>${row['7']}</span>`;
                            return result;
                        }
                    },
                    {
                        "title": "结束时间",
                        "class": "center videoEndTime",
                        render: function (data, type, row, meta) {
                            // var editUrlPath = myTable.editUrl + row.id + ".gsp"; //修改地址
                            var result = '';
                            result += `<span videoEndTime=${row['15']}>${row['8']}</span>`;
                            return result;
                        }
                    },
                    {
                        "title": "报警类型",
                        "class": "center",
                        orderable: false,
                        render: function (data, type, row, meta) {
                            var result = '';
                            result += `<span class="demo demoUp" >${data}</span>`;
                            return result;
                        }
                    },
                    {
                        "title": "文件大小(M)",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "资源类型",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "码流类型",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "存储器类型",
                        "class": "center",
                        orderable: false,
                    },
                    {
                        "title": "录像开始时间",
                        "class": "center",
                        orderable: false,
                        visible: false,
                    },
                    {
                        "title": "录像结束时间时间",
                        "class": "center",
                        orderable: false,
                        visible: false,
                    }
                ]
            });
            $('#resourceListDataTable tbody').on('click', 'tr', function () {
                resourceList.resourceSelected($(this)[0]);
            });
            $('#resourceListDataTable tbody').on('dblclick', 'tr', function () {
                resourceList.ondblclickDevice($(this)[0]);
            });
            resourceList.videoStatusInit(channelDate, videoPlayDeviceLists, defaultDate);
        },

        fillResourceList: function (data) {
            $("#resourceListDataTable tbody").html(data);
            setTimeout(function () {
                $(".demoUp").mouseover(function () {
                    var _this = $(this);
                    if (_this.attr("data-alarm") !== "undefined") {
                        _this.justToolsTip({
                            animation: "moveInTop",
                            width: "auto",
                            contents: _this.attr("data-alarm"),
                            gravity: 'top'
                        });
                    }
                })
            }, 1000);
        },

        /**
         * 播放控制插件初始化
         */
        videoStatusInit: function (cNumberData, videoPlayDataList, defaultDate) {
            if (oldType === '0') {
                $('#resourceModule').addClass('slider-module');
                $('#playModule').addClass('slider-module');
            } else {
                $('#resourceModule').removeClass('slider-module');
                $('#playModule').removeClass('slider-module');
            }
            $('#resourceListVideoFrame').css("cursor", "pointer");
            $("#resourceListVideoFrame").on("click", resourceList.resourceVideoFrameFn); //资源列表-视频单帧播放

            var containers = $('#containers');
            containers.show();
            videoPluginSelect = false;
            if (videoControlPlugin) {
                videoControlPlugin.stop();
            }
            videoControlPlugin = null;
            var channelData = resourceList.returnVideoList(cNumberData, videoPlayDataList);

            videoControlPlugin = containers.videoStatus({
                channels: cNumberData,
                channelData: channelData,
                // 拖拽开始
                onDragStart: function () {
                    videoDragState = true;
                },
                defaultDate: Object.keys(channelData).length === 0 && defaultDate ? new Date(defaultDate) : undefined,
                // 拖拽结束
                onDragEnd: function ($this, state) {

                    videoPluginSelect = true;
                    videoDragState = false;
                    resourceList.videoPlayJump(state);
                    // resourceList.getDragVideoTime(state, videoPlayDataList);
                },
                // 持续播放
                onPlaying: function ($this, state) {
                    if (!videoDragState) {
                        resourceList.isNextClip(state, videoPlayDataList);
                    }
                    var playListVideoStop = $("#playListVideoStop");
                    if (playListVideoStop.css('cursor') === 'not-allowed') {
                        playListVideoStop.css("cursor", "pointer").off("click").on("click", resourceList.videoStopFn); //播放列表-视频停止
                    }
                },
                // 点击选中时间点后触发事件
                onStatusClick: function ($this, state) {
                    videoPluginSelect = true;
                    resourceList.videoPlayJump(state);
                    // resourceList.getDragVideoTime(state, videoPlayDataList);
                }
            });
            $('#scalingBtn').removeClass('fa-chevron-up').addClass('fa-chevron-down');
        },
        /**
         * 视频进度跳转
         */
        videoPlayJump: function (state) {
            var time = $('#thisDateOf').text() + ' ' + state.currentHour + ':' + state.currentMinute + ':' + state.currentSecond;
            var sendTime = resourceList.dateToyyMMddHHmmss(new Date(time.replace(/-/g, '/')));
            mseVideoLists.values().map(function (item) {
                item.cmdSeek(sendTime);
            })
        },
        /**
         * 播放控制插件拖拽结束后，组装对应视频段集合
         */
        getDragVideoTime: function (state, videoPlayDataList) {
            videoControlPlugin.pause();
            // 获取所有通道号
            var $checkbox = $('.channel-checkbox');
            var channelArray = [];
            for (var i = 0; i < $checkbox.length; i++) {
                channelArray.push($($checkbox[i]).data('channel'));
            }

            var vehicleIdString = ''; // 监控对象id
            var channelString = ''; // 通道号
            for (i = 0; i < channelArray.length; i++) {
                vehicleIdString += oldVehicleId + ',';
                channelString += channelArray[i] + ',';
            }
            vehicleIdString = vehicleIdString.substring(0, vehicleIdString.length - 1);
            channelString = channelString.substring(0, channelString.length - 1);
            // 数据组装
            let time = $('#thisDateOf').text() + ' ' + state.currentHour + ':' + state.currentMinute + ':' + state.currentSecond;
            resourceList.sendStopInstruct(vehicleIdString, channelString, 'drag',
                resourceList.dateToyyMMddHHmmss(new Date(time.replace(/-/g, '/'))));
        },

        /**
         * 返回拖拽结束后的视频集合
         */
        returnDragVideoTimeList: function (ms, videoPlayDataList) {
            var list = [];
            for (var i = 0; i < channelDate.length; i++) {
                if (_checkChannelList.get('subChk_' + channelDate[i]) === 'true') {
                    var values = videoPlayDataList.get(channelDate[i]);
                    if (values !== undefined) {
                        for (var j = 0; j < values.length; j++) {
                            // 判断拖拽点时间处于哪个视频片段
                            if (ms >= values[j][0] && ms <= values[j][1]) {
                                if (oldType === '0') {
                                    list.push([
                                        ms,
                                        values[j][1],
                                        values[j][values[j].length - 1],
                                        channelDate[i]
                                    ]);
                                } else {
                                    list.push([
                                        ms,
                                        values[j][1],
                                        values[j][values[j].length - 1],
                                        channelDate[i]
                                    ]);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            return list;
        },

        /**
         * 判断是否还在当前视频片段进行播放
         */
        isNextClip: function (state, videoPlayDataList) {
            var time = $('#thisDateOf').text() + ' ' + state.currentHour + ':' + state.currentMinute + ':' + state.currentSecond;
            var date = new Date(time.replace(/-/g, '/'));
            var ms = (date.getTime() / 1000) + 1;
            channelNumVideoList = [];
            var playChannum = [];

            for (var i = 0; i < channelDate.length; i++) {
                if (_checkChannelList.get('subChk_' + channelDate[i]) === 'true') {
                    var values = videoPlayDataList.get(channelDate[i]);
                    if (values !== undefined) {
                        for (var j = 0; j < values.length; j++) {
                            if (ms >= values[j][0] && ms <= values[j][1]) {
                                var isHasCNumber = false;
                                for (var s = 0; s < pluginSendData.length; s++) {
                                    if (pluginSendData[s][3] === (channelDate[i])) {
                                        if (ms > pluginSendData[s][1]) {
                                            var list = [
                                                values[j][0],
                                                values[j][1],
                                                values[j][values[j].length - 1],
                                                channelDate[i]
                                            ];
                                            channelNumVideoList.push(list);
                                            playChannum.push(list[2]);
                                            pluginSendData[s] = list;
                                        }
                                        isHasCNumber = true;
                                    }
                                }
                                if (!isHasCNumber) {
                                    list = [
                                        values[j][0],
                                        values[j][1],
                                        values[j][values[j].length - 1],
                                        channelDate[i]
                                    ];
                                    channelNumVideoList.push(list);
                                    playChannum.push(list[2]);
                                    pluginSendData.push(list);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (channelNumVideoList.length == channelDate) {
                videoControlPlugin.pause();
            }
            if (channelNumVideoList.length > 0) {
                resourceList.sendStopInstruct(oldVehicleId, playChannum.join(','), 'resourceChange')
            }
        },

        /**
         * 返回初始化特定结构的视频list json对象
         */
        returnVideoList: function (cNumberData, videoPlayDataList) {
            var list = {};
            for (var i = 0; i < cNumberData.length; i++) {
                var values = videoPlayDataList.get(cNumberData[i]);
                var videoList = [];
                if (values !== undefined) {
                    for (var j = 0; j < values.length; j++) {
                        videoList.push({
                            'ID': j + 1,
                            'DisplayText': '',
                            'StationId': cNumberData[i],
                            'StartTime': resourceList.getTimeFormat(values[j][0]),
                            'EndTime': resourceList.getTimeFormat(values[j][1])
                        });
                    }
                    list[cNumberData[i]] = videoList;
                }
            }
            return list;
        },

        /**
         * 解析报警类型
         * @author yangyi
         */
        parsingAlarmType: function (data) {
            if (data !== 0) {
                var alarmType = []
                var j = 0;
                var alarmTypeTwo = data.toString();
                for (var i = alarmTypeTwo.length - 1; i >= 0; i--) {
                    if (alarmTypeTwo[i] !== 0) {
                        alarmType.push(j)
                    }
                    j++
                }
                var str = ""
                for (var k = 0; k < alarmType.length; k++) {
                    str += alarmTypeLists.elements.filter(it => it.key == alarmType[k])[0].value + ",";
                }
            } else {
                str = "";
            }
            return str;
        },

        /**
         * 文件上传
         * @author yangyi
         */
        fileUpload: function (uploadData) {
            $("#uploadVideoModal").modal("show");
            var str = uploadData.split(",");
            $("#upload_brand").text(str[0]);
            $("#upload_vehicleId").val(str[1]);
            $("#upload_channelNumbe").val(str[2]);
            $("#upload_startTime").val(str[3]);
            $("#upload_endTime").val(str[4]);
            $("#upload_alarm").val(str[5]);
            $("#upload_resourceType").val(str[6]);
            $("#upload_streamType").val(str[7]);
            $("#upload_storageAddress").val(str[8]);
            $("#state").val(str[9]);
            $("#stop").val(str[10]);
            $("#cancel").val(str[11]);
            $("#filesize").val(str[12]);
            $("#uploadId").val(str[13]);
            // $("#stop" + str[1]).removeAttr("disabled");
            // $("#cancel" + str[1]).removeAttr("disabled");
            var url = '/clbs/realTimeVideo/resource/getFtpMsg';
            var data = {
                "vehicleId": str[1],
                "startTime": str[3],
                "channelNumber": str[2],
                "alarm": str[5]
            }
            json_ajax("POST", url, "json", false, data, function (data) {
                if (data.success) {
                    var fileUrl = data.obj.fileUrl;
                    var ftpBean = data.obj.ftpBean;
                    var host = ftpBean.host;
                    var port = ftpBean.port;
                    var username = ftpBean.username;
                    var password = ftpBean.password;
                    $("#FTPServerIp").val(host);
                    $("#FTPort").val(port);
                    $("#FTPUserName").val(username);
                    $("#FTPassword").val(password);
                    $("#FTPpwd").val(password);
                    $("#fileUploadPath").val(fileUrl);
                    // $("#stop" + str[1]).attr("disabled", "disabled");
                    // $("#cancel" + str[1]).attr("disabled", "disabled");
                }
            });
            event.stopPropagation();
        },

        /**
         * 文件上传提交
         * @author yangyi
         */
        submit: function () {
            if (resourceList.validates()) {
                var wifi;
                var lan;
                var threeOrFourG;
                var performConditions = false; // 执行条件勾选
                if ($("#wifi").is(":checked")) {
                    performConditions = true;
                    wifi = 1;
                } else {
                    wifi = 0;
                }
                if ($("#lan").is(":checked")) {
                    performConditions = true;
                    lan = 1;
                } else {
                    lan = 0;
                }
                if ($("#threeOrFourG").is(":checked")) {
                    performConditions = true;
                    threeOrFourG = 1;
                } else {
                    threeOrFourG = 0;
                }

                // 判断是否有执行条件勾选
                if (performConditions) {
                    if (wifi == 0 && lan == 0 && threeOrFourG == 0) {
                        $("#executeError").removeClass("hidden");
                        return false;
                    }
                    var str = threeOrFourG.toString() + lan.toString() + wifi.toString();
                    var executeOn = parseInt(str, 2);
                    $("#executeOn").val(executeOn);
                    $("#executeError").addClass("hidden");

                    var state = $("#state").val();
                    var stop = $("#stop").val();
                    var uploadId = $("#uploadId").val();
                    var cancel = $("#cancel").val();
                    var fileIdList = [state, stop, cancel, uploadId];
                    var pwd = $("#FTPpwd").val();
                    //使用base64加密
                    pwd = resourceList.encode(pwd);
                    $("#FTPassword").val(pwd);
                    $("#uploadVideoModal").modal("hide");
                    $("#uploadFrom").ajaxSubmit(function (data) {
                        var str = $.parseJSON(data);
                        if (str.success) {
                            var uploadState = $("#" + state);
                            uploadState.text("上传中");
                            var msgId = str.obj.msgId;
                            var userName = str.obj.userName;
                            resourceList.fileUploadWebSoket(msgId);
                            fileUploadList.put(msgId, fileIdList);
                            uploadState.attr("data-msgId", msgId);
                            uploadState.attr("data-userName", userName);
                            $("#" + stop).removeAttr("disabled");
                            $("#" + cancel).removeAttr("disabled");
                            $("#" + uploadId).attr("disabled", "disabled");
                        } else {
                            layer.msg("文件上传下发指令失败");
                        }
                    });
                } else {
                    layer.msg('请至少勾选一项');
                }
            }

        },

        //校验
        validates: function () {
            return $("#uploadFrom").validate({
                rules: {
                    FTPServerIp: {
                        required: true,
                        isContainsNumberAndPoint: '#FTPServerIp',
                        maxlength: 15,
                        minlength: 7
                    },
                    FTPort: {
                        required: true,
                        number: true,
                        max: 65535,
                        min: 1
                    },
                    FTPUserName: {
                        required: true,
                        maxlength: 20,
                        minlength: 1

                    },
                    FTPassword: {
                        required: true,
                        maxlength: 20,
                        minlength: 1

                    }
                },
                messages: {
                    FTPServerIp: {
                        required: publicNull,
                        maxlength: publicSize15,
                        minlength: publicSize7,
                        isContainsNumberAndPoint: '只能输入数字和.'
                    },
                    FTPort: {
                        required: publicNull,
                        number: publicNumberFloat,
                        max: videoResourceListPortLength,
                        min: publicMinSize1Length
                    },
                    FTPUserName: {
                        required: publicNull,
                        maxlength: publicSize20,
                        minlength: publicMinSize1Length
                    },
                    FTPassword: {
                        required: publicNull,
                        maxlength: publicSize20,
                        minlength: publicMinSize1Length
                    }
                }
            }).form();
        },

        /**
         * @author
         * 文件上传websocket
         * @param msgSNAck
         */
        fileUploadWebSoket: function (msgSNAck) {
            var resourceLists = {
                "desc": {
                    "MsgId": 4614,
                    "UserName": $("#userName").text(),
                    "cmsgSN": msgSNAck
                },
                "data": param
            };
            webSocket.subscribe(headers, "/user/topic/fileUploadStatus", resourceList.fileUploadWebSoketCallBack, "/app/vehicle/resourceList", resourceLists);
        },

        /**
         *
         * 文件上传回调
         * @author yangyi
         * @param data
         */
        fileUploadWebSoketCallBack: function (data) {
            var str = $.parseJSON(data.body);
            var result = str.data.msgBody.result;
            var msgSNACK = str.data.msgBody.msgSNACK;
            var fileIdList = fileUploadList.get(msgSNACK);
            if (fileIdList != null) {
                var state = fileIdList[0];
                var stop = fileIdList[1];
                var cancel = fileIdList[2];
                var uploadId = fileIdList[3];
                if (result == 1) {
                    $("#" + state).text("上传失败")
                } else {
                    $("#" + state).text("上传成功")
                }
                var stopBtn = $("#" + stop);
                stopBtn.text("暂停");
                stopBtn.attr("disabled", "disabled");
                $("#" + cancel).attr("disabled", "disabled");
                $("#" + uploadId).removeAttr("disabled");
            } else {
                $(".td_state").text("");
                var fileStop = $(".fileStop");
                fileStop.text("暂停");
                fileStop.attr("disabled", "disabled");
                $(".fileCancel").attr("disabled", "disabled");
                $(".fileUpload").removeAttr("disabled");
            }
        },

        /**
         * 文件上传状态控制
         * @author yangyi
         */
        status: function (data, type, status) {
            var str = $(data).text();
            var $state = $("#state" + status);
            var $stop = $("#stop" + status);
            var msgId = $state.attr("data-msgId");
            if (type == 1) { //文件上传取消
                $state.text("");
                $stop.text("暂停");
                $stop.attr("disabled", "disabled");
                $("#cancel" + status).attr("disabled", "disabled");
                $("#upload" + status).removeAttr("disabled");
                resourceList.fileController(msgId, 2);
            } else if (type == 0) {
                if (str === "暂停") { //文件上传暂停
                    $state.text("已暂停");
                    $stop.text("继续");
                    resourceList.fileController(msgId, 0);
                } else { //文件上传继续
                    $state.text("上传中");
                    $stop.text("暂停");
                    resourceList.fileController(msgId, 1);
                }
            }
            event.stopPropagation();
        },

        /**
         * 文件上传控制下发
         * @author yangyi
         * @param data
         * @param type
         */
        fileController: function (data, type) {
            var url = "/clbs/realTimeVideo/resource/control";
            var parameter = {
                "msgSn": data,
                "control": type,
                "vehicleId": oldVehicleId
            };
            json_ajax("POST", url, "json", false, parameter, null);
        },

        /**
         * 音视频类型转换
         * @author yangyi
         * @param data
         * @returns {*}
         */
        videoType: function (data) {
            switch (data) {
                case 0:
                    return "音视频"
                case 1:
                case "1":
                    return "音频"
                case 2:
                case "2":
                    return "视频"
                case 3:
                    return "音频或视频"
                case "0":
                    return "音视频混传"
            }
        },

        /**
         * 码流类型转换
         * @author yangyi
         * @param data
         * @returns {*}
         */
        streamType: function (data) {
            switch (data) {
                case 0:
                    return "所有码流"
                case 1:
                    return "主码流"
                case 2:
                    return "子码流"

            }
        },

        /**
         * 存储类型转换
         * @author yangyi
         * @param data
         * @returns {*}
         */
        storageType: function (data) {
            switch (data) {
                case 0:
                    return "所有存储器"
                case 1:
                    return "主存储器"
                case 2:
                    return "灾备存储器"
            }
        },

        /**
         * 组装日历数据，显示含有数据时间段的日期，
         * @author yangyi
         * @param data
         * @param initYMD
         */
        buildDate: function (data, initYMD, value) {
            var date = [];
            for (var i = 0; i < data.length; i++) {
                date.push(["20" + data[i], "20" + data[i]]);
            }
            var calendar3 = $('.calendar3');
            calendar3.html("");
            calendar3.calendar({
                highlightRange: date,
                changeYMcallback: resourceList.changeYMcallback,
                initYMD: initYMD
            });
            if (!value) {
                if (data.length > 0) {
                    layer.msg('获取资源成功，请选择日期')
                } else {
                    layer.msg('重试或请用高级查询')
                }
            } else {
                layer.msg('请选择监控对象', {
                    time: -1
                })
            }
            resourceList.videoIsFlag = false
            $('.calendar3 tbody td').each(function () {
                if ($(this).hasClass("widget-disabled")) {
                    $(this).removeClass("widget-videoHlight").removeClass("widget-stopHighlight");
                    $(this).children("span").children("span.mileageList").text("-");
                }
            })

            // layer.msg('获取资源成功，请选择日期');
        },

        /**
         * 时间格式转换
         * @author yangyi
         * @param date
         */
        dateFormat: function (date) {
            var pattern = /(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/;
            var formatedDate = date.replace(pattern, '$1/$2/$3 $4:$5:$6');
            var ddate = new Date(formatedDate);
            return ddate.Format('yyyy-MM-dd hh:mm:ss');
        },

        dateToyyMMddHHmmss: function (date) {
            return date.getFullYear().toString().substring(2) +
                resourceList.fillZero(date.getMonth() + 1) +
                resourceList.fillZero(date.getDate()) +
                resourceList.fillZero(date.getHours()) +
                resourceList.fillZero(date.getMinutes()) +
                resourceList.fillZero(date.getSeconds());
        },
        fillZero: function (n) {
            return n < 10 ? '0' + n : n
        },
        yyMMddHHmmssToDate: function (yyMMddHHmmss) {
            let nowDate = new Date();
            let pattern = /(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/;
            let formatDateStr = yyMMddHHmmss.replace(pattern, '$1/$2/$3 $4:$5:$6');
            return new Date(nowDate.getFullYear().toString().substring(0, 2) + formatDateStr);
        },
        /**
         *日期数组去重
         * @author yangyi
         * @param data
         * @returns {[null]}
         */
        unique: function (data) {
            var res = [];
            var json = {};
            for (var i = 0; i < data.length; i++) {
                if (!json[data[i]]) {
                    res.push(data[i]);
                    json[data[i]] = 1;
                }
            }
            return res;
        },

        /**
         * 获取行驶轨迹数据
         * @author wangjianyu
         */
        history: function (positionals) {
            resultful = positionals.resultful;
            lineArr = [];
            coordMap.clear();
            for (var i = 0; i < resultful.length; i++) {
                var latitude = resultful[i].latitude; //获取纬度;
                var longtitude = resultful[i].longtitude; //获取经度;

                var vtime = resultful[i].vtime;
                var originalLongtitude = resultful[i].originalLongtitude;
                var originalLatitude = resultful[i].originalLatitude;
                coordMap.put(vtime, [originalLongtitude, originalLatitude])
                if (latitude !== 0 && longtitude !== 0) {
                    lineArr.push([longtitude, latitude]);
                }

            }
            if (lineArr.length > 0) {
                resourceList.polyline(lineArr);
            }
        },

        /**
         * 报警名称单个换行
         * @author yangyi
         */
        alarmNameSplit: function (data) {
            var str = "";
            if (data != "") {
                var alarmSplit = data.split(",");
                for (var i = 0; i < alarmSplit.length; i++) {
                    str += alarmSplit[i] + "<br/>"
                }
                return str;
            }
        },

        /**
         * 单击列表高亮，执行下载功能，
         * @author yangyi
         * @param e
         */
        onclickVideo: function (e) {
            if (!$(e).hasClass("tableHighlight-blue")) {
                $("#resourceListDataTable tbody").find("tr").removeClass("tableHighlight-blue");
                $(e).addClass("tableHighlight-blue");
                //播放按钮图标改变
                $("#resourceListVideoPlay").removeClass("video-resource-play-check").addClass("video-resource-play");
                //视频暂停
                $("#videoSource")[0].pause();
                //重置快进快退
                resourceVideoGoingIndex = 1;
                resourceVideoBackIndex = 1;
                $("#resourceLeftGripVal").text(1);
                $("#leftGripValue").css("left", "0px");
                //资源列表 - 清空暂停视频时间记录
                resourceListVideoPauseTime = 0;
            }
        },

        /**
         * 双击资源列表播放
         * @author yangyi
         * @param e
         */
        ondblclickVideo: function (e) {
            $("#resourceListDataTable tbody").find("tr").removeClass("tableHighlight-blue");
            $(e).addClass("tableHighlight-blue");
            //视频暂停
            $("#videoSource")[0].pause();
            var ftpName = $(e).attr("data-ftpName");
            var fileName = $(e).attr("data-fileName");
            // var channelNum = $(e).attr('data-num');
            resourceList.publicVideoPlay(fileName, ftpName,
                function () {
                    $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
                    $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
                });
        },

        /**
         * 双击资源列表播放
         * @author lkh
         * @param e
         */
        ondblclickFTPVideo: function (e) {
            $("#resourceListDataTables tbody").find("tr").removeClass("tableHighlight-blue");
            $(e).addClass("tableHighlight-blue");
            //视频暂停
            $("#videoSource")[0].pause();
            var ftpName = $(e).attr("data-ftpName");
            var fileName = $(e).attr("data-fileName");

            // var channelNum = $(e).attr('data-num');
            resourceList.publicVideoPlay(fileName, ftpName,
                function () {
                    $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
                    $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
                });
        },

        /**
         * 文件下载公用方法，下载后播放，单双击调用
         * @author yangyi
         * @param fileName
         * @param ftpName
         * @param playCallback
         */
        publicVideoPlay: function (fileName, ftpName, playCallback) {
            // resourceList.onFtpVideoPlayEnd();
            var url = "/clbs/realTimeVideo/resource/fileDownload";
            var parameter = {
                "fileName": fileName,
                "ftpName": ftpName
            };
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success) {
                    var name = data.obj.fileName;
                    var srcStr = "/clbs/resourceVideo/" + name;
                    var videoSource = $("#videoSource");
                    videoSource.removeAttr("src");
                    videoSource.attr("src", srcStr);
                    videoSource[0].addEventListener("ended", function () {
                        resourceList.onFtpVideoPlayEnd();
                    });
                    videoSource[0].addEventListener("abort", function () {
                        resourceList.onFtpVideoPlayEnd();
                    });
                    videoSource[0].addEventListener("error", function () {
                        if (document.getElementById("videoSource").error) {
                        }
                        resourceList.onFtpVideoPlayEnd();
                    });
                    //视频播放获取播放倍数公用函数
                    resourceList.getVideoPlaybackSpeedFn("videoSource", "resourceLeftGripVal", true);
                    if (playCallback) {
                        playCallback();
                    }
                }
            });
        },
        /**
         * @author lichong
         * ftp文件播放结束时调用
         */
        onFtpVideoPlayEnd: function () {
            $("#videoSource").removeAttr("src");

            $("#resourceListVideoPlay").removeClass("video-resource-play-check").addClass("video-resource-play").prop("title", "播放");

            $("#resourceListVideoFrame").prop("title", "关键帧播放");
            if (timerIframePlayer) {
                clearInterval(timerIframePlayer);
                timerIframePlayer = null;
            }
        },
        /**
         * @author lichong
         * ftp文件模拟关键帧播放
         */
        autoPlayIframeVideo: function () {
            var videoSource = $("#videoSource");
            videoSource[0].pause();
            videoSource[0].currentTime += 3;
        },

        /**
         * @author wangjianyu
         * 文件下载请求
         */
        fileDownload: function (fileName, ftpName, dowloadName) {

            var url = "/clbs/realTimeVideo/resource/fileDownload";
            var parameter = {
                "fileName": fileName,
                "ftpName": ftpName
            };
            json_ajax("POST", url, "json", false, parameter, function (data) {
                if (data.success) {
                    window.location.href = "/clbs/realTimeVideo/resource/download?path=" + dowloadName + "";
                }
            });
            if (navigator.appName === "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") === "MSIE9.0") {
                event.cancelBubble = true
            } else {
                event.stopPropagation();
            }
        },

        /**
         * @author lkh
         * 文件下载请求
         */
        fileHrefDownload: function (url) {
            window.location.href = url;
        },

        /**
         * 日期格式转换为时间戳
         * @author yangyi
         * @param time
         * @returns {number}
         */
        timestamp: function (time) {
            time = time.replace(/-/g, '/');
            var times = new Date(time);
            var timestamp = times.getTime();
            timestamp = timestamp / 1000;
            return timestamp;
        },

        /**
         * 公用列表集合组装方式，主要包含以下集合组装
         * FtpResourcesLists     查询FTP的资源列表集合组装
         * deviceResourcesLists  查询终端的资源列表集合组装
         * videoPlayLists        查询FTP视频资源集合组装
         * videoPlayDeviceLists  查询终端视频资源集合组装
         * @author yangyi
         * FTP资源列表集合组装
         */
        publicMapData: function (time, data, mapType, _newKey) {
            var list = mapType.get(time);
            if (list == null) {
                if (_newKey) {
                    mapType.put(time, [data], _newKey);
                } else {
                    mapType.put(time, [data]);
                }
            } else {
                if (mapType.containsKey(time)) {
                    mapType.remove(time);
                }
                list.push(data);
                if (_newKey) {
                    mapType.put(time, list, _newKey);
                } else {
                    mapType.put(time, list);
                }
                // mapType.put(time, list);
            }
        },

        /**
         * 线路绘制
         * @author wangjiangyu
         * @param lineArr
         */
        polyline: function (lineArr) {
            var icoName = "../../resources/img/vehicle.png";
            //创建车辆行驶的图标
            if (vico != null && vico != "") {
                icoName = "../../resources/img/vico/" + vico;
            }
            marker = new AMap.Marker({
                map: videoMap,
                position: lineArr[0],
                icon: "../../resources/img/" + icoName,
                offset: new AMap.Pixel(-26, -13),
                autoRotation: true
            });
            // 绘制轨迹
            var polylines = new AMap.Polyline({
                map: videoMap,
                path: lineArr,
                strokeColor: "#3366FF", //线颜色
                strokeOpacity: 1, //线透明度
                strokeWeight: 5, //线宽
                strokeStyle: "solid", //线样式
                strokeDasharray: [10, 5],
                zIndex: 51 //补充线样式
            });
            videoMap.setFitView(); //轨迹居中
        },

        /**
         * tab选项卡向下隐藏显示函数
         * @author yangbike
         */
        tabHideDownFn: function () {
            if ($(this).hasClass("fa fa-chevron-down")) {
                $('#videoRightTop').css('height', 'calc(100% - 42px)');
                $(this).removeClass("fa fa-chevron-down").addClass("fa fa-chevron-up");
                //获取通道号勾选数量  传入分屏函数(此方法用于列表隐藏显示后 视频自适应计算)
                var _channelNumber = 0;
                for (var i = 0; i < _checkChannelList.values().length; i++) {
                    if (_checkChannelList.values()[i] === "true") {
                        _channelNumber += 1;
                    }
                }
                //列表显示隐藏视频自适应显示函数(分屏自动计算)(参数：_channelNumber 通道号数量)
                resourceList.thisListShowsVideoScreenSeparated(_channelNumber);
            } else {
                //判断tab当前选择在资源列表
                var height = ($('#videoRightBottom').height() + 10) + 'px';
                $('#videoRightTop').css('height', 'calc(100% - ' + height + ')');
                $(this).removeClass("fa fa-chevron-up").addClass("fa fa-chevron-down");
                //获取通道号勾选数量  传入分屏函数(此方法用于列表隐藏显示后 视频自适应计算)
                _channelNumber = 0;
                for (i = 0; i < _checkChannelList.values().length; i++) {
                    if (_checkChannelList.values()[i] === "true") {
                        _channelNumber += 1;
                    }
                }
                //列表显示隐藏视频自适应显示函数(分屏自动计算)(参数：_channelNumber 通道号数量)
                resourceList.thisListShowsVideoScreenSeparated(_channelNumber);
            }
        },

        /**
         * 列表显示隐藏视频自适应显示函数(分屏自动计算)
         * @author yangbike
         * @param _number 分隔数量
         */
        thisListShowsVideoScreenSeparated: function (_number) {
            //日历点击请求数据后  未点击播放前  页面默认video标签自适应计算
            var defaultVideoLen = $("#resource-video-module div").length;
            //分隔1
            if (_number === 1 || defaultVideoLen === 1) {
                resourceList.theListShowsVideoAdaptation(1, 1, null, null);
            }
            //分隔2
            else if (_number === 2) {
                resourceList.theListShowsVideoAdaptation(2, 1, null, null);
            }
            //分隔3
            else if (_number === 3) {
                resourceList.theListShowsVideoAdaptation(2, 2, 1, 2);
            }
            //分隔4
            else if (_number === 4) {
                resourceList.theListShowsVideoAdaptation(2, 2, null, null);
            }
            //分隔5 - 6
            else if (_number === 5 || _number === 6) {
                if (_number === 5) {
                    resourceList.theListShowsVideoAdaptation(3, 3, 2, 2);
                } else if (_number === 6) {
                    resourceList.theListShowsVideoAdaptation(3, 2, null, null);
                }
            }
            //分隔 7 - 8
            else if (_number === 7 || _number === 8) {
                if (_number === 7) {
                    resourceList.theListShowsVideoAdaptation(4, 4, 3, 3);
                } else if (_number === 8) {
                } else if (_number === 8) {
                    resourceList.theListShowsVideoAdaptation(4, 4, 3, 3);
                }
            }
            //分隔9
            else if (_number === 9) {
                resourceList.theListShowsVideoAdaptation(3, 3, null, null);
            }
            //分隔10
            else if (_number === 10) {
                resourceList.theListShowsVideoAdaptation(5, 5, 4, 4);
            }
            //分隔11 - 16
            else if (_number === 11 || _number === 12 || _number === 13 || _number === 14 || _number === 15 || _number === 16) {
                if (_number === 11) {
                    resourceList.theListShowsVideoAdaptation(4, 3, null, null);
                } else if (_number === 12) {
                    resourceList.theListShowsVideoAdaptation(4, 3, null, null);
                } else if (_number === 13) {
                    resourceList.theListShowsVideoAdaptation(4, 4, null, null);
                } else if (_number === 14) {
                    resourceList.theListShowsVideoAdaptation(4, 4, null, null);
                } else if (_number === 15) {
                    resourceList.theListShowsVideoAdaptation(4, 4, null, null);
                } else if (_number === 16) {
                    resourceList.theListShowsVideoAdaptation(4, 4, null, null);
                }
            }
        },

        /**
         * 列表显示隐藏视频自适应显示函数
         * @author yangbike
         */
        theListShowsVideoAdaptation: function (_equalW, _equalH, _oneW, _oneH) {
            //高宽度
            var vwidth = 100 / _equalW;
            //判断视频模块是否隐藏
            if ($("#mapAllShow").children().hasClass("fa fa-chevron-right")) {
                var vheight = $("#resource-map-module").height() / _equalH;
            } else {
                vheight = $("#resource-video-module").height() / _equalH;
            }
            //默认高宽度
            $("#resource-video-module div:nth-child(2)").nextAll().css({
                "width": vwidth + "%",
                "height": "calc(100% - (100% - " + vheight + "px))"
            });
            if (_oneW != null && _oneH != null) {
                //第一个视频高宽度
                var vOneWidth = vwidth * _oneW;
                var vOneHeight = vheight * _oneH;
                $("#resource-video-module div.v-one").css({
                    "width": vOneWidth + "%",
                    "height": "calc(100% - (100% - " + (vOneHeight - 0.1) + "px))"
                });
            }
        },

        /**
         * 视频倍数转换
         * @author wangjianyu
         */
        _unfast: function (a) {
            if (a === 0) {
                return 1;
            } else if (a === 2) {
                return 2
            } else if (a === 4) {
                return 3
            } else if (a === 8) {
                return 4
            } else if (a === 16) {
                return 5
            }
        },

        /**
         * 播放列表 - 视频后退
         * @author yangbike
         */
        videoBackFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#playListVideoBack").removeClass("video-back-check").addClass("video-back");
            }, 100)
            $("#playListVideoBack").removeClass("video-back").addClass("video-back-check");
            var playLeftGripVal = $("#playLeftGripVal");
            if (oldType === "0") { // 终端

                var videoPlaySpeed = Number(playLeftGripVal.text());
                if (videoPlaySpeed !== 1) {
                    videoPlaySpeed = videoPlaySpeed / 2;
                } else {
                    videoPlaySpeed = 16;
                }
                if (videoPlaySpeed === 1) {
                    $("#rightGripValue").css("left", "0");
                    playLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 2) {
                    $("#rightGripValue").css("left", "18px");
                    playLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 4) {
                    $("#rightGripValue").css("left", "36px");
                    playLeftGripVal.text(4);
                } else if (videoPlaySpeed === 8) {
                    $("#rightGripValue").css("left", "61px");
                    playLeftGripVal.text(8);
                } else if (videoPlaySpeed === 16) {
                    $("#rightGripValue").css("left", "98px");
                    playLeftGripVal.text(16);
                }
                resourceList.videoPlayBack();
            } else {
                //判断播放后视频集合中数据长度
                if (videoDataList.elements.length > 0) {
                    //获取快进值 判断是否快进
                    var fastForwardVal = Number(playLeftGripVal.text());
                    if (fastForwardVal !== 1 && fastForwardVal > 1) {
                        playListVideoGoingIndex = 1;
                    } else {
                        if (playListVideoBackIndex !== 0.0625) {
                            playListVideoBackIndex /= 2;
                        }
                    }
                    for (var i = 0; i < videoDataList.elements.length; i++) {
                        //获取通道号
                        var dataKey = videoDataList.elements[i].key;
                        //获取video对象
                        var videos = document.getElementById("v_" + dataKey + "_Source");
                        //有快进倍数值  先还原
                        if (fastForwardVal !== 1 && fastForwardVal > 1) {
                            var minVal = fastForwardVal / 2;
                            if (minVal === 8) {
                                $("#rightGripValue").css("left", "61px");
                                playLeftGripVal.text(minVal);
                                playListVideoGoingIndex = 4;
                                videos.playbackRate = minVal;
                            } else if (minVal === 4) {
                                $("#rightGripValue").css("left", "36px");
                                playLeftGripVal.text(minVal);
                                playListVideoGoingIndex = 3;
                                videos.playbackRate = minVal;
                            } else if (minVal === 2) {
                                $("#rightGripValue").css("left", "18px");
                                playLeftGripVal.text(minVal);
                                playListVideoGoingIndex = 2;
                                videos.playbackRate = minVal;
                            } else if (minVal === 1) {
                                $("#rightGripValue").css("left", "0px");
                                playLeftGripVal.text(minVal);
                                playListVideoGoingIndex = 1;
                                videos.playbackRate = minVal;
                            }
                            videoControlPlugin.setSpeed(playListVideoGoingIndex);
                        }
                        //无快进值 则直接进行快退计算
                        else {
                            if (playListVideoBackIndex === 0.5) {
                                playLeftGripVal.text("1/2");
                                // videos.playbackRate = playListVideoBackIndex;
                            } else if (playListVideoBackIndex === 0.25) {
                                playLeftGripVal.text("1/4");
                                // videos.playbackRate = playListVideoBackIndex;
                            } else if (playListVideoBackIndex === 0.125) {
                                playLeftGripVal.text("1/8");
                                // videos.playbackRate = playListVideoBackIndex;
                            } else if (playListVideoBackIndex === 0.0625) {
                                playLeftGripVal.text("1/16");
                                // videos.playbackRate = playListVideoBackIndex;
                            } else {
                                return false;
                            }
                            videos.playbackRate = playListVideoBackIndex;
                            videoControlPlugin.setSpeed(playListVideoBackIndex);
                        }
                    }
                } else {
                    return false;
                }
            }
        },

        /**
         * 视频播放插件点击自动播放方法
         * @author yangbike
         */
        pluginClickAutoPlay: function () {
            //获取通道号勾选数量  传入分屏函数
            var _channelNumber = 0;
            var _channelNumberList = [];
            for (var i = 0; i < _checkChannelList.values().length; i++) {
                if (_checkChannelList.values()[i] === "true") {
                    _channelNumber += 1;
                    var keyStr = _checkChannelList.keys()[i];
                    var keyChannel;
                    if (keyStr.length >= 9) {
                        keyChannel = keyStr.substring(keyStr.length - 2, keyStr.length);
                    } else {
                        keyChannel = keyStr.substring(keyStr.length - 1, keyStr.length);
                    }
                    _channelNumberList.push(keyChannel);
                }
            }
            // _channelNumberList.sort();
            //判断通道号是否勾选
            if (_channelNumber > 0) {
                if (oldType === "0") { // 终端
                    //请求视频文件时设为false 播放按钮点击时进入暂停相关逻辑
                    var playListVideoPlay = $("#playListVideoPlay");
                    if (playListVideoPlay.attr('data-stop-state')) {
                        playListVideoPlay.attr('data-stop-state', false);
                        if (playListVideoPlay.hasClass("video-play")) {
                            // 当前播放列表  视频播放方式
                            var playListWay = $('#videoPlayWay').val();
                            if (playListWay === playListVideoPlayWay) {
                                resourceList.videoStartPlay();
                            }
                        }
                    }
                    //视频面板分隔函数(参数：_channelNumber 通道号数量)
                    if (videoPlayFlag) {
                        videoPlayFlag = false;
                        resourceList.videoScreenSeparated(_channelNumberList);
                    }
                    //点击播放后 禁用通道号选择
                    $("#checkAll").prop("disabled", "disabled");
                    for (i = 0; i < _checkChannelList.keys().length; i++) {
                        $("#" + _checkChannelList.keys()[i]).prop("disabled", "disabled");
                    }
                } else {
                    //判断第一次播放及后续播放
                    if (videoPlayFlag) {
                        //重置为FALSE 资源列表TAB切换播放列表时
                        videoPlayFlag = false;
                        //视频面板分隔函数(参数：_channelNumber 通道号数量)
                        resourceList.videoScreenSeparated(_channelNumberList);
                        //点击播放后 禁用通道号选择
                        $("#checkAll").prop("disabled", "disabled");
                        for (i = 0; i < _checkChannelList.keys().length; i++) {
                            $("#" + _checkChannelList.keys()[i]).prop("disabled", "disabled");
                        }
                    } else {
                        //视频暂停后 从停止点重新播放
                        for (i = 0; i < videoDataList.elements.length; i++) {
                            var _channelKeys = videoDataList.elements[i].key;
                            $("#v_" + _channelKeys + "_Source")[0].play();
                        }
                    }
                }
            }
        },

        /**
         * 播放列表 - 视频播放
         * @author yangbike
         */
        videoPlayFn: function () {
            getResFlag = false;
            //判断终端及FTP是否有数据
            if (videoPlayDeviceLists.elements.length > 0 || videoPlayLists.elements.length > 0) {
                var hasChannelData = _checkChannelList.elements;
                var checkChannelArr = [];
                for (var i = 0; i < hasChannelData.length; i++) {
                    if (hasChannelData[i].value === 'true') {
                        checkChannelArr.push(hasChannelData[i].key.replace('subChk_', ''));
                    }
                }
                var playDataFlag = false; // 判断所勾选通道是否有播放资源
                var item;
                for (i = 0; i < checkChannelArr.length; i++) {
                    if (oldType !== "0") {
                        item = videoPlayLists.get(Number(checkChannelArr[i]));
                    } else {
                        item = videoPlayDeviceLists.get(Number(checkChannelArr[i]));
                    }
                    if (item) {
                        playDataFlag = true;
                        break;
                    }
                }
                if (!playDataFlag) {
                    layer.msg('亲,所选通道没有数据哦');
                    return;
                }
                if (resourceList.channelHasChecked()) {
                    //播放
                    var playListVideoPlay = $("#playListVideoPlay");
                    if (playListVideoPlay.hasClass("video-play")) {
                        videoPlayFlag = true;
                        $("#playListVideoStop").off("click").on("click", resourceList.videoStopFn); //播放列表-视频停止
                        $("#playListVideoStop").css("cursor", "pointer");
                        //ftp
                        if (oldType !== "0") {
                            //播放按钮及停止按钮图标改变
                            playListVideoPlay.removeClass("video-play").addClass("video-play-check");
                            //播放按钮TITLE设置
                            playListVideoPlay.prop("title", "暂停");
                            //插件播放调用
                            // videoPlay();
                            pluginClickPlayFlag = true;
                            if (!pluginClickPlayFlag) {
                                resourceList.pluginClickAutoPlay();
                            }
                            //插件传输数据播放Flag设置为true 自动进入插件传输数据函数
                            //播放过程中点击暂停后再播放
                            if (ftpVideoPlayStopFlag) {
                                for (i = 0; i < videoDataList.elements.length; i++) {
                                    //通道号key
                                    var _channelKeys = videoDataList.elements[i].key;
                                    for (var j = 0; j < videoChannelStopTimeList.elements.length; j++) {
                                        if (_channelKeys == videoChannelStopTimeList.elements[j].key) {
                                            var _thisTime = videoChannelStopTimeList.elements[j].value;
                                        }
                                    }
                                    //设置播放时间
                                    $("#v_" + _channelKeys + "_Source")[0].currentTime = _thisTime;
                                    //播放视频
                                    $("#v_" + _channelKeys + "_Source")[0].play();
                                    document.getElementById("v_" + _channelKeys + "_Source").addEventListener('canplay', function () {
                                        videoControlPlugin.continue();
                                    });
                                }
                                ftpVideoPlayStopFlag = false;
                            } else {
                                if (pluginClickSendPlayEntrance) {
                                    //视频播放后 点击暂停后重新选择插件时间点数据 再次播放
                                    // $("video").removeAttr("src");
                                    resourceList.jwVideoClear();
                                    pluginSendData = resourceList.getFtpVideoList();
                                    resourceList.playFTPVideoDataFn(pluginSendData);
                                } else {
                                    // 正常播放
                                    if (!videoPluginSelect) {
                                        videoControlPlugin.play();
                                        videoControlPlugin.pause();
                                    }
                                    pluginSendData = resourceList.getFtpVideoList();
                                    resourceList.playFTPVideoDataFn(pluginSendData);
                                }
                            }
                        }
                        //终端
                        else {
                            //插件传输数据播放Flag设置为true 自动进入插件传输数据函数
                            pluginClickPlayFlag = true;
                            // 进行视频指令下发
                            resourceList.terminalVideoPlayback();
                        }
                        //视频播放暂停按钮点击时地图描绘线路
                        playPauseMapTrackFlag = true;
                        //调用地图轨迹描绘函数
                        resourceList.trakPlayBack(playPauseMapTrackFlag);
                    }
                    //停止
                    else {
                        //播放按钮图标改变
                        playListVideoPlay.removeClass("video-play-check").addClass("video-play").attr('data-stop-state', true);
                        //播放按钮TITLE设置
                        playListVideoPlay.prop("title", "播放");
                        pluginClickPlayFlag = false;
                        if (oldType === "0") { // 终端
                            videoPluginSelect = true;
                            videoControlPlugin.pause();
                            // debugger
                            $('video').attr('data-video-stop', true);
                            resourceList.videoPlayEnd(null, 'TIMEOUT');
                            // resourceList.videoStopPlay();
                            //终端视频资源播放暂停后 重置为true
                            terminalVideoPlayStopFlag = true;
                        } else {
                            //判断插件点击位置是否有数据
                            if (pluginSendData.length > 0) {
                                videoControlPlugin.pause();
                                //获取到的视频集合 对应video标签进行停止播放
                                for (i = 0; i < videoDataList.elements.length; i++) {
                                    //通道号key
                                    _channelKeys = videoDataList.elements[i].key;
                                    //暂停视频
                                    $("#v_" + _channelKeys + "_Source")[0].pause();

                                    //获取视频暂停时间
                                    var stopVideoTime = $("#v_" + _channelKeys + "_Source")[0].currentTime;
                                    //存储通道号及播放暂停时间集合数据组装
                                    if (videoChannelStopTimeList.isEmpty()) {
                                        videoChannelStopTimeList.put(_channelKeys, stopVideoTime);
                                    } else {
                                        if (videoChannelStopTimeList.containsKey(_channelKeys)) {
                                            videoChannelStopTimeList.remove(_channelKeys);
                                        }
                                        videoChannelStopTimeList.put(_channelKeys, stopVideoTime);
                                    }

                                }
                            }
                            videoPlayFlag = false;
                            //FTP视频资源播放暂停后 重置为true
                            ftpVideoPlayStopFlag = true;
                        }
                        //视频播放暂停按钮点击时地图描绘线路
                        playPauseMapTrackFlag = false;
                        //调用地图轨迹描绘函数
                        resourceList.trakPlayBack(playPauseMapTrackFlag);
                    }
                } else {
                    layer.msg('亲，请选择需要播放的视频通道号');
                }
            } else {
                layer.msg('亲,请先获取数据哦');
            }
        },

        /**
         * 停止播放
         * @author wushengsong
         */
        stop: function ($checkbox) {
            if (oldType === "0") { // 终端
                // 获取所有通道号
                if ($checkbox === undefined) {
                    $checkbox = $('.channel-checkbox');
                }
                var channelArray = [];
                for (var i = 0; i < $checkbox.length; i++) {
                    channelArray.push($($checkbox[i]).data('channel'));
                }
                resourceList.resourceVideoStopFn(channelArray);
                // resourceList.playListResetFn();
            } else {
                //FTP视频重置
                videoControlPlugin.stop();
                resourceList.playListResetFn();
                pluginSendData = [];
                videoDataList.clear();
                //重置快进快退
                playListVideoGoingIndex = 1;
                playListVideoBackIndex = 1;
                $("#playLeftGripVal").text(1);
                $("#rightGripValue").css("left", "0px");
                //重置是否点击播放FLAG
                pluginClickPlayFlag = false;
                ftpVideoPlayStopFlag = false;
            }
        },

        /**
         * 资源列表切换播放列表及播放暂停后再次播放 重新下发终端资源视频播放公用函数
         * @author yangbike
         */
        terminalVideoPlayback: function (rMode) {
            if (!videoPluginSelect) {
                videoControlPlugin.play();
                var playSpeed = $('#playLeftGripVal').text();
                videoControlPlugin.setSpeed(playSpeed);
                videoControlPlugin.pause();
            }
            // 清空正在播放的video list数组
            pluginSendData = [];
            //调用自动播放函数 执行分屏相关
            resourceList.pluginClickAutoPlay();
            //播放按钮及停止按钮图标改变
            var playListVideoPlay = $("#playListVideoPlay");
            playListVideoPlay.removeClass("video-play").addClass("video-play-check");
            //播放按钮TITLE设置
            playListVideoPlay.prop("title", "暂停");
            //获取当前时间轴时间
            var hh = $('#h').text();
            var mm = $('#m').text();
            var ss = $('#s').text();
            var nowHMS = pluginStopTime + " " + hh + mm + ss + "";
            var pluginClickEndTime = resourceList.timestamp(nowHMS);
            if (videoPlayDeviceLists.elements.length > 0) {
                // 当前播放列表  视频播放方式
                var playListWay = $('#videoPlayWay').val();
                if (playListWay !== playListVideoPlayWay) {
                    resourceList.jwVideoClear();
                }
                // var checkChannelValue =_checkChannelList.values();
                var channelData = videoPlayDeviceLists.elements;
                for (var i = 0; i < videoPlayDeviceLists.elements.length; i++) {
                    var checkState = _checkChannelList.get('subChk_' + channelData[i].key);
                    if (checkState !== 'true') {
                        continue;
                    }
                    //获取通道号
                    var _thisKey = channelData[i].key;
                    var _thisValue = videoPlayDeviceLists.get(_thisKey);
                    var psTime;
                    var edTime;
                    var channelNum;
                    //获取开始结束时间
                    if (_thisValue) {
                        for (var j = 0; j < _thisValue.length; j++) {
                            if (pluginClickEndTime >= _thisValue[j][0] && pluginClickEndTime <= _thisValue[j][1]) {
                                psTime = pluginClickEndTime;
                                edTime = _thisValue[j][1];
                                channelNum = _thisValue[j][_thisValue[j].length - 1];

                                // 将播放的时间段视频保存
                                pluginSendData.push([
                                    psTime,
                                    edTime,
                                    channelNum,
                                    _thisKey
                                ]);
                            }
                        }
                    }
                }
                // 开始对视频进行订阅
                resourceList.terminalVideoSub(pluginSendData, undefined, rMode);
            }
        },

        // 终端视频订阅
        terminalVideoSub: function (info, playType, rMode) {
            var multiple; //播放倍数
            var remoteMode; //回放方式
            if ($('#resourceListActive').hasClass('active')) { //资源列表
                multiple = Number($('#resourceLeftGripVal').text());
                if (rMode) {
                    remoteMode = rMode;
                } else {
                    remoteMode = Number($('#videoResourcePlayWay').val());
                }

            } else { // 播放列表
                multiple = Number($('#playLeftGripVal').text());
                if (rMode) {
                    remoteMode = rMode;
                } else {
                    remoteMode = Number($('#videoPlayWay').val());
                }
            }
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            var node = treeObj.getNodeByParam("id", oldVehicleId, null);
            var simcardNumber = node.simcardNumber; //电话号码
            var deviceNumber = node.deviceNumber; //终端编号
            var forwardOrRewind = resourceList.getVideoPlaySpeed(multiple); //快进快退倍数

            // 组装订阅数据
            var ids = [];
            var cNums = [];
            var vTypes = [];
            var value = videoPlayDeviceLists.get(info[0] ? Number(info[0][2]) : '');
            if (info) {
                for (var i = 0; i < info.length; i++) {
                    ids.push(oldVehicleId);
                    cNums.push(info[i][2]);
                    if (value) {
                        vTypes.push(value[0][2]);
                    }
                    // 关闭视频socket
                    var createVideoId = simcardNumber + '-' + info[i][2];
                    if (mseVideoLists.containsKey(createVideoId)) {
                        var videoObj = mseVideoLists.get(createVideoId);
                        videoObj.closeSocket();
                        mseVideoLists.remove(createVideoId);
                    }
                }
            }
            var idsStr = [...new Set(ids)].join(',');
            var cNumsStr = [...new Set(cNums)].join(',');
            var vTypesStr = [...new Set(vTypes)].join(',');
            var startStr = (info[0] ? info[0][0] : '');
            var endStr = (info[0] ? info[0][1] : '');
            var node = treeObj.getNodeByParam("id", oldVehicleId, null);
            var cNumsStrData = cNumsStr.split(',')
            var subData = []
            cNumsStrData.forEach(it => {
                subData.push({
                    vehicleId: idsStr,
                    channelNumber: it,
                    videoType: vTypesStr,
                    flag: false,
                    simcardNumber: node.simcardNumber,
                    deviceNumber: deviceNumber,
                    streamType: value ? value[0][3] : '',
                    storageType: value ? value[0][4] : '',
                    remoteMode: remoteMode,
                    forwardOrRewind: forwardOrRewind,
                    physicsChannel: it,
                    startTime: playType === 'RESOURCE' ? startStr : resourceList.timestampTransform(startStr),
                    endTime: playType === 'RESOURCE' ? endStr : resourceList.timestampTransform(endStr)
                })
            })
            broadVideoCastSocket.forEach(it => {
                it.cmdCloseVideo()
                it.closeSocket()
            })
            resourceList.openTerminalVideo(subData);
        },
        // 订阅成功后打开视频
        openTerminalVideo: function (subInfo) {
            var info = subInfo;
            var takeUpChannel = [];
            if (info) {
                var protocol = 'ws://';
                if (document.location.protocol === 'https:') {
                    protocol = 'wss://';
                }
                var videoUrl = `/clbs/v/monitoring/audioAndVideoParameters/${treePid}`
                json_ajax("POST", videoUrl, "json", true, null, function (data) {
                    if (broadVideoCastSocket.length > 0) {
                        broadVideoCastSocket.forEach(it => {
                            it.cmdCloseVideo()
                            it.closeSocket()
                        })
                    }
                    broadVideoCastSocket = []
                    $('#resource-video-module video').siblings('img,div').remove();
                    for (let i = 0; i < info.length; i++) {
                        var value = info[i];
                        // 没有被订阅过
                        if (!value.flag) {
                            isPlayFlag = true;
                            socketConnectNum += 1;
                            var id = value.simcardNumber + '-' + value.channelNumber;
                            var domId;
                            if ($('#resourceListActive').hasClass('active') || $('#FTPListActive').hasClass('active')) {
                                domId = 'videoSource';
                            } else {
                                domId = 'v_' + value.channelNumber + '_Source';
                            }
                            window.videoPlaybackSourceId = id;
                            var url = protocol + videoRequestUrl + ':' + videoResourcePort + '/' + value.simcardNumber + '/' + value.channelNumber;
                            var videoData = { //固定参数
                                channelCount: data.obj.vocalTractStr || '0',
                                audioFormat: data.obj.audioFormatStr,
                                playType: "TRACK_BACK",
                                dataType: "1",
                                userID: data.obj.userUuid,
                                deviceID: data.obj.deviceId,
                                sampleRate: data.obj.samplingRateStr || '8000',
                                vehicleId: value.vehicleId,
                                channelNumber: value.channelNumber,
                                physicsChannel: value.physicsChannel,
                                deviceNumber: value.deviceNumber,
                                videoType: value.videoType,
                                streamType: '0',
                                storageType: value.storageType,
                                remoteMode: value.remoteMode,
                                forwardOrRewind: value.forwardOrRewind,
                                startTime: value.startTime,
                                endTime: value.endTime,
                                domId: domId,
                                id: id,
                                simcardNumber: value.simcardNumber,
                                deviceType: data.obj.deviceType,
                            };
                            var newPanoramic = false;
                            if (channelLists && channelLists.length > 0) {
                                for (let pp = 0; pp < channelLists.length; pp++) {
                                    if (value.channelNumber == channelLists[pp].physicsChannel) {
                                        newPanoramic = channelLists[pp].panoramic;
                                    }
                                }
                            }
                            if (data.success && !info[i].flag) {
                                // $('#' + domId).siblings('img,div').remove();
                                var createMseVideo = new RTPMediaPlayer({
                                    domId: domId,
                                    url: url,
                                    data: videoData,
                                    playType: 'TRACK_BACK',
                                    panoramaType: newPanoramic === true ? 1 : 0,
                                    vrImageSrc: '/clbs/resources/img/qj360.png',
                                    // 开始播放
                                    onMessage: function ($data, failMsg) {
                                        var infodata = JSON.parse(failMsg);
                                        if (infodata.data.msgBody.code == -1004 ||
                                            infodata.data.msgBody.code == -1005 ||
                                            infodata.data.msgBody.code == -1006 ||
                                            infodata.data.msgBody.code == -1008
                                        ) {
                                            layer.msg(infodata.data.msgBody.msg);
                                        }
                                        if (infodata.data.msgBody.code == -1008) {
                                            info[i].flag = true
                                            // //播放按钮及停止按钮图标改变
                                            // playListVideoPlay.removeClass("video-play").addClass("video-play-check");
                                            // //播放按钮TITLE设置
                                            // playListVideoPlay.prop("title", "暂停");
                                        } else if (infodata.data.msgBody.code == -1005) {
                                            var aaa = '#v_' + $data.channelNumber + '_Source';
                                            var videoBackground = window.localStorage.getItem('videoBg');
                                            $(aaa).attr(
                                                'style', 'width:100%; height:100%;background-image: url(' + videoBackground + ')!important'
                                            )
                                        }
                                    },
                                    socketOpenFun: function ($data, backOwn) {
                                        var videoResourSetting = {
                                            vehicleId: $data.vehicleId, // 车辆ID
                                            simcardNumber: $data.simcardNumber, // sim卡号
                                            channelNumber: $data.channelNumber, // 通道号
                                            sampleRate: $data.sampleRate, // 采样率
                                            channelCount: $data.channelCount, // 声道数
                                            audioFormat: $data.audioFormat, // 编码格式
                                            playType: $data.playType, // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                                            dataType: $data.dataType, // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                                            userID: $data.userID, // 用户IDtakeUpChannel
                                            deviceID: $data.deviceID, // 终端ID
                                            streamType: $data.streamType, // 码流类型0：主码流，1：子码流
                                            startTime: $data.startTime,
                                            endTime: $data.endTime,
                                            remoteMode: JSON.stringify($data.remoteMode),
                                            forwardOrRewind: JSON.stringify($data.forwardOrRewind),
                                            storageType: JSON.stringify($data.storageType),
                                            deviceType: $data.deviceType,
                                        };
                                        backOwn.play(videoResourSetting);
                                    },
                                    onPlaying: function ($state, $this) {
                                        if ($('#playListActive').hasClass('active')) {
                                            $('#playModule').removeClass('slider-module');
                                        } else {
                                            $('#resourceModule').removeClass('slider-module');
                                        }
                                        resourceList.videoPlaySuccess($state, $this);
                                    },
                                    socketCloseFun: function () {
                                        socketConnectNum -= 1;
                                        if (socketConnectNum === 0 && isPlayFlag && videoPlayFlag) {
                                            //取消播放按钮播放样式
                                            if ($("#playListVideoPlay").hasClass('video-play-check')) {
                                                resourceList.videoPlayFn();
                                                videoControlPlugin.pause();
                                                layer.msg('视频订阅失败,已暂停播放');
                                            }
                                            /*$("#playListVideoPlay").removeClass("video-play-check").addClass("video-play").prop("title", "播放");
                                            videoControlPlugin.pause();// 暂停播放器指针移动*/
                                        }
                                    },
                                });
                                mseVideoLists.put(id, createMseVideo);
                                broadVideoCastSocket.push(createMseVideo);

                            }
                        } else {
                            takeUpChannel.push(value.channelNumber);
                        }
                    }
                })


            }

            // 通道号占用情况
            if (takeUpChannel.length > 0) {
                var takeUpStr = '被占用视频通道为：' + takeUpChannel.join(',');
                layer.msg(takeUpStr);
            }
        },

        // 视频播放触发事件
        videoPlaySuccess: function (state, $this) {
            if ($('#playListVideoPlay').hasClass('video-play-check')) {
                resourceList.videoHandleBtnChange();
                if ($('#playListActive').hasClass('active')) {
                    videoControlPlugin.continue();
                }
                // 将初始化的视频对象存入集合

                terminalVideoPlayList.remove(state.channelNumber);
                terminalVideoPlayList.put(state.channelNumber, true);
                // 改变对应通道号和时间段的播放状态
                var time = searchDate + ' ' + $('#h').text() + $('#m').text() + $('#s').text();
                var date = (new Date(time.replace(/-/g, '/'))).getTime() / 1000;
                var values = videoPlayDeviceLists.get(Number(state.channelNumber));
                for (var i = 0; i < values.length; i++) {
                    if (date >= values[i][0] && date <= values[i][1]) {
                        values[i][5] = true;
                        break;
                    }
                }
                videoPlayDeviceLists.remove(state.channelNumber);
                videoPlayDeviceLists.put(state.channelNumber, values);
                var videoObj = $('#' + state.domId);
                videoObj.attr('data-id', state.id);
                videoObj.off('click').on('click', resourceList.openVideoChannelVoice)
            } else {
                // 通过资源列表播放，直接打开声音
                var id = window.videoPlaybackSourceId;
                if (mseVideoLists.containsKey(id)) {
                    var thisVideoObj = mseVideoLists.get(id);
                    thisVideoObj.openVideoVoice();
                    createChannelVoice = id;
                }
            }

            // $('#video-main-content .video-main-right .video-right-top #resource-video-module video').attr(
            //     'style', 'width:100%; height:100%;background-image: url(/clbs/resources/img/videoPrompt/video7.png)!important'
            // )
        },

        /**
         * 轨迹播放暂停方法
         * @author yangbike
         */
        trakPlayBack: function (flog) {
            var indexStart = 0; //下标归零
            var videoPlayFnTime = pluginClickStartTime; //当前视频时间

            if (lineArr.length > 0) {
                if (flog) {
                    indexStart = resourceList.videoPlayGetStartTime(videoPlayFnTime); //获取当前时间所对应的轨迹点下标
                    if (indexStart === undefined) {
                        indexStart = 0;
                    }
                    //当前状态为播放
                    if (lineArr !== undefined) {
                        marker.setPosition(lineArr[indexStart]); //将小车移动到播放点
                    }
                } else {
                    //获取停止时间
                    var hh = $(".time-info #h").text();
                    var mm = $(".time-info #m").text();
                    var ss = $(".time-info #s").text();
                    var nowHMS = pluginStopTime + " " + hh + mm + ss + "";
                    var pluginClickEndTime = resourceList.timestamp(nowHMS);
                    //当前状态为暂停
                    var indexEnde; //暂停播放下标位置;因为只会在这里用一次，所以不用设置全局变量
                    videoPlayFnTime = pluginClickEndTime; //当前视频时间
                    indexEnde = resourceList.videoPlayGetStartTime(videoPlayFnTime); //获取当前时间所对应的轨迹点下标
                    if (lineArr !== undefined) {
                        if (indexEnde === undefined) {
                            indexEnde = lineArr.length - 1;
                        }
                        marker.setPosition(lineArr[indexEnde]); //将小车移动到结束点
                        resourceList.coverPolyline(indexStart, indexEnde);
                    }
                }
            }
        },

        /**
         * 播放列表 - 视频重置
         * @author yangbike
         */
        videoStopFn: function () {
            var $checkbox = $('.channel-checkbox');
            var channelArray = [];
            for (var i = 0; i < $checkbox.length; i++) {
                channelArray.push($($checkbox[i]).data('channel'));
            }
            if (channelArray.length === 0) {
                channelArray = null;
            }
            $('#playLeftGripVal').text(1);
            $('#rightGripValue').css('left', 0);
            $('#playModule').addClass('slider-module');
            videoPluginSelect = false;
            terminalVideoPlayList.clear();
            videoPlayState = false;
            isPlayFlag = false;
            $('#playListVideoPlay').removeAttr('data-stop-state');
            //图标点击渐变效果
            setTimeout(function () {
                $("#playListVideoStop").removeClass("video-stop-check").addClass("video-stop");
            }, 100)
            $("#playListVideoStop").removeClass("video-stop").addClass("video-stop-check");

            //取消播放按钮播放样式
            $("#playListVideoPlay").removeClass("video-play-check").addClass("video-play");
            //点击停止后 启用通道号选择
            $("#checkAll").removeAttr("disabled");
            for (i = 0; i < _checkChannelList.keys().length; i++) {
                $("#" + _checkChannelList.keys()[i]).removeAttr("disabled");
            }
            //重置时设为true 播放方法进入请求视频相关
            videoPlayFlag = true;

            if (oldType === "0") { // 终端
                resourceList.resourceVideoStopFn(channelArray);

            } else {
                //FTP视频重置
                videoControlPlugin.stop();
                resourceList.playListResetFn();
                pluginSendData = [];
                videoDataList.clear();
                //重置快进快退
                playListVideoGoingIndex = 1;
                playListVideoBackIndex = 1;
                $("#playLeftGripVal").text(1);
                $("#rightGripValue").css("left", "0px");
                //重置是否点击播放FLAG
                pluginClickPlayFlag = false;
                ftpVideoPlayStopFlag = false;
            }

            //重置点击 未除播放以外的操作按钮绑定禁用样式
            resourceList.playListResetFn();
            if (marker) {
                marker.setPosition(lineArr[0]); //将小车移动到播放点
            }
            if (polylinesNew != null) {
                polylinesNew.hide();
            }
            polylinesNew = null;
        },

        /**
         * 播放列表 - 视频重置调用函数
         * @author yangbike
         */
        playListResetFn: function () {
            //清空视频
            var videoObj = $("#resource-video-module div:nth-child(2)");
            videoObj.nextAll().remove();
            //显示播放列表添加的视频面板
            videoObj.removeClass("hidden-video").css({
                "width": "100%",
                "height": "100%"
            });
            $("#playListVideoFrame,#playListVideoStop,#playListVideoBack,#playListVideoGoing").css("cursor", "not-allowed");
            //播放列表-视频后退
            $("#playListVideoBack").off("click");
            //播放列表-视频停止
            $("#playListVideoStop").off("click");
            //播放列表-视频单帧播放
            $("#playListVideoFrame").off("click");
            //播放列表-视频前进
            $("#playListVideoGoing").off("click");
            //播放列表 - 还原查询终端资源类型时快进快退初始值
            $("#playListVideoBack,#playListVideoGoing").attr("data", "0");
            //重置地图轨迹描绘flag
            playPauseMapTrackFlag = null;
        },

        /**
         * 播放列表 - 视频单帧播放
         * @author yangbike
         */
        videoFrameFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#playListVideoFrame").removeClass("video-frame-check").addClass("video-frame");
            }, 100)
            $("#playListVideoFrame").removeClass("video-frame").addClass("video-frame-check");
            if (oldType === "0") { // 终端
                videoControlPlugin.pause();
                // 获取所有通道号
                var $checkbox = $('.channel-checkbox');
                var channelArray = [];
                for (var i = 0; i < $checkbox.length; i++) {
                    channelArray.push($($checkbox[i]).data('channel'));
                }

                var vehicleIdString = ''; // 监控对象id
                var channelString = ''; // 通道号
                for (i = 0; i < channelArray.length; i++) {
                    vehicleIdString += oldVehicleId + ',';
                    channelString += channelArray[i] + ',';
                }
                vehicleIdString = vehicleIdString.substring(0, vehicleIdString.length - 1);
                channelString = channelString.substring(0, channelString.length - 1);
                resourceList.sendStopInstruct(vehicleIdString, channelString, 'keyframes')
            }
        },

        /**
         * 播放列表 - 视频前进
         * @author yangbike
         */
        videoGoingFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#playListVideoGoing").removeClass("video-going-check").addClass("video-going");
            }, 100)
            $("#playListVideoGoing").removeClass("video-going").addClass("video-going-check");
            var playLeftGripVal = $("#playLeftGripVal");
            if (oldType === "0") { // 终端
                var videoPlaySpeed = Number(playLeftGripVal.text());
                if (videoPlaySpeed !== 16) {
                    videoPlaySpeed = videoPlaySpeed * 2;
                } else {
                    videoPlaySpeed = 1;
                }
                if (videoPlaySpeed === 1) {
                    $("#rightGripValue").css("left", "0");
                    playLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 2) {
                    $("#rightGripValue").css("left", "18px");
                    playLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 4) {
                    $("#rightGripValue").css("left", "36px");
                    playLeftGripVal.text(4);
                } else if (videoPlaySpeed === 8) {
                    $("#rightGripValue").css("left", "61px");
                    playLeftGripVal.text(8);
                } else if (videoPlaySpeed === 16) {
                    $("#rightGripValue").css("left", "98px");
                    playLeftGripVal.text(16);
                }
                resourceList.videoQuickPlay();
            } else {
                //判断播放后视频集合中数据长度
                if (videoDataList.elements.length > 0) {
                    //获取快进值 判断是否快进
                    var fastForwardVal = playLeftGripVal.text();
                    if (fastForwardVal.indexOf("/") > -1) {
                        playListVideoBackIndex = 1;
                    } else {
                        if (playListVideoGoingIndex !== 5) {
                            playListVideoGoingIndex += 1;
                        }
                    }
                    for (var i = 0; i < videoDataList.elements.length; i++) {
                        //获取通道号
                        var dataKey = videoDataList.elements[i].key;
                        //获取video对象
                        var videos = document.getElementById("v_" + dataKey + "_Source");
                        //有快退倍数  先还原
                        if (fastForwardVal.indexOf("/") > -1) {
                            if (fastForwardVal === "1/2") {
                                videos.playbackRate = 0.5 * 2;
                                playLeftGripVal.text("1");
                            } else if (fastForwardVal === "1/4") {
                                videos.playbackRate = 0.25 * 2;
                                playLeftGripVal.text("1/2");
                            } else if (fastForwardVal === "1/8") {
                                videos.playbackRate = 0.125 * 2;
                                playLeftGripVal.text("1/4");
                            } else if (fastForwardVal === "1/16") {
                                videos.playbackRate = 0.0625 * 2;
                                playLeftGripVal.text("1/8");
                            } else {
                                return false;
                            }
                        } else {
                            //记录点击次数
                            if (playListVideoGoingIndex === 2) {
                                videos.playbackRate = 2;
                                $("#rightGripValue").css("left", "18px");
                                playLeftGripVal.text(playListVideoGoingIndex);
                                videoControlPlugin.setSpeed(2);
                            } else if (playListVideoGoingIndex === 3) {
                                videos.playbackRate = 4;
                                $("#rightGripValue").css("left", "36px");
                                playLeftGripVal.text(4);
                                videoControlPlugin.setSpeed(4);
                            } else if (playListVideoGoingIndex === 4) {
                                videos.playbackRate = 8;
                                $("#rightGripValue").css("left", "61px");
                                playLeftGripVal.text(8);
                                videoControlPlugin.setSpeed(8);
                            } else if (playListVideoGoingIndex === 5) {
                                videos.playbackRate = 16;
                                $("#rightGripValue").css("left", "98px");
                                playLeftGripVal.text(16);
                                videoControlPlugin.setSpeed(16);
                            } else if (playListVideoGoingIndex > 5) {
                                playListVideoGoingIndex = 5;
                                videos.playbackRate = 16;
                                playLeftGripVal.text(16);
                            }
                        }
                    }
                }
            }
        },

        /**
         * 资源列表 - 视频后退
         * @author yangbike
         */
        resourceVideoBackFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#resourceListVideoBack").removeClass("video-resource-back-check").addClass("video-resource-back");
            }, 100)
            $("#resourceListVideoBack").removeClass("video-resource-back").addClass("video-resource-back-check");
            //获取video对象
            var videos = document.getElementById("videoSource");
            //判断 终端及FTP
            var resourceLeftGripVal = $("#resourceLeftGripVal");
            if (oldType === "0") {
                var videoPlaySpeed = Number(resourceLeftGripVal.text());
                if (videoPlaySpeed !== 1) {
                    videoPlaySpeed = videoPlaySpeed / 2;
                } else {
                    videoPlaySpeed = 16;
                }
                if (videoPlaySpeed === 1) {
                    $("#leftGripValue").css("left", "0");
                    resourceLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 2) {
                    $("#leftGripValue").css("left", "18px");
                    resourceLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 4) {
                    $("#leftGripValue").css("left", "36px");
                    resourceLeftGripVal.text(4);
                } else if (videoPlaySpeed === 8) {
                    $("#leftGripValue").css("left", "61px");
                    resourceLeftGripVal.text(8);
                } else if (videoPlaySpeed === 16) {
                    $("#leftGripValue").css("left", "98px");
                    resourceLeftGripVal.text(16);
                }

                // var channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue').attr('data-channelNum');
                var channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;

                resourceList.videoPlayBack(channelNum);
            } else {
                //获取快进值 判断是否快进
                var fastForwardVal = Number(resourceLeftGripVal.text());
                //有快进倍数值  先还原
                if (fastForwardVal !== 1 && fastForwardVal > 1) {
                    resourceVideoGoingIndex = 1;
                    var minVal = fastForwardVal / 2;
                    if (minVal === 8) {
                        $("#leftGripValue").css("left", "61px");
                        resourceLeftGripVal.text(minVal);
                        resourceVideoGoingIndex = 4;
                        videos.playbackRate = minVal;
                    } else if (minVal === 4) {
                        $("#leftGripValue").css("left", "36px");
                        resourceLeftGripVal.text(minVal);
                        resourceVideoGoingIndex = 3;
                        videos.playbackRate = minVal;
                    } else if (minVal === 2) {
                        $("#leftGripValue").css("left", "18px");
                        resourceLeftGripVal.text(minVal);
                        resourceVideoGoingIndex = 2;
                        videos.playbackRate = minVal;
                    } else if (minVal === 1) {
                        $("#leftGripValue").css("left", "0px");
                        resourceLeftGripVal.text(minVal);
                        resourceVideoGoingIndex = 1;
                        videos.playbackRate = minVal;
                    } else {
                        return false;
                    }
                }
                //无快进值 则直接进行快退计算
                else {
                    resourceVideoBackIndex /= 2;
                    if (resourceVideoBackIndex === 0.5) {
                        resourceLeftGripVal.text("1/2");
                        videos.playbackRate = resourceVideoBackIndex;
                    } else if (resourceVideoBackIndex === 0.25) {
                        resourceLeftGripVal.text("1/4");
                        videos.playbackRate = resourceVideoBackIndex;
                    } else if (resourceVideoBackIndex === 0.125) {
                        resourceLeftGripVal.text("1/8");
                        videos.playbackRate = resourceVideoBackIndex;
                    } else if (resourceVideoBackIndex === 0.0625) {
                        resourceLeftGripVal.text("1/16");
                        videos.playbackRate = resourceVideoBackIndex;
                    } else {
                        return false;
                    }
                }
            }
        },

        resourceListPlayChannelNum: null,
        /**
         * 资源列表 - 视频播放
         * @author yangbike
         */
        resourceVideoPlayFn: function () {
            //播放
            getResFlag = false;
            if ($(this).hasClass("video-resource-play")) {
                //判断未双击列表 直接点击播放问题
                // var videoSrc = $("#videoSource source").attr("src");
                if ($("#resourceListDataTable tbody").find("tr").hasClass("tableHighlight-blue")) {
                    //播放按钮TITLE设置
                    if (oldType === "0") { // 终端
                        // $('#videoResourcePlayWay').val('0')
                        var channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;
                        resourceList.resourceListPlayChannelNum = channelNum;
                        var physicsChannel = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;
                        var videoStartTime = $('#resourceListDataTable tbody tr.tableHighlight-blue .videoStartTime span').attr('videostarttime');
                        var videoEndTime = $('#resourceListDataTable tbody tr.tableHighlight-blue .videoEndTime span').attr('videoendtime');

                        var subArray = [
                            [videoStartTime, videoEndTime, channelNum, physicsChannel]
                        ];
                        resourceList.terminalVideoSub(subArray, 'RESOURCE');
                        $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
                        $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
                    } else {
                        var tableHighlight = $(".tableHighlight-blue");
                        var fileName = tableHighlight.attr("data-fileName");
                        var ftpName = tableHighlight.attr("data-ftpName");
                        // var channelNum = $(".tableHighlight-blue").attr('data-num');
                        resourceList.publicVideoPlay(fileName, ftpName,
                            function () {
                                $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
                                $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
                            });
                        //播放按钮及停止按钮图标改变
                    }
                } else {
                    //终端FTP播放
                    if ($("#resourceListDataTables tbody").find("tr").hasClass("tableHighlight-blue")) {
                        var tableHighlight = $(".tableHighlight-blue");
                        var fileName = tableHighlight.attr("data-fileName");
                        var ftpName = tableHighlight.attr("data-ftpName");
                        // var channelNum = $(".tableHighlight-blue").attr('data-num');
                        resourceList.publicVideoPlay(fileName, ftpName,
                            function () {
                                $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
                                $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
                            });

                    } else {
                        layer.msg("请选择文件后点击播放或直接双击文件播放");
                        return false;
                    }

                }
            }
            //停止
            else {
                //播放按钮图标改变
                $(this).removeClass("video-resource-play-check").addClass("video-resource-play");
                //播放按钮TITLE设置
                $("#resourceListVideoPlay").prop("title", "播放");
                //视频暂停
                if (oldType === "0") { // 终端
                    if ($("#FTPListActive").hasClass("active")) {
                        //终端FTP列表暂停 2021.1.19
                        var videoSource = $("#videoSource");
                        if (!(videoSource[0].paused)) {
                            videoSource[0].pause();
                            resourceListVideoPauseTime = videoSource[0].currentTime;
                        }
                        // resourceList.videoPlayEnd([channelNum], null);
                    } else {
                        $('#videoSource').attr('data-video-stop', true);
                        // channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue').attr('data-channelNum');
                        channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;

                        // 下发视频关闭指令
                        var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
                        var node = treeObj.getNodeByParam("id", oldVehicleId, null);
                        var simcardNumber = node.simcardNumber; // 电话号码
                        var createVideoId = simcardNumber + '-' + channelNum;
                        if (mseVideoLists.containsKey(createVideoId)) {
                            var videoObj = mseVideoLists.get(createVideoId);
                            videoObj.closeSocket();
                            mseVideoLists.remove(createVideoId);
                        }
                        resourceList.videoPlayEnd([channelNum], null);

                    }


                } else {
                    var videoSource = $("#videoSource");
                    if (!(videoSource[0].paused)) {
                        videoSource[0].pause();
                        resourceListVideoPauseTime = videoSource[0].currentTime;
                    }
                    // resourceList.onFtpVideoPlayEnd();
                }
            }
        },

        /**
         * 资源列表 - 视频重置
         * @author yangbike
         */
        resourceVideoStopFn: function (channel) {
            $('#resourceModule').addClass('slider-module');
            document.getElementById('videoSource').src = '';
            document.getElementById('videoSource').load();
            document.getElementById('videoSource').removeAttribute('src');
            //图标点击渐变效果
            setTimeout(function () {
                $("#resourceListVideoStop").removeClass("video-resource-stop-check").addClass("video-resource-stop");
            }, 100)

            $("#resourceListVideoStop").removeClass("video-resource-stop").addClass("video-resource-stop-check");

            //播放按钮图标改变
            $('#resourceListVideoPlay').removeClass("video-resource-play-check").addClass("video-resource-play");
            //播放按钮TITLE设置
            $("#resourceListVideoPlay").prop("title", "播放");

            if (oldType === "0" && !ftpVideoPlay) {
                document.getElementById('videoSource').setAttribute('data-currenttime', 0);
                document.getElementById('videoSource').setAttribute('data-video-stop', false);
                resourceVideoPlayIndex = [];
                var channelNum = null;
                if (channel === undefined) {
                    if ($('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0]) {
                        channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML
                    }
                } else if (typeof channel === 'object') {
                    channelNum = channel;
                } else if (typeof channel === 'string') {
                    channelNum = [channel];
                }
                resourceList.videoPlayEnd(channelNum);
                resourceList.jwVideoClear();
            } else {
                //重置视频
                // document.getElementById('videoSource').removeAttribute('src');
                $("#videoSource").removeAttr("src");
                //移除高亮
                $("#resourceListDataTable tbody").find("tr").removeClass("tableHighlight-blue");

                //重置快进快退
                resourceVideoGoingIndex = 1;
                resourceVideoBackIndex = 1;
                $("#resourceLeftGripVal").text(1);
                $("#leftGripValue").css("left", "0px");
                //资源列表 - 清空暂停视频时间记录
                resourceListVideoPauseTime = 0;

                resourceList.onFtpVideoPlayEnd();
            }
        },

        /**
         * 资源列表 - 视频单帧播放
         * @author yangbike
         */
        resourceVideoFrameFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#resourceListVideoFrame").removeClass("video-resource-frame-check").addClass("video-resource-frame");
            }, 100)
            $("#resourceListVideoFrame").removeClass("video-resource-frame").addClass("video-resource-frame-check");

            var $this = $('#resourceListDataTable tbody tr.tableHighlight-blue');
            if (oldType === "0") {
                // $('#videoResourcePlayWay').val('3');
                // 终端视频
                var channelNum;
                var videoStartTime;
                var videoEndTime;
                if ($('#resourceListActive').hasClass('active')) {
                    if ($('#resourceListDataTable tbody tr.tableHighlight-blue').length > 0) {
                        channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;
                        videoStartTime = $('#resourceListDataTable tbody tr.tableHighlight-blue .videoStartTime span').attr('videostarttime');
                        videoEndTime = $('#resourceListDataTable tbody tr.tableHighlight-blue .videoEndTime span').attr('videoendtime');
                    }
                } else {
                    if ($('#resourceListDataTables tbody tr.tableHighlight-blue').length > 0) {
                        channelNum = $('#resourceListDataTables tbody tr.tableHighlight-blue td:nth-child(8)')[0].innerHTML;
                        videoStartTime = $('#resourceListDataTables tbody tr.tableHighlight-blue td:nth-child(9)')[0].innerHTML;
                        videoEndTime = $('#resourceListDataTables tbody tr.tableHighlight-blue td:nth-child(10)')[0].innerHTML;
                    }
                }
                if (videoStartTime && videoEndTime && channelNum) {
                    var subArray = [
                        [videoStartTime, videoEndTime, channelNum]
                    ];
                    resourceList.terminalVideoSub(subArray, 'RESOURCE', '3');
                    setTimeout(function () {
                        resourceList.videokeysPlay(channelNum);
                    }, 1000);
                }
            } else {
                if (!timerIframePlayer) {
                    if (!$this || $this.length <= 0) {
                        layer.msg("请选择文件后点击播放或直接双击文件播放");
                        return;
                    }
                    var tableHighlight = $(".tableHighlight-blue");
                    var fileName = tableHighlight.attr("data-fileName");
                    var ftpName = tableHighlight.attr("data-ftpName");
                    resourceList.publicVideoPlay(fileName, ftpName,
                        function () {
                            $("#resourceListVideoFrame").prop("title", "暂停");
                            $("#videoSource")[0].pause();
                            timerIframePlayer = setInterval(resourceList.autoPlayIframeVideo, 1000);
                        });

                } else {
                    resourceList.onFtpVideoPlayEnd();
                }
            }
        },

        /**
         * 资源列表 - 视频前进
         * @author yangbike
         */
        resourceVideoGoingFn: function () {
            //图标点击渐变效果
            setTimeout(function () {
                $("#resourceListVideoGoing").removeClass("video-resource-going-check").addClass("video-resource-going");
            }, 100)
            $("#resourceListVideoGoing").removeClass("video-resource-going").addClass("video-resource-going-check");
            //获取video对象
            var videos = document.getElementById("videoSource");
            //判断 终端及FTP
            var resourceLeftGripVal = $("#resourceLeftGripVal");
            if (oldType === "0") {
                var videoPlaySpeed = Number(resourceLeftGripVal.text());
                if (videoPlaySpeed !== 16) {
                    videoPlaySpeed = videoPlaySpeed * 2;
                } else {
                    videoPlaySpeed = 1;
                }
                if (videoPlaySpeed === 1) {
                    $("#leftGripValue").css("left", "0");
                    resourceLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 2) {
                    $("#leftGripValue").css("left", "18px");
                    resourceLeftGripVal.text(videoPlaySpeed);
                } else if (videoPlaySpeed === 4) {
                    $("#leftGripValue").css("left", "36px");
                    resourceLeftGripVal.text(4);
                } else if (videoPlaySpeed === 8) {
                    $("#leftGripValue").css("left", "61px");
                    resourceLeftGripVal.text(8);
                } else if (videoPlaySpeed === 16) {
                    $("#leftGripValue").css("left", "98px");
                    resourceLeftGripVal.text(16);
                }

                // var channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue').attr('data-channelNum');
                var channelNum = $('#resourceListDataTable tbody tr.tableHighlight-blue td:nth-child(7)')[0].innerHTML;

                resourceList.videoQuickPlay(channelNum);
            } else {
                //获取快进值 判断是否快进
                var fastForwardVal = resourceLeftGripVal.text();
                //有快退倍数  先还原
                if (fastForwardVal.indexOf("/") > -1) {
                    resourceVideoBackIndex = 1;
                    if (fastForwardVal === "1/2") {
                        videos.playbackRate = 0.5 * 2;
                        resourceLeftGripVal.text("1");
                    } else if (fastForwardVal === "1/4") {
                        videos.playbackRate = 0.25 * 2;
                        resourceLeftGripVal.text("1/2");
                    } else if (fastForwardVal === "1/8") {
                        videos.playbackRate = 0.125 * 2;
                        resourceLeftGripVal.text("1/4");
                    } else if (fastForwardVal === "1/16") {
                        videos.playbackRate = 0.0625 * 2;
                        resourceLeftGripVal.text("1/8");
                    } else {
                        return false;
                    }
                } else {
                    //获取点击次数
                    resourceVideoGoingIndex += 1;
                    //记录点击次数
                    if (resourceVideoGoingIndex === 2) {
                        videos.playbackRate = 2;
                        $("#leftGripValue").css("left", "18px");
                        resourceLeftGripVal.text(resourceVideoGoingIndex);
                    } else if (resourceVideoGoingIndex === 3) {
                        videos.playbackRate = 4;
                        $("#leftGripValue").css("left", "36px");
                        resourceLeftGripVal.text(4);
                    } else if (resourceVideoGoingIndex === 4) {
                        videos.playbackRate = 8;
                        $("#leftGripValue").css("left", "61px");
                        resourceLeftGripVal.text(8);
                    } else if (resourceVideoGoingIndex === 5) {
                        videos.playbackRate = 16;
                        $("#leftGripValue").css("left", "98px");
                        resourceLeftGripVal.text(16);
                    } else if (resourceVideoGoingIndex > 5) {
                        resourceVideoGoingIndex = 5;
                        videos.playbackRate = 16;
                        resourceLeftGripVal.text(16);
                        return false;
                    }
                }
            }
        },

        /**
         * 监听视频播放结束
         * @author yangbike
         */
        listenerResourceVideoFn: function () {
            //重置快进快退相关参数
            resourceVideoGoingIndex = 1;
            resourceVideoBackIndex = 1;
            $("#resourceLeftGripVal").text(1);
            $("#leftGripValue").css("left", "0px");
        },

        /**
         * 根据视频播放时间获得当前轨迹下标
         * @author wangjianyu
         * @param vtime
         * @returns {number}
         */
        videoPlayGetStartTime: function (vtime) {
            if (resultful !== undefined) {
                var index = 0;
                for (var i = 0; i < resultful.length; i++) {
                    var latitude = resultful[i].latitude; //获取纬度;
                    var longtitude = resultful[i].longtitude; //获取经度;
                    var time = resultful[i].vtime; //获得行驶数据中的所有轨迹点
                    //因为轨迹点都是过滤掉经纬度为0的坐标,所以为了避免下标异常,获取时间的时候将重新计算下标
                    if (latitude !== 0 && longtitude !== 0) {
                        var startTime = time - vtime;
                        if (startTime >= 0) {
                            return index;
                        }
                        index++;
                    }
                }
            }
        },

        /**
         * 根据开始下标点和结束下标点获取轨迹断，并覆盖到地图上
         * @author wangjianyu
         * @param indexStart
         * @param indexEnde
         */
        coverPolyline: function (indexStart, indexEnde) {
            var lineArrNew = lineArr.slice(indexStart, indexEnde + 1); //根据下标获取数组中的一段值
            // 绘制轨迹
            polylinesNew = new AMap.Polyline({
                map: videoMap,
                path: lineArrNew,
                strokeColor: "#ff17e5", //线颜色
                strokeOpacity: 1, //线透明度
                strokeWeight: 5, //线宽
                strokeStyle: "solid", //线样式
                strokeDasharray: [10, 5],
                zIndex: 51, //补充线样式
                showDir: true
            });
        },

        /**
         *初始化报警数据，初始化查询服务器类型
         * @author yangyi
         */
        initSelect: function () {
            var findType = $("#findType").val();
            var str = "<option value='0'>终端资源</option>";
            str += "<option id='ftpServer' value=" + findType + ">" + findType + "</option>";
            $("#type").append(str);
            resourceList.setAlarm();
        },

        /**
         * @author yangyi
         * @param treeId
         * @param parentNode
         * @param responseData
         * @returns {*}
         */
        ajaxAlarmDataFilter: function (treeId, parentNode, responseData) {
            alarmTypeLists = new resourceList.maps(); //重新加载树之后,重置集合
            if (responseData != null && responseData.length > 0) {
                for (var i = 0; i < responseData.length; i++) {
                    var alarmId = responseData[i].type;
                    var alarmName = responseData[i].name;
                    alarmTypeLists.put(alarmId, alarmName); //将报警类型设为key,将报警名称设置为参数,组装成FTP报警类型数据字典
                }
            }
            return responseData;
        },

        /**
         * 点击前事件
         * @author yangyi
         */
        beforeClickAlarm: function (event, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj("alarmTree");
            if (treeNode.checked) {
                treeObj.checkNode(treeNode, false, true, true);
            } else {
                treeObj.checkNode(treeNode, true, true, true);
            }
        },

        /**
         * 请求成功事件
         * @author yangyi
         * @param event
         * @param treeId
         * @constructor
         */
        AlarmTreeAsyncSuccess: function (event, treeId) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.expandAll(true);
            // $('#treeLoading').hide();
        },

        /**
         * @author yangyi
         * 树勾选事件
         */
        fuzzyOnCheckAlarm: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("alarmTree");
            //若为取消勾选则不展开节点
            resourceList.alarmBinaryClear();
            // var typeNumber = parseInt(treeNode.type);
            var nodes = zTree.getCheckedNodes(true);
            // var nodesFalse = zTree.getCheckedNodes(false);
            var str = "";
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].alarmType !== "all") {
                    resourceList.alarmBinary(parseInt(nodes[i].type), 1);
                    str += nodes[i].name + ",";
                }
            }
            alarmTen = "";
            for (var j = 0; j < alarmBinary.length; j++) {
                alarmTen += alarmBinary[j];
            }
            alarmTen = parseInt(alarmTen, 2);

            if (nodes.length === 37) {
                $("#alarmSelect").val(treeNode.name);
            } else {
                $("#alarmSelect").val(str);
            }

        },

        /**
         * 过滤报警类型
         * @author lijie
         * @param alarm
         */

        filterAlarmType: function (alarm) {
            if (alarm !== 0 & alarm != null & alarm !== "" & alarmTen !== 0 & alarmTen !== "" && alarmTen != null) {
                //当选中了报警类型或返回数据为无报警类型时，过滤传回来的数据中是选中报警类型的数据
                var alarmBinary1 = alarm.toString(2);
                var alarmBinary2 = alarmTen.toString(2);
                for (var i = 0; i < alarmBinary1.length; i++) {
                    if (alarmBinary1.charAt(i) === 1 && alarmBinary2.charAt(alarmBinary2.length - alarmBinary1.length + i)) {
                        return true;
                    }
                }
                return false;
            }
            //当没有选中类型时，直接返回true
            return alarmTen === 0 || alarmTen === "" || alarmTen === null;


        },


        /**
         * 日期解析，二进制转换为10进制
         * @author yangyi
         * @param data
         * @param type
         */
        alarmBinary: function (data, type) {
            var length = alarmBinary.length - (data + 1);
            alarmBinary.splice(length, 1, type);
        },

        /**
         * 重置报警二进制位数
         * @author yangyi
         */
        alarmBinaryClear: function () {
            alarmBinary = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        },

        /**
         * 高级查询模块显示隐藏函数
         * @author yangbike
         */
        advancedSearchFn: function () {
            var advanced = $(".advanced-search-content");
            if (advanced.is(":hidden")) {
                advanced.slideDown();
                $("#advancedSearchSpan").removeAttr("class").addClass("fa fa-caret-down");
            } else {
                advanced.slideUp();
                $("#advancedSearchSpan").removeAttr("class").addClass("fa fa-caret-up");
            }
        },

        /**
         * 视频分隔16屏
         * @author yangbike
         * @param data 分隔数量
         */
        videoScreenSeparated: function (data) {
            var _number = data.length;
            //video对象默认Logo显示函数
            resourceList.videoSeparatedLogoPosition();
            //分隔1
            if (_number === 1) {
                resourceList.videoSeparatedPublicFn(1, data, 1, 1, null, null);
            }
            //分隔2
            else if (_number === 2) {
                resourceList.videoSeparatedPublicFn(1, data, 2, 1, null, null);
            }
            //分隔3
            else if (_number === 3) {
                resourceList.videoSeparatedPublicFn(1, data, 2, 2, 1, 2);
            }
            //分隔4
            else if (_number === 4) {
                resourceList.videoSeparatedPublicFn(1, data, 2, 2, null, null);
            }
            //分隔5 - 6
            else if (_number === 5 || _number === 6) {
                if (_number === 5) {
                    resourceList.videoSeparatedPublicFn(1, data, 3, 3, 2, 2);
                } else if (_number === 6) {
                    resourceList.videoSeparatedPublicFn(1, data, 3, 2, null, null);
                }
            }
            //分隔 7 - 8
            else if (_number === 7 || _number === 8) {
                if (_number === 7) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, 3, 3);
                } else if (_number === 8) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, 3, 3);
                }
            }
            //分隔9
            else if (_number === 9) {
                resourceList.videoSeparatedPublicFn(1, data, 3, 3, null, null);
            }
            //分隔10
            else if (_number === 10) {
                resourceList.videoSeparatedPublicFn(1, data, 5, 5, 4, 4);
            }
            //分隔11 - 16
            else if (_number === 11 || _number === 12 || _number === 13 || _number === 14 || _number === 15 || _number === 16) {
                if (_number === 11) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 3, null, null);
                } else if (_number === 12) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 3, null, null);
                } else if (_number === 13) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, null, null);
                } else if (_number === 14) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, null, null);
                } else if (_number === 15) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, null, null);
                } else if (_number === 16) {
                    resourceList.videoSeparatedPublicFn(1, data, 4, 4, null, null);
                }
            }
        },

        /**
         * 定义视频分隔公共函数
         * @author yangbike
         * @param _loopStart 循环变量开始值
         * @param _loopEnd 循环变量结束值
         * @param _equalW 等分宽度
         * @param _equalH 等分高度
         * @param _oneW 第一个视频等分宽度(无需计算 则传入null)
         * @param _oneH 第一个视频等分高度(无需计算 则传入null)
         */
        videoSeparatedPublicFn: function (_loopStart, _loopEnd, _equalW, _equalH, _oneW, _oneH) {
            //定义视频
            var _html = "";
            for (var i = _loopStart; i <= _loopEnd.length; i++) {
                _html +=
                    '<div class="pull-left v-' + videoName[i - 1] + ' ">' +
                    '<video autoplay width="100%" height="100%" src="" id="v_' + _loopEnd[i - 1] + '_Source">' +
                    '</video>' +
                    // '<span >测试文本</span>' +
                    '</div>';
            }
            //判断视频数据集合中是否存在数据
            if (videoPlayLists.elements.length > 0 || videoPlayDeviceLists.elements.length) {
                //隐藏原有单个视频
                $("#resourceVideoDefault").addClass("hidden-video");
            }
            //隐藏页面初始化默认视频
            if ($("#playListActive").hasClass("active")) {
                $("#playListVideoDefault").addClass("hidden-video");
            }
            //清空视频dom
            $("#resource-video-module div:nth-child(2)").nextAll().remove();
            //添加视频dom节点
            $("#resource-video-module").append(_html);
            //视频高宽度自适应计算
            resourceList.theListShowsVideoAdaptation(_equalW, _equalH, _oneW, _oneH);
        },

        /**
         * 定义分屏后视频Logo显示函数
         * @author yangbike
         */
        videoSeparatedLogoPosition: function () {
            $("video").css("background-size", "15%");
        },

        /**
         * 全选
         * @author yangbike
         */
        allSelection: function () {
            var timeLine = $("#timeLine");
            if ($(this).is(":checked")) {
                $("input[name='subChk']").prop("checked", true);
                $("#playListDataTable tbody").find("tr").show();
                var channelKeys = _checkChannelList.keys();
                var _index = 0;
                for (var j = 0; j < channelKeys.length; j++) {
                    if (_checkChannelList.get(channelKeys[j]) === "false") {
                        _index += 1;
                    }
                }
                //增加红线长度
                var _timeLineHeight = timeLine.height();
                timeLine.css("height", _timeLineHeight + (_index * 30) + "px");
                _checkChannelList.clear();
                for (var i = 0; i < channelKeys.length; i++) {
                    _checkChannelList.put(channelKeys[i], "true");
                    //截取通道号对应编号
                    var _thisIdLen = channelKeys[i].length;
                    if (_thisIdLen === 9) {
                        var tableTrNum = channelKeys[i].substr(_thisIdLen - 2, _thisIdLen);
                    } else {
                        tableTrNum = channelKeys[i].substr(_thisIdLen - 1, _thisIdLen);
                    }
                    //取消通道号数据表格想对应值表格
                    $(".station[data-key=" + tableTrNum + "]").show();
                    $("#station_" + tableTrNum).show();
                }
            } else {
                $("input[name='subChk']").removeAttr("checked");
                $("#playListDataTable tbody").find("tr").hide();
                channelKeys = _checkChannelList.keys();
                _checkChannelList.clear();
                for (i = 0; i < channelKeys.length; i++) {
                    _checkChannelList.put(channelKeys[i], "false");
                    //截取通道号对应编号
                    _thisIdLen = channelKeys[i].length;
                    if (_thisIdLen === 9) {
                        tableTrNum = channelKeys[i].substr(_thisIdLen - 2, _thisIdLen);
                    } else {
                        tableTrNum = channelKeys[i].substr(_thisIdLen - 1, _thisIdLen);
                    }
                    //取消通道号数据表格想对应值表格
                    $(".station[data-key=" + tableTrNum + "]").hide();
                    $("#station_" + tableTrNum).hide();
                    //增加红线长度
                    _timeLineHeight = timeLine.height();
                    timeLine.css("height", _timeLineHeight - 30 + "px");
                }
            }
            resourceList.videoPlayAreaChange();
            resourceList.setMainRightLayout();
        },

        /**
         * 单选
         * @author yangbike
         */
        singleChoice: function () {
            //获取当前勾选的通道号复选框
            var _thisId = $(this).attr("id");
            //判断当前勾选是否为取消
            var timeLine = $("#timeLine");
            if (!($("#" + _thisId).is(":checked"))) {
                //改变之前存储的状态集合
                if (_checkChannelList.containsKey(_thisId)) {
                    _checkChannelList.remove(_thisId);
                }
                _checkChannelList.put(_thisId, "false");
                //字符串截取 截取相对应编号
                var _thisIdLen = _thisId.toString().length;
                if (_thisIdLen === 9) {
                    var tableTrNum = _thisId.toString().substr(_thisIdLen - 2, _thisIdLen);
                } else {
                    tableTrNum = _thisId.toString().substr(_thisIdLen - 1, _thisIdLen);
                }
                //取消通道号数据表格想对应值表格
                $(".station[data-key=" + tableTrNum + "]").hide();
                $("#station_" + tableTrNum).hide();
                //value集合中不包含false 则全部为勾选状态
                if (_checkChannelList.values().indexOf("false") !== -1) {
                    //取消全选按钮勾选状态
                    $("#checkAll").removeAttr("checked");
                }
                //减短红线长度
                var _timeLineHeight = timeLine.height();
                timeLine.css("height", _timeLineHeight - 30 + "px");
            } else {
                //改变之前存储的状态集合
                if (_checkChannelList.containsKey(_thisId)) {
                    _checkChannelList.remove(_thisId);
                }
                _checkChannelList.put(_thisId, "true");
                //字符串截取 截取相对应编号
                _thisIdLen = _thisId.toString().length;
                if (_thisIdLen === 9) {
                    tableTrNum = _thisId.toString().substr(_thisIdLen - 2, _thisIdLen);
                } else {
                    tableTrNum = _thisId.toString().substr(_thisIdLen - 1, _thisIdLen);
                }
                //取消通道号数据表格想对应值表格
                $(".station[data-key=" + tableTrNum + "]").show();
                $("#station_" + tableTrNum).show();
                //增加红线长度
                _timeLineHeight = timeLine.height();
                timeLine.css("height", _timeLineHeight + 30 + "px");
                //value集合中不包含false 则全部为勾选状态
                if (_checkChannelList.values().indexOf("false") === -1) {
                    //取消全选按钮勾选状态
                    $("#checkAll").prop("checked", "checked");
                }
            }
            resourceList.videoPlayAreaChange();
            resourceList.setMainRightLayout();
        },
        /**
         * 隐藏初始化多余的通道
         * @author lkh
         */
        hideIntTable: function () {
            var timeLine = $("#NewtimeLine");
            var _timeLineHeight = timeLine.height();
            for (let i = 0; i < channelDate.length; i++) {
                if (resourceList.showchekedLable(channelDate[i])) {
                    //取消通道号数据表格想对应值表格
                    $('#channel_' + channelDate[i]).hide()
                    $('#channelTitle_' + channelDate[i]).hide()
                    timeLine.css("height", _timeLineHeight - 30 + "px");
                    if (_checkChannelList.values().indexOf("false") !== -1) {
                        //取消全选按钮勾选状态
                        $("#checkAll").removeAttr("checked");
                    }
                }
            }
        },
        //通道是否默认的
        showchekedLable: function (val) {
            var value
            for (let i = 0; i < channelDatesNoSelect.length; i++) {
                if (channelDatesNoSelect[i] == val) {
                    value = true
                    return value
                } else {
                    value = false
                }
            }
            return value
        },
        /**
         * 监控对象通道号前端数据及DOM节点添加
         * @author yangbike
         */
        getMonitorObjeChannelNumber: function (data) {
            var _channelDatasome = data;
            var hb_channelData = _channelDatasome.concat(channelAllList)
            var _channelData = [...new Set(hb_channelData)]
            //清空通道号DOM节点
            var videoChannelSelection = $("#videoChannelSelection");
            videoChannelSelection.html("");
            //判断通道号数据是否为空
            if (data.length > 0) {
                //定义通道号复选框HTMl
                var _checkAll = '';
                var _checkHtml = '';
                var _dataTr = '';
                for (var i = 0; i < _channelData.length; i++) {
                    if (i === 0) {
                        _checkAll = '<label class="checkbox-inline"><input type="checkbox" id="checkAll" checked="checked"> 全部</label>';
                    }
                    if (!resourceList.showchekedLable(_channelData[i])) {
                        _checkHtml += '<label class="checkbox-inline"><input type="checkbox"' +
                            ' name="subChk" class="channel-checkbox"' +
                            ' data-channel="' + _channelData[i] +
                            '" checked="checked" id="subChk_' + _channelData[i] + '"> 通道' + _channelData[i] + '</label>';
                    } else {
                        _checkHtml += '<label class="checkbox-inline nocheckoutChannel"><input type="checkbox"' +
                            ' name="subChk" class="channel-checkbox"' +
                            ' data-channel="' + _channelData[i] +
                            '"  id="subChk_' + _channelData[i] + '"> 通道' + _channelData[i] + '</label>';
                    }
                    _dataTr += '<div class="station" data-key="' + _channelData[i] + '">' +
                        '<table border="0" class="techinian" cellpadding="0" cellspacing="0">' +
                        '<tr>' +
                        '<td class="">' +
                        '通道 ' + _channelData[i] + '' +
                        '</td>' +
                        '</tr>' +
                        '</table>' +
                        '</div>';
                    //保存通道复选框ID及状态到集合中(_checkChannelList)
                    if (_checkChannelList.isEmpty()) {
                        if (!resourceList.showchekedLable(_channelData[i])) {
                            _checkChannelList.put("subChk_" + _channelData[i], "true");
                        } else {
                            _checkChannelList.put("subChk_" + _channelData[i], "false");
                        }
                    } else {
                        if (_checkChannelList.containsKey("subChk_" + _channelData[i])) {
                            _checkChannelList.remove("subChk_" + _channelData[i]);
                        }
                        if (!resourceList.showchekedLable(_channelData[i])) {
                            _checkChannelList.put("subChk_" + _channelData[i], "true");
                        } else {
                            _checkChannelList.put("subChk_" + _channelData[i], "false");
                        }
                    }
                }
                //添加通道号复选框到容器
                videoChannelSelection.append(_checkAll);
                videoChannelSelection.append(_checkHtml);
                //时间维度显示模块
                var stationContainer = $("#stationContainer");
                stationContainer.append('<div class="time-dimension">小时</div>');
                //添加通道号数量到数据表格
                stationContainer.append(_dataTr);
                //时间维度父级顶部内边距
                if (stationContainer.html !== "" || stationContainer.html != null) {
                    stationContainer.css({
                        "padding-top": "0px"
                    });
                }
                //全选通道号复选框绑定事件
                $("#checkAll").on("click", resourceList.allSelection);
                //单选通道号复选框绑定事件
                $("input[name='subChk']").on("click", resourceList.singleChoice);
                $(".nocheckoutChannel").mouseover(function (e) {
                    var content = '<div>请维护此终端或此型号终端的视频通道参数</div>'
                    var _this = $(this);
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: content,
                        gravity: 'top',
                        events: 'mouseover',
                    });
                });
                resourceList.setMainRightLayout();
                resourceList.hideIntTable()
            }
            layer.closeAll();
        },

        /**
         * 获取当前监控对象通道号视频资源文件
         * @author yangbike
         * @param info 通道号对应视频文件集合
         */
        getThisChannelVideoResourceFile: function (info) {
            //判断插件点击位置是否有数据
            if (info.length === 0) {
                if (oldType === "0") {
                    resourceList.videoPlayEnd(null, null);
                } else {
                    //点击空白后  视频全部清空
                    for (var i = 0; i < videoDataList.elements.length; i++) {
                        var _channelKeys = videoDataList.elements[i].key;
                        document.getElementById('v_' + _channelKeys + '_Source').removeAttribute('src');
                        // $("#v_"+ _channelKeys +"_Source").removeAttr("src");
                    }
                    //重置
                    resourceList.playListResetFn();
                    //插件传输数据播放Flag设置为true 自动进入插件传输数据函数
                    pluginClickPlayFlag = true;
                    videoPlayFlag = true;
                    pluginClickBlankChangeTabFlag = true;
                }

                // 播放状态全部设为默认状态false
                var keys = videoPlayDeviceLists.keys();

                for (i = 0; i < keys.length; i++) {
                    var values = videoPlayDeviceLists.get(keys[i]);
                    for (var j = 0; j < values.length; j++) {
                        values[j][5] = false;
                    }
                    videoPlayDeviceLists.remove(keys[i]);
                    videoPlayDeviceLists.put(keys[i], values);
                }

            } else {
                // 终端类型
                if (oldType === "0") {
                    if ($("#playListVideoPlay").hasClass("video-play-check")) {
                        //调用自动播放函数 执行分屏相关
                        resourceList.pluginClickAutoPlay();
                        //终端视频资源下发播放公用函数
                        resourceList.terminalVideoSub(info);
                        // resourceList.terminalVideoSub(info);
                    }
                } else {
                    if ($("#playListVideoPlay").hasClass("video-play-check")) {
                        //ftp视频数据组装播放
                        resourceList.playFTPVideoDataFn(info);
                    }
                }
                //播放按钮点击后 绑定其他操作按钮点击方法
                resourceList.videoHandleBtnChange();
            }
        },

        /**
         * ftp视频数据组装播放
         * @author yangbike
         */
        playFTPVideoDataFn: function (info) {
            console.log(11111111);
            videoPlayFlag = true;
            //判断是否点击了播放
            if (pluginClickPlayFlag) {
                //判断只执行一次分屏 禁止初始化已播放视频数据
                if ($("#resource-video-module div:nth-child(2)").nextAll().length === 0) {
                    //调用自动播放函数 执行分屏相关
                    resourceList.pluginClickAutoPlay();
                }
                //请求视频文件时设为false 播放按钮点击时进入暂停相关逻辑
                videoPlayFlag = false;
                //判断查询结果
                if (videoPlayLists !== undefined) {
                    //定义变量接收插件数据
                    var plugStartTime;
                    var plugEndTime;
                    var plugChannel;
                    //获取插件传入的数据
                    for (var i = 0; i < info.length; i++) {
                        plugStartTime = info[i][0];
                        plugEndTime = info[i][1];
                        plugChannel = info[i][2];
                        var channelNum = info[i][3] ? info[i][3] : plugChannel;
                        //插件点击时间  用于地图轨迹点
                        pluginClickStartTime = plugStartTime;
                        //根据通道号获取集合数据
                        var videoListVal = videoPlayLists.get(channelNum);
                        //根据时间 遍历value值
                        var videoStartTime;
                        var videoUrl;
                        for (var j = 0; j < videoListVal.length; j++) {
                            //判断时间范围
                            if (plugStartTime >= videoListVal[j][0] && plugEndTime <= videoListVal[j][1]) {
                                //获取视频开始时间值
                                videoStartTime = videoListVal[j][0];
                                var videoEndTime = videoListVal[j][1];
                                videoUrl = videoListVal[j][2];
                                //保存根据时间及通道号查询的数据 key:通道号,value:[]
                                if (videoDataList.containsKey(channelNum)) {
                                    videoDataList.remove(channelNum);
                                }
                                videoDataList.put(channelNum, [videoStartTime, videoEndTime, videoUrl]);
                                break;
                            }
                        }
                        //视频下载
                        var url = "/clbs/realTimeVideo/resource/fileDownload";
                        var parameter = {
                            "fileName": videoUrl,
                            "ftpName": oldType
                        };
                        layer.load(2);
                        json_ajax_p("POST", url, "json", false, parameter, function (data) {
                            layer.closeAll('loading');
                            if (data.success) {
                                var name = data.obj.fileName;
                                // var videoList = videoUrl.split('/');
                                // var name = videoList[videoList.length - 1];
                                // resourceList.addDomSourcePublicFn(name, plugChannel, plugStartTime, videoStartTime);
                                resourceList.addDomSourcePublicFn(name, channelNum, plugStartTime, videoStartTime);
                            } else {
                                resourceList.videoStopFn();
                                layer.msg(data.msg);
                            }
                        });
                    }
                }
            }
        },

        /**
         * 视频播放video source标签公用函数
         * @author yangbike
         * @param _videoName 视频名称
         * @param _channelKeys 通道号
         * @param _thisPlugNowTime 插件点击视频开始时间
         * @param _thisVideoNowTime 视频数据开始时间
         */
        addDomSourcePublicFn: function (_videoName, _channelKeys, _thisPlugNowTime, _thisVideoNowTime) {
            //判断插件点击位置是否有数据
            if (pluginSendData.length > 0) {
                //计算时间差
                var timeDifference = _thisPlugNowTime - _thisVideoNowTime;
                //视频地址
                var srcStr = "/clbs/resourceVideo/" + _videoName;
                //添加视频地址
                var source = $("#v_" + _channelKeys + "_Source");
                source.attr("src", srcStr);
                //设置视频播放起始时间
                var _videoObj = document.getElementById("v_" + _channelKeys + "_Source");
                if (_videoObj != null) {
                    _videoObj.currentTime = timeDifference;
                }
                //播放视频时  取值播放倍数 如=1 则不执行此函数
                var multipleVal = $("#playLeftGripVal").text();
                if (Number(multipleVal) !== 1) {
                    //视频播放获取播放倍数公用函数
                    resourceList.getVideoPlaybackSpeedFn("v_" + _channelKeys + "_Source", "playLeftGripVal", false);
                }
                //视频右键禁用
                source.on("contextmenu", function () {
                    return false
                });
                //监听视频播放状态
                if (source.length > 0) {
                    source[0].addEventListener("ended", function () {
                        source.removeAttr("src");
                    });
                }

                // 播放控制插件move
                //if ($('#playListActive').hasClass('active')) {
                var speed = $('#playLeftGripVal').text();
                videoControlPlugin.setSpeed(speed);
                videoControlPlugin.continue();
                resourceList.videoHandleBtnChange();
                //}
            }
        },

        /**
         * 视频播放获取播放倍数公用函数
         * @author yangbike
         * @param _videoId video对象Id信息
         * @param _spanDomId 倍数值DOM id信息
         * @param _resourceFlag 资源列表视频播放flag
         */
        getVideoPlaybackSpeedFn: function (_videoId, _spanDomId, _resourceFlag) {
            //获取视频对象
            var _videoObj = document.getElementById(_videoId);
            if (_videoObj != null) {
                //获取倍数值
                var _mfVal = $("#" + _spanDomId).text();
                var speed = null;
                //判断倍数值是快退值及快进值
                if (_mfVal.indexOf("/") > -1) {
                    if (_mfVal === "1/2") {
                        _videoObj.playbackRate = 0.5;
                        speed = 0.5;
                    } else if (_mfVal === "1/4") {
                        _videoObj.playbackRate = 0.25;
                        speed = 0.25;
                    } else if (_mfVal === "1/8") {
                        _videoObj.playbackRate = 0.125;
                        speed = 0.125;
                    } else if (_mfVal === "1/16") {
                        _videoObj.playbackRate = 0.0625;
                        speed = 0.0625;
                    }
                } else {
                    if (_mfVal === "1") {
                        _videoObj.playbackRate = 1;
                        speed = 1;
                    } else if (_mfVal === "2") {
                        _videoObj.playbackRate = 2;
                        speed = 2;
                    } else if (_mfVal === "4") {
                        _videoObj.playbackRate = 4;
                        speed = 4;
                    } else if (_mfVal === "8") {
                        _videoObj.playbackRate = 8;
                        speed = 8;
                    } else if (_mfVal === "16") {
                        _videoObj.playbackRate = 16;
                        speed = 16;
                    }
                }
                if ($('#playListActive').hasClass('active')) {
                    if (videoControlPlugin) {
                        videoControlPlugin.setSpeed(speed);
                    }
                }

                if (_resourceFlag) {
                    if (resourceListVideoPauseTime !== 0) {
                        _videoObj.currentTime = resourceListVideoPauseTime;
                    }
                }
            }
        },

        /**
         * 查询类型切换，FTP类型隐藏 音视频，码流，存储类型
         * @author yangyi
         */
        typeSelect: function () {
            var type = $("#type").val();
            ifadvanced = false;
            if (type !== "0") {
                var $checkbox = $('.channel-checkbox');
                var channelArray = [];
                for (var i = 0; i < $checkbox.length; i++) {
                    channelArray.push($($checkbox[i]).data('channel'));
                }
                resourceList.resourceVideoStopFn(channelArray);
                inquiryType = 1;
                getResFlag = false;
                $("#videoType").attr("disabled", "disabled");
                $("#alarmBox").hide();
                $("#codeSchema").attr("disabled", "disabled");
                $("#storageType").attr("disabled", "disabled");
                $("#videoPlayWay").attr("disabled", "disabled");
                $("#videoResourcePlayWay").attr("disabled", "disabled");
                $('#FTPListActive').attr('style', 'display:none')
            } else {
                //终端资源
                inquiryType = 0;
                getResFlag = true;
                $("#videoType").removeAttr("disabled");
                $("#alarmBox").show();
                $("#codeSchema").removeAttr("disabled");
                $("#storageType").removeAttr("disabled");
                // $("#videoPlayWay").removeAttr("disabled");
                // $("#videoResourcePlayWay").removeAttr("disabled");
                $('#FTPListActive').attr('style', 'display:block')
            }
            if (vehicleTreeId !== "") {
                resourceList.inquiry(1, true);
            } else {
                //执行清空函数  清空相对应集合 数组 dom
                resourceList.pageLoadsEmpty();
                //清空 FTP资源列表数据
                FtpResourcesLists.clear();
                //清空 资源列表数据
                deviceResourcesLists.clear();
                // 高级查询清空高级集合
                FtpAdvancedResourcesLists.clear();

                // $("#resourceListDataTable tbody").html(""); // 	清空资源列表数据
                $('#resourceList').html('') // 	清空资源列表数据
                $('#ftpList').html('') // 	清空FTP列表数据
                var yearMonth = $('.calendar.calendar3 table caption span').html();
                var date = yearMonth.replace(/[^0-9]/ig, "-");
                var year = date.split('-')[0];
                var month = date.split('-')[1];
                resourceList.buildDate([], [year, month, 1], true);
            }
        },

        /**
         * 监听切换到资源列表函数
         * @author yangbike
         */
        monitorCutoverresourceListFn: function (e) {
            var _thisId = e.currentTarget.hash;
            if (_thisId === '#playList') { // 播放列表
                // 如果是在资源列表播放的，停止播放，并下发9202
                if ($('#resourceListVideoPlay').hasClass('video-resource-play-check')) {
                    resourceList.resourceVideoStopFn();
                }
                $("#resourceVideoDefault").addClass("hidden-video");
                if ($('#resource-video-module .pull-left').length > 2) {
                    $("#playListVideoDefault").addClass("hidden-video");
                } else {
                    $("#playListVideoDefault").removeClass("hidden-video");
                }
                //隐藏播放列表视频操作面板
                $(".video-back-module,.video-play-module").show();
                //显示资源列表视频操作面板
                $(".video-resource-module, .video-play-select").hide();
            } else if (_thisId === '#resourceList') { // 资源列表
                // 如果正在播放，停止播放
                if ($("#playListVideoPlay").hasClass("video-play-check")) {
                    // $('#playListVideoPlay').removeClass('video-play-check').addClass('video-play');
                    // resourceList.stop();
                    // 获取所有通道号
                    resourceList.videoStopFn();
                }
                resourceList.onFtpVideoPlayEnd();
                $("#resourceVideoDefault").removeClass("hidden-video");
                $("#playListVideoDefault").addClass("hidden-video");
                //隐藏播放列表视频操作面板
                $(".video-back-module,.video-play-module").hide();
                //显示资源列表视频操作面板
                $(".video-resource-module, .video-play-select").show();
            } else if (_thisId === '#ftpList') {
                // 如果正在播放，停止播放
                if ($("#playListVideoPlay").hasClass("video-play-check")) {
                    // $('#playListVideoPlay').removeClass('video-play-check').addClass('video-play');
                    // resourceList.stop();
                    // 获取所有通道号
                    resourceList.videoStopFn();
                }
                $("#resourceVideoDefault").removeClass("hidden-video");
                $("#playListVideoDefault").addClass("hidden-video");
                //隐藏播放列表视频操作面板
                $(".video-back-module,.video-play-module").hide();
                //显示资源列表视频操作面板
                $(".video-resource-module, .video-play-select").show();

            }
            if ($('#scalingBtn').hasClass('fa-chevron-down')) {
                resourceList.setMainRightLayout();
            }
        },

        /**
         * 打开终端视频声音
         */
        openVideoChannelVoice: function () {

            $('video').css('border', 'none');

            $(this).css({
                'border': '3px solid #6dcff6',
            });

            if (createChannelVoice != null) {
                if (mseVideoLists.containsKey(createChannelVoice)) {
                    var videoObj = mseVideoLists.get(createChannelVoice);
                    videoObj.closeVideoVoice();
                }
            }

            var id = $(this).attr('data-id');
            if (mseVideoLists.containsKey(id)) {
                var thisVideoObj = mseVideoLists.get(id);
                thisVideoObj.openVideoVoice();
                createChannelVoice = id;
            }
        },

        /**
         * 资源列表订阅
         * @author aoxianghua
         */
        videoSocketSubscribe: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    // webSocket.subscribe(headers, "/user/" + $("#userName").text() + "/mediainfo", resourceList.videoSocketCallBack, null, null);
                    webSocket.subscribe(headers, "/user/topic/video/history/day", resourceList.getLastOilDataCallBack, null, null);
                } else {
                    resourceList.videoSocketSubscribe();
                }
            }, 2000);
        },

        /**
         * 订阅120f 回调解析日历数据
         * @author lijie
         */
        videoDateSocketSubscribe: function () {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    webSocket.subscribe(headers, "/user/topic/video/history/month", resourceList.getMothDataCallBack, null, null);
                }
            }, 2000);
        },
        historyControlCallBack: function (body) {
            // if (true) {
            let remote = Number(body.obj.remote);
            let closeType = body.obj.closeType;
            if (remote === 2) {
                if (closeType === 'drag') {
                    let date = resourceList.yyMMddHHmmssToDate(body.obj.dragPlaybackTime);
                    let ms = date.getTime() / 1000;
                    let typeValue = $("#type").val();
                    let DragVideoList = resourceList.returnDragVideoTimeList(ms, String(typeValue) === '0' ? videoPlayDeviceLists : videoPlayLists);
                    pluginSendData = DragVideoList;
                    // 清空所有通道的视频
                    var videoList = document.getElementsByTagName('video');
                    for (let i = 0; i < videoList.length; i++) {
                        videoList[i].src = '';
                        videoList[i].removeAttribute('src');
                    }
                    if (DragVideoList.length > 0) {
                        resourceList.getThisChannelVideoResourceFile(DragVideoList);
                    }
                    return;
                }
                if (closeType === 'resourceChange') {
                    resourceList.getThisChannelVideoResourceFile(channelNumVideoList);
                    return;
                }
                if (closeType === 'keyframes') {
                    resourceList.terminalVideoPlayback('3');
                    setTimeout(function () {
                        resourceList.videokeysPlay();
                    }, 1500);
                    return;
                }
                if (closeType !== 'TIMEOUT') {
                    if (videoControlPlugin) {
                        videoControlPlugin.stop();
                    }
                }
                layer.msg('下发成功');
                return;
            }
            layer.msg('下发成功');
            if (remote === 3 || remote === 4) {
                videoControlPlugin.setDirection(remote === 3 ? 'forward' : 'backward');
                videoControlPlugin.setSpeed(resourceList.getVideoActualPlaySpeed(Number(body.obj.forwardOrRewind)));
            }
            // }
            // else {
            //     layer.msg('下发失败');
            // }
        },

        /**
         *组装解析日历数据
         * @author lijie
         */
        getMothDataCallBack: function (data) {
            var body = $.parseJSON(data.body);
            if (!body.success) {
                layer.msg(body.msg);
                return;
            }
            let obj = body.obj;
            //等待3秒如果没有数据就提示 重试或请用高级查询
            if (!getMothDataCallBackTime && showValueData) {
                getMothDataCallBackTime = setInterval(function () {
                    layer.msg('重试或请用高级查询');
                }, 3000)
            }
            if (obj === undefined || obj === null || obj === '') {
                setTimeout(function () {
                    if (getMothDataCallBackTime) {
                        clearInterval(getMothDataCallBackTime);
                        getMothDataCallBackTime = null;
                    }
                }, 4000)
                showValueData = true
                return;
            } else {
                layer.closeAll();
                var dateList = [];
                var mothDate = body.obj.toString(2);
                var mothDateLength = mothDate.length;
                if (mothDateLength < 31) {
                    for (var i = 0; i < 31 - mothDateLength; i++) {
                        mothDate = "0" + mothDate;
                    }
                }

                if (terminalDateTimer) clearTimeout(terminalDateTimer);

                var yearMonth = $('.calendar.calendar3 table caption span').html();
                var date = yearMonth.replace(/[^0-9]/ig, "-");
                var year = date.split('-')[0].substring(2, 4);
                var month = date.split('-')[1];
                if (month.length < 2) {
                    month = '0' + month;
                }
                var dateParma = year + month

                for (var n = 0; n < mothDate.length; n++) {
                    if (mothDate.charAt(n) == 1) {
                        if (31 - n > 9) {
                            dateList.push(dateParma + (31 - n).toString())
                        } else {
                            dateList.push(dateParma + "0" + (31 - n).toString())
                        }
                    }
                }
                if (dateList.length === 0) {
                    layer.msg('暂无数据');
                }
                resourceList.buildDate(dateList, [date.split('-')[0], month, 1]);
                if (getMothDataCallBackTime) {
                    clearInterval(getMothDataCallBackTime);
                    getMothDataCallBackTime = null;
                }
                showValueData = true
            }
        },

        /**
         * 视频后退
         * @author aoxianghua
         */
        videoPlayBack: function (channelNum) {
            var vehicleIdString; // 监控对象id
            var multiple; // 快进快退倍数
            var channelString; // 通道号
            // 区分 资源列表快进倍数还是播放列表快进倍数
            if ($("#resourceListActive").hasClass("active")) {
                multiple = Number($('#resourceLeftGripVal').text());
            } else {
                multiple = Number($('#playLeftGripVal').text());
            }
            if (channelNum === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                vehicleIdString = oldVehicleId;
                channelString = channelNum
            }
            resourceList.sendForwardInstruct(vehicleIdString, channelString, resourceList.getVideoPlaySpeed(multiple))
        },

        /**
         * 暂停回放
         * @author aoxianghua
         */
        videoStopPlay: function (channelNum) {
            var vehicleIdString; // 监控对象id
            var channelString; // 通道号
            if (channelNum === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                vehicleIdString = oldVehicleId;
                channelString = channelNum
            }
            resourceList.sendPauseInstruct(vehicleIdString, channelString);
        },

        /**
         * 视频id、通道号字符串拼接
         * @author aoxianghua
         */
        stringCallBack: function () {
            var idString = ''; // id
            var channelString = ''; // 通道号
            var channelTypeString = ''; // 通道类型

            var data = videoPlayDeviceLists.elements;
            for (var i = 0; i < data.length; i++) {
                var checkedState = _checkChannelList.get('subChk_' + data[i].key);
                if (checkedState === 'true') {
                    idString += oldVehicleId + ',';
                    if (data[i].newKey) {
                        channelString += data[i].newKey + ',';
                    } else {
                        channelString += data[i].key + ',';
                    }
                    var channelValue = videoPlayDeviceLists.get(data[i].key);
                    channelTypeString += channelValue[2] + ','
                }
            }

            idString = idString.substring(0, idString.length - 1);
            channelString = channelString.substring(0, channelString.length - 1);
            channelTypeString = channelTypeString.substring(0, channelTypeString.length - 1);
            return [idString, channelString, channelTypeString];
        },
        /**
         * 发送停止指令
         */
        sendStopInstruct: function (vehicleId, channelNum, closeType, dragPlaybackTime) {
            let requestStr = {
                obj: {
                    vehicleIds: vehicleId,
                    channelNums: channelNum,
                    remote: 2,
                    forwardOrRewind: 0,
                    dragPlaybackTime: dragPlaybackTime,
                    closeType: closeType
                }
            };
            broadVideoCastSocket.forEach(it => {
                it.cmdCloseVideo()
                it.closeSocket()
            })
            resourceList.historyControlCallBack(requestStr);
        },
        /**
         * 发送暂停指令
         */
        sendPauseInstruct: function (vehicleId, channelNum) {
            let requestStr = {
                vehicleIds: vehicleId,
                channelNums: channelNum,
                remote: 1,
                forwardOrRewind: 0,
                dragPlaybackTime: 0
            };
            webSocket.send("/app/video/history/pause", headers, requestStr)
        },
        /**
         * 发送开始指令
         */
        sendStartInstruct: function (vehicleId, channelNum) {
            let requestStr = {
                vehicleIds: vehicleId,
                channelNums: channelNum,
                remote: 0,
                forwardOrRewind: 0,
                dragPlaybackTime: 0
            };
            webSocket.send("/app/video/history/start", headers, requestStr)
        },
        /**
         * 发送快进指令
         */
        sendForwardInstruct: function (vehicleId, channelNum, forwardOrRewind) {
            let requestStr = {
                obj: {
                    vehicleIds: vehicleId,
                    channelNums: channelNum,
                    remote: 3,
                    forwardOrRewind: forwardOrRewind,
                    dragPlaybackTime: 0
                }
            };
            broadVideoCastSocket.forEach(it => {
                it.cmdForwardPlay(forwardOrRewind)
            })
            resourceList.historyControlCallBack(requestStr);
        },
        /**
         * 发送快退指令
         */
        sendBackwardInstruct: function (vehicleId, channelNum, forwardOrRewind) {
            let requestStr = {
                obj: {
                    vehicleIds: vehicleId,
                    channelNums: channelNum,
                    remote: 4,
                    forwardOrRewind: forwardOrRewind,
                    dragPlaybackTime: 0
                }
            };
            broadVideoCastSocket.forEach(it => {
                it.cmdForwardPlay(forwardOrRewind)
            })
            resourceList.historyControlCallBack(requestStr);
        },
        /**
         * 发送关键帧指令
         */
        sendKeyframeInstruct: function (vehicleId, channelNum) {
            let requestStr = {
                vehicleIds: vehicleId,
                channelNums: channelNum,
                remote: 6,
                forwardOrRewind: 0,
                dragPlaybackTime: 0
            };
            webSocket.send("/app/video/history/keyframe", headers, requestStr)
        },

        /**
         * 开始播放
         * @author aoxianghua
         */
        videoStartPlay: function (channelNum) {
            var vehicleIdString; // 监控对象id
            var channelString; // 通道号
            if (channelNum === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                vehicleIdString = oldVehicleId;
                channelString = channelNum
            }
            videoControlPlugin.continue();
            resourceList.sendStartInstruct(vehicleIdString, channelString);
        },

        /**
         * 快进回放
         * @author aoxianghua
         */
        videoQuickPlay: function (channelNum) {
            var vehicleIdString; // 监控对象id
            var multiple; // 快进快退倍数
            var channelString; // 通道号
            if ($("#resourceListActive").hasClass("active")) {
                multiple = Number($('#resourceLeftGripVal').text());
            } else {
                multiple = Number($('#playLeftGripVal').text());
            }
            if (channelNum === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                vehicleIdString = oldVehicleId;
                channelString = channelNum
            }
            resourceList.sendForwardInstruct(vehicleIdString, channelString, resourceList.getVideoPlaySpeed(multiple));
        },

        /**
         * 关键帧播放
         * @author aoxianghua
         */
        videokeysPlay: function (channelNum) {
            var vehicleIdString; // 监控对象id
            var channelString; // 通道号
            if (channelNum === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                vehicleIdString = oldVehicleId;
                channelString = channelNum
            }
            resourceList.sendKeyframeInstruct(vehicleIdString, channelString);
        },

        /**
         * 结束播放
         * @author aoxianghua
         */
        videoPlayEnd: function (cNumArray, closeType) {
            var vehicleIdString = ''; // 监控对象id
            var channelString = ''; // 通道号
            if (cNumArray === null || cNumArray === undefined) { // 控制所有
                var values = resourceList.stringCallBack();
                vehicleIdString = values[0];
                channelString = values[1];
            } else {
                for (var i = 0; i < cNumArray.length; i++) {
                    vehicleIdString += oldVehicleId + ',';
                    channelString += cNumArray[i] + ',';
                }
                vehicleIdString = vehicleIdString.substring(0, vehicleIdString.length - 1);
                channelString = channelString.substring(0, channelString.length - 1);
            }
            // 数据组装
            var data = {
                vehicleId: vehicleIdString,
                channelNum: channelString,
                remote: 2,
                forwardOrRewind: 0,
                dragPlaybackTime: 0
            }
            if (closeType === 'TIMEOUT') {
                $("#playListVideoFrame,#playListVideoBack,#playListVideoGoing").css("cursor", "not-allowed");
                //播放列表-视频后退
                $("#playListVideoBack").off("click");
                //播放列表-视频停止
                // $("#playListVideoStop").off("click");
                //播放列表-视频单帧播放
                $("#playListVideoFrame").off("click");
                //播放列表-视频前进
                $("#playListVideoGoing").off("click");
                broadVideoCastSocket.forEach(it => {
                    it.cmdPause()
                })
            } else {
                broadVideoCastSocket.forEach(it => {
                    it.closeSocket()
                })
            }
            resourceList.sendStopInstruct(vehicleIdString, channelString, closeType);
            //下发结束播放后 重置视频模块
            if (!closeType === 'TIMEOUT') {
                $("#resource-video-module div:nth-child(2)").nextAll().remove();
                if ($('#playListActive').hasClass('active')) {
                    $("#playListVideoDefault").removeClass("hidden-video");
                } else {
                    $("#playListVideoDefault").addClass("hidden-video");
                }
            }
            // $('#video-main-content .video-main-right .video-right-top #resource-video-module video').attr(
            //     'style', 'width:100%; height:100%;background-image: url(/clbs/resources/img/videoPrompt/videoLogo2.png)!important'
            // )
        },

        /**
         * 单击终端资源列表高亮
         * @author aoxianghua
         */
        resourceSelected: function (e) {
            var selectedResource = $(e);
            if (!selectedResource.hasClass("tableHighlight-blue")) {
                $("#resourceListDataTable tbody").find("tr").removeClass("tableHighlight-blue");
                selectedResource.addClass("tableHighlight-blue");
                //重置快进快退
                resourceVideoGoingIndex = 1;
                resourceVideoBackIndex = 1;
                $("#resourceLeftGripVal").text(1);
                $("#leftGripValue").css("left", "0px");
                if ($('#videoSource').attr('name') === "videoSource") {
                    setTimeout(function () {
                        var channelNum = selectedResource.context.children[6].innerText;
                        resourceList.videoStopPlay(channelNum);
                    }, 500)
                }
            }

        },

        /**
         * 双击终端
         * @author yangyi
         */
        ondblclickDevice: function (e) {
            if ($("#resourceListActive").hasClass("active")) {
                // 点击日期时，如果是在资源列表播放的，停止播放，并下发9202
                if ($('#resourceListVideoPlay').hasClass('video-resource-play-check')) {
                    resourceList.resourceVideoStopFn(resourceList.resourceListPlayChannelNum);
                }
            }
            subscribeSingleVideo = true;
            document.getElementById('videoSource').setAttribute('data-currenttime', 0);
            document.getElementById('videoSource').setAttribute('data-video-stop', false);
            var selectedResource = $(e);

            //datatable取数据的结构
            var channelNum = selectedResource.context.children[6].innerText;
            resourceList.resourceListPlayChannelNum = channelNum;
            var physicsChannel = selectedResource.context.children[6].innerText;
            var videoStartTime = selectedResource.context.children[7].innerHTML.toString().slice(22, 34);
            var videoEndTime = selectedResource.context.children[8].innerHTML.toString().slice(20, 32);
            // 资源列表暂无视频播放
            if (resourceVideoPlayIndex.length === 0) {
                resourceVideoPlayIndex = [channelNum, videoStartTime, videoEndTime, physicsChannel];
                var subArray = [
                    [videoStartTime, videoEndTime, channelNum, physicsChannel]
                ];
                resourceList.terminalVideoSub(subArray, 'RESOURCE');
            } else {
                $("#leftGripValue").css("left", "0px");
                $('#resourceLeftGripVal').text(1);
                resourceVideoPlayIndex = [channelNum, videoStartTime, videoEndTime, physicsChannel];
                subArray = [
                    [videoStartTime, videoEndTime, channelNum, physicsChannel]
                ];
                resourceList.terminalVideoSub(subArray, 'RESOURCE');
            }
            $("#resourceListVideoPlay").removeClass("video-resource-play").addClass("video-resource-play-check").prop("title", "暂停");
        },

        /**
         * 时间戳转日期格式
         * @author aoxianghua
         */
        timestampTransform: function (time) {
            var date = new Date(time * 1000),
                YY = String(date.getFullYear()).substring(2, 4),
                MM = (date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
                DD = date.getDate() < 10 ? '0' + date.getDate() : date.getDate(),
                hh = date.getHours() < 10 ? '0' + date.getHours() : date.getHours(),
                mm = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes(),
                ss = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
            return YY + MM + DD + hh + mm + ss;
        },

        /**
         * 获取yyyy-MM-dd hh:mm:ss时间格式
         * @param time
         * @returns {number}
         */
        getTimeFormat: function (time) {
            var date = new Date(time * 1000),
                YY = date.getFullYear(),
                MM = (date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1),
                DD = date.getDate() < 10 ? '0' + date.getDate() : date.getDate(),
                hh = date.getHours() < 10 ? '0' + date.getHours() : date.getHours(),
                mm = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes(),
                ss = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
            return YY + '-' + MM + '-' + DD + ' ' + hh + ':' + mm + ':' + ss;
        },

        resourceTovideo: function () {
            var jumpFlag = false;
            var permissionUrls = $("#permissionUrls").val();
            if (permissionUrls !== null && permissionUrls !== undefined) {
                var urllist = permissionUrls.split(",");
                if (urllist.indexOf("/realTimeVideo/video/list") > -1) {
                    return true;
                }
            }
            if (!jumpFlag) {
                layer.msg("无操作权限，请联系管理员");
            }
            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        // 视频插件清空
        jwVideoClear: function () {
            var videos = document.getElementsByTagName('video');
            for (var i = 0; i < videos.length; i++) {
                videos[i].removeAttribute('src');
            }

            var videoObjs = mseVideoLists.values();
            mseVideoLists.clear();
            for (var j = 0; j < videoObjs.length; j++) {
                videoObjs[j].closeSocket();
            }
        },

        getIco: function () {
            var url = "/clbs/realTimeVideo/resource/getIco";
            var parameter = {
                "vehicleId": oldVehicleId
            };
            json_ajax_p("POST", url, "json", true, parameter, function (data) {
                vico = data;
            });
        },

        // 获取播放倍速的下发参数
        getVideoPlaySpeed: function (num) {
            var index;
            switch (num) {
                case 1:
                    index = 1;
                    break;
                case 2:
                    index = 2;
                    break;
                case 4:
                    index = 3;
                    break;
                case 8:
                    index = 4;
                    break;
                case 16:
                    index = 5;
                    break;
            }

            return index;
        },
        getVideoActualPlaySpeed: function (num) {
            var index;
            switch (num) {
                case 1:
                    index = 1;
                    break;
                case 2:
                    index = 2;
                    break;
                case 3:
                    index = 4;
                    break;
                case 4:
                    index = 8;
                    break;
                case 5:
                    index = 16;
                    break;
            }
            return index;
        },
        // 获取监控对象数量
        getMonitoringNumber: function (data) {
            objAllNum = data.obj.allV // 监控对象数量
            resourceList.resourceListTree();
        },

        // 加载视频数据后，调整页面布局高度
        setMainRightLayout: function () {
            var height = ($('#videoRightBottom').height() + 10) + 'px';

            $('#videoRightTop').css('height', 'calc(100% - ' + height + ')');
        },

        // 勾选通道后，播放控制区域对应改变
        videoPlayAreaChange: function () {
            var cNumberList = [];
            for (var i = 0; i < channelDate.length; i++) {
                if (_checkChannelList.get('subChk_' + channelDate[i]) === 'true') {
                    cNumberList.push(channelDate[i]);
                }
            }
            cNumberList = cNumberList.join(',');
            videoControlPlugin.setChannels(cNumberList);
        },

        // 判断通道号是否有勾选
        channelHasChecked: function () {
            var state = false;
            var values = _checkChannelList.values();
            for (var i = 0; i < values.length; i++) {
                if (values[i] === 'true') {
                    state = true;
                    break;
                }
            }
            return state
        },

        // 关键帧播放、重置、快退和快进可点击
        videoHandleBtnChange: function () {
            //播放按钮点击后 绑定其他操作按钮点击方法
            $("#playListVideoBack").off("click").on("click", resourceList.videoBackFn); //播放列表-视频后退
            $("#playListVideoStop").off("click").on("click", resourceList.videoStopFn); //播放列表-视频停止
            $("#playListVideoGoing").off("click").on("click", resourceList.videoGoingFn); //播放列表-视频前进

            //播放状态下 除播放以外的操作按钮绑定可用样式
            if (oldType === '0') {
                $("#playListVideoFrame").off("click").on("click", resourceList.videoFrameFn); //播放列表-视频单帧播放
                $('#playListVideoFrame').css("cursor", "pointer");
            }
            $("#playListVideoStop,#playListVideoBack,#playListVideoGoing").css("cursor", "pointer");
        },

        // 获取Ftp对应时间的播放资源
        getFtpVideoList: function () {
            var hh = $('#h').text();
            var mm = $('#m').text();
            var ss = $('#s').text();
            var nowHMS = pluginStopTime + " " + hh + mm + ss + "";
            var ms = resourceList.timestamp(nowHMS);
            var list = [];
            for (var i = 0; i < channelDate.length; i++) {
                if (_checkChannelList.get('subChk_' + channelDate[i]) === 'true') {
                    var values = videoPlayLists.get(channelDate[i]);
                    if (values !== undefined) {
                        for (var j = 0; j < values.length; j++) {
                            // 判断拖拽点时间处于哪个视频片段
                            if (ms >= values[j][0] && ms <= values[j][1]) {
                                list.push([
                                    ms,
                                    values[j][1],
                                    values[j][values[j].length - 1],
                                    channelDate[i]
                                ]);
                            }
                        }
                    }
                }
            }
            return list;
        },
        _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
        encode: function (e) {
            var t = "";
            var n, r, i, s, o, u, a;
            var f = 0;
            e = resourceList._utf8_encode(e);
            while (f < e.length) {
                n = e.charCodeAt(f++);
                r = e.charCodeAt(f++);
                i = e.charCodeAt(f++);
                s = n >> 2;
                o = (n & 3) << 4 | r >> 4;
                u = (r & 15) << 2 | i >> 6;
                a = i & 63;
                if (isNaN(r)) {
                    u = a = 64
                } else if (isNaN(i)) {
                    a = 64
                }
                t = t + this._keyStr.charAt(s) + this._keyStr.charAt(o) + this._keyStr.charAt(u) + this._keyStr.charAt(a)
            }
            return t
        },
        _utf8_encode: function (e) {
            e = e.replace(/rn/g, "n");
            var t = "";
            for (var n = 0; n < e.length; n++) {
                var r = e.charCodeAt(n);
                if (r < 128) {
                    t += String.fromCharCode(r)
                } else if (r > 127 && r < 2048) {
                    t += String.fromCharCode(r >> 6 | 192);
                    t += String.fromCharCode(r & 63 | 128)
                } else {
                    t += String.fromCharCode(r >> 12 | 224);
                    t += String.fromCharCode(r >> 6 & 63 | 128);
                    t += String.fromCharCode(r & 63 | 128)
                }
            }
            return t
        },
        all: function () {
            onlineType = 'all';
            $("#treeLoading i").css('visibility', 'visible');
            $("#treeLoading span").text('加载中，请稍后');
            $('#treeLoading').show();
            resourceList.resourceListTree();
        },
        online: function () {
            onlineType = 'online';
            $("#treeLoading i").css('visibility', 'visible');
            $("#treeLoading span").text('加载中，请稍后');
            $('#treeLoading').show();
            resourceList.resourceListTree();
        },
        offline: function () {
            onlineType = 'offline';
            $("#treeLoading i").css('visibility', 'visible');
            $("#treeLoading span").text('加载中，请稍后');
            $('#treeLoading').show();
            resourceList.resourceListTree();
        },
        //终端ftp切换请求数据
        ftpListGet: function () {
            var startTime = $("#searchStartTime").val();
            var endTime = $("#searchEndTime").val();
            if (startTime !== "" && endTime !== "") {
                var startCheckTime = startTime;
                var endCheckTime = endTime;
                var reg = new RegExp('-', 'g');
                startCheckTime = startCheckTime.replace(reg, '/'); //正则替换
                endCheckTime = endCheckTime.replace(reg, '/');
                startCheckTime = new Date(parseInt(Date.parse(startCheckTime), 10)).getTime();
                endCheckTime = new Date(parseInt(Date.parse(endCheckTime), 10)).getTime();
                if (startCheckTime > endCheckTime) {
                    layer.msg("开始时间应在结束时间之前");
                    return false;
                }
                if (type !== 0) { //ftp
                    if ((endCheckTime - startCheckTime) > 2678400000) {
                        layer.msg("查询FTP视频信息时间范围不能大于一个月");
                        return false;
                    }
                } else { //终端
                    terminalDateTimer = setTimeout(function () {
                        layer.msg('终端无反馈,请重试');
                    }, 60000)
                }
            }
            var vehicleId = vehicleTreeId;
            var brand = vehicleTreeName;
            var alarmType = alarmTen;
            var channlNumer = $("#channelNum").val();
            var videoType = $("#videoType").val();
            var streamType = $("#codeSchema").val();
            var storageType = $("#storageType").val();
            var yearMonth = $('.calendar.calendar3 table caption span').html();
            var date = yearMonth.replace(/[^0-9]/ig, "-");
            var year = date.split('-')[0].substring(2, 4);
            var month = date.split('-')[1];
            if (month.length < 2) {
                month = '0' + month;
            }
            var dateParma = year + month
            if (selectSearchType === 1) {
                var D = $('#thisDateOf').text();
                startTime = D + " 00:00:00";
                endTime = D + " 23:59:59";
            }
            var alarmType = alarmTen;
            if (alarmType === "") {
                alarmType = 0;
            }
            var url = "/clbs/realTimeVideo/resource/getResource";
            var parameter = {
                "vehicleId": vehicleId,
                "brand": brand,
                "alarmType": alarmType,
                "channlNumer": channlNumer,
                "startTime": startTime,
                "endTime": endTime,
                "type": 'FTP服务器',
                "msgSN": msgSNAck,
                "videoType": videoType,
                "streamType": streamType,
                "storageType": storageType,
                "ftpName": 'FTP服务器',
                "date": dateParma
            };
            ftpVideoPlay = true
            json_ajax_p("POST", url, "json", true, parameter, resourceList.inquiryNewBack)

        },
    }

    $(function () {
        resourceList.init();
        resourceList.searchTimeLoad();
        resourceList.initSelect();
        $('#chooseAll_videoPlayback').on('click', resourceList.all);
        $('#online_videoPlayback').on('click', resourceList.online);
        $('#chooseMissLine_videoPlayback').on('click', resourceList.offline);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id === 'groupSelect') {
                if (navigator.appName === "Microsoft Internet Explorer") {
                    $('.resource-video-input').append('<div class="input-placeholder">请选择监控对象</div>');
                }
                vehicleTreeId = '';
                resourceList.resourceListTree();
            }
        });
        $("video").on("contextmenu", function () {
            return false
        });
        //资源列表倍数滑块初始化
        $('#resourceListSilder').nstSlider({
            "left_grip_selector": ".leftGrip",
            "rounding": {
                '0': '0',
                '1': '2',
                '2': '4',
                '4': '6',
                '8': '10',
                '16': '16'
            },
            "value_changed_callback": function (cause, leftValue) {
                $("#resourceLeftGripVal").text(leftValue);
                //直接拖动滑块过程 视频播放速度
                if (leftValue === 1) {
                    resourceVideoGoingIndex = 1;
                } else if (leftValue === 2) {
                    resourceVideoGoingIndex = 2;
                } else if (leftValue === 4) {
                    resourceVideoGoingIndex = 3;
                } else if (leftValue === 8) {
                    resourceVideoGoingIndex = 4;
                } else if (leftValue === 16) {
                    resourceVideoGoingIndex = 5;
                }
                resourceListVideoPauseTime = $("#videoSource")[0].currentTime;
                //视频播放获取播放倍数公用函数
                if (oldType !== '0') {
                    resourceList.getVideoPlaybackSpeedFn("videoSource", "resourceLeftGripVal", true);
                }
            }
        });
        // 模糊搜索
        var inputChange;
        $("#inquiry").on("click", resourceList.showQueryInfo); //显示高级查询弹窗
        $("#advancedSubmit").on("click", function () {
            var searchEndTime = $('#searchEndTime');
            var endTime = new Date(searchEndTime.val());
            var endTimeStamp = endTime.getTime();
            var searchStartTime = $('#searchStartTime');
            var startTime = new Date(searchStartTime.val());
            var startTimeStamp = startTime.getTime();
            if (endTimeStamp - startTimeStamp > 86400000) {
                layer.msg("查询时间范围不能大于一天");
                return false;
            }

            ifadvanced = true;
            getResFlag = true;
            showValueData = false
            selectSearchType = 2
            if ($('#type').val() === '0') {
                resourceList.getResourceData(searchStartTime.val(), searchEndTime.val());
                resourceList.inquiry(2) //终端添加 FTP列表添加的
                startTimeGlobal = searchStartTime.val();
                endTimeGlobal = searchEndTime.val();
            } else {
                resourceList.inquiry(2)
            }
        }); //执行高级查询
        $('#FTPListActive').on("click", function () {
            $('#resourceListVideoFrame').css('cursor', 'not-allowed');
            $('#resourceListVideoFrame').off('click');
            resourceList.ftpListGet()
        })
        $('#resourceListActive').on("click", function () {
            var type = $('#type').val();
            if (type === '0') {
                $('#resourceListVideoFrame').css('cursor', 'pointer');
                $("#resourceListVideoFrame").off('click').on("click", resourceList.resourceVideoFrameFn);
            } else {
                $('#resourceListVideoFrame').css('cursor', 'not-allowed');
                $('#resourceListVideoFrame').off('click');
            }
            ftpVideoPlay = false
        })
        $('#playListActive').on("click", function () {
            ftpVideoPlay = false
        })
        if (navigator.appName === "Microsoft Internet Explorer") {
            if (navigator.appVersion.split(";")[1].replace(/[ ]/g, "") === "MSIE9.0") {
                var search;
                groupSelect.on("focus", function () {
                    search = setInterval(function () {
                        var param = groupSelect.val();
                        if (param === '') {
                            resourceList.resourceListTree();
                        } else {
                            resourceList.searchVehicleTree(param);
                        }
                    }, 500);
                }).on("blur", function () {
                    clearInterval(search);
                });
            } else {
                groupSelect.removeAttr('placeholder');
                $('.resource-video-input').append('<div class="input-placeholder">请选择监控对象</div>');
                groupSelect.on('input propertychange', function () {
                    $('.input-placeholder').remove();
                    if (inputChange !== undefined) {
                        clearTimeout(inputChange);
                    }
                    inputChange = setTimeout(function () {
                        var param = groupSelect.val();
                        if (param === '') {
                            $('.resource-video-input').append('<div class="input-placeholder">请选择监控对象</div>');
                            resourceList.resourceListTree();
                        } else {
                            $('.input-placeholder').remove();
                            resourceList.searchVehicleTree(param);
                        }
                    }, 500);
                })
            }
        } else {
            groupSelect.on('input propertychange', function () {
                if (inputChange !== undefined) {
                    clearTimeout(inputChange);
                }
                inputChange = setTimeout(function () {
                    vehicleTreeId = '';
                    var param = groupSelect.val();
                    if (param === '') {
                        resourceList.resourceListTree();
                    } else {
                        resourceList.searchVehicleTree(param);
                    }
                }, 500);
            });
        }


        $("#groupSelectSpan,#groupSelect").on("click", resourceList.showMenu); //车辆树下拉显示
        $("#alarmSelectSpan,#alarmSelect").on("click", resourceList.showMenuAlarm); //车辆树下拉显示
        $("#mapAllShow").on("click", resourceList.mapAllShowFn); //页面右侧地图模块大小显示
        $("#scalingBtn").on("click", resourceList.tabHideDownFn); //tab选项卡向下隐藏显示函数
        //播放列表视频操作面板相关函数绑定
        $("#playListVideoPlay").on("click", resourceList.videoPlayFn); //播放列表-视频播放
        //初始化播放 除播放以外的操作按钮绑定禁用样式
        $("#playListVideoFrame,#playListVideoStop,#playListVideoBack,#playListVideoGoing").css("cursor", "not-allowed");
        $('#resourceListVideoFrame').css("cursor", "not-allowed");
        //资源列表视频操作面板相关函数绑定
        $("#resourceListVideoPlay").on("click", resourceList.resourceVideoPlayFn); //资源列表-视频播放
        $("#resourceListVideoBack").on("click", resourceList.resourceVideoBackFn); //资源列表-视频后退
        $("#resourceListVideoStop").on("click", function () {
            resourceList.resourceVideoStopFn()
        }); //资源列表-视频停止
        // $("#resourceListVideoFrame").on("click",resourceList.resourceVideoFrameFn);//资源列表-视频单帧播放
        $("#resourceListVideoGoing").on("click", resourceList.resourceVideoGoingFn); //资源列表-视频前进
        var resourceVideos = document.getElementById("videoSource");
        resourceVideos.addEventListener("ended", resourceList.listenerResourceVideoFn); //资源列表视频播放结束监听函数

        $("#advancedSearch,#advancedSearchSpan").on("click", resourceList.advancedSearchFn); //高级查询
        $('.video-right-bottom a[data-toggle="tab"]').on('shown.bs.tab', resourceList.monitorCutoverresourceListFn); //监听tab切换到资源列表
        $("#type").on("change", resourceList.typeSelect); //查询终端类型切换
        $("#resourceTovideo").on("click", resourceList.resourceTovideo); // 跳转至实时视频

        //选择ftp服务器时时间清空按钮不显示
        $('.laydate-btns-clear').css({
            'display': 'none'
        }); //默认选择ftp服务器
        $("#searchStartTime, #searchEndTime").focus(function () {
            var type = $('#type').val();
            if (type !== 0) {
                $('.laydate-btns-clear').css({
                    'display': 'none'
                });
            } else {
                $('.laydate-btns-clear').css({
                    'display': 'inline-block'
                });
            }
        })
        $("#sendSubmit").click(resourceList.submit);

        $(window).resize(function () {
            var _channelNumber = 0;
            for (var i = 0; i < _checkChannelList.values().length; i++) {
                if (_checkChannelList.values()[i] === "true") {
                    _channelNumber += 1;
                }
            }
            //列表显示隐藏视频自适应显示函数(分屏自动计算)(参数：_channelNumber 通道号数量)
            resourceList.thisListShowsVideoScreenSeparated(_channelNumber);
        });
    })
})(window, $)