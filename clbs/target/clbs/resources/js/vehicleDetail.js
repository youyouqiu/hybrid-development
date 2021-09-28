//# sourceURL=vehicleEdit.js
(function ($, window) {
    var vehicleId;
    var selectVehicleType;
    var selfRespectFlag = false;
    var abilityWorkFlag = false;
    vehicleEdit = {
        //点击显示隐藏信息
        hiddenparameterFn: function () {
            var clickId = $(this).attr('id');
            if (!($("#" + clickId + "-content").is(":hidden"))) {
                $("#" + clickId + "-content").slideUp();
                $("#" + clickId).children("font").text("显示更多");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                $("#" + clickId + "-content").slideDown();
                $("#" + clickId).children("font").text("隐藏信息");
                $("#" + clickId).children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        //获取车辆类别
        getVehicleCategory: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/listCategory";
            var data = {"vehicleCategory": ""}
            json_ajax("POST", url, "json", false, data, vehicleEdit.categoryCallBack);
        },
        categoryCallBack: function (data) {
            var vc = $("#vehicleCategory").attr("value");
            var result = data.records;
            var str = "<option value=''></option>";
            for (var i = 0; i < result.length; i++) {
                if (vc == result[i].vehicleCategory) {
                    str += '<option selected="selected" value="' + result[i].id + '" standard="' + result[i].standard + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                } else {
                    str += '<option  value="' + result[i].id + '" standard="' + result[i].standard + '">' + html2Escape(result[i].vehicleCategory) + '</option>'
                }
            }

            $("#vehicleCategory").html(str);
        },
        //判断用户所选车辆类别标准
        checkCategoryStandard: function (standard) {
            var freightTransportBox = $(".freightTransport-box");
            var constructionMachineryBox = $(".constructionMachinery-box");
            if (standard == '0' || standard == null || standard == 'null' || standard == undefined) {
                freightTransportBox.hide();
                constructionMachineryBox.hide();
            }
            else if (standard == '1') {
                freightTransportBox.show();
                constructionMachineryBox.hide();
            } else if (standard == '2') {
                freightTransportBox.hide();
                constructionMachineryBox.show();
            }
        },
        //自重是否必填
        selfRespectRequired: function () {
            var vehicleCategory = $("#vehicleCategory option:selected").html();
            var drivingWay = $("#vehicleSubtypes option:selected").attr('driving');
            if (vehicleCategory == '工程车辆' && drivingWay == '1') {
                selfRespectFlag = true;
                $('.selfRespect-required').show();
            }
            else {
                selfRespectFlag = false;
                $('.selfRespect-required').hide();
            }
        },
        selfRespectShow: function () {
            if ($("#selfRespect").val() == '') {
                if (selfRespectFlag) {
                    $("#selfRespect-error").html('请输入车辆自重');
                    $("#selfRespect-error").show();
                }
                else {
                    $("#selfRespect-error").hide();
                }
            }
            else {
                $("#selfRespect-error").hide();
            }
        },

        //工作能力是否必填
        abilityWorkRequired: function () {
            var vehicleCategory = $("#vehicleCategory option:selected").html();
            var vehicleType = $("#vehicleType option:selected").html();
            if (vehicleCategory == '运输车辆' && vehicleType == '拖车') {
                abilityWorkFlag = true;
                $('.abilityWork-required').show();
            }
            else {
                abilityWorkFlag = false;
                $('.abilityWork-required').hide();
            }
        },
        abilityWorkShow: function () {
            var abilityWorkError = $("#abilityWork-error");
            if ($("#abilityWork").val() == '') {
                if (abilityWorkFlag) {
                    abilityWorkError.html('请输入车辆工作能力');
                    abilityWorkError.show();
                }
                else {
                    abilityWorkError.hide();
                }
            }
            else {
                abilityWorkError.hide();
            }
        },
      // 运输证和行驶证正反面照片显示
      carInfoPhoto: function () {
        var transportNumberPhotoUrl = $("#transportNumberPhoto").val();
        var drivingLicenseFrontPhotoUrl = $("#drivingLicenseFrontPhoto").val();
        var drivingLicenseDuplicatePhotoUrl = $("#drivingLicenseDuplicatePhoto").val();
        var vehiclephotoUrl = $("#vehiclephoto").val();
        var index = 4;
        if (transportNumberPhotoUrl != '') {
          transportNumberPhotoUrl = transportNumberPhotoUrl;
          $('#transportNumberImageUrl').attr('src', transportNumberPhotoUrl);
        } else {
          $('#transportNumberImage').hide();
          $('#transportNumberText').hide();
          $('#carInfoPhotoTable').css('width', '75%');
          index -= 1;
        }
        if (drivingLicenseFrontPhotoUrl != '') {
          drivingLicenseFrontPhotoUrl = drivingLicenseFrontPhotoUrl;
          $('#drivingLicenseFrontImageUrl').attr('src', drivingLicenseFrontPhotoUrl);
        } else {
          $('#drivingLicenseFrontImage').hide();
          $('#drivingLicenseFrontText').hide();
          $('#carInfoPhotoTable').css('width', '50%');
          index -= 1;
        }
        if (drivingLicenseDuplicatePhotoUrl != '') {
          drivingLicenseDuplicatePhotoUrl = drivingLicenseDuplicatePhotoUrl;
          $('#drivingLicenseDuplicateImageUrl').attr('src', drivingLicenseDuplicatePhotoUrl);
        } else {
          $('#drivingLicenseDuplicateImage').hide();
          $('#drivingLicenseDuplicateText').hide();
          $('#carInfoPhotoTable').css('width', '100%');
          index -= 1;
        }
        if (vehiclephotoUrl != '') {
          vehiclephotoUrl = vehiclephotoUrl;
          $("#preview").attr("src", vehiclephotoUrl);
        } else {
          $('#vehicleImage').hide();
          $('#vehicleText').hide();
          $('#carInfoPhotoTable').css('width', '100%');
          index -= 1;
        }
        $('#carInfoTableFirstTr').css('width', 100 / index + '%');
      }
    }
    $(function () {
        // var vehiclephoto = $("#vehiclephoto").val();
        // var src;
        // if (vehiclephoto == "") {
        //     src = "/clbs/resources/img/showMedia_img.png";
        //     $("#preview").attr("style", "width:0px");
        // } else {
        //     src = "/clbs/upload/" + vehiclephoto
        // }
        // $("#preview").attr("src", src);
        vehicleEdit.carInfoPhoto();

        //点击显示隐藏信息
        $(".info-span").on("click", vehicleEdit.hiddenparameterFn);

        vehicleEdit.getVehicleCategory();
        //按车辆类别对应的标准动态显示信息
        var curStandard = $("#vehicleCategory option:selected").attr("standard");
        vehicleEdit.checkCategoryStandard(curStandard);
        $('#vehicleCategory').change(function () {
            var selectStandard = $(this).find("option:selected").attr("standard");
            vehicleEdit.checkCategoryStandard(selectStandard);
        });
        //判断自重是否必填
        vehicleEdit.selfRespectRequired();
        $("#vehicleSubtypes").on("change", function () {
            vehicleEdit.selfRespectRequired();
            $("#selfRespect-error").hide();
        });
        $("#selfRespect").on("input propertychange change", function () {
            vehicleEdit.selfRespectShow();
        })

        //工作能力是否必填
        vehicleEdit.abilityWorkRequired();
        $("#abilityWork").on("input propertychange change", function () {
            vehicleEdit.abilityWorkShow();
        });
        var stateRepair=$("#stateRepair").val();
        var stateRepairVal='';
        switch (stateRepair){
            case '0':
                stateRepairVal='未维修';
                break;
             case '1':
                stateRepairVal='已维修';
                break;
        }
        $("#stateRepairVal").val(stateRepairVal);
    })
})($, window)