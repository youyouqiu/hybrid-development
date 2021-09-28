define(['common', 'locationInfo', 'map', 'table'], function (Common, LocationInfo, Map, Table) {
    var VideoModule;
    var treeSearchState = false; // 组织树是否处于查询状态
    var allObjNum; // 监控对象总数
    var diyueall; //用户权限下的车id集合
    var urlRewriteVideoId; // 实时监控跳转 - 监控对象ID
    var urlRewriteVideoName; // 实时监控跳转 - 监控对象名称
    var isUngzip = true; // 监控对象树文件
    var currentTreeType = 'all'; // 监控对象树加载类型
    var aliasesClass = 'aliasesStyle'; // 监控对象别名样式
    var ztreeAsyncStatus = true; // 监控对象树是否异步加载
    var searchTimeout; // 监控对象树模糊查询定时对象
    var selectNodeProtocol; // 当前监控对象的协议类型
    var monitorTimeIndex; // 车辆树右键菜单 监听时间初始
    var listenInterval; // 监听定时器对象
    var audioListenSocket; // 监听 - 音频实例对象
    var haeundaeChannelNum; // 云台功能使用车辆通道号
    var speedZoomParameter; // 云台速度快慢值
    var subScribeLatestInfoMap = new Common.map();
    var currentMonitorId = []; // 保存订阅监控对象id
    var tree = {
        /**
         * 初始化时获取参数
         */
        init: function () {
            urlRewriteVideoId = Common.getAddressUrl('videoId');
            urlRewriteVideoName = Common.getAddressUrl('videoName');

            /**
             * 获取当前用户下监控对象数量
             * 并加载相应的监控对象树
             */
            this.getMonitoringNumber();
            /**
             * 视频画面控制初始化
             */
            this.videoScreenControlFn();
            /**
             * 视频画面控制关闭监听
             */
            this.listenVideoScreenControlOff();
            /**
             * 监听声音控制
             */
            this.controlAudioVoice();
        },
        /**
         * 获取当前用户下监控对象数量
         */
        getMonitoringNumber: function () {
            var $this = this;
            $('#treeLoading').show();
            json_ajax(
                'POST',
                '/clbs/m/functionconfig/fence/bindfence/getStatistical',
                'json',
                true,
                {webType: 2},
                function (data) {
                    var url;
                    var param;
                    allObjNum = data.obj.allV;
                    diyueall = data.obj.vehicleIdArray;
                    /**
                     * 监控对象树初始化 - 加载不同数据
                     */
                    if (urlRewriteVideoId) {
                        $('#search_condition').val(urlRewriteVideoName);
                        var queryType = $('#searchType').val();
                        url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTreeFuzzy';
                        param = {queryParam: urlRewriteVideoName, queryType: queryType, webType: 2, type: 1};
                    } else if (allObjNum <= 300) {
                        url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTree';
                        param = {webType: 2};
                    } else {
                        url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
                    }
                    $this.initZtree(url, param);
                }
            );
        },
        /**
         * 监控对象树加载
         */
        initZtree: function (url, otherParam) {
            var setting = {
                async: {
                    url: url,
                    type: 'post',
                    enable: true,
                    autoParam: ['id'],
                    otherParam: otherParam,
                    dataType: "json",
                    dataFilter: this.ajaxDataFilter.bind(this)
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree,
                    aliasesClass: aliasesClass,
                    addHoverDom: this.addHoverDom.bind(this),
                    removeHoverDom: this.removeHoverDom.bind(this),
                    addDiyDom: this.addDiyDom.bind(this),
                },
                check: {
                    enable: false
                },
                data: {
                    simpleData: {
                        enable: true
                    },
                    key: {
                        title: 'name'
                    }
                },
                callback: {
                    onAsyncError: this.zTreeOnAsyncError.bind(this),
                    beforeAsync: this.beforeAsync.bind(this),
                    onClick: this.onClickV.bind(this),
                    beforeDblClick: this.zTreeBeforeDblClick.bind(this),
                    onDblClick: this.onDbClickV.bind(this),
                    onAsyncSuccess: this.zTreeOnAsyncSuccess.bind(this),
                    onExpand: this.zTreeOnExpand.bind(this),
                    onRightClick: this.zTreeShowRightMenu.bind(this),
                    onNodeCreated: this.zTreeOnNodeCreated.bind(this)
                }
            };
            $.fn.zTree.init($('#vTreeList'), setting);
        },
        /**
         * ztree加载数据预处理事件
         */
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var rData = responseData;
            if (isUngzip) {
                rData = JSON.parse(ungzip(responseData));
            }
            if (currentTreeType === 'offline') {// 离线树,过滤离线监控对象数为0的分组
                rData = rData.filter(function (currentValue) {
                    return !(currentValue.type === 'assignment' && currentValue.offLine === 0);
                });
                rData = filterQueryResult(rData, null, 'assignment');
            }
            if (currentTreeType === 'online' || $('#search_condition').val() !== '') {
                rData = filterQueryResult(rData, null);
            }
            if (rData.length === 0) {
                $('#treeLoading span').text(treeSearchState ? '未找到查询对象' : '您没有数据');
                $('#treeLoading').show().find('i').hide();
            } else {
                $('#treeLoading').hide();
            }
            return rData;
        },
        /**
         * 监控对象树-鼠标移入显示
         */
        addHoverDom: function (treeId, treeNode) {
            if (['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1) {
                var $this = this;
                var aObj = $('#' + treeNode.tId + '_span');
                var online_state = aObj.hasClass('obj_select_online');
                var heartBeat_state = aObj.hasClass('obj_select_heartbeat');
                if (online_state || heartBeat_state) {
                    var btn = $('#deleteBtn_' + treeNode.id);
                    if (btn.length > 0) return;
                    btn = $("<span class='button video_close_ico' id='deleteBtn_" + treeNode.id + "' title='关闭'></span>");
                    $('#' + treeNode.tId + '_span').after(btn);
                    btn.on('click', function () {
                        $this.unsubscribeLocation(treeNode.id);
                        VideoModule.video.videoCloseFun(treeNode.id, 'monitor');
                    });
                }
            }
        },
        /**
         * 监控对象树-鼠标移出事件
         */
        removeHoverDom: function (treeId, treeNode) {
            if (['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1) {
                $('#deleteBtn_' + treeNode.id).remove();
            }
        },
        /**
         * 在节点上固定显示自定义控件
         */
        addDiyDom: function (treeId, treeNode) {
            if (['vehicle', 'people', 'thing'].indexOf(treeNode.type) !== -1 && treeNode.status && treeNode.status !== 3) {
                var url = treeNode.acc === 1 ? "../../resources/img/videoPrompt/accgreen.svg" : "../../resources/img/videoPrompt/accgray.svg";
                var str = "<span><img style='height: 14px;vertical-align: top' src='"+ url +"'></span>";
                $("#" + treeNode.tId + "_a").append(str);
            }
        },
        /**
         * 监控对象树-加载失败事件
         */
        zTreeOnAsyncError: function (event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
            $('#treeLoading text').text(treeSearchState ? '查询失败' : '加载失败');
            $('#treeLoading').show().find('i').hide();
            treeSearchState = false;
        },
        /**
         * 异步加载之前事件
         */
        beforeAsync: function () {
            return ztreeAsyncStatus;
        },
        /**
         * 监控对象树-单击事件
         */
        onClickV: function (event, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            treeObj.cancelSelectedNode(treeNode);
            var nodeSpan = $('#' + treeNode.tId + '_span');
            if (['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1) {
                $('.node_name').removeClass('obj_select');
                nodeSpan.addClass('obj_select');
                if (Map.map.getCurrentMonitorId() !== treeNode.id) {
                    Map.map.monitorHide(Map.map.getCurrentMonitorId());
                    Map.map.monitorShow(treeNode.id);
                    if (Map.map.isSubscribe(treeNode.id)) {
                        Map.map.setCurrentMonitorId(treeNode.id);
                    }
                    /**
                     * 切换显示监控对象信息
                     */
                    var data = subScribeLatestInfoMap.get(treeNode.id);
                    if (data) {
                        LocationInfo.subscribeInfo.setInfo(data);
                    }
                }
            }
        },
        /**
         * 监控对象树-双击前事件
         */
        zTreeBeforeDblClick: function (event, treeNode) {
            if (treeNode !== null) {
                var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
                treeObj.cancelSelectedNode(treeNode);
                if (['vehicle', 'thing', 'people', 'channel'].indexOf(treeNode.type) !== -1) {
                    // 保存视频抽查统计记录
                    json_ajax(
                        'POST',
                        '/clbs/realTimeVideo/video/saveVideoSpotCheckRecord',
                        'json',
                        true,
                        {
                            vehicleId: treeNode.type === 'channel' ? treeNode.vehicleId : treeNode.id,
                        },
                    );
                    var id = treeNode.type === 'channel' ?  treeNode.parentTId : treeNode.tId;
                    var icoClass = $('#' + id + '_ico').attr('class');
                    if (icoClass !== null && icoClass !== undefined
                        && icoClass.indexOf('vehicleSkin') === -1
                        && icoClass.indexOf('peopleSkin') === -1
                        && icoClass.indexOf('icon-offline') === -1) {
                        return true;
                    } else {
                        json_ajax(
                            'POST',
                            '/clbs/realTimeVideo/video/saveVideoInspectionRecord',
                            'json',
                            true,
                            {
                                vehicleId: treeNode.type === 'channel' ? treeNode.vehicleId : treeNode.id,
                                playStatus: 1,
                                failReason: 1,
                            }
                        );
                    }
                }
                layer.msg('监控对象离线');
                return false;
            }
        },
        /**
         * 监控对象树-双击事件
         */
        onDbClickV: function (event, treeId, treeNode) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            var isMonitor = ['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1;
            var tid = isMonitor ? treeNode.tId : treeNode.parentTId;
            var id = isMonitor ? treeNode.id : treeNode.vehicleId;
            var nodeSpan = $('#' + tid + '_span');
            var onlineState = nodeSpan.hasClass('obj_select_online');
            var heartbeatState = nodeSpan.hasClass('obj_select_heartbeat');
            $('.node_name').removeClass('obj_select');
            nodeSpan.addClass('obj_select');
            selectNodeProtocol = treeNode.deviceType;
            /**
             * 判断该监控对象是否在线
             * 对在线的监控对象的设置选中状态
             */
            if (!(onlineState || heartbeatState)) {
                var nodes = treeObj.getNodesByParam('id', id, null);
                if (nodes) {
                    for (var i = 0; i < nodes.length; i += 1) {
                        var node = nodes[i];
                        $('#' + node.tId + '_span').addClass('obj_select_online');
                    }
                    ;
                }
                ;
            }

            /**
             * 取消订阅已订阅过的监控对象的位置信息和视频
             */
            if (treeNode.type !== 'channel') {
                for (var i = 0; i < currentMonitorId.length; i += 1) {
                    if (currentMonitorId[i] !== id) {
                        // var currentNode = treeObj.getNodeByParam('id', currentMonitorId[i], null);
                        this.cancelSubscribe(currentMonitorId[i]);
                    }
                }
                currentMonitorId = [];
            }

            if (currentMonitorId.indexOf(id) === -1) {
                currentMonitorId.push(id);
            }

            /**
             * 如果当前显示监控对象与双击监控对象id不一样
             * 就隐藏当前地图上显示的监控对象
             */
            if (Map.map.getCurrentMonitorId() !== id) {
                Map.map.monitorHide(Map.map.getCurrentMonitorId());
                Map.map.monitorShow(id);
                Map.map.setCurrentMonitorId(id);
            }
            this.subscribeLocation(treeNode);

            /**
             *  监控对象下添加视频通道号，以便进行视频订阅
             */
            this.getMonitorChannel(treeNode);
        },
        /**
         * 监控对象树-加载成功事件
         */
        zTreeOnAsyncSuccess: function () {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            var nodes = treeObj.transformToArray(treeObj.getNodes());
            if (nodes.length > 0) {
                $('#treeLoading').hide();
            }
            ztreeAsyncStatus = false;
            treeSearchState = false;
            /**
             * 监控对象树加载为在线类型，即全部展开
             */
            if (currentTreeType === 'online' || currentTreeType === 'search') {

                this.expandNodes(treeObj, nodes);
            }
            this.alarmJump();
            /**
             * 已经订阅的监控对象，在切换重新加载监控对象树后
             * 对其进行颜色改变
             */
            this.reloadMonitorStatus();

            /**
             * 实时监控跳转到实时视频
             * 订阅对应监控对象实时视频
             */
            if (urlRewriteVideoId) {
                this.expandNodes(treeObj, nodes);
                var treeNode = treeObj.getNodeByParam('id', urlRewriteVideoId, null);
                if (treeNode != null) {
                    urlRewriteVideoId = null;
                    urlRewriteVideoName = null;
                    this.onDbClickV(null, null, treeNode);
                }
            }
        },
        /**
         * 更新监控对象状态
         */
        reloadMonitorStatus: function () {
            var keys = Map.map.getSubscribeInfoMap().keys();
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            for (let i = 0; i < keys.length; i += 1) {
                var nodes = treeObj.getNodesByParam('id', keys[i], null);
                for (let j = 0; j < nodes.length; j += 1) {
                    var node = nodes[j];
                    $('#' + node.tId + '_span').addClass('obj_select_online');
                }
            }

        },
        /**
         *  展开组织树节点
         */
        expandNodes: function (treeObj, nodes) {
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                if (node.type === 'assignment' && node.children) {
                    treeObj.expandNode(node, true, false, false, true);
                }
            }
        },
        /**
         * 监控对象树-节点展开事件
         */
        zTreeOnExpand: function (event, treeId, treeNode) {
            var $this = this;
            if (treeNode.type === 'assignment') {
                if (treeNode.children === undefined) {
                    /**
                     * 获取分组下监控对象
                     */
                    var data = {assignmentId: treeNode.id, isChecked: true, monitorType: 'monitor', webType: 2};
                    json_ajax(
                        'POST',
                        '/clbs/m/functionconfig/fence/bindfence/new/putMonitorByAssign',
                        'json',
                        true,
                        data,
                        function (info) {
                            $this.addNodes(info, treeNode, false);
                            /**
                             * 订阅监控对象-状态信息
                             */
                            var vehicleIds = treeNode.children.map(function (item) {
                                return {vehicleID: item.id};
                            });
                            $this.subscribeStatus(vehicleIds);
                        }
                    );
                } else {
                    /**
                     * 订阅监控对象-状态信息
                     */
                    var vehicleIds = treeNode.children.map(function (item) {
                        return {vehicleID: item.id};
                    });
                    this.subscribeStatus(vehicleIds);
                }
            } else if (['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1 && treeNode.children === undefined) { // 获取监控对象的逻辑通道号
                data = {vehicleId: treeNode.id, isChecked: false};
                json_ajax(
                    'POST',
                    '/clbs/realTimeVideo/video/getChannels',
                    'json',
                    true,
                    data,
                    function (info) {
                        $this.addNodes(info, treeNode, false);
                        $this.updateChannelStatus(treeNode.id);
                    }
                );
            }
        },
        /**
         * 监控对象树-节点添加
         */
        addNodes: function (data, treeNode, flag) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            treeObj.addNodes(treeNode, JSON.parse(ungzip(data.msg)), flag);
        },
        /**
         * 展开监控对象加载通道号
         * 根据已订阅的视频通道进行图标替换
         */
        updateChannelStatus: function (monitorId) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            var nodes = treeObj.getNodesByParam('id', monitorId, null);
            var subscribeVideoModule = VideoModule.video.getSubscribeVideoModule();
            for (var i = 0; i < nodes.length; i += 1) {
                var channelNodes = nodes[i].children;
                if (channelNodes) {
                    for (var j = 0; j < channelNodes.length; j++) {
                        var id = monitorId + '-' + channelNodes[j].logicChannel;
                        if (subscribeVideoModule.has(id)) {
                            channelNodes[j].iconSkin = 'btnImage channel-subscribe';
                            treeObj.updateNode(channelNodes[j]);
                        }
                    }
                }

            }
        },
        /**
         * 订阅监控对象-状态信息
         */
        subscribeStatus: function (vehicleIds) {
            var requestStrS = {
                desc: {
                    MsgId: 40964,
                    UserName: $('#userName').text(),
                },
                data: vehicleIds
            };
            webSocket.subscribe(
                headers,
                '/user/topic/cachestatus',
                this.subscribeStatusCallback.bind(this),
                '/app/vehicle/subscribeCacheStatusNew',
                requestStrS
            );
        },
        /**
         * 订阅状态信息回调事件
         * 用于更新监控对象图标
         */
        subscribeStatusCallback: function (msg) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            var data = $.parseJSON(msg.body);
            if (data.desc.msgID === 39321) {
                var d = data.data;
                for (var i = 0; i < d.length; i += 1) {
                    var info = d[i];
                    var nodes = treeObj.getNodesByParam('id', info.vehicleId, null);
                    if (nodes) {
                        for (var j = 0; j < nodes.length; j += 1) {
                            var node = nodes[j];
                            this.updateTreeDom(treeObj, node, info.vehicleStatus);
                        }
                    }
                }
            } else if (data.desc.msgID === 34952 || data.desc.msgID === 30583) {
                var d = data.data;
                var nodes = treeObj.getNodesByParam('id', d[0].vehicleId, null);
                if (nodes) {
                    for (var i = 0; i < nodes.length; i += 1) {
                        var node = nodes[i];
                        this.updateTreeDom(treeObj, node, d[0].vehicleStatus);
                    }
                }
            }
        },
        /**
         * 更新监控对象图标
         */
        updateTreeDom: function (treeObj, node, type) {
            if (['vehicle', 'thing', 'people'].indexOf(node.type) !== -1) {
                if (type === 3) {
                    node.iconSkin = 'btnImage icon-offline';
                } else if (type === 11) {
                    node.iconSkin = 'btnImage icon-heartbeat';
                } else {
                    node.iconSkin = 'icon-online';
                }
                treeObj.updateNode(node);
            }
        },
        /**
         * 监控对象树-右键事件
         */
        zTreeShowRightMenu: function (event, treeId, treeNode) {
            var $this = this;
            if (['vehicle', 'thing', 'people'].indexOf(treeNode.type) !== -1) {
                $('#rMenu').css('width', '143px');
                if (treeNode.iconSkin !== 'vehicleSkin') {
                    var index = $('#' + treeNode.tId + '_ico').attr('class').indexOf('icon-online');
                    var type;
                    if (index !== -1) {
                        type = '在线'
                    } else {
                        type = '心跳'
                    }
                    /**
                     * 获取用户名
                     */
                    var userName = $('#userName').text();
                    /**
                     * 获取逻辑通道号 0 音视频、 1 音频、 2 视频
                     */
                    var channelNumType;
                    var logicChannel;
                    var streamType;
                    /**
                     * 判断当前监控对象下是否加载了通道号信息
                     */
                    if (!treeNode.children) {
                        var url = '/clbs/realTimeVideo/video/getChannels';
                        var data = {vehicleId: treeNode.id, isChecked: false};
                        json_ajax('POST', url, 'json', false, data, function (info) {
                            $this.addNodes(info, treeNode, true);
                            $this.updateChannelStatus(treeNode.id);
                        });
                    }

                    for (var i = 0; i < treeNode.children.length; i++) {
                        //优选获取音频逻辑通道号
                        if (treeNode.children[i].channelType === 1) {
                            logicChannel = treeNode.children[i].logicChannel;
                            streamType = treeNode.children[i].streamType;
                            channelNumType = 1;
                            break;
                        }
                    }

                    /**
                     * 参数：
                     * vehicleId  车辆id
                     * clientX  x轴坐标
                     * clientY  y轴坐标
                     * status 监控对象类型
                     * monitorName  车牌号
                     * channelNum  逻辑通道号
                     * simcardNumber 终端手机号
                     * userName  用户名
                     * plateColor 车牌颜色
                     * channelNumType 通道号类型
                     * streamType 协议类型
                     * deviceType 终端类型
                     */
                    var D = {
                        vehicleId: treeNode.id,
                        clientX: event.clientX,
                        clientY: event.clientY,
                        status: type,
                        monitorName: treeNode.name,
                        channelNum: logicChannel,
                        simcardNumber: treeNode.simcardNumber,
                        userName: userName,
                        plateColor: treeNode.plateColor,
                        channelNumType: channelNumType,
                        streamType: streamType,
                        deviceType: treeNode.deviceType,
                        acc: treeNode.acc,
                        status: treeNode.status,
                    };
                    this.createMenu(D);
                }
            }
        },
        /**
         * 创建监控对象右键菜单
         * vehicleId  车辆id
         * clientX  x轴坐标
         * clientY  y轴坐标
         * status 监控对象类型
         * monitorName  车牌号
         * channelNum  逻辑通道号
         * simcardNumber 终端手机号
         * userName  用户名
         * plateColor 车牌颜色
         * channelNumType 通道号类型
         * streamType 协议类型
         * deviceType 终端类型
         */
        createMenu: function (data) {
            var monitorName = data.monitorName;
            if (monitorName) {
                monitorName = encodeURI(monitorName);
            }
            var html = '<div class="col-md-12" id="treeRightMenu-l" style="padding:0">';

            if (!data.channelNum) {
                html += '<a href="javascript:void(0)" onClick="layer.msg(\'当前监控对象没有音频通道号，请在设置通道号指令里设置音频类型的通道并下发 \')">对讲</a>';
            } else {
                html += '<a href="/clbs/realTimeVideo/video/talkBackPage?' +
                    'brand=' + monitorName + '&' +
                    'vehicleId=' + data.vehicleId + '&' +
                    'mobile=' + data.simcardNumber + '&' +
                    'streamType=' + data.streamType + '&' +
                    'channelNumber=' + data.channelNum + '&' +
                    'vehicleColor=' + data.plateColor + '&' +
                    'channelNumType=' + data.channelNumType + '" ' +
                    'data-toggle="modal" data-target="#commonSmWin">对讲</a>';
            }
            ;

            html += '<a id="monitorListening">监听</a>';
            html += '<a href="/clbs/realTimeVideo/video/broadCastPage" data-toggle="modal" data-target="#commonSmWin">广播</a>';
            if (data.status !== 3 && data.acc === 0) {
                html += '<a id="monitorWakeup">唤醒</a>';
            }
            html += '<a href="/clbs/v/monitoring/getSendTextByBatchPage_' + data.deviceType + '" data-toggle="modal" data-target="#commonSmWin">批量文本信息下发</a>';
            html += '<a id="settingChannelLink" data-type="' + data.deviceType + '" ' +
                'href="/clbs/realTimeVideo/video/channelSettingPage?vehicleId=' + data.vehicleId + '&brand=' + monitorName + '" ' +
                'data-toggle="modal" data-target="#commonWin">设置通道号</a>';
            html += '<a id="videoProperties">查询音视频属性</a>';

            if (data.deviceType !== '23') {
                html += '<a href="/clbs/realTimeVideo/video/vedioSleepSettingPage?' +
                    'vehicleId=' + data.vehicleId + '&' +
                    'brand=' + monitorName + '" data-toggle="modal" data-target="#commonWin">设置休眠唤醒</a>';
            }
            if (data.type === '心跳' && data.deviceType !== '23') {
                html += '<p id="videoSleepBtn"><a>休眠唤醒</a></p></div>';
            } else {
                html += '</div>';
            }
            $('#rMenu').html(html);
            this.menuBindFn(data);
            window.currentRightClickVehicleId = data.vehicleId;
            this.rMenuUlShowOrPosition(data.clientX, data.clientY);
        },
        /**
         * 监控对象右键菜单绑定点击事件
         */
        menuBindFn: function (data) {
            var $this = this;
            $('#monitorListening').unbind('click').bind('click', function () {
                $this.rightClickMonitorFn(data);
            })
            $('#videoProperties').unbind('click').bind('click', function () {
                $this.searchAudioAndVideoDataFn(data.vehicleId);
            });
            $('#videoSleepBtn').unbind('click').bind('click', function () {
                $this.videoSleep(data.vehicleId, data.monitorName);
            });
            $('#monitorWakeup').unbind('click').bind('click', function () {
                $this.monitorWakeupFn(data.vehicleId);
            })
        },
        /**
         * 右键菜单 - 监听单击事件
         */
        rightClickMonitorFn: function (data) {
            var $this = this;
            if (!data.channelNum) {
                layer.msg("当前监控对象没有音频通道号，请在设置通道号指令里设置音频类型的通道并下发！");
            } else {
                /**
                 * 菜单隐藏
                 */
                $('#rMenu').css({visibility: 'hidden'});
                var list = {
                    mobile: data.simcardNumber,
                    channelNum: data.channelNum,
                    vehicleId: data.vehicleId,
                    channelType: data.channelNumType,
                    brand: data.monitorName,
                    vehicleColor: data.plateColor,
                    streamType: data.streamType,
                };

                json_ajax('POST', '/clbs/v/monitoring/audioAndVideoParameters/' + data.vehicleId, 'json', true, null, function (d) {
                    if (d.success) {
                        $this.listeningSuccess(d.obj, list);
                    }
                });
            }
        },
        /**
         * 监听下发后回调方法
         */
        listeningSuccess: function (data, list) {
            $("#rightClickMonitor").removeClass("hidden");
            /**
             * 为按钮添加类样式 区分下发成功及失败
             */
            $("#monitorIco").removeAttr("class");
            $("#monitorIco").addClass("monitor-success");
            Table.table.logFindCilck();
            this.audioListening(data, list);
        },
        /**
         * 监听
         */
        audioListening: function (audioData, list) {
            var $this = this;
            var simCardLength = list.mobile.length;
            var sNumber = list.mobile;
            if (simCardLength < 12) {
                for (var i = 0; i < 12 - simCardLength; i++) {
                    sNumber = '0' + sNumber;
                }
            }
            var protocol = 'ws://';
            if (document.location.protocol === 'https:') {
                protocol = 'wss://';
            }
            var url = protocol + videoRequestUrl + ':' + audioRequestPort + '/' + sNumber + '/' + list.channelNum + '/2';

            var data = {
                vehicleId: list.vehicleId,
                simcardNumber: sNumber,
                channelNumber: list.channelNum,
                sampleRate: audioData.samplingRateStr || 8000,
                channelCount: audioData.vocalTractStr || 0,
                audioFormat: audioData.audioFormatStr,
                playType: 'UP_WAY',
                dataType: 3,
                userID: audioData.userUuid,
                deviceID: audioData.deviceId,
                streamType: 1,
                deviceType: audioData.deviceType,
            };

            audioListenSocket = new RTPMediaPlayer({
                url: url,
                type: 'UP_WAY',
                audioEnabled: true,
                videoEnabled: false,
                recordEnabled: false,
                data: data,
                onMessage: function($data, $msg) {
                    var info = JSON.parse($msg);
                    if (info.data.msgBody.code == -1004 ||
                        info.data.msgBody.code == -1005 ||
                        info.data.msgBody.code == -1006 ||
                        info.data.msgBody.code == -1008
                    ) {
                        layer.msg(info.data.msgBody.msg);
                    }
                },
                socketOpenFun: function ($data, $$this) {
                    var setting = {
                        vehicleId: $data.vehicleId,                         // 车辆ID
                        simcardNumber: $data.simcardNumber,                 // sim卡号
                        channelNumber: JSON.stringify($data.channelNumber), // 通道号
                        sampleRate: JSON.stringify($data.sampleRate),       // 采样率
                        channelCount: JSON.stringify($data.channelCount),   // 声道数
                        audioFormat: $data.audioFormat,                     // 编码格式
                        playType: $data.playType,                           // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
                        dataType: JSON.stringify($data.dataType),           // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
                        userID: $data.userID,                               // 用户ID
                        deviceID: $data.deviceID,                           // 终端ID
                        streamType: JSON.stringify($data.streamType),       // 码流类型0：主码流，1：子码流
                        deviceType: $data.deviceType,
                    };
                    console.log('setting', setting);
                    $$this.play(setting);
                },
                openAudioSuccess: function () {
                    $this.monitorAudioSuccess();
                },
                // socket关闭成功
                socketCloseFun: function ($state) {
                    $("#monitorIco").addClass("monitor-success");
                    $this.listenBtnClick();
                },
            });
            // 为监听按钮绑定点击事件
            $("#monitorIco").unbind('click').bind("click", function () {
                $this.listenBtnClick()
            });
        },
        /**
         * 监听连接成功
         */
        monitorAudioSuccess: function () {
            $('#rightClickMonitor').addClass('audio-listen');
            //替换暂停图标
            $("#monitorIco").attr("src", "../../resources/img/pause.png");
            //监听时间
            monitorTimeIndex = 1;
            listenInterval = setInterval(function () {
                $("#monitorText").html("正在监听 时间：" + (monitorTimeIndex++) + "秒");
            }, 1000);
        },
        listenBtnClick: function () {
            if ($("#monitorIco").hasClass("monitor-success")) {
                $('#rightClickMonitor').removeClass('audio-listen');
                // 关闭socket
                if(audioListenSocket) {
                    audioListenSocket.closeSocket();
                    audioListenSocket.cmdCloseVideo();
                }
                //清空监听时间
                clearInterval(listenInterval);
                $('#monitorText').html('连接中...');
                monitorTimeIndex = 1;
                //隐藏
                $('#rightClickMonitor').addClass('hidden');
                $('#monitorIco').removeClass('monitor-success');
                Table.table.logFindCilck();
            }
        },
        /**
         * 监控对象树-DOM节点创建成功事件
         */
        zTreeOnNodeCreated: function (event, treeId, treeNode) {

        },
        /**
         * 联动报警跳转
         */
        alarmJump: function () {
            var vId = Common.getAddressUrl('vid');
            var pId = Common.getAddressUrl('pid');
            if (vId && pId) {
                var param = {'id': vId, 'pid': pId};
                this.subscribeLocation(param);
            }
        },
        /**
         * 监控对象位置信息订阅
         */
        subscribeLocation: function (param) {
            var userName = $('#userName').text();
            var id = param.vehicleId ? param.vehicleId : param.id;
            var requestStrS = {
                desc: {
                    MsgId: 40964,
                    UserName: userName
                },
                data: [id]
            };
            webSocket.subscribe(headers, '/user/topic/location', this.locationCallback.bind(this), '/app/location/subscribe', requestStrS);

            /**
             * 联动报警车辆树订阅
             */
            if (!param.iconSkin) {
                var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
                var treeParentNode = treeObj.getNodeByParam('id', param.pid, null);
                treeObj.expandNode(treeParentNode, true, false, false, true); // 展开分组
                this.alarmGetZtreeDom(param.id);
            }
        },
        /**
         * 订阅位置信息回调事件
         */
        locationCallback: function (msg) {
            var data = JSON.parse(msg.body);
            if (data.desc !== 'neverOnline') {
                if (Map.map.getCurrentMonitorId() === data.data.msgBody.monitorInfo.monitorId) {
                    LocationInfo.subscribeInfo.setInfo(data);
                }
                subScribeLatestInfoMap.set(data.data.msgBody.monitorInfo.monitorId, data);
                Map.map.setMarkersData(data);
            }
        },
        /**
         * 联动报警获取节点
         */
        alarmGetZtreeDom: function (id) {
            var treeObj = $.fn.zTree.getZTreeObj('vTreeList');
            var treeNode = treeObj.getNodeByParam('id', id, null);
            if (treeNode != null) {
                this.onDbClickV(null, null, treeNode); // 订阅监控对象
            } else {
                console.error('err：监控对象树节点未获取');
            }
        },
        /**
         *  监控对象树重新加载
         *  type = 7 不在线
         *  type = 1 在线
         *  type = 9 心跳
         */
        ztreeReload: function (event) {
            $('#treeLoading span').text('加载中，请稍候');
            $('#treeLoading').show().find('i').show();
            treeSearchState = false;
            $('#search_condition').val('');
            var type = event.data.type;
            var url;
            var param = null;
            if (type === 7) {
                isUngzip = true;
                if (allObjNum <= 300) {
                    url = '/clbs/m/basicinfo/monitoring/vehicle/new/treeStateInfo';
                    param = {webType: 2, type: type};
                } else {
                    url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
                }
                currentTreeType = 'offline';
            } else if (type === 1 || type === 9) {
                isUngzip = false;
                url = '/clbs/m/basicinfo/monitoring/vehicle/new/treeStateInfo';
                param = {webType: 2, type: type};
                currentTreeType = 'online';
            }
            ztreeAsyncStatus = true;
            this.initZtree(url, param);
        },
        /**
         * 监控对象树-模糊查询
         */
        fuzzySearch: function () {
            treeSearchState = true;
            $('#treeLoading span').text('正在查询，请稍候');
            $('#treeLoading').show().find('i').show();
            var value = $('#search_condition').val();
            var url;
            var param;
            isUngzip = true;
            ztreeAsyncStatus = true;
            var queryType = $('#searchType').val();
            if (value !== '') {
                url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTreeFuzzy';
                param = {queryParam: value, queryType: queryType, webType: 2, type: 1};
            } else {
                if (allObjNum <= 300) {
                    url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTree';
                    param = {webType: 2};
                } else {
                    url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
                }
            }
            currentTreeType = 'search';
            this.initZtree(url, param);
        },
        /**
         *  监控对象树-模糊查询-防抖
         */
        debounce: function () {
            var $this = this;
            if (searchTimeout) {
                clearTimeout(searchTimeout);
            }
            searchTimeout = setTimeout(function () {
                $this.fuzzySearch();
            }, 500);
        },
        /**
         * 监控对象树-刷新
         */
        refreshTree: function () {
            treeSearchState = true;
            $('#treeLoading span').text('加载中，请稍候');
            $('#treeLoading').show().find('i').show();
            $('#search_condition').val('');
            var url;
            var param = null;
            isUngzip = true;
            ztreeAsyncStatus = true;
            if (allObjNum <= 300) {
                url = '/clbs/m/functionconfig/fence/bindfence/new/monitorTree';
                param = {webType: 2};
            } else {
                url = '/clbs/m/functionconfig/fence/bindfence/bigDataMonitorTree';
            }
            currentTreeType = 'all';
            this.initZtree(url, param);
        },
        /**
         * 监控对象树-别名显示
         */
        showAliases: function () {
            if ($('#showAliases input').is(':checked')) {
                $('.aliasesStyle').show();
                aliasesClass = 'aliasesStyle aliasesShow';
            } else {
                $('.aliasesStyle').hide();
                aliasesClass = 'aliasesStyle';
            }
        },
        /**
         * 返回用户权限下车辆id集合
         */
        getUserMonitorIds: function () {
            return diyueall;
        },
        /**
         * 右键菜单  - 查询音视频属性
         */
        searchAudioAndVideoDataFn: function (vId) {
            //菜单隐藏
            $('#rMenu').css({"visibility": "hidden"});
            //请求查询音视频属性下发接口
            var url = "/clbs/realTimeVideo/video/sendParamCommand";
            var parameter = {vehicleId: vId, orderType: 8};
            json_ajax("POST", url, "json", true, parameter, function (data) {
                if (data.success) {
                    Table.table.logFindCilck();
                }
            });
        },
        /**
         * 休眠唤醒
         */
        videoSleep: function (id, vName) {
            var url = '/clbs/realTimeVideo/videoSetting/getVideoSleepParam';
            var data = {vehicleId: id};
            json_ajax("POST", url, "json", true, data, function (info) {
                if (info.obj != null && info.obj.wakeupHandSign === 1) { // 手动唤醒启用
                    $('#videoSleepBtn').html('<a href="/clbs/realTimeVideo/video/vedioSleepPage?brand=' + vName + '&vehicleId=' + id + '" data-toggle="modal" data-target="#commonSmWin">休眠唤醒</a>')
                    setTimeout(function () {
                        $('#videoSleepBtn a').click();
                    }, 200);
                } else { // 停用
                    layer.msg('请在设置休眠唤醒指令里启用手动唤醒并下发');
                }
            });
        },
        /**
         * ztree 右键菜单定位
         */
        rMenuUlShowOrPosition: function (x, y) {
            $("#rMenu ul").show();
            if (y < 0) {
                $('#rMenu').css({top: (y - y) + 'px', left: (x + 35) + 'px', visibility: 'visible'});
            } else {
                $('#rMenu').css({top: (y) + 'px', left: (x + 35) + 'px', visibility: 'visible'});
            }
        },
        /**
         * 云台控制
         */
        cloudStationFn: function () {
            //车辆树是否订阅
            if (Map.map.getCurrentMonitorId()) {
                //视频是否选择
                if ($('#video-module div').hasClass('this-click')) {
                    var connectState = $('#connectState').attr('value');
                    if (connectState == 1) { // 云台连接打开
                        if ($(this).hasClass('video-yun-sett')) {
                            //图标改变
                            $(this).removeClass('video-yun-sett').addClass('video-yun-sett-check');
                            $('#haeundaeModal').modal('show');
                            //云台宽度
                            $('#haeundaeModal>.modal-dialog').css('width', '500px');
                            $('#haeundaeModal .modal-dialog').css('top', ($(window).height() - 308) / 2 + "px");//位置
                        } else {
                            //图标改变
                            $(this).removeClass('video-yun-sett-check').addClass('video-yun-sett');
                        }
                    } else {
                        layer.msg('该通道号云台未连接');
                    }

                } else {
                    layer.msg("请先选中一个音视频窗口");
                }
            } else {
                layer.msg("请双击订阅监控对象");
            }
        },
        /**
         * 视频画面设置显示函数
         */
        videoDimmingFn: function () {
            /**
             * 车辆树是否订阅
             */
            if (Map.map.getCurrentMonitorId()) {
                var videoDimming = $("#videoDimming");
                if (videoDimming.hasClass("video-dimming")) {
                    videoDimming.removeClass("video-dimming");
                    videoDimming.addClass("video-dimming-check");
                    $("#videoScreenControl").modal("show");
                    $("#videoScreenControl .modal-dialog").css("top", ($(window).height() - 268) / 2 + "px");//位置
                } else {
                    videoDimming.removeClass("video-dimming-check");
                    videoDimming.addClass("video-dimming");
                }

            } else {
                layer.msg("请双击订阅监控对象");
            }
        },
        /**
         * 视频画面控制
         */
        videoScreenControlFn: function () {
            var $this = this;
            /**
             * 亮度
             */
            $('.nsBrightness').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    $('#brightnessVal').val(leftValue);
                    $this.doVideoScreenControl();
                }
            });
            /**
             * 色度
             */
            $('.nsChroma').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    $('#chromaVal').val(leftValue);
                    $this.doVideoScreenControl();
                }
            });
            /**
             * 对比度
             */
            $('.nsContrast').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    $('#contrastVal').val(leftValue);
                    $this.doVideoScreenControl();
                }
            });
            /**
             * 饱和度
             */
            $('.nsSaturation').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    $('#saturationVal').val(leftValue);
                    $this.doVideoScreenControl();
                }
            });
            /**
             * 音量
             */
            $('.nsVolume').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    $('#volumeVal').val(leftValue);
                    var data = VideoModule.video.getSubscribeVideoModule();
                    var keys = data.keys();
                    for (var i = 0; i < keys.length; i++) {
                        var obj = data.get(keys[i]);
                        obj.mediaPlayer.setAudioVoice(leftValue / 100);
                    }
                }
            });
        },
        /**
         * 监听视频画面控制模块关闭
         */
        listenVideoScreenControlOff: function () {
            $('#videoScreenControl').on('hidden.bs.modal', function () {
                var videoDimming = $("#videoDimming");
                videoDimming.removeClass("video-dimming-check");
                videoDimming.addClass("video-dimming");
            });
        },
        /**
         * 监听-声音大小控制
         */
        controlAudioVoice: function () {
            $('.audioVoice').nstSlider({
                "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                    var number = leftValue / 100;
                    if (audioListenSocket) {
                        audioListenSocket.setAudioVoice(number);
                    }
                }
            });
        },
        doVideoScreenControl: function () {
            var saturate = $('#saturationVal').val() / 51;
            var hue = $("#chromaVal").val();
            var brightness = $("#brightnessVal").val() / 255;
            var contrast = $("#contrastVal").val() / 51;
            var filters = "saturate(" + saturate + ") hue-rotate(" + hue + "deg) brightness(" + brightness + ") contrast(" + contrast + ")";
            var video = $("video");
            video.css('-webkit-filter', filters);
            video.css('-ms-filter', filters);
            video.css('-moz-filter', filters);
            video.css('-o-filter', filters);
            video.css('filter', filters);
        },
        /**
         * 云台关闭
         */
        cloudStationCloseFn: function () {
            $("#videoYunSett").removeClass("video-yun-sett-check").addClass("video-yun-sett");
            $("#haeundaeModal").modal("hide");
        },
        /**
         * 灯光开关监听函数
         */
        haeundaeLightCheckedFn: function () {
            var control;
            //开启
            if ($(this).is(":checked")) {
                layer.msg("灯光开启");
                control = 1;
            } else {
                layer.msg("灯光关闭");
                control = 0;
            }

            var type = 4; // 灯光
            var channelNum = haeundaeChannelNum; // 通道号
            var vehicleId = Map.map.getCurrentMonitorId();
            var url = "/clbs/cloudTerrace/sendParam";
            var data = {"vehicleId": vehicleId, "channelNum": channelNum, "type": type, "control": control};
            json_ajax("post", url, "json", true, data, function (data) {
                if (data.success) {
                    Table.table.logFindCilck();
                }
            });
        },
        /**
         * 雨刷开关监听函数
         */
        haeundaeWipersCheckedFn: function () {
            var control;
            //开启
            if ($(this).is(":checked")) {
                layer.msg("雨刷开启");
                control = 1;
            } else {
                layer.msg("雨刷关闭");
                control = 0;
            }

            var type = 3; // 雨刷
            var channelNum = haeundaeChannelNum; // 通道号
            var vehicleId = Map.map.getCurrentMonitorId();
            var url = "/clbs/cloudTerrace/sendParam";
            var data = {vehicleId: vehicleId, channelNum: channelNum, type: type, control: control};
            json_ajax("post", url, "json", false, data, function (data) {
                if (data.success) {
                    Table.table.logFindCilck();
                }
            });
        },
        /**
         * 变焦 变倍 光圈控制函数
         */
        zoomDoubleApertureFn: function () {
            var control; // 0:调大 1：调小
            var type; // 1 调焦 2 调光 5 变倍
            var clickId = $(this).attr("id");
            //变焦 加
            if (clickId === "zoomPlus") {
                layer.msg("变焦 加");
                type = 1;
                control = 0;

            }
            //变焦 减
            else if (clickId === "zoomLess") {
                layer.msg("变焦 减");
                type = 1;
                control = 1;
            }
            //变倍 加
            else if (clickId === "doublePlus") {
                layer.msg("变倍 加");
                type = 5;
                control = 0;
            }
            //变倍 减
            else if (clickId === "doubleLess") {
                layer.msg("变倍 减");
                type = 5;
                control = 1;
            }
            //光圈  加
            else if (clickId === "aperturePlus") {
                layer.msg("光圈  加");
                type = 2;
                control = 0;
            }
            //光圈 减
            else if (clickId === "apertureLess") {
                layer.msg("光圈 减");
                type = 2;
                control = 1;
            }
            if (type !== undefined && control !== undefined) {
                var channelNum = haeundaeChannelNum; // 通道号
                var vehicleId = Map.map.getCurrentMonitorId();
                var url = "/clbs/cloudTerrace/sendParam";
                var data = {"vehicleId": vehicleId, "channelNum": channelNum, "type": type, "control": control};
                json_ajax("post", url, "json", false, data, function (data) {
                    if (data.success) {
                        Table.table.logFindCilck();
                    }
                });
            }
        },
        /**
         * 云台控制摄像头方向函数
         */
        haeundaeCameraPathFn: function () {
            var direction = [];
            var _thisId = $(this).attr("id");
            //左上  1 & 3
            if (_thisId === "haeundaeLeftTop") {
                // 下发两次 上和左
                direction = [1, 3];
            }
            //上 1
            else if (_thisId === "haeundaeTop") {
                direction = [1];
            }
            //右上 1 & 4
            else if (_thisId === "haeundaeRightTop") {
                direction = [1, 4];
            }
            //左 3
            else if (_thisId === "haeundaeLeft") {
                direction = [3];
            }
            //中(停止) 0
            else if (_thisId === "haeundaeCenter") {
                direction = [0];
            }
            //右 4
            else if (_thisId === "haeundaeRight") {
                direction = [4];
            }
            //左下 2 & 3
            else if (_thisId === "haeundaeLeftBottom") {
                direction = [2, 3];
            }
            //下 2
            else if (_thisId === "haeundaeBottom") {
                direction = [2];
            }
            //右下 2 & 4
            else if (_thisId === "haeundaeRightBottom") {
                direction = [2, 4];
            }

            if (direction.length > 0) {
                /**
                 * 0:云台旋转 0x9301
                 * 1:云台调整焦距控制 0x9302
                 * 2:云台调整光圈控制 0x9303
                 * 3:云台雨刷控制 0x9304
                 * 4:红外补光控制 0x9305
                 * 5:云台变倍控制 0x9306
                 * @type {number}
                 */
                var type = 0; // 下发指令
                var speed = speedZoomParameter; // 旋转速度
                var channelNum = haeundaeChannelNum; // 通道号
                var vehicleId = Map.map.getCurrentMonitorId();
                var url = "/clbs/cloudTerrace/sendParam";
                for (var i = 0; i < direction.length; i++) {
                    // 组装数据
                    var control = direction[i]; // 方向
                    var data = {
                        "vehicleId": vehicleId,
                        "channelNum": channelNum,
                        "type": type,
                        "speed": speed,
                        "control": control
                    };
                    json_ajax("post", url, "json", false, data, function (data) {
                        if (data.success) {
                            Table.table.logFindCilck();
                        }
                    });
                }
            }
        },
        /**
         * 云台速度滑块控制函数
         */
        cloudSilderValueFn: function (_thisVal) {
            //获取云台速度变倍参数值
            speedZoomParameter = _thisVal;
        },
        /**
         * 请求监控对象下通道号并添加到对应监控对象底部
         */
        getMonitorChannel: function (treeNode) {
            var $this = this;
            /**
             * 判断双击对象是监控对象还是视频通道
             */
            if (treeNode.type !== 'channel') {
                /**
                 * 判断监控对象下是否已经加载通道号数据
                 * 若未加载，进行数据请求并将节点添加到监控对象下级
                 */
                if (!treeNode.children) {
                    var url = '/clbs/realTimeVideo/video/getChannels';
                    var data = {vehicleId: treeNode.id, isChecked: false};
                    json_ajax("POST", url, "json", true, data, function (info) {
                        $this.addNodes(info, treeNode, true);
                        VideoModule.video.subscribeVideoChannel(treeNode);
                    });
                } else {
                    VideoModule.video.subscribeVideoChannel(treeNode);
                }
            } else {
                VideoModule.video.subscribeVideoChannel(treeNode);
            }
        },
        /**
         * 取消位置信息订阅
         */
        unsubscribeLocation: function (id) {
            // 取消订阅位置信息
            var cancelStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $('#userName').text()
                },
                "data": [id]
            };

            webSocket.send('/app/location/unsubscribe', headers, cancelStrS);

            subScribeLatestInfoMap.remove(id);

            if (Map.map.getCurrentMonitorId() === id) {
                /**
                 * 清空订阅后位置信息内容
                 */
                LocationInfo.subscribeInfo.setInfo();
            }

            /**
             * 清空地图上对应监控对象标注物
             */
            Map.map.clearData(id);

            this.recoverMonitorNode(id);
        },
        /**
         * 监控对象节点恢复默认状态
         */
        recoverMonitorNode: function (id) {
            var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
            var nodes = treeObj.getNodesByParam('id', id, null);
            for (let i = 0; i < nodes.length; i += 1) {
                $('#' + nodes[i].tId + '_span').attr('class', 'node_name');
            }
        },
        /**
         * 视频通道节点恢复默认
         */
        recoverChannelNode: function (id, channel) {
            var treeObj = $.fn.zTree.getZTreeObj("vTreeList");
            var nodes = treeObj.getNodesByParam('id', id, null);
            for (let i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                if (node.children) {
                    for (let j = 0; j < node.children.length; j += 1) {
                        var channelNode = node.children[j];
                        if (channel === channelNode.logicChannel) {
                            channelNode.iconSkin = 'channelSkin';
                            treeObj.updateNode(channelNode);
                        }
                    }
                }
            }
        },
        setHaeundaeChannelNum: function (data) {
            haeundaeChannelNum = data;
        },
        getSelectNodeProtocol: function () {
            return selectNodeProtocol;
        },
        setVideoModule: function (module) {
            VideoModule = module;
        },
        /**
         * 唤醒点击响应事件
         */
        monitorWakeupFn: function (monitorId) {
            $('#wakeupModal').modal('show');
            $('#wakeupMonitorId').val(monitorId);
            $('#rMenu').css({visibility: 'hidden'});
        },
        /**
         * 唤醒提交
         */
        wakeupSubmitFn: function () {
            if (this.wakeupValidate()) {
                var wakeupTime = $('#wakeupTime').val();
                var monitorId = $('#wakeupMonitorId').val();
                var requestStrS = {
                    desc: {
                        UserName: $('#userName').text(),
                    },
                    data: {
                        monitorId: monitorId,
                        wakeUpDuration: wakeupTime,
                    }
                };
                webSocket.subscribe(
                    headers,
                    '/user/topic/device/wakeUp',
                    this.wakeupCallback.bind(this),
                    '/app/device/wakeUp',
                    requestStrS
                );
                this.wakeupCloseFn();
            }
        },
        /**
         * 唤醒验证
         */
        wakeupValidate: function () {
            return $('#wakeupForm').validate({
                rules: {
                    wakeupTime: {
                        required: true,
                        range: [0, 1000],
                    }
                },
                messages: {
                    wakeupTime: {
                        required: '请输入唤醒时间',
                        range: '可输入范围是：0-1000',
                    },
                },
            }).form();
        },
        /**
         * 唤醒关闭恢复默认值
         */
        wakeupCloseFn: function () {
            $('#wakeupModal').modal('hide');
            $('#wakeupTime').val(10);
        },
        /**
         * 唤醒下发回调事件
         */
        wakeupCallback: function (data) {
            layer.msg(data.body === '0' ? '唤醒成功' : '唤醒失败');
        },
        /**
         * 取消订阅
         */
        cancelSubscribe: function (id) {
            this.unsubscribeLocation(id);
            VideoModule.video.videoCloseFun(id, 'monitor');
        }
    };

    window.getGroupList = function (info, treeNode) {
        tree.addNodes(info, treeNode, false);
        tree.updateChannelStatus(treeNode.id);
    };

    window.searchZtree = function (monitorId, monitorName) {
        urlRewriteVideoId = monitorId;
        urlRewriteVideoName = monitorName;
        $('#search_condition').val(monitorName);
        tree.fuzzySearch.call(tree);
    }

    $(function () {
        $('#searchOffLine').on('click', {type: 7}, tree.ztreeReload.bind(tree));// 不在线
        $('#searchOnline').on('click', {type: 1}, tree.ztreeReload.bind(tree));// 在线
        $('#searchHeartBeat').on('click', {type: 9}, tree.ztreeReload.bind(tree));// 心跳
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id === 'search_condition') {
                tree.fuzzySearch.call(tree);
            }
        });
        $('#search_condition').on('input propertychange', tree.debounce.bind(tree));// 模糊查询
        $('#searchType').on('change', function () {
            if ($('#search_condition').val() !== '') {
                tree.fuzzySearch.call(tree);
            }
        });
        $('#refresh').on('click', tree.refreshTree.bind(tree));// 刷新树
        // 监控对象别名显示控制
        $('#showAliases').on('click', tree.showAliases.bind(tree));
        $("#videoYunSett").on("click", tree.cloudStationFn); // 云台控制
        $("#videoDimming").on("click", tree.videoDimmingFn); // 视频亮度
        $('#haeundaeModal').on('hidden.bs.modal', tree.cloudStationCloseFn);//云台窗口关闭后执行函数
        $("#haeundaeLight").on("click", tree.haeundaeLightCheckedFn);//灯光开关监听函数
        $("#haeundaeWipers").on("click", tree.haeundaeWipersCheckedFn);//雨刷开关监听函数
        $("#zoomPlus, #zoomLess, #doublePlus, #doubleLess, #aperturePlus, #apertureLess").on("click", tree.zoomDoubleApertureFn);//变焦 变倍 光圈控制函数
        $("#haeundaeLeftTop, #haeundaeTop, #haeundaeRightTop, #haeundaeLeft, #haeundaeCenter, #haeundaeRight, #haeundaeLeftBottom, #haeundaeBottom, #haeundaeRightBottom").on("click", tree.haeundaeCameraPathFn);//云台控制摄像头方向函
        $('.haeundaeNstSlider').nstSlider({
            "left_grip_selector": ".leftGrip", "value_changed_callback": function (cause, leftValue) {
                tree.cloudSilderValueFn(leftValue);//云台速度滑块
            }
        });
        $('#wakeupSubmit').on('click', tree.wakeupSubmitFn.bind(tree));
        $('#wakeupClose').on('click', tree.wakeupCloseFn.bind(tree));
    })

    return {
        tree: tree,
    };
});