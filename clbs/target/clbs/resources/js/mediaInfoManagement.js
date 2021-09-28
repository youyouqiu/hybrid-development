(function (window, $) {
    // var myTable;
    var mediaPathList = {};
    var nowIndex = 0;
    var mediaLen = 0;
    var zTreeIdJson = {};
    var wmvPaths;
    var url = "/clbs/resources/img/media/";
    var checkFlag = false; //判断组织节点是否是勾选操作
    var size;//当前权限监控对象数量
    var oldVehicleId;
    var vehicleId;

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询
    var ifAllCheck = true; //刚进入页面小于100自动勾选
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;
    var curEditBtn = null;

    mediaInfoManagement = {
        initTable: function () {
            //表格列定义
            var columnDefs = [{
                //第一列，用来显示序号
                "searchable": false,
                "orderable": false,
                "targets": 0
            }];
            var columns = [{
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            }, {
                //操作
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var html = '<button type="button" onclick="mediaInfoManagement.updateMediaPath(\'' + row.mediaUrlNew + '\');" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>预览</button>&ensp;' +
                        '<button type="button" onclick="mediaInfoManagement.downMediaByPath(\'' + row.mediaUrlNew + '\',\'' + row.mediaName + '\');" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>下载</button>&ensp;' +
                        '<button onclick="mediaInfoManagement.editDescription(\'' + row.id + '\',\'' + row.description + '\')" type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;' +
                        '<button type="button" onclick="mediaInfoManagement.deleteMedia(\'' + row.id + '\');" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>删除</button>'
                    return html;
                }
            }, {
                "data": 'brand',
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return getPlateColor(row.plateColor);
                }
            }, {
                "data": "assignment",
                "class": "text-center",
            }, {
                "data": "eventCode",
                "class": "text-center",
                render: function (data, type, row, meta){
                    switch (data) {
                        case -1:
                            return '视频截图';
                        case 0:
                            return '平台下发指令';
                        case 1:
                            return '定时动作';
                        case 2:
                            return '抢劫报警触发';
                        case 3:
                            return '碰撞侧翻报警触发';
                        case 9:
                            return 'IC卡插卡触发';
                        default:
                            return ''
                    }
                }
            }, {
                "data": "uploadTime",
                "class": "text-center",
            }, {
                "data": "mediaName",
                "class": "text-center"
            }, {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return mediaInfoManagement.transMediaType(row.type);
                }
            }, {
                "data": "createDataUsername",
                "class": "text-center"
            }, {
                "data": "description",
                "class": "text-center demoUp",
                "width": "100px",
                render: function (data, type, row, meta) {
                    if (data == null) return '';
                    if (data && data.length > 10) {
                        data = data.substring(0, 10) + "...<span class='text' style='display: none'>" + data + '</span>';
                    }
                    return data;
                }
            }];

            //ajax参数
            var ajaxDataParamFun = function (d) {
                vehicleId = $('#charSelect').attr("data-id"); // 车id
                if (oldVehicleId != vehicleId) {
                    mediaPathList = {};
                    mediaLen = 0;
                }
                oldVehicleId = vehicleId;
                $("#carName").text($("input[name='charSelect']").val());
                $("#graphShow").css({"height": "600px", "display": "block"});
                //显示多媒体展示
                $(".carName,.left-btn,.right-btn,#showMedia").css("display", "block");
                $(".left-btn,.right-btn").removeClass("hidden");
                //选择视频
                if ($("#mediaCheckType").val() == "2") {
                    $("#photoChange").removeClass("hidden");
                    $("#showMedia").html(
                        "<video width='500' height='500' controls='controls' autoplay='autoplay'>" +
                        "<source src='' type='video/mp4'>" +
                        "</video>"
                    );
                }
                //选择音频
                if ($("#mediaCheckType").val() == "1") {
                    $("#photoChange").removeClass("hidden");
                    $("#showMedia").html(
                        "<audio style='margin:240px 0px;' controls='controls' autoplay='autoplay'>" +
                        "<source src='' type='audio/mpeg'>" +
                        "</audio>");
                }
                //选择图片
                if ($("#mediaCheckType").val() == "0") {
                    $("#photoChange").removeClass("hidden");
                    $("#showMedia").html("<img src='/clbs/resources/img/showMedia_img.png'/>");
                }
                var timeInterval = $('#timeInterval').val().split('--');
                var startTime = timeInterval[0];
                var endTime = timeInterval[1];
                var type = $('#mediaCheckType').val(); //多媒体类型
                d.vehicleId = vehicleId;
                d.startTime = startTime;
                d.endTime = endTime;
                d.type = type;
            };

            //表格setting
            var setting = {
                listUrl: '/clbs/m/reportManagement/media/list',
                columnDefs: columnDefs, //表格列定义
                columns: columns, //表格列
                dataTableDiv: 'dataTable', //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable: true, //是否分页
                showIndexColumn: true, //是否显示第一列的索引列
                enabledChange: true,
                drawCallbackFun: mediaInfoManagement.drawCallbackFun,
                async: false
            };
            //创建表格ter
            myTable = new TG_Tabel.createNew(setting);
            //表格初始化
            myTable.init();
        },
        // 修改备注
        editDescription: function (mediaId, description) {
            curEditBtn = this;
            $('#vehicleId').attr('value', mediaId);
            $('#description').val(description == "null" ? '' : description);
            $('#editDescriptionModal').modal('show');
        },
        saveDescription: function () {
            $('#descriptionForm').ajaxSubmit(function (data) {
                var json = JSON.parse(data);
                console.log(JSON.parse(data));
                if (json.success) {
                    $('#editDescriptionModal').modal('hide');
                    layer.msg("修改成功", {move: false});
                    mediaInfoManagement.refreshTable();
                } else if (json.msg) {
                    layer.msg(json.msg);
                }
            })
        },
        refreshTable: function () {
            // var vehicleId = $('#charSelect').attr("data-id"); // 车id
            // if (vehicleId == "" || vehicleId == null) {
            //     layer.msg(selectMonitoringObjec);
            //     return;
            // }
            if (!mediaInfoManagement.validates()) return;
            layer.load(2);
            myTable.requestData();
            layer.closeAll('loading');
        },
        validates: function () {
            return $("#hourslist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    groupId: {
                        required: true
                    },
                    endTime: {
                        required: true,
                        compareDate: "#timeInterval",
                        compareDateDiff: "#timeInterval"
                    },
                    startTime: {
                        required: true
                    },
                    charSelect: {
                        required: true
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    groupId: {
                        required: "不能为空"
                    },
                    endTime: {
                        required: "请选择结束日期！",
                        compareDate: "结束日期必须大于开始日期!",
                        compareDateDiff: "查询的日期必须小于一周"
                    },
                    startTime: {
                        required: "请选择开始日期！",
                    },
                    charSelect: {
                        required: "监控对象不能为空"
                    }
                }
            }).form();
        },
        drawCallbackFun: function () {
            // var rowsNode = $('#dataTable tbody tr');
            // $(rowsNode).each(function(index,node){
            //     // $(node).find('td:last').attr("onmouseover",'onMouseover(this)');
            //     if(index > 0){
            //         $(node).find('td:last').addClass('demoUp');
            //     }
            // })
            onMouseover();
            var flag = mediaPathList[1];
            mediaPathList = {};
            nowIndex = 0;
            var rows = $('#dataTable').dataTable().api().data();
            if (rows.length > 0) {
                $(rows).each(function (e, q) {
                    var key = e + 1;
                    mediaLen = key;
                    mediaPathList[key] = q.mediaUrlNew;
                })
            } else {
                mediaLen = 0;
            }

            if (mediaPathList[1]) {
                mediaInfoManagement.updateMediaPath(mediaPathList[1]);
            }
        },
        init: function () {
            $("[data-toggle='tooltip']").tooltip();
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
                    url: mediaInfoManagement.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    otherParam: {"icoType": "0"},
                    dataFilter: mediaInfoManagement.ajaxDataFilter
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
                    beforeClick: mediaInfoManagement.beforeClickVehicle,
                    onCheck: mediaInfoManagement.onCheckVehicle,
                    beforeCheck: mediaInfoManagement.zTreeBeforeCheck,
                    onExpand: mediaInfoManagement.zTreeOnExpand,
                    //beforeAsync: mediaInfoManagement.zTreeBeforeAsync,
                    onAsyncSuccess: mediaInfoManagement.zTreeOnAsyncSuccess,
                    onNodeCreated: mediaInfoManagement.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
        },
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                mediaInfoManagement.init();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param},
                        dataFilter: mediaInfoManagement.ajaxQueryDataFilter
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
                        beforeClick: mediaInfoManagement.beforeClickVehicle,
                        onCheck: mediaInfoManagement.onCheckVehicle,
                        onExpand: mediaInfoManagement.zTreeOnExpand,
                        onNodeCreated: mediaInfoManagement.zTreeOnNodeCreated
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
            for (var i = 0; i < nodesArr.length; i++) {
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl: function (treeId, treeNode) {
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            } else if (treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
            }
        },
        //组织树预处理加载函数
        ajaxDataFilter: function (treeId, parentNode, responseData) {//加载组织树
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
                return data;
            }
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (size <= 5000 && ifAllCheck) {
                treeObj.checkAllNodes(true);
            }
            mediaInfoManagement.getCharSelect(treeObj);
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        beforeClickVehicle: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getVehicleCount",
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
                if (nodesLength > 5000) {
                    layer.msg(maxSelectItem);
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
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    mediaInfoManagement.validates();
                }, 600);
            }
            if (treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)) {
                mediaInfoManagement.getCharSelect(zTree);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
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
        getCharSelect: function (treeObj) {
            var nodes = treeObj.getCheckedNodes(true)
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            var veh = [];
            var vid = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle") {
                    veh.push(nodes[i].name)
                    vid.push(nodes[i].id)
                }
            }
            var vehName = mediaInfoManagement.unique(veh);
            var vehId = mediaInfoManagement.unique(vid);
            $("#charSelect").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++) {
                deviceDataList.value.push({
                    name: vehName[j],
                    id: vehId[j]
                });
            }
            ;

            $("#charSelect").bsSuggest({
                indexId: 1, //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click", function () {
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
            if (deviceDataList.value.length > 0) {
                $("#charSelect").val(deviceDataList.value[0].name).attr("data-id", deviceDataList.value[0].id);
            }
            $("#groupSelect,#groupSelectSpan").bind("click", mediaInfoManagement.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
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
            $("body").bind("mousedown", mediaInfoManagement.onBodyDown);
        },
        hideMenu: function () {
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", mediaInfoManagement.onBodyDown);
        },
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(
                event.target).parents("#menuContent").length > 0)) {
                mediaInfoManagement.hideMenu();
            }
        },
        //获取到选择的节点
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree.getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "vehicle") {
                    v += nodes[i].name + ",";
                    vid += nodes[i].id + ",";
                }
            }
            allVid = vid;
            vnameList = v;
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
        transMediaType: function (type) {
            if (type == 0) {
                return "图片";
            } else if (type == 1) {
                return "音频";
            } else if (type == 2) {
                return "视频";
            }
        },
        right_arrow: function () { // 下一辆车
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function () {
                if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (trIndex == nowIndex) {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(0).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(0).attr("data-key"));
            } else {
                var nextIndex = parseInt(nowIndex) + 1;
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nextIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nextIndex).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        left_arrow: function () {
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function () {
                if ($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")) {
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (nowIndex == 0) {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(trIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(trIndex).attr("data-key"));
            } else {
                $("input[name='charSelect']").attr("data-id", $(".table-condensed tr").eq(nowIndex - 1).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nowIndex - 1).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        clickRightMedia: function () {
            nowIndex++;
            var table = $('#dataTable').dataTable().api();
            var pageInfo = table.page.info();
            if (nowIndex == mediaLen && mediaLen != 1 && pageInfo.pages != pageInfo.page + 1) {
                table = $('#dataTable').dataTable().api();
                table.page('next').draw(false);
                table = $('#dataTable').dataTable().api();
                pageInfo = table.page.info();
                if (pageInfo.pages == pageInfo.page + 1) {
                    mediaLen = pageInfo.end - pageInfo.start;
                }
            }
            if (mediaLen == 0 || nowIndex == mediaLen) {
                layer.msg(lastFile, {move: false});
                nowIndex--;
            } else {
                // 更改文件内容
                mediaInfoManagement.updateMediaPath(mediaPathList[nowIndex + 1]);
            }
        },
        clickLeftMedia: function () {
            nowIndex--;
            var table = $('#dataTable').dataTable().api();
            if (nowIndex < 0 && table.page() > 0) {
                table.page('previous').draw(false);
                var rows = table.data();
                if (rows.length > 0) {
                    $(rows).each(function (e, q) {
                        var key = e + 1;
                        mediaLen = key;
                        mediaPathList[key] = q.mediaUrlNew;
                    })
                } else {
                    mediaLen = 0;
                }
                nowIndex = table.data().length - 1;
            }
            if (mediaLen != 0 && nowIndex >= 0) {
                // 更改文件内容
                mediaInfoManagement.updateMediaPath(mediaPathList[nowIndex + 1]);
            } else {
                layer.msg(firstFile, {move: false});
                nowIndex++;
            }
        },
        //查询多媒体展示预览
        updateMediaPath: function (path) {
            if (path != null && path != "" && path != "null") {
                //选择视频
                if ($("#mediaCheckType").val() == "2") {
                    $("#showMedia").html(
                        "<video width='500' height='500' controls='controls' autoplay='autoplay'>" +
                        "<source src='" + path + "' type='video/mp4'>" +
                        "</video>"
                    );
                }
                //选择音频
                else if ($("#mediaCheckType").val() == "1") {
                    $("#showMedia").html(
                        "<audio style='margin:240px 0px;' controls='controls' autoplay='autoplay'>" +
                        "<source src='" + path + "' type='audio/mpeg'>" +
                        "</audio>");
                }
                //选择图片
                else if ($("#mediaCheckType").val() == "0") {
                    $("#showMedia").html("<img src='" + path + "'/>");
                }
            }else{
                layer.msg('该多媒体格式无法预览');
            }
        },
        downMediaByPath: function (mediaUrl, fileName) {
            if (mediaUrl != null && mediaUrl != "" && fileName != null && fileName != "") {
                var url = "/clbs/m/reportManagement/media/downMedia";
                var parameter = {"mediaUrl": mediaUrl, "fileName": fileName};
                var form = $('<form method="POST" action="' + url + '">');
                $.each(parameter, function (k, v) {
                    form.append($('<input type="hidden" name="' + k +
                        '" value="' + v + '">'));
                });
                $('body').append(form);
                form.submit(); //自动提交
                form.remove();
            }
        },
        deleteMedia: function (id) {
            var url = "/clbs/m/reportManagement/media/delete";
            var parameter = {"id": id};
            json_ajax("POST", url, "json", true, parameter, function (data) {
                myTable.requestData();
            });
        },
        // 开始时间
        startDay: function (day) {
            var timeInterval = $('#timeInterval').val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day;
                today.setTime(targetday_milliseconds); // 注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = mediaInfoManagement.doHandleMonth(tMonth + 1);
                tDate = mediaInfoManagement.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = mediaInfoManagement.doHandleMonth(endMonth + 1);
                endDate = mediaInfoManagement.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0, 10).replace("-", "/").replace("-", "/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = mediaInfoManagement.doHandleMonth(vMonth + 1);
                vDate = mediaInfoManagement.doHandleMonth(vDate);
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
                    vendMonth = mediaInfoManagement.doHandleMonth(vendMonth + 1);
                    vendDate = mediaInfoManagement.doHandleMonth(vendDate);
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
        todayClick: function () {
            mediaInfoManagement.getsTheCurrentTime();
            $('#timeInterval').val(startTime + '--' + endTime);
            mediaInfoManagement.refreshTable();
        },
        yesterdayClick: function () {
            mediaInfoManagement.startDay(-1);
            $('#timeInterval').val(startTime + '--' + endTime);
            mediaInfoManagement.refreshTable();
        },
        nearlyThreeDays: function () {
            mediaInfoManagement.startDay(-3);
            $('#timeInterval').val(startTime + '--' + endTime);
            mediaInfoManagement.refreshTable();
        },
        nearlySevenDays: function () {
            mediaInfoManagement.startDay(-7);
            $('#timeInterval').val(startTime + '--' + endTime);
            mediaInfoManagement.refreshTable();
        },
        tomorrowChangeClick: function () {
            var nowDate = new Date();
            endTimes = nowDate.getFullYear()
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
            var sone = endTimes;
            mediaInfoManagement.startDay(+1);
            var stwo = endTime;
            if (stwo > sone) {
                layer.msg("暂时没有办法穿越，明天我再帮你您看吧！");
            } else {
                $('#timeInterval').val(startTime + '--' + endTime);
                mediaInfoManagement.refreshTable();
            }
        },
    }
    $(function () {
        //初始化页面
        mediaInfoManagement.init();
        mediaInfoManagement.initTable();
        $('[data-toggle="tooltip"]').tooltip();
        $('#timeInterval').dateRangePicker({
            dateLimit: 31
        });
        mediaInfoManagement.getsTheCurrentTime();
        $("#groupSelectSpan,#groupSelect").bind("click", mediaInfoManagement.showMenu);
        $("#inquireClick").on("click", mediaInfoManagement.refreshTable);
        $("#rightClickVehicle").bind("click", mediaInfoManagement.right_arrow);
        $("#leftClickVehicle").bind("click", mediaInfoManagement.left_arrow);
        $("#right-arrow").bind("click", mediaInfoManagement.clickRightMedia);
        $("#left-arrow").bind("click", mediaInfoManagement.clickLeftMedia);
        //今天
        $("#todayClick").on("click", mediaInfoManagement.todayClick);
        //前一天
        $("#yesterdayClick").on("click", mediaInfoManagement.yesterdayClick);
        //前三天
        $("#nearlyThreeDays").on("click", mediaInfoManagement.nearlyThreeDays);
        //前七天
        $("#nearlySevenDays").on("click", mediaInfoManagement.nearlySevenDays);
        //昨天明天
        $("#yesterdayChange").on("click", mediaInfoManagement.yesterdayClick);
        $("#tomorrowChange").on("click", mediaInfoManagement.tomorrowChangeClick);

        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                mediaInfoManagement.searchVehicleTree(param);
            }
            ;
        });

        // 修改备注
        $('#saveDescription').on('click', mediaInfoManagement.saveDescription);
        /**
         * 监控对象树模糊查询
         */
        var inputChange;
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            ;
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            ;
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    mediaInfoManagement.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            mediaInfoManagement.searchVehicleTree(param);
        });

    })
}(window, $))

function onMouseover() {
    var demoUps = document.getElementsByClassName("demoUp");
    for (var i = 0; i < demoUps.length; i++) {
        if ($(demoUps[i]).parent().parent()[0].tagName != 'THEAD') {
            demoUps[i].addEventListener("mouseover", function () {
                var _this = $(this);
                if (_this.find('.text').text()) {
                    _this.justToolsTip({
                        animation: "moveInTop",
                        width: "auto",
                        contents: _this.find('.text').text(),
                        gravity: 'top'
                    });
                }
            });
        }
    }
}