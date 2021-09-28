window.paramter = {
    updataFenceData: function (msg) {
        if (msg != null) {
            var dataList = $.parseJSON(msg.body);
            console.log(dataList);
            var subModule = dataList.data.msgBody.subModule;
            var result = dataList.data.msgBody.result;
            var monitorId = dataList.desc.monitorId;
            if (commandType == Number(subModule)) {
                if (multiFlag) {
                    $('#' + self).html(paramter.issueStatus(result));
                } else {
                    $('input[name=subChk]').each(function () {
                        var jsonObj = $.parseJSON($(this).val());
                        var vehicleId = jsonObj.vehicleId;
                        if (vehicleId == monitorId) {
                            $(this).closest('td').next().next('td').html(paramter.issueStatus(result));
                        }
                    });
                }
            }
        }
    },
    issueStatus: function (data) {
        var ele = '';
        switch (data) {
            case 0:
                ele = "参数已生效";
                break;
            case 1:
                ele = "参数未生效";
                break;
            case 2:
                ele = "参数消息有误";
                break;
            case 3:
                ele = "参数不支持";
                break;
            case 4:
                ele = "参数下发中";
                break;
            case 5:
                ele = "终端离线，未下发";
                break;
            case 7:
                ele = "终端处理中";
                break;
            case 8:
                ele = "参数下发失败";
                break;
        }
        return ele;
    }
};