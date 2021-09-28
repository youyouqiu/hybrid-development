(function (window, $) {
    var ipAddress = '',
        serverIndex=0,
        progressInx = 0,
        cpuTimer = null,
        networkMax = 3000;

    var myTable,
        cpuChart,
        ramChart,
        diskChart,
        progressChart,
        serverList;

    var ProgressDatas=[],
        serverDetailDatas=[],
        progressChartsDatas=[],
        cpuDatas=[],
        cpuDateDatas=[];

    serverMonitorList = {
        init: function () {
            serverMonitorList.setTable('dataTable');
            serverMonitorList.getDefaultIp();
        },
        //获取默认ip
        getDefaultIp:function(){
            json_ajax("POST", '/clbs/adas/lb/monitor/default', "json", false, {}, function (data) {
                if(data.success == true){
                    data=data.obj;
                    ipAddress=data.defaultIp;
                }
            });
        },
        //table渲染
        setTable: function(id){
            var columns = [
                {
                    "data":'serverName',
                    "class": "text-center"
                },
                {
                    "data": "ipAddress",
                    "class": "text-center",
                },
                {
                    "data": "isOnline",
                    "class": "text-center",
                    render : function(data) {
                        var txt='';
                        if(data==1){
                            txt='<span class="memStatus normal">是</span>';
                        }else if(data==1){
                            txt='<span>否</span>';
                        }

                        return txt;
                    }
                },
                {
                    "data": "systemName",
                    "class": "text-center",
                },
                {
                    "data": "systemWay",
                    "class": "text-center",
                },{
                    "data":'runTime',
                    "class": "text-center",
                    render: function(data){
                        var dateArr = data.split(',');
                        var date =dateArr[0] + '天' + dateArr[1] + '小时' + dateArr[2] + '分钟'
                        return date;
                    }
                },
                {
                    "data": "systemStatus",
                    "class": "text-center",
                    render : function(data){
                        return serverMonitorList.getTableStatus(data);
                    }
                },
                {
                    "data": "cpuStatus",
                    "class": "text-center",
                    render : function(data){
                        return serverMonitorList.getTableStatus(data);
                    }
                },
                {
                    "data": "memStatus",
                    "class": "text-center",
                    render : function(data){
                        return serverMonitorList.getTableStatus(data);
                    }
                },
                {
                    "data": "diskStatus",
                    "class": "text-center",
                    render : function(data){
                        return serverMonitorList.getTableStatus(data);
                    }
                },{
                    "data": "networkStatus",
                    "class": "text-center",
                    render : function(data){
                        return serverMonitorList.getTableStatus(data);
                    }
                },
                {
                    "data": "ipAddress",
                    "class": "text-center",
                    render:function(data){
                        return '<button class="editBtn editBtn-info" data-value="'+data+'"><i class="fa fa-sun-o"></i>详情</button>'
                    }
                }];
            var setting = {
                listUrl: '/clbs/adas/lb/monitor/list',
                columnDefs:null,
                columns: columns, //表格列
                dataTableDiv: id, //表格
                pageable: true, //是否分页
                showIndexColumn: false, //是否显示第一列的索引列sda
                enabledChange: true,
                ajaxCallBack:function(data){
                    var html='';
                    if(data.records.length>0){
                        serverList=data.records;

                        serverList.forEach(function(item,index){
                            if(ipAddress == item.ipAddress){
                                serverIndex = index;
                            }
                            html+='<div class="show-item" data-value="'+item.ipAddress+'">'+item.serverName+'</div>'
                        });

                        $('#serverLists').html(html);
                        $('.show-item').eq(serverIndex).addClass('active');
                        $('#fistServer').text(serverList[serverIndex].serverName);
                        $('#showBox .show-item').bind('click',serverMonitorList.changeSever);
                    }
                },
            }
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        getTableStatus:function(data){
            var txt='';
            if(data==0){
                txt='<span class="memStatus normal">正常</span>';
            }else if(data==1){
                txt='<span class="memStatus abnormal1">异常</span>';
            }else if(data==2){
                txt='<span class="memStatus abnormal2">异常</span>';
            }else{
                txt='<span>--</span>';
            }

            return txt;
        },
        editBtnFuns:function(){
            progressInx=0;
            serverIndex=$(this).parents('tr').index();
            ipAddress=$(this).data("value");
            $('#showBox .show-item').eq(serverIndex).addClass('active').siblings().removeClass('active');
            serverMonitorList.updateDetail();
        },
        changeTableStatus:function(){
            var inx = serverIndex+1;
            var tr = $('#dataTable tr:eq('+inx+')');
            var system = serverMonitorList.getTableStatus(serverDetailDatas.systemStatus),
                cpu = serverMonitorList.getTableStatus(serverDetailDatas.cpuStatus),
                mem = serverMonitorList.getTableStatus(serverDetailDatas.memStatus),
                disk = serverMonitorList.getTableStatus(serverDetailDatas.diskStatus),
                network = serverMonitorList.getTableStatus(serverDetailDatas.networkStatus);

            tr.find('td:eq(6)').html(system);
            tr.find('td:eq(7)').html(cpu);
            tr.find('td:eq(8)').html(mem);
            tr.find('td:eq(9)').html(disk);
            tr.find('td:eq(10)').html(network);
        },
        //详情
        updateDetail:function(){
            if($('#basicInfo').is(':hidden')){
                $('#basicInfo').slideDown();
                $('#serverArrow .fa').removeClass('fa-chevron-up').addClass('fa-chevron-down');
            }

            //初始化图表
            cpuDateDatas=[],cpuDatas=[];
            serverMonitorList.ramCharts();
            serverMonitorList.diskCharts();
            serverMonitorList.cpuCharts();
            serverMonitorList.progressCharts();
            serverMonitorList.showBasic();

            //10秒更新
            if(cpuTimer){
                clearInterval(cpuTimer);
                cpuTimer=null;
            }
            cpuTimer = setInterval(function () {
                serverMonitorList.showBasic();
            }, 10000);
        },
        showBasic:function(){
            var params = {
                ipAddress:ipAddress,
            };

            var startTime = serverMonitorList.formateDate(new Date());
            cpuDateDatas.push(startTime);

            json_ajax("POST", '/clbs/adas/lb/monitor/detail', "json", false, params, function (data) {
                if(data.success == true){
                    serverDetailDatas=data.obj;
                    serverMonitorList.setBasicInfo();
                    serverMonitorList.changeTableStatus();

                    //设置图表数据
                    serverMonitorList.ramChartDatas();
                    serverMonitorList.diskChartDatas();
                    serverMonitorList.cpuChartDatas();
                    serverMonitorList.setProgressList();
                }
            });
        },
        setBasicInfo:function(){
            var isOnline = '',
                sysStatus='';

            switch (serverDetailDatas.isOnline) {
                case "1":
                    isOnline='<span class="memStatus normal">是</span>';
                    break;
                case "2":
                    isOnline='<span class="memStatus outLine">否</span>';
                    break;
                default:
                    break;
            }

            switch (serverDetailDatas.systemStatus) {
                case "0":
                    sysStatus='<span class="memStatus normal">正常</span>';
                    break;
                case "1":
                    sysStatus='<span  class="memStatus abnormal1">异常</span>';
                    break;
                case "2":
                    sysStatus='<span  class="memStatus abnormal2">异常</span>';
                    break;
                default:
                    break;
            }

            //基本信息
            $('#carName .name').text(serverDetailDatas.serverName).data('value',serverDetailDatas.ipAddress);
            $('#serverName').text(serverDetailDatas.serverName);
            $('#info1').text(serverDetailDatas.ipAddress);
            $('#info2').html(isOnline);
            $('#info3').html(sysStatus);
            $('#info4').text(serverDetailDatas.systemName);
            $('#info5').text(serverDetailDatas.systemWay);

            //运行时长
            var dateArr = serverDetailDatas.runTime.split(',');
            $('#H').text(dateArr[0]);
            $('#M').text(dateArr[1]);
            $('#S').text(dateArr[2]);

            //进程total
            $('#processTotal3').text(serverDetailDatas.networkOutflow+'k/s');
            $('#processTotal4').text(serverDetailDatas.networkInflow+'k/s');

            //内存
            var remUse = parseInt(serverDetailDatas.memUse / 1024);
            var remTotal = parseInt(serverDetailDatas.memTotal / 1024);
            $('#ramTips').text('物理内存/已使用内存 '+ remTotal + '/' + remUse +'(MB)');
        },
        //cpu图表
        cpuCharts:function(){
            var option = {
                grid:{
                    containLabel:true,
                    left:50,
                    right:70,
                    // bottom:0,
                    top:20,
                },
                tooltip: {
                    trigger: 'axis',
                },
                dataZoom: {
                    start: 0,
                    end: 100,
                },
                xAxis: {
                    data:cpuDateDatas,
                    type: 'category',
                    boundaryGap:false,
                },
                yAxis: {
                    min:0,
                    max:100,
                    type: 'value',
                    axisLabel: {
                        formatter: '{value}%'
                    },
                },
                series: [{
                    name:'cpu占比',
                    type: 'line'
                }]
            };

            cpuChart = echarts.init(document.getElementById('cpuCharts'));
            cpuChart.setOption(option);
        },
        cpuChartDatas:function(){
            cpuDatas.push(serverDetailDatas.cpuPercent);
            cpuChart.setOption({
                xAxis: {
                    data:cpuDateDatas,
                },
                series: [{
                    data: cpuDatas,
                }]
            })
        },
        formateDate:function(now){
            return now.toLocaleDateString() + '\n' + now.getHours() + now.toLocaleTimeString().substr(-6,6);
        },
        //进程图表
        progressCharts: function(){
            var option = {
                tooltip: {},
                radar: {
                    indicator: [
                        { name: 'CPU占用(100%)', max: 100},
                        { name: '内存占用(100%)', max: 100},
                        { name: '网络流出(3000k/s)', max: 3000},
                        { name: '网络接入(3000k/s)', max: 3000},
                    ],
                    radius:'80%',
                    center: ['40%', '50%'],
                    splitNumber:3,
                    shape:'polygon'
                },
                series: [{
                    type: 'radar',
                }]
            };

            progressChart = echarts.init(document.getElementById('progressCharts'));
            progressChart.setOption(option);
        },
        setProgressList:function(){
            var menuLists = '';
            ProgressDatas=serverDetailDatas.list;
            ProgressDatas.forEach(function(item){
                menuLists+='<p class="item">'+item.processName+'</p>';
            });

            $('.progress-menu .menu-list').html(menuLists);
            var menu = $('.progress-menu .item');
            menu.click(serverMonitorList.getProgressDatas);
            menu.eq(progressInx).trigger('click');
        },
        getProgressDatas:function(){
            progressInx = $(this).index();
            var inx = progressInx;
            var datas=ProgressDatas[inx];
            var cpu = datas.processCpu,
                men = datas.processMem,
                netIn = serverDetailDatas.networkInflow,
                netout = serverDetailDatas.networkOutflow;

            netIn = netIn > networkMax ? networkMax : netIn;
            netout = netout > networkMax ? networkMax : netout;
            $(this).addClass('active').siblings().removeClass('active');
            $('#processTotal1').text(cpu+'%');
            $('#processTotal2').text(men+'%');

            progressChartsDatas=[
                cpu,
                men,
                netIn,
                netout,
            ];
            progressChart.setOption({
                series:[{
                    data : [
                        {
                            value : progressChartsDatas,
                            name:'进程',
                            label: {
                                normal: {
                                    show: false,
                                    // position: 'top',
                                    formatter:function(params){
                                        var data = params.data.value;
                                        var dataInx = data.indexOf(params.value);

                                        if(dataInx == 0 || dataInx==1){
                                            return params.value+'%';
                                        }else{
                                            return params.value + 'k/s';
                                        }
                                    }
                                }
                            }
                        },
                    ]
                }]
            })
        },
        //内存图表
        ramCharts:function(){
            var option = {
                tooltip : {
                    formatter: "{a} <br/>{b} : {c}%"
                },
                series: [
                    {
                        name: '内存',
                        type: 'gauge',
                        radius:'100%',
                        splitNumber:10,
                        center: ['50%', '70%'],
                        min: 0,
                        max: 100,
                        startAngle:180,
                        endAngle:0,
                        axisLabel:{
                            formatter: '{value}%'
                        },
                        axisLine:{
                            lineStyle: {
                                color:[[0.75, '#91c7ae'], [0.9, '#ffab2d'], [1, '#c80002']],
                                width: 20,
                            }
                        },
                        pointer:{
                            length:'80%',
                            width:5,
                        },
                        detail: {
                            formatter:'{value}%',
                            fontSize:12,
                            offsetCenter:[0, '15%'],
                        },
                    }
                ]
            };

            ramChart = echarts.init(document.getElementById('ramCharts'));
            ramChart.setOption(option);
        },
        ramChartDatas:function(){
            var value = parseFloat(serverDetailDatas.memPercent * 100).toFixed(2);
            var status='';

            if(value > 0 && value <= 75){
                status='低';
            }else  if(value > 75 && value <= 90){
                status='中';
            }else  if(value > 90 && value <= 100){
                status='高';
            }

            ramChart.setOption({
                series:[{
                    data: [{value: value, name: status}]
                }]
            });
        },
        //硬盘饼状图
        diskCharts: function () {
            var option = {
                color:['#c80002','#adadad'],
                legend: {
                    orient: 'vertical',
                    right: 0,
                    top: 20,
                    data: ['已使用空间','未使用空间']
                },
                series : [
                    {
                        type: 'pie',
                        radius : '80%',
                        center: ['40%', '50%'],
                        hoverAnimation:false,
                        label:{
                            normal: {
                                position: 'inside',
                                formatter: '{c}%'
                            },
                        },
                        labelLine:{
                            normal: {
                                show: false
                            }
                        },
                    }
                ]
            };

            diskChart = echarts.init(document.getElementById('diskCharts'));
            diskChart.setOption(option);
        },
        diskChartDatas:function(){
            var value = serverDetailDatas.diskPercent;

            diskChart.setOption({
                series:[{
                    data:[
                        {
                            value:value,
                            name:'已使用空间'
                        },
                        {
                            value:100-value,
                            name:'未使用空间'
                        },
                    ]
                }]
            })
        },
        //服务器选择下拉
        serverList:function(){
            var slide=$('#showBox');
            slide.toggleClass('hidden');
        },
        changeSever:function(){
            inx=$(this).index();
            serverIndex=inx;
            ipAddress=$(this).data("value");
            $(this).addClass('active').siblings().removeClass('active');
            serverMonitorList.updateDetail();
        },
        serverBtnFuns:function(){
            var showItem = $('#showBox .show-item');
            var id = $(this).attr('id');

            if(serverList.length<0){
                return;
            }

            if(id == 'rightClickVehicle'){
                if(serverIndex==serverList.length-1){
                    layer.msg('已经是最后一台了');
                    return;
                }
                serverIndex++;
            }else if(id == 'leftClickVehicle'){
                if(serverIndex==0){
                    layer.msg('已经是第一台了');
                    return;
                }
                serverIndex--;
            }

            showItem.eq(serverIndex).addClass('active').siblings().removeClass('active');
            ipAddress=showItem.eq(serverIndex).data('value');
            serverMonitorList.updateDetail();
        },
        serverArrowFunc:function(){
            var icon = $(this).find('.fa');
            var info = $('#basicInfo');
            progressInx=0;
            if(info.is(':hidden')){
                info.slideDown();
                serverMonitorList.updateDetail();
                icon.removeClass('fa-chevron-up').addClass('fa-chevron-down');
            }else{
                info.slideUp();
                icon.removeClass('fa-chevron-down').addClass('fa-chevron-up');
                if(cpuTimer){
                    clearInterval(cpuTimer);
                    cpuTimer=null;
                }
                return;
            }
        },
        /**
         * 图表resize
         */
        chartResize:function () {
            cpuChart.resize();
            ramChart.resize();
            diskChart.resize();
            progressChart.resize();
        },
    }
    $(function () {
        //初始化页面
        serverMonitorList.init();
        $('#carName').hover(serverMonitorList.serverList);
        $('#dataTable tbody').on('click','.editBtn',serverMonitorList.editBtnFuns);
        $('#serverArrow').on('click',serverMonitorList.serverArrowFunc);
        $('#rightClickVehicle,#leftClickVehicle').on('click',serverMonitorList.serverBtnFuns);

        //图标自适应
        $(window).resize(function () {
            serverMonitorList.chartResize();
        })
    })
})(window, $)