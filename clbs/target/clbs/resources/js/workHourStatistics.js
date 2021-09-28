(function ($, window) {
    var startTime;
    var endTime;
    var date = [];//图形表时间
    var speed = [];//速度
    var accTemp = [];//acc

    // wjk
    var speed = [];//速度
    var flowrate = []; //瞬时流量
    var duration_state =[];//状态持续时长
    // var allWorkTime=[];//总工作时长
    // var allstopTime=[];//总停机时长
    // var allWaitTime=[];//总待机时长
    //工作停机待机状态数组
    var _daijiArr= [];
    var _tingjiArr=[];
    var _workArr = [];
    var thresholdWorkFlow=0;//阈值

    var speedThreshold = null;//速度阈值

    var fluctuateVal = []; //波动值

    var chart;
    var myChart;
    var option;
    var oilMax;
    var flogKey;//判断当前车辆是否绑定传感器
    // 车辆list
    var vehicleList = JSON.parse($("#vehicleList").attr("value"));

    var bflag = true; //模糊查询
    var crrentSubV = []; //模糊查询？

    var zTreeIdJson = {};
    var size;//当前权限监控对象数量
    var checkFlag = false;
    var searchSate = true;

    var ifAllCheck = true; //刚进入页面小于5000自动勾选

    var sensorSequence = '0';

    var fluxOrvoltage = '瞬时流量';

    var ifEmptyData=[] //是否空数据

    var _vehicleId;

    var chartLegendData;

    var thresholdValueType = '工作阈值';

    var workInspectionMethodType = null;

    var myTable;
    //用于ie浏览器判断选择节点树时是否需要调用搜索功能
    var isSearch = true;

    //总数据列定义
    // var allTableDataColumns = [ {
    //     //第一列，用来显示序号
    //     "data" : null,
    //     "class" : "text-center"
    // },{
    //     "data" : "plateNumber",
    //     "class" : "text-center"
    // },{
    //     "data" : "workingPositionStr",
    //     "class" : "text-center"
    // },{
    //     "data" : "startTimeStr",
    //     "class" : "text-center"
    // },{
    //     "data" : "endTimeStr",
    //     "class" : "text-center"
    // },{
    //     "data" : "continueTimeStr",
    //     "class" : "text-center"
    // },{
    //     "data" : "groupName",
    //     "class" : "text-center"
    // },{
    //     "data" : null,
    //     "class" : "text-center",
    //     render: function (data, type, row, meta) {
    //         //return data.longtitude+","+data.latitude
    //         return "加载中..."
    //     }
    // }, {
    //     "data": null,
    //     "class": "text-center hideTr",
    //     render: function (data, type, row, meta) {
    //         if (row.importantData == 1){
    //             return "<a class='importantTr'></a>"
    //         }
    //     }
    // }];
    var allTableDataColumns = [ {
        //第一列，用来显示序号
        "data" : null,
        "class" : "text-center"
    },{
        "data" : "plateNumber", //监控对象
        "class" : "text-center"
    },{
        "data" : "groupName", //所属企业
        "class" : "text-center"
    },{
        "data" : "workingPosition", //工作状态
        "class" : "text-center",
        render: function (data, type, row, meta) {
            if (row.workingPosition == '0'){
                return '<i class="haltState"></i> 停机'
            } else if (row.workingPosition == '1') {
                return '<i class="workState"></i> 工作'
            }else if (row.workingPosition == '2') {
                return '<i class="standByState"></i> 待机'
            }
                return '无'
            
        }
    },{
        "data" : "vtimeStr", //时间
        "class" : "text-center"
    },{
        "data" : "continueTimeStr", //状态持续时长
        "class" : "text-center"
    },{
        "data" : "checkData", //瞬时流量
        "class" : "text-center"
    },{
        "data" : null,
        "class" : "text-center",
        render: function (data, type, row, meta) {
            //return data.longtitude+","+data.latitude
            return "加载中..."
        }
    }, {
        "data": null,
        "class": "text-center hideTr",
        render: function (data, type, row, meta) {
            if (row.workingPosition == '0'){
                return "<a class='haltTr'></a>";
            } else if (row.workingPosition == '1') {
                return "<a class='workTr'></a>";
            }else if (row.workingPosition == '2') {
                return "<a class='standByTr'></a>";
            }
        }
    }];



    //工作数据等列定义
    var workTableComlumns = [ {
        //第一列，用来显示序号
        "data" : null,
        "class" : "text-center"
    },{
        "data" : "plateNumber", //监控对象
        "class" : "text-center"
    },{
        "data" : "groupName", //所属企业
        "class" : "text-center"
    },{
        "data" : "workingPositionStr", //工作状态
        "class" : "text-center"
    },{
        "data" : "vtimeStr", //时间
        "class" : "text-center"
    },{
        "data" : "continueTimeStr", //状态持续时长
        "class" : "text-center"
    },{
        "data" : "checkData", //瞬时流量
        "class" : "text-center"
    },{
        "data" : null,
        "class" : "text-center",
        render: function (data, type, row, meta) {
            //return data.longtitude+","+data.latitude
            return "加载中..."
        }
    }, {
        "data": null,
        "class": "text-center hideTr",
        render: function (data, type, row, meta) {
            if (row.importantData == 1){
                return "<a class='importantTr'></a>"
            }
        }
    }];


    oilstatiscal = {
        init: function () {
            if (searchSate) {
                for(var i = 0, len = accTemp.length; i < len; i++){
                    if(accTemp[i] == 1){
                        accTemp[i] = 0;
                    }else if(accTemp[i] == 0){
                        accTemp[i] = 1
                    };
                };
                searchSate = false;
            }
            myChart = echarts.init(document.getElementById('sjcontainer'));


            var flowrate2=[];
            var speedThresholdArr=[];
            for (var i=0;i<date.length;i++){
                if(workInspectionMethodType == 0 || workInspectionMethodType == 1){
                    flowrate2.push(thresholdWorkFlow)
                }else if (workInspectionMethodType == 2){
                    speedThresholdArr.push(speedThreshold)
                }
            }
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var relVal = "";
                        var thresholdUnit;
                        if (fluxOrvoltage == '瞬时流量'){
                            thresholdUnit = 'L/h'
                        } else if (fluxOrvoltage == '电压'){
                            thresholdUnit = 'V'
                        }

                        var unit = ['','L/h','','km/h','L/h','',thresholdUnit,'','',thresholdUnit];
                        if(a[0]){
                            relVal = a[0].name;
                            if(a[0].data == null && a[0].seriesName != '停机' && a[0].seriesName != '待机' && a[0].seriesName != '工作'){
                                relVal = "无相关数据";
                            }

                            var ifDataNull = false;
                            a.forEach(function (val,index) {
                                if (val.data != null){
                                    ifDataNull = true;
                                }
                            })

                            if (!ifDataNull){
                                relVal = "无相关数据";
                                return relVal;
                            }

                            if (ifEmptyData[a[0].dataIndex] == 'empty'){
                                relVal = '无相关数据';
                                return relVal;
                            }

                            // if (a[3].data == 'empty'){
                            //     relVal = '无相关数据'
                            // } else{
                            var isEffectiveData = true; // 是否无效数据
                            for(var i = 0; i < a.length; i++){

                                if(workInspectionMethodType == '1' && a[i].seriesName == '工作阈值'){
                                    continue;
                                }

                                if(workInspectionMethodType != '2' && (a[i].seriesName == '波动值' || a[i].seriesName == '速度阈值')){
                                    continue;
                                }
                                if(a[i].seriesName == '状态持续时长'){
                                    continue;
                                }


                                if (a[i].seriesName == "工作" ){
                                    if (a[i].data == '1'){
                                        relVal +="<br/>工作状态："+ a[i].seriesName;
                                        isEffectiveData = false;
                                    }

                                }else
                                if (a[i].seriesName == "停机" ){
                                    if (a[i].data == '1'){
                                        relVal +="<br/>工作状态："+ a[i].seriesName;
                                        isEffectiveData = false;
                                    }

                                }else
                                if (a[i].seriesName == "待机"){
                                    if (a[i].data == '1'){
                                        relVal +="<br/>工作状态："+ a[i].seriesName;
                                        isEffectiveData = false;
                                    }

                                }
                                else{
                                    if(a[i].seriesName === '波动阈值' || a[i].seriesName === '阈值'){
                                        relVal += "<br/>"+ a[i].seriesName +"："+ thresholdWorkFlow + "L/h";
                                    }else if(a[i].seriesName == '工作阈值'){
                                        relVal += "<br/>"+ a[i].seriesName +"："+ thresholdWorkFlow + "V";
                                    }else if(a[i].seriesName == '速度阈值'){
                                        relVal += "<br/>"+ a[i].seriesName +"："+ speedThreshold + "km/h";
                                    }else {
                                        if (a[i].value === '' || a[i].value == undefined){
                                            relVal += "<br/>"+ a[i].seriesName +"：";
                                        } else {
                                            if (a[i].seriesName == '电压'){
                                                relVal += "<br/>"+ a[i].seriesName +"："+ a[i].value + "V";
                                            }else {
                                                relVal += "<br/>"+ a[i].seriesName +"："+ a[i].value + unit[a[i].seriesIndex] +"";
                                            }
                                        }
                                    }
                                }

                                if (isEffectiveData && i == '2') {
                                    relVal +="<br/>工作状态：";
                                }
                            }
                            // }
                            return relVal;
                        }
                    }
                },
                legend: {
                    data: chartLegendData,
                    left: 'auto'
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        min: 0,
                        max: 240,
                        position: 'left',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: fluxOrvoltage,
                        scale: true,
                        position: 'right',
                        // offset: 60,
                        min: 0,
                        // max: 600,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '状态',
                        scale: true,
                        show:false,
                        position: 'right',
                        offset: 60,
                        min: 0,
                        max: 1,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                            show:false
                        }
                    }
                ],
                dataZoom: [{
                    type: 'inside'

                }, {
                    start: 0,
                    end: 10,
                    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                    handleSize: '80%',
                    handleStyle: {
                        color: '#fff',
                        shadowBlur: 3,
                        shadowColor: 'rgba(0, 0, 0, 0.6)',
                        shadowOffsetX: 2,
                        shadowOffsetY: 2
                    }
                }],
                series: [
                    {
                        name:'待机',
                        type:'line',
                        step:'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#ffefe0'
                            }
                        },
                        areaStyle: {
                            normal:{
                                color: '#ffefe0'
                            }
                        },
                        data: _daijiArr,
                    },
                    {
                        name:'工作',
                        type:'line',
                        step:'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#ffc1c3'
                            }
                        },
                        areaStyle: {
                            normal:{
                                color: '#ffc1c3'
                            }
                        },
                        data: _workArr,
                    },
                    {
                        name:'停机',
                        type:'line',
                        step:'end',
                        yAxisIndex: 2,
                        // smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#f3f3f3'
                            }
                        },
                        areaStyle: {
                            normal:{
                                color: '#f3f3f3'
                            }
                        },
                        data: _tingjiArr,
                    },
                    {
                        name: '速度',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(153, 204, 0)'
                            }
                        },
                        label:{
                            normal:{
                                formatter :'{value}km/h'
                            }
                        },
                        data: speed
                    },
                    {
                        name: fluxOrvoltage,
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'rgb(0, 255, 255)'
                            }
                        },
                        data: flowrate
                    },
                    {
                        name:'状态持续时长',
                        type:'line',
                        yAxisIndex: 2,
                        smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#000'
                            }
                        },
                        areaStyle: {
                            normal:{
                                color: '#000'
                            }
                        },
                        data: duration_state
                    },
                    // {
                    //     name:'总工作时长',
                    //     type:'line',
                    //     yAxisIndex: 2,
                    //     smooth: true,
                    //     symbol: 'none',
                    //     symbolSize :15,
                    //     showSymbol: false,
                    //     sampling: 'average',
                    //     itemStyle: {
                    //         normal: {
                    //             color: '#000'
                    //         }
                    //     },
                    //     areaStyle: {
                    //         normal:{
                    //             color: '#000'
                    //         }
                    //     },
                    //     data: allWorkTime
                    // },
                    // {
                    //     name:'总待机时长',
                    //     type:'line',
                    //     yAxisIndex: 2,
                    //     smooth: true,
                    //     symbol: 'none',
                    //     symbolSize :15,
                    //     showSymbol: false,
                    //     sampling: 'average',
                    //     itemStyle: {
                    //         normal: {
                    //             color: '#000'
                    //         }
                    //     },
                    //     areaStyle: {
                    //         normal:{
                    //             color: '#000'
                    //         }
                    //     },
                    //     data: allWaitTime
                    // },
                    // {
                    //     name:'总停机时长',
                    //     type:'line',
                    //     yAxisIndex: 2,
                    //     smooth: true,
                    //     symbol: 'none',
                    //     symbolSize :15,
                    //     showSymbol: false,
                    //     sampling: 'average',
                    //     itemStyle: {
                    //         normal: {
                    //             color: '#000'
                    //         }
                    //     },
                    //     areaStyle: {
                    //         normal:{
                    //             color: '#000'
                    //         }
                    //     },
                    //     data: allstopTime
                    // },
                    {
                        // name: '工作阈值',
                        name: '波动值',
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        show:false,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'transparent'
                            }
                        },
                        data: fluctuateVal //波动值
                    },
                    {
                        // name: '工作阈值',
                        name: thresholdValueType,
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'red'
                            }
                        },
                        data: flowrate2
                    },
                    {
                        name: '速度阈值',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize :15,
                        showSymbol: false,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: 'red'
                            }
                        },
                        label:{
                            normal:{
                                formatter :'{value}km/h'
                            }
                        },
                        data: speedThresholdArr
                    },
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        treeInit: function(){
            var setting = {
                async: {
                    //url: "/clbs/m/functionconfig/fence/bindfence/getTreeByMonitorCount",
                    url: oilstatiscal.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple","icoType": "0"},
                    dataFilter: oilstatiscal.ajaxDataFilter
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
                    beforeClick: oilstatiscal.beforeClickVehicle,
                    onCheck: oilstatiscal.onCheckVehicle,
                    beforeCheck: oilstatiscal.zTreeBeforeCheck,
                    onExpand: oilstatiscal.zTreeOnExpand,
                    //beforeAsync: oilstatiscal.zTreeBeforeAsync,
                    onAsyncSuccess: oilstatiscal.zTreeOnAsyncSuccess,
                    onNodeCreated: oilstatiscal.zTreeOnNodeCreated
                }
            };
            $.fn.zTree.init($("#treeDemo"), setting, null);
            $("[data-toggle='tooltip']").tooltip();
        },
        //模糊查询树
        searchVehicleTree: function (param) {

            ifAllCheck = false;//模糊查询不自动勾选

            crrentSubV = [];
            $('#charSelect').val('');
            if (param == null || param == undefined || param == '') {
                bflag = true;
                // $.fn.zTree.init($("#treeDemo"), setting, null);
                oilstatiscal.treeInit()
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
                        dataFilter: oilstatiscal.ajaxQueryDataFilter
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
                        beforeClick: oilstatiscal.beforeClickVehicle,
                        onCheck: oilstatiscal.onCheckVehicle,
                        // beforeCheck: oilstatiscal.zTreeBeforeCheck,
                        onExpand: oilstatiscal.zTreeOnExpand,
                        //beforeAsync: oilstatiscal.zTreeBeforeAsync,
                        // onAsyncSuccess: oilstatiscal.zTreeOnAsyncSuccess,
                        onNodeCreated: oilstatiscal.zTreeOnNodeCreated
                    }
                };
                $.fn.zTree.init($("#treeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            responseData = JSON.parse(ungzip(responseData))
            var nodesArr;
            if($('#queryType').val() == "vehicle"){
                nodesArr = filterQueryResult(responseData, crrentSubV);
            }else {
                nodesArr = responseData;
            }
            for (var i=0;i<nodesArr.length;i++){
                nodesArr[i].open = true;
            }
            return nodesArr;
        },
        getTreeUrl : function (treeId, treeNode){
            if (treeNode == null) {
                return "/clbs/m/personalized/ico/vehicleTree";
            }else if(treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=monitor";
            }
        },
        ajaxDataFilter: function(treeId, parentNode, responseData){
            var rData;
            if (!responseData.msg) {
                rData = responseData;
            } else {
                rData = responseData.msg;
            }
            //if (responseData.msg) {
            var obj = JSON.parse(ungzip(rData));
            var data;
            if (obj.tree != null && obj.tree != undefined) {
                data = obj.tree;
                size = obj.size;
            }else{
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                data[i].open = true;
            }
            return data;
        },
        zTreeBeforeAsync: function () {
            return bflag;
        },
        zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg){

            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            // if(size <= 5000){
            //     treeObj.checkAllNodes(true);
            // }
            if(size <= 5000 && ifAllCheck){
                treeObj.checkAllNodes(true);
            }
            oilstatiscal.getCharSelect(treeObj);

            bflag = false;
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
        beforeClickVehicle: function(treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeBeforeCheck : function(treeId, treeNode){
            var flag = true;
            if (!treeNode.checked) {
                if(treeNode.type == "group" || treeNode.type == "assignment"){ //若勾选的为组织或分组
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;

                    json_ajax("post", "/clbs/m/personalized/ico/getCheckedVehicle",
                        "json", false, {"parentId": treeNode.id,"type": treeNode.type}, function (data) {
                            if(data.success){
                                nodesLength += data.obj;
                            }else{
                                layer.msg(data.msg);
                            }
                        });

                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i=0;i<nodes.length;i++) {
                        nodeId = nodes[i].id;
                        if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
                            //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                            var nd = zTree.getNodeByParam("tId",nodes[i].tId,treeNode);
                            if(nd == null && $.inArray(nodeId,ns) == -1){
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength += ns.length;
                }else if(treeNode.type == "people" || treeNode.type == "vehicle"){ //若勾选的为监控对象
                    var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                        .getCheckedNodes(true), v = "";
                    var nodesLength = 0;
                    //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                    var ns = [];
                    //节点id
                    var nodeId;
                    for (var i=0;i<nodes.length;i++) {
                        nodeId = nodes[i].id;
                        if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
                            if($.inArray(nodeId,ns) == -1){
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = ns.length + 1;
                }
                if(nodesLength > 5000){
                    layer.msg(maxSelectItem);
                    flag = false;
                }
            }
            if(flag){
                //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                if(treeNode.type == "group" && !treeNode.checked){
                    checkFlag = true;
                }
            }
            return flag;
        },
        onCheckVehicle: function(e, treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            //若为取消勾选则不展开节点
            if(treeNode.checked){
                isSearch = false;
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
                setTimeout(() => {
                    oilstatiscal.validates();
                }, 600);
            }
            if(treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)){
                oilstatiscal.getCharSelect(zTree);
            }
            crrentSubV = [];
            crrentSubV.push(treeNode.id);
        },
        zTreeOnExpand : function (event, treeId, treeNode) {
            //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
            if(treeNode.type == "group" && !checkFlag){
                return;
            }
            //初始化勾选操作判断表示
            checkFlag = false;
            var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (treeNode.type == "group"){
                var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
                json_ajax("post", url, "json", false, {"groupId": treeNode.id,"isChecked":treeNode.checked,"monitorType":"vehicle"}, function (data) {
                    var result = data.obj;
                    if (result != null && result != undefined){
                        $.each(result, function(i) {
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
            var nodes = treeObj.getCheckedNodes(true);
            var allNodes = treeObj.getNodes();
            if (nodes.length > 0) {
                $("#groupSelect").val(allNodes[0].name);
            } else {
                $("#groupSelect").val("");
            }
            $("#charSelect").val("").attr("data-id","").bsSuggest("destroy");
            var arrays = [];
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].type == "vehicle" || nodes[i].type == "people" || nodes[i].type == "thing") {
                    for (var k = 0; k < vehicleList.length; k++) {
                        if (nodes[i].id == vehicleList[k].vehicleId) {
                            arrays.push({
                                name: nodes[i].name,
                                id: nodes[i].id
                            });
                        }
                    }
                }
            }
            // 去掉数组中id相同的元素
            arrays = objArrRemoveRepeat(arrays);
            var deviceDataList = {value: arrays};
            $("#charSelect").empty();
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
            if(deviceDataList.value.length > 0){
                $("#charSelect").val(deviceDataList.value[0].name).attr("data-id",deviceDataList.value[0].id);
            }
            $("#groupSelect,#groupSelectSpan").bind("click",oilstatiscal.showMenu);
            $("#button").removeClass('disabled loading-state-button').find('i').attr("class", 'caret');
        },
        unique: function(arr){
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        showMenu: function(e){
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
          $("body").bind("mousedown", oilstatiscal.onBodyDown);
        },
        hideMenu: function(){
            $("#menuContent").fadeOut("fast");
            $("body").unbind("mousedown", oilstatiscal.onBodyDown);
        },
        onBodyDown: function(event){
            if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuContent").length > 0)) {
                oilstatiscal.hideMenu();
            }
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
                tMonth = oilstatiscal.doHandleMonth(tMonth + 1);
                tDate = oilstatiscal.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " " + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24 * parseInt(num);
                today.setTime(end_milliseconds); // 注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = oilstatiscal.doHandleMonth(endMonth + 1);
                endDate = oilstatiscal.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " " + "23:59:59";
            } else {
                var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
                var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
                var dateList = new Date();
                dateList.setTime(vtoday_milliseconds);
                var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
                var vDate = dateList.getDate();
                vMonth = oilstatiscal.doHandleMonth(vMonth + 1);
                vDate = oilstatiscal.doHandleMonth(vDate);
                startTime = vYear + "-" + vMonth + "-" + vDate + " "
                    + "00:00:00";
                if(day == 1){
                    endTime = vYear + "-" + vMonth + "-" + vDate + " "
                        + "23:59:59";
                }else{
                    var endNum = -1;
                    var vendtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * parseInt(endNum);
                    var dateEnd = new Date();
                    dateEnd.setTime(vendtoday_milliseconds);
                    var vendYear = dateEnd.getFullYear();
                    var vendMonth = dateEnd.getMonth();
                    var vendDate = dateEnd.getDate();
                    vendMonth = oilstatiscal.doHandleMonth(vendMonth + 1);
                    vendDate = oilstatiscal.doHandleMonth(vendDate);
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
        // 当前时间
        nowDay: function () {
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
        },
        // ajax请求数据
        getSensorMessage : function (band) {
            var flog;
            var url = "/clbs/v/oilmassmgt/oilquantitystatistics/getSensorMessage";
            var data = {"band": band};
            json_ajax("POST", url, "json", false,data,function(data){
                flog = data;
            });
            return flog;
        },
        drivingMile : function(data){
            var startMile;
            var endMile;
            var length=data.length;
            if(flogKey == "true"){
                startMile = Number(data[0].mileageTotal === undefined ? 0 : data[0].mileageTotal);
                for(var i=length-1;i>=0;i--){
                    var mileageTotal=Number(data[i].mileageTotal === undefined ? 0 : data[i].mileageTotal);
                    if(mileageTotal!=0){
                        endMile=mileageTotal;
                        break;
                    }
                }
            }else{
                startMile = Number(data[0].gpsMile);
                for(var i=length-1;i>=0;i--){
                    var gpsMile = Number(data[i].gpsMile);
                    if(gpsMile!=0){
                        endMile=gpsMile;
                        break;
                    }
                }
            }
            // 里程
            return endMile - startMile;
        },
        fiterNumber : function(data){
            if(data==null||data==undefined||data==""){
                return data;
            }
                var data=data.toString();
                data=parseFloat(data);
                return data;
            
        },
        airConditionStatus : function(io,data){
            // 此方法，IO[0]对应空调状态IO口，IO[1],对应 1 常开或是 2 常关，0也是常关，
            // return 返回值，0（关闭）/ 1（开启） / 2（未设置传感器）,
            if(io[1]==0){
                return 2;
            }
            var airStatus=0;
            if(io[0]==1){
                if(data.ioOne!=undefined){
                    airStatus=data.ioOne;
                }else{
                    return "";
                }
            }else if(io[0]==2){
                if(data.ioOne!=undefined){
                    airStatus=data.ioTwo;
                }else{
                    return "";
                }
            }else if(io[0]==3){
                if(data.ioThree!=undefined){
                    airStatus=data.ioThree;
                }else{
                    return "";
                }
            }else if(io[0]==4){
                if(data.ioFour!=undefined){
                    airStatus=data.ioFour;
                }else{
                    return "";
                }
            }else{
                return 2;
            }
            if(io[1]==1){
                if(airStatus==0){
                    airStatus=1;
                }else{
                    airStatus=0;
                }
            }
            return airStatus;
        },
        //上一天(此段js可以删除，页面没有这个按钮)
        upDay: function(){
            oilstatiscal.startDay(1);
            var charNum = $("#charSelect").attr("data-id");
            var groupValue = $("#groupSelect").val();
            if (charNum != "" && groupValue != "") {
                var dateValue = new Date().getTime();
                var startTimeValue = new Date(startTime.split(" ")[0].replace(/-/g,"/")).getTime();
                if(startTimeValue <= dateValue){
                    // oilstatiscal.ajaxList(charNum, startTime, endTime);
                    // oilstatiscal.validates()
                    var parme = {
                        vehicleId:charNum,
                        startTimeStr:startTime,
                        endTimeStr:endTime,
                        sensorSequence:sensorSequence
                    }
                    oilstatiscal.inquireChart(parme);
                }else{

                    var timeInterval = $('#timeInterval').val().split('--');
                    startTime = timeInterval[0];
                    endTime = timeInterval[1];

                    layer.msg("暂时没办法穿越，明天我再帮您看吧！");
                }
            } else {
                layer.msg("请选择监控对象！", {move: false});
            }
        },
        // 今天
        todayClick: function () {
            oilstatiscal.nowDay();
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!oilstatiscal.validates()) return;
            var parme = {
                vehicleId:charNum,
                startTimeStr:startTime,
                endTimeStr:endTime,
                sensorSequence:sensorSequence
            }
            oilstatiscal.inquireChart(parme);
        },
        // 前一天
        yesterdayClick: function () {
            oilstatiscal.startDay(-1);
            var startValue = $("#startTime")
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!oilstatiscal.validates()) return;
            var parme = {
                vehicleId:charNum,
                startTimeStr:startTime,
                endTimeStr:endTime,
                sensorSequence:sensorSequence
            }
            oilstatiscal.inquireChart(parme);
        },
        // 近三天
        nearlyThreeDays: function () {
            oilstatiscal.startDay(-3);
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!oilstatiscal.validates()) return;
            var parme = {
                vehicleId:charNum,
                startTimeStr:startTime,
                endTimeStr:endTime,
                sensorSequence:sensorSequence
            }
            oilstatiscal.inquireChart(parme);
        },
        // 近七天
        nearlySevenDays: function () {
            oilstatiscal.startDay(-7);
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!oilstatiscal.validates()) return;
            var parme = {
                vehicleId:charNum,
                startTimeStr:startTime,
                endTimeStr:endTime,
                sensorSequence:sensorSequence
            }
            oilstatiscal.inquireChart(parme);
        },
        // 查询
        inquireClick: function () {
            var groupValue = $("#groupSelect").val();
            var timeInterval = $('#timeInterval').val().split('--');
            startTime = timeInterval[0];
            endTime = timeInterval[1];
            var charNum = $("#charSelect").attr("data-id");
            _vehicleId = charNum;
            if (!oilstatiscal.validates()) return;
            var parme = {
                vehicleId:charNum,
                startTimeStr:startTime,
                endTimeStr:endTime,
                sensorSequence:sensorSequence
            }
            oilstatiscal.inquireChart(parme);
        },
        inquireChart:function (parme) {
            date = [];
            speed=[];
            flowrate = [];

            fluctuateVal = []

            var brandName=$("input[name='charSelect']").val();
            if (brandName.length > 8) {
                $("#carName").attr("title",brandName).tooltip('fixTitle');
                brandName = brandName.substring(0, 7) + '...';
            }else{
                $('#carName').removeAttr('data-original-title')
            }
            $("#carName").text(brandName);

            $(".toopTip-btn-left,.toopTip-btn-right").css("display","inline-block");
            thresholdValueType = '工作阈值';
            json_ajax('post','/clbs/v/workhourmgt/workHourStatistics/getChartInfo','json',true,parme,oilstatiscal.ChartajaxDataCallback)
        },
        // 图表数据
        ChartajaxDataCallback:function(data){
            workInspectionMethodType = null;
            chartLegendData = [];
            _daijiArr= [];
            _tingjiArr=[];
            _workArr = [];

            duration_state=[];//状态持续时长
            // allWorkTime=[];//总工作时长
            // allstopTime=[];//总停机时长
            // allWaitTime=[];//总待机时长

            ifEmptyData=[];

            if (data.success && data.obj.workHourInfo){
                var workHourInfo = JSON.parse(ungzip(data.obj.workHourInfo))

                layer.closeAll('loading');
                $('#timeInterval').val(startTime + '--' + endTime);

                $('#allTime').text(data.obj.totalDuration);
                $('#workTime').text(data.obj.workDuration)
                $('#waitTime').text(data.obj.standByDuration)
                $('#stopTime').text(data.obj.haltDuration)
                $('#noDataTime').text(data.obj.invalidDuration)


                thresholdWorkFlow = (data.obj.thresholdValue != undefined && data.obj.thresholdValue != null && data.obj.thresholdValue != "null") ? Number(data.obj.thresholdValue) : 0;
                if (data.obj.workInspectionMethod == '1') {
                    thresholdValueType = '阈值';
                    fluxOrvoltage = '瞬时流量';
                    $('#fluxOrvoltage-th').text('瞬时流量');
                    $('#spillReport').show();
                    $('#waitTimeLi').show();

                    $('#fluctuateVal-th').hide();

                    workInspectionMethodType = 1;
                    chartLegendData = [
                        {name:'速度'},
                        {name:'工作'},
                        {name:'停机'},
                        {name:'瞬时流量'}
                    ];
                }else if(data.obj.workInspectionMethod == '0'){
                    thresholdValueType = '工作阈值'
                    fluxOrvoltage = '电压';
                    $('#fluxOrvoltage-th').text('电压');
                    $('#spillReport').hide();
                    $('#waitTimeLi').hide();

                    chartLegendData = [
                        {name:'速度'},
                        {name:'工作'},
                        {name:'停机'},
                        {name:'电压'}
                    ];

                    workInspectionMethodType=0;
                }else if(data.obj.workInspectionMethod == '2'){
                    fluxOrvoltage = '瞬时流量';
                    $('#fluxOrvoltage-th').text('瞬时流量');
                    $('#spillReport').hide();
                    $('#waitTimeLi').show();
                    speedThreshold = (data.obj.speedThreshold != undefined && data.obj.speedThreshold != null && data.obj.speedThreshold != "null") ? Number(data.obj.speedThreshold) : 0;
                    thresholdValueType='波动阈值'

                    chartLegendData = [
                        {name:'速度'},
                        {name:'工作'},
                        {name:'待机'},
                        {name:'停机'},
                        {name:'瞬时流量'}
                        // {name:'电压'}
                    ];

                    workInspectionMethodType=2;
                }



                if (workHourInfo.length>0){
                    // 这一段组装待机停机工作时间段数组
                    _daijiArr= [];
                    _tingjiArr=[];
                    _workArr = [];

                    for (var i = 0, len = workHourInfo.length-1; i <= len; i++) {

                        if (workHourInfo[i].effectiveData == '1'){ //无效数据
                            if(workHourInfo[i].speed == null){
                                workHourInfo[i].speed = 0;
                            }
                            workHourInfo[i].continueTimeStr = '';
                            workHourInfo[i].workTotalStr = '';
                            workHourInfo[i].checkData = '';
                            workHourInfo[i].haltTotalStr = '';
                            workHourInfo[i].standbyTotalStr = '';
                        }

                        if (workHourInfo[i].effectiveData == '3'){ //啥都不显示
                            // workHourInfo[i].speed = 'empty'; //设置一个empty在formater里判断
                            ifEmptyData.push('empty')
                        }else {
                            ifEmptyData.push('1')
                        }

                        date.push(workHourInfo[i].vtimeStr);

                        speed.push(workHourInfo[i].speed);

                        duration_state.push(workHourInfo[i].continueTimeStr);
                        // allWorkTime.push(workHourInfo[i].workTotalStr);
                        // allstopTime.push(workHourInfo[i].haltTotalStr);
                        // allWaitTime.push(workHourInfo[i].standbyTotalStr);

                        // if (!workHourInfo[i].checkData){
                        //     workHourInfo[i].checkData = 0
                        // }
                        flowrate.push(workHourInfo[i].checkData)

                        fluctuateVal.push(workHourInfo[i].fluctuateValue)

                        if(!workHourInfo[i].workingPosition){
                            workHourInfo[i].workingPosition = '0';
                        }
                        // 这一段组装待机停机工作时间段数组
                        if (workHourInfo[i].workingPosition == '2'){

                            if (workHourInfo[i].effectiveData == '1' || workHourInfo[i].effectiveData == '3'){ //无效数据
                                _daijiArr.push(0);
                            } else {
                                _daijiArr.push(1);
                            }

                            _tingjiArr.push(0);
                            _workArr.push(0);
                        }else if (workHourInfo[i].workingPosition == '1'){

                            if (workHourInfo[i].effectiveData == '1' || workHourInfo[i].effectiveData == '3'){ //无效数据
                                _workArr.push(0);
                            } else {
                                _workArr.push(1);
                            }

                            _daijiArr.push(0);
                            _tingjiArr.push(0);
                            // _workArr.push(1)
                        }else if (workHourInfo[i].workingPosition == '0'){

                            if (workHourInfo[i].effectiveData == '1' || workHourInfo[i].effectiveData == '3'){ //无效数据
                                _tingjiArr.push(0);
                            } else {
                                _tingjiArr.push(1);
                            }

                            _daijiArr.push(0);
                            // _tingjiArr.push(1);
                            _workArr.push(0)
                        }
                    }

                    // for (var i=0;i<_daijiArr.length;i++){
                    //     if (_daijiArr[i] == 1 && _daijiArr[i+1]=== null && i<_daijiArr.length-1){
                    //         _daijiArr[i+1] = 1;
                    //         i++;
                    //     }
                    // }
                    // for (var j=0;j<_tingjiArr.length;j++){
                    //     if (_tingjiArr[j] == 1 && _tingjiArr[j+i] === null && j<_tingjiArr.length-1){
                    //         _tingjiArr[j+1] = 1;
                    //         j++;
                    //     }
                    // }
                    // for (var k=0;k<_workArr.length;k++){
                    //     if (_workArr[k] == 1 && _workArr[k+1] == null && k<_workArr.length-1){
                    //         _workArr[k+1] = 1;
                    //         k++;
                    //     }
                    // }

                    $("#graphShow").show();
                    $("#showClick").attr("class","fa fa-chevron-down");
                    searchState = true;
                    oilstatiscal.init()
                }else {
                    $("#showClick").attr("class", "fa fa-chevron-up");
                    $("#graphShow").hide();
                    $("#allTime").text("0小时0分");
                    $("#workTime").text("0小时0分");
                    $("#waitTime").text("0小时0分");
                    $("#stopTime").text("0小时0分");
                    $("#oilTable_wrapper").children("div.row").hide();
                }
                $("#graphShow").show();
                $("#showClick").attr("class","fa fa-chevron-down");
                searchState = true;
                oilstatiscal.init();

                $('.dataTableShow li').removeClass('active');
                $('#allReport').addClass('active')

                if (oilstatiscal.validates()) {
                    $("#oilTable tbody").html("");
                    var parme = {
                        'url':'/clbs/v/workhourmgt/workHourStatistics/getTotalDataFormInfo',
                        'vehicleId':$("#charSelect").attr("data-id"),
                        'startTimeStr':startTime,
                        'endTimeStr':endTime,
                        'sensorSequence':sensorSequence
                    }

                    $('#oilTableBox').removeClass('active').addClass('active');
                    $('#classifyTableBox').removeClass('active');
                    thresholdValueType = '工作阈值';
                    oilstatiscal.infoinputTab(parme,allTableDataColumns,'oilTable');
                }
            }else {
                _daijiArr= [];
                _tingjiArr=[];
                _workArr = [];
                oilstatiscal.init();
                if(myTable != undefined){
                    myTable.dataTable.clear();
                    myTable.dataTable.draw();
                }
                layer.msg(data.msg);
            }
        },
        // 勾选数据
        //时间戳转换日期 
        UnixToDate: function(unixTime, isFull, timeZone) {
            if (typeof (timeZone) == 'number') {
                unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
            }
            var time = new Date(unixTime * 1000);
            var ymdhis = "";
            ymdhis += time.getFullYear() + "-";
            ymdhis += (time.getMonth() + 1) + "-";
            ymdhis += time.getDate();
            if (isFull === true) {
                ymdhis += " " + time.getHours() + ":";
                ymdhis += time.getMinutes() + ":";
                ymdhis += time.getSeconds();
            }
            return ymdhis;
        },
        showClick: function () {
            if ($(this).hasClass("fa-chevron-up")) {
                $(this).attr("class", "fa fa-chevron-down");
                $("#graphShow").show();
            } else {
                $(this).attr("class", "fa fa-chevron-up");
                $("#graphShow").hide('300');
            }
        },
        draggle: function () {
            $("#showClick").attr("class", "fa fa-chevron-down");
            $("#graphShow").show();
        },
        //创建表格
        infoinputTab: function(parme,columns,tableDiv){
            var columnDefs = [ {
                //第一列，用来显示序号
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            } ];
            var ajaxDataParamFun = function (d) {
                d.vehicleId = parme.vehicleId; //模糊查询
                d.startTimeStr = parme.startTimeStr;
                d.endTimeStr = parme.endTimeStr;
                d.sensorSequence = parme.sensorSequence;
            };


            //表格setting
            var setting = {
                listUrl : parme.url,
                columnDefs : columnDefs, //表格列定义
                columns : columns, //表格列
                dataTableDiv : tableDiv, //表格
                ajaxDataParamFun: ajaxDataParamFun, //ajax参数
                pageable : true, //是否分页
                showIndexColumn : true, //是否显示第一列的索引列
                enabledChange : true,
                getAddress: true,//是否逆地理编码
                address_index: 8,
                drawCallbackFun:oilstatiscal.drawCallbackFun
            };
            //创建表格
            myTable = new TG_Tabel.createNew(setting);


            myTable.init();

            // $('.fluctuateValueTd').hide()

        },
        // 创建工作数据待机数据停机数据表格
        drawCallbackFun:function(e, settings, json){ //给重要的数据加上颜色
            // $('#oilTable .workTr').each(function (index,ele) {
            //     $(this).closest('tr').addClass('work')
            // })
            // $('#oilTable .haltTr').each(function (index,ele) {
            //     $(this).closest('tr').addClass('halt')
            // })
            // $('#oilTable .standByTr').each(function (index,ele) {
            //     $(this).closest('tr').addClass('standBy')
            // })
        },
        toHHMMSS: function(data){
            var totalSeconds=data*60*60;
            var hour = Math.floor(totalSeconds/60/60);
            var minute = Math.floor(totalSeconds/60%60);
            var second = Math.floor(totalSeconds%60);
            return hour!=0 ? hour+"小时"+minute+"分"+second+"秒" : minute != 0 ? minute+"分"+second+"秒" : second != 0 ? second+"秒" : 0
        },
        removeClass: function(){
            var dataList = $(".dataTableShow");
            for(var i = 0; i < 3; i++){
                dataList.children("li").removeClass("active");
            }
        },
        allReportClick: function(){
            oilstatiscal.removeClass();
            $(this).addClass("active");
            $('#oilTableBox').removeClass('active').addClass('active');
            $('#classifyTableBox').removeClass('active');

            if(date.length!=0) {

                if (oilstatiscal.validates()) {
                    $("#oilTable tbody").html("");
                    var parme = {
                        'url':'/clbs/v/workhourmgt/workHourStatistics/getTotalDataFormInfo',
                        'vehicleId':_vehicleId,
                        'startTimeStr':startTime,
                        'endTimeStr':endTime,
                        'sensorSequence':sensorSequence
                    }
                    oilstatiscal.infoinputTab(parme,allTableDataColumns,'oilTable');
                }
            }
        },
        amountReportClick: function(){ //工作数据
            oilstatiscal.removeClass();
            $(this).addClass("active");

            $('#classifyTableBox').removeClass('active').addClass('active');
            $('#oilTableBox').removeClass('active');

            if(date.length!=0) {
                if (oilstatiscal.validates()) {

                    $("#classifyTable tbody").html("");
                    var parme = {
                        'url':'/clbs/v/workhourmgt/workHourStatistics/getWorkingDataFormInfo',
                        'vehicleId':_vehicleId,
                        'startTimeStr':startTime,
                        'endTimeStr':endTime,
                        'sensorSequence':sensorSequence
                    }
                    oilstatiscal.infoinputTab(parme,workTableComlumns,'classifyTable');
                }
            }

            //点击显示标注
            $('#classifyTable tbody').on( 'click', 'tr', function () {
                // $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings("").find("td").css('background-color', "#fff");
                var backgroundcolor=$(this).find("td").css('background-color');
                if(backgroundcolor=="rgb(220, 245, 255)"){
                    // $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    // $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    $("#dataTable tbody tr").siblings().find("td").css('background-color', "#fff");
                }else{
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var timeStr=$(this).find("td").eq("4").text();
                    myChart.dispatchAction({
                        type: 'showTip',
                        seriesIndex:1 ,//第几条series
                        // dataIndex: seriesIndex,//第几个tooltip
                        name:timeStr
                    });


                }
                myChart.setOption(option);
            } );
        },
        spillReportClick: function(){ //待机数据
            oilstatiscal.removeClass();
            $(this).addClass("active");

            $('#classifyTableBox').removeClass('active').addClass('active');
            $('#oilTableBox').removeClass('active')

            if(date.length!=0) {
                if (oilstatiscal.validates()) {
                    $("#oilTable tbody").html("");
                    var parme = {
                        'url':'/clbs/v/workhourmgt/workHourStatistics/getStandingByDataFormInfo',
                        'vehicleId':_vehicleId,
                        'startTimeStr':startTime,
                        'endTimeStr':endTime,
                        'sensorSequence':sensorSequence
                    }
                    oilstatiscal.infoinputTab(parme,workTableComlumns,'classifyTable');
                }
            }

            //点击显示标注
            $('#classifyTable tbody').on( 'click', 'tr', function () {
                // $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings().find("td").css('background-color', "#fff");
                var backgroundcolor=$(this).find("td").css('background-color');
                if(backgroundcolor=="rgb(220, 245, 255)"){
                    // $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    // $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    $(this).siblings("").find("td").css('background-color', "#fff");
                }else{
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var timeStr=$(this).find("td").eq("4").text();
                    myChart.dispatchAction({
                        type: 'showTip',
                        seriesIndex:1 ,//第几条series
                        // dataIndex: seriesIndex,//第几个tooltip
                        name:timeStr
                    });


                }
                myChart.setOption(option);
            } );

        },
        haltReportClick:function(){ //停机数据
            oilstatiscal.removeClass();
            $(this).addClass("active");

            $('#classifyTableBox').removeClass('active').addClass('active');
            $('#oilTableBox').removeClass('active')

            if(date.length!=0){
                if (oilstatiscal.validates()) {
                    $("#oilTable tbody").html("");
                    var parme = {
                        'url':'/clbs/v/workhourmgt/workHourStatistics/getDowntimeDataFormInfo',
                        'vehicleId':_vehicleId,
                        'startTimeStr':startTime,
                        'endTimeStr':endTime,
                        'sensorSequence':sensorSequence
                    }
                    oilstatiscal.infoinputTab(parme,workTableComlumns,'classifyTable');
                }
            }

            //点击显示标注
            $('#classifyTable tbody').on( 'click', 'tr', function () {
                // $(this).siblings(".odd").find("td").css('background-color', "#f9f9f9");
                $(this).siblings().find("td").css('background-color', "#fff");
                var backgroundcolor=$(this).find("td").css('background-color');
                if(backgroundcolor=="rgb(220, 245, 255)"){
                    // $("#dataTable tbody tr").siblings(".odd").find("td").css('background-color', "#f9f9f9");
                    // $("#dataTable tbody tr").siblings(".even").find("td").css('background-color', "#fff");
                    $(this).siblings().find("td").css('background-color', "#fff");
                }else{
                    $(this).find("td").css('background-color', "#DCF5FF");
                    var timeStr=$(this).find("td").eq("4").text();
                    myChart.dispatchAction({
                        type: 'showTip',
                        seriesIndex:1 ,//第几条series
                        // dataIndex: seriesIndex,//第几个tooltip
                        name:timeStr
                    });


                }
                myChart.setOption(option);
            } );

        },
        timeStamp2String: function(time){
            var time = time.toString();
            var startTimeIndex = time.replace("-","/").replace("-","/");
            var val = Date.parse(startTimeIndex);
            var datetime = new Date();
            datetime.setTime(val);
            var year = datetime.getFullYear();
            var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
            var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
            var hour = datetime.getHours() < 10 ? "0" + datetime.getHours() : datetime.getHours();
            var minute = datetime.getMinutes() < 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
            var second = datetime.getSeconds() < 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
            return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
        },
        timeAdd: function(time){
            var str = time.toString();
            str = str.replace(/-/g, "/");
            return new Date(str);
        },
        GetDateDiff: function(startTime, endTime, diffType){
            // 将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
            startTime = startTime.replace(/-/g, "/");
            endTime = endTime.replace(/-/g, "/");
            // 将计算间隔类性字符转换为小写
            diffType = diffType.toLowerCase();
            var sTime = new Date(startTime); // 开始时间
            var eTime = new Date(endTime); // 结束时间
            // 作为除数的数字
            var divNum = 1;
            switch (diffType) {
                case "second":
                    divNum = 1000;
                    break;
                case "minute":
                    divNum = 1000 * 60;
                    break;
                case "hour":
                    divNum = 1000 * 3600;
                    break;
                case "day":
                    divNum = 1000 * 3600 * 24;
                    break;
                default:
                    break;
            }
            return parseFloat((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); //
        },
        //过滤数组空值
        filterTheNull: function(value){
            for (var i = 0; i < value.length; i++) {
                // if(value[i]!=0){
                if (value[i] == null || value[i] == "" || typeof(value[i]) == "undefined"||value[i] == 0) {
                    value.splice(i, 1);
                    i = i - 1;
                }
                // }
            }
            return value
        },
        //判断数组是否为空
        arrayIsNull: function(value){
            if (value === undefined || value.length == 0) {
                return false;
            }
            return true;
        },

        validates: function(){
            return $("#oilist").validate({
                rules: {
                    groupSelect: {
                        zTreeChecked: "treeDemo"
                    },
                    charSelect:{
                        required: true
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
                    }
                },
                messages: {
                    groupSelect: {
                        zTreeChecked: vehicleSelectBrand
                    },
                    charSelect:{
                        required: "监控对象不能为空"
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
                    }
                }
            }).form();
        },
        left_arrow: function(){
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function(){
                if($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")){
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (nowIndex == 0) {
                $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(trIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(trIndex).attr("data-key"));
            }else {
                $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(nowIndex-1).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nowIndex-1).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        right_arrow: function(){
            $("#button.dropdown-toggle").click();
            $("#button.dropdown-toggle").click();
            var trIndex = $(".table-condensed tr").size() - 1;
            var nowIndex = 0;
            $(".table-condensed tr").each(function(){
                if($(this).attr("data-id") == $("input[name='charSelect']").attr("data-id")){
                    nowIndex = $(this).attr("data-index");
                }
            })
            if (trIndex == nowIndex) {
                $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(0).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(0).attr("data-key"));
            }else {
                var nextIndex = parseInt(nowIndex)+1;
                $("input[name='charSelect']").attr("data-id",$(".table-condensed tr").eq(nextIndex).attr("data-id"));
                $("input[name='charSelect']").val($(".table-condensed tr").eq(nextIndex).attr("data-key"));
            }
            $("#inquireClick").click();
        },
        endTimeClick: function(){
            var width = $(this).width();
            var offset = $(this).offset();
            var left = offset.left - (207 - width);
            $("#laydate_box").css("left", left + "px");
        },
        engineChart:function () {
            $(this).removeClass('active').addClass('active').siblings().removeClass('active');
            sensorSequence = $(this).attr('sensorSequenceVal');
            oilstatiscal.inquireClick()
        },

    }
    $(function () {
        $('input').inputClear();
        oilstatiscal.treeInit();
        // oilstatiscal.init()
        $("#toggle-left").bind("click", function () {
            setTimeout(function () {
                oilstatiscal.init();
            }, 500)
        });
        Array.prototype.isHas = function (a) {
            if (this.length === 0) {
                return false
            }
            ;
            for (var i = 0; i < this.length; i++) {
                if (this[i].seriesName === a) {
                    return true
                }
            }
        };
        oilstatiscal.nowDay();
        $('#timeInterval').dateRangePicker({dateLimit: 7});
        $("#todayClick").bind("click", oilstatiscal.todayClick);
        $("#yesterdayClick,#right-arrow").bind("click", oilstatiscal.yesterdayClick);
        $("#nearlyThreeDays").bind("click", oilstatiscal.nearlyThreeDays);
        $("#nearlySevenDays").bind("click", oilstatiscal.nearlySevenDays);
        $("#inquireClick").bind("click", oilstatiscal.inquireClick);
        $("#showClick").bind("click", oilstatiscal.showClick);
        $("#left-arrow").bind("click",oilstatiscal.upDay);
        $('#allReport').bind("click",oilstatiscal.allReportClick);
        // $('#amountReport').bind("click",oilstatiscal.amountReportClick);
        // $('#spillReport').bind("click",oilstatiscal.spillReportClick);
        // $('#haltReport').bind("click",oilstatiscal.haltReportClick);
        $("#endTime").bind("click",oilstatiscal.endTimeClick);
        $("#groupSelectSpan,#groupSelect").bind("click",oilstatiscal.showMenu); //组织下拉显示
        //$("#groupSelectSpan,#groupSelect").bind("click",showMenuContent); //组织下拉显示
        $('#engine1').bind('click',oilstatiscal.engineChart) //发动机型号
        $('#engine2').bind('click',oilstatiscal.engineChart) //发动机型号2


        $('input').inputClear().on('onClearEvent',function(e,data){
            var id = data.id;
            if(id == 'groupSelect'){
                var param = $("#groupSelect").val();
                oilstatiscal.searchVehicleTree(param);
            };
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
                isSearch = true;
            }
            $("#charSelect").val("").attr("data-id", "").bsSuggest("destroy");
            inputChange = setTimeout(function () {
                if (isSearch){
                    var param = $("#groupSelect").val();
                    oilstatiscal.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
        $('#queryType').on('change', function (){
            var param=$("#groupSelect").val();
            oilstatiscal.searchVehicleTree(param);
        });
    });
}($, window));