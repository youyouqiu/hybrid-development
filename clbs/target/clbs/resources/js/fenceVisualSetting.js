//# sourceURL=fenceVisualSetting.js
(function ($, window) {
    window.visualSetting = {
        initTree:function(){
            var fenceAll = {
                async: {
                    url: "/clbs/m/regionManagement/fenceManagement/getFenceTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    dataFilter: visualSetting.FenceAjaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    // addHoverDom: fenceOperation.addHoverDom,
                    // removeHoverDom: fenceOperation.removeHoverDom,
                    dblClickExpand: false,
                    nameIsHTML: true,
                    fontCss: setFontCss_ztree
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    // onClick: fenceOperation.onClickFenceChar,
                    // onCheck: fenceOperation.onCheckFenceChar
                }
            };
            $.fn.zTree.init($("#visualSettingTree"), fenceAll, null);
        },
        FenceAjaxDataFilter: function (treeId, parentNode, responseData) {
            var ret = [];
            var userSetting = $('#userSetting').val().replace(/[\[\]]/g,'');//用户已经设置的围栏
            userSetting = userSetting.split(',').map(function(x){
                return x.trim();
            });
            if (responseData && responseData.msg){
                ret = JSON.parse(responseData.msg);
            }
            for (var i = 0; i < ret.length; i++) {
                if (ret[i].pId === "0"){
                    ret[i].pId = 'top';
                    ret[i].open = true;
                }
                if (userSetting.indexOf(ret[i].id)> -1){//勾选用户已经勾选的围栏
                    ret[i].checked = true;
                }
            }
            ret.splice(0,0,{
                id:'top',
                pId:'',
                name:'全部围栏',
                open:true
            });
            return ret;
        },
        doSubmit: function () {
            var fenceIdArray = [];
            var zTree = $.fn.zTree.getZTreeObj("visualSettingTree");
            var nodes = zTree.getCheckedNodes(true);
            for (var i = 0, len = nodes.length; i < len; i++) {
                if (nodes[i].type === 'fence'){
                    fenceIdArray.push(nodes[i].id);
                }
            }
            $('#visualSettingIds').val(fenceIdArray.join(','))
            $("#visualSettingForm").ajaxSubmit(function (data) {
                var json = eval("(" + data + ")");
                if (json.success) {
                    $("#commonSmWin").modal("hide");
                } else {
                    if(json.msg){
                        layer.msg(json.msg);
                    }
                }
            });
        },
        /*validates: function () {
            return $("#visualSettingForm").validate({
                rules: {
                    jobName: {
                        required: true,
                        isJobNameCanNull: true,
                        remote: {
                            type: "post",
                            async: false,
                            url: "/clbs/m/basicinfo/monitoring/job/checkJobName",
                            data: {
                                name: function () {
                                    return $("#jobName").val();
                                }
                            }
                        }
                    },
                    jobIconName: {
                        required: true
                    }
                },
                messages: {
                    jobName: {
                        required: nameNull,
                        isJobNameCanNull: jobNameError,
                        remote: jobNameExist
                    },
                    jobIconName: {
                        required: publicNull
                    }
                }
            }).form();
        },
        //图片上传预览功能
        setImagePreview: function (avalue) {
            visualSetting.uploadImage(); // 上传图片到服务器
            var docObj = document.getElementById("doc");
            var imgObjPreview = document.getElementById("preview");
            if (docObj.files && docObj.files[0]) {
                //火狐下，直接设img属性
                imgObjPreview.style.display = 'block';
                imgObjPreview.style.width = '200px';
                imgObjPreview.style.height = '200px';
                //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
                if (window.navigator.userAgent.indexOf("Chrome") >= 1 || window.navigator.userAgent.indexOf("Safari") >= 1) {
                    imgObjPreview.src = window.webkitURL.createObjectURL(docObj.files[0]);
                } else {
                    imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
                }
            }
            else {
                //IE下，使用滤镜
                docObj.select();
                var imgSrc = document.selection.createRange().text;
                var localImagId = document.getElementById("localImag");
                //必须设置初始大小
                localImagId.style.width = "200px";
                localImagId.style.height = "200px";
                //图片异常的捕捉，防止用户修改后缀来伪造图片
                try {
                    localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
                    localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
                }
                catch (e) {
                    layer.msg(publicPictureError);
                    return false;
                }
                imgObjPreview.style.display = 'none';
                document.selection.empty();
            }
            return true;
        },
        //上传图片
        uploadImage: function () {
            var docObj = document.getElementById("doc");
            if (docObj.files && docObj.files[0]) {
                var formData = new FormData();
                formData.append("file", docObj.files[0]);
                var d = new Date();
                var random = d.getTime()
                formData.append("file", docObj.files[0]);
                formData.append("id", random);
                $.ajax({
                    url: '/clbs/m/basicinfo/monitoring/job/upload_img',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        data = $.parseJSON(data);
                        if (data.imgName == "0") {
                            layer.msg("不支持的图片格式文件，<br/>支持格式（png，jpg，gif，jpeg）");
                            $("#preview").src("");
                        } else {
                            $("#jobIconName").val(data.imgName);
                        }
                    },
                    error: function (data) {
                        layer.msg(publicUploadingError);
                    }
                });
            }
        }*/
    };
    $(function () {
        visualSetting.initTree();
        $("#visualSettingSave").on("click", visualSetting.doSubmit);//围栏显示设置保存
    })
})($, window)