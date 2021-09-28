(function(window,$){
	var selectYear = $("#year");
	var selectMonth = $("#month");
	var curMonthQuery = $("#curMonthQuery"); // 本月查询按钮
	var preMonthQuery = $("#preMonthQuery"); // 上月查询按钮
	var curYearQuery = $("#curYearQuery"); // 本年查询按钮
	var preYearQuery = $("#preYearQuery"); // 上年查询按钮
	var groupId; // 组织id
	var vehicleId; // 车辆id
	var brand; // 车牌号
	var year_const; // 年份
	var month_const; // 月份
	comprehensiveEnergyReport = {
		// 初始化方法
		init : function () {
			// 初始化组织树
			var setting = {
				async : {
					url : "/clbs/m/functionconfig/fence/bindfence/vehicelTree",
					type : "post",
					enable : true,
					autoParam : [ "id" ],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    dataFilter: comprehensiveEnergyReport.ajaxDataFilter
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
					dblClickExpand : false
				},
				data : {
					simpleData : {
						enable : true
					}
				},
				callback : {
					beforeClick : comprehensiveEnergyReport.beforeClick,
					onClick : comprehensiveEnergyReport.onClick
				}
			};
			$.fn.zTree.init($("#ztreeDemo"), setting, null);
			// 默认显示当前时间
			comprehensiveEnergyReport.defaultCurDate();
		},
		beforeClick : function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            zTree.checkNode(treeNode, !treeNode.checked, null, true);
            return false;
		},
		onClick:function(e, treeId, treeNode){
            $("#brands").val("").bsSuggest("destroy");
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
                nodes = zTree.getCheckedNodes(true);
            var veh=[];
            var pname=[];
            var vehID=[];
            for(var i=0;i<nodes.length;i++){
                if(nodes[i].type=="vehicle"){
                    veh.push(nodes[i].name);
                    vehID.push(nodes[i].id);
                    pname.push(nodes[i].getParentNode().name)
                }
            }
            var pnames = comprehensiveEnergyReport.unique(pname);
            var vehName = comprehensiveEnergyReport.unique(veh);
            var vehIDs = comprehensiveEnergyReport.unique(vehID);
            $("#brands").empty();
            var deviceDataList = {value: []};
            for (var j = 0; j < vehName.length; j++){
                deviceDataList.value.push({
                    name: vehName[j],
                    id: vehIDs[j]
                });
            }
            $("#brands").bsSuggest({
                indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                data: deviceDataList,
                effectiveFields: ["name"]
            }).on('onDataRequestSuccess', function (e, result) {
            }).on("click",function(){
            }).on('onSetSelectValue', function (e, keyword, data) {
            }).on('onUnsetSelectValue', function () {
            });
            $("#brands").val(vehName[0]).attr("data-id",vehIDs[0]);
		},
		showMenu : function () {
            if ($("#zTreeContent").is(":hidden")) {
                var inpwidth = $("#groupName").width();
                var spwidth = $("#arrowDown").width();
				var allWidth = inpwidth + spwidth + 21;
                $("#zTreeContent").css("width",allWidth + "px");
            	$(window).resize(function() {
                    var inpwidth = $("#groupName").width();
                    var spwidth = $("#arrowDown").width();
    				var allWidth = inpwidth + spwidth + 21;
                    $("#zTreeContent").css("width",allWidth + "px");
            	})
                $("#zTreeContent").slideDown("fast");
            } else {
                $("#zTreeContent").is(":hidden");
            }
            $("body").bind("mousedown", comprehensiveEnergyReport.onBodyDown);
		}, 
		onBodyDown : function (event) {
			if (!(event.target.id == "menuBtn" || event.target.id == "zTreeContent" || $(event.target).parents("#zTreeContent").length > 0)) {
				comprehensiveEnergyReport.hideMenu();
			}
		},
		hideMenu : function () {
			$("#zTreeContent").fadeOut("fast");
			$("body").unbind("mousedown", comprehensiveEnergyReport.onBodyDown);
		},
        ajaxDataFilter:function (treeId, parentNode, responseData) {
            if (responseData) {
                var veh=[];
                var gourps=[];
                var vehID=[];
                for(var i=0;i<responseData.length;i++){
                    if(responseData[i].pId==""){
                        $("#zTreeCitySel").val(responseData[i].name);
                    }
                    responseData[i].checked=true
                    if(responseData[i].type=="group"){
                        gourps.push(responseData[i].name)
                    }
                    var gName = comprehensiveEnergyReport.unique(gourps)
                    $("#zTreeCitySel").val(gName[0]);
                    if(responseData[i].type=="vehicle"){
                        veh.push(responseData[i].name)
                        vehID.push(responseData[i].id);
                    }
                }
                var vehName = comprehensiveEnergyReport.unique(veh);
                var vehIDs = comprehensiveEnergyReport.unique(vehID);
                $("#brands").empty();
                var deviceDataList = {value: []};
                for (var j = 0; j < vehName.length; j++){
                    deviceDataList.value.push({
                        name: vehName[j],
                        id: vehIDs[j]
                    });
                };
                $("#brands").bsSuggest({
                    indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
                    indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
                    data: deviceDataList,
                    effectiveFields: ["name"]
                }).on('onDataRequestSuccess', function (e, result) {
                }).on("click",function(){
                }).on('onSetSelectValue', function (e, keyword, data) {
                }).on('onUnsetSelectValue', function () {
                });
                $("#brands").val(vehName[0]).attr('data-id',vehIDs[0]);
            }
            return responseData;
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
		// 显示错误信息
		showErrorMsg : function (msg, inputId) {
			if ($("#error_label").is(":hidden")) {
				$("#error_label").text(msg);
				$("#error_label").insertAfter($("#" + inputId));
		        $("#error_label").show();
			} else {
				$("#error_label").is(":hidden");
			} 
		},
		// 隐藏错误信息
		hideErrorMsg : function () {
			$("#error_label").hide();
		},
		validates : function () {
			return $("#queryForm").validate({
				rules : {
					groupName : {
						required : true
					},
                    brands : {
					  required:true
                    },
					endDate : {
						compareDate : "#startDate"
					}
				},
				messages : {
					groupName : {
						required : "不能为空"
					},
                    brands : {
					  required:"车牌号不能为空"
                    },
					endDate : {
						compareDate : "结束时间必须大于开始时间!"
					}
				}
			}).form();
		},
		// 默认显示当月时间
		defaultCurDate : function () {
			var date = new Date();
			var year = date.getFullYear();
			var month = (date.getMonth() + 1) > 9 ? (date.getMonth() + 1) : "0"
					+ (date.getMonth() + 1);
			year_const = parseInt(year);
			month_const = parseInt(month);
			comprehensiveEnergyReport.getStartAndEndDateByMonth(parseInt(month));
			$("#year").val(parseInt(year));
			$("#month").val(parseInt(month));
		}, 
		// 根据月，设置当月的起始时间和结束时间
		getStartAndEndDateByMonth : function (month) {
			month = parseInt(month);
			var startTime = "";
			var endTime = "";
			if (month == 10 || month == 11 || month == 12)
				startTime = year_const + "-" + month + "-" + "01" + " 00:00:00";
			else {
				startTime = year_const + "-0" + month + "-" + "01" + " 00:00:00";
			}
			if (month == 1 || month == 3 || month == 5 || month == 7
					|| month == 8 || month == 10 || month == 12) {
				if (month == 10 || month == 12)
					endTime = year_const + "-" + month + "-" + "31" + " 23:59:59";
				else
					endTime = year_const + "-0" + month + "-" + "31" + " 23:59:59";
			} else if (month == 4 || month == 6 || month == 9 || month == 11) {
				if (month == 11)
					endTime = year_const + "-" + month + "-" + "30" + " 23:59:59";
				else
					endTime = year_const + "-0" + month + "-" + "30" + " 23:59:59";
			} else {
				endTime = year_const + "-0" + month + "-" + "29" + " 23:59:59";
			}
			$("#startDate").val(startTime);
			$("#endDate").val(endTime);
			$("#month").val(month);;
		},
		// 根据年，设置当年的起始时间和结束时间
		getStartAndEndDateByYear : function (year) {
			var startTime = "";
			var endTime = "";
			startTime = year + "-" + "01" + "-" + "01" + " 00:00:00";
			endTime = year + "-" + "12" + "-" + "31" + " 23:59:59";
			$("#startDate").val(startTime);
			$("#endDate").val(endTime);
			$("#year").val(year);
		},
		// 年份选择框change事件
		selectYearChange : function () {
			var curY = $("#year").val();
			var curM = $("#month").val();
			year_const = curY;
			if (curM != "") {
				comprehensiveEnergyReport.getStartAndEndDateByMonth(parseInt(curM));
			} else {
				comprehensiveEnergyReport.getStartAndEndDateByYear(year_const);
			}
		},
		// 月份选择框change事件
		selectMonthChange : function () {
			var curM = $("#month").val();
			if (curM != "") {
				comprehensiveEnergyReport.getStartAndEndDateByMonth(parseInt(curM));
			} else {
				comprehensiveEnergyReport.getStartAndEndDateByYear(year_const);
			}
		},
		// 查询
		inquireClick : function() {
            var vehicleId = $("#brands").attr("data-id");
            var groupId = $("#zTreeContent").val();
			var yyyy = $("#year").val();
			var mm = $("#month").val();
			if (comprehensiveEnergyReport.validates) {
				if (mm != "") {
					$("#echart_title").text(yyyy + "年" + mm + "月综合能耗图表");
				} else {
					$("#echart_title").text(yyyy + "年综合能耗图表");
				}
				startDate = $("#startDate").val();
				endDate = $("#endDate").val();
				comprehensiveEnergyReport.ajaxList(startDate, endDate, groupId, vehicleId);
			}
		},
		// 本月查询按钮点击事件
		curMonthQueryClick : function () {
			comprehensiveEnergyReport.defaultCurDate();
			comprehensiveEnergyReport.inquireClick();
		},
		// 上月查询按钮点击事件
		preMonthQueryClick : function () {
			var curMonth = $("#month").val();
			if (null == curMonth || "null" == curMonth || "" == curMonth) {
				comprehensiveEnergyReport.defaultCurDate();
				curMonth = $("#month").val();
			}
			var curYear = $("#year").val();
			if (curMonth == "1") {
				$("#year").val(parseInt(curYear) - 1);
				$("#month").val("12");
			} else {
				$("#month").val(parseInt(curMonth) - 1);
			}
			
			year_const = parseInt($("#year").val());
			comprehensiveEnergyReport.getStartAndEndDateByMonth($("#month").val());
			comprehensiveEnergyReport.inquireClick();
		},
		// 本年查询按钮点击事件
		curYearQueryClick : function () {
			comprehensiveEnergyReport.defaultCurDate();
			comprehensiveEnergyReport.getStartAndEndDateByYear(year_const);
			$("#month").val("");
			comprehensiveEnergyReport.inquireClick();
		},
		// 上年查询按钮点击事件
		preYearQueryClick : function () {
			var curYear = $("#year").val();
			if (curYear != "" && curYear != 1987) {
				$("#year").val(parseInt(curYear) - 1);
				$("#month").val("");
			}
			comprehensiveEnergyReport.getStartAndEndDateByYear($("#year").val());
			comprehensiveEnergyReport.inquireClick();
		},
		// ajax请求数据
		ajaxList:function(startDate, endDate, groupId, vehicleId){
			layer.load(2);
			var year = $("#year").val();
			var month = $("#month").val();
			var url = "/clbs/v/carbonmgt/comprehensiveEnergyReport/list";
			var data = {"startDate":startDate, "endDate":endDate, "groupId":groupId, "vehicleId":vehicleId, "year":year, "month":month};
			json_ajax("POST", url, "json", true, data, comprehensiveEnergyReport.ajaxListCallback);
		},
		// ajax请求数据回调
		ajaxListCallback : function (data) {
			var mileage = []; //里程
			var totalFuelConsumption = []; //实际能耗
			var baseBenchmark = []; //基准能耗
			var savingEnergy = []; //节约能耗
			var averageSpeed = []; //平均速度
			var energySavingRate = []; //节能率
			var actualEmissions = []; //实际排放
			var baseEmissions = []; //基准排放
			var reduceEmissionsAmount = []; //减排量
			var duceEmissionsRate = []; //减排率
			var echartsDate = [];//数据对应时间点
			for(var i = 0, dataLength = data.length; i < dataLength; i++){
				mileage.push(data[i].mileage);
				totalFuelConsumption.push(data[i].totalFuelConsumption);
				baseBenchmark.push(data[i].baseBenchmark);
				savingEnergy.push(data[i].savingEnergy);
				averageSpeed.push(data[i].averageSpeed);
				energySavingRate.push(data[i].energySavingRate);
				actualEmissions.push(data[i].actualEmissions);
				baseEmissions.push(data[i].baseEmissions);
				reduceEmissionsAmount.push(data[i].reduceEmissionsAmount);
				duceEmissionsRate.push(data[i].reduceEmissionsRate);
				echartsDate.push(data[i].time);
			};
			$(".echartArea").show();
			//节能图表	
			var energyDataName = ["里程","实际能耗","基准能耗","节约能耗","平均速度","节能率"];
			comprehensiveEnergyReport.compositeChart("energyEcharts",mileage,totalFuelConsumption,baseBenchmark,savingEnergy,averageSpeed,energySavingRate,echartsDate,energyDataName);
			//减排图表
			var emissionDataName = ["里程","实际排放","基准排放","减排量","平均速度","减排率"];
			comprehensiveEnergyReport.compositeChart("emissionEcharts",mileage,actualEmissions,baseEmissions,reduceEmissionsAmount,averageSpeed,duceEmissionsRate,echartsDate,emissionDataName);
			$("#toggle-left").bind("click", function () {
                setTimeout(function () {
                	comprehensiveEnergyReport.compositeChart("energyEcharts",mileage,totalFuelConsumption,baseBenchmark,savingEnergy,averageSpeed,energySavingRate,echartsDate,energyDataName);
    				comprehensiveEnergyReport.compositeChart("emissionEcharts",mileage,actualEmissions,baseEmissions,reduceEmissionsAmount,averageSpeed,duceEmissionsRate,echartsDate,emissionDataName);
                }, 500)
            });
			comprehensiveEnergyReport.setFooterData(data);
		},
		// 给图表最下面的显示项赋值
		setFooterData : function (data) {
			if (null != data && data.length > 0) {
				var m = $("#month").val();
				if (m == "") { // 按年统计的
					$("#footer_1").text(data[0].energySaving_curPeriod); // 当年节能
				} else { // 按月统计
					$("#footer_1").text(data[0].fuelType + " " + data[0].energySaving_curPeriod); // 当月节能
				}
				$("#footer_2").text(data[0].energySavingRate_curPeriod); // 节能率
				$("#footer_3").text(data[0].energySavingFee_curPeriod); // 节省费用
				$("#footer_4").text(data[0].curTotalReduceEmissions_CO2); // 减排CO2
			}
		},
		//echarts图表显示
		compositeChart: function(id,data1,data2,data3,data4,data5,data6,echartsDate,dataName){
			var myChart = echarts.init(document.getElementById(id));
            option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                    	var unit = ['km','L','L','L','km/h','%'];
                        var relVal = "";
                        relVal = a[0].name;
                        if(a[0].data == null){
                        	 relVal = "无相关数据";
                        }else{
                            for(var i = 0; i < a.length; i++){
                                relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:"+ a[i].color +"'></span>"+ a[i].seriesName +"："+ (a[i].data == undefined ? "无数据" : (a[i].data + unit[a[i].seriesIndex])) + "";
                            }
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: dataName,
                    left: 'auto',
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: echartsDate
                },
                yAxis: [
                    {
                        type: 'value',
                        name: "里程(km)",
                        scale: true,
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
                        name: '能耗(L)',
                        scale: true,
                        position: 'left',
                        offset: 60,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                        	show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '速度(km/h)',
                        scale: true,
                        splitNumber: 1,
                        position: 'right',
                       /*  offset:100, */
                        splitLine:{
                        	show:false
                        }
                    },
                    {
                        type: 'value',
                        name: '百分率(%)',
                        scale: true,
                        position: 'right',
                        offset: 60,
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine:{
                        	show:false
                        },
                    },
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
                        name: dataName[0],
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#0000cc'
                            }
                        },
                        label:{
                        	normal:{
                        		formatter :'{value}km'
                        	}
                        },
                        data: data1
                    },
                    {
                        name: dataName[1],
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#cc3300'
                            }
                        },
                        data: data2
                    },
                    {
                        name: dataName[2],
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#66cc00'
                            }
                        },
                        data: data3
                    },
                    {
                        name: dataName[3],
                        yAxisIndex: 1,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#663300'
                            }
                        },
                        data: data4
                    },
                    {
                        name: dataName[4],
                        yAxisIndex: 2,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#0099cc'
                            }
                        },
                        data: data5
                    },
                    {
                        name: dataName[5],
                        yAxisIndex: 3,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#ff9900'
                            }
                        },
                        data: data6
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
		}
	}
	$(function(){
		comprehensiveEnergyReport.init();
		$("#groupName,#arrowDown").bind("click", comprehensiveEnergyReport.showMenu);
		selectYear.bind("change", comprehensiveEnergyReport.selectYearChange);
		selectMonth.bind("change", comprehensiveEnergyReport.selectMonthChange);
		$("#inquireClick").bind("click", comprehensiveEnergyReport.inquireClick);
		curMonthQuery.bind("click", comprehensiveEnergyReport.curMonthQueryClick);
		preMonthQuery.bind("click", comprehensiveEnergyReport.preMonthQueryClick);
		curYearQuery.bind("click", comprehensiveEnergyReport.curYearQueryClick);
		preYearQuery.bind("click", comprehensiveEnergyReport.preYearQueryClick);
	});
})(window,$)