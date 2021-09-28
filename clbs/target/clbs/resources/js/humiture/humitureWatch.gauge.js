/*** humitureWatch.gauge.js start ***/

var wenduChart;				
var shiduChart;
var sensorInfo;
var currentWenduId;
var currentShiduId;
var wenshiduInfo=null;
var currentVid=null;
var wenduAlarm = null;
var shiduAlarm = null;
var hwGauge={
		init: function(){
			wenduChart = echarts.init(document.getElementById('wenduGauge'));
			shiduChart = echarts.init(document.getElementById('shiduGauge'));
			

	        hwGauge.update({
	        	value:-50
	        },"wendu");
	        hwGauge.update({
	        	value:0
	        },"shidu");
		},
		update:function(data,type){
			var commonOption = {
					toolbox: {
	        	        feature: {
	        	            restore: {show:false},
	        	            saveAsImage: {show:false}
	        	        }
	        	    },
	        	    series:[{
	        	    	type: 'gauge',
	        	    	splitNumber:10,
	        	    	radius:'100%',
	        	    	splitLine: {           // 分隔线
        	                length: 25,         // 属性length控制线长
        	                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
        	                    color: 'auto'
        	                }
        	            },
        	            axisLabel:{
        	              formatter:function(value){
        	                  return value + '';
        	              }  
        	            },
        	            detail: {
        	                formatter:function(value){
        	                    if(value<30){
        	                    	return '{e|} {d|异常}'
        	                    }else{  
        	                        return '{c|} {b|正常}'
        	                    }
        	                },
        	                borderColor:'black',
        	                borderWidth:2,
        	                padding:0,
        	                borderRadius:80,
        	                
        	                width:80,
        	                rich: {
	
	        	                b:{
	        	                    color:'black', 
	        	                    fontSize:16,
	        	                },
	        	                c:{
	        	                    backgroundColor:'green',
	        	                    width:15,
	        	                    height:15,
	        	                    borderRadius:55
	        	                },
	        	                d:{
	        	                    color:'black',  
	        	                    fontSize:16,
	        	                },
	        	                e:{
	        	                    backgroundColor:'#d77020',
	        	                    width:15,
	        	                    height:15,
	        	                    borderRadius:55
	        	                }
        	                }
        	            },
	        	    }]
			};
			// 计算阈值
			var alarmColor=null;
			
			var option = {
	        		tooltip : {
	        	        formatter: "{b} : {c}"
	        	    },
	        	    series: [
	        	        {
	        	        	name: '温度 (℃)',
	        	            min:-50,
	        	            max:100,
	        	            detail:{},
	        	            axisLine:{
	        	                lineStyle:{
	        	                	width:15,
	        	                    color:[[0.3333333333, '#d77020'], [0.5, '#84bb53'],[1, '#d77020']]
	        	                }
	        	            },
	        	            
	        	            data: [{value: 50, name: '温度 (℃)',fontSize:22}]
	        	        }
	        	    ]
	        };
			if(type=="shidu"){
				option = {
	        		tooltip : {
	        	        formatter: "{b} : {c} "
	        	    },
	        	    series: [
	        	        {
	        	        	name: '湿度 (%RH)',
	        	            min:0,
	        	            radius:'100%',
	        	            max:100,
	        	            detail:{},
	        	            axisLine:{
	        	                lineStyle:{
	        	                	width:15,
	        	                    color:[[0.3, '#d77020'], [0.7, '#84bb53'],[1, '#d77020']]
	        	                }
	        	            },
	        	            
	        	            data: [{value: 20, name: '湿度 (%RH)',fontSize:22}]
	        	        }
	        	    ]
				};
			}
			if(data.alarmDown !=null && data.alarmUp !=null){
				option.series[0].axisLine.lineStyle.color[0][0] = (data.alarmDown - option.series[0].min) / (option.series[0].max - option.series[0].min);
				option.series[0].axisLine.lineStyle.color[1][0] = (data.alarmUp - option.series[0].min) / (option.series[0].max - option.series[0].min);
				option.series[0].detail.formatter=function(value){
                    if(value<data.alarmDown || value > data.alarmUp){
                    	return '{e|} {d|异常}'
                    }else{  
                        return '{c|} {b|正常}'
                    }
                }
			}
			option.series[0].data[0].value = data.value;
			
			option = $.extend(true,{},commonOption,option);
			if(type == "wendu"){
				wenduChart.setOption(option);
			}else{
				shiduChart.setOption(option);
			}
		},
		initType:function(r, treeNode){
			// 初始化
			wenshiduInfo=null;
			$('#gaugeAddr').html('-');
			$('#gaugeDianya').html('-');
	        
			var wenduI=0, shiduI=0;
			var template = '<div class="active">温度传感1</div>';
			if(r.length>0){
				wenduAlarm = [];
				shiduAlarm = [];
			}
			var clickTypeCB = function(){
				var $this = $(this);
				// 处理样式
				var $prev = $this.siblings('.active');
				var thisSensorId=$this.data('sensorOutId');
				var prevSensorId=$prev.data('sensorOutId');
				if(prevSensorId == null || thisSensorId == prevSensorId){
					return;
				}
				$prev.removeClass('active');
				$this.addClass('active');
				// 处理数据
				if(thisSensorId >=21 && thisSensorId <=25){
					currentWenduId = thisSensorId;
				}else{
					currentShiduId = thisSensorId;
				}
				hwGauge.render();
			};
			var hasWendu=false,hasShidu=false;
			for(var i=0;i<r.length;i++){
				var item = r[i];
				if(item.sensorOutId >=21 && item.sensorOutId <=25){
					// 温度传感器
					if(!hasWendu){
						$('#wenduGaugeType').empty();
						hasWendu=true;
					}
					var div = null;
					if(wenduI++ == 0){
						div=$('<div class="active">温度传感'+(item.sensorOutId-20)+'</div>');
						currentWenduId = item.sensorOutId;
					}else{
						div=$('<div>温度传感'+(item.sensorOutId-20)+'</div>');
					}
					div.data('sensorOutId',item.sensorOutId);
					div.click(clickTypeCB);
					$('#wenduGaugeType').append(div);
					wenduAlarm.push({
						sensorOutId:item.sensorOutId,
						alarmUp:item.alarmUp,
						alarmDown:item.alarmDown
					});
				}else{
					// 湿度传感器
					if(!hasShidu){
						$('#shiduGaugeType').empty();
						hasShidu=true;
					}
					var div = null;
					if(shiduI++ == 0){
						div=$('<div class="active">湿度传感'+(item.sensorOutId-25)+'</div>');
						currentShiduId = item.sensorOutId;
					}else{
						div=$('<div>湿度传感'+(item.sensorOutId-25)+'</div>');
					}
					div.data('sensorOutId',item.sensorOutId);
					div.click(clickTypeCB);
					$('#shiduGaugeType').append(div);
					shiduAlarm.push({
						sensorOutId:item.sensorOutId,
						alarmUp:item.alarmUp,
						alarmDown:item.alarmDown
					});
				}
				
			}
	        
			if(wenduI==0){
				$('#wenduGauge').css('opacity','0.2');
				hwGauge.update({
		        	value:-50
		        },"wendu");
				$('#wenduGaugeType').css('visibility','hidden');
			}else{
				$('#wenduGauge').css('opacity','1');
				$('#wenduGaugeType').css('visibility','visible');
			}
			if(shiduI==0){
				$('#shiduGauge').css('opacity','0.2');
				hwGauge.update({
		        	value:0
		        },"shidu");
				$('#shiduGaugeType').css('visibility','hidden');
			}else{
				$('#shiduGauge').css('opacity','1');
				$('#shiduGaugeType').css('visibility','visible');
			}
			r.wenduI=wenduI;
			r.shiduI=shiduI;
			if(r.length>0){
				sensorInfo=r;
			}else{
				sensorInfo=null;
			}
		},
		unsubscribe:function(){
			if(currentVid!=null){
				// 取消订阅位置信息
				var cancelStrS = {
	                "desc": {
	                    "MsgId": 40964,
	                    "UserName": $("#userName").text()
	                },
	                "data": [currentVid]
	            };
	            webSocket.unsubscribealarm(headers, "/app/location/unsubscribe", cancelStrS);
			}
		},
		subscribe:function(vid){
			if(!vid){
				return;
			}
			
			currentVid = vid;
			// 订阅位置信息

        	var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": [ currentVid ]
            };
        	webSocket.subscribe(headers, '/user/topic/location', hwGauge.updateRealLocation, "/app/location/subscribe", requestStrS);
		},
		updateRealLocation:function(msg){
			var data = JSON.parse(msg.body);
        	if(data.desc !== "neverOnline"){
        		wenshiduInfo = {
        				addr:(data.data.msgBody.positionDescription === null ||
							data.data.msgBody.positionDescription === undefined) ?
							'未定位' : data.data.msgBody.positionDescription,
        				temperatureSensor:data.data.msgBody.temperatureSensor,
        				temphumiditySensor:data.data.msgBody.temphumiditySensor
        		}
        		hwGauge.render();
        		hwMap.update(data);
            }
		},
		render:function(){
			if(wenshiduInfo != null){
				if(sensorInfo){
					$('#gaugeAddr').html(wenshiduInfo.addr);
					$('#gaugeDianya').html('220v');
				}
				var sensorOutId=currentWenduId;
				if(sensorInfo && sensorInfo.wenduI>0){
					var alarm=null;
					for(var k=0; k < wenduAlarm.length; k++){
						if(sensorOutId == wenduAlarm[k].sensorOutId){
							alarm = wenduAlarm[k];
						}
					}
					var _value = -50;
					if(alarm && wenshiduInfo.temperatureSensor){
						for(var i=0;i<wenshiduInfo.temperatureSensor.length;i++){
							var item = wenshiduInfo.temperatureSensor[i];
							if(sensorOutId == item.id.toString(16)){
								_value = item.temperature;
								break;
							}
						}
						if(i<wenshiduInfo.temperatureSensor.length){
							$('#wenduGauge').css('opacity','1');
						}else{
							$('#wenduGauge').css('opacity','0.2');
						}
					}else{
						$('#wenduGauge').css('opacity','0.2');
					}
					hwGauge.update({
						value : _value,
						alarmUp : alarm.alarmUp,
						alarmDown : alarm.alarmDown
					},"wendu");
					
				}
				sensorOutId = currentShiduId;
				if(sensorInfo && sensorInfo.shiduI>0){
					var alarm=null;
					for(var k=0; k < shiduAlarm.length; k++){
						if(sensorOutId == shiduAlarm[k].sensorOutId){
							alarm = shiduAlarm[k];
						}
					}
					var _value = 0;
					if(alarm && wenshiduInfo.temphumiditySensor){
						for(var i=0;i<wenshiduInfo.temphumiditySensor.length;i++){
							var item = wenshiduInfo.temphumiditySensor[i];
							if(sensorOutId == item.id.toString(16)){
								_value = item.temperature;
								break;
							}
						}
						if(i<wenshiduInfo.temphumiditySensor.length){
							$('#shiduGauge').css('opacity','1');
						}else{
							$('#shiduGauge').css('opacity','0.2');
						}
					}else{
						$('#shiduGauge').css('opacity','0.2');
					}
					hwGauge.update({
						value : _value,
						alarmUp : alarm.alarmUp,
						alarmDown : alarm.alarmDown
					},"shidu");
					
				}
//				if(sensorInfo && sensorInfo.shiduI > 0 && wenshiduInfo.temphumiditySensor){
//					for(var i=0;i<wenshiduInfo.temphumiditySensor.length;i++){
//						var item = wenshiduInfo.temphumiditySensor[i];
//						if(sensorOutId == item.id.toString(16)){
//							var alarm=null;
//							for(var k=0; k < shiduAlarm.length; k++){
//								if(sensorOutId == shiduAlarm[k].sensorOutId){
//									alarm = shiduAlarm[k];
//								}
//							}
//							if(!alarm){
//								return false;
//							}
//							hwGauge.update({
//								value : item.temperature/10,
//								alarmUp : alarm.alarmUp,
//								alarmDown : alarm.alarmDown
//							},"shidu");
//							break;
//						}
//						
//					}
//				}
				
			}
		}
}

$(function(){
	hwGauge.init();
})

/*** humitureWatch.gauge.js end ***/