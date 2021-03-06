(function (window, $) {
    var vIcoTypeIndex = 1;
    var vIcoTypeFlag = true;
    var vIco = "";
    addVehicleCategory = {
        init: function () {
            $("#vIcoTypeNum").html("");
            var url = "/clbs/m/personalized/ico/findIco";
            json_ajax("POST", url, "json", false, null, addVehicleCategory.callBack);
        },
        callBack: function (data) {
            var length = data.length / 10;
            if (addVehicleCategory.isInteger(length)) {
                length = parseInt(length);
            } else {
                length = parseInt(length) + 1;
            }
            var str = "";
            var i = 1;
            for (var j = 1; j <= length; j++) {
                str += '<div class="vIcoTypePages" id="vIcoTypePages_' + j + '">';
                for (i; i <= data.length; i++) {
                    var k = i - 1;
                    var icoName = data[k].icoName;
                    var id = data[k].id;
                    var state = data[k].defultState;
                    if (state == 0) {
                        str += '<li class="vIcoContainer" id="vIcoContainer_' + i + '" onclick="addVehicleCategory.vIcoContainerCheckedFn(this,\'' + id + '\',\'' + icoName + '\')">';
                        str += '<div class="vico"><img src="../../../../../resources/img/vico/' + icoName + '"></div>';
                        str += '</li>';
                    } else {
                        str += '<li class="vIcoContainer" id="vIcoContainer_' + i + '" onclick="addVehicleCategory.vIcoContainerCheckedFn(this,\'' + id + '\',\'' + icoName + '\')">';
                        str += '<div class="vico"><img src="../../../../../resources/img/vico/' + icoName + '"></div>';
                        str += '<div class="vicoDel" id="vicoDel_16' + i + '" onclick="addVehicleCategory.vIcoContainerDeleteFn(this,\'' + id + '\')"><img src="../../../../../resources/img/vico/vbhDel.png"/></div>';
                        str += '</li>';
                    }
                    if (i == 10 * j) {
                        i++
                        break;
                    }
                }
                str += '</div>';
            }
            $("#vIcoTypeNum").html(str);
        },
        isInteger: function (obj) {
            return obj % 1 === 0
        },
        arrow: function () {
            $("#pro-arrow,#next-arrow").attr("disabled", "disabled");

            var vIco = $("#vIcoTypeNum").find("div.vIcoTypePages");
            for (var i = 0; i < vIco.length; i++) {
                if (vIco.length > 1) {
                    if (vIcoTypeFlag) {
                        vIco.splice(0, 1);
                        vIcoTypeFlag = false;
                    }
                    var otherId = vIco[i].id;
                    $("#" + otherId).hide();
                    $("#vIcoTypePages_1").show();
                    $("#next-arrow").removeAttr("disabled", "disabled");
                    $("#pro-arrow").attr("disabled", "disabled");
                    $("#vIcoTypeNum").css({"width": "auto"});
                } else {
                    $("#vIcoTypeNum").css({"width": "auto"});
                    $("#pro-arrow,#next-arrow").attr("disabled", "disabled");
                }
            }
        },
        uploadImageIndex: function () {
            var docObj = document.getElementById("doc");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                var dataImgType = $(docObj).val().split(".")[1];
                if (dataImgType == "png") {
                    $.ajax({
                        url: '/clbs/m/personalized/ico/upload_img',
                        type: 'POST',
                        data: formData,
                        async: false,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function (data) {
                            data = $.parseJSON(data);
                            if (data.state == "-1") {
                                layer.msg("?????????????????????????????????");
                                return;
                            } else if (data.state == "1") {
                                layer.msg("????????????????????????67x37");
                                return;
                            } else {
                                vIcoTypeIndex = 1;
                                addVehicleCategory.init();
                                addVehicleCategory.arrow();
                                $(".vIcoContainer").hover(addVehicleCategory.vIcoContainerOverFn);
                                $("#vIcoContainer_1").click();
                                //????????????????????????
                                /* $("#upIcoSize").css({
                                     "width" : data.width,
                                     "height" : data.height
                                 });*/
                            }
                        },
                        error: function (data) {
                            layer.msg("???????????????");
                        }
                    });
                }
                else{
                    layer.msg("?????????????????????????????????");
                }
            }
        },
        //????????????????????????????????????
        setImagePreviewIndex: function (avalue) {
            addVehicleCategory.uploadImageIndex(); // ????????????????????????
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");

            if (docObj.files && docObj.files[0]) {
                //?????????????????????img??????
                /*imgObjPreview.style.display = 'block';*/
                //??????7??????????????????????????????getAsDataURL()?????????????????????????????????
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    //imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    //imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                }
            }
            else {
                //IE??????????????????
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag-index");
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("?????????????????????????????????");
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            $("#doc").val("");
            return true;
        },
        getImageWidth: function (url, callback) {
            var img = new Image();
            img.src = url;
            // ???????????????????????????????????????????????????
            if (img.complete) {
                callback(img.width, img.height);
            } else {
                // ???????????????????????????
                img.onload = function () {
                    callback(img.width, img.height);
                }
            }
        },
        validates: function () {
            return $("#addCategory").validate({
                rules: {
                    vehicleCategory: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/type/repetitions",
                            data: {
                                vehicleCategory: function () {
                                    return $("#vehicleCategory").val();
                                }
                            }
                        }
                    },
                    description: {
                        maxlength: 50
                    },
                },
                messages: {
                    vehicleCategory: {
                        required: vehicleCategoryNull,
                        maxlength: publicSize20,
                        remote: vehicleCategoryExists
                    },
                    description: {
                        maxlength: publicSize50
                    },

                }
            }).form();
        },
        //????????????
        vIcoContainerOverFn: function (e) {
            //????????????id
            var vIcoContainer_id = e.currentTarget.id;
            //??????????????????
            if ($("#" + vIcoContainer_id).children("div.vicoDel").is(":hidden")) {
                $("#" + vIcoContainer_id).children("div.vicoDel").show();
            } else {
                $("#" + vIcoContainer_id).children("div.vicoDel").hide();
            }
        },
        //??????????????????
        vIcoContainerDeleteFn: function (thisb, thisNum) {
            //??????????????????
            var e = arguments.callee.caller.arguments[0] || window.event;
            e.stopPropagation();
            var icoId = thisNum;
            var url = "/clbs/m/personalized/ico/delIco";
            var data = {"id": icoId};
            layer.confirm('????????????', {
                title: '????????????',
                icon: 3, // ????????????
                btn: ['??????', '??????'] //??????
            }, function () {
                json_ajax("POST", url, "json", false, data, addVehicleCategory.delCallBack);
            });
        },
        delCallBack: function (data) {
            if (data == true) {
                vIcoTypeIndex = 1;
                layer.closeAll('dialog');
                addVehicleCategory.init();
                addVehicleCategory.arrow();
                $(".vIcoContainer").hover(addVehicleCategory.vIcoContainerOverFn);
                $("#ico").val("");
                $("#icoName").val("");
                $("#thisVIcoInfo").hide();
            } else {
                layer.msg(categoryIcoBind);
            }
        },
        //????????????
        vIcoContainerCheckedFn: function (e, id, icoName) {
            var vIcoChecked_id = e.id;
            $("#vIcoTypeNum").find("li").removeClass("vIcoCChecked");
            $("#" + vIcoChecked_id).addClass("vIcoCChecked");
            $("#ico").val(id);
            $("#icoName").val(icoName);
            var imgType = icoName.split(".")[1];
            if (imgType == "png") {
                $("#preview").attr("src", "../../../../../resources/img/vico/" + icoName + "");
                $("#thisVIcoInfo").show();
            }
            else {
                layer.msg("???????????????????????????????????????????????????!");
            }
        },
        nextArrowFn: function () {
            var vIcolength = $("#vIcoTypeNum").find("div.vIcoTypePages").length;
            vIcoTypeIndex++;
            var vIcoIndex = vIcoTypeIndex - 1;
            $("#vIcoTypePages_" + vIcoIndex).hide();
            $("#vIcoTypePages_" + vIcoTypeIndex).show();
            if (vIcoTypeIndex == vIcolength) {
                $("#next-arrow").attr("disabled", "disabled");
            }
            if (vIcoTypeIndex > vIcoIndex) {
                $("#pro-arrow").removeAttr("disabled", "disabled");
            }
        },
        proArrowFn: function () {
            vIcoTypeIndex--;
            var vIcoIndex = vIcoTypeIndex + 1;
            $("#vIcoTypePages_" + vIcoIndex).hide();
            $("#vIcoTypePages_" + vIcoTypeIndex).show();
            $("#next-arrow").removeAttr("disabled", "disabled");
            if (vIcoTypeIndex == 1) {
                $("#pro-arrow").attr("disabled", "disabled");
                $("#next-arrow").removeAttr("disabled", "disabled");
            }
        },
        doSubmits: function () {
            var icoName = $("#icoName").val();
            if (addVehicleCategory.validates()) {
                if (icoName == "") {
                    layer.msg(categoryIco)
                    return false;
                }
                ;
                addHashCode1($("#addCategory"));
                $("#addCategory").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        $("#commonWin").modal("hide");
                        vehicleModelsManagement.category();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        $('input').inputClear();
        addVehicleCategory.init();
        addVehicleCategory.arrow();
        $("#addSubmit").bind("click", addVehicleCategory.doSubmits);
        $(".vIcoContainer").hover(addVehicleCategory.vIcoContainerOverFn);
        $("#next-arrow").on("click", addVehicleCategory.nextArrowFn);
        $("#pro-arrow").on("click", addVehicleCategory.proArrowFn);
        $("#addCategory").keydown(function (event) {
            switch (event.keyCode) {
                case 13:
                    return false;
            }
        });
    })
})(window, $)