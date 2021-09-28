(function ($, window) {

    var msgDetails = {
        //初始化
        init: function () {
            var riskId = $('#riskValId').val();

            $("#exportDoc").click(function () {
                var riskNumber = $('#riskNumberId').text();
                var exportUrl = '/clbs/r/riskManagement/disposeReport/exportDoc?riskId=' + riskId + "&riskNumber=" + riskNumber;
                window.location.href =exportUrl;
            });
            json_ajax('post', '/clbs/r/riskManagement/disposeReport/getRiskReportDetails', "json", true, {'riskId': riskId}, msgDetails.initRiskReportCallBack)
        },
        setRiskRecorder: function (recorderList) {
            var rowNum = 4;
            var showRowNum = 0;
            if (recorderList) {
                var len = recorderList.length;
                showRowNum = Math.ceil(len / rowNum)
            }
            if (showRowNum > 0) {
                var recorderContent = '';
                for (var time = 0; time < showRowNum; time++) {
                    recorderContent = recorderContent + '<tr>';
                    var eventFirstIndex = time * rowNum;
                    var eventLastIndex = eventFirstIndex + rowNum;
                    for (var index = eventFirstIndex; index < eventLastIndex; index++) {
                        var recorder = recorderList[index];
                        if (recorder) {
                            recorderContent = recorderContent + '<td>' + recorder.eventTime.split(' ')[1] + '     ' + recorder.riskEvent + '</td>';
                        } else {
                            recorderContent = recorderContent + '<td style="width:201px"></td>';
                        }

                    }
                    recorderContent = recorderContent + '</tr>';
                }

                $('#recorderBody').append(recorderContent);
            }

        },

        setDriversPhoto: function (drivers) {
            var picRowNum = 3;
            var showPicRowNum = 0;
            if (drivers) {
                var len = drivers.length;
                showPicRowNum = Math.ceil(len / picRowNum)
            }
            if (showPicRowNum > 0) {
                var driverContent = '';
                for (var time = 0; time < showPicRowNum; time++) {
                    driverContent = driverContent + '<tr>';
                    var driverFirstIndex = time * picRowNum;
                    var driverLastIndex = driverFirstIndex + picRowNum;
                    for (var index = driverFirstIndex; index < driverLastIndex; index++) {
                        var driver = drivers[index];
                        if (driver) {
                            driverContent = driverContent + '<td style="width:33%;height:260px;">';
                            driverContent = driverContent + '<img src="' + driver.photograph + '" style="width:95%;height:95%;"><br>';
                            driverContent = driverContent + ' <label><' + (index + 1) + '>' + driver.name + '</label>';
                            driverContent = driverContent + '</td>'
                        } else {
                            driverContent = driverContent + '<td style="width:201px"></td>';
                        }

                    }
                    driverContent = driverContent + '</tr>';
                }
                $('#imgsBodyId').append(driverContent);
            }else{
                var driverContent = '';
                driverContent = driverContent + '<tr>';
                for(var index =0; index<3;index++){
                    driverContent = driverContent + '<td style="width:33%;height:260px;"></td>';
                }
                driverContent = driverContent + '</tr>';
                $('#imgsBodyId').append(driverContent);
            }

        },
        getDrivers: function (drivers) {

            var driversContents = '';
            if (drivers&&drivers.length>0) {
                for (var index = 0, len = drivers.length; index < len; index++) {
                    var driver = drivers[index];
                    var driverContent = '';

                    driverContent = driverContent + '<tr><td style="text-align:left"><' + (index + 1) + '>' + driver.name + '          ';
                    driverContent = driverContent + '联系电话：' + driver.phone + '          ';
                    driverContent = driverContent + '紧急联系人：' + driver.emergencyContact + '          ';
                    driverContent = driverContent + '紧急联系人电话：' + driver.emergencyContactPhone + '<br/></td></tr>';
                    driversContents = driversContents + driverContent;
                }



            }
            return driversContents;
        },
        getDealVisit: function (dealVisit) {
            var dealVisitContent = '';

            if (dealVisit) {
                var index = 0;
                var warningAccuracy = dealVisit.warningAccuracy;
                var warnAfterStatus = dealVisit.warnAfterStatus;
                var interventionPersonnel = dealVisit.interventionPersonnel;
                var interventionAfterStatus = dealVisit.interventionAfterStatus;
                var warningLevel = dealVisit.warningLevel;
                var content = dealVisit.content;
                if (dealVisit.warningAccuracy) {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + warningAccuracy + ';' + '<br>';

                }
                if (dealVisit.warnAfterStatus) {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + warnAfterStatus + ';' + '<br>';

                }
                if (dealVisit.interventionPersonnel) {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + interventionPersonnel + ';' + '<br>';

                }
                if (dealVisit.interventionAfterStatus) {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + interventionAfterStatus + ';' + '<br>';

                }
                if (dealVisit.warningLevel) {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + warningLevel + ';' + '<br>';

                }
                if (dealVisit.content && dealVisit.content.trim() !== '') {
                    index = index + 1;
                    dealVisitContent = dealVisitContent + index + '.' + content + '。' + '<br>';
                }
            }
            return dealVisitContent;
        },
        setVists: function (riskVisits) {
            if (riskVisits && riskVisits.length > 0) {
                var visitsContent = "";
                for (var index = 0, len = riskVisits.length; index < len; index++) {
                    var visit = riskVisits[index];
                    var visitContent = "";
                    visitContent = visitContent + "<tr><td style='vertical-align: middle'>回访" + (index + 1) + '</td><td  colspan="5" style="padding:0px;">';
                    visitContent = visitContent + '<table class="table table-striped table-bordered" cellspacing="0" width="100%"\n' +
                        '                    style="margin-bottom: 0px;">';
                    visitContent = visitContent + '<tr><td colspan="5" style="text-align:left;height:150px;">' + msgDetails.getDealVisit(visit) + '</td></tr>';

                    var driverName = '';
                    if (visit.driverName) {
                        driverName = '<1>' + visit.driverName;
                    }
                    visitContent = visitContent + '<tr><td style="width:80px;" >司机名称</td><td style="text-align:left;">' + driverName + '</td></tr></table></td></tr>';
                    visitsContent = visitsContent + visitContent;
                }
            }

            $('#dealVisitId').after(visitsContent);

        },
        setVisitReason: function (riskVisits) {

            if (riskVisits && riskVisits.length > 0) {
                var riskVisitsReason = '';
                for (var index = 0, len = riskVisits.length; index < len; index++) {
                    var riskVisitReason = '';
                    var riskVisit = riskVisits[index];
                    if (riskVisit) {
                        var reasonStr = '';
                        if (riskVisit.reason) {
                            reasonStr = riskVisit.reason;
                        }
                        riskVisitReason = riskVisitReason + '<tr><td>回访理由' + (index + 2) + '</td><td colspan="5">' + reasonStr + '</td></tr>';
                        riskVisitsReason = riskVisitsReason + riskVisitReason;
                    }
                }

                $('#dealVisitReasonId').after(riskVisitsReason);
            }
        },
        initRiskReportCallBack: function (data) {

            $('#riskNumberId').text(data.riskNumber);
            var wdate = data.warningTime.split(' ')[1];
            $('#warningTimeId').text(wdate);
            $('#brandId').text(data.brand);

            $('#riskTypeId').text(data.riskType);
            $('#riskLevelId').text(data.riskLevel);
            $('#speedId').text(data.speed + 'km/h');
            $('#addressId').text(data.address);

            //预警记录
            var recorderList = data.reafList;
            msgDetails.setRiskRecorder(recorderList);
            $('#groupNameId').text(data.groupName);
            $('#vehicleTypeId').text(data.vehicleType);
            $('#groupPhoneId').text(data.groupPhone);
            var drivers = data.drivers;
            $('#driverInfoBody').html(msgDetails.getDrivers(drivers));
            //司机图片
            msgDetails.setDriversPhoto(drivers);
            $('#dealPeopleId').text(data.dealId);
            $('#dealVistId').html(msgDetails.getDealVisit(data.dealVisit));
            if (data.dealVisit.driverName) {
                $("#dealVisitDriverNameId").text('<1>' + data.dealVisit.driverName);
            }
            //回访记录
            msgDetails.setVists(data.riskVisits);
            //回访理由1->对应处理的那个理由
            $('#dealVisitReason').text(data.dealVisit.reason);
            //添加回访理由
            msgDetails.setVisitReason(data.riskVisits);
            //风控结果
            $('#resultId').text(data.riskResult);

        }
    }
    $(function () {
        msgDetails.init();
    })
})($, window)