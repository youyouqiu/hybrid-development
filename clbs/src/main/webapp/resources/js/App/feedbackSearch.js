(function(window,$){
	var myTable;
	var startTime,endTime;

	feedbackSearch = {
        /**
         * 表格初始化
         */
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
                    "data" : "userName",
                    "class" : "text-center"
                },
                {
                    "data" : "feedBack",
                    "class" : "text-center",
                    render:function (data) {
                        if (data != null && data != '') {
                            return html2Escape(data);
                        }
                        else {
                            return '';
                        }
                    }
                },
                {
                    "data" : "submitDate",
                    "class" : "text-center"
                }
            ];
            //ajax参数
            var ajaxDataParamFun = function(d) {
            	var timeInterval = $('#timeInterval').val().split('--');

            	d.startTime = timeInterval[0];
                d.endTime = timeInterval[1];
                d.userName = $('#usernameSearch').val();
            };
            //表格setting
            var setting = {
                listUrl : "/clbs/m/app/feedback",
                columnDefs : columnDefs, //表格列定义
                columns : columns, //表格列
                dataTableDiv : 'dataTable', //表格
                ajaxDataParamFun : ajaxDataParamFun, //ajax参数
                pageable : true, //是否分页
                showIndexColumn : true, //是否显示第一列的索引列
                enabledChange : true,
                type: 'POST'
            };
            myTable = new TG_Tabel.createNew(setting);
            myTable.init();
        },
        /**
         * 导出
         */
        export: function () {
            if(getRecordsNum() > 60000){
                return layer.msg("导出数据量过大，请缩小查询范围再试（单次导出最多支持6万条）！");
            }
            var timeInterval = $('#timeInterval').val().split('--');
            var startTime = timeInterval[0];
            var endTime = timeInterval[1];
            var username = $('#usernameSearch').val();
            var data = {"startTime": startTime, 'endTime': endTime, "userName": username};
            var url = "/clbs/m/app/feedbackSearch/export";
            json_ajax("POST", url, "json", false, data, feedbackSearch.exportCallback);     //发送请求
        },
        exportCallback: function (reuslt) {
            if (reuslt == true) {
                var url = "/clbs/m/app/feedbackSearch/export";
                window.location.href = url;
            } else {
                layer.msg(exportFail,{move:false});
            }
        },
        /**
         * 时间
         * @param day
         */
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
                tMonth = feedbackSearch.doHandleMonth(tMonth + 1);
                tDate = feedbackSearch.doHandleMonth(tDate);
                var num = -(day + 1);
                startTime = tYear + "-" + tMonth + "-" + tDate + " "
                    + "00:00:00";
                var end_milliseconds = today.getTime() + 1000 * 60 * 60 * 24
                    * parseInt(num);
                today.setTime(end_milliseconds); //注意，这行是关键代码
                var endYear = today.getFullYear();
                var endMonth = today.getMonth();
                var endDate = today.getDate();
                endMonth = feedbackSearch.doHandleMonth(endMonth + 1);
                endDate = feedbackSearch.doHandleMonth(endDate);
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
                vMonth = feedbackSearch.doHandleMonth(vMonth + 1);
                vDate = feedbackSearch.doHandleMonth(vDate);
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
                    vendMonth = feedbackSearch.doHandleMonth(vendMonth + 1);
                    vendDate = feedbackSearch.doHandleMonth(vendDate);
                    endTime = vendYear + "-" + vendMonth + "-" + vendDate + " "
                        + "23:59:59";
                }
            }
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
        doHandleMonth: function (month) {
            var m = month;
            if (month.toString().length == 1) {
                m = "0" + month;
            }
            return m;
        },
        /**
         * 查询
         * @param number
         */
        inquireClick: function (number) {
        	if(number==0){ 
        		feedbackSearch.getsTheCurrentTime();
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number==-1){
        		feedbackSearch.startDay(-1);
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number==-3){
        		feedbackSearch.startDay(-3)
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}else if(number ==-7){
        		feedbackSearch.startDay(-7)
        		$('#timeInterval').val(startTime + '--' + endTime);
        	}

        	var timeInterval = $('#timeInterval').val().split('--');
        	startTime = timeInterval[0];
        	endTime = timeInterval[1];
            if (feedbackSearch.validates()) {
            	myTable.filter(); 
            }
            $("#dataTable_first").click();
        },
        validates: function(){
            return $("#feedbackSearchList").validate({
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
    } 
    $(function(){
        $('input').inputClear();
        $('#timeInterval').dateRangePicker();
        feedbackSearch.getsTheCurrentTime();//当前时间
        feedbackSearch.init();//初始化页面
    })
})(window,$)