(function (window, $) {
    var submissionFlag = false;
    intercomObjAdd = {
        init: function () {
            var simList = JSON.parse($('#simcardList').val());
            intercomObjAdd.simInit(simList);
        },
        simInit: function (dataList) {
            var simList = {value: []};
            var i = dataList.length;
            while (i--) {
                simList.value.push({
                    name: dataList[i].simcardNumber,
                    id: dataList[i].simcardId,
                    type: dataList[i].monitorName
                });
            }
            $("#simCard").bsSuggest({
                indexId: 1,
                indexKey: 0,
                idField: "id",
                keyField: "name",
                effectiveFields: ["name"],
                searchFields: ["id"],
                data: simList
            }).on('onDataRequestSuccess', function (e, result) {
            }).on('onSetSelectValue', function (e, keyword, data1) {
                $("#simCardId").val(keyword.id);
                $("#monitorName").val(keyword.type);
            }).on('onUnsetSelectValue', function () {
            }).on("input propertychange", function () {
                $("#simCardId").val('');
                $("#monitorName").val('');
            });
        },
        validates: function () {
            return $("#addIntercomForm").validate({
                rules: {
                    simcardNumber: {
                        required: true,
                        isIntercomSim: true,
                        minlength: 7,
                        maxlength: 12,
                        digits: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/intercom/intercomObject/repetitionSimCard",
                            data: {
                                newSimcardNumber: function () {
                                    return $("#simCard").val()
                                },
                                newSimcardId: function () {
                                    return $("#simCardId").val()
                                },
                            },
                            dataFilter: function (data, type) {
                                var data = JSON.parse(data);
                                if (data.success) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        }
                    },
                    intercomDeviceId: {
                        required: true,
                        maxlength: 7,
                        intercomTerminalNo: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/intercom/intercomObject/repetitionIntercomDeviceId",
                            data: {
                                newIntercomDeviceId: function () {
                                    return $("#beforeNo").val() + $("#afterNo").val()
                                },
                                newIntercomObjectIdId: function () {
                                    return $("#originalModelId").val()
                                },
                            },
                        }
                    },
                    devicePassword: {
                        required: true,
                        devicePwd: true,
                    },
                },
                messages:
                    {
                        simcardNumber: {
                            required: '?????????????????????????????????',
                            minlength: '?????????7-12?????????',
                            maxlength: '?????????7-12?????????',
                            digits: '?????????7-12?????????',
                            remote: '????????????????????????'
                        },
                        intercomDeviceId: {
                            required: '????????????????????????',
                            maxlength: '?????????7????????????????????????',
                            remote: '????????????????????????'
                        },
                        devicePassword: {
                            required: '?????????????????????',
                        },
                    }
            }).form();
        },
        doSubmit: function () {
            $('#originalModelId').val($('#beforeNo option:selected').attr('modelid'));
            if (intercomObjAdd.validates() && !submissionFlag) {
                submissionFlag = true;
                $("#addIntercomForm").ajaxSubmit(function (data) {
                    submissionFlag = false;
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonSmWin").modal("hide");
                        myTable.requestData();
                    } else {
                        layer.msg('????????????????????????');
                    }
                });
            }
        },
    };
    $(function () {
        intercomObjAdd.init();
        // input???????????????
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'simCard') {
                $("#simCardId").val('');
                $("#monitorName").val('');
            }
        });
        $('#beforeNo').on('change', function () {
            $('#intercomModal').val($('#beforeNo').find('option:selected').attr('intercomName'));
        });
        $('#doSubmit').on('click', intercomObjAdd.doSubmit);
    })
})(window, $);