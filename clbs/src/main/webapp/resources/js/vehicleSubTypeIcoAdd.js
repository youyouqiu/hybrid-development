(function (window, $) {
    var vIcoTypeIndex = 1;
    var vIcoTypeFlag = true;
    var vIco = "";
    addVehicleSubType = {
        init: function () {
            $("#vIcoTypeNum").html("");
            var url = "/clbs/m/personalized/ico/findIco";
            json_ajax("POST", url, "json", false, null, addVehicleSubType.callBack);
        },
        callBack: function (data) {
            var length = data.length / 10;
            if (addVehicleSubType.isInteger(length)) {
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
                        str += '<li class="vIcoContainer" id="vIcoContainer_' + i + '" onclick="addVehicleSubType.vIcoContainerCheckedFn(this,\'' + id + '\',\'' + icoName + '\')">';
                        str += '<div class="vico"><img src="../../../../../resources/img/vico/' + icoName + '"></div>';
                        str += '</li>';
                    } else {
                        str += '<li class="vIcoContainer" id="vIcoContainer_' + i + '" onclick="addVehicleSubType.vIcoContainerCheckedFn(this,\'' + id + '\',\'' + icoName + '\')">';
                        str += '<div class="vico"><img src="../../../../../resources/img/vico/' + icoName + '"></div>';
                        str += '<div class="vicoDel" id="vicoDel_16' + i + '" onclick="addVehicleSubType.vIcoContainerDeleteFn(this,\'' + id + '\')"><img src="../../../../../resources/img/vico/vbhDel.png"/></div>';
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
        getVehicleType: function () {
            var url = "/clbs/m/basicinfo/monitoring/vehicle/type/vehicleTypes";
            var data = {"vehicleType": ""}
            json_ajax("POST", url, "json", false, data, addVehicleSubType.vehicleTypeCallBack);
        },
        vehicleTypeCallBack: function (data) {
            var result = data.obj;
            var str = "";
            for (var i = 0; i < result.length; i++) {
                str += '<option  value="' + result[i].id + '">' + result[i].vehicleType + '</option>'
            }
            $("#vehicleType").html(str);
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
                                layer.msg("不支持的图片格式文件！");
                                return;
                            } else if (data.state == "1") {
                                layer.msg("图片大小不要超过67x37");
                                return;
                            } else {
                                vIcoTypeIndex = 1;
                                addVehicleSubType.init();
                                addVehicleSubType.arrow();
                                $(".vIcoContainer").hover(addVehicleSubType.vIcoContainerOverFn);
                                $("#vIcoContainer_1").click();
                                //图片外层赋值尺寸
                                /* $("#upIcoSize").css({
                                     "width" : data.width,
                                     "height" : data.height
                                 });*/
                            }
                        },
                        error: function (data) {
                            layer.msg("上传失败！");
                        }
                    });
                }
                else {
                    layer.msg("不支持的图片格式文件！");
                }
            }
        },
        //下面用于图片上传预览功能
        setImagePreviewIndex: function (avalue) {
            addVehicleSubType.uploadImageIndex(); // 上传图片到服务器
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");

            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                /*imgObjPreview.style.display = 'block';*/
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    //imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    //imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                }
            }
            else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag-index");
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    alert("不支持的图片格式文件！");
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
            // 如果图片被缓存，则直接返回缓存数据
            if (img.complete) {
                callback(img.width, img.height);
            } else {
                // 完全加载完毕的事件
                img.onload = function () {
                    callback(img.width, img.height);
                }
            }
        },
        validates: function () {
            return $("#addSubType").validate({
                rules: {
                    pid:{
                        required: true
                    },
                    vehicleSubtypes: {
                        required: true,
                        maxlength: 20,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/vehicle/type/checkSubTypeRepeat",
                            data: {
                                vehicleType: function () {
                                    return $("#vehicleType option:selected").html();
                                },
                                vehicleSubtypes: function () {
                                    return $("#vehicleSubtypes").val();
                                }
                            }
                        }
                    },
                    description: {
                        maxlength: 50
                    },
                },
                messages: {
                    pid:{
                        required: '请选择车辆类型'
                    },
                    vehicleSubtypes: {
                        required: '请输入车辆子类型',
                        maxlength: '输入范围1-20位',
                        remote: '该车辆子类型已存在'
                    },
                    description: {
                        maxlength: publicSize50
                    },

                }
            }).form();
        },
        //鼠标悬浮
        vIcoContainerOverFn: function (e) {
            //鼠标悬浮id
            var vIcoContainer_id = e.currentTarget.id;
            //判断是否显示
            if ($("#" + vIcoContainer_id).children("div.vicoDel").is(":hidden")) {
                $("#" + vIcoContainer_id).children("div.vicoDel").show();
            } else {
                $("#" + vIcoContainer_id).children("div.vicoDel").hide();
            }
        },
        //悬浮删除点击
        vIcoContainerDeleteFn: function (thisb, thisNum) {
            //阻止事件冒泡
            var e = arguments.callee.caller.arguments[0] || window.event;
            e.stopPropagation();
            var icoId = thisNum;
            var url = "/clbs/m/personalized/ico/delIco";
            var data = {"id": icoId};
            layer.confirm('是否删除', {
                title: '操作确认',
                icon: 3, // 问号图标
                btn: ['确定', '取消'] //按钮
            }, function () {
                json_ajax("POST", url, "json", false, data, addVehicleSubType.delCallBack);
            });
        },
        delCallBack: function (data) {
            if (data == true) {
                vIcoTypeIndex = 1;
                layer.closeAll('dialog');
                addVehicleSubType.init();
                addVehicleSubType.arrow();
                $(".vIcoContainer").hover(addVehicleSubType.vIcoContainerOverFn);
                $("#ico").val("");
                $("#icoName").val("");
                $("#thisVIcoInfo").hide();
            } else {
                layer.msg(categoryIcoBind);
            }
        },
        //点击选中
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
                layer.msg("不支持的图片格式文件！");
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
            if (addVehicleSubType.validates()) {
                /*if (icoName == "") {
                    layer.msg(categoryIco);
                    return false;
                }*/
                addHashCode1($("#addSubType"));
                $("#addSubType").ajaxSubmit(function (data) {
                    var json = eval("(" + data + ")");
                    if (json.success) {
                        layer.msg('添加成功！');
                        $("#commonWin").modal("hide");
                        subTable.requestData();
                    } else {
                        layer.msg(json.msg);
                    }
                });
            }
        },
    }
    $(function () {
        $('input').inputClear();
        addVehicleSubType.init();
        addVehicleSubType.getVehicleType();
        addVehicleSubType.arrow();
        $("#addSubmit").bind("click", addVehicleSubType.doSubmits);
        $(".vIcoContainer").hover(addVehicleSubType.vIcoContainerOverFn);
        $("#next-arrow").on("click", addVehicleSubType.nextArrowFn);
        $("#pro-arrow").on("click", addVehicleSubType.proArrowFn);
        $("#addSubType").keydown(function (event) {
            switch (event.keyCode) {
                case 13:
                    return false;
            }
        });
    })
})(window, $)