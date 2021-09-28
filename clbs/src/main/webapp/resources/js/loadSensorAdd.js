
//# sourceURL=workHourSensorAdd.js
(function(window,$){
    addOilRodSensor = {
        validates: function(){
            return $("#addForm").validate({
                rules: {
                    sensorNumber: {
                        required: true,
                        maxlength : 25,
                        isRightSensorModel: true,
                        // isRightfulString : true,
                        remote: {
                            type:"post",
                            async:false,
                            url:"/clbs/v/loadmgt/loadsensor/repetition" ,
                            data:{
                                sensorNumber:function(){return $("#sensorNumber").val();}
                            }
                        }
                    }
                },
                messages: {
                    sensorNumber: {
                        required: workHourSensorNumberNull,
                        maxlength: workHourSensorNumberLength,
                        // isRightfulString : workHourSensorNumberFormat,
                        remote: oilSensorNumberExists
                    }
                }
            }).form();
        },
        //提交
        doSubmits: function(){
            if(addOilRodSensor.validates()){
                addHashCode($("#addForm"));
                $("#addForm").ajaxSubmit(function(data) {
                    var data = $.parseJSON(data)
                    if(data.success){
                        $("#commonWin").modal("hide");
                        layer.msg('新增成功',{move:false});
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
        $("#doSubmitsAdd").bind("click",addOilRodSensor.doSubmits);
    })
})(window,$)