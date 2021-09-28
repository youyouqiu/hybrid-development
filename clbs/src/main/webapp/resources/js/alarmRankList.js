(function (window, $) {
    var tabInx = 1; // 1 监控对象
                    // 2 驾驶员
    var timeIntervalDom = $('#timeInterval' + tabInx); // 时间选择框dom对象
    //时间
    var startTime,
        endTime;
    var myTable,
        tableListArray=[];
    var dateLimit=31;//限制只能查询一个月的数据

    //树结构
    var alarmRankZTree;
    var groupId=[]; // 选中查询对象id集合
    var panelFlag1 = true; // 是否渲染组织树1
    var panelFlag2 = true; // 是否渲染组织树2
    var searchFlag=false;
    var crrentSubV = []; //模糊查询
    var size;
    var ifAllCheck = true; //刚进入页面小于5000自动勾选
    var TREE_MAX_CHILDREN_LENGTH=5000;
    var zTreeIdJson = {};

    var forbiddenRepeat = true; // 防重复提交
    alarmRankList = {
        init: function () {
            alarmRankList.getsTheCurrentTime(); // 初始化开始时间 结束时间

            alarmRankList.setTable('#dataTable' + tabInx); // 渲染table
            alarmRankList.setTableHead('#dataTable'+tabInx,"#Ul-menu-text"+tabInx); // 自定义表头

            alarmRankList.treeInit(); // 渲染组织树

        },
        getsTheCurrentTime: function () {
            var nowDate = new Date();
            startTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
            endTime = nowDate.getFullYear()
                + "-"
                + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
                    + parseInt(nowDate.getMonth() + 1)
                    : parseInt(nowDate.getMonth() + 1))
                + "-"
                + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
                    : nowDate.getDate());
        },
        /**
         * table渲染
         * @param id
         */
        setTable: function(id){

            myTable = $(id).DataTable({
                "destroy": true,
                "lengthChange": true,// 是否允许用户自定义显示数量
                "bPaginate": true, // 翻页功能
                "bFilter": false, // 列筛序功能
                "searching": true,// 本地搜索
                "ordering": false, // 排序功能
                "Info": true,// 页脚信息
                "autoWidth": true,// 自动宽度
                "stripeClasses": [],
                "pageLength": 10,
                "lengthMenu": [5,10, 20, 50, 100, 200],
                "pagingType": "simple_numbers", // 分页样式
                "dom": "t" + "<'row'<'col-md-2 col-sm-12 col-xs-12 noPadding'l><'col-md-4 col-sm-12 col-xs-12 noPadding'i><'col-md-6 col-sm-12 col-xs-12 noPadding'p>>",
                "oLanguage": {// 国际语言转化
                    "oAria": {
                        "sSortAscending": " - click/return to sort ascending",
                        "sSortDescending": " - click/return to sort descending"
                    },
                    "sLengthMenu": "显示 _MENU_ 记录",
                    "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录。",
                    "sZeroRecords": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sEmptyTable": "我本将心向明月，奈何明月照沟渠，不行您再用其他方式查一下？",
                    "sLoadingRecords": "正在加载数据-请等待...",
                    "sInfoEmpty": "当前显示0到0条，共0条记录",
                    "sInfoFiltered": "（数据库中共为 _MAX_ 条记录）",
                    "sProcessing": "<img src='../resources/user_share/row_details/select2-spinner.gif'/> 正在加载数据...",
                    "sSearch": "模糊查询：",
                    "sUrl": "",
                    "oPaginate": {
                        "sFirst": "首页",
                        "sPrevious": " 上一页 ",
                        "sNext": " 下一页 ",
                        "sLast": " 尾页 "
                    }
                },
                "columnDefs": [{
                    "targets": [0, 2, 3, 4],
                    "searchable": false
                }],
                "drawCallback": function(settings) {
                },
                "order": [
                    [0, null]
                ],// 第一列排序图标改为默认

            });
            myTable.on('order.dt search.dt', function () {
                myTable.column(0, {
                    search: 'applied',
                    order: 'applied'
                }).nodes().each(function (cell, i) {
                    cell.innerHTML = i + 1;
                });
            }).draw();

        },
        /**
         * 隐藏显示table头
         * @param id
         * @param head
         */
        setTableHead: function(id,head){
            //显示隐藏数据列表
            var menu_text = "";
            var table = $(id).find("tr th:gt(0)");
            for (var i = 0; i < table.length; i++) {
                menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 1) + "\" />" + table[i].innerHTML + "</label></li>"
            }
            $(head).html(menu_text);

            //显示隐藏列
            $('.toggle-vis').off('change').on('change', function (e) {
                var column = myTable.column($(this).attr('data-column'));
                column.visible(!column.visible());
                $(".keep-open").addClass("open");
            });
        },
        // 组织树地址
        getTreeUrl:function(treeId, treeNode){
            if(tabInx==1){
                if (treeNode == null) {
                    return "/clbs/m/personalized/ico/vehicleTree";
                } else if (treeNode.type == "assignment") {
                    return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=vehicle";
                }
            }else if(tabInx==2){
                return '/clbs/r/reportManagement/adasAlarmRank/driverTree';
            }
        },
        ajaxDataFilter: function(treeId, parentNode, responseData){
            var data=responseData;
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                if (obj.tree != null && obj.tree != undefined) {
                    data = obj.tree;
                    size = obj.size;
                } else {
                    data = obj
                }
            }
            return data;
        },
        // 获取组织树的dom
        getTreeObj: function() {
            var treeObj; // 获取当前组织树对象
            var dom; // 获取input对象
            if(tabInx == 1) {
                treeObj = $.fn.zTree.getZTreeObj("treeDemo1");
                dom = $('#groupSelect1');
            }else if(tabInx == 2) {
                treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
                dom = $('#groupSelect2');
            }
            return [treeObj, dom]
        },
        // 组织树对象勾选，更新input值
        zTreeOnCheck: function(event, treeId, treeNode){
            var [treeObj, dom] = alarmRankList.getTreeObj(); // 获取当前对象dom
            if(treeNode.checked){
                searchFlag = false;
                var zTree = $.fn.zTree.getZTreeObj(treeId);
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    alarmRankList.validates();
                    alarmRankList.validatesTwo();
                }, 600);
            }
            var nodes=treeObj.getCheckedNodes(true);
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
            groupId = []; // 每次清空
            nodes.forEach(function(d) {
                if(tabInx === 1) {
                    if(d.type === "vehicle") {
                        groupId.push(d.id);
                    }
                }else if(tabInx === 2) {
                    if(d.type === "people") {
                        groupId.push(d.id);
                    }
                }
            });

            if(nodes.length > 0) {
                dom.val(treeNode.name);
            }else {
                dom.val("");
            }

        },
        getCheckedNodes: function (treeId,monitorType) {
            groupId=[];
            var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
                .getCheckedNodes(true);
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == monitorType) {
                    groupId.push(nodes[i].id)
                }
            }
        },
        // 更新当前选择的node
        updateztreeNode: function() {
            var treeObj= alarmRankList.getTreeObj()[0]; // 获取当前对象dom
            var nodes=treeObj.getCheckedNodes(true);
            var groups = [];
            nodes.forEach(function(item){
                if(tabInx==1 && item.type === "vehicle"){
                    groups.push(item.id);
                }else if(tabInx==2){
                    groups.push(item.id);
                }
            });
            return groups;
        },
        // 初始化全选组织树
        zTreeOnAsyncSuccess: function(){
            // var [treeObj, dom ]= alarmRankList.getTreeObj(); // 获取当前对象dom
            //
            // // 条件限制
            // if((ifAllCheck&&size<=5000&&tabInx==1)||tabInx==2){
            //     treeObj.checkAllNodes(true);
            // }
            //
            // var nodes=treeObj.getCheckedNodes(true); // 获取选中的对象；
            // // console.log(nodes);
            // nodes.forEach(function(item){
            //     // if(tabInx==1){
            //     //     groupId.push(item.id);
            //     // }else if(tabInx==2){
            //     //     groupId.push(item.id);
            //     // }
            //     groupId.push(item.id);
            // });

            // var treeNode =  treeObj.getNodes(); // 获取当前选中对方父级node
            // if(!searchFlag||tabInx==2){
            //     dom.val(treeNode[0].name); // 更新input框显示值
            // }
        },
        /**
         * 组织
         * 模糊查询树
         */
        treeInit:function(){
            var treeSetting = {
                async : {
                    url : alarmRankList.getTreeUrl,
                    type : "post",
                    enable : true,
                    autoParam : [ "id" ],
                    dataType : "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter:alarmRankList.ajaxDataFilter
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
                view : {
                    selectedMulti : false,
                    nameIsHTML: true,
                    dblClickExpand: false,
                    countClass: "group-number-statistics"
                },
                data : {
                    simpleData : {
                        enable : true
                    }
                },
                callback : {
                    beforeCheck: alarmRankList.zTreeBeforeCheck,
                    onCheck: alarmRankList.zTreeOnCheck,
                    onAsyncSuccess: alarmRankList.zTreeOnAsyncSuccess,
                    onClick: alarmRankList.ztreeOnClick,
                    onExpand: alarmRankList.zTreeOnExpand,
                    onNodeCreated: alarmRankList.zTreeOnNodeCreated,
                }
            };

            // 初始加载一个组织树
            if(tabInx == 1) {
                alarmRankZTree = $.fn.zTree.init($("#treeDemo1"), treeSetting, null);
                panelFlag1 = false;
            } else if(tabInx == 2) {
                alarmRankZTree = $.fn.zTree.init($("#treeDemo2"), treeSetting, null);
                panelFlag2 = false;
            }
            //
            // 初始渲染
            $("#panelTabq1").on("click", function () {
                if (panelFlag1) {
                    tabInx = 1;
                    $.fn.zTree.init($("#treeDemo1"), treeSetting, null);
                    alarmRankList.setTable('#dataTable1');
                    alarmRankList.setTableHead('#dataTable1',"#Ul-menu-text1"); // 自定义表头
                    panelFlag1 = false;
                }
                tabInx = 1
                alarmRankList.setTable('#dataTable' + tabInx); // 渲染table
                alarmRankList.setTableHead('#dataTable'+tabInx,"#Ul-menu-text"+tabInx); // 自定义表头
            });
            $("#panelTabq2").on("click", function () {
                if (panelFlag2) {
                    tabInx = 2;
                    $.fn.zTree.init($("#treeDemo2"), treeSetting, null);
                    alarmRankList.setTable('#dataTable2');
                    alarmRankList.setTableHead('#dataTable2',"#Ul-menu-text2"); // 自定义表头
                    panelFlag2 = false;
                }
                tabInx = 2
                alarmRankList.setTable('#dataTable' + tabInx); // 渲染table
                alarmRankList.setTableHead('#dataTable'+tabInx,"#Ul-menu-text"+tabInx); // 自定义表头
            });
        },
        zTreeBeforeCheck: function (treeId, treeNode) {
            var flag = true;
            if(treeId==='treeDemo2') return  true;
            if (!treeNode.checked) {
                if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
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
                    var zTree = $.fn.zTree.getZTreeObj(treeId), nodes = zTree
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
        //模糊查询树
        searchVehicleTree: function (param) {
            ifAllCheck = false;//模糊查询不自动勾选
            crrentSubV = [];
            if (param == null || param == undefined || param == '') {
                bflag = true;
                alarmRankList.treeInit();
            } else {
                bflag = true;
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type": $('#queryType').val(), "queryParam": param, "queryType": "vehicle"},
                        dataFilter: alarmRankList.ajaxQueryDataFilter
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
                        onCheck: alarmRankList.zTreeOnCheck,
                        onAsyncSuccess: alarmRankList.zTreeOnAsyncSuccess,
                        onClick: alarmRankList.ztreeOnClick,
                        onExpand: alarmRankList.zTreeOnExpand,
                        onNodeCreated: alarmRankList.zTreeOnNodeCreated, // 用于捕获节点生成 DOM 后的事件回调函数
                    }
                };
                $.fn.zTree.init($("#treeDemo1"), querySetting, null);
            }
        },
        zTreeOnNodeCreated: function (event, treeId, treeNode) {
            if(tabInx==2) return;
            var id = treeNode.id.toString();
            var list = [];
            if (zTreeIdJson[id] == undefined || zTreeIdJson[id] == null) {
                list = [treeNode.tId];
                zTreeIdJson[id] = list;
            } else {
                zTreeIdJson[id].push(treeNode.tId)
            }
        },
        zTreeOnExpand: function (event, treeId, treeNode) {
            if(tabInx==2) return;
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if (treeNode.type == "group" && !checkFlag) {
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo1");
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
                            var chNodes = result[i];//获取对应的value
                            if (zTreeIdJson[pid] != undefined) {
                                var parentTid = zTreeIdJson[pid][0];
                                var parentNode = treeObj.getNodeByTId(parentTid);
                                if (parentNode.children === undefined) {
                                    treeObj.addNodes(parentNode, []);
                                }
                            }
                        });
                    }
                })
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData));
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
        // 是否点击文字就可进行选择
        ztreeOnClick: function(e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo" + tabInx);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            // 第2个参数为true：点击组织group时，全部勾选或取消
            // 第3个参数为null：点击组织group时，仅选择勾选对象，不包括子组织

            return false;
        },
        // 隐藏组织树下拉框
        hideMenu: function () {
            var menuContent=$('#menuContent'+tabInx);
            menuContent.fadeOut("fast");
            $("body").unbind("mousedown", alarmRankList.onBodyDown);
        },
        // 隐藏组织树下拉框（点击页面其他区域）
        onBodyDown: function (event) {
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent"+tabInx || $(
                event.target).parents("#menuContent"+tabInx).length > 0)) {
                alarmRankList.hideMenu();
            }
        },
        /**查询
         * @param number
         */
        inquireClick: function (number) {
            // 更新当前dom对象
            timeIntervalDom = $('#timeInterval' + tabInx);
            // 时间参数获取
            if(number==-1){
                alarmRankList.startDay(-1);
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(number==-3){
                alarmRankList.startDay(-3)
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(number ==-7){
                alarmRankList.startDay(-7)
                timeIntervalDom.val(startTime + '--' + endTime);
            }else if(number == 0) {
                var yesterday = alarmRankList.getYesterDay();
                timeIntervalDom.val(yesterday + '--' + yesterday);
            }

            if(tabInx===1){
                alarmRankList.getCheckedNodes('treeDemo1','vehicle');
            }else{
                alarmRankList.getCheckedNodes('treeDemo2','people');
            }
            var timeInterval = timeIntervalDom.val().split('--');
            var reg = /-/g;
            startTime = timeInterval[0].replace(reg,'');
            endTime = timeInterval[1].replace(reg,'');

            var url = "",
                parameter;

            if (!alarmRankList.validates()) {
                return;
            }
            if (!alarmRankList.validatesTwo()) {
                return;
            }

            if(groupId !== "") {
                // $('#isShow-q').addClass('hide');
                if(tabInx==1){//监控对象
                    url = "/clbs/r/reportManagement/adasAlarmRank/getVehicleRankPage";
                    parameter = {
                        "startTime": startTime,
                        "endTime": endTime,
                        "vehicleIds": groupId.join(',')
                    };
                }else if(tabInx==2){//驾驶员
                    url = "/clbs/r/reportManagement/adasAlarmRank/getDriverRankPage";
                    parameter = {
                        "startTime": startTime,
                        "endTime": endTime,
                        "driverIds": groupId.join(',')
                    };
                }
                if(!forbiddenRepeat) return; // 请求尚未收到结果时，forbiddenRepeat 为false，返回
                json_ajax("POST", url, "json", true, parameter, alarmRankList.getCallback);
            }

        },
        // 日期格式化
        startDay: function (day) {
            var timeInterval = timeIntervalDom.val().split('--');
            var startValue = timeInterval[0];
            var endValue = timeInterval[1];
            if (startValue == "" || endValue == "") {
                var today = new Date();
                var targetday_milliseconds = today.getTime() + 1000 * 60 * 60
                    * 24 * day;

                today.setTime(targetday_milliseconds); //注意，这行是关键代码
                var tYear = today.getFullYear();
                var tMonth = today.getMonth();
                var tDate = today.getDate();
                tMonth = alarmRankList.doHandleMonth(tMonth + 1);
                tDate = alarmRankList.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate;
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = alarmRankList.doHandleMonth(endMonth + 1);
                endDate = alarmRankList.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate;
            } else {
                var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = alarmRankList.doHandleMonth(vMonth + 1);
                vDate = alarmRankList.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate;
                if(day == 1){
                    endTime = vYear + "-" + vMonth + "-" + vDate;
                }else{
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = alarmRankList.doHandleMonth(vendMonth + 1);
                    vendDate = alarmRankList.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate;
                }
            }
        },
        // 月份格式化
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        // 日期矫正
        validates: function(){
            return $("#alarmRankList1").validate({
                rules: {
                    groupSelect:{
                        regularChar: true,
                        // required: true
                        zTreeChecked: "treeDemo1"
                    },
                    timeInterval:{
                        required: true
                    }
                },
                messages: {
                    groupSelect:{
                        zTreeChecked: vehicleSelectBrand
                    },
                    timeInterval:{
                        required: "请选择日期"
                    }
                }
            }).form();
        },
        validatesTwo: function(){
            return $("#alarmRankList2").validate({
                rules: {
                    groupSelect:{
                        zTreeChecked: "treeDemo2"
                    },
                    timeInterval:{
                        required: true
                    }
                },
                messages: {
                    groupSelect:{
                        zTreeChecked: "请至少选择一个驾驶员"
                    },
                    timeInterval:{
                        required: "请选择日期"
                    }
                }
            }).form();
        },
        // 请求数据成功后：数据格式化
        getCallback:function(data){
            forbiddenRepeat = false; // 将此值置为false, 防止重复提交
            tableListArray=[];
            if(data.success){
                forbiddenRepeat = true; // 已获取结果，此值置为true
                data=data.obj;
                data.forEach(function(item,index){
                    var obj=[];

                    if(tabInx==1){
                        obj=[
                            index + 1,
                            item.brand, // 监控对象
                            item.groupName, // 所属企业
                            item.area, // 所属区域
                            item.total, // 报警数
                            item.percentageString, // 占比
                        ];
                    }else if(tabInx==2){
                        obj=[
                            index + 1, // 序号
                            item.driverName, // 驾驶员
                            item.groupName, // 所属公司
                            item.total, // 报警数
                            item.percentageString, // 占比
                        ];
                    }
                    tableListArray.push(obj);
                });
            }else {
                forbiddenRepeat = true; // 已获取结果，此值置为true
            }
            alarmRankList.reloadData();
        },
        // 表格数据渲染
        reloadData: function () {

            var currentPage = myTable.page();
            $("#simpleQueryParam"+tabInx).val("");
            myTable.clear();
            myTable.rows.add(tableListArray);
            myTable.column(1).search('', false, false).page(currentPage).draw();
        },
        // 获取时间
        getYesterDay: function () {
            var nowDate = new Date();
            var date = new Date(nowDate.getTime() - 24*60*60*1000);
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth()+1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var yesterdate = year + seperator1 + month + seperator1 + strDate;
            return yesterdate;
        },
        /**
         * 导出
         */
        exportRisk: function () {
            if (!alarmRankList.validates()) {
                return;
            }
            if (!alarmRankList.validatesTwo()) {
                return;
            }
            var url = "";
            var parameter;
            var timeInterval = timeIntervalDom.val().split('--');
            var reg = /-/g;
            startTime = timeInterval[0].replace(reg,'');
            endTime = timeInterval[1].replace(reg,'');

            var groups = alarmRankList.updateztreeNode();  // 全局groupId有问题

            if(tabInx==1){
                url = "/clbs/r/reportManagement/adasAlarmRank/exportVehicleRank";
                parameter = {
                    "vehicleIds": groups.join(','),
                    "startTime": startTime,
                    "endTime": endTime
                };
                if(getRecordsNum('dataTable1_info') > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
            }else if(tabInx==2){
                url = "/clbs/r/reportManagement/adasAlarmRank/exportDriverRank";
                parameter = {
                    "driverIds": groups.join(','),
                    "startTime": startTime,
                    "endTime": endTime
                };
                if(getRecordsNum('dataTable2_info') > 60000){
                    return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
                }
            }
            exportExcelUseForm(url, parameter);
        },
        /**
         * 对搜索结果的模糊查询
         */
        dataSearch:function(){
            var tsval = $("#simpleQueryParam"+tabInx).val();
            myTable.column(1).search(tsval, false, false).draw();
        },
        /**
         * 刷新列表
         */
        refreshTable: function () {
            $("#simpleQueryParam"+tabInx).val("");
            var tsval = $("#simpleQueryParam"+tabInx).val();
            myTable.column(1).search(tsval, false, false).draw();
        },
        // ztree快速清空搜索框（原方法： search_ztree ->searchByFlag_ztree)
        // 原方法会展开所有节点，导致此处搜索框过卡，此处修改引用
        searchByFlag_ztreeq: function(treeId, searchConditionId, flag, type, searchValue) {
        //<1>.搜索条件
        var searchCondition = searchValue != undefined ? searchValue : $('#' + searchConditionId).val();
        var highlightNodes = [], newHighlightNodes = [];
        var allNodes = [];
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        searchParam = searchCondition;
        if (type == "vehicle") {
            highlightNodes = treeObj.getNodesByFilter(monitorParamFuzzyFilter);
            // allNodes = treeObj.getNodesByFilter(monitorFilter); // 所有type型nodes
            allNodes = treeObj.transformToArray(treeObj.getNodes()); // 所有节点
            newHighlightNodes = highlightNodes;
        } else {
            highlightNodes = treeObj.getNodesByParamFuzzy("name", searchCondition, null); // 满足搜索条件的节点
            allNodes = treeObj.getNodesByParam("type", type, null); // 所有type型nodes
            if (type == 'assignment') {
                allNodes = allNodes.concat(treeObj.getNodesByParam("type", 'group', null))
            }

            for (var i = 0, len = highlightNodes.length; i < len; i++) {
                if (highlightNodes[i].type == type) {
                    newHighlightNodes.push(highlightNodes[i]);
                }
            }
        }
        if (searchCondition != "") {
            searchParam = searchCondition;
            if (type == "group" || type == "assignment") {  // 企业
                // 需要显示是节点（包含父节点）
                var showNodes = [];
                if (newHighlightNodes != null) {
                    for (var i = 0; i < newHighlightNodes.length; i++) {
                        //组装显示节点的父节点的父节点....直到根节点，并展示
                        getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes);
                    }
                    treeObj.hideNodes(allNodes);
                    treeObj.showNodes(showNodes);
                    // treeObj.expandAll(isExpend);
                }

            } else {
                // <2>.得到模糊匹配搜索条件的节点数组集合
                // treeObj.hideNodes(allNodes);
                // treeObj.showNodes(highlightNodes);
                // treeObj.expandAll(true);
                // 需要显示是节点（包含父节点）
                var showNodes = [];
                // 只显示直接上级
                if (newHighlightNodes != null) {
                    for (var i = 0; i < newHighlightNodes.length; i++) {
                        //组装显示节点的父节点的父节点....直到根节点，并展示
                        getParentShowNodes_ztree(treeId, newHighlightNodes[i], showNodes);
                    }
                    treeObj.hideNodes(allNodes);
                    treeObj.showNodes(showNodes);
                    // treeObj.expandAll(isExpend);
                }
            }
        } else {
            treeObj.showNodes(allNodes);
            // treeObj.expandAll(isExpend);
        }
        // <3>.高亮显示并展示【指定节点s】
        // highlightAndExpand_ztree(treeId, highlightNodes, flag);
    }
    };
    $(function () {
        // 初始化
        alarmRankList.init();
        //时间
        $("#timeInterval1").dateRangePicker2({
            dateLimit:dateLimit,
            isOffLineReportFlag: true,
            nowDate:alarmRankList.getYesterDay(),
            isShowHMS:false
        });
        $("#timeInterval2").dateRangePicker2({
            dateLimit:dateLimit,
            isOffLineReportFlag: true,
            nowDate:alarmRankList.getYesterDay(),
            isShowHMS:false
        });

        //组织树显示
        $("#groupSelect1,#groupSelect2").bind("click", showMenuContent);
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect1') {
                searchFlag = true;
                ifAllCheck=false;
                var param = $("#groupSelect1").val();
                alarmRankList.searchVehicleTree(param);
            }else if(id == 'groupSelect2') {
                searchFlag = true;
                ifAllCheck=false;
                var param = $("#groupSelect2").val();
                alarmRankList.searchVehicleTree(param);
            }
        });
        // 搜索结果模糊查询
        var inputChange;
        $("#groupSelect1").on('input propertychange', function (value) {
            ifAllCheck=false;
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                searchFlag = true;
            }
            inputChange = setTimeout(function () {
                if (searchFlag) {
                    var param = $("#groupSelect1").val();
                    alarmRankList.searchVehicleTree(param);
                }
                searchFlag = true;
            }, 500);
        });
        $("#groupSelect2").on('input propertychange', function (value) {
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
            treeObj.checkAllNodes(false);
            search_ztree('treeDemo2', 'groupSelect2', 'people');
        });

        // 对搜索结果的筛选查询
        $("#search_button1,#search_button2").on("click", alarmRankList.dataSearch);
        // 对搜索结果的筛选查询
        $("#simpleQueryParam1,#simpleQueryParam2").on("keydown", function (e) {
            var key=e.which;
            if(key==13){
                e.preventDefault();
                alarmRankList.dataSearch();
            }
        });

        //导出
        $("#exportRisk1,#exportRisk2").bind("click", alarmRankList.exportRisk);
        //刷新
        $("#refreshTableMonth1,#refreshTableMonth2").bind("click", alarmRankList.refreshTable);

        // ztree的input清空功能
        $('input').inputClear();
        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'groupSelect1'){
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo1");
                alarmRankList.searchByFlag_ztreeq('treeDemo1', 'groupSelect1', "", 'group');
                treeObj.checkAllNodes(false);
            }
            if(id == 'groupSelect2'){
                var treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
                alarmRankList.searchByFlag_ztreeq('treeDemo2', 'groupSelect2', "", 'group');
                treeObj.checkAllNodes(false);
            }
        });
        $('#queryType').on('change', function () {
            var param = $("#groupSelect").val();
            alarmRankList.searchVehicleTree(param);
        });
    })
})(window, $);

