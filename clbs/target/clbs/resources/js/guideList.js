//中环引导页
(function (window, $) {
    var mids = [];
    //map
    var flag = true,//工具图标按钮开关
        isCluster = undefined,//是否聚合开关
        isCarNameShow = true,//车牌号显示开关
        isWeatherShow = false,//天气显示开关
        isArea = $('#isarea').val();//权限 1：为区域用户

    var map,
        mouseTool,
        mapPlugin,
        trafficLayer,
        satellLayer,
        cluster,
        maxZoom = 16;

    //地图点标记map集合
    var markerAllData,
        markerUpdateData,
        markerAllList,
        markerViewingArea,
        markerOutside,
        carNameContentLUMap,
        carNameMarkerContentMap,
        carNameMarkerMap;

    //menu
    var menuList = $('#menuList'),//菜单列表dom
        menuIcon = $('#menuBtn'),//菜单按钮
        menuListW,
        menuFlag = false;

    //eCharts
    var operationCharts;
    var vehicleOperationDatas = [],
        n = 0;
    var OperationTimer = null;
    var guideOperation = [];
    var pageInitFlag = false;
    var totalPageData = [];
    var totalPage = 0;
    var pageSize = 10;
    var pageNumber = 1;

    //table
    var tableLen = 10;
    var tableTimer = null;

    var tableTimer1 = null;
    var hasSubscribeVicArr = [];
    var heatmap=null;
    var dateType = 1;

    guideOperation = {
        /**
         * 初始化
         */
        init: function () {
            //地图初始化
            map = new AMap.Map("aMapContainer", {
                resizeEnable: true,
                center: [116.397428, 39.90923],
                zoom: 5,
            });

            guideOperation.getPermission();
            guideOperation.greeting();
            guideOperation.tableSwiper(0);

            //接口数据渲染
            guideOperation.getvehicleOperation();//运营类型
            // guideOperation.getRegionAndEventAlarm();//区域报警
            guideOperation.getServerce();//服务器性能
            // guideOperation.getCompanyTbody();//企业排行榜
            guideOperation.getDriverTbody(); //驾驶员排行榜
            guideOperation.getMonitorTbody();//监控对象排行榜
            guideOperation.getNowRisk();//实时风险报警数
            guideOperation.getOnlineInfo();//在线车辆
            guideOperation.getYestodayRisk();//昨日风险报警数
            setInterval(function () {
                guideOperation.getNowRisk();
                guideOperation.getOnlineInfo();
            }, 30000);
            OperationTimer = setInterval(guideOperation.getOperationChartData, 3000);

            //添加地图控件
            mapPlugin = map.plugin(['AMap.ToolBar', 'AMap.Scale', "AMap.Heatmap"], function () {
                var barOptions = {
                    offset: new AMap.Pixel(10, 50),
                    position: 'LB'
                };
                map.addControl(new AMap.ToolBar(barOptions));//工具条控件
                map.addControl(new AMap.Scale());//比例尺控件

                heatmap = new AMap.Heatmap(map,{
                    radius: 10, //给定半径
                    zIndex: 999,
                    blur:0.6,
                    // opacity:0.5,
                    'gradient':{
                        0.08:'#299817',
                        0.16:'#3ab126',
                        0.25:'#04cd0b',
                        0.33:'#00f106',
                        0.41:'#30ff00',
                        0.50:'#9fff00',
                        0.58:'#d9ff00',
                        0.66:'#ffdd00',
                        0.75:'#ff7400',
                        0.83:'#ff0c00',
                        0.91:'#d7000a',
                        1:'#a71414',
                    }
                });//热力图
            });

            //全局设置 地图鼠标工具插件
            mouseTool = new AMap.MouseTool(map);

            //全局设置 实时路况图层
            trafficLayer = new AMap.TileLayer.Traffic({zIndex: 10});
            trafficLayer.setMap(map);
            trafficLayer.hide();

            // 卫星地图
            satellLayer = new AMap.TileLayer.Satellite();
            satellLayer.setMap(map);
            satellLayer.hide();

            //map集合实例化
            guideOperation.createMap();

            //获取监控对象数据
            guideOperation.getMarkerData();//第一次获取所有
            //guideOperation.websocketRequest();

            //天气初始化
            mapWeather.getWeatherDatas();
        },
        /**table-swiper
         *权限页面判断
         */
        getPermission: function () {
            if (isArea) {
                $('.table-wrap').eq(0).show();//一期
            } else {
                $('.table-wrap').eq(1).show();//二期
            }

            guideOperation.setTableH();
        },
        /**
         * 图表legend分页效果实现
         * */
        setLegendPagination: function (operationLegend) {
            totalPageData = operationLegend ? operationLegend : totalPageData;
            var total = totalPageData.length;
            totalPage = Math.ceil(total / pageSize); //总页数
            if (totalPage < 2) {
                $("#legend_page").hide();
                return;
            }
            $("#legend_page").show();
            if (pageNumber <= totalPage && pageNumber > 0) {　//保证页数在有效值范围内
                var legendData = [];
                var skip = parseInt((pageNumber - 1)) * (pageSize);
                for (var i = skip; i < skip + pageSize; ++i) {
                    legendData[i - skip] = totalPageData[i];
                }
                $("#page_text").html(pageNumber + '/' + totalPage);　//分页图标中间的显示内容，如1/6,代表当前页和总的页数
                $('#nextIcon').removeClass('noClick');
                $('#preIcon').removeClass('noClick');
                operationCharts.setOption({
                    legend: {
                        data: legendData
                    }
                });
                if (pageNumber == 1) {
                    $('#preIcon').addClass('noClick');
                }
                if (pageNumber == totalPage) {
                    $('#nextIcon').addClass('noClick');
                }
            }
        },
        pageClickEvent: function (event) {
            var id = event.target.id;
            if ($(event.target).hasClass('noClick')) return;
            if (id == "preIcon") {
                pageNumber--;
                guideOperation.setLegendPagination();
            } else if (id == "nextIcon") {
                pageNumber++;
                guideOperation.setLegendPagination();
            }
        },
        /**
         * 饼状图
         */
        operationChart: function (operationLegend, operationChartDatas) {
            var option = {
                // color:['#79d5ff','#807ae3','#9ebffa','#56a1d5','#fadb71'],
                tooltip: {
                    trigger: 'item',
                    formatter: "{b}:{d}%"
                },
                legend: {
                    // type: 'scroll',
                    selectedMode: false,
                    orient: 'horizontal',
                    id: 'operationLegend',
                    left: 200,
                    top: 85,
                    width: 450,
                    data: operationLegend ? operationLegend : []
                },
                series: [
                    {
                        name: '来源',
                        type: 'pie',
                        radius: ['75%', '100%'],
                        center: ['15%', '50%'],
                        avoidLabelOverlap: false,
                        hoverAnimation: false,
                        label: {
                            normal: {
                                show: false,
                                position: 'center',
                                textStyle: {
                                    fontSize: 40,
                                    color: '#767676'
                                },
                                formatter: ['{c}', '{title|{b}}'].join('\n'),
                                rich: {
                                    title: {
                                        fontSize: 16,
                                        color: '#767676',
                                        height: 30,
                                        lineHeight: 30,
                                    }
                                }
                            }
                        },
                    }
                ]
            };

            operationCharts = echarts.init(document.getElementById('operationChart'));
            operationCharts.setOption(option);

            if (operationLegend && !pageInitFlag) {
                pageInitFlag = true;
                guideOperation.setLegendPagination(operationLegend);
            }
        },
        getOperationChartData: function () {
            var operationLegend = [],
                operationChartDatas = [],
                show = false;
            if (n >= vehicleOperationDatas.length) {
                n = 0;
            }

            vehicleOperationDatas.forEach(function (item, index) {
                //组装饼状图数据
                operationLegend.push(item.name);
                if (index == n) {
                    show = true;
                } else {
                    show = false;
                }

                var obj = {
                    value: item.number,
                    name: item.name,
                    label: {
                        normal: {
                            show: show,
                        }
                    }
                };
                operationChartDatas.push(obj);
            });

            operationCharts.setOption({
                /* legend: {
                     data: operationLegend
                 },*/
                series: [{
                    data: operationChartDatas,
                }]
            });
            n++;
            if (operationLegend && !pageInitFlag) {
                pageInitFlag = true;
                operationCharts.setOption({
                     legend: {
                         data: operationLegend
                     },
                });
                guideOperation.setLegendPagination(operationLegend);
            }
        },
        /**
         * table轮播
         */
        tableSwiper: function (i) {
            if (tableTimer) {
                clearInterval(tableTimer);
                tableTimer = null;
            }

            var tabs = $('.panel-tab .btn'),
                tables = $('#table-swiper .base-table');
            var tabLen = tabs.length;

            tables.eq(i).addClass('active').siblings().removeClass('active');
            tabs.eq(i).addClass('active').siblings().removeClass('active');

            tableTimer = setInterval(function () {
                tables.eq(i).addClass('active').siblings().removeClass('active');
                tabs.eq(i).addClass('active').siblings().removeClass('active');
                i++;
                if (i >= tabLen) {
                    i = 0;
                }
            }, 5000);
        },
        /**
         * 问候语
         */
        greeting: function () {
            var now = new Date(),
                hour = now.getHours();
            var dom = $('#greetings');
            var txt = '早上好';

            if (hour >= 11 && hour < 13) {
                txt = '中午好'
            } else if (hour >= 13 && hour < 18) {
                txt = '下午好'
            } else if (hour >= 18) {
                txt = '晚上好'
            }

            dom.text(txt);
        },
        /**
         * 监控对象推送
         */
        websocketRequest: function (mids) {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    guideOperation.markerSocket(mids);//上下线实时更新
                    setTimeout(function () {
                        guideOperation.markerStateSocket(mids);//状态改变实时更新
                    }, 1000)
                } else {
                    guideOperation.websocketRequest(mids);
                }
            }, 1500);
        },
        unsubscribeNew: function (ancelStrS) {
            setTimeout(function () {
                if (webSocket.conFlag) {
                    guideOperation.markerSocket(ancelStrS);//上下线实时更新
                    setTimeout(function () {
                        guideOperation.markerStateSocket(ancelStrS);//状态改变实时更新
                    }, 1000)
                } else {
                    guideOperation.unsubscribeNew(ancelStrS);
                }
            }, 1500);
        },
        /**
         * 设置区域风险报警自适应高度
         */
        // setTableH: function () {
        //     var mainH,
        //         offsetH1,
        //         offsetH2;
        //
        //     mainH = $('#guideData').eq(0).height();
        //     if (isArea) {
        //         offsetH2 = $('.table-swiper').eq(0).position().top;
        //         offsetH1 = $('.table-wrap').eq(0).position().top;
        //     } else {
        //         offsetH2 = $('.table-swiper').eq(1).position().top;
        //         offsetH1 = $('.table-wrap').eq(1).position().top;
        //     }
        //
        //     var offsetH = offsetH1 + offsetH2,
        //         tableH = mainH - offsetH;
        //
        //     $('.base-table').height(tableH);
        // },
        setTableH: function () {
            var windowH = $(window).height();
            var offsetH = $('#table-swiper').offset().top

            var tableH = windowH - offsetH;

            $('.base-table').height(tableH);
        },
        /**
         * 实时风险报警数
         */
        getNowRisk: function () {
            var url = '';
            if (isArea) {//区域
                url = '/clbs/adas/lb/show/getNowRisk'
            } else {
                url = '/clbs/adas/lbOrg/show/getNowRisk'
            }
            json_ajax("POST", url, "json", true, {}, function (data) {
                var html = '';
                var arr = data.toString().split('');
                arr.forEach(function (item) {
                    html += '<span class="num">' + item + '</span>';
                });
                html += '<span>件</span>';

                $('#nowRiskNum').html(html);
            });
        },
        /**
         * 在线车辆
         */
        getOnlineInfo: function () {
            var url = '';
            if (isArea) {//区域
                url = '/clbs/adas/lb/show/getVehicleOnlie'
            } else {
                url = '/clbs/adas/lbOrg/show/getVehicleOnlie'
            }
            json_ajax("POST", url, "json", true, {}, function (data) {
                var html = '';
                var arr = data.toString().split('');
                arr.forEach(function (item) {
                    html += '<span class="num">' + item + '</span>';
                });
                html += '<span>辆</span>';
                $('#onlineInfo').html(html);
                $('#onlineNum').text(data);
                // var networkOutflow = data.networkOutflow > 1000 ? '1000+' : data.networkOutflow;
                // var networkInflow = data.networkInflow > 1000 ? '1000+' : data.networkInflow;
                // $('#networkOutflow').text(networkOutflow);
                // $('#networkStatus').text(networkInflow);
            });
        },
        /**
         * 昨日风险报警数
         */
        getYestodayRisk: function () {
            var url = '';
            if (isArea) {//区域
                url = '/clbs/adas/lb/show/getYesterdayRisk'
            } else {
                url = '/clbs/adas/lbOrg/show/getYesterdayRisk'
            }
            json_ajax("POST", url, "json", true, {}, function (data) {
                $('#yestodayRisk').text(data);
            });
        },
        /**
         *服务器性能
         */
        getServerce: function () {
            var serveceStatus = $('.cpuStatus');
            json_ajax("POST", "/clbs/adas/lb/monitor/status", "json", true, {}, function (data) {
                if (data.success == true) {
                    data = data.obj;
                    guideOperation.getCpuStatus(data.cpuStatus, serveceStatus.eq(0));
                    guideOperation.getCpuStatus(data.memStatus, serveceStatus.eq(1));
                    guideOperation.getCpuStatus(data.diskStatus, serveceStatus.eq(2));

                    var networkOutflow = 0 + 'KB/s';
                    if(data.networkOutflow){
                        networkOutflow = data.networkOutflow >= 1024 ? (data.networkOutflow / 1024).toFixed(2)+'MB/s' : data.networkOutflow+'KB/s';
                    }

                    var networkInflow = 0 + 'KB/s';
                    if(data.networkInflow){
                        networkInflow = data.networkInflow >= 1024 ? (data.networkInflow / 1024).toFixed(2)+'MB/s' : data.networkInflow+'KB/s';
                    }
                    $('#networkOutflow').text(networkOutflow);
                    $('#networkStatus').text(networkInflow);
                }
            });
        },
        /**
         * 排行榜,服务器性能跳转判断
         */
        getUrlJump: function () {
            var self = $(this),
                url = '';
            var name = self.data('value');
            if (name == '服务器监控报表') {
                url = '/clbs/r/reportManagement/serverMonitor/list';
            } else if (name == '报警排行统计报表') {
                url = '/clbs/r/reportManagement/adasAlarmRank/list';
            }

            var params = {
                moduleName: name,
            };
            json_ajax("POST", "/clbs/adas/lb/guide/isPermissions", "json", true, params, function (data) {
                if (data) {
                    window.location.href = url;
                }
                /*else {
                                   layer.msg("很抱歉,此账号暂时未开通此功能权限,请与管理员联系!");
                                   return false;
                               }*/
            });
        },
        /**
         * 渲染状态显示
         * @param status ： 状态码
         * @param statusDom ： 状态显示颜色dom
         * @param txtDom ： 文字显示dom
         */
        getCpuStatus: function (status, statusDom) {
            var html = '',
                statusTxt = '',
                statusClass = '',
                alt = '';

            if (status == '0') {
                statusClass = 'normal';
                statusTxt = '正常';
            } else if (status == '1') {
                statusClass = 'abnormal1';
                statusTxt = '异常';
                alt = '资源使用率偏高,请注意！';
            } else if (status == '2') {
                statusClass = 'abnormal2';
                statusTxt = '异常';
                alt = '资源使用率严重不足,警告！';
            }

            html += '<span>' + statusTxt + '</span>' +
                '<i class="fa fa-question-circle" alt="' + alt + '"></i>';
            statusDom.html(html);
            statusDom.addClass(statusClass);
            $(".fa-question-circle").mouseover(guideOperation.toolTipsFunc);
        },
        /**
         * wdq注释说明：区域用户暂时屏蔽
         * 区域风险报警(区域用户)
         */
        // getRegionAndEventAlarm: function() {
        //     var params = {
        //         provinceCode:'230000',
        //         cityCode:'0',
        //         countyCode:'0'
        //     };
        //     json_ajax("POST", "/clbs/adas/lb/show/getRegionAndEventAlarm", "json", true, params, function (data) {
        //         if(data.success == true) {
        //             data = data.obj.region;
        //
        //             var html = '',
        //                 riskSortList = [];
        //
        //             riskSortList = guideOperation.sortArray(data,'count');
        //             riskSortList.forEach(function(item){
        //                 var percent = guideOperation.toDecimal2NoZero(item.percent * 100) + '%';//百分比转换
        //
        //                 html += '<tr>' +
        //                     '<td>NO.'+ item.sort +'</td>' +
        //                     '<td>'+ item.cityName +'</td>' +
        //                     '<td>'+ item.count +'</td>' +
        //                     '<td>'+ percent +'</td>' +
        //                     '</tr>';
        //             })
        //
        //             $('#riskTbody').html(html);
        //         }
        //     });
        // },
        /**
         * 监控对象排行榜(企业用户)
         */
        getMonitorTbody: function () {
            json_ajax("POST", "/clbs/adas/lb/guide/getRankOfVehicle", "json", true, {}, function (data) {
                if (data.success == true) {
                    data = data.obj;

                    var html = '',
                        riskSortList = [];

                    riskSortList = guideOperation.sortArray(data, 'total');

                    for (var i = 0; i < tableLen; i++) {
                        var item = riskSortList[i] || {};

                        if ('groupName' in item) {
                            var percent = guideOperation.toDecimal2NoZero(item.percentage * 100) + '%';//占比
                            html += '<tr>' +
                                '<td>NO.' + item.sort + '</td>' +
                                '<td class="brandName" alt="' + item.brand + '">' + item.brand + '</td>' +
                                '<td>' + item.total + '</td>' +
                                '<td>' + percent + '</td>';

                            if (item.ratio == 'up') {
                                html += '<td><i class="fa fa-arrow-up abnormal2"></i></td>';
                            } else if (item.ratio == 'down') {
                                html += '<td><i class="fa fa-arrow-down normal"></i></td>';
                            } else {
                                html += '<td><i class="fa fa-window-minimize"></i></td>';
                            }

                            html += '</tr>';
                        } else {
                            html += '<tr><td colspan="5" style="color:transparent;">无数据</td></tr>';
                        }
                    }

                    $('#monitorTbody').html(html);
                    $(".brandName").mouseover(guideOperation.toolTipsFunc);
                }
            });
        },
        /**
         * 企业排行榜(企业用户)
         */
        getCompanyTbody: function () {
            json_ajax("POST", "/clbs/adas/lb/guide/getRankOfGroup", "json", true, {}, function (data) {
                if (data.success == true) {
                    data = data.obj;

                    var html = '',
                        riskSortList = [];

                    riskSortList = guideOperation.sortArray(data, 'total');

                    for (var i = 0; i < tableLen; i++) {
                        var item = riskSortList[i] || {};

                        if ('groupName' in item) {
                            var percent = guideOperation.toDecimal2NoZero(item.percentage * 100) + '%';//占比
                            html += '<tr>' +
                                '<td>NO.' + item.sort + '</td>' +
                                '<td class="groupName" alt="' + item.groupName + '">' + item.groupName + '</td>' +
                                '<td>' + item.total + '</td>' +
                                '<td>' + percent + '</td>';

                            if (item.ratio == 'up') {
                                html += '<td><i class="fa fa-arrow-up abnormal2"></i></td>';
                            } else if (item.ratio == 'down') {
                                html += '<td><i class="fa fa-arrow-down normal"></i></td>';
                            } else {
                                html += '<td><i class="fa fa-window-minimize"></i></td>';
                            }

                            html += '</tr>';
                        } else {
                            html += '<tr><td colspan="5" style="color:transparent;">无数据</td></tr>';
                        }
                    }

                    $('#companyTbody').html(html);
                    $(".groupName").mouseover(guideOperation.toolTipsFunc);
                }
            });
        },
        /**
         * 驾驶员排行榜(企业用户)
         */
        getDriverTbody: function () {
            var url = '/clbs/adas/lb/guide/getRankOfDriver',
                paramData = {limitSize: 10},
                     html = '',
                driverList = [];
            json_ajax('post', url, 'json', true, paramData, function (data) {
               if(data.success){
                   data = data.obj;
                   driverList = guideOperation.sortArray(data, 'total');
                   console.log(driverList);
                   for(var i = 0; i < tableLen; i++){
                        var item = driverList[i] || {};
                        if('groupName' in item){
                            html += '<tr>' +
                                    '<td>NO.' + item.sort + '</td>' +
                                    '<td class="groupName" alt=" '+ item.driverName +' ">' + item.driverName + '</td>' +
                                    '<td>' + item.total + '</td>' +
                                    '<td>' + item.percentageString + '</td>';
                            if(item.ringRatio == 'up'){
                                html += '<td><i class="fa fa-arrow-up abnormal2"></i></td>'
                            }else if(item.ringRatio == 'down'){
                                html += '<td><i class="fa fa-arrow-down nowrap"></i></td>'
                            }else {
                                html += '<td><i class="fa fa-window-minimize"></i></td>'
                            }
                            html += '</tr>'

                        }else{
                            html += '<tr><td colspan="5" style="color:transparent;">无数据</td></tr>';
                        }

                   }
                   $('#companyTbody').html(html);
                   $('.groupName').mouseover(guideOperation.toolTipsFunc);
               }
            });
        },
        /**
         * 保留2位小数，如：2，还会保留2 不会补0
         * @param x
         * @returns {string}
         */
        toDecimal2NoZero: function (x) {
            var f = Math.round(x * 100) / 100;
            var s = f.toString();
            return s;
        },
        /**
         *区域风险报警排序
         * data : 需要排序的数组
         */
        sortArray: function (data, name) {
            var t = null,
                list = [];

            if (data.length != 0) {
                data.forEach(function (item, index) {
                    //只取数据前面10条
                    if (index < 10) {
                        if (t == null) {
                            t = item;
                            t.sort = index + 1;
                        } else {
                            var n = item;
                            if (n[name] == t[name]) {
                                n.sort = t.sort;
                            } else {
                                n.sort = index + 1;
                            }
                            t = n;
                        }

                        t.sort = parseInt(t.sort) < 10 ? '0' + parseInt(t.sort) : t.sort;//不满10的前面添加0
                        list.push(t);
                    }
                })
            }

            return list;
        },
        /**
         * 获取车辆运营类型
         */
        getvehicleOperation: function () {
            var params = {
                provinceCode: 230000,
                cityCode: 0,
                countyCode: 0
            }
            var url = '';
            if (isArea) {//区域
                url = '/clbs/adas/lb/show/getOperationCategory';
            } else {
                url = '/clbs/adas/lbOrg/show/getOperationCategory';
            }
            json_ajax("POST", url, "json", true, params, function (data) {
                if (data.success == true) {
                    vehicleOperationDatas = data.obj;
                    guideOperation.operationChart();
                    guideOperation.getOperationChartData();//运营类型
                }
            });
        },
        /**
         * 车辆运营类型
         */
        getvehicleOperationAlarmRatio: function () {
            var html = '',
                linkUrl = '',
                sum = 0;
            if (isArea) {
                linkUrl = "/clbs/adas/lb/show/list";
            } else {
                linkUrl = "/clbs/adas/lbOrg/show/list";
            }

            vehicleOperationDatas.forEach(function (item) {
                var percent = parseFloat(item.ratio);
                sum += parseInt(item.number);

                if (parseInt(item.number) != 0) {
                    html += '<div class="chart">' +
                        '   <a href="' + linkUrl + '" class="circle-progress" data-percent=' + percent + '>' +
                        '       <div class="text">' + item.name + '</div>' +
                        '   </a>' +
                        '   <div class="tips">' +
                        '       <span class="green">' + item.number + '&ensp;</span>' +
                        '       <span class="red">' + item.ratio + '</span>' +
                        '   </div>' +
                        '</div>'
                }
            });

            $('#cateTotal').text(sum);
            $('#chatItem').html(html);
        },
        /**
         * menu展开
         */
        menuShow: function () {
            guideOperation.getvehicleOperationAlarmRatio();//车辆运营类型占比获取数据
            // menu 圆形进度条
            $('.circle-progress').easyPieChart({
                barColor: '#51bbeb',
                trackColor: 'rgba(208,222,240,.5)',
                size: 70,
                scaleColor: false,
                lineCap: 'butt',
                lineWidth: 10,
                animate: 1000
            });

            menuListW = menuList.width();

            if (menuFlag) {
                menuList.animate({'marginRight': -menuListW + 'px'}, 500);
                menuFlag = false;
            } else {
                menuList.css({'marginRight': -menuListW + 'px'});//menu 初始化
                menuList.animate({'marginRight': 0}, 500);
                menuFlag = true;
            }
        },

        /*------------------------------------------点聚合-------------------------------------------------*/
        /**
         * map集合实例化
         */
        createMap: function () {
            markerAllData = new guideOperation.mapVehicle();
            markerUpdateData = new guideOperation.mapVehicle();
            // markerUpdateData = [];
            markerAllList = new guideOperation.mapVehicle();
            markerViewingArea = new guideOperation.mapVehicle();
            markerOutside = new guideOperation.mapVehicle();
            carNameMarkerContentMap = new guideOperation.mapVehicle();
            carNameMarkerMap = new guideOperation.mapVehicle();
            carNameContentLUMap = new guideOperation.mapVehicle();

        },
        /**
         * 封装map集合
         */
        mapVehicle: function () {
            this.elements = {};
            //获取MAP元素个数
            this.size = function () {
                return Object.keys(this.elements).length;
            };
            //判断MAP是否为空
            this.isEmpty = function () {
                return (Object.keys(this.elements).length < 1);
            };
            //删除MAP所有元素
            this.clear = function () {
                this.elements = {};
            };
            //向MAP中增加元素（key, value)
            this.put = function (_key, _value) {
                this.elements[_key] = _value;
            };
            //删除指定KEY的元素，成功返回True，失败返回False
            this.remove = function (_key) {
                delete this.elements[_key];
            };
            //获取指定KEY的元素值VALUE，失败返回NULL
            this.get = function (_key) {
                return this.elements[_key];
            };
            //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
            this.element = function (_index) {
                var keys = Object.keys(this.elements);
                var key = keys[_index];
                return this.elements[key];
            };
            //判断MAP中是否含有指定KEY的元素
            this.containsKey = function (_key) {
                if (this.elements[_key]) {
                    return true;
                } else {
                    return false;
                }
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
                var keys = Object.keys(this.elements);
                for (var i = 0, len = keys.length; i < len; i++) {
                    arr.push(this.elements[keys[i]]);
                }
                return arr;
            };
            //获取MAP中所有KEY的数组（ARRAY）
            this.keys = function () {
                return Object.keys(this.elements);
            };
        },
        /**
         *第一次接受所有监控对象数据
         */
        getMarkerData: function () {
            json_ajax("POST", "/clbs/adas/lb/guide/getVehiclePositional", "json", true, {}, function (data) {
                if (data.success) {
                    data = data.obj;

                    //第一次存储所有的监控对象
                    for (var i = 0; i < data.length; i++) {
                        var item = data[i];

                        var id = item.vehicleId;
                        var info = guideOperation.setData(item);
                        markerAllData.put(id, info);
                        markerUpdateData.put(id, info);
                    }

                    if (markerUpdateData.size() > 0) {
                        guideOperation.creatCluster();
                    }
                }
            });
        },
        /**
         * 组装数据
         * @param data
         * @returns {*[]}
         */
        setData: function (data) {
            var id = data.vehicleId,//id
                markerLngLat = [data.longitude, data.latitude],//经纬度
                carState = data.status,//状态
                carNum = data.brand,//车牌号
                icon = data.vehicleIcon,//图标
                angles = data.direction;//角度

            //组装监控对象需要存储的信息
            var updateInfo = [
                id, // ID
                markerLngLat, // 经度纬度
                carState,//车状态
                carNum,//车牌号
                icon,//图标
                angles//角度
            ];

            return updateInfo;
        },
        /**
         * 获取实时监控对象(位置变化)
         */
        markerSocket: function (mids) {
            // var param = [];
            // var vehicleIds = eval ("(" + $("#vehicleIds").val()+ ")");
            // $(vehicleIds).each(function(i,id){
            //     param.push({'vehicleID':id});
            // });

            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": mids
            };

            webSocket.subscribe(headers, "/user/topic/location", function (data) {
                data = JSON.parse(data.body);
                var updateInfo = guideOperation.setData(data);
                var id = updateInfo[0],
                    markerLngLat = updateInfo[1];

                if (markerAllData.containsKey(id)) {
                    var info = markerAllData.get(id);
                    if (markerLngLat.toString() != info[1].toString()) {//经纬度改变
                        markerUpdateData.put(id, updateInfo);
                    }
                }
                else {
                    markerUpdateData.put(id, updateInfo);
                }
                markerAllData.put(id, updateInfo);

                //非聚合
                var zoom = map.getZoom();
                if (zoom >= maxZoom) {
                    guideOperation.saveMarkerData(updateInfo);
                } else {
                    //聚合状态判断是否有位置更新数据
                    if (markerUpdateData.size() > 0) {
                        guideOperation.creatCluster();
                    }
                }
            }, "/app/location/subscribe", requestStrS);
        },
        /**
         * 存储非聚合状态下监控对象集合
         * @param updateInfo : 监控对象数据
         */
        saveMarkerData: function (updateInfo) {
            var id = updateInfo[0],
                markerLngLat = updateInfo[1],
                carState = updateInfo[2],
                carNum = updateInfo[3],
                icon = updateInfo[4];

            //判断是否为第一条数据进入
            if (markerViewingArea.size() == 0 && markerAllData.size() == 1) {
                guideOperation.createMarker(updateInfo);
            }
            else {
                //判断是否在可视区域内
                if (pathsTwo.contains(markerLngLat)) {
                    //判断id是否存在
                    if (markerViewingArea.containsKey(id)) {
                        //存在则更新marker数据
                        var info = markerViewingArea.get(id);
                        var marker = info[0];
                        marker.mid = id;
                        marker.carState = carState;
                        marker.carNum = carNum;
                        marker.icon = icon;

                        marker.setPosition(markerLngLat);
                        guideOperation.carNameEvade(id, carNum, marker.getPosition(), null, '0', icon, false, carState);
                    }
                    else {
                        //不存在则创建
                        guideOperation.createMarker(updateInfo);
                    }
                }
                else {
                    //存储可视区域外的监控对象
                    guideOperation.saveMarkerOutsideInfo(updateInfo);
                }
            }
        },
        /**
         * 获取实时监控对象(状态改变)
         */
        markerStateSocket: function (mids) {
            // var param = [];
            // var vehicleIds = eval ("(" + $("#vehicleIds").val()+ ")");
            // $(vehicleIds).each(function(i,id){
            //     param.push({'vehicleID':id});
            // });

            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": mids
            };

            webSocket.subscribe(headers, '/user/topic/cachestatus', function (data) {
                data = JSON.parse(data.body).data;
                guideOperation.UpdateMarkerState(data);
            }, "/app/vehicle/subVehicleStatusNew", requestStrS);
        },
        /**
         * 监控对象状态更新
         */
        UpdateMarkerState: function (data) {
            var zoom = map.getZoom();

            data.forEach(function (item) {
                var id = item.vehicleId;
                //所有监控对象
                if (markerAllData.containsKey(id)) {
                    var info = markerAllData.get(id);
                    info[2] = item.vehicleStatus;

                    //可视区域内
                    if (markerViewingArea.containsKey(id) && zoom >= maxZoom) {
                        var info2 = markerViewingArea.get(id);
                        info2[2] = item.vehicleStatus;

                        var carNum = info2[3],
                            icon = info2[4],
                            carState = item.vehicleStatus;

                        var marker = info2[0];
                        marker.carState = item.vehicleStatus;

                        guideOperation.carNameEvade(id, carNum, marker.getPosition(), null, '0', icon, false, carState);
                    }
                }
            })
        },
        /**
         * 监控对象在地图层级改变或拖拽后状态更新
         * 判断地图层级:
         * (1)大于等于maxZoom：重新计算地图上哪些监控对象在可视区域内||区域外
         * (2)小于maxZoom：进行聚合
         */
        markerStateListening: function () {
            // 根据地图层级变化相应改变paths
            guideOperation.pathsChangeFun();
            guideOperation.LimitedSizeTwo();

            var mapZoom = map.getZoom();
            mapWeather.showWeather(map, mapZoom, isWeatherShow);//天气
            // guideOperation.changeHeatBlur(mapZoom);//热力图
            // console.log('地图层级', mapZoom);

            if (mapZoom >= maxZoom) {// 刚进入非聚合状态
                if (isCluster == undefined || isCluster) {
                    if (cluster != undefined) {
                        cluster.clearMarkers();
                    }
                    isCluster = false;
                }
                guideOperation.clusterToCreateMarker();
                var values = markerViewingArea.values(), mids = [];
                for (var i = 0, len = values.length; i < len; i++) {
                    var marker = values[i][0];
                    var mid = marker.mid;

                    if (hasSubscribeVicArr.indexOf(mid) == -1) {
                        hasSubscribeVicArr.push(mid)
                        mids.push(mid);
                    }
                }
                setTimeout(function () {
                    if (mids.length) {
                        if (webSocket.conFlag) {
                            guideOperation.markerSocket(mids);//上下线实时更新
                            setTimeout(function () {
                                guideOperation.markerStateSocket(mids);//状态改变实时更新
                            }, 1000)
                        } else {
                            guideOperation.websocketRequest(mids);
                        }
                    }
                }, 1500);
            }
            else {// 刚进入聚合状态
                if (!isCluster && isCluster != undefined && markerAllList.size() > 0) {
                    isCluster = true;
                    guideOperation.clearMapForMarker();
                    if (markerUpdateData.size() > 0) {
                        guideOperation.creatCluster();
                    } else {
                        guideOperation.createMarkerClusterer();
                    }
                    var mids = [];
                    for (var i = 0; i < hasSubscribeVicArr.length; i++) {
                        mids.push({'vehicleID': hasSubscribeVicArr[i]})
                    }

                    if (mids.length) {
                        var cancelStrS = {
                            "desc": {
                                "MsgId": 40964,
                                "UserName": $("#userName").text()
                            },
                            "data": mids
                        };
                        webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatusNew", cancelStrS);

                        setTimeout(function () {
                            webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", cancelStrS);
                        }, 1000)

                    }

                    hasSubscribeVicArr = [];
                }
            }
        },
        /**
         * 存储聚合点集合
         */
        creatCluster: function () {
            var data = markerUpdateData.values();

            for (var i = 0; i < data.length; i++) {
                var info = data[i];
                var id = info[0],//id
                    markerLngLat = info[1];//经纬度

                if (markerAllList.containsKey(id)) {
                    var exit = markerAllList.get(id);
                    if (exit.markerLngLat != markerLngLat) {
                        exit.setPosition(markerLngLat);
                    }
                    continue;
                }

                var marker = new AMap.Marker({
                    position: markerLngLat,
                    icon: '/clbs/resources/img/1.png',//标记点自定义图标
                    offset: new AMap.Pixel(-26, -13), //相对于基点的位置
                    autoRotation: true
                });
                marker.mid = id;
                marker.markerLngLat = markerLngLat;
                markerAllList.put(id, marker);
            }
            //创建聚合点
            guideOperation.createMarkerClusterer();
            markerUpdateData.clear();//清除
        },
        createMarkerClusterer: function () {
            //清除之前创建的聚合点
            if (cluster != undefined) {
                cluster.clearMarkers();
                cluster.off('click', guideOperation.clusterClickFun);
            }

            cluster = new AMap.MarkerClusterer(map, markerAllList.values(), {zoomOnClick: false});
            cluster.on('click', guideOperation.clusterClickFun);//聚合点点击
        },
        /**
         * 创建监控对象图标
         * @param info : 监控对象存储的数据信息
         */
        createMarker: function (info) {
            var id = info[0],//id
                markerLngLat = info[1], // 经纬度
                carState = info[2], // 状态
                carNum = info[3], // 状态
                icon = info[4],//图标
                angles = info[5]; // 角度

            //创建监控对象图标
            var marker = guideOperation.carNameEvade(
                id,
                carNum,
                markerLngLat,
                true,
                '0',
                icon,
                false,
                carState
            );

            //设置角度
            var angle = Number(angles) + 270;
            marker.setAngle(angle);

            //监控对象图标添加字段
            marker.mid = id;//id
            marker.carNum = carNum; // 车牌号
            marker.carState = carState; // 状态
            marker.icon = icon; // 图标

            //组装可视区域监控对象数据集
            var markerList = [
                marker, // marker图标
                markerLngLat, // 经纬度
                carState,//状态
                carNum,//车牌号
                angles, // 角度
                '0', // 类型(人/车)
                icon, // 图标
            ];
            markerViewingArea.put(id, markerList);//存入markerViewingArea
        },
        /**
         * 聚合状态刚消失创建marker
         */
        clusterToCreateMarker: function () {
            var info = markerAllData.values();

            //循环监控点创建非聚合图标
            for (var i = 0, len = info.length; i < len; i++) {
                var id = info[i][0],
                    markerLngLat = info[i][1], // 经纬度
                    carState = info[i][2], // 状态
                    carNum = info[i][3]; // 车牌号

                if (pathsTwo.contains(markerLngLat)) {
                    if (markerViewingArea.containsKey(id)) {
                        guideOperation.carNameEvade(id, carNum, markerLngLat, false, '0', null, false, carState);
                    } else {
                        guideOperation.createMarker(info[i]);
                    }
                } else {
                    //存储可视区域外的监控对象
                    guideOperation.saveMarkerOutsideInfo(id, info[i]);
                }
            }
        },
        /**
         * 聚合点击事件
         * @param data
         */
        clusterClickFun: function (data) {
            var position = data.lnglat;
            var zoom = map.getZoom();

            if (zoom < 6) {
                map.setZoomAndCenter(6, position);
            }
            else if (zoom < 11) {
                map.setZoomAndCenter(11, position);
            }
            else if (zoom < 15) {
                map.setZoomAndCenter(15, position);
            }
            else if (zoom < maxZoom) {
                map.setZoomAndCenter(maxZoom, position);
            }
        },
        /**
         * 根据地图层级变化相应改变paths
         */
        pathsChangeFun: function () {
            var mapZoom = map.getZoom();

            if (mapZoom == 18) {
                guideOperation.LimitedSize(6);
            } else if (mapZoom == 17) {
                guideOperation.LimitedSize(5);
            } else if (mapZoom == 16) {
                guideOperation.LimitedSize(4);
            } else if (mapZoom == 15) {
                guideOperation.LimitedSize(3);
            } else if (mapZoom == 14) {
                guideOperation.LimitedSize(2);
            } else if (mapZoom <= 13 && mapZoom >= 6) {
                guideOperation.LimitedSize(1);
            }
            ;
        },
        /**
         * 扩大地图懒加载范围，提升地图拖动性能
         */
        LimitedSizeTwo: function () {
            var southwest = map.getBounds().getSouthWest();
            var northeast = map.getBounds().getNorthEast();
            var mcenter = map.getCenter();                  //获取中心坐标
            var pixel2 = map.lnglatTocontainer(mcenter);//根据坐标获得中心点像素
            var mcx = pixel2.getX();                    //获取中心坐标经度像素
            var mcy = pixel2.getY();                    //获取中心坐标纬度像素
            var southwestx = mcx + (mcx * 0.8);
            var southwesty = mcy * 0.2;
            var northeastx = mcx * 0.2;
            var northeasty = mcy + (mcy * 0.8);
            var ll = map.containTolnglat(new AMap.Pixel(southwestx, southwesty));
            var lll = map.containTolnglat(new AMap.Pixel(northeastx, northeasty));
            pathsTwo = new AMap.Bounds(
                lll,//东北角坐标
                ll //西南角坐标
            );
        },
        LimitedSize: function (size) {
            paths = null;
            var southwest = map.getBounds().getSouthWest();//获取西南角坐标
            var northeast = map.getBounds().getNorthEast();//获取东北角坐标
            var possa = southwest.lat;//纬度（小）
            var possn = southwest.lng;
            var posna = northeast.lat;
            var posnn = northeast.lng;
            var psa = possa - ((posna - possa) * size);
            var psn = possn - ((posnn - possn) * size);
            var pna = posna + ((posna - possa) * size);
            var pnn = posnn + ((posnn - possn) * size);
            paths = new AMap.Bounds(
                [psn, psa], //西南角坐标
                [pnn, pna]//东北角坐标
            );
        },
        /**
         * 清空地图上已创建监控对象图标
         */
        clearMapForMarker: function () {
            var values = markerViewingArea.values();
            for (var i = 0, len = values.length; i < len; i++) {
                var marker = values[i][0];
                map.remove([marker]);
            }

            markerViewingArea.clear();//可视区域内
            markerOutside.clear();//可视区域外
            var nameValues = carNameMarkerContentMap.values();
            map.remove(nameValues);//车牌
        },
        /**
         * 车牌号设置
         * @param id : id
         * @param name : 车牌号
         * @param lnglat : 经纬度
         * @param flag :
            * @param type : 判断监控对象类型
         * @param ico : 图标
         * @param showFlag : 是否显示车牌号
         * @param stateInfo : 监控对象状态
         * @returns {AMap.Marker}
         */
        carNameEvade: function (id, name, lnglat, flag, type, ico, showFlag, stateInfo) {
            //监控对象图片大小
            var value = lnglat;
            var picWidth;
            var picHeight;
            var icons;

            /*if (name.length > 8) {
                name = name.substring(0, 7) + '...';
            }
            var num = 0;
            for (var i = 0; i < name.length; i++) {//判断车牌号含有汉字数量
                if (name[i].match(/^[\u4E00-\u9FA5]{1,}$/)) {
                    num++;
                }
            }
            if (num > 3) {
                name = name.substring(0, 4) + '...';
            }*/

            //车
            if (type == "0") {
                if (ico == "null" || ico == undefined || ico == null) {
                    icons = "/clbs/resources/img/vehicle.png";
                } else {
                    icons = "/clbs/resources/img/vico/" + ico;
                }
                picWidth = 58 / 2;
                picHeight = 26 / 2;
            }
            //人
            else if (type == "1") {
                icons = "/clbs/resources/img/123.png";
                picWidth = 30 / 2;
                picHeight = 30 / 2;
            }

            if (isCarNameShow) {
                //显示对象姓名区域大小
                var nameAreaWidth = 90;
                var nameAreaHeight = 38;
                //车辆状态没判断
                var carState = guideOperation.stateCallBack(stateInfo);
                var id = id;
                var name = name;
                //判断是否第一个创建
                var markerAngle = 0; //图标旋转角度
                if (carNameMarkerMap.containsKey(id)) {
                    var thisCarMarker = carNameMarkerMap.get(id);
                    var ssmarker = new AMap.Marker({
                        icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png",
                        position: [116.41, 39.91]
                    });
                    markerAngle = thisCarMarker.getAngle();
                    var s = ssmarker.getAngle();
                    if (markerAngle > 360) {
                        var i = Math.floor(markerAngle / 360);
                        markerAngle = markerAngle - 360 * i;
                    }
                    ;
                }
                //将经纬度转为像素
                var pixel = map.lngLatToContainer(value);
                var pixelX = pixel.getX();
                var pixelY = pixel.getY();
                var pixelPX = [pixelX, pixelY];
                //得到车辆图标四个角的像素点(假设车图标永远正显示)58*26
                var defaultLU = [pixelX - picWidth, pixelY - picHeight];//左上
                var defaultRU = [pixelX + picWidth, pixelY - picHeight];//右上
                var defaultLD = [pixelX - picWidth, pixelY + picHeight];//左下
                var defaultRD = [pixelX + picWidth, pixelY + picHeight];//右下
                //计算后PX
                var pixelRD = guideOperation.countAnglePX(markerAngle, defaultRD, pixelPX, 1, picWidth, picHeight);
                var pixelRU = guideOperation.countAnglePX(markerAngle, defaultRU, pixelPX, 2, picWidth, picHeight);
                var pixelLU = guideOperation.countAnglePX(markerAngle, defaultLU, pixelPX, 3, picWidth, picHeight);
                var pixelLD = guideOperation.countAnglePX(markerAngle, defaultLD, pixelPX, 4, picWidth, picHeight);
                //四点像素转为经纬度
                var llLU = map.containTolnglat(new AMap.Pixel(pixelLU[0], pixelLU[1]));
                var llRU = map.containTolnglat(new AMap.Pixel(pixelRU[0], pixelRU[1]));
                var llLD = map.containTolnglat(new AMap.Pixel(pixelLD[0], pixelLD[1]));
                var llRD = map.containTolnglat(new AMap.Pixel(pixelRD[0], pixelRD[1]));
                //车牌显示位置左上角PX
                var nameRD_LU = [pixelRD[0], pixelRD[1]];
                var nameRU_LU = [pixelRU[0], pixelRU[1] - nameAreaHeight];
                var nameLU_LU = [pixelLU[0] - nameAreaWidth, pixelLU[1] - nameAreaHeight];
                var nameLD_LU = [pixelLD[0] - nameAreaWidth, pixelLD[1]];
                //分别将上面四点转为经纬度
                var llNameRD_LU = map.containTolnglat(new AMap.Pixel(nameRD_LU[0], nameRD_LU[1]));
                var llNameRU_LU = map.containTolnglat(new AMap.Pixel(nameRU_LU[0], nameRU_LU[1]));
                var llNameLU_LU = map.containTolnglat(new AMap.Pixel(nameLU_LU[0], nameLU_LU[1]));
                var llNameLD_LU = map.containTolnglat(new AMap.Pixel(nameLD_LU[0], nameLD_LU[1]));
                //判断车牌号该显示的区域
                var isOneArea = true;
                var isTwoArea = true;
                var isThreeArea = true;
                var isFourArea = true;
                //取出所有的左上角的经纬度并转为像素
                var contentArray = [];
                if (!carNameContentLUMap.isEmpty()) {
                    carNameContentLUMap.remove(id);
                    var carContent = carNameContentLUMap.values();
                    for (var i = 0; i < carContent.length; i++) {
                        var contentPixel = map.lngLatToContainer(carContent[i]);
                        contentArray.push([contentPixel.getX(), contentPixel.getY()]);
                    }
                    ;
                }
                ;
                if (contentArray.length != 0) {
                    for (var i = 0; i < contentArray.length; i++) {
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameRD_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameRD_LU[1] || (nameRD_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameRD_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isOneArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameRU_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameRU_LU[1] || (nameRU_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameRU_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isTwoArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameLU_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameLU_LU[1] || (nameLU_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameLU_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isThreeArea = false;
                        }
                        ;
                        if (!((contentArray[i][0] + nameAreaWidth) <= nameLD_LU[0] || (contentArray[i][1] + nameAreaHeight) <= nameLD_LU[1] || (nameLD_LU[0] + nameAreaWidth) <= contentArray[i][0] || (nameLD_LU[1] + nameAreaHeight) <= contentArray[i][1])) {
                            isFourArea = false;
                        }
                        ;
                    }
                    ;
                }
                ;
                var isConfirm = true;
                var mapPixel;
                var LUPX;
                var showLocation;
                if (isOneArea) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = new AMap.Pixel(0, 0);
                    isConfirm = false;
                    showLocation = "carNameShowRD";
                } else if (isConfirm && isTwoArea) {
                    mapPixel = llRU;
                    LUPX = llNameRU_LU;
                    offsetCarName = new AMap.Pixel(0, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowRU";
                } else if (isThreeArea && isConfirm) {
                    mapPixel = llLU;
                    LUPX = llNameLU_LU;
                    offsetCarName = new AMap.Pixel(-nameAreaWidth, -nameAreaHeight);
                    isConfirm = false;
                    showLocation = "carNameShowLU";
                } else if (isFourArea && isConfirm) {
                    mapPixel = llLD;
                    LUPX = llNameLD_LU;
                    offsetCarName = new AMap.Pixel(-nameAreaWidth, 0);
                    isConfirm = false;
                    showLocation = "carNameShowLD";
                }
                ;
                if (mapPixel == undefined) {
                    mapPixel = llRD;
                    LUPX = llNameRD_LU;
                    offsetCarName = new AMap.Pixel(0, 0);
                    showLocation = "carNameShowRD";
                }
                ;
            }
            ;

            if (flag != null) {
                if (flag) {//创建marker
                    //车辆
                    if (!showFlag) {
                        var markerLocation = new AMap.Marker({
                            position: value,
                            icon: icons,
                            offset: new AMap.Pixel(-picWidth, -picHeight), //相对于基点的位置
                            autoRotation: true,//自动调节图片角度
                            map: map,
                        });
                        markerLocation.name = name;
                        //车辆名
                        carNameMarkerMap.put(id, markerLocation);
                    }
                    ;
                    if (isCarNameShow) {
                        var carContent = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                        if (carNameMarkerContentMap.containsKey(id)) {
                            var nameValue = carNameMarkerContentMap.get(id);
                            map.remove([nameValue]);
                            carNameMarkerContentMap.remove(id);
                        }
                        ;
                        var markerContent = new AMap.Marker({
                            position: mapPixel,
                            content: carContent,
                            offset: offsetCarName,
                            autoRotation: true,//自动调节图片角度
                            map: map,
                            zIndex: 999

                        });
                        markerContent.setMap(map);
                        carNameMarkerContentMap.put(id, markerContent);
                        carNameContentLUMap.put(id, LUPX);
                        if (isConfirm) {
                            markerContent.hide();
                        } else {
                            markerContent.show();
                        }
                        ;
                    }
                    ;
                    if (!showFlag) {
                        return markerLocation;
                    }
                    ;
                } else {//改变位置
                    if (isCarNameShow) {
                        var carContentHtml = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                        if (carNameMarkerContentMap.containsKey(id)) {
                            var carContent = carNameMarkerContentMap.get(id);
                            if (isConfirm) {
                                carContent.hide();
                            } else {
                                carContent.show();

                                carContent.setContent(carContentHtml);
                                carContent.setPosition(mapPixel);
                                carContent.setOffset(offsetCarName);
                            }
                            carNameContentLUMap.put(id, LUPX);
                        }
                        ;
                    }
                    ;
                }
                ;
            } else {
                if (isCarNameShow) {
                    var carContentHtml = "<p class='" + showLocation + "'><i class='" + carState + "'></i>&ensp;<span class='monitorNameBox'>" + name + "</span></p>";
                    if (carNameMarkerContentMap.containsKey(id)) {
                        var thisMoveMarker = carNameMarkerContentMap.get(id);
                        if (isConfirm) {
                            thisMoveMarker.hide();
                        } else {
                            thisMoveMarker.show();
                            thisMoveMarker.setContent(carContentHtml);
                            thisMoveMarker.setPosition(mapPixel);
                            thisMoveMarker.setOffset(offsetCarName);
                        }
                        carNameContentLUMap.put(id, LUPX);
                    }
                    ;
                }
                ;
            }
            ;
        },
        /**
         * 计算车牌号四个定点的像素坐标
         * @param angle
         * @param pixel
         * @param centerPX
         * @param num
         * @param picWidth
         * @param picHeight
         * @returns {*[]|*}
         */
        countAnglePX: function (angle, pixel, centerPX, num, picWidth, picHeight) {
            var thisPX;
            var thisX;
            var thisY;
            if ((angle <= 45 && angle > 0) || (angle > 180 && angle <= 225) || (angle >= 135 && angle < 180) || (angle >= 315 && angle < 360)) {
                angle = 0;
            }
            ;
            if ((angle < 90 && angle > 45) || (angle < 270 && angle > 225) || (angle > 90 && angle < 135) || (angle > 270 && angle < 315)) {
                angle = 90;
            }
            ;
            if (angle == 90 || angle == 270) {
                if (num == 1) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
                if (num == 2) {
                    thisX = centerPX[0] + picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 3) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] - picWidth;
                }
                ;
                if (num == 4) {
                    thisX = centerPX[0] - picHeight;
                    thisY = centerPX[1] + picWidth;
                }
                ;
            }
            ;
            if (angle == 0 || angle == 180 || angle == 360) {
                thisX = pixel[0];
                thisY = pixel[1];
            }
            ;
            thisPX = [thisX, thisY];
            return thisPX;
        },
        /**
         * 监控对象状态返回
         * @param stateInfo
         * @returns {*}
         */
        stateCallBack: function (stateInfo) {
            var state;
            switch (stateInfo) {
                case 4:
                    state = 'carStateStop';
                    break;
                case 10://在线
                    state = 'carStateRun';
                    break;
                case 5:
                    state = 'carStateAlarm';
                    break;
                case 2:
                    state = 'carStateMiss';
                    break;
                case 3://离线
                    state = 'carStateOffLine';
                    break;
                case 9:
                    state = 'carStateOverSpeed';
                    break;
                case 11:
                    state = 'carStateheartbeat';
                    break;
            }
            ;
            return state;
        },
        /**
         * 保存可视区域外的监控对象信息
         * @param info
         * @param content
         */
        saveMarkerOutsideInfo: function (info) {
            var id = info[0],
                markerLngLat = info[1];

            // 删除可视区域内的信息
            if (markerViewingArea.containsKey(id)) {
                var marker = markerViewingArea.get(id)[0];
                map.remove([marker]);
                markerViewingArea.remove(id);
                var nameValues = carNameMarkerContentMap.get(id);
                map.remove(nameValues);//车牌
                carNameMarkerContentMap.remove(id);
            }

            markerOutside.put(id, markerLngLat);
        },
        /**
         * 地图setcenter完成后触发事件
         */
        mapMoveendFun: function () {
            guideOperation.pathsChangeFun();
            guideOperation.LimitedSizeTwo();
        },
        /**
         * 车牌号标注是否显示
         * @param flag : 车牌显示开关
         */
        carNameState: function (flag) {
            var carNameMarkerValue;
            if (!carNameMarkerContentMap.isEmpty()) {
                carNameMarkerValue = carNameMarkerContentMap.values();
            }
            ;
            if (flag) {
                //重新计算对象名称位置
                guideOperation.carNameShow();
            } else {
                if (carNameMarkerValue != undefined) {
                    for (var i = 0, len = carNameMarkerValue.length; i < len; i++) {
                        carNameMarkerValue[i].hide();
                    }
                    ;
                }
                ;
            }
            ;
        },
        /**
         * 重新计算对象名称位置
         */
        carNameShow: function () {
            var info = markerViewingArea.values();

            for (var i = 0, len = info.length; i < len; i++) {
                var marker = info[i][0];

                var id = marker.mid,//id
                    carState = marker.carState,//状态
                    carNum = marker.carNum,//车牌号
                    icon = marker.icon;//图标

                guideOperation.carNameEvade(id, carNum, marker.getPosition(), true, "0", icon, true, carState);
            }
        },

        /*-----------------------------------tool工具条操作----------------------------------*/
        /**
         * 工具图标按钮
         */
        toolClick: function () {
            var $toolOperateClick = $("#toolOperateClick");
            var toolW = $toolOperateClick.outerWidth() + 10;

            if (flag) {
                $toolOperateClick.animate({marginRight: "7px"});
                flag = false;
            } else {
                $toolOperateClick.animate({marginRight: "-" + toolW + "px"});

                //重置其它样式
                $("#disSetMenu,#mapDropSettingMenu").hide();
                $("#toolOperateClick i").removeClass('active');
                $("#toolOperateClick span").css('color', '#5c5e62');
                flag = true;
            }
        },
        /**
         * 工具操作
         */
        toolClickList: function () {
            var self = $(this);

            var id = self.attr('id'),
                i = self.children('i'),
                span = self.children('span.mapToolClick');

            var dropMenu1 = $("#disSetMenu"),//显示设置下拉
                dropMenu2 = $("#mapDropSettingMenu");//地图设置下拉

            //显示设置
            if (id == 'displayClick') {
                dropMenu1.slideToggle();

                if (!dropMenu2.is(":hidden")) {
                    dropMenu2.slideUp();
                }
                return;
            }
            //地图设置
            else if (id == "mapDropSetting") {
                dropMenu2.slideToggle();

                if (!dropMenu1.is(":hidden")) {
                    dropMenu1.slideUp();
                }
                return;
            }
            //其它设置
            else {
                //关闭下拉
                dropMenu1.slideUp();
                dropMenu2.slideUp();

                if (i.hasClass("active")) {
                    i.removeClass('active');
                    span.css('color', '#5c5e62');

                    //关闭当前鼠标操作
                    mouseTool.close(true);
                }
                else {
                    $("#toolOperateClick i").removeClass('active');
                    $("#toolOperateClick span.mapToolClick").css('color', '#5c5e62');
                    i.addClass('active');
                    span.css('color', '#6dcff6');

                    //关闭当前鼠标操作
                    mouseTool.close(true);

                    //工具操作地图调用
                    guideOperation.toolSetMap(id);
                }
                return;
            }
        },
        /**
         * 工具操作地图
         * @param id : 当前点击工具条菜单的id
         */
        toolSetMap: function (id) {
            //拉框放大
            if (id == "magnifyClick") {
                mouseTool.rectZoomIn();
            }
            //拉框放小
            else if (id == "shrinkClick") {
                mouseTool.rectZoomOut();
            }
            //距离量算
            else if (id == "countClick") {
                mouseTool.rule();
            }
        },
        /**
         * 地图设置
         * 实时路况
         * 卫星地图
         */
        mapSet: function () {
            var self = $(this);
            var id = self.attr('id'),
                checked = self.prop('checked'),
                label = self.siblings('label');

            //实时路况
            if (id == 'realTimeRC') {
                if (checked) {
                    trafficLayer.show();
                    label.addClass("preBlue");
                }
                else {
                    trafficLayer.hide();
                    label.removeClass("preBlue");
                }

                return;
            }
            //卫星地图
            else if (id == 'defaultMap') {
                if (checked) {
                    satellLayer.show();
                    label.addClass("preBlue");
                }
                else {
                    satellLayer.hide();
                    label.removeClass("preBlue");
                }
                return;
            }
        },
        /**
         *显示设置
         * 标识显示,天气显示
         */
        disSet: function () {
            var self = $(this);

            var id = self.attr('id'),
                checked = self.prop('checked'),
                label = self.siblings('label');
            var alarmDate = $('#alarmDate');

            if (id == "logoDisplay") {
                if (!checked) {
                    isCarNameShow = false;
                    label.removeClass("preBlue");
                } else {
                    isCarNameShow = true;
                    label.addClass("preBlue");
                }

                guideOperation.carNameState(isCarNameShow);
            }

            if (id == 'weather') {
                var mapZoom = map.getZoom();
                if (!checked) {
                    isWeatherShow = false;
                    label.removeClass("preBlue");
                } else {
                    isWeatherShow = true;
                    label.addClass("preBlue");
                }
                mapWeather.showWeather(map, mapZoom, isWeatherShow);
            }
            //区域风险
            if (id == 'areaAlarm') {
                if (checked) {
                    label.addClass("preBlue");
                    heatmap.show();
                    guideOperation.getHeatMap();
                    alarmDate.removeClass('hide');
                }
                else {
                    label.removeClass("preBlue");
                    heatmap.hide();
                    alarmDate.addClass('hide');
                }
                return;
            }

        },
        /**
         * 提示
         */
        toolTipsFunc: function () {
            var self = $(this);
            if (self.attr("alt")) {
                self.justToolsTip({
                    animation: "moveInTop",
                    width: "auto",
                    contents: self.attr("alt"),
                    gravity: 'top',
                    distance: 0
                });
            }
        },
        //热力图
        getHeatMap: function(){
            var param = {
                type:dateType
            };
            json_ajax("POST", '/clbs/adas/lb/guide/showHotMap', "json", true, param, function (data) {
                if(data.success) {
                    var datas = data.obj;
                    var points = [];
                    for (var i = 0; i < datas.length; i++) {
                        var item = datas[i];
                        var obj = {
                            "lng": item.lon,
                            "lat": item.lat,
                            "count": item.content,

                        };
                        points.push(obj);
                    }
                    heatmap.setDataSet({
                        data: points,
                        // max:100,
                    });
                }
            });
        },
        dateChange: function(){
            var value = $('input[name="alarmDate"]:checked').val();
            dateType = value;
            guideOperation.getHeatMap();
        },
        /*changeHeatBlur:function(zoom){
            // var blur = 0;
            if(zoom <=8){
                // blur = 0.6;
                heatmap.setOptions({
                    blur: 0.6
                });
            }else{
                // blur = 0;
                heatmap.setOptions({
                    blur: 0
                });
            }

            var obj = heatmap.getOptions();
            console.log('参数', obj);
            heatmap.setMap(map);
            heatmap.show();
        }*/
    };

    $(function () {
        //初始化
        guideOperation.init();
        //重置 区域风险报警自适应高度
        $(window).resize(guideOperation.setTableH);
        $('#guideData .panel-heading').click(function () {
            setTimeout(guideOperation.setTableH, 400);
        });

        //menu显示隐藏事件
        menuIcon.click(guideOperation.menuShow);

        // 当范围缩小、拖拽结束、地图移动、中心点变化时触发
        map.on('zoomend', guideOperation.markerStateListening);
        map.on('dragend', guideOperation.markerStateListening);
        map.on('moveend', guideOperation.mapMoveendFun);

        //工具图标按钮点击事件
        $('#toolClick').click(guideOperation.toolClick);
        $("#toolOperateClick>.fenceA").on("click", guideOperation.toolClickList);

        //地图设置(实时路况、卫星图）,显示设置(标识显示）
        $('#mapDropSettingMenu .monitoringSelect').change(guideOperation.mapSet);
        $('#disSetMenu .monitoringSelect').change(guideOperation.disSet);

        //隐藏蒙层
        $('#maduleHide').bind('click', function () {
            $('.aMapMadule-box').fadeOut();

            if (OperationTimer) {
                clearInterval(OperationTimer);
                OperationTimer = null;
            }
        });
        $('.panel-tab .btn').hover(function () {
            var inx = $(this).index();
            guideOperation.tableSwiper(inx);
        });
        $('#stretch3-body a,.panel-tab .btn').on('click', guideOperation.getUrlJump);
        $('#legend_page span').on('click', guideOperation.pageClickEvent);
        $('#alarmDate input[name="alarmDate"]').on('change', guideOperation.dateChange);
    })
})(window, $)