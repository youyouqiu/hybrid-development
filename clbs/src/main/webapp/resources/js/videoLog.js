(function(window,$){
	var myTable;
	var begin;
	var over;
	videoLog = {
    	init: function(){
    		//表格列定义
            var columnDefs = [ {
                //第一列，用来显示序号
                "searchable" : false,
                "orderable" : false,
                "targets" : 0
            }];
            var columns = [
                {
                    //第一列，用来显示序号
                    "data" : null,
                    "class" : "text-center"
                },
                {
                    "data" : "eventDate",
                    "class" : "text-center"
                },
                {
                    "data" : "ipAddress",
                    "class" : "text-center"
                },
                {
                    "data" : "username",
                    "class" : "text-center"
                },
                {
                    "data" : "brand",
                    "class" : "text-center"
                },
                {
                    "data" : "plateColor",
                    "class" : "text-center",
                    render: function (data, type, row, meta) {
                        return getPlateColor(data);
                    }
                },
                {
                    "data" : "groupName",
                    "class" : "text-center"
                },
                {
                    "data" : "userCount",
                    "class" : "text-center"
                },
                {
                    "data" : "logFlow",
                    "class" : "text-center",
                    render: function (data, type, row, meta) {
                        if (data.toString().length == 3) {
                            data = data + "0";
                        }
                        return data;
                    }
                },
                {
                    "data" : "message",
                    "class" : "text-center",
                    render: function (data, type, row, meta) {
                    	if (row.logSource == "1") {
	       					 return "<a onclick = 'videoLog.showLogContent(\"" + data + "\")'>" + row.monitoringOperation + "</a>";
                    	}else if(row.module=='more'){
                    		 return "<a onclick = 'videoLog.showLogContent(\"" + data + "\")'>" + row.monitoringOperation + "</a>";
                    	}else if(row.module=='batch'){ //批量删除
                            return "<a onclick = 'videoLog.showLogContent(\"" + data + "\")'>" + row.monitoringOperation + "</a>";
                        }else if(data.indexOf("修改绑定关系")!=-1){
                            return "<a onclick = 'videoLog.showLogContent(\"" + data + "\")'>" + "信息列表：修改绑定关系" + "</a>";
                        } else if (row.monitoringOperation === '批量文本信息下发') {
                            return "<a onclick = 'videoLog.showLogContent(\"" + data + "\")'>" + row.monitoringOperation + "</a>";
                        }
                    		if (data.indexOf("<br/>") > 0) {
                    			return data;
                    		}
                        		return html2Escape(data);


                    }

                },
                {
                    "data" : "logSource",
                    "class" : "text-center",
                    render: function (data, type, row, meta) {
                        if (data == "1") {
                            return '终端上传';
                        } else if (data == "2") {
                            return '平台下发';
                        } else if (data == "3") {
                            return '平台操作';
                        }
                    }
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function(d) {
            	var timeInterval = $('#timeInterval').val().split('--');
            	d.startTime = timeInterval[0];
            	d.endTime = timeInterval[1];
                d.username = $('#usernameSearch').val();
                d.message = $('#messageSearch').val();
            };
            //表格setting
            var setting = {
                listUrl : "/clbs/m/reportManagement/logSearch/videoLog",
                columnDefs : columnDefs, //表格列定义
                columns : columns, //表格列
                dataTableDiv : 'dataTable', //表格
                ajaxDataParamFun : ajaxDataParamFun, //ajax参数
                pageable : true, //是否分页
                showIndexColumn : true, //是否显示第一列的索引列
                enabledChange : true
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },

        videoExport: function () {
            if ($("#dataTable tbody tr td").hasClass("dataTables_empty")) {
                layer.msg("列表无任何数据，无法导出");
                return;
            }
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            var endTime = timeInterval[1];
            var username = $('#usernameSearch').val();
            var message = $('#messageSearch').val();
            var data = {"startTime": startTime, 'endTime': endTime, "username": username,"message": message};
            var url = "/clbs/m/reportManagement/logSearch/videoExport";
            json_ajax("POST", url, "json", false, data, videoLog.exportCallback); //发送请求
        },

        exportCallback: function (reuslt) {
            if (reuslt == true) {
                var url = "/clbs/m/reportManagement/logSearch/videoExport";
                window.location.href = url;
            } else {
                layer.msg(exportFail,{move:false});
            }
        },




        //开始时间
        startDay: function (day) {
        	var timeInterval = $('#timeInterval').val().split('--');
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
                tMonth = videoLog.doHandleMonth(tMonth + 1);
                tDate = videoLog.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = videoLog.doHandleMonth(endMonth + 1);
                endDate = videoLog.doHandleMonth(endDate);
                endTime = endYear + "-" + endMonth + "-" + endDate + " "
                    + "23:59:59";
            } else {
            	var startTimeIndex = startValue.slice(0,10).replace("-","/").replace("-","/");
            	var vtoday_milliseconds = Date.parse(startTimeIndex) + 1000 * 60 * 60 * 24 * day;
            	var dateList = new Date();
            	dateList.setTime(vtoday_milliseconds);
            	var vYear = dateList.getFullYear();
                var vMonth = dateList.getMonth();
            	var vDate = dateList.getDate();
                vMonth = videoLog.doHandleMonth(vMonth + 1);
                vDate = videoLog.doHandleMonth(vDate);
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
                    vendMonth = videoLog.doHandleMonth(vendMonth + 1);
                    vendDate = videoLog.doHandleMonth(vendDate);
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
            if(atime!=undefined && atime!=""){
            	startTime = atime;
        	}
        },
        inquireClick: function (number) {
        	if(number==0){
        		videoLog.getsTheCurrentTime();
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number==-1){
        		videoLog.startDay(-1);
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number==-3){
        		videoLog.startDay(-3)
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number ==-7){
        		videoLog.startDay(-7)
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}
        	var timeInterval = $('#timeInterval').val().split('--');
        	startTime = timeInterval[0];
        	endTime = timeInterval[1];
            if (videoLog.validates()) {
            	myTable.filter();
            }
            $("#dataTable_first").click();
            $("#export").removeAttr("disabled");
        },
        validates: function(){
            return $("#logSearchList").validate({
                rules: {
                    endTime: {
                        compareDate: "#timeInterval",
                    }
                },
                messages: {
                    endTime: {
                        compareDate: "结束日期必须大于开始日期!",
                    }
                }
            }).form();
        },
        showLogContent: function (content){ // 显示log详情
      		$("#logDetailDiv").modal("show");
      		$("#logContent").html(content);
      	},
    }
    $(function(){
    	$('input').inputClear();
    	$('#timeInterval').dateRangePicker({
            dateLimit: 31
        })
    	//当前时间
    	videoLog.getsTheCurrentTime();
        //初始化页面
    	videoLog.init();
        //查询
    })
}(window,$))