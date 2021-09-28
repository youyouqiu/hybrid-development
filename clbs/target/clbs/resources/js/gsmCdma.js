(function(window,$){
    var addLocationTimeIndex = 2;
    
    realTimeMonitoringGsmCdma = {
        //基站参数设置 定点时间添加
        addLocationTimeEvent: function(){
            var bsfpLength = $("#baseStation-MainContent").find("div.form-group").length;
            var bs = parseInt(bsfpLength) + 1;
            if(bs > 12){
                layer.msg("定点时间最多允许存在12个哟！");
            }else{
            	addLocationTimeIndex++;
                var html = "<div class='form-group'><label class='col-md-4 control-label'>定点时间：</label><div class='col-md-4'><input type='text' id='locationTimes_"+addLocationTimeIndex+"' name='locationTimes' onclick='' class='form-control' style='cursor: pointer;  background-color: #fafafa;' readonly/></div><div class='col-md-1'><button type='button' class='btn btn-danger baseStationDelete deleteIcon'><span class='glyphicon glyphicon-trash' aria-hidden='true'></span></button></div></div>";
                $("#baseStation-MainContent").append(html);
                laydate.render({elem: '#locationTimes_'+addLocationTimeIndex,type: 'time',theme: '#6dcff6'});
                $("#locationTimes_"+addLocationTimeIndex).val(loadInitTime);
                $(".baseStationDelete").on("click",function(){
                    $(this).parent().parent().remove();
                });
            }
        },
        getHoursMinutesSeconds: function(){
        	loadInitTime = 
            + (loadInitNowDate.getHours()< 10 ? "0" + loadInitNowDate.getHours() : loadInitNowDate.getHours())+":"
            + (loadInitNowDate.getMinutes() < 10 ? "0" + loadInitNowDate.getMinutes() : loadInitNowDate.getMinutes())+":"
            + (loadInitNowDate.getSeconds() < 10 ? "0" + loadInitNowDate.getSeconds() : loadInitNowDate.getSeconds());
        	$("#requiteTime,#locationTimes").val(loadInitTime);
        },
        getsTheCurrentTime: function () {
        	var nowDate = new Date();
            startTime = parseInt(nowDate.getFullYear()+1)
            + "-"
            + (parseInt(nowDate.getMonth() + 1) < 10 ? "0"
            + parseInt(nowDate.getMonth() + 1)
            : parseInt(nowDate.getMonth() + 1))
            + "-"
            + (nowDate.getDate() < 10 ? "0" + nowDate.getDate()
            : nowDate.getDate()) + " "
            + (nowDate.getHours()< 10 ? "0" + nowDate.getHours():nowDate.getHours())+":"
            + (nowDate.getMinutes() < 10 ? "0" + nowDate.getMinutes():nowDate.getMinutes())+":"
            + (nowDate.getSeconds() < 10 ? "0" + nowDate.getSeconds():nowDate.getSeconds())+" ";
            $("#reportStartTime").val(startTime);
        },
        reportformValide:function(){
            return $("#reportFrequency").validate({
                rules: {
                    hours: {
                        required: true,
                        isInt1to24:true,
                    },
                    minute: {
                        required: true,
                        isInt1to59:true
                    },
                    reportStartTime:{
                        required:true,
                        selectDate:true
                    }
                },
                messages: {
                    hours: {
                        required: "不能为空",
                        isInt1to24:"请输入1-24之间的整数"
                    },
                    minute: {
                        required: "不能为空",
                        isInt1to59:"请输入0-59之间的整数"
                    },
                    reportStartTime:{
                        required:"请选择一个时间",
                        selectDate:"选择的时间必须大于等于今天"
                    }
                }
            }).form();
        },
        tailAfter:function () {
            if (realTimeMonitoringGsmCdma.tailAfterFormValide) {
                $("#locationTailAfterList").ajaxSubmit(function (data) {
                    $("#locationTailAfter").modal("hide");
                    if(JSON.parse(data).success){
                        layer.msg("指令发送成功");
                        setTimeout("dataTableOperation.logFindCilck()",500);
                    }
                });
            }
        },
        tailAfterFormValide:function(){
            return $("#locationTailAfterList").validate({
                rules: {
                    tailAfterTime:{
                        required:true,
                        isDigits:true

                    },
                    IntervalTime:{
                        required:true,
                        isDigits:true
                    }
                },
                message:{
                    tailAfterTime:{
                        required:"跟踪时长不能为空",
                        isDigits:"请输入正确的数字"
                    },
                    IntervalTime:{
                        required:"时间间隔不能为空",
                        isDigits:"请输入正确的数字"
                    }
                }
            }).form()
        },
        getSystemTime:function(){
            var url="/clbs/v/monitoring/getTime";
            json_ajax("POST",url,"json",true,null,realTimeMonitoringGsmCdma.systemCallBack);
        },
        systemCallBack:function (data) {
            if(data != null){
                    $("#showTime").val(data);
            }
        },
    }
    $(function(){
        realTimeMonitoringGsmCdma.getsTheCurrentTime();
        realTimeMonitoringGsmCdma.getHoursMinutesSeconds();
    	$(".modal-body").addClass("modal-body-overflow");
        laydate.render({elem: '#baseStationFixedTime',type: 'time',theme: '#6dcff6'});
        laydate.render({elem: '#tailAfterdTime',type: 'time',theme: '#6dcff6'});
        $("#goInfoLocationTailAfter").on("click",realTimeMonitoringGsmCdma.tailAfter);
        $("#systemTime").on("click",realTimeMonitoringGsmCdma.getSystemTime);
    })
})(window,$)