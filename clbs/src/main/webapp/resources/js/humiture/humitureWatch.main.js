/*** humitureWatch.min.js start ***/
var $contentLeft = $("#content-left"),
	$contentRight = $("#content-right"), 
	$sidebar = $(".sidebar"), 
	$mainContentWrapper = $(".main-content-wrapper");
var globalLineLegend=[];
var tempSensorThresholdData = []; // 温度传感器信息(上下阈值)
var humSensorThresholdData = []; // 湿度传感器个数(上下阈值)
var hwMain = {
		init: function(){
			// 隐藏左侧导航栏
			$sidebar.attr("class", "sidebar sidebar-toggle");
//			$mainContentWrapper.attr("class", "main-content-wrapper main-content-toggle-left");
			var winHeight = $(window).height();						//可视区域高度
			var headerHeight = $("#header").height();				//头部高度
			var thetreeHeight = winHeight - headerHeight - 185;		//树高度
			// $("#thetree").css('height',thetreeHeight + "px");		//树高度
			$('#search_condition').inputClear().on('onClearEvent',function(e,data){
				humiture.refreshTree();
		    });
//			var myChartContainer = function () {
//				$('#temAndHumDataShow>div').get(0).style = $('#temAndHumDataShow').width()+'px';
//				$('#temAndHumDataShow>div>canvas').get(0).style = $('#temAndHumDataShow').width()+'px';
//            };

			$('#toggle-left-button').click(function(){
//				myChartContainer();
				setTimeout(function(){
					myChart.resize();
				},400);
			});
			window.onresize=function(){
				var winHeight = $(window).height();						//可视区域高度
				var headerHeight = $("#header").height();				//头部高度
				var thetreeHeight = winHeight - headerHeight - 185;		//树高度
				// $("#thetree").css('height',thetreeHeight + "px");		//树高度
				myChart.resize();
			}
		},
		formatTime :function(dateStr,onlyDate,spliter) {
			if(!spliter){
				spliter = '/';
			}
			var formatNumber = function(n) {
			  n = n.toString()
			  return n[1] ? n : '0' + n
			}
			var date=new Date();
			date.setTime(dateStr)
			var year = date.getFullYear()
			var month = date.getMonth() + 1
			var day = date.getDate()
			var hour = date.getHours()
			var minute = date.getMinutes()
			var second = date.getSeconds()

			if(onlyDate){
				return [year, month, day].map(formatNumber).join(spliter) 
			}else{
				return [year, month, day].map(formatNumber).join(spliter) + ' ' + [hour, minute, second].map(formatNumber).join(':')
			}
		},
		dbClickTreeCB: function(treeNode){
			

			var now = new Date();
			var startTime= hwMain.formatTime(now.getTime(), true, '-') + ' 00:00:00';
			var endTime= hwMain.formatTime(now.getTime(), true, '-') + ' 23:59:59';
			var toPost = {
					vehicleId : treeNode.id,
					flag : 1,
					startTime: startTime,
					endTime: endTime
			}
			var url = '/clbs/v/monitoring/humiture/tempHum';
			// 上传
			json_ajax("POST", url, "json", true, toPost, function(r){
				if(!r.success){
					layer.msg(publicErrorTip);
					return;
				}
				r = r.obj.transdusermonitorSet;
				console.log('传感器信息：',r)
				hwGauge.initType(r,treeNode);
				hwGauge.unsubscribe();
				hwGauge.update({
		        	value:-50
		        },"wendu");
		        hwGauge.update({
		        	value:0
		        },"shidu");
				hwGauge.subscribe(treeNode.id);
				if(r.length>0){
					$('#line').css('opacity','1');
					$('#lineMask').hide();
					$('#vehicleId').val(treeNode.id);
					humitureWatch.todayClick();
				}else{
					$('#line').css('opacity','0.1');
					$('#lineMask').show();
					$("#vehicleId").val('');
				}
				globalLineLegend=[];
				 for(var i=0;i<r.length;i++){
				 	var item = r[i];
				 	if(item.sensorOutId >=21 && item.sensorOutId <=25){
				 		//温度
				 		globalLineLegend.push('温度传感器'+(item.sensorOutId-20))
				 	}else{
				 		globalLineLegend.push('湿度传感器'+(item.sensorOutId-25))
				 	}
				 }
				 if(!r.length){
					 humitureWatch.ajaxList("ca00dba8-baed-475b-83d1-e43f56e84353", "2018-01-25 00:00:00", "2018-01-24 23:59:59",1);
				 }
//                var sensorInfo = r;
//                if (sensorInfo != null && sensorInfo.length > 0) {
//                    for (var number = 0; number < sensorInfo.length; number++) {
//                        if (sensorInfo[number].sensorOutId == 21 || sensorInfo[number].sensorOutId == 22 || sensorInfo[number].sensorOutId == 23 ||
//                            sensorInfo[number].sensorOutId == 24 ||sensorInfo[number].sensorOutId == 25) { // 温度传感器信息
//                            var tempSensorType;
//                            switch (sensorInfo[number].sensorOutId) {
//                                case 21:
//                                    tempSensorType= "温度传感器1";
//                                    break;
//                                case 22:
//                                    tempSensorType = "温度传感器2";
//                                    break;
//                                case 23:
//                                    tempSensorType = "温度传感器3";
//                                    break;
//                                case 24:
//                                    tempSensorType = "温度传感器4";
//                                    break;
//                                case 25:
//                                    tempSensorType = "温度传感器5";
//                                    break;
//                                default:
//                                    tempSensorType = "未知...";
//                                    break;
//                            }
//                            var tempThreshold =
//                                {"sensorId": sensorInfo[number].sensorOutId,"alarmUp":sensorInfo[number].alarmUp,
//                                    "alarmDown":sensorInfo[number].alarmDown,"tempSensorType":tempSensorType};
//                            tempSensorThresholdData.push(tempThreshold);
//                            globalLineLegend.push(tempSensorType);
//                        }
//
//                        if (sensorInfo[number].sensorOutId == 26 || sensorInfo[number].sensorOutId == 27 || sensorInfo[number].sensorOutId == 28 ||
//                            sensorInfo[number].sensorOutId == 29) { //湿度传感器信息
//                            var humSensorType;
//                            switch (sensorInfo[number].sensorOutId) {
//                                case 26:
//                                    humSensorType = "湿度传感器1";
//                                    break;
//                                case 27:
//                                    humSensorType = "湿度传感器2";
//                                    break;
//                                case 28:
//                                    humSensorType = "湿度传感器3";
//                                    break;
//                                case 29:
//                                    humSensorType = "湿度传感器4";
//                                    break;
//                                default:
//                                    humSensorType = "未知...";
//                                    break;
//                            }
//                            var humitThreshold = {"sensorId": sensorInfo[number].sensorOutId,"alarmUp":sensorInfo[number].alarmUp,
//                                "alarmDown":sensorInfo[number].alarmDown,"humSensorType":humSensorType};
//                            humSensorThresholdData.push(humitThreshold);
//                            globalLineLegend.push(humSensorType);
//                        }
//                    }
//                    console.log("传感器的信息");
//                    console.log(tempSensorThresholdData);
//                    console.log(humSensorThresholdData);
//                    console.log(globalLineLegend);
//                }
				// globalLineLegend = globalLineLegend.sort();
			});
		}
}

$(function(){
	hwMain.init();
})

/*** humitureWatch.min.js end ***/