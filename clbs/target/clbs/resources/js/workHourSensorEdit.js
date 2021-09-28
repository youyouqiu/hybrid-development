
//# sourceURL=workHourSensorAdd.js
(function(window,$){
    editOilRodSensor = {
        validates: function(){
            return $("#editForm").validate({
                rules: {
                    sensorNumber: {
                        required: true,
                        maxlength : 25,
                        isRightSensorModel: true,
                        // isRightfulString_workhourSensorType : true,
                        remote: {
                            type:"post",
                            async:false,
                            url:"/clbs/v/workhourmgt/workhoursensor/repetition" ,
                            data:{
                                sensorNumber:function(){return $("#sensorNumber").val();},
                                id:function(){return $("#editFieldId").val();}
                            }
                        }
                    }
                },
                messages: {
                    sensorNumber: {
                        required: workHourSensorNumberNull,
                        maxlength: workHourSensorNumberLength,
                        // isRightfulString_workhourSensorType : workHourSensorNumberFormat,
                        remote: oilSensorNumberExists
                    }
                }
            }).form();
        },
        //提交
        doSubmits: function(){
            if(editOilRodSensor.validates()){
                $('#editForm #detectionMode').prop('disabled',false);
                addHashCode($("#editForm"));
                $("#editForm").ajaxSubmit(function(data) {
                    $('#editForm #detectionMode').prop('disabled',true);
                    var data = $.parseJSON(data);
                    if(data.success){
                        $("#commonWin").modal("hide");
                        layer.msg('修改成功',{move:false});
                        myTable.requestData();
                    }else{
                        layer.msg(data.msg,{move:false});
                    }
                });
            }
        },
    }
    $(function(){
        $('input').inputClear();
        $("#editSubmits").bind("click",editOilRodSensor.doSubmits);
    })
})(window,$)