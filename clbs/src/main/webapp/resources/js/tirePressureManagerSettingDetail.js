(function (window, $) {
    var transduserManage = JSON.parse($("#allSensor").attr("value"));
    var sensorId = $('#sensorId').val();
    var autotime = $('#autotime').val();

    tirePressureSettingDetail = {
        init: function () {
            tirePressureSettingDetail.getAutoTime();
            //传感器型号下拉选
            if (transduserManage != null && transduserManage.length > 0) {
                for (var i = 0; i < transduserManage.length; i++) {
                    var id = transduserManage[i].id;

                    console.log(transduserManage);
                    if(id == sensorId){
                        $('#sensorNumber').val(transduserManage[i].sensorNumber);
                        $("#sensorId").val(transduserManage[i].id);
                        $("#compensate").val(tirePressureSettingDetail.compensateVal(transduserManage[i].compensate));
                        $("#filterFactor").val(tirePressureSettingDetail.filterFactorVal(transduserManage[i].filterFactor));
                    }
                }
            }

        },
        //自动上传时间
        getAutoTime:function(){
            var autotimeName = '';
            switch (autotime) {
                case '1':
                    autotimeName='被动';
                    break;
                case '2':
                    autotimeName=10;
                    break;
                case '3':
                    autotimeName=20;
                    break;
                case '4':
                    autotimeName=30;
                    break;
                default:
                    autotimeName = '';
                    break;
            }
            $('#autoTimeName').val(autotimeName);
        },
        compensateVal:function(data){
            switch (data){
                case 1:
                    return '使能';
                    break;
                case 2:
                    return '禁用';
                    break;
                default:
                    break;
            }
        },
        filterFactorVal:function(data){
            switch (data){
                case 1:
                    return '实时';
                    break;
                case 2:
                    return '平滑';
                    break;
                case 3:
                    return '平稳';
                    break;
                default:
                    break;
            }
        }
    }
    $(function () {
        tirePressureSettingDetail.init();
    })
})(window, $)