(function(window,$){
    var submissionFlag = false;
	obdVehicleEdit = {
	    //提交
        doSubmit: function () {
            if (submissionFlag) {  // 防止重复提交
                return;
            } else {
                if (obdVehicleEdit.validates()) {
                    submissionFlag = true;
                    addHashCode($("#editForm"));
                    $("#editForm").ajaxSubmit(function (data) {
                        var json = eval("(" + data + ")");
                        if (json.success) {
                            $("#commonWin").modal("hide");
                            myTable.requestData();
                        } else {
                            layer.msg(json.msg);
                        }
                    });
                }
            }
        },
        //表单验证
        validates: function () {
            return $("#editForm").validate({
                rules: {
                    name: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/v/obdManager/obdVehicleType/repetition",
                            data: {
                                type: function () {
                                    return $("#deviceType").val();
                                },
                                id:function () {
                                    return $('#vehicleId').val();
                                },
                            },
                        }
                    },
                    code: {
                        required: true,
                        maxlength: 10,
                        check16: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/v/obdManager/obdVehicleType/checkCode",
                            data: {
                                id:function () {
                                    return $('#vehicleId').val();
                                },
                            },
                        }
                    },
                    description: {
                        required: false,
                        maxlength: 50
                    }
                },
                messages: {
                    name: {
                        required: modelNameNull,
                        maxlength: publicSize20,
                        remote:modelNameExists,
                    },
                    code: {
                        required: deviceIdNull,
                        maxlength: publicSize10,
                        remote:deviceIdExists,
                    },
                    description: {
                        maxlength: publicSize50
                    }
                },
                submitHandler: function (form) {

                }
            }).form();
        },
        //车辆类型切换
        deviceTypeChange:function(){
            var group = $('#deviceNameGroup'),
                nameInput = $('#name');
            var deviceType = $(this).val();
            if(deviceType==0){
                group.removeClass('typeGroup');
                nameInput.attr('placeholder','请输入车型名称');
            }else{
                group.addClass('typeGroup');
                nameInput.attr('placeholder','请输入发动机类型');
            }
            $('#name-error').text('').hide();
        }
	}
	$(function(){
        $('input').inputClear();
        $("#deviceType").on("change",obdVehicleEdit.deviceTypeChange);
        $("#doSubmit").bind("click", obdVehicleEdit.doSubmit);
	})
})(window,$)