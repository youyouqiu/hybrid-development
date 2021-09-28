// 企业信息数据
(function (window, $) {
    var orgList = '';// 勾选车辆name
    var orgId = '';// 勾选车辆id
    var exportParam = null;
    var noInfo = $('#orgMenuContent .noInfo');// 组织树加载信息提示语
    var messageInfo = $('#orgMessage');// 请求返回提示
    var exportBtn = $('#exportOrgData');// 导出按钮

    orgInfoData = {
        /**
         * 企业树相关方法
         * */
        treeInit: function () {
            var setting = {
                async: {
                    url: '/clbs/m/basicinfo/enterprise/professionals/tree?isOrg=1',
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    dataFilter: orgInfoData.ajaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    dblClickExpand: false,
                    nameIsHTML: true,
                    countClass: "group-number-statistics"
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    beforeClick: orgInfoData.beforeClick,
                    onCheck: orgInfoData.onCheckOrg,
                    onAsyncSuccess: orgInfoData.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#orgTreeDemo"), setting, null);
        },
        ajaxDataFilter: function (treeId, parentNode, responseData) {
            var data = responseData;
            if (data.length === 0) {
                noInfo.html('未查询到匹配项').show();
            } else {
                noInfo.html('未查询到匹配项').hide();
            }
            return data || [];
        },
        beforeClick: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        zTreeOnAsyncSuccess: function (event, treeId, treeNode, msg) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true);
        },
        onCheckOrg: function (e, treeId, treeNode) {
            if (treeNode && treeNode.checked) {
                setTimeout(() => {
                    orgInfoData.getCheckedNodes();
                    orgInfoData.validates();
                }, 600);
            }
            orgInfoData.getCheckedNodes();
            orgInfoData.getCharSelect(treeId);
        },
        getCharSelect: function (treeId) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var nodes = treeObj.getCheckedNodes(true);
            if (nodes.length > 0) {
                $("#orgGroupSelect").val(nodes[0].name);
            } else {
                $("#orgGroupSelect").val("");
            }
        },
        getCheckedNodes: function () {
            var zTree = $.fn.zTree.getZTreeObj("orgTreeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "", vid = "";
            for (var i = 0, l = nodes.length; i < l; i++) {
                if (nodes[i].type == "group") {
                    v += nodes[i].name;
                    vid += nodes[i].id;
                }
            }
            orgList = v;
            orgId = vid;
        },

        /**
         * 数据查询
         * */
        orgInfoDataSearch: function () {
            orgInfoData.getCheckedNodes();
            if (!orgInfoData.validates()) {
                return;
            }
            messageInfo.html('数据请求中...');
            exportBtn.prop('disabled', true);
            var url = '/clbs/jl/vehicle/organization';
            json_ajax("get", url, "json", false, {orgId: orgId}, function (data) {
                exportParam = null;
                $('#orgDataTable td').html('--');
                if (data.success) {
                    var info = data.obj;
                    if (!info.msg) {
                        exportParam = info;
                        exportBtn.prop('disabled', false);
                        for (key in info) {
                            $('#orgInfo-' + key).html(info[key]);
                        }
                        messageInfo.html(info.sendTime + '下发');
                    } else {
                        messageInfo.html(data.obj.msg);
                    }
                } else if (data.msg) {
                    messageInfo.html(data.msg);
                }
            });
        },
        validates: function () {
            return $("#orgFormData").validate({
                rules: {
                    offOperatePlatform: {
                        required: true
                    },
                    groupSelect: {
                        zTreeCheckGroup: "orgTreeDemo"
                    }
                },
                messages: {
                    offOperatePlatform: {
                        required: '请选择平台名称'
                    },
                    groupSelect: {
                        zTreeCheckGroup: '请选择企业',
                    }
                }
            }).form();
        },
        exportOrgData: function () {
            if (!exportParam) {
                return;
            }
            var url = '/clbs/jl/vehicle/organization/export';
            exportExcelUseForm(url, exportParam);
        },
    };
    $(function () {
        orgInfoData.treeInit();
        $("#orgInfoDataSearch").bind("click", orgInfoData.orgInfoDataSearch);
        $("#orgGroupSelect").bind("click", showMenuContent);
        exportBtn.bind("click", orgInfoData.exportOrgData);// 导出

        $('input').inputClear().on('onClearEvent', function (e, data) {
            if (data.id == 'orgGroupSelect') {
                orgInfoData.treeInit();
            }
        });
        // 企业树模糊查询
        $("#orgGroupSelect").on('input propertychange', function () {
            search_ztree('orgTreeDemo', 'orgGroupSelect', 'group');
        });
    })
}(window, $))