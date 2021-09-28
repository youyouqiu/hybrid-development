(function (window, $) {
    $(function () {
        var categoryTreeInput = $("#categoryTreeInput"); //类别列表树搜索
        var categoryTreeIsSearch = true, searchTreeHidden = [];
        var plateformListData = []; //平台名称列表
        var offOperatePlatform = $(".offOperatePlatform"); // 平台名称下拉框
        var currentSelectName = '停运车辆数据';// 当前选中项名字

        //左边类别列表
        var categoryList = {
            treeId: "categoryTree",
            treeData: [
                {id: 1, pId: 0, name: "上报数据", open: true},
                {id: 11, pId: 1, name: "停运车辆数据", element: '#railwayVehicle'},
                {id: 12, pId: 1, name: "违规车辆数据", element: '#violatVehicle'},
                {id: 13, pId: 1, name: "报警车辆数据", element: '#policeVehicle'},

                {id: 2, pId: 0, name: "请求数据", open: true},
                {id: 21, pId: 2, name: "车辆信息数据", element: '#vehicleInfoDataBox'},
                {id: 22, pId: 2, name: "企业信息数据", element: '#orgInfoDataBox'},
                {id: 23, pId: 2, name: "车辆运营状态数据", element: '#vehicleOperationStateBox'},
                {id: 24, pId: 2, name: "平台考核数据", element: '#platformAssessDataBox'},
                {id: 25, pId: 2, name: "企业考核数据", element: '#orgAssessDataBox'},
                {id: 26, pId: 2, name: "企业车辆违规考核数据", element: '#orgVehicleAssessDataBox'},
            ],
            treeInit: function () {
                var treeSetting = {
                    view: {
                        showIcon: false,
                        selectedMulti: false,
                        nameIsHTML: true,
                        fontCss: setFontCss_ztree
                    },
                    data: {
                        simpleData: {
                            enable: true
                        }
                    },
                    callback: {
                        beforeClick: categoryList.beforeClick,
                        onClick: categoryList.zTreeOnClick,
                        onNodeCreated: categoryList.zTreeOnNodeCreated,
                    }
                };
                $.fn.zTree.init($("#" + categoryList.treeId), treeSetting, categoryList.treeData);
            },
            beforeClick: function (treeId, treeNode) {
                if (treeNode.pId === null) {// 禁止选中父节点
                    return false;
                }
                return true;
            },
            //点击节点
            zTreeOnClick: function (event, treeId, treeNode) {
                if (treeNode.element && $(treeNode.element).is(':hidden')) {
                    $(treeNode.element).show().siblings().hide();
                }
                currentSelectName = treeNode.name;
            },
            zTreeOnNodeCreated: function (event, treeId, treeNode) {
                var name = treeNode.name;
                if (name === currentSelectName) {
                    var treeObj = $.fn.zTree.getZTreeObj(treeId);
                    treeObj.selectNode(treeNode, true, true);
                }
            },
            // 树结构模糊搜索
            searchTree: function (txtObj) {
                $('#treeNoInfo').hide();
                if (txtObj) {
                    var zTreeObj = $.fn.zTree.getZTreeObj(categoryList.treeId);
                    //显示上次搜索后背隐藏的结点
                    if (searchTreeHidden && searchTreeHidden.length) zTreeObj.showNodes(searchTreeHidden);
                    //查找不符合条件的叶子节点
                    var parentArr = [];

                    function filterFunc(node) {
                        var _keywords = txtObj;
                        if (node.isParent) {
                            parentArr.push(node);
                            return false
                        }
                        return node.name.indexOf(_keywords.toUpperCase()) === -1;
                    }

                    //获取不符合条件的叶子结点
                    searchTreeHidden = zTreeObj.getNodesByFilter(filterFunc);
                    var hideLen = searchTreeHidden.length;
                    if (hideLen === 9) {
                        searchTreeHidden = searchTreeHidden.concat(parentArr);
                        $('#treeNoInfo').show();
                    } else {
                        var num = 0, num1 = 0;
                        for (var i = 0; i < hideLen; i++) {
                            if (searchTreeHidden[i].pId === 1) {
                                num++;
                            }
                            if (searchTreeHidden[i].pId === 2) {
                                num1++
                            }
                        }
                        if (num === 3) {
                            searchTreeHidden.push(parentArr[0]);
                        }
                        if (num1 === 6) {
                            searchTreeHidden.push(parentArr[1]);
                        }
                    }

                    //隐藏不符合条件的叶子结点
                    zTreeObj.hideNodes(searchTreeHidden);

                    // 搜索后保留之前的勾选状态
                    if (currentSelectName.indexOf(txtObj.toUpperCase()) !== -1) {
                        var nodes = zTreeObj.getNodesByParam("name", currentSelectName, null);
                        zTreeObj.selectNode(nodes[0]);
                    }
                } else {
                    categoryList.treeInit();
                }
            },
        }
        //平台名称列表
        var plateformList = {
            //获取平台名称下拉列表
            getPlateformList: function () {
                var url = '/clbs/jl/vehicle/stopped/plateform';
                json_ajax("GET", url, "json", false, {}, function (res) {
                    if (res.success) {
                        plateformListData = res.obj;
                        var html = '';
                        if (plateformListData.length === 0) {
                            html = '<option value="">暂无数据</option>';
                        }
                        for (var i = 0, len = plateformListData.length; i < len; i++) {
                            html += '<option value="' + plateformListData[i].id + '">' + plateformListData[i].name + '</option>'
                        }
                        offOperatePlatform.html(html);
                    }
                });
            }
        }
        categoryList.treeInit();
        plateformList.getPlateformList();
        $('input').inputClear();
        //监听组织树输入框groupInput
        $("input[name=groupSelect]").inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'categoryTreeInput') {  //类别列表树搜索
                var treeObj = $.fn.zTree.getZTreeObj(categoryList.treeId);
                categoryList.searchTree('');
            }
            ;
        });
        //类别列表树搜索
        var categoryTreeTimer;
        categoryTreeInput.on('input propertychange', function (value) {
            if (categoryTreeTimer !== undefined) {
                clearTimeout(categoryTreeTimer);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                categoryTreeIsSearch = true;
            }
            categoryTreeTimer = setTimeout(function () {
                if (categoryTreeIsSearch) {
                    var param = categoryTreeInput.val();
                    categoryList.searchTree(param);
                }
                categoryTreeIsSearch = true;
            }, 500);
        });
    })
}(window, $))
