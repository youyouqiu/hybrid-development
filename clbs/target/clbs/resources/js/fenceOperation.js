var vehicleFenceList = "";
var oldFencevehicleIds;
var followId = null; // 当前跟踪的监控对象的ID
var followLocation = null; // 当前跟踪的监控对象的经纬度数组
var followPath = null; // 当前跟踪的监控对象的路径对象

var fenceOperation = {
    // 初始化
    init: function () {
        // 围栏树
        var fenceAll = {
            async: {
                url: "/clbs/m/functionconfig/fence/bindfence/fenceTree",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {"type": "multiple"},
                dataFilter: fenceOperation.FenceAjaxDataFilter
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
                addHoverDom: fenceOperation.addHoverDom,
                removeHoverDom: fenceOperation.removeHoverDom,
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
                onClick: fenceOperation.onClickFenceChar,
                onCheck: fenceOperation.onCheckFenceChar
            }
        };
        $.fn.zTree.init($("#fenceDemo"), fenceAll, null);
        //IE9（模糊查询）
        if (navigator.appName === "Microsoft Internet Explorer" && navigator.appVersion.split(";")[1].replace(/[ ]/g, "") === "MSIE9.0") {
            var search;
            $("#searchFence").bind("focus", function () {
                search = setInterval(function () {
                    search_ztree('fenceDemo', 'searchFence', 'fence');
                }, 500);
            }).bind("blur", function () {
                clearInterval(search);
            });
        }
        // 时分秒选择器
        var hmsTime = '<div id="hmsTime" style="text-align:center;background-color:#ffffff;border:1px solid #cccccc;width:200px; display:none;"><div id="hourseSelect"><div style="text-align:center;width:200px;background-color:#6dcff6;color:#ffffff;">时</div><table style="width:200px;border-top:1px solid #cccccc; color:#6dcff6;"><thead></thead><tbody><tr><td>01</td><td>02</td><td>03</td><td>04</td></tr><tr><td>05</td><td>06</td><td>07</td><td>08</td></tr><tr><td>09</td><td>10</td><td>11</td><td>12</td></tr><tr><td>13</td><td>14</td><td>15</td><td>16</td></tr><tr><td>17</td><td>18</td><td>19</td><td>20</td></tr><tr><td>21</td><td>22</td><td>23</td><td>00</td></tr></tbody></table></div><div id="minuteSelect" style="display:none;"><div style="text-align:center;width:200px;background-color:#6dcff6;color:#ffffff;">分</div><table style="width:200px;border-top:1px solid #cccccc; color:#6dcff6"><thead></thead><tbody><tr><td>01</td><td>02</td><td>03</td><td>04</td><td>05</td><td>06</td></tr><tr><td>07</td><td>08</td><td>09</td><td>10</td><td>11</td><td>12</td></tr><tr><td>13</td><td>14</td><td>15</td><td>16</td><td>17</td><td>18</td></tr><tr><td>19</td><td>20</td><td>21</td><td>22</td><td>23</td><td>24</td></tr><tr><td>25</td><td>26</td><td>27</td><td>28</td><td>29</td><td>30</td></tr><tr><td>31</td><td>32</td><td>33</td><td>34</td><td>35</td><td>36</td></tr><tr><td>37</td><td>38</td><td>39</td><td>40</td><td>41</td><td>42</td></tr><tr><td>43</td><td>44</td><td>45</td><td>46</td><td>47</td><td>48</td></tr><tr><td>49</td><td>50</td><td>51</td><td>52</td><td>53</td><td>54</td></tr><tr><td>55</td><td>56</td><td>57</td><td>58</td><td>59</td><td>00</td></tr></tbody></table></div><div id="secondSelect" style="display:none;"><div style="text-align:center;width:200px;background-color:#6dcff6;color:#ffffff;">秒</div><table style="width:200px;border-top:1px solid #cccccc;color:#6dcff6;"><thead></thead><tbody><tr><td>01</td><td>02</td><td>03</td><td>04</td><td>05</td><td>06</td></tr><tr><td>07</td><td>08</td><td>09</td><td>10</td><td>11</td><td>12</td></tr><tr><td>13</td><td>14</td><td>15</td><td>16</td><td>17</td><td>18</td></tr><tr><td>19</td><td>20</td><td>21</td><td>22</td><td>23</td><td>24</td></tr><tr><td>25</td><td>26</td><td>27</td><td>28</td><td>29</td><td>30</td></tr><tr><td>31</td><td>32</td><td>33</td><td>34</td><td>35</td><td>36</td></tr><tr><td>37</td><td>38</td><td>39</td><td>40</td><td>41</td><td>42</td></tr><tr><td>43</td><td>44</td><td>45</td><td>46</td><td>47</td><td>48</td></tr><tr><td>49</td><td>50</td><td>51</td><td>52</td><td>53</td><td>54</td></tr><tr><td>55</td><td>56</td><td>57</td><td>58</td><td>59</td><td>00</td></tr></tbody></table></div></div>';
        $("body").append(hmsTime);
        $("#hmsTime tr td").on("mouseover", function () {
            $(this).css({
                "background-color": "#6dcff6",
                "color": "#ffffff"
            })
        }).on("mouseout", function () {
            $(this).css({
                "background-color": "#ffffff",
                "color": "#6dcff6"
            })
        });
        // datatable列表显示隐藏列
        var table = $("#dataTableBind tr th:gt(1)");
        var menu_text = '';
        menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(2) + "\" disabled />" + table[0].innerHTML + "</label></li>";
        for (var i = 1; i < table.length; i++) {
            menu_text += "<li><label><input type=\"checkbox\" checked=\"checked\" class=\"toggle-vis\" data-column=\"" + parseInt(i + 2) + "\" />" + table[i].innerHTML + "</label></li>"
        }
        $("#Ul-menu-text-bind").html(menu_text);
        laydate.render({elem: '#arriveTime', type: 'datetime', theme: '#6dcff6'});
        laydate.render({elem: '#leaveTime', type: 'datetime', theme: '#6dcff6'});
        laydate.render({elem: '#msStartTime', type: 'datetime', theme: '#6dcff6'});
        laydate.render({elem: '#msEndTime', type: 'datetime', theme: '#6dcff6'});
        laydate.render({elem: '#muStartTime', type: 'datetime', theme: '#6dcff6'});
        laydate.render({elem: '#muEndTime', type: 'datetime', theme: '#6dcff6'});
    },
    // 围栏绑定列表
    fenceBindList: function () {
        //表格列定义
        var columnDefs = [{
            //第一列，用来显示序号
            "searchable": false,
            "orderable": false,
            "targets": 0
        }];
        var columns = [
            {
                //第一列，用来显示序号
                "data": null,
                "class": "text-center"
            },
            {
                "data": null,
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var result = '';
                    var obj = {};
                    obj.fenceConfigId = row.id;
                    obj.paramId = row.paramId;
                    obj.vehicleId = row.vehicle_id;
                    obj.fenceId = row.fence_id;
                    var jsonStr = JSON.stringify(obj);
                    result += "<input  type='checkbox' name='subChk'  value='" + jsonStr + "' />";
                    return result;
                }
            },
            {
                "data": null,
                "class": "text-center", //最后一列，操作按钮
                render: function (data, type, row, meta) {
                    var editUrlPath = myTable.editUrl + ".gsp?id=" + row.id + "&vehicleId=" + row.vehicle_id + "&name=" + encodeURI(row.name) + "&type=" + row.type + "&alarmSource=" + row.alarm_source; //修改地址
                    var result = '';
                    //修改按钮
                    result += '<button href="' + editUrlPath + '" data-target="#commonWin" data-toggle="modal"  type="button" class="editBtn editBtn-info"><i class="fa fa-pencil"></i>修改</button>&ensp;';
                    // 围栏下发按钮
                    var protocolTypeArr = ['0', '1', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28'];
                    if (row.type === 'zw_m_marker' || row.alarm_source === 1 || (protocolTypeArr.indexOf(row.deviceType) === -1)) {
                        result += ' <button disabled onclick="fenceOperation.sendFenceOne(\'' + row.id + '\',\'' + row.paramId + '\',\'' + row.vehicle_id + '\',\'' + row.fence_id + '\')" class="editBtn btn-default" type="button"><i class="glyphicon glyphicon-circle-arrow-down"></i>围栏下发</button>&ensp;'
                    } else {
                        result += ' <button onclick="fenceOperation.sendFenceOne(\'' + row.id + '\',\'' + row.paramId + '\',\'' + row.vehicle_id + '\',\'' + row.fence_id + '\')" class="editBtn editBtn-info" type="button"><i class="glyphicon glyphicon-circle-arrow-down"></i>围栏下发</button>&ensp;'
                    }
                    //删除按钮
                    result += '<button type="button" onclick="myTable.deleteItem(\''
                        + row.id
                        + '\')" class="deleteButton editBtn disableClick"><i class="fa fa-trash-o"></i>解除绑定</button>';
                    return result;
                }
            }, {
                "data": "type",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return fenceOperation.fencetypepid(data);
                }
            }, {
                "data": "name",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    var fenceId = '<a onclick="fenceOperation.tableFence(\'' + row.fenceId + '\')">' + html2Escape(data) + '</a>';
                    return fenceId;
                }
            }, {
                "data": "send_down_id",
                "class": "text-center"
            }, {
                "data": "brand",
                "class": "text-center"
            }, {
                "data": "dirStatus",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == "0") {
                        return '参数已生效';
                    } else if (data == "1") {
                        return '参数未生效';
                    } else if (data == "2") {
                        return "参数消息有误";
                    } else if (data == "3") {
                        return "参数不支持";
                    } else if (data == "4") {
                        return "参数下发中";
                    } else if (data == "5") {
                        return "终端离线，未下发";
                    } else if (data == "7") {
                        return "终端处理中";
                    } else if (data == "8") {
                        return "终端接收失败";
                    }
                    return "";

                }
            }, {
                "data": "send_fence_type",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 0) {
                        return '更新';
                    } else if (data === 1) {
                        return '追加';
                    } else if (data === 2) {
                        return "修改";
                    }

                }
            }, {
                "data": "alarm_source",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data == '0') {
                        return '终端报警';
                    } else if (data == '1') {
                        return '平台报警';
                    } else if (data == '2') {
                        return '平台与终端';
                    }
                }
            }, {
                "data": "alarm_start_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data != null && data !== "") {
                        var dateStr = data.substring(0, 10);
                        return dateStr;
                    }
                    return "";

                }
            }, {
                "data": "alarm_end_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data != null && data !== "") {
                        var dateStr = data.substring(0, 10);
                        return dateStr;
                    }
                    return "";

                }
            }, {
                "data": "alarm_start_date",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data != null && data !== "") {
                        var dateStr = data.substring(10);
                        return dateStr;
                    }
                    return "";

                }
            }, {
                "data": "alarm_end_date",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data != null && data !== "") {
                        var dateStr = data.substring(10);
                        return dateStr;
                    }
                    return "";

                }
            }, {
                "data": "alarm_in_platform",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data === 1) {
                        return 'V';
                    } else if (data === 0) {
                        return 'X';
                    }
                    return "";

                }
            }, {
                "data": "alarm_out_platform",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (data === 1) {
                        return 'V';
                    } else if (data === 0) {
                        return 'X';
                    }
                    return "";

                }
            }, {
                "data": "alarm_in_driver",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 1) {
                        return 'V';
                    } else if (data === 0) {
                        return 'X';
                    }
                    return "";


                }
            }, {
                "data": "alarm_out_driver",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 1) {
                        return 'V';
                    } else if (data === 0) {
                        return 'X';
                    }
                    return "";


                }
            }, {
                "data": "speed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return data;
                }
            }, {
                "data": "night_max_speed",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return data ? data : '';
                }
            }, {
                "data": "night_limit_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    return data ? data : '';
                }
            }, {
                "data": "over_speed_last_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    return data;

                }
            }, {
                "data": "travel_long_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    return data;

                }
            }, {
                "data": "travel_small_time",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    return data;

                }
            }, {
                "data": "open_door",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 0) {
                        return 'V';
                    } else if (data === 1) {
                        return 'X';
                    }
                    return "";


                }
            }, {
                "data": "communication_flag",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 0) {
                        return 'V';
                    } else if (data === 1) {
                        return 'X';
                    }
                    return "";


                }
            }, {
                "data": "gnss_flag",
                "class": "text-center",
                render: function (data, type, row, meta) {
                    if (row.alarm_source === 1) {
                        return "";
                    }
                    if (data === 0) {
                        return 'V';
                    } else if (data === 1) {
                        return 'X';
                    }
                    return "";


                }
            }

        ];
        //ajax参数
        var ajaxDataParamFun = function (d) {
            d.simpleQueryParam = $('#simpleQueryParam').val(); //模糊查询
            d.queryFenceIdStr = queryFenceId.unique3().join(",");
        };
        // 表格setting
        var bindSetting = {
            listUrl: "/clbs/m/functionconfig/fence/bindfence/list",
            editUrl: "/clbs/m/functionconfig/fence/bindfence/editById",
            deleteUrl: "/clbs/m/functionconfig/fence/bindfence/delete_",
            deletemoreUrl: "/clbs/m/functionconfig/fence/bindfence/deletemore",
            enableUrl: "/clbs/c/user/enable_",
            disableUrl: "clbs/c/user/disable_",
            columnDefs: columnDefs, //表格列定义
            columns: columns, //表格列
            dataTableDiv: 'dataTableBind', //表格
            ajaxDataParamFun: ajaxDataParamFun, //ajax参数
            pageable: true, //是否分页
            showIndexColumn: true, //是否显示第一列的索引列
            enabledChange: true,
            pageNumber: 4,
            setPageNumber: false
        };
        // 创建表格
        myTable = new TG_Tabel.createNew(bindSetting);
        // 表格初始化
        myTable.init();
    },
    // 围栏绑定checked操作
    tableCheckAll: function () {
        $('input[name="subChk"]').prop("checked", this.checked);
    },
    // 电子围栏查询
    searchFenceCarSearch: function () {
        search_ztree('fenceDemo', 'searchFence', 'fence');
    },
    // 围栏隐藏
    fenceHidden: function (nodesId) {
        if (lineSpotMap.containsKey(nodesId)) {
            var thisStopArray = lineSpotMap.get(nodesId);
            if (!vinfoWindwosClickVid) {
                infoWindow.close();
            }
            for (var i = 0; i < thisStopArray.length; i++) {
                thisStopArray[i].hide();
            }
        }
        if (fenceIDMap.containsKey(nodesId)) {
            var thisFence = fenceIDMap.get(nodesId);
            if (PolyEditorMap !== undefined) {
                var obj = PolyEditorMap.get(nodesId);
                if (obj !== undefined) {
                    if (Array.isArray(obj)) {
                        for (var j = 0; j < obj.length; j++) {
                            obj[j].close();
                        }
                    } else {
                        obj.close();
                    }
                }
            }
            if (Array.isArray(thisFence)) {
                for (i = 0; i < thisFence.length; i++) {
                    thisFence[i].hide();
                }
            } else {
                thisFence.hide();
            }
        }
        fenceOperation.hideFence(nodesId);
    },
    // 围栏显示
    fenceShow: function (nodesId, node) {
        if (fenceIDMap.containsKey(nodesId)) {
            var thisFence = fenceIDMap.get(nodesId);
            if (thisFence !== undefined) {
                if (Array.isArray(thisFence)) {
                    for (var s = 0; s < thisFence.length; s++) {
                        thisFence[s].show();
                    }
                    map.setFitView(thisFence);
                } else {
                    thisFence.show();
                    map.setFitView(thisFence);
                }
            }
        } else {
            fenceOperation.getFenceDetail([node], map);
        }
        if (lineSpotMap.containsKey(nodesId)) {
            var thisStopArray = lineSpotMap.get(nodesId);
            for (var y = 0; y < thisStopArray.length; y++) {
                thisStopArray[y].show();
            }
        }
    },
    // 分段点显示与否
    sectionPointState: function (nodesId, flag) {
        if (sectionPointMarkerMap.containsKey(nodesId)) {
            var thisPointMarker = sectionPointMarkerMap.get(nodesId);
            for (var i = 0; i < thisPointMarker.length; i++) {
                if (flag) {
                    thisPointMarker[i].show();
                } else {
                    thisPointMarker[i].hide();
                }
            }
        }
    },
    // 电子围栏点击事件
    onClickFenceChar: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        if (treeNode.pId !== 'zw_m_travel_layer') {
            isAddDragRoute = false;
            isEdit = true;
            var nodes = zTree.getSelectedNodes(true);
            if (treeNode.type === 'fenceParent') {
                zTree.cancelSelectedNode(nodes[0]);
                return false;
            }
            $("#charMap").attr("class", "fa fa-chevron-down mapBind");
            $("#charMapArea").css("display", "block");
            var nodesId = treeNode.id;
            if (clickStateChar === undefined) {
                clickStateChar = nodesId;
                zTree.checkNode(nodes[0], true, true);
                nodes[0].checkedOld = true;
                fenceOperation.fenceHidden(nodesId);
                fenceOperation.getFenceDetail(nodes, map);
            } else {
                if (nodesId === clickStateChar) {
                    if (charFlag) {
                        zTree.checkNode(nodes[0], false, true);
                        nodes[0].checkedOld = false;
                        zTree.cancelSelectedNode(nodes[0]);
                        var checkNodes = zTree.getCheckedNodes(true);
                        fenceCheckLength = checkNodes.length;
                        fenceOperation.fenceHidden(nodesId);
                        fenceOperation.sectionPointState(nodesId, false);
                        charFlag = false;
                    } else {
                        charFlag = true;
                        clickStateChar = nodesId;
                        zTree.checkNode(nodes[0], true, true);
                        nodes[0].checkedOld = true;
                        fenceOperation.fenceHidden(nodesId);
                        fenceOperation.getFenceDetail(nodes, map);
                        fenceOperation.sectionPointState(nodesId, true);
                    }
                } else {
                    charFlag = true;
                    clickStateChar = nodesId;
                    zTree.checkNode(nodes[0], true, true);
                    nodes[0].checkedOld = true;
                    fenceOperation.fenceHidden(nodesId);
                    fenceOperation.getFenceDetail(nodes, map);
                    fenceOperation.sectionPointState(nodesId, true);
                }
            }
            // 通过所选择的围栏节点筛选绑定列表
            fenceOperation.getcheckFenceNode(zTree);
            myTable.filter();
            myTable.requestData();
        } else {
            if (treeNode.checked) {
                if (treeNode.name === fenceLayerSelect) {
                    zTree.checkNode(treeNode, false, true);
                    zTree.cancelSelectedNode(treeNode);
                    amapFunCollection.amapFenceFun(treeNode, false);
                    fenceLayerSelect = null;
                } else {
                    zTree.cancelSelectedNode();
                    zTree.selectNode(treeNode);
                    zTree.checkNode(treeNode, true, true);
                    amapFunCollection.amapFenceFun(treeNode, true);
                    fenceLayerSelect = treeNode.name;
                }
            } else {
                zTree.selectNode(treeNode);
                zTree.checkNode(treeNode, true, true);
                amapFunCollection.amapFenceFun(treeNode, true);
                fenceLayerSelect = treeNode.name;
            }
        }
    },
    getcheckFenceNode: function (zTree) {
        var checkFences = zTree.getCheckedNodes(true);
        queryFenceId = [];
        if (checkFences != null && checkFences.length > 0) {
            for (var i = 0; i < checkFences.length; i++) {
                if (checkFences[i].isParent === false) {
                    queryFenceId.push(checkFences[i].id);
                }
            }
        }
    },
    // 电子围栏勾选事件
    onCheckFenceChar: function (e, treeId, treeNode) {
        if (treeNode.pId === 'zw_m_travel_layer' || treeNode.id === 'zw_m_travel_layer') {
            if (treeNode.checked) {
                amapFunCollection.amapFenceFun(treeNode, true);
            } else {
                amapFunCollection.amapFenceFun(treeNode, false);
            }
            return false;
        }
        isAddDragRoute = false;
        isEdit = true;
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getCheckedNodes(true);
        var nodeLength = nodes.length;
        // 通过所选择的围栏节点筛选绑定列表
        fenceOperation.getcheckFenceNode(zTree);
        myTable.requestData();
        /*myTable.filter();*/
        if (nodeLength > fenceCheckLength) {
            fenceCheckLength = nodeLength;
            var changeNodes = zTree.getChangeCheckedNodes();
            for (var i = 0, len = changeNodes.length; i < len; i++) {
                changeNodes[i].checkedOld = true;
            }
            for (var j = 0; j < changeNodes.length; j++) {
                var nodesId = changeNodes[j].id;
                fenceOperation.fenceShow(nodesId, changeNodes[j]);
                fenceOperation.sectionPointState(nodesId, true);
            }
        } else {
            fenceCheckLength = nodeLength;
            changeNodes = zTree.getChangeCheckedNodes();
            for (var i = 0, len = changeNodes.length; i < len; i++) {
                changeNodes[i].checkedOld = false;
                zTree.cancelSelectedNode(changeNodes[i]);
                nodesId = changeNodes[i].id;
                fenceOperation.hideFence(nodesId);
                fenceOperation.fenceHidden(nodesId);
                fenceOperation.sectionPointState(nodesId, false);
            }
        }
    },
    //当点击或选择围栏时，访问后台返回围栏详情
    getFenceDetail: function (fenceNode, showMap) {
        // ajax访问后端查询
        layer.load(2);
        $.ajax({
            type: "POST",
            url: "/clbs/m/functionconfig/fence/bindfence/getFenceDetails",
            data: {
                "fenceNodes": JSON.stringify(fenceNode)
            },
            dataType: "json",
            success: function (data) {
                layer.closeAll('loading');
                if (data.success) {
                    var dataList = data.obj;
                    if (dataList != null && dataList.length > 0) {
                        if (dataList[0].fenceType === "zw_m_line") {
                            fanceID = dataList[0].fenceData[0].lineId;
                        }
                        for (var i = 0; i < dataList.length; i++) {
                            var fenceData;
                            var fenceType = dataList[i].fenceType;
                            var wayPointArray;
                            if (fenceType === 'zw_m_travel_line') {
                                fenceData = dataList[i].allPoints;
                                wayPointArray = dataList[i].passPointData;
                            } else {
                                fenceData = dataList[i].fenceData;
                            }
                            var lineSpot = dataList[i].lineSpot === undefined ? [] : dataList[i].lineSpot;
                            var lineSegment = dataList[i].lineSegment === undefined ? [] : dataList[i].lineSegment;
                            if (fenceType === "zw_m_marker") { // 标注
                                fenceOperation.drawMark(fenceData, showMap);
                            } else if (fenceType === "zw_m_line") { // 线
                                fenceOperation.drawLine(fenceData, lineSpot, lineSegment, showMap);
                            } else if (fenceType === "zw_m_rectangle") { // 矩形
                                fenceOperation.drawRectangle(fenceData, showMap);
                            } else if (fenceType === "zw_m_polygon") { // 多边形
                                fenceOperation.drawPolygon(fenceData, showMap);
                            } else if (fenceType === "zw_m_circle") { // 圆形
                                fenceOperation.drawCircle(fenceData, showMap);
                            } else if (fenceType === "zw_m_administration") { // 行政区域
                                var aId = dataList[0].aId;
                                fenceOperation.drawAdministration(fenceData, aId, showMap);
                            } else if (fenceType === "zw_m_travel_line") { // 行驶路线
                                fenceOperation.drawTravelLine(fenceData, showMap, dataList[i].travelLine, wayPointArray);
                            }
                        }
                    }
                    if (window.prevFence) {
                        map.remove(window.prevFence);
                        window.prevFence = null;
                    }
                }
            }
        });
    },
    updateFence: function (fenceId_shape, flag) {
        var width;
        var strs = fenceId_shape.split("#");
        var fenceId = strs[0];
        var shape = strs[1];
        var fenceId_shape_value;
        if (strs.length === 3) {
            fenceId_shape_value = strs[0] + "#" + strs[1];
        } else {
            fenceId_shape_value = fenceId_shape;
        }
        // ajax访问后端查询
        $.ajax({
            type: "POST",
            async: false,
            url: "/clbs/m/functionconfig/fence/managefence/previewFence",
            data: {"fenceIdShape": fenceId_shape_value},
            dataType: "json",
            success: function (data) {
                var rightclick = map.currentMap === 'tMap' ? 'contextmenu' : "rightclick";
                if (data.success) {
                    var dataList = data.obj;
                    if (flag) {
                        width = dataList[0].line.width;
                    } else {
                        if (strs.length === 2) {
                            if (dataList != null && dataList.length > 0) {
                                $("#myPageTop").hide();
                                $("#result").hide();
                                $(".fenceA").removeClass("fenceA-active");
                                mouseTool.close(true);
                                for (var i = 0; i < dataList.length; i++) {
                                    var fenceType = dataList[i].fenceType;
                                    var fenceData;
                                    var travelLine;
                                    var passPointData;
                                    if (fenceType === 'zw_m_travel_line') {
                                        travelLine = dataList[i].travelLine;
                                        passPointData = dataList[i].passPointData;
                                    } else {
                                        fenceData = dataList[i].fenceData;
                                    }
                                    var line = dataList[i].line;
                                    var lineSegment = dataList[i].lineSegment === undefined ? [] : dataList[i].lineSegment;
                                    var lineSpot = [];
                                    var polygon = dataList[i].polygon;
                                    if (fenceType === "zw_m_marker") { // 标注
                                        layer.msg(fenceOperationLableEdit);
                                        map.off(rightclick, amendLine);
                                        map.off(rightclick, amendPolygon);
                                        map.off(rightclick, amendCircle);
                                        fenceOperation.drawMark(fenceData, map);
                                        polyFence.setDraggable(true);
                                        moveMarkerFenceId = fenceId;
                                        moveMarkerBackData = fenceData;
                                        polyFence.on("mouseup", fenceOperation.moveMarker);
                                    } else if (fenceType === "zw_m_line") { // 线
                                        // layer.confirm("是否重置该线路的分段？", {btn : ["是", "否"]}, function () {
                                        $("#lineId").val(fenceId);
                                        var url = "/clbs/m/functionconfig/fence/managefence/resetSegment";
                                        json_ajax("POST", url, "json", false, {"lineId": fenceId}, fenceOperation.resetSegment);
                                        layer.closeAll();
                                        if (fenceSectionPointMap.containsKey(fenceId)) {
                                            fenceSectionPointMap.remove(fenceId);
                                        }
                                        if (PolyEditorMap.containsKey(fenceId)) {
                                            PolyEditorMap.remove(fenceId);
                                        }
                                        var lineEditorObjArray = [];
                                        for (var i = 0; i < polyFence.length; i++) {
                                            var lineEditorObj = map.polylineEditor(map, polyFence[i]);
                                            lineEditorObj.open();
                                            lineEditorObjArray.push(lineEditorObj);
                                        }
                                        PolyEditorMap.put(fenceId, lineEditorObjArray);
                                        //隐藏分段限速点
                                        if (sectionPointMarkerMap.containsKey(fenceId)) {
                                            var sectionPointMarkerMapValue = sectionPointMarkerMap.get(fenceId);
                                            for (var t = 0; t < sectionPointMarkerMapValue.length; t++) {
                                                sectionPointMarkerMapValue[t].hide();
                                            }
                                            sectionPointMarkerMap.remove(fenceId);
                                        }
                                        if (lineSpotMap.containsKey(fenceId)) {
                                            var thisStopArray = lineSpotMap.get(fenceId);
                                            for (i = 0; i < thisStopArray.length; i++) {
                                                thisStopArray[i].hide();
                                            }
                                        }
                                        map.off(rightclick, amendCircle);
                                        map.off(rightclick, amendPolygon);
                                        amendLine = function () {
                                            fenceOperation.rightClickHandler(fenceType, line, fenceId);
                                        };
                                        map.on(rightclick, amendLine);
                                        //});
                                    } else if (fenceType === "zw_m_rectangle") { // 矩形
                                        layer.msg(fenceOperationAreaReelect);
                                        fenceOperation.drawRectangle(fenceData, map);
                                        mouseToolEdit.rectangle();
                                        rectangle = true;
                                        mouseToolEdit.on("draw", function (e) {
                                            if (!isAddFlag && !isAreaSearchFlag) {
                                                var changeArray = e.obj.aE ? e.obj.aE[0][0] : e.obj.getPath();
                                                var pointSeqs = ""; // 点序号
                                                var longitudes = ""; // 所有的经度
                                                var latitudes = ""; // 所有的纬度
                                                var array = new Array();
                                                if (changeArray) {
                                                    // var flagArr = [changeArray[0].P];
                                                    /*for (var i = 0; i < changeArray.length; i++) {
                                                        // if (i != 0 && changeArray[i].P == flagArr[0]) {
                                                        //     flagArr.push(changeArray[i].P);
                                                        // }
                                                        array.push([changeArray[i].lng, changeArray[i].lat]);
                                                    }*/
                                                    array = changeArray;
                                                    var newArr = [];
                                                    if (array.length > 0 && !array[0][0]) {
                                                        for (i = 0; i < array.length; i++) {
                                                            newArr.push([array[i].lng, array[i].lat]);
                                                        }
                                                        array = newArr;
                                                    }
                                                    // if (changeArray.length != flagArr.length) {
                                                    $("#LUPointLngLat").val(array[0][0] + "," + array[0][1]);
                                                    if (array.length > 2) {
                                                        $("#RDPointLngLat").val(array[2][0] + "," + array[2][1]);
                                                    }
                                                    // }
                                                    for (i = 0; i < array.length; i++) {
                                                        $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(2)").text(array[i][0]);
                                                        $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(3)").text(array[i][1]);
                                                        pointSeqs += i + ","; // 点序号
                                                        longitudes += array[i][0] + ","; // 把所有的经度组合到一起
                                                        latitudes += array[i][1] + ","; // 把所有的纬度组合到一起
                                                    }
                                                }
                                                // 去掉点序号、经度、纬度最后的一个逗号
                                                if (pointSeqs.length > 0) {
                                                    pointSeqs = pointSeqs.substr(0, pointSeqs.length - 1);
                                                }
                                                if (longitudes.length > 0) {
                                                    longitudes = longitudes.substr(0, longitudes.length - 1);
                                                }
                                                if (latitudes.length > 0) {
                                                    latitudes = latitudes.substr(0, latitudes.length - 1);
                                                }
                                                $("#addOrUpdateRectangleFlag").val("1"); // 修改矩形，给此文本框赋值为1
                                                $("#rectangleId").val(fenceId); // 矩形区域id
                                                // 矩形修改框弹出时给文本框赋值
                                                $("#rectangleName").val(fenceData.name);
                                                $("#rectangleType").val(fenceData.type);
                                                $("#rectangleDescription").val(fenceData.description);
                                                $("#pointSeqsRectangles").val(pointSeqs);
                                                $("#longitudesRectangles").val(longitudes);
                                                $("#latitudesRectangles").val(latitudes);
                                                pageLayout.closeVideo();
                                                setTimeout(function () {
                                                    $("#rectangle-form").modal("show");
                                                }, 200);
                                            }
                                        });
                                        map.off(rightclick, amendLine);
                                        map.off(rightclick, amendPolygon);
                                        map.off(rightclick, amendCircle);
                                    } else if (fenceType === "zw_m_polygon") { // 多边形
                                        fenceOperation.drawPolygon(fenceData, map);
                                        if (PolyEditorMap.containsKey(fenceId)) {
                                            PolyEditorMap.remove(fenceId);
                                        }
                                        var polygonEditorObj = map.polygonEditor(map, polyFence);
                                        polygonEditorObj.open();
                                        PolyEditorMap.put(fenceId, polygonEditorObj);
                                        map.off(rightclick, amendCircle);
                                        map.off(rightclick, amendLine);
                                        amendPolygon = function () {
                                            fenceOperation.rightClickHandler(fenceType, polygon, fenceId);
                                        };
                                        map.on(rightclick, amendPolygon);
                                    } else if (fenceType === "zw_m_circle") { // 圆形
                                        fenceOperation.drawCircle(fenceData, map);
                                        if (PolyEditorMap.containsKey(fenceId)) {
                                            PolyEditorMap.remove(fenceId);
                                        }
                                        var circleEditorObj = map.circleEditor(map, polyFence);
                                        circleEditorObj.open();
                                        PolyEditorMap.put(fenceId, circleEditorObj);
                                        map.off(rightclick, amendLine);
                                        map.off(rightclick, amendPolygon);
                                        amendCircle = function () {
                                            $("#addOrUpdateCircleFlag").val("1"); // 修改圆，给此文本框赋值为1
                                            $("#circleId").val(fenceId); // 圆形区域id
                                            // 圆形区域修改框弹出时给文本框赋值
                                            $("#circleName").val(fenceData.name);
                                            $("#circleType").val(fenceData.type);
                                            $("#circleDescription").val(fenceData.description);
                                            var center = polyFence.getCenter();
                                            if (map.lnglatTransToAmap) {// 将经纬度转换为高德的
                                                var newPoint = map.lnglatTransToAmap(center.lng, center.lat);
                                                center = {
                                                    lng: newPoint[0],
                                                    lat: newPoint[1]
                                                }
                                            }
                                            var radius = polyFence.getRadius();
                                            $("#circle-lng").attr("value", center.lng);
                                            $("#circle-lat").attr("value", center.lat);
                                            $("#circle-radius").attr("value", radius);
                                            $("#editCircleLng").val(center.lng);
                                            $("#editCircleLat").val(center.lat);
                                            $("#editCircleRadius").val(radius);
                                            pageLayout.closeVideo();
                                            setTimeout(function () {
                                                $("#circleArea").modal("show")
                                            }, 200);
                                        };
                                        map.on(rightclick, amendCircle);
                                    } else if (fenceType === "zw_m_administration") {

                                    } else if (fenceType === 'zw_m_travel_line') { //修改行驶路线
                                        isAddDragRoute = true;
                                        $('#addOrUpdateTravelFlag').val('1');
                                        var this_line_id = travelLine.id;
                                        $('#travelLineId').val(this_line_id);
                                        if (travelLineMap.containsKey(this_line_id)) {
                                            var this_fence = travelLineMap.get(this_line_id);
                                            map.remove([this_fence]);
                                            travelLineMap.remove(this_line_id);
                                        }
                                        var lineOffset = travelLine.lineOffset; //偏移量
                                        var lineType = travelLine.lineType;//围栏类型
                                        var lineName = travelLine.name; //围栏名称
                                        var description = travelLine.description;//描述信息
                                        $('#dragRouteLineName').val(lineName);
                                        $('#dragRouteType').val(lineType);
                                        $('#excursion').val(lineOffset);
                                        $('#dragRouteDescription').val(description);
                                        var start_lnglat = [travelLine.startLongitude, travelLine.startLatitude];
                                        var end_lnglat = [travelLine.endLongitude, travelLine.endLatitude];
                                        var pointArray = [];
                                        pointArray.push(start_lnglat);
                                        if (passPointData !== undefined) {
                                            for (var j = 0, len = passPointData.length; j < len; j++) {
                                                pointArray.push([passPointData[j].longitude, passPointData[j].latitude]);
                                            }
                                        }
                                        pointArray.push(end_lnglat);
                                        //逆地理编码
                                        fenceOperation.getAddressValue(pointArray, 0, []);
                                        $('#drivenRoute').show();
                                        //路径规划
                                        fenceOperation.madeDragRoute(pointArray);
                                    }
                                }
                            }
                        } else {
                            fenceOperation.fenceDetails(dataList);
                        }
                    }
                } else {
                    layer.msg(data.msg);
                }
            }
        });
        return width;
    },
    //修改时右键点击事件:type-围栏类型；data-当前修改需要回显的数据
    rightClickHandler: function (type, data, fenceId) {
        var changeArray;
        if (Array.isArray(polyFence)) {
            var lineAllArray = [];

            for (var j = 0; j < polyFence.length; j++) {
                var changeLineArray = polyFence[j].getPath();
                if (map.lnglatTransToAmap) {// 将经纬度转换为高德的
                    var newPath = [];
                    for (var i = 0; i < changeLineArray.length; i++) {
                        var newPoint = map.lnglatTransToAmap(changeLineArray[i].lng, changeLineArray[i].lat);
                        newPath.push({
                            lng: newPoint[0],
                            lat: newPoint[1]
                        })
                    }
                    changeLineArray = newPath;
                }
                lineAllArray = lineAllArray.concat(changeLineArray);
            }
            changeArray = lineAllArray
        } else {
            changeArray = polyFence.getPath();
            if (map.lnglatTransToAmap) {// 将经纬度转换为高德的
                var newPath = [];
                for (var i = 0; i < changeArray.length; i++) {
                    var newPoint = map.lnglatTransToAmap(changeArray[i].lng, changeArray[i].lat);
                    newPath.push({
                        lng: newPoint[0],
                        lat: newPoint[1]
                    })
                }
                changeArray = newPath;
            }
        }
        var pointSeqs = ""; // 点序号
        var longitudes = ""; // 所有的经度
        var latitudes = ""; // 所有的纬度
        var array = [];
        for (var i = 0; i < changeArray.length; i++) {
            array.push([changeArray[i].lng, changeArray[i].lat]);
        }

        var strc = '<div class="isQueryShow" style="padding: 0 30px"><label>拐点数据 <span class="fa fa-chevron-up" aria-hidden="true"></span></label></div>';
        strc += '<div class="pointList" style="padding-right: 30px">';
        for (i = 0; i < array.length; i++) {
            $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(2)").text(array[i][0]);
            $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(3)").text(array[i][1]);
            pointSeqs += i + ","; // 点序号
            longitudes += array[i][0] + ","; // 把所有的经度组合到一起
            latitudes += array[i][1] + ","; // 把所有的纬度组合到一起

            var num = i;
            strc += '<div class="form-group sectionLngLat">';
            strc += '<label class="col-md-2 control-label">经度：</label>';
            strc += '<div class="col-md-3 sectionLng">';
            strc += '<input type="text" id="piecewiseLng' + num + '" name="lng" placeholder="请输入经度值" value="' + array[i][0] + '" class="form-control" />';
            strc += '</div>';
            strc += '<label class="col-md-2 control-label">纬度：</label>';
            strc += '<div class="col-md-3 sectionLat">';
            strc += '<input type="text" id="piecewiseLat' + num + '" name="lat" placeholder="请输入纬度值" value="' + array[i][1] + '" class="form-control" />';
            strc += '</div>';
            strc += '<button type="button" class="btn btn-primary addLngLat">';
            strc += '<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>';
            strc += '</button>&ensp;';
            strc += '<button type="button" class="btn btn-danger removeLngLat">';
            strc += '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>';
            strc += '</button>';
            strc += '</div>'
        }
        // 去掉点序号、经度、纬度最后的一个逗号
        if (pointSeqs.length > 0) {
            pointSeqs = pointSeqs.substr(0, pointSeqs.length - 1);
        }
        if (longitudes.length > 0) {
            longitudes = longitudes.substr(0, longitudes.length - 1);
        }
        if (latitudes.length > 0) {
            latitudes = latitudes.substr(0, latitudes.length - 1);
        }
        if (type === "zw_m_line") {
            $("#addOrUpdateLineFlag").val("1"); // 修改线路，给此文本框赋值为1
            $("#lineId").val(fenceId); // 线路id
            // 路线修改框弹出时给文本框赋值
            $("#lineName1").val(data.name);
            $("#lineType1").val(data.type);
            $("#lineWidth1").val(data.width);
            $("#lineDescription1").val(data.description);
            $("#pointSeqs").val(pointSeqs);
            $("#longitudes").val(longitudes);
            $("#latitudes").val(latitudes);
            $('#inflectionPointBox').html(strc);
            $(".addLngLat").unbind("click").bind("click", fenceOperation.newAddLngLat);
            $(".removeLngLat").unbind("click").bind("click", fenceOperation.newRemoveLngLat);
            $(".isQueryShow").unbind("click").bind("click", fenceOperation.isQueryShow);
            pageLayout.closeVideo();
            setTimeout(function () {
                $("#addLine").modal("show");
            }, 200);
        } else if (type === "zw_m_rectangle") {
            $("#addOrUpdateRectangleFlag").val("1"); // 修改矩形，给此文本框赋值为1
            $("#rectangleId").val(fenceId); // 矩形区域id
            // 矩形修改框弹出时给文本框赋值
            $("#rectangleName").val(data.name);
            $("#rectangleType").val(data.type);
            $("#rectangleDescription").val(data.description);
            $("#pointSeqsRectangles").val(pointSeqs);
            $("#longitudesRectangles").val(longitudes);
            $("#latitudesRectangles").val(latitudes);
            pageLayout.closeVideo();
            setTimeout(function () {
                $("#rectangle-form").modal("show");
            }, 200);
        } else if (type === "zw_m_polygon") {
            var html = '';
            for (var i = 0; i < array.length; i++) {
                html += '<div class="form-group">'
                    + '<label class="col-md-3 control-label">顶点' + (i + 1) + '经纬度：</label>'
                    + '<div class=" col-md-8">'
                    + '<input type="text" name="polygonPointLngLat" placeholder="请输入顶点经纬度" value="' + array[i][0] + "," + array[i][1] + '" class="form-control rectangleAllPointLngLat"/>'
                    + '</div>'
                    + '</div>'
            }
            $("#rectangleAllPointShow").html(html);
            $("#addOrUpdatePolygonFlag").val("1"); // 修改多边形，给此文本框赋值为1
            $("#polygonId").val(fenceId); // 多边形区域id
            // 多边形修改框弹出时给文本框赋值
            $("#polygonName").val(data.name);
            $("#polygonType").val(data.type);
            $("#polygonDescription").val(data.description);
            $("#pointSeqsPolygons").val(pointSeqs);
            $("#longitudesPolygons").val(longitudes);
            $("#latitudesPolygons").val(latitudes);
            pageLayout.closeVideo();
            setTimeout(function () {
                $("#myModal").modal("show");
            }, 200);
        }
    },
    // 删除围栏
    deleteFence: function (treeNode) {
        var url = "/clbs/m/functionconfig/fence/managefence/delete_" + treeNode.id + ".gsp";
        layer.confirm(fenceOperationFenceDeleteConfirm, {
            btn: ['确定', '取消'],
            icon: 3,
            move: false,
            title: "操作确认",
        }, function (index) {
            json_ajax("POST", url, "json", true, null, function (data) {
                if (data.success) {
                    fenceOperation.fenceHidden(treeNode.id);
                    if (fenceIDMap.containsKey(treeNode.id)) {
                        var fence = fenceIDMap.get(treeNode.id);
                        map.remove(fence);
                    }
                    fenceIDMap.remove(treeNode.id);
                    fenceOperation.sectionPointState(treeNode.id, false);
                    if (lineSpotMap.containsKey(treeNode.id)) {
                        var thisStopArray = lineSpotMap.get(treeNode.id);
                        map.remove(thisStopArray);
                        lineSpotMap.remove(treeNode.id);
                    }
                    zTree.removeNode(treeNode);
                    fenceOperation.addNodes();
                } else {
                    layer.msg(data.msg);
                }
            });
            layer.close(index);
        }, function (index) {
            layer.close(index);
        });
    },
    //标注
    drawMark: function (mark, thisMap) {
        var markId = mark.id;
        if (fenceIDMap.containsKey(markId)) {
            var markerObj = fenceIDMap.get(markId);
            thisMap.remove(markerObj);
            fenceIDMap.remove(markId);
        }
        var dataArr = [];
        dataArr.push(mark.longitude);
        dataArr.push(mark.latitude);

        polyFence = map.marker({
            position: dataArr,
            offset: map.pixel(-9, -23),
            label: {
                content: mark.name,
                offset: map.pixel(-10, -23),
            },
            size: {w: 32, h: 32}
        });

        if (mark.markIcon === 1) {
            polyFence.setIcon('../../resources/img/circleIcon.png');
        } else {
            polyFence.setIcon('../../resources/img/mark_bs.png');
        }

        polyFence.on('mouseover', function (e) {
            // console.log($("#" + e.target.geometry.id).attr("x"), $("#" + e.target.geometry.id).attr("y"), 'xy');
            // console.log($("#" + e.target.layer.id).css("left"), $("#" + e.target.layer.id).css("top"), 'lefttop');
            $('#createName').html(html2Escape(mark.name));
            $('#createType').html(html2Escape(mark.type));
            $('#createPer').html(html2Escape(mark.createDataUsername));
            $('#createTime').html(mark.createDataTime);
            $('#createDesc').html(html2Escape(mark.description));
            var alertContH = $('#markerInfoWindow').height();
            // 天地图兼容性更改
            if (map.currentMap === 'tMap') {
                e.pixel = e.containerPoint;
            }
            // 四位地图标注弹框兼容性更改
            $('#markerInfoWindow').css({
                'left': (e.pixel ? e.pixel.x : $("#" + e.target.geometry.id).attr("x") - parseInt($("#" + e.target.layer.id).css("left"))) - 88 + 'px',
                'top': (e.pixel ? e.pixel.y : $("#" + e.target.geometry.id).attr("y") - parseInt($("#" + e.target.layer.id).css("top"))) - alertContH - 10 + 'px',
            }).show();
        });
        polyFence.on('mouseout', function (e) {
            $('#markerInfoWindow').hide()
        });
        thisMap.on('resize', function (e) {
            $('#markerInfoWindow').hide()
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIDMap.put(markId, polyFence);
    },
    //线
    drawLine: function (line, lineSpot, lineSegment, thisMap) {
        var lineAllPointArray = [];
        for (var i = 0, len = line.length; i < len; i++) {
            lineAllPointArray.push([line[i].longitude, line[i].latitude]);
        }
        lineAllPointArray = lineAllPointArray.unique3();
        var startPointLatLng = [line[0].longitude, line[0].latitude];//起点坐标
        var endPointLatLng = [line[line.length - 1].longitude, line[line.length - 1].latitude];//终点坐标
        var lineId = line[0].lineId;
        $("#lineId").val(lineId);
        //是否存在线
        if (fenceIDMap.containsKey(lineId)) {
            var thisFence = fenceIDMap.get(lineId);
            if (Array.isArray(thisFence)) {
                for (i = 0; i < thisFence.length; i++) {
                    thisFence[i].hide();
                }
            } else {
                thisFence.hide();
            }
            fenceIDMap.remove(lineId);
        }
        if (PolyEditorMap.containsKey(lineId)) {
            var mapEditFence = PolyEditorMap.get(lineId);
            if (Array.isArray(mapEditFence)) {
                for (i = 0; i < mapEditFence.length; i++) {
                    mapEditFence[i].close();
                }
            } else {
                mapEditFence.close();
            }
        }
        var dataArr = [];
        if (line.length > 0) {
            $.each(line, function (index, item) {
                if (item.type === '0') {
                    dataArr[index] = [item.longitude, item.latitude];
                }
            });
            var spotArray = [];
            for (i = 0; i < lineSpot.length; i++) {
                var dataArrm = [];
                content = [];
                content.push("名称：" + lineSpot[i].name);
                content.push("经度：" + lineSpot[i].longitude);
                content.push("维度：" + lineSpot[i].latitude);
                content.push("达到时间：" + lineSpot[i].arriveTime);
                content.push("离开时间：" + lineSpot[i].leaveTime);
                content.push("描述：" + lineSpot[i].description);
                content.push('<a id="jump" onClick="fenceOperation.deleteKeyPoint(\'' + lineSpot[i].id + '\')">删除</a>');
                dataArrm.push(lineSpot[i].longitude);
                dataArrm.push(lineSpot[i].latitude);
                drawFence = map.marker({
                    position: dataArrm
                });
                drawFence.content = content.join("<br/>");
                drawFence.setMap(thisMap);
                thisMap.setFitView(drawFence);
                spotArray.push(drawFence);
                drawFence.on('click', amapOperation.markerClick);
            }
            if (lineSpotMap.containsKey(lineId)) {
                var thisStopArray = lineSpotMap.get(lineId);
                map.remove(thisStopArray);
                lineSpotMap.remove(lineId);
            }
            lineSpotMap.put(lineId, spotArray);
            $.each(dataArr, function (index, item) {
                // index是索引值（即下标）   item是每次遍历得到的值；
                if (item === undefined) {
                    dataArr.splice(index, 1);
                }
            });
        }
        var c = 1;
        var lineSectionArray = [];
        if (lineSegment.length !== 0) {
            var segment = [];
            for (i = 0; i < lineSegment.length; i++) {
                var segmentE = [];
                var segmentLon = lineSegment[i].longitude.split(",");
                var segmentLat = lineSegment[i].latitude.split(",");
                for (j = 0; j < segmentLon.length; j++) {
                    segmentE[j] = [Number(segmentLon[j]), Number(segmentLat[j])]
                }
                segment.push(segmentE)
            }
            if (sectionPointMarkerMap.containsKey(lineId)) {
                var sectionPointMarkerMapValue = sectionPointMarkerMap.get(lineId);
                for (var t = 0; t < sectionPointMarkerMapValue.length; t++) {
                    sectionPointMarkerMapValue[t].hide();
                }
                sectionPointMarkerMap.remove(lineId);
            }
            var createSectionMarkerValue = [];
            for (i = 0; i < segment.length; i++) {
                if (segment[i].length > 1) {
                    var pointLatLng = segment[i][segment[i].length - 1];
                    var num = '<p class="sectionPointIcon">' + (i + 1) + '</p>';
                    var sectionMarker = map.marker({
                        icon: "../../resources/img/sectionPoint.png",
                        position: pointLatLng,
                        content: num,
                        number: (i + 1),
                        offset: map.pixel(-10, -25),
                        size: {w: 19, h: 35}
                    });
                    sectionMarker.setMap(map);
                    createSectionMarkerValue.push(sectionMarker);
                    if (lineSegment[i].maximumSpeed >= 0 && lineSegment[i].maximumSpeed <= 40) {
                        var polyFencec = map.polyline({
                            path: segment[i], //设置线覆盖物路径
                            strokeColor: "#66CD00", //线颜色
                            strokeOpacity: 1, //线透明度
                            strokeWeight: 5, //线宽
                            strokeStyle: "dashed", //线样式
                            strokeDasharray: [10, 5],
                            //补充线样式
                            map: thisMap,
                        });
                        // polyFencec.setMap(thisMap);
                        // thisMap.setFitView(polyFencec);
                        lineSectionArray.push(polyFencec);
                    }
                    if (lineSegment[i].maximumSpeed > 40 && lineSegment[i].maximumSpeed <= 80) {
                        polyFencec = map.polyline({
                            path: segment[i], //设置线覆盖物路径
                            strokeColor: "#EEEE00", //线颜色
                            strokeOpacity: 1, //线透明度
                            strokeWeight: 5, //线宽
                            strokeStyle: "dashed", //线样式
                            strokeDasharray: [10, 5],
                            //补充线样式
                            map: thisMap
                        });
                        // polyFencec.setMap(thisMap);
                        // thisMap.setFitView(polyFencec);
                        lineSectionArray.push(polyFencec);
                    }
                    if (lineSegment[i].maximumSpeed > 80 && lineSegment[i].maximumSpeed <= 100) {
                        polyFencec = map.polyline({
                            path: segment[i], //设置线覆盖物路径
                            strokeColor: "#EE7600", //线颜色
                            strokeOpacity: 1, //线透明度
                            strokeWeight: 5, //线宽
                            strokeStyle: "dashed", //线样式
                            strokeDasharray: [10, 5],
                            //补充线样式
                            map: thisMap
                        });
                        // polyFencec.setMap(thisMap);
                        // thisMap.setFitView(polyFencec);
                        lineSectionArray.push(polyFencec);
                    }
                    if (lineSegment[i].maximumSpeed > 100) {
                        polyFencec = map.polyline({
                            path: segment[i], //设置线覆盖物路径
                            strokeColor: "#EE0000", //线颜色
                            strokeOpacity: 1, //线透明度
                            strokeWeight: 5, //线宽
                            strokeStyle: "dashed", //线样式
                            strokeDasharray: [10, 5],
                            //补充线样式
                            map: thisMap
                        });
                        // polyFencec.setMap(thisMap);
                        // thisMap.setFitView(polyFencec);
                        lineSectionArray.push(polyFencec);
                    }
                }
            }
            thisMap.setFitView(lineSectionArray);
            sectionPointMarkerMap.put(lineId, createSectionMarkerValue);
            fenceIDMap.put(lineId, lineSectionArray);
        } else {
            polyFencec = map.polyline({
                path: dataArr, //设置线覆盖物路径
                strokeColor: "#3366FF", //线颜色
                strokeOpacity: c, //线透明度
                strokeWeight: 5, //线宽
                strokeStyle: "solid", //线样式
                strokeDasharray: [10, 5],
                zIndex: 51,
                //补充线样式
                map: thisMap
            });
            lineSectionArray.push(polyFencec);
            fenceIDMap.put(lineId, polyFencec);
            // polyFencec.setMap(thisMap);
            thisMap.setFitView(polyFencec);
        }
        polyFence = lineSectionArray;
        if (isEdit) {
            for (var j = 0; j < polyFence.length; j++) {
                var polyFenceList = polyFence[j];
                var points = lineAllPointArray.slice(); //保存线上的所有点(包括分段点)
                //线单击
                polyFenceList.on('click', function (e) {
                    if (map.getZoom() >= 16) {
                        if (map.currentMap === 'baidu') {
                            var clickLng = e.latLng.lng;
                            var clickLat = e.latLng.lat;
                            var newPoint = map.bMapTransAMap(clickLng, clickLat);
                            clickLng = newPoint[0];
                            clickLat = newPoint[1];
                        } else {
                            var clickLng = e.lnglat.getLng();
                            var clickLat = e.lnglat.getLat();
                        }
                        var clickLngLat = [clickLng, clickLat];
                        if (sectionMarkerPointArray.containsKey(lineId)) {
                            var sectionValue = sectionMarkerPointArray.get(lineId);
                            var value = sectionValue[0];
                            var valueArray = [];
                            for (var m = 0; m < value.length; m++) {
                                if (sectionValue[1] === false) {
                                    var index = lineAllPointArray.indexOf(value[m]);
                                    lineAllPointArray.splice(index, 1);
                                } else {
                                    valueArray.push(value[m]);
                                }
                            }
                            valueArray.push(clickLngLat);
                            sectionMarkerPointArray.remove(lineId);
                            sectionMarkerPointArray.put(lineId, [valueArray, true]);
                        } else {
                            sectionMarkerPointArray.put(lineId, [[clickLngLat], true]);
                        }
                        layer.confirm(fenceOperationOperationSelect, {
                            btn: ['关键点', '分段', '取消'],
                            closeBtn: 0,
                            btn3: function () {
                                if (lineSegment.length === 0) {
                                    fenceOperation.sectionRateLimitingClose(lineId);
                                }
                            },
                            id: 'lineClickOperation',
                            success: function (layero) {
                                var btn = layero.find('.layui-layer-btn').children('.layui-layer-btn1').css({
                                    'border-color': '#4898d5',
                                    'background-color': '#2e8ded',
                                    'color': '#ffffff',
                                });
                            },
                        }, function () {
                            layer.closeAll();
                            if (lineSegment.length === 0) {
                                fenceOperation.sectionRateLimitingClose(lineId);
                            }
                            $("#marking-lng").val(clickLng);
                            $("#marking-lat").val(clickLat);
                            $("#lineIDs").val(lineId);
                            pageLayout.closeVideo();
                            $('#addMonitoringTag').modal('show');
                        }, function () {
                            if (lineSegment.length !== 0) {
                                // layer.confirm("是否重置该线路的分段？", {btn : ["是", "否"]}, function () {
                                $("#lineId").val(lineId);
                                var url = "/clbs/m/functionconfig/fence/managefence/resetSegment";
                                json_ajax("POST", url, "json", false, {"lineId": lineId}, fenceOperation.resetSegment);
                                layer.closeAll();
                                //清除分段点标注
                                if (sectionPointMarkerMap.containsKey(lineId)) {
                                    var sectionPointMarkerMapValue = sectionPointMarkerMap.get(lineId);
                                    for (var t = 0; t < sectionPointMarkerMapValue.length; t++) {
                                        sectionPointMarkerMapValue[t].hide();
                                    }
                                    sectionPointMarkerMap.remove(lineId);
                                }
                                if (fenceSectionPointMap.containsKey(lineId)) {
                                    fenceSectionPointMap.remove(lineId);
                                }
                                //});
                            } else if (lineSegment.length === 0) {
                                //把分段点区域的所有经纬度按顺序存在集合
                                if (fenceSectionPointMap.containsKey(lineId)) {
                                    var sectionPointLatLng = [];//分段点经纬度集合
                                    var fenceSectionPointMapValue = fenceSectionPointMap.get(lineId);
                                    sectionPointLatLng.push(clickLngLat);
                                    //取出分段点经纬度存入
                                    for (var m = 0; m < fenceSectionPointMapValue.length; m++) {
                                        sectionPointLatLng.push(fenceSectionPointMapValue[m][fenceSectionPointMapValue[m].length - 1]);
                                    }
                                    //把点击的分段点经纬度加入路线经纬度集合中
                                    for (i = 0, len = points.length; i < len - 1; i++) {
                                        var inLine = map.isPointOnSegment(clickLngLat, points[i], points[i + 1]);
                                        if (inLine) {
                                            if (!(points[i][0] === clickLngLat[0] && points[i][1] === clickLngLat[1])) {
                                                points.splice(i + 1, 0, clickLngLat);
                                                break;
                                            }
                                        }
                                    }
                                    fenceSectionPointMap.remove(lineId);
                                    var indexArray = [];
                                    //将各个分段点位置存入集合
                                    for (var n = 0; n < sectionPointLatLng.length; n++) {//循环分段点集合
                                        len = points.length;
                                        for (var s = 0; s < len; s++) {//循环所有点
                                            if (points[s][0] === sectionPointLatLng[n][0] && points[s][1] === sectionPointLatLng[n][1]) {
                                                indexArray.push(s);
                                            }
                                        }
                                    }
                                    var startIndex = 0;
                                    var sectionValue = [];
                                    //是否含有该ID标注,然后删除
                                    if (sectionPointMarkerMap.containsKey(lineId)) {
                                        sectionPointMarkerMapValue = sectionPointMarkerMap.get(lineId);
                                        for (t = 0; t < sectionPointMarkerMapValue.length; t++) {
                                            sectionPointMarkerMapValue[t].hide();
                                        }
                                        sectionPointMarkerMap.remove(lineId);
                                    }
                                    markerPointMap = [];
                                    var indexSortArray = indexArray.sort(fenceOperation.sortNumber);
                                    for (var y = 0; y < indexSortArray.length; y++) {
                                        var end = Number(indexSortArray[y]);
                                        var section = points.slice(startIndex, end + 1);
                                        startIndex = end;
                                        sectionValue.push(section);
                                        pointLatLng = points[indexSortArray[y]];
                                        num = '<p class="sectionPointIcon">' + (y + 1) + '</p>';
                                        sectionMarker = map.marker({
                                            icon: "../../resources/img/sectionPoint.png",
                                            position: pointLatLng,
                                            content: num,
                                            number: (y + 1),
                                            offset: map.pixel(-10, -25),
                                            size: {w: 19, h: 35}
                                        });
                                        sectionMarker.setMap(map);
                                        markerPointMap.push(sectionMarker);
                                    }
                                    sectionPointMarkerMap.put(lineId, markerPointMap);
                                    fenceSectionPointMap.put(lineId, sectionValue);
                                } else {
                                    //第一次存值
                                    for (i = 0, len = points.length - 1; i < len; i++) {
                                        inLine = map.isPointOnSegment(clickLngLat, points[i], points[i + 1]);

                                        if (inLine) {
                                            if (!(points[i][0] === clickLngLat[0] && points[i][1] === clickLngLat[1])) {
                                                points.splice(i + 1, 0, clickLngLat); //插入分段点
                                                var firstSection = points.slice(0, i + 2);
                                                var lastSection = points.slice(i + 1, points.length);
                                                fenceSectionPointMap.put(lineId, [firstSection, lastSection]);
                                                break;
                                            } else {
                                                firstSection = points.slice(0, i + 1);
                                                lastSection = points.slice(i, points.length);
                                                fenceSectionPointMap.put(lineId, [firstSection, lastSection]);
                                            }
                                        }
                                    }
                                    //添加分段点图标
                                    var markerPointMap = [];
                                    for (var j = 0; j < 2; j++) {
                                        var num = '<p class="sectionPointIcon">' + (j + 1) + '</p>';
                                        var pointLatLng = [];
                                        if (j === 0) {
                                            pointLatLng = clickLngLat;
                                        } else {
                                            pointLatLng = lineAllPointArray[lineAllPointArray.length - 1];
                                        }
                                        var sectionMarker = map.marker({
                                            icon: "../../resources/img/sectionPoint.png",
                                            position: pointLatLng,
                                            content: num,
                                            number: (j + 1),
                                            offset: map.pixel(-10, -25),
                                            size: {w: 19, h: 35}
                                        });
                                        sectionMarker.setMap(map);
                                        markerPointMap.push(sectionMarker);
                                    }
                                    sectionPointMarkerMap.put(lineId, markerPointMap);
                                }
                                var routMap = [];
                                var sectionPointArray = fenceSectionPointMap.get(lineId);
                                if (!sectionPointArray) {
                                    layer.confirm("分段数据错误", {
                                        btn: ["关闭"],
                                        closeBtn: 0
                                    }, function () {
                                        layer.closeAll();
                                    });
                                    return;
                                }
                                ;
                                for (var i = 0; i < sectionPointArray.length; i++) {
                                    var array = [];
                                    for (j = 0; j < sectionPointArray[i].length; j++) {
                                        array.push(sectionPointArray[i][j][0] + ";" + sectionPointArray[i][j][1]);
                                    }
                                    routMap.push(array);
                                }
                                layer.confirm(fenceOperationLineSubsection, {
                                    btn: ["完成", "继续分段"],
                                    closeBtn: 0
                                }, function () {
                                    $("#lineId").val(lineId);
                                    var value = lineId + "#zw_m_line";
                                    var width = fenceOperation.updateFence(value, true);
                                    var str = "";
                                    var strc = "";
                                    layer.closeAll();
                                    for (i = 0; i < routMap.length; i++) {
                                        var hrefs = "#route" + (i + 1);
                                        var rids = "route" + (i + 1);
                                        var sectionlng = "section-lng" + (i + 1);
                                        var sectionlat = "section-lat" + (i + 1);
                                        if (i === 0) {
                                            str += '<li class="active" id="TabFenceBox"><a href="#route1" data-toggle="tab">路段1</a></li>';
                                            strc += '<div class="tab-pane active" id="route1">';
                                            strc += '<div class="form-group hidden">';
                                            strc += '<input id="lineIDms1" name="lineId" value=""/>';
                                            strc += '<input id="sumn" name="sumn" value=""/>';
                                            strc += '<div class="col-md-3">';
                                            strc += '<input  id="section-lng1"   name="longitude" value=""/>';
                                            strc += '</div>';
                                            strc += '<div class="col-md-3" >';
                                            strc += '<input id="section-lat1"   name="latitude" value=""/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label"><label class="text-danger">*</label> 偏移量(m)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入偏移量" value="' + width + '" onkeyup="value=value.replace(/[^0-9]/g,\'\') " class="form-control" id="sectionOffset' + i + '" name="offset"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">路段行驶过长阈值(s)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段行驶过长阈值" value="0" class="form-control" id="sectionLongThreshold' + i + '" name="overlengthThreshold"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">路段行驶不足阈值(s)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段行驶不足阈值" value="0" class="form-control" id="sectionInsufficientThreshold' + i + '" name="shortageThreshold"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">路段最高速度（km/h）：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段最高速度" value="0" class="form-control" id="maxSpeed' + i + '" name="maximumSpeed"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">夜间最高速度(km/h)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请选择夜间限速时间段" value="0" class="form-control" id="nightMaxSpeed' + i + '" name="nightMaxSpeed"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">夜间限速时间段：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input style="cursor: pointer;  background-color: #fafafa;" class="dateTime2 form-control layer-date laydate-icon valid" id="nightLimitTime' + i + '" name="nightLimitTime" readonly>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">超速持续时间（s）：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入超速持续时间" value="0" class="form-control" id="durationSpeeding' + i + '" name="overspeedTime"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="isQueryShow"><label>拐点数据 <span class="fa fa-chevron-up" aria-hidden="true"></span></label></div>';
                                            strc += '<div class="pointList">';
                                            // 第一段路, 第一个点为拐点, 下标从0开始, 倒数第二个结束
                                            for (var t = 0; t < routMap[i].length - 1; t++) {
                                                var routMapArray = routMap[i][t].split(';');
                                                var num = i + '' + t;
                                                strc += '<div class="form-group sectionLngLat">';
                                                strc += '<label class="col-md-2 control-label">经度：</label>';
                                                strc += '<div class="col-md-3 sectionLng">';
                                                strc += '<input type="text" id="piecewiseLng' + num + '" name="lng" placeholder="请输入经度值" value="' + routMapArray[0] + '" class="form-control" />';
                                                strc += '</div>';
                                                strc += '<label class="col-md-2 control-label">纬度：</label>';
                                                strc += '<div class="col-md-3 sectionLat">';
                                                strc += '<input type="text" id="piecewiseLat' + num + '" name="lat" placeholder="请输入纬度值" value="' + routMapArray[1] + '" class="form-control" />';
                                                strc += '</div>';
                                                if (t === 0) {
                                                    strc += '<button type="button" class="btn btn-primary addLngLat">';
                                                    strc += '<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>';
                                                    strc += '</button>'
                                                } else {
                                                    strc += '<button type="button" class="btn btn-danger removeLngLat">';
                                                    strc += '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>';
                                                    strc += '</button>'
                                                }
                                                strc += '</div>'
                                            }
                                            strc += '</div>';
                                            strc += '</div>';
                                        } else {
                                            str += '<li id="TabFenceBox"><a href="' + hrefs + '" data-toggle="tab">路段' + (i + 1) + '</a></li>';
                                            strc += '<div class="tab-pane" id="' + rids + '">';
                                            strc += '<div class="form-group hidden">';
                                            strc += '<div class="col-md-3">';
                                            strc += '<input  id="' + sectionlng + '"    name="longitude" value=""/>';
                                            strc += '</div>';
                                            strc += '<div class="col-md-3" >';
                                            strc += '<input id="' + sectionlat + '"   name="latitude" value=""/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label"><label class="text-danger">*</label> 偏移量(m)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入偏移量" value="' + width + '" onkeyup="value=value.replace(/[^0-9]/g,\'\') " class="form-control" id="sectionOffset' + i + '" name="offset"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">  路段行驶过长阈值(s)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段行驶过长阈值" value="0" class="form-control" id="sectionLongThreshold' + i + '" name="overlengthThreshold"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">路段行驶不足阈值(s)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段行驶不足阈值" value="0" class="form-control" id="sectionInsufficientThreshold' + i + '" name="shortageThreshold"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">路段最高速度（km/h）：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入路段最高速度" value="0" class="form-control" id="maxSpeed' + i + '" name="maximumSpeed"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">夜间最高速度(km/h)：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请选择夜间限速时间段" value="0" class="form-control" id="nightMaxSpeed' + i + '" name="nightMaxSpeed"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">夜间限速时间段：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input style="cursor: pointer;  background-color: #fafafa;" class="dateTime2 form-control layer-date laydate-icon valid" id="nightLimitTime' + i + '" name="nightLimitTime" readonly>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="form-group">';
                                            strc += '<label class="col-md-5 control-label">超速持续时间（s）：</label>';
                                            strc += '<div class=" col-md-5">';
                                            strc += '<input type="text" placeholder="请输入超速持续时间" value="0" class="form-control" id="durationSpeeding' + i + '" name="overspeedTime"/>';
                                            strc += '</div>';
                                            strc += '</div>';
                                            strc += '<div class="isQueryShow"><label>拐点数据 <span class="fa fa-chevron-up" aria-hidden="true"></span></label></div>';
                                            strc += '<div class="pointList">';
                                            if (i === routMap.length - 1) {
                                                // 最后一段路，最后一个点为拐点, 下标从1开始, 倒数第一个结束
                                                for (t = 1; t < routMap[i].length; t++) {
                                                    routMapArray = routMap[i][t].split(';');
                                                    num = i + '' + t;
                                                    strc += '<div class="form-group sectionLngLat">';
                                                    strc += '<label class="col-md-2 control-label">经度：</label>';
                                                    strc += '<div class="col-md-3 sectionLng">';
                                                    strc += '<input type="text" id="piecewiseLng' + num + '" name="lng" placeholder="请输入经度值" value="' + routMapArray[0] + '" class="form-control" />';
                                                    strc += '</div>';
                                                    strc += '<label class="col-md-2 control-label">纬度：</label>';
                                                    strc += '<div class="col-md-3 sectionLat">';
                                                    strc += '<input type="text" id="piecewiseLat' + num + '" name="lat" placeholder="请输入纬度值" value="' + routMapArray[1] + '" class="form-control" />';
                                                    strc += '</div>';
                                                    if (t === 1) {
                                                        strc += '<button type="button" class="btn btn-primary addLngLat">';
                                                        strc += '<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>';
                                                        strc += '</button>'
                                                    } else {
                                                        strc += '<button type="button" class="btn btn-danger removeLngLat">';
                                                        strc += '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>';
                                                        strc += '</button>'
                                                    }
                                                    strc += '</div>'
                                                }
                                            } else {
                                                // 中间路段，第一个点和最后一个点为分段点, 下标从1开始, 倒数第二个结束
                                                for (t = 1; t < routMap[i].length - 1; t++) {
                                                    routMapArray = routMap[i][t].split(';');
                                                    strc += '<div class="form-group sectionLngLat">';
                                                    strc += '<label class="col-md-2 control-label">经度：</label>';
                                                    strc += '<div class="col-md-3 sectionLng">';
                                                    strc += '<input type="number" type="number" min="73.66" max="135.05" name="lng" placeholder="请输入经度值" value="' + routMapArray[0] + '" class="form-control" />';
                                                    strc += '</div>';
                                                    strc += '<label class="col-md-2 control-label">纬度：</label>';
                                                    strc += '<div class="col-md-3 sectionLat">';
                                                    strc += '<input type="number" min="3.86" max="53.55" name="lat" placeholder="请输入纬度值" value="' + routMapArray[1] + '" class="form-control" />';
                                                    strc += '</div>';
                                                    if (t === 1) {
                                                        strc += '<button type="button" class="btn btn-primary addLngLat">';
                                                        strc += '<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>';
                                                        strc += '</button>'
                                                    } else {
                                                        strc += '<button type="button" class="btn btn-danger removeLngLat">';
                                                        strc += '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>';
                                                        strc += '</button>'
                                                    }
                                                    strc += '</div>'
                                                }
                                            }
                                            strc += '</div>';
                                            strc += '</div>';
                                        }
                                    }
                                    var indexValue = [];
                                    var pointsLat = [];
                                    var pointsLng = [];
                                    for (i = 0, len = lineAllPointArray.length; i < len; i++) {
                                        indexValue.push(i);
                                        pointsLat.push(lineAllPointArray[i][0]);
                                        pointsLng.push(lineAllPointArray[i][1]);
                                    }
                                    $("#pointSeqs").val(indexValue.join(','));
                                    $("#longitudes").val(pointsLat.join(','));
                                    $("#latitudes").val(pointsLng.join(','));
                                    $("#tenples").html(str);
                                    $("#pagecontent").html(strc);
                                    $(".addLngLat").unbind("click").bind("click", fenceOperation.addLngLat);
                                    $(".removeLngLat").unbind("click").bind("click", fenceOperation.removeLngLat);
                                    $(".isQueryShow").unbind("click").bind("click", fenceOperation.isQueryShow);
                                    $("#lineIDms1").attr("value", lineId);
                                    $("#sumn").attr("value", routMap.length);
                                    for (var i = 0; i < routMap.length; i++) {
                                        var sectionlng = "section-lng" + (i + 1);
                                        $("#" + sectionlng).attr("value", routMap[i] + "]");
                                        laydate.render({
                                            elem: '#nightLimitTime' + i
                                            , type: 'time'
                                            , range: '--'
                                            , format: 'HH:mm'
                                            , noSecond: true
                                            , trigger: 'click'
                                        });
                                    }
                                    pageLayout.closeVideo();
                                    $('#addMonitoringSection').modal('show');
                                }, function () {
                                });
                            }
                        });
                    } else {
                        if (map.currentMap === 'baidu') {
                            thisMap.setZoomAndCenter(16, {lng: e.latLng.lng, lat: e.latLng.lat});
                        } else {
                            thisMap.setZoomAndCenter(16, [e.lnglat.getLng(), e.lnglat.getLat()]);
                        }
                    }
                });
            }
        }
    },
    //删除关键点
    deleteKeyPoint: function (id) {
        infoWindow.close();
        var url = "/clbs/m/functionconfig/fence/bindfence/deleteKeyPoint";
        json_ajax("post", url, "json", false, {"kid": id}, fenceOperation.deleteKeyPointCallBack)
    },
    // 删除关键点更新围栏
    deleteKeyPointCallBack: function (data) {
        if (data.success === true) {
            fenceOperation.resetFance();
        } else {
            if (data.msg.toString().indexOf("系统错误") > -1) {
                layer.msg(data.msg, {move: false});
            }
        }
    },
    //重置返回
    resetSegment: function (data) {
        if (data.success) {
            fenceOperation.resetFance()
        } else {
            if (data.msg.toString().indexOf("系统错误") > -1) {
                layer.msg(data.msg, {move: false});
            }
        }
    },
    //新增修改成功重置围栏
    resetFance: function () {
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var lineFenceId = $("#lineId").val();
        var nodes = zTree.getNodesByParam('id', lineFenceId, null);
        // ajax访问后端查询
        layer.load(2);
        $.ajax({
            type: "POST",
            url: "/clbs/m/functionconfig/fence/bindfence/getFenceDetails",
            async: false,
            data: {
                "fenceNodes": JSON.stringify(nodes)
            },
            dataType: "json",
            success: function (data) {
                layer.closeAll('loading');
                if (data.success) {
                    var dataList = data.obj;
                    if (dataList != null && dataList.length > 0) {
                        if (dataList[0].fenceType === "zw_m_line") {
                            fanceID = dataList[0].fenceData[0].lineId;
                        }
                        for (var i = 0; i < dataList.length; i++) {
                            var fenceType = dataList[i].fenceType;
                            var fenceData = dataList[i].fenceData;
                            var lineSpot = dataList[i].lineSpot;
                            var lineSegment = dataList[i].lineSegment === undefined ? [] : dataList[i].lineSegment;
                            if (fenceType === "zw_m_marker") { // 标注
                                fenceOperation.drawMark(fenceData, map);
                            } else if (fenceType === "zw_m_line") { // 线
                                fenceOperation.drawLine(fenceData, lineSpot, lineSegment, map);
                            } else if (fenceType === "zw_m_rectangle") { // 矩形
                                fenceOperation.drawRectangle(fenceData, map);
                            } else if (fenceType === "zw_m_polygon") { // 多边形
                                fenceOperation.drawPolygon(fenceData, map);
                            } else if (fenceType === "zw_m_circle") { // 圆形
                                fenceOperation.drawCircle(fenceData, map);
                            }
                        }
                    }
                }
            }
        });
    },
    //矩形
    drawRectangle: function (rectangle, thisMap) {
        $("#LUPointLngLat").val(rectangle.leftLongitude + "," + rectangle.leftLatitude);
        $("#RDPointLngLat").val(rectangle.rightLongitude + "," + rectangle.rightLatitude);
        var rectangleId = rectangle.id;
        if (fenceIDMap.containsKey(rectangleId)) {
            var thisFence = fenceIDMap.get(rectangleId);
            thisFence.show();
            map.setFitView(thisFence);
        }
        else {
            var dataArr = new Array();
            if (rectangle != null) {
                dataArr.push([rectangle.leftLongitude, rectangle.leftLatitude]); // 左上角
                dataArr.push([rectangle.rightLongitude, rectangle.leftLatitude]); // 右上角
                dataArr.push([rectangle.rightLongitude, rectangle.rightLatitude]); // 右下角
                dataArr.push([rectangle.leftLongitude, rectangle.rightLatitude]); // 左下角
            }
            polyFence = map.polygon({
                path: dataArr,//设置多边形边界路径
                strokeColor: "#FF33FF", //线颜色
                strokeOpacity: 0.2, //线透明度
                strokeWeight: 3, //线宽
                fillColor: "#1791fc", //填充色
                fillOpacity: 0.35,
                //填充透明度
                map: thisMap,
            });
            // polyFence.setMap(thisMap);
            thisMap.setFitView(polyFence);
            fenceIDMap.put(rectangleId, polyFence);
        }
    },
    //多边形
    drawPolygon: function (polygon, thisMap) {
        var polygonId = polygon[0].polygonId;
        if (fenceIDMap.containsKey(polygonId)) {
            var thisFence = fenceIDMap.get(polygonId);
            thisFence.hide();
            fenceIDMap.remove(polygonId);
        }
        if (PolyEditorMap.containsKey(polygonId)) {
            var mapEditFence = PolyEditorMap.get(polygonId);
            mapEditFence.close();
        }
        map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendPolygon);
        var dataArr = new Array();
        if (polygon != null && polygon.length > 0) {
            for (var i = 0; i < polygon.length; i++) {
                dataArr.push([polygon[i].longitude, polygon[i].latitude]);
            }
        }
        var html = '';
        for (var i = 0; i < dataArr.length; i++) {
            html += '<div class="form-group">'
                + '<label class="col-md-3 control-label">顶点' + (i + 1) + '经纬度：</label>'
                + '<div class=" col-md-8">'
                + '<input type="text" placeholder="请输入顶点经纬度" value="' + dataArr[i][0] + "," + dataArr[i][1] + '" class="form-control rectangleAllPointLngLat"/>'
                + '</div>'
                + '</div>'
        }
        $("#rectangleAllPointShow").html(html);
        polyFence = map.polygon({
            path: dataArr,//设置多边形边界路径
            strokeColor: "#FF33FF", //线颜色
            strokeOpacity: 0.2, //线透明度
            strokeWeight: 3, //线宽
            fillColor: "#1791fc", //填充色
            fillOpacity: 0.35,
            //填充透明度
            map: thisMap,
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIDMap.put(polygonId, polyFence);
    },
    //圆形
    drawCircle: function (circle, thisMap) {
        var circleId = circle.id;
        if (fenceIDMap.containsKey(circleId)) {
            var thisFence = fenceIDMap.get(circleId);
            thisFence.hide();
            fenceIDMap.remove(circleId);
        }
        if (PolyEditorMap.containsKey(circleId)) {
            var mapEditFence = PolyEditorMap.get(circleId);
            mapEditFence.close();
        }
        map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendCircle);
        polyFence = map.circle({
            center: map.lngLat(circle.longitude, circle.latitude),// 圆心位置
            radius: circle.radius, //半径
            strokeColor: "#F33", //线颜色
            strokeOpacity: 1, //线透明度
            strokeWeight: 3, //线粗细度
            fillColor: "#ee2200", //填充颜色
            fillOpacity: 0.35,
            //填充透明度
            map: thisMap,
        });
        polyFence.setMap(thisMap);
        thisMap.setFitView(polyFence);
        fenceIDMap.put(circleId, polyFence);
    },
    // 树结构  围栏类型旁新增按钮
    addHoverDom: function (treeId, treeNode) {
        if (treeNode.id === 'zw_m_travel_layer' || treeNode.pId === 'zw_m_travel_layer') {
            return false;
        }
        // 树节点的类型
        var nodeType = treeNode.type;
        // 权限
        var permissionValue = $('#permission').val();

        if (nodeType != null && nodeType !== undefined && nodeType !== "" && nodeType === "fenceParent") {
            var sObj = $("#" + treeNode.tId + "_span");
            var theImport = $("#" + treeNode.tId + "_span");
            if (treeNode.editNameFlag || $("#addBtn_" + treeNode.tId).length > 0) {
                return;
            }
            var id = (100 + newCount);
            var pid = treeNode.id;
            var addStr = "<span class='button add' id='addBtn_"
                + treeNode.tId
                + "' title='新增'></span>";
            var tImport;
            if (pid === "zw_m_line") {
                tImport = "<a class='button import' id='import_" + treeNode.tId + "' href='/clbs/v/monitoring/import?pid=" + pid + "' data-toggle='modal' data-target='#commonSmWin' title='导入'></a>";
            } else if (pid === "zw_m_polygon") {
                tImport = "<a class='button import' id='import_" + treeNode.tId + "' href='/clbs/v/monitoring/import?pid=" + pid + "' data-toggle='modal' data-target='#commonSmWin' title='导入'></a>";
            }
            // 判断是否有可写权限
            if (permissionValue === "true") {
                theImport.after(tImport);
                sObj.after(addStr);
            }
            var btn = $("#addBtn_" + treeNode.tId);
            if (btn) {
                btn.bind("click", function () {
                    mouseToolEdit.close(true);
                    amapOperation.clearLabel();
                    isAddDragRoute = false;
                    $('#drivenRoute').hide();
                    $('.lngLat_show').children('span').attr('class', 'fa fa-chevron-up');
                    $('.pointList').hide();
                    $(".fenceA i").removeClass("active");
                    $(".fenceA span").css('color', '#5c5e62');
                    isAddFlag = true;
                    isAreaSearchFlag = false;
                    if (treeNode.name === "标注") {
                        layer.msg('请在地图上点出标注点', {time: 1000});
                        fenceOperation.clearMapMarker();
                        mouseTool.marker({offset: map.pixel(-9, -23)});
                    } else if (treeNode.name === "路线") {
                        layer.msg('请在地图上画出路线', {time: 1000});
                        isDistanceCount = false;
                        fenceOperation.clearLine();
                        mouseTool.polyline();
                    } else if (treeNode.name === "矩形") {
                        layer.msg('请在地图上画出矩形', {time: 1000});
                        fenceOperation.clearRectangle();
                        mouseTool.rectangle();
                        clickRectangleFlag = true;
                    } else if (treeNode.name === "圆形") {
                        layer.msg('请在地图上画出圆形', {time: 1000});
                        fenceOperation.clearCircle();
                        mouseTool.circle();
                    } else if (treeNode.name === "多边形") {
                        layer.msg('请在地图上画出多边形', {time: 1000});
                        fenceOperation.clearPolygon();
                        mouseTool.polygon();
                        clickRectangleFlag = false;
                    } else if (treeNode.name === '导航路线') {
                        $('#drivenRoute').show();
                        fenceOperation.addItem();
                    } else if (treeNode.name === '行政区划') {
                        $("#administrationName").val("");
                        $("#administrationDistrict").val("");
                        $('#province').val('--请选择--');
                        document.getElementById('city').innerHTML = '';
                        document.getElementById('district').innerHTML = '';
                        pageLayout.closeVideo();
                        $('#administrativeArea').modal('show');
                    }
                    return false;
                });
            }
        } else if (nodeType != null && nodeType !== undefined && nodeType !== "" && nodeType === "fence") {
            var sEdit = $("#" + treeNode.tId + "_span");
            var sDetails = $("#" + treeNode.tId + "_span");
            var deleteList = $("#" + treeNode.tId + "_span");
            var sBind = $("#" + treeNode.tId + "_span");
            if (treeNode.editNameFlag || $("#editBtn_" + treeNode.tId).length > 0) {
                return;
            }
            var detailsStr = "<span class='button binds' id='detailsBtn_"
                + treeNode.tId
                + "' title='详情'></span>";
            var bindStr = "<span class='button details' id='bindBtn_"
                + treeNode.tId
                + "' title='绑定'></span>";
            var editStr = '';
            if (treeNode.pId !== "zw_m_administration") {
                editStr = "<span class='button edit' id='editBtn_"
                    + treeNode.tId
                    + "' title='修改' ></span>";
            } else {
                editStr = "<span id='editBtn_"
                    + treeNode.tId
                    + "' title='修改' ></span>";
            }
            var deleteStr = "<span class='button remove' id='deleteBtn_"
                + treeNode.tId
                + "' title='删除' ></span>";
            // 判断是否有可写权限
            if (permissionValue === "true") {
                deleteList.after(deleteStr);
                sDetails.after(detailsStr);
                sEdit.after(editStr);
                sBind.after(bindStr);
            }
            var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
            var editBtn = $("#editBtn_" + treeNode.tId);
            if (editBtn) {
                editBtn.bind("click", function () {
                    amapOperation.clearLabel();
                    //关闭其它围栏修改功能
                    fenceOperation.closeFenceEdit();
                    isAddDragRoute = false;
                    $('#drivenRoute').hide();
                    $('.lngLat_show').children('span').attr('class', 'fa fa-chevron-up');
                    $('.pointList').hide();
                    mouseToolEdit.close(true);
                    isEdit = false;
                    isAddFlag = false;
                    isAreaSearchFlag = false;
                    var value = treeNode.id + "#" + treeNode.pId;
                    zTree.checkNode(treeNode, true, true);
                    treeNode.checkedOld = true;
                    fenceOperation.updateFence(value);
                    return false;
                });
            }
            var bindBtn = $("#bindBtn_" + treeNode.tId);
            if (bindBtn) {
                bindBtn.bind("click", function () {
                    isAddDragRoute = false;
                    $('#drivenRoute').hide();
                    trid = [];
                    fenceOperation.fenceBind(treeNode.pId, treeNode.name, treeNode.fenceInfoId, treeNode.id);
                    return false;
                });
            }
            var deleteBtn = $("#deleteBtn_" + treeNode.tId);
            if (deleteBtn) {
                deleteBtn.bind("click", function () {
                    //fenceOperation.hideFence(treeNode.id);
                    isAddDragRoute = false;
                    $('#drivenRoute').hide();
                    infoWindow.close();
                    fenceOperation.deleteFence(treeNode);
                    //fenceOperation.fenceHidden(treeNode.id);

                    return false;
                });
            }
            var detailsBtn = $("#detailsBtn_" + treeNode.tId);
            if (detailsBtn) {
                detailsBtn.bind("click", function () {
                    isAddDragRoute = false;
                    $('#drivenRoute').hide();
                    $("#detailsFenceName").text(treeNode.name);
                    var value = treeNode.id + "#" + treeNode.pId + "#" + true;
                    fenceOperation.updateFence(value);
                    pageLayout.closeVideo();
                    $("#detailsModel").modal('show');
                    return false;
                })
            }
        }
    },
    // 删除电子围栏按钮
    removeHoverDom: function (treeId, treeNode) {
        // 树节点的类型
        var nodeType = treeNode.type;
        if (nodeType != null && nodeType !== undefined && nodeType !== "" && nodeType === "fenceParent") {
            $("#addBtn_" + treeNode.tId).unbind().remove();
            $("#import_" + treeNode.tId).unbind().remove();
        } else if (nodeType != null && nodeType !== undefined && nodeType !== "" && nodeType === "fence") {
            $("#editBtn_" + treeNode.tId).unbind().remove();
            $("#detailsBtn_" + treeNode.tId).unbind().remove();
            $("#bindBtn_" + treeNode.tId).unbind().remove();
            $("#deleteBtn_" + treeNode.tId).unbind().remove();
        }
    },
    //关键点
    doSubmits1: function () {
        if (fenceOperation.validate_key()) {
            $("#monitoringTag").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#addMonitoringTag").modal("hide");
                    $("#monitoringTag").clearForm();
                    fenceOperation.resetFance();
                } else {
                    layer.msg(data.msg, {move: false});
                }
            });
        }
    },
    //分段监控
    doSubmitsMonitor: function () {
        fenceOperation.sectionRateLimitLngLat();
        fenceOperation.sectionThreadSave();
        if (fenceOperation.validate_Monitor()) {
            $("#monitoringSection").ajaxSubmit(function (data) {
                data = JSON.parse(data);
                if (data.success) {
                    $("#addMonitoringSection").modal("hide");
                    $("#monitoringSection").clearForm();
                    fenceOperation.resetFance();
                } else {
                    layer.msg(data.msg, {move: false});
                }
            });
        }
    },
    //分段限速提交前将修改后的经纬度塞值
    sectionRateLimitLngLat: function () {
        var thisLineID = $("#lineId").val();
        fenceOperation.clearLine();
        $("#lineId").val(thisLineID);
        var tableLength = $("#tenples li").length;
        var lineLng = '';
        var lineLat = '';
        var index = -1;
        var indexValue = '';
        for (var i = 0; i < tableLength; i++) {
            var id = 'route' + (i + 1);
            var listLength = $("#" + id).children('div.pointList').children('div.sectionLngLat').length;
            var value = '';
            if (i < tableLength - 1) {
                for (var y = 0; y < listLength; y++) {
                    var getLng = $("#" + id).children('div.pointList').children('div.sectionLngLat:eq(' + y + ')').children('.sectionLng').children('input');
                    var lng = getLng.val() === '' ? getLng.attr('value') : getLng.val();
                    var getLat = $("#" + id).children('div.pointList').children('div.sectionLngLat:eq(' + y + ')').children('.sectionLat').children('input');
                    var lat = getLat.val() === '' ? getLat.attr('value') : getLat.val();
                    if (lng !== '' && lat !== '') {
                        lineLng += lng + ",";
                        lineLat += lat + ",";
                        index++;
                        indexValue += index + ",";
                        value += lng + ";" + lat + ",";
                    }
                }
                var nextID = 'route' + (i + 2);
                var nextLngID = $("#" + nextID).children('div.pointList').children('div.sectionLngLat:eq(0)').children('.sectionLng').children('input');
                var nextLng = nextLngID.val() === '' ? nextLngID.attr('value') : nextLngID.val();
                var nextLatID = $("#" + nextID).children('div.pointList').children('div.sectionLngLat:eq(0)').children('.sectionLat').children('input');
                var nextLat = nextLatID.val() === '' ? nextLatID.attr('value') : nextLatID.val();
                if (nextLng !== '' && nextLat !== '') {
                    value += nextLng + ";" + nextLat + "]";
                }
                var section = 'section-lng' + (i + 1);
                //$("#" + section).attr('value', value);
            } else {
                for (var y = 0; y < listLength; y++) {
                    var getLng = $("#" + id).children('div.pointList').children('div.sectionLngLat:eq(' + y + ')').children('.sectionLng').children('input');
                    var lng = getLng.val() === '' ? getLng.attr('value') : getLng.val();
                    var getLat = $("#" + id).children('div.pointList').children('div.sectionLngLat:eq(' + y + ')').children('.sectionLat').children('input');
                    var lat = getLat.val() === '' ? getLat.attr('value') : getLat.val();
                    if (y !== listLength - 1) {
                        if (lng !== '' && lat !== '') {
                            lineLng += lng + ",";
                            lineLat += lat + ",";
                            index++;
                            indexValue += index + ",";
                            value += lng + ";" + lat + ",";
                        }
                    } else {
                        if (lng !== '' && lat !== '') {
                            lineLng += lng + ",";
                            lineLat += lat + ",";
                            index++;
                            indexValue += index + ",";
                            value += lng + ";" + lat + "]";
                        }
                    }
                }
                section = 'section-lng' + (i + 1);
                //$("#" + section).attr('value', value);
            }
        }
        lineLng = lineLng.substring(0, lineLng.length - 1);
        lineLat = lineLat.substring(0, lineLat.length - 1);
        indexValue = indexValue.substring(0, indexValue.length - 1);
        // $("#pointSeqs").val(indexValue);
        // $("#longitudes").val(lineLng);
        // $("#latitudes").val(lineLat);
        fenceOperation.editLngLat();
    },
    //分段限速修改后的经纬度提交
    editLngLat: function () {
        var thisId = $("#lineId").val();
        $("#addOrUpdateLineFlag").val("1");
        var thisData = thisId + "#" + "zw_m_line";
        var thisParams = {"fenceIdShape": thisData};
        var url = "/clbs/m/functionconfig/fence/managefence/previewFence";
        ajax_submit("POST", url, "json", false, thisParams, true, fenceOperation.editCallBack);
    },
    editCallBack: function (data) {
        if (data.success) {
            var datalist = data.obj[0];
            var text = datalist.line.description;
            var type = datalist.line.type;
            var width = datalist.line.width;
            var name = datalist.line.name;
            $("#lineWidth1").val(width);
            $("#lineDescription1").val(text);
            $("#lineType1").val(type);
            $("#lineName1").val(name);
        } else {
            layer.msg(data.msg, {move: false});
        }
    },
    //关键点验证
    validate_key: function () {
        return $("#monitoringTag").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                arriveTime: {
                    required: true
                },
                leaveTime: {
                    required: true,
                    compareDate: "#arriveTime"
                },
                description: {
                    maxlength: 100
                }
            },
            messages: {
                name: {
                    required: fenceOperationPointNameNull,
                    maxlength: publicSize20
                },
                arriveTime: {
                    required: fenceOperationArriveTimeSelect,
                },
                leaveTime: {
                    required: fenceOperationLeaveTimeSelect,
                    compareDate: fenceOperationAlTimeCheck
                },
                description: {
                    maxlength: publicSize100
                }
            }
        }).form();
    },
    //路段验证
    validate_Monitor: function () {
        return $("#monitoringSection").validate({
            ignore: '',
            rules: {
                offset: {
                    required: true,
                    range: [0, 255],
                },
                overlengthThreshold: {
                    required: false,
                    range: [0, 65535],
                    digits: true
                },
                shortageThreshold: {
                    required: false,
                    range: [0, 65535],
                    digits: true
                },
                maximumSpeed: {
                    required: true,
                    range: [0, 65535],
                },
                nightMaxSpeed: {
                    range: [0, 65535],
                },
                overspeedTime: {
                    required: true,
                    range: [0, 65535],
                    digits: true
                },
                lng: {
                    required: true,
                    range: [73.66, 135.05],
                },
                lat: {
                    required: true,
                    range: [3.86, 53.55],
                },
            },
            messages: {
                offset: {
                    required: fenceOperationOffsetNull,
                    maxlength: fenceOperationScope255
                },
                overlengthThreshold: {
                    required: fenceOperationOverLength,
                    range: fenceOperationScope65535,
                    digits: requiredInt
                },
                shortageThreshold: {
                    required: fenceOperationTooShort,
                    range: fenceOperationScope65535,
                    digits: requiredInt
                },
                maximumSpeed: {
                    required: fenceOperationMaxSpeed,
                    range: fenceOperationScope65535,
                },
                nightMaxSpeed: {
                    range: fenceOperationScope65535,
                },
                overspeedTime: {
                    required: fenceOperationOverSpeedTime,
                    range: fenceOperationScope65535,
                    digits: requiredInt
                },
                lng: {
                    required: fenceOperationLongitudeNull,
                    range: fenceOperationLongitudeScope,
                },
                lat: {
                    required: fenceOperationLatitudeNull,
                    range: fenceOperationLatitudeScope,
                },
            }
        }).form();
    },
    //图形画完回调事件
    createSuccess: function (data) {
        console.log(data, data)
        //区域查车成功后
        if ($("#queryClick i").hasClass("active")) {
            changeArray = data.obj.getBounds();
            var url = "/clbs/v/monitoring/regionalQuery";
            ajax_submit("POST", url, "json", true, null, true, fenceOperation.regionalQuery);
        }
        //标注
        if (data.obj.CLASS_NAME === "AMap.Marker") {
            $("#addOrUpdateMarkerFlag").val("0");
            var marker = data.obj.getPosition();
            $("#mark-lng").attr("value", marker.lng);
            $('#markerLng').val(marker.lng);
            $("#mark-lat").attr("value", marker.lat);
            $('#markerLat').val(marker.lat);
            pageLayout.closeVideo();
            $("#mark").modal('show');
        }
        //圆
        if (data.obj.CLASS_NAME === "Overlay.Circle") {
            $("#addOrUpdateCircleFlag").val("0");
            var center;
            if(Array.isArray(data.obj.getCenter())){
                center = {lng : data.obj.getCenter()[0], lat : data.obj.getCenter()[1]}
            }else{
                center = data.obj.getCenter()
            }
            var radius = data.obj.getRadius();
            $("#circle-lng").attr("value", center.lng);
            $("#circle-lat").attr("value", center.lat);
            $("#circle-radius").attr("value", radius);
            $("#editCircleLng").val(center.lng);
            $("#editCircleLat").val(center.lat);
            $("#editCircleRadius").val(radius);
            pageLayout.closeVideo();
            $("#circleArea").modal('show');
        }
        if (data.obj.CLASS_NAME === "Overlay.Polyline" || data.obj.CLASS_NAME === "Overlay.Polygon" || data.obj.CLASS_NAME === "Overlay.Rectangle") {
            var pointSeqs = ""; // 点序号
            var longitudes = ""; // 所有的经度
            var latitudes = ""; // 所有的纬度
            var array = new Array();
            if (data.obj.CLASS_NAME === "Overlay.Rectangle" && data.obj.aE) {
                console.log(data.obj.getOptions());
                array = data.obj.aE[0][0];
            } else {
                var path = data.obj.getPath();
                for (var i = 0; i < path.length; i++) {
                    array.push([path[i].lng, path[i].lat]);
                }
            }

            // 去除array中相邻的重复点
            array = fenceOperation.removeAdjoinRepeatPoint(array);
            var fileinfo = "";
            for (var i = 0; i < array.length; i++) {
                fileinfo += '<tr>';
                fileinfo += '<td>' + i + '</td>';
                fileinfo += '<td>' + 'aa' + '</td>';
                fileinfo += '<td>' + 'bb' + '</td>';
                fileinfo += '</tr>';
            }
            $('#tal').html(fileinfo);

            var strc = '<div class="isQueryShow" style="padding: 0 30px"><label>拐点数据 <span class="fa fa-chevron-up" aria-hidden="true"></span></label></div>';
            strc += '<div class="pointList" style="padding-right: 30px">';
            //矩形判断
            for (i = 0; i < array.length; i++) {
                $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(2)").text(array[i][0]);
                $("#table-lng-lat tbody tr:nth-child(" + parseInt(i + 1) + ")").children("td:nth-child(3)").text(array[i][1]);
                pointSeqs += i + ","; // 点序号
                longitudes += array[i][0] + ","; // 把所有的经度组合到一起
                latitudes += array[i][1] + ","; // 把所有的纬度组合到一起

                var num = i;
                strc += '<div class="form-group sectionLngLat">';
                strc += '<label class="col-md-2 control-label">经度：</label>';
                strc += '<div class="col-md-3 sectionLng">';
                strc += '<input type="text" id="piecewiseLng' + num + '" name="lng" placeholder="请输入经度值" value="' + array[i][0] + '" class="form-control" />';
                strc += '</div>';
                strc += '<label class="col-md-2 control-label">纬度：</label>';
                strc += '<div class="col-md-3 sectionLat">';
                strc += '<input type="text" id="piecewiseLat' + num + '" name="lat" placeholder="请输入纬度值" value="' + array[i][1] + '" class="form-control" />';
                strc += '</div>';
                strc += '<button type="button" class="btn btn-primary addLngLat">';
                strc += '<span class="glyphicon glyphicon-plus" aria-hidden="true"></span>';
                strc += '</button>&ensp;';
                strc += '<button type="button" class="btn btn-danger removeLngLat">';
                strc += '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>';
                strc += '</button>';
                strc += '</div>'
            }
            $('#inflectionPointBox').html(strc);
            $(".addLngLat").unbind("click").bind("click", fenceOperation.newAddLngLat);
            $(".removeLngLat").unbind("click").bind("click", fenceOperation.newRemoveLngLat);
            $(".isQueryShow").unbind("click").bind("click", fenceOperation.isQueryShow);

            // 去掉点序号、经度、纬度最后的一个逗号
            if (pointSeqs.length > 0) {
                pointSeqs = pointSeqs.substr(0, pointSeqs.length - 1);
            }
            if (longitudes.length > 0) {
                longitudes = longitudes.substr(0, longitudes.length - 1);
            }
            if (latitudes.length > 0) {
                latitudes = latitudes.substr(0, latitudes.length - 1);
            }
            $("#pointSeqs").val(pointSeqs);
            $("#longitudes").val(longitudes);
            $("#latitudes").val(latitudes);
            $("#pointSeqsRectangles").val(pointSeqs);
            $("#longitudesRectangles").val(longitudes);
            $("#latitudesRectangles").val(latitudes);
            $("#pointSeqsPolygons").val(pointSeqs);
            $("#longitudesPolygons").val(longitudes);
            $("#latitudesPolygons").val(latitudes);

            //线
            if (data.obj.CLASS_NAME === "Overlay.Polyline" && !isDistanceCount) {
                $("#addOrUpdateLineFlag").val("0");
                pageLayout.closeVideo();
                $("#addLine").modal('show');
            }
            //矩形
            if (data.obj.CLASS_NAME === "Overlay.Rectangle" && clickRectangleFlag && isAddFlag) {
                if (!isAreaSearchFlag) {
                    if (array.length < 4) {
                        return false;
                    }
                    $("#LUPointLngLat").val(array[0][0] + "," + array[0][1]);
                    $("#RDPointLngLat").val(array[2][0] + "," + array[2][1]);
                    $("#addOrUpdateRectangleFlag").val("0");
                    pageLayout.closeVideo();
                    $("#rectangle-form").modal('show');

                }
            }
            //多边形
            if (data.obj.CLASS_NAME === "Overlay.Polygon" && !clickRectangleFlag && isAddFlag) {
                if (!$("#queryClick i").hasClass("active")) {
                    var html = '';
                    for (var i = 0; i < array.length; i++) {
                        html += '<div class="form-group">'
                            + '<label class="col-md-3 control-label">顶点' + (i + 1) + '经纬度：</label>'
                            + '<div class=" col-md-8">'
                            + '<input type="text" placeholder="请输入顶点经纬度" value="' + array[i][0] + "," + array[i][1] + '" class="form-control rectangleAllPointLngLat"/>'
                            + '</div>'
                            + '</div>'
                    }
                    $("#rectangleAllPointShow").html(html);
                    $("#addOrUpdatePolygonFlag").val("0");
                    pageLayout.closeVideo();
                    $("#myModal").modal('show');
                }
            }
        }
    },
    //添加拐点
    newAddLngLat: function () {
        var pointList = $(this).closest('.pointList');
        var curBox = $(this).parent('div.sectionLngLat');
        var thisArea = curBox.clone(true);
        var num = pointList.find('div.sectionLngLat').length;
        thisArea.find('input').val('');
        thisArea.find('input').eq(0).attr('id', 'piecewiseLng' + num);
        thisArea.find('input').eq(1).attr('id', 'piecewiseLat' + num);
        curBox.after(thisArea);
        $(".addLngLat").unbind("click").bind("click", fenceOperation.newAddLngLat);
        $(".removeLngLat").unbind("click").bind("click", fenceOperation.newRemoveLngLat);

        if (num > 1) {
            pointList.find('button.removeLngLat').removeAttr('disabled');
        }
    },
    //删除拐点
    newRemoveLngLat: function () {
        $(this).parent('.sectionLngLat').remove();
        var num = $('.pointList').find('div.sectionLngLat').length;
        if (num < 3) {
            $('.pointList').find('button.removeLngLat').prop('disabled', true);
        }
    },
    regionalQuery: function (data) {
        $("#dataTable tbody").html('');
        var objRegional = data.obj;
        var isHasCar = false;
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        var html = '';
        var param = [];
        var select = [];//去重
        var sum = 0;
        for (var i = 0; i < objRegional.length; i++) {
            var longitude = objRegional[i][2];
            var latitude = objRegional[i][1];
            var flagssss = changeArray.contains([longitude, latitude]);
            if (flagssss === true) { // 判断车辆是否在指定区域内
                isHasCar = true;
                var carMsgID = objRegional[i][0];//车辆ID
                if (select.toString().indexOf(carMsgID) === -1) {
                    sum++;
                    var carName = objRegional[i][4];
                    var carGroup = objRegional[i][3];
                    if (cheakdiyuealls.length < MAX_SUBSCRIBTION_LENGTH) {
                        var nodes = zTree.getNodesByParam("id", carMsgID, null);
                        crrentSubV.push(objRegional[i][0]);
                        crrentSubName.push(objRegional[i][4]);

                        for (var j = 0; j < nodes.length; j++) {
                            // crrentSubV.push(objRegional[i][0]);
                            // crrentSubName.push(objRegional[i][4]);
                            zTree.checkNode(nodes[j], true, true);
                            nodes[j].checkedOld = true;
                            zTree.updateNode(nodes[j]);
                        }
                        cheakdiyuealls.push(carMsgID);
                        param.push(carMsgID);
                    }

                    html += '<tr><td>' + carName + '</td><td>' + carGroup + '</td></tr>';
                    select.push(carMsgID);
                }
            }
        }
        if (sum > MAX_SUBSCRIBTION_LENGTH) {
            $("#sumRegionalQuery").text("共计" + sum + "个监控对象,已订阅" + MAX_SUBSCRIBTION_LENGTH + "个");
        } else {
            $("#sumRegionalQuery").text("共计" + sum + "个监控对象！");
        }
        $("#dataTable tbody").html(html);
        if (isHasCar) {
            pageLayout.closeVideo();
            $("#areaSearchCar").modal('show');
            var requestStrS = {
                "desc": {
                    "MsgId": 40964,
                    "UserName": $("#userName").text()
                },
                "data": param
            };
            cancelList = [];
            isAreaSearch = true;
            webSocket.subscribe(headers, "/user/topic/location", dataTableOperation.updateRealLocation, "/app/location/subscribe", requestStrS);
        } else {
            layer.msg(trackAreaMonitorNull);
            mouseTool.close(true);
            $("#queryClick i").removeClass("active");
            $("#queryClick span").css('color', '#5c5e62');
        }
    },
    //去除array中相邻的重复点
    removeAdjoinRepeatPoint: function (array) {
        //去除array中相邻的重复点
        var tempArray = [];
        if (array != null && array.length > 1) {
            tempArray.push([array[0][0], array[0][1]]);
            for (var i = 1; i < array.length; i++) {
                var templongtitude = array[i][0];
                var templatitude = array[i][1];
                if (templongtitude === array[i - 1][0] && templatitude === array[i - 1][1]) {
                    continue;
                } else {
                    tempArray.push([templongtitude, templatitude]);
                }
            }
            array = tempArray;
        }
        return array;
    },
    //清空标注
    clearMapMarker: function () {
        $("#addOrUpdateMarkerFlag").val("0");
        $("#markerId").val("");
        $("#markerName").val("");
        $("#markerDescription").val("");
        $("#mark-lng").attr("value", "");
        $("#mark-lat").attr("value", "");
    },
    //清空线路
    clearLine: function () {
        $("#addOrUpdateLineFlag").val("0");
        $("#lineId").val("");
        $("#lineName1").val("");
        $("#lineWidth1").val("");
        $("#lineDescription1").val("");
        $("#pointSeqs").val("");
        $("#longitudes").val("");
        $("#latitudes").val("");
    },
    //清空矩形
    clearRectangle: function () {
        $("#addOrUpdateRectangleFlag").val("0");
        $("#rectangleId").val("");
        $("#rectangleName").val("");
        $("#rectangleDescription").val("");
        $("#pointSeqsRectangles").val("");
        $("#longitudesRectangles").val("");
        $("#latitudesRectangles").val("");
    },
    //清空多边形
    clearPolygon: function () {
        $("#addOrUpdatePolygonFlag").val("0");
        $("#polygonId").val("");
        $("#polygonName").val("");
        $("#polygonDescription").val("");
        $("#pointSeqsPolygons").val("");
        $("#longitudesPolygons").val("");
        $("#latitudesPolygons").val("");
    },
    //清空圆形
    clearCircle: function () {
        $("#addOrUpdateCircleFlag").val("0");
        $("#circleId").val("");
        $("#circleName").val("");
        $("#circleDescription").val("");
        $("#circle-lng").attr("value", "");
        $("#circle-lat").attr("value", "");
        $("#circle-radius").attr("value", "");
    },
    searchCarClose: function () {
        $("#areaSearchCar").modal('hide');
        $("#queryClick i").removeClass("active");
        $("#queryClick span").css('color', '#5c5e62');
        mouseTool.close(true);
    },
    //标注保存
    annotatedSave: function (thisBtn) {
        if (fenceOperation.validate_marker()) {
            thisBtn.disabled = true;
            $('#mark-lng').attr('value', $('#markerLng').val());
            $('#mark-lat').attr('value', $('#markerLat').val());
            $("#marker").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    saveFenceName = $('#markerName').val();
                    saveFenceType = 'zw_m_marker';
                    $("#mark").modal("hide");
                    mouseTool.close(true);
                    var markFenceID = $("#markerId").val();
                    fenceOperation.addNodes();
                    var markFence = fenceIDMap.get(markFenceID);
                    if (markFence !== undefined) {
                        markFence.setDraggable(false);
                    }
                    var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
                    var nodes = zTree.getNodesByParam("id", markFenceID, null);
                    fenceOperation.getFenceDetail(nodes, map);
                } else {
                    if (datas.msg == null) {
                        layer.msg(fenceOperationLableExist);
                    } else if (datas.msg != null) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
        }
    },
    //标注添加时验证
    validate_marker: function () {
        return $("#marker").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 10
                },
                description: {
                    maxlength: 255
                },
                groupName: {
                    required: true,
                },
                markerLngName: {
                    required: true,
                    range: [1, 200],
                },
                markerLatName: {
                    required: true,
                    range: [1, 200],
                },
            },
            messages: {
                name: {
                    required: markerNameNull,
                    maxlength: publicSize10
                },
                description: {
                    maxlength: publicSize255
                },
                groupName: {
                    required: publicSelectGroupNull,
                },
                markerLngName: {
                    required: fenceOperationLoAlaNull,
                    range: markerLngLatErr,
                },
                markerLatName: {
                    required: fenceOperationLoAlaNull,
                    range: markerLngLatErr,
                },
            }
        }).form();
    },
    //线保存
    threadSave: function (thisBtn) {
        var pointList = $('#addLineForm .pointList');
        if (pointList.find('label.error').length > 0) {
            pointList.show();
            pointList.siblings('.isQueryShow').find('span').attr('class', 'fa fa-chevron-down');
        }
        if (fenceOperation.validate_line()) {
            layer.load(2);
            thisBtn.disabled = true;

            // 拼装新的经纬度值
            var longitudes = [];
            var latitudes = [];
            var numStr = [];
            var sectionLngLat = $('#addLineForm').find('.sectionLngLat');
            for (var i = 0, len = sectionLngLat.length; i < len; i++) {
                var long = $(sectionLngLat[i]).find('input').eq(0).val();
                var lat = $(sectionLngLat[i]).find('input').eq(1).val();
                longitudes.push(long);
                latitudes.push(lat);
                numStr.push(i);
            }
            $("#pointSeqs").val(numStr.join(','));
            $("#longitudes").val(longitudes.join(','));
            $("#latitudes").val(latitudes.join(','));
            $("#pointSeqsRectangles").val(numStr.join(','));
            $("#longitudesRectangles").val(longitudes.join(','));
            $("#latitudesRectangles").val(latitudes.join(','));
            $("#pointSeqsPolygons").val(numStr.join(','));
            $("#longitudesPolygons").val(longitudes.join(','));
            $("#latitudesPolygons").val(latitudes.join(','));


            $("#addLineForm").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    isEdit = true;
                    $("#addLine").modal("hide");
                    saveFenceName = $('#lineName1').val();
                    saveFenceType = 'zw_m_line';
                    mouseTool.close(true);
                    var lineId = $("#lineId").val();
                    var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
                    var node = zTree.getNodesByParam("id", lineId, null);
                    fenceOperation.getFenceDetail(node, map);
                    fenceOperation.addNodes();
                    map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendLine);
                    if (PolyEditorMap.containsKey(lineId)) {
                        var mapEditFence = PolyEditorMap.get(lineId);
                        if (Array.isArray(mapEditFence)) {
                            for (var i = 0; i < mapEditFence.length; i++) {
                                mapEditFence[i].close();
                            }
                        } else {
                            mapEditFence.close();
                        }
                    }
                } else {
                    if (datas.msg == null) {
                        layer.msg(fenceOperationLineExist);
                        layer.closeAll('loading');
                    } else if (datas.msg != null) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
        }
    },
    sectionThreadSave: function () {
        if (fenceOperation.validate_line()) {
            $("#addLineForm").ajaxSubmit();
        }
    },
    //线路添加时验证
    validate_line: function () {
        return $("#addLineForm").validate({
            ignore: '',
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                width: {
                    required: true,
                    range: [0, 255],
                },
                description: {
                    maxlength: 100
                },
                groupName: {
                    required: true,
                },
                lng: {
                    required: true,
                    number: true,
                    range: [73.66, 135.05]
                },
                lat: {
                    required: true,
                    number: true,
                    range: [3.86, 53.55]
                }
            },
            messages: {
                lng: {
                    required: "请输入经度值",
                    number: '请输入73.66-135.05之间的数字',
                    range: '请输入73.66-135.05之间的数字'
                },
                lat: {
                    required: '请输入纬度值',
                    number: '请输入3.86-53.55之间的数字',
                    range: '请输入3.86-53.55之间的数字'
                },
                name: {
                    required: lineNameNull,
                    maxlength: publicSize20
                },
                width: {
                    required: fenceOperationOffsetNull,
                    maxlength: fenceOperationScope255
                },
                description: {
                    maxlength: publicSize100
                },
                groupName: {
                    required: publicSelectGroupNull,
                }
            }
        }).form();
    },
    //矩形保存
    rectangleSave: function (thisBtn) {
        var nowLULnglat = $("#LUPointLngLat").val().split(',');
        var nowRDLngLat = $("#RDPointLngLat").val().split(',');
        $("#longitudesRectangles").attr('value', nowLULnglat[0] + "," + nowRDLngLat[0] + "," + nowRDLngLat[0] + "," + nowLULnglat[0]);
        $("#latitudesRectangles").attr('value', nowLULnglat[1] + "," + nowRDLngLat[1] + "," + nowRDLngLat[1] + "," + nowLULnglat[1]);
        if (fenceOperation.validate_rectangle()) {
            thisBtn.disabled = true;
            $("#rectangles").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    $("#rectangle-form").modal("hide");
                    saveFenceName = $('#rectangleName').val();
                    saveFenceType = 'zw_m_rectangle';
                    mouseToolEdit.close(true);
                    mouseTool.close(true);
                    var rectang_fenceId = $("#rectangleId").val();
                    if (rectang_fenceId !== "") {
                        var thisFence = fenceIDMap.get(rectang_fenceId);
                        fenceIDMap.remove(rectang_fenceId);
                        thisFence.hide();
                    }
                    fenceOperation.addNodes();
                } else {
                    if (datas.msg == null) {
                        layer.msg(fenceOperationRectangleExist);
                    } else if (datas.msg != null) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
        }
    },
    //矩形添加时验证
    validate_rectangle: function () {
        return $("#rectangles").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                type: {
                    required: false,
                    maxlength: 20
                },
                description: {
                    maxlength: 100
                },
                lnglatQuery_LU: {
                    required: true,
                    isLngLat: [[135.05, 53.55], [73.66, 3.86]],
                },
                lnglatQuery_RD: {
                    required: true,
                    isLngLat: [[135.05, 53.55], [73.66, 3.86]],
                },
                groupName: {
                    required: true
                }
            },
            messages: {
                name: {
                    required: areaNameNull,
                    maxlength: publicSize20
                },
                type: {
                    required: deviationNull,
                    maxlength: publicSize20
                },
                description: {
                    maxlength: publicSize100
                },
                lnglatQuery_LU: {
                    required: fenceOperationLoAlaNull,
                    isLngLat: fenceOperationLoAlaError,
                },
                lnglatQuery_RD: {
                    required: fenceOperationLoAlaNull,
                    isLngLat: fenceOperationLoAlaError,
                },
                groupName: {
                    required: publicSelectGroupNull
                }
            },
        }).form();
    },
    //多边形保存
    polygonSave: function (thisBtn) {
        var polygonId = $("#polygonId").val();
        var rectanglePointMag = [];
        var allPointArray = [];
        $('.rectangleAllPointLngLat').each(function () {
            var value = $(this).val().split(',');
            var msgArray = [];
            msgArray.polygonId = polygonId;
            msgArray.longitude = value[0];
            msgArray.latitude = value[1];
            rectanglePointMag.push(msgArray);
            allPointArray.push(value);
        });
        var lngValue = '';
        var latValue = '';
        for (var i = 0, len = allPointArray.length; i < len; i++) {
            if (i !== len - 1) {
                lngValue += allPointArray[i][0] + ",";
                latValue += allPointArray[i][1] + ",";
            } else {
                lngValue += allPointArray[i][0];
                latValue += allPointArray[i][1];
            }
        }
        $("#longitudesPolygons").attr('value', lngValue);
        $("#latitudesPolygons").attr('value', latValue);
        var polygonId = $("#polygonId").val();
        var rectanglePointMag = [];
        var allPointArray = [];
        $('.rectangleAllPointLngLat').each(function () {
            var value = $(this).val().split(',');
            var msgArray = [];
            msgArray.polygonId = polygonId;
            msgArray.longitude = value[0];
            msgArray.latitude = value[1];
            rectanglePointMag.push(msgArray);
            allPointArray.push(value);
        });
        var lngValue = '';
        var latValue = '';
        for (var i = 0, len = allPointArray.length; i < len; i++) {
            if (i !== len - 1) {
                lngValue += allPointArray[i][0] + ",";
                latValue += allPointArray[i][1] + ",";
            } else {
                lngValue += allPointArray[i][0];
                latValue += allPointArray[i][1];
            }
        }
        $("#longitudesPolygons").attr('value', lngValue);
        $("#latitudesPolygons").attr('value', latValue);
        if (fenceOperation.validate_polygon()) {
            thisBtn.disabled = true;
            $("#polygons").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    $("#myModal").modal("hide");
                    saveFenceName = $('#polygonName').val();
                    saveFenceType = 'zw_m_polygon';
                    $(".fenceA").removeClass("fenceA-active");
                    mouseTool.close(true);
                    map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendPolygon);
                    var polygonId = $("#polygonId").val();
                    if (PolyEditorMap.containsKey(polygonId)) {
                        var mapEditFence = PolyEditorMap.get(polygonId);
                        mapEditFence.close();
                    }
                    fenceOperation.addNodes();
                } else {
                    if (datas.msg == null) {
                        layer.msg(fenceOperationPolygonExist);
                    } else {
                        layer.msg(datas.msg, {move: false});
                    }
                }
            });
        }
    },
    //多边形区域添加时验证
    validate_polygon: function () {
        return $("#polygons").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                type: {
                    maxlength: 20
                },
                description: {
                    maxlength: 100
                },
                groupName: {
                    required: true
                }
            },
            messages: {
                name: {
                    required: areaNameNull,
                    maxlength: publicSize20
                },
                type: {
                    maxlength: publicSize20
                },
                description: {
                    maxlength: publicSize100
                },
                groupName: {
                    required: publicSelectGroupNull
                }
            }
        }).form();
    },
    //圆保存
    roundSave: function (thisBtn) {
        var circleLng = $("#editCircleLng").val();
        var circleLat = $("#editCircleLat").val();
        var circleRadius = $("#editCircleRadius").val();
        if (circleLng !== '') {
            $("#circle-lng").attr('value', circleLng);
        }
        if (circleLat !== '') {
            $("#circle-lat").attr('value', circleLat);
        }
        if (circleRadius !== '') {
            $("#circle-radius").attr('value', circleRadius);
        }
        if (fenceOperation.validate_circle()) {
            thisBtn.disabled = true;
            $("#circles").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    $("#circleArea").modal("hide");
                    saveFenceName = $('#circleName').val();
                    saveFenceType = 'zw_m_circle';
                    mouseTool.close(true);
                    var circleId = $("#circleId").val();
                    if (PolyEditorMap.containsKey(circleId)) {
                        var mapEditFence = PolyEditorMap.get(circleId);
                        mapEditFence.close();
                    }
                    map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendCircle);
                    fenceOperation.addNodes();
                } else {
                    if (datas.msg == null) {
                        layer.msg(fenceOperationCircleExist);
                    } else if (datas.msg != null) {
                        layer.msg(data.msg, {move: false});
                    }
                }
            });
        }
    },
    //圆形区域添加时验证
    validate_circle: function () {
        return $("#circles").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                type: {
                    maxlength: 20
                },
                description: {
                    maxlength: 100
                },
                centerPointLng: {
                    required: true,
                    isLng: [135.05, 53.55],
                },
                centerPointLat: {
                    required: true,
                    isLat: [73.66, 3.86],
                },
                centerRadius: {
                    required: true,
                    number: true,
                },
                groupName: {
                    required: true
                }
            },
            messages: {
                name: {
                    required: areaNameNull,
                    maxlength: publicSize20
                },
                type: {
                    maxlength: publicSize20
                },
                description: {
                    maxlength: publicSize100
                },
                centerPointLng: {
                    required: fenceOperationLongitudeNull,
                    isLng: fenceOperationLongitudeError,
                },
                centerPointLat: {
                    required: fenceOperationLatitudeNull,
                    isLat: fenceOperationLatitudeError,
                },
                centerRadius: {
                    required: fenceOperationCircleRadiusNull,
                    number: publicNumberNull,
                },
                groupName: {
                    required: publicSelectGroupNull
                }
            }
        }).form();
    },
    //清除错误信息
    clearErrorMsg: function () {
        mouseTool.close(true);
        $("label.error").hide();
        $(".error").removeClass("error");
    },
    // 新增围栏树节点
    addNodes: function () {
        fenceIdArray = [];
        fenceOpenArray = [];
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var allNodes = zTree.getNodes();
        for (var i = 0, len = allNodes.length; i < len; i++) {
            if (allNodes[i].open === true) {
                fenceOpenArray.push(allNodes[i].id);
            }
        }
        var nodes = zTree.getCheckedNodes(true);
        for (var i = 0, len = nodes.length; i < len; i++) {
            fenceIdArray.push(nodes[i].id);
        }
        var fenceAll = {
            async: {
                url: "/clbs/m/functionconfig/fence/bindfence/fenceTree",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                dataFilter: fenceOperation.ajaxFenceDataFilter,
                otherParam: {"type": "multiple"}
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
                addHoverDom: fenceOperation.addHoverDom,
                removeHoverDom: fenceOperation.removeHoverDom,
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
                onClick: fenceOperation.onClickFenceChar,
                onCheck: fenceOperation.onCheckFenceChar,
                onAsyncSuccess: fenceOperation.zTreeOnAsyncFenceSuccess
            }
        };
        $.fn.zTree.init($("#fenceDemo"), fenceAll, null);
    },
    //围栏绑定
    fenceBind: function (fenceId, fenceName, fenceInfoId, fenceIdstr) {
        fenceOperation.clearFenceBind();
        $("#fenceID").val(fenceId);
        $("#fenceName").val(fenceName);
        $("#fenceInfoId").val(fenceInfoId);
        pageLayout.closeVideo();

        // 圆形区域、矩形区域、多边形区域、路线绑定、修改时，增加夜间最高速度(km/h)(night_top_speed)、夜间限速时间段(night_limit_time)列
        if (fenceId == "zw_m_line" || fenceId == "zw_m_rectangle" || fenceId == "zw_m_circle" || fenceId == "zw_m_polygon") {
            $('.addFenceTh').show();
        } else {
            $('.addFenceTh').hide();
        }
        $("#fenceBind").modal('show');
        fenceOperation.initBindFenceTree();

        // json_ajax("post", '/clbs/m/functionconfig/fence/bindfence/getVehicleIdsByFenceId', "json", false, {"fenceId": fenceIdstr}, function (data) {
        //     oldFencevehicleIds = data.obj;
        // })
        return false;
    },
    initBindFenceTree: function () {
        bindFenceSetChar = {
            async: {
                url: fenceOperation.getFenceTreeUrl,
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                otherParam: {"type": "multiple"},
                dataFilter: fenceOperation.ajaxDataFilter
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
                beforeClick: fenceOperation.beforeClickFenceVehicle,
                onAsyncSuccess: fenceOperation.zTreeVFenceOnAsyncSuccess,
                beforeCheck: fenceOperation.zTreeBeforeCheck,
                onCheck: fenceOperation.onCheckFenceVehicle,
                onExpand: fenceOperation.zTreeOnExpand,
                onNodeCreated: fenceOperation.zTreeOnNodeCreated,
            }
        };
        $.fn.zTree.init($("#treeDemoFence"), bindFenceSetChar, null);
    },
    getFenceTreeUrl: function (treeId, treeNode) {
        if (treeNode == null) {
            return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
        } else if (treeNode.type === "assignment") {
            return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId=" + treeNode.id + "&isChecked=" + treeNode.checked + "&monitorType=monitor";
        }
    },
    //组织树预处理函数
    ajaxDataFilter: function (treeId, parentNode, responseData) {
//        responseData = JSON.parse(ungzip(responseData));
//        return responseData;
        var treeObj = $.fn.zTree.getZTreeObj("treeDemoFence");
        if (responseData.msg) {
            var obj = JSON.parse(ungzip(responseData.msg));
            var data;
            if (obj.tree != null && obj.tree !== undefined) {
                data = obj.tree;
                fenceSize = obj.size;
            } else {
                data = obj
            }
            for (var i = 0; i < data.length; i++) {
                if (data[i].type === "group") {
                    data[i].open = true;
                }
            }
        }
        return data;
    },
    beforeClickFenceVehicle: function (treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        zTree.checkNode(treeNode, !treeNode.checked, null, true);
        return false;
    },
//    zTreeOnAsyncSuccess: function(event, treeId, treeNode, msg){
//
//	},
    zTreeBeforeCheck: function (treeId, treeNode) {
        var flag = true;
        if (!treeNode.checked) {
            if (treeNode.type === "group" || treeNode.type === "assignment") { //若勾选的为组织或分组
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    .getCheckedNodes(true), v = "";
                var nodesLength = 0;

                json_ajax("post", "/clbs/a/search/getMonitorNum",
                    "json", false, {"id": treeNode.id, "type": treeNode.type}, function (data) {
                        if (data.success) {
                            nodesLength += data.obj;
                        } else {
                            layer.msg(treeCheckError);
                        }
                    });

                //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                var ns = [];
                //节点id
                var nodeId;
                for (var i = 0; i < nodes.length; i++) {
                    nodeId = nodes[i].id;
                    if (nodes[i].type === "people" || nodes[i].type === "vehicle" || nodes[i].type === "thing") {
                        //查询该节点是否在勾选组织或分组下，若在则不记录，不在则记录
                        var nd = zTree.getNodeByParam("tId", nodes[i].tId, treeNode);
                        if (nd == null && $.inArray(nodeId, ns) === -1) {
                            ns.push(nodeId);
                        }
                    }
                }
                nodesLength += ns.length;
            } else if (treeNode.type === "people" || treeNode.type === "vehicle" || treeNode.type === "thing") { //若勾选的为监控对象
                var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                    .getCheckedNodes(true), v = "";
                var nodesLength = 0;
                //存放已记录的节点id(为了防止车辆有多个分组而引起的统计不准确)
                var ns = [];
                //节点id
                var nodeId;
                for (var i = 0; i < nodes.length; i++) {
                    nodeId = nodes[i].id;
                    if (nodes[i].type === "people" || nodes[i].type === "vehicle" || nodes[i].type === "thing") {
                        if ($.inArray(nodeId, ns) === -1) {
                            ns.push(nodeId);
                        }
                    }
                }
                nodesLength = ns.length + 1;
            }
//            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
//                .getCheckedNodes(true), v = "";
//            var nodesLength = 0;
//            for (var i=0;i<nodes.length;i++) {
//                if(nodes[i].type == "people" || nodes[i].type == "vehicle"){
//                    nodesLength += 1;
//                }
//            }
//            if (treeNode.type == "group" || treeNode.type == "assignment"){ // 判断若勾选节点数大于5000，提示
//                var zTree = $.fn.zTree.getZTreeObj("treeDemo")
//                json_ajax("post", "/clbs/a/search/getMonitorNum",
//                    "json", false, {"id": treeNode.id,"type": treeNode.type}, function (data) {
//                        nodesLength += data;
//                    })
//            } else if (treeNode.type == "people" || treeNode.type == "vehicle") {
//                nodesLength += 1;
//            }
            if (nodesLength > 5000) {
                layer.msg(treeMaxLength5000);
                flag = false;
            }
        }
        if (flag) {
            //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
            if (treeNode.type === "group" && !treeNode.checked) {
                checkFlag = true;
            }
        }
        return flag;
    },
    onCheckFenceVehicle: function (e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence"), nodes = zTree
            .getCheckedNodes(true), v = "";
        //若为取消勾选则不展开节点
        if (treeNode.checked) {
            zTree.expandNode(treeNode, true, true, true, true); // 展开节点
        }
        // 记录勾选的节点
        var v = "";
        for (var i = 0, l = nodes.length; i < l; i++) {
            if (nodes[i].type === "vehicle" || nodes[i].type === "people" || nodes[i].type === "thing") {
                v += nodes[i].id + ",";
            }
        }
        vehicleFenceList = v;
    },
    zTreeOnExpand: function (event, treeId, treeNode) {
        //判断是否是勾选操作展开的树(是则继续执行，不是则返回)
        if (treeNode.type === "group" && !checkFlag) {
            return;
        }
        var treeObj = $.fn.zTree.getZTreeObj("treeDemoFence");
        // if (treeNode.children) {
        //     for (var i = 0, l = treeNode.children.length; i < l; i++) {
        //         treeObj.checkNode(treeNode.children[i], false, true);
        //         if ($.inArray(treeNode.children[i].id, oldFencevehicleIds) != -1) {
        //             console.log(treeNode.children[i].id)
        //             treeObj.checkNode(treeNode.children[i], true, true);
        //         }
        //     }
        // }
        //初始化勾选操作判断表示
        checkFlag = false;

        if (treeNode.type === "group") {
            var url = "/clbs/m/functionconfig/fence/bindfence/putMonitorByGroup";
            json_ajax("post", url, "json", false, {
                "groupId": treeNode.id,
                "isChecked": treeNode.checked,
                "monitorType": "monitor"
            }, function (data) {
                var result = data.obj;
                if (result != null && result !== undefined) {
                    $.each(result, function (i) {
                        var pid = i; //获取键值
                        var chNodes = result[i]; //获取对应的value
                        var parentTid = fenceZTreeIdJson[pid][0];
                        var parentNode = treeObj.getNodeByTId(parentTid);
                        if (parentNode.children === undefined) {
                            treeObj.addNodes(parentNode, []);
                        }
                    });
                }
            })
        }
    },
    zTreeOnNodeCreated: function (event, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        var id = treeNode.id.toString();
        var list = [];
        if (fenceZTreeIdJson[id] === undefined || fenceZTreeIdJson[id] == null) {
            list = [treeNode.tId];
            fenceZTreeIdJson[id] = list;
        } else {
            fenceZTreeIdJson[id].push(treeNode.tId)
        }
    },
    zTreeVFenceOnAsyncSuccess: function (event, treeId, treeNode, msg) {

        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        // 更新节点数量
        treeObj.updateNodeCount(treeNode);
        // 默认展开200个节点
        var initLen = 0;
        notExpandNodeInit = treeObj.getNodesByFilter(assignmentNotExpandFilter);
        for (i = 0; i < notExpandNodeInit.length; i++) {
            treeObj.expandNode(notExpandNodeInit[i], true, true, false, true);
            initLen += notExpandNodeInit[i].children.length;
            if (initLen >= 200) {
                break;
            }
        }

    },
    /**
     * 选中已选的节点
     */
    checkCurrentNodes: function (treeNode) {
        // var crrentSubV = vehicleFenceList.split(",");
        // var treeObj = $.fn.zTree.getZTreeObj("treeDemoFence");
        // if (treeNode != undefined && treeNode != null && treeNode.type === "assignment" && treeNode.children != undefined) {
        //     var list = treeNode.children;
        //     if (list != null && list.length > 0) {
        //         for (var j = 0; j < list.length; j++) {
        //             var znode = list[j];
        //             if (crrentSubV != null && crrentSubV != undefined && crrentSubV.length !== 0 && $.inArray(znode.id, crrentSubV) != -1) {
        //                 treeObj.checkNode(znode, true, true);
        //             }
        //         }
        //     }
        // }
        var treeObj = $.fn.zTree.getZTreeObj("treeDemoFence");
        if (treeNode.children) {
            for (var i = 0, l = treeNode.children.length; i < l; i++) {
                if ($.inArray(treeNode.children[i].id, oldFencevehicleIds) !== -1) {
                    treeObj.checkNode(treeNode.children[i], true, true);
                }
            }
        }
    },
    //围栏绑定模糊查询
    searchVehicleSearch: function () {
        //search_ztree('treeDemoFence', 'searchVehicle','vehicle');
        if (fenceInputChange !== undefined) {
            clearTimeout(fenceInputChange);
        }
        fenceInputChange = setTimeout(function () {
            var param = $("#searchVehicle").val();
            if (param === '') {
                fenceOperation.initBindFenceTree();
            } else {
                fenceOperation.searchBindFenceTree(param);
            }
        }, 500);
    },
    ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
        responseData = JSON.parse(ungzip(responseData));
        var list = [];
        if (vehicleFenceList != null && vehicleFenceList !== undefined && vehicleFenceList !== "") {
            var str = (vehicleFenceList.slice(vehicleFenceList.length - 1) === ',') ? vehicleFenceList.slice(0, -1) : vehicleFenceList;
            list = str.split(",");
        }
        return filterQueryResult(responseData, list);
    },
    //check选择
    checkAllClick: function () {
        if ($(this).prop("checked") === true) {
            $("#checkAll").attr("checked", true);
            $("#tableList input[type='checkbox']").prop("checked",
                $(this).prop("checked"));
            $('#tableList tbody tr').addClass('selected');
            trid = [];
            for (i = 1; i < $("#tableList tr").length; i++) {
                trid.push("list" + i);
            }
        } else {
            $("#checkAll").attr("checked", false);
            $("#tableList input[type='checkbox']").prop("checked", false);
            $('#tableList tbody tr').removeClass('selected');
            trid = [];
        }
    },
    // 点击添加(按围栏 )
    addBtnClick: function () {
        trid = [];
        // 动态添加表格
        vehicelTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        vehicleNode = vehicelTree.getCheckedNodes();
        if (vehicleNode == null || vehicleNode.length === 0) {
            layer.msg(fenceOperationMonitorNull, {move: false});
        } else {
            var fenceName = $("#fenceName").val();
            var fenceInfoId = $("#fenceInfoId").val();
            // 先清空table 中的数据
            $("#tableList tbody").html("");
            // 去重
            vehicleNode = vehicleNode.unique2();
            var j = 0;
            for (var i = 0; i < vehicleNode.length; i++) {
                if (vehicleNode[i].type === "vehicle" || vehicleNode[i].type === "people" || vehicleNode[i].type === "thing") {
                    j++;
                    var inRadioName = "Inradio" + j;
                    var outRadioName = "Outradio" + j;
                    var inDriverName = "InDriver" + j;
                    var outDriverName = "OutDriver" + j;
                    var sendFenceTypeName = "sendFenceType" + j;
                    var alarmSourceName = "alarmSourceName" + j;
                    var openDoorName = "openDoor" + j;
                    var communicationFlagName = "communicationFlag" + j;
                    var gnssFlagName = "gnssFlag" + j;
                    var fencetype = $("#fenceID").val();

                    var fenceId = $("input#fenceID").val();


                    var tr = "<tr id='list" + j + "'><td><input id='checkList" + j + "' type='checkbox' onclick='fenceOperation.checkboxis(this)'   name='thead'/></td><td>"
                        + fenceName
                        + "</td><td>"
                        + vehicleNode[i].name
                        + "</td><td><label name = 'fenceType' style='margin-bottom:0px;'>"
                        + fenceOperation.fencetypepid(fencetype, alarmSourceName)
                        + "</label></td>"
                        + fenceOperation.sendFenceTypeTd(fencetype, sendFenceTypeName)
                        + fenceOperation.alarmSourceCheck(fencetype, alarmSourceName)
                        + "<td id = 'startTime'><input id='startDatePlugin" + j + "' onclick='fenceOperation.selectDate(this)' style='width:120px;cursor:default;' class='form-control layer-date laydate-icon selectTime' name='startTime' readonly /></td>"
                        + "<td id = 'endTime'><input id='endDatePlugin" + j + "'onclick='fenceOperation.selectDate(this)' style='width:120px;cursor:default;'  class='form-control layer-date laydate-icon selectTime' name='endTime' readonly /></td>"
                        + "<td id = 'alarmStartDateTD'><input id='startTimeHMS" + j + "'onclick='fenceOperation.selectTime(this)' style='width:120px;cursor:default;'  class='form-control layer-date laydate-icon'  name='alarmStartDateTD'  readonly /></td>"
                        + "<td id = 'alarmEndDateTD'><input id='endTimeHMS" + j + "' onclick='fenceOperation.selectTime(this)' style='width:120px;cursor:default;'  class='form-control layer-date laydate-icon' name='alarmEndDateTD'  readonly /></td>"
                        + "<td id = 'alarmIn'><input type='radio' value = 1 checked name='" + inRadioName + "' id='" + inRadioName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + inRadioName + "'>是</label><input type='radio' value = 0 name='" + inRadioName + "' id='" + inRadioName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + inRadioName + "s'>否</label></td>"
                        + "<td id = 'alarmOut'><input type='radio'  value = 1 checked name='" + outRadioName + "' id='" + outRadioName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + outRadioName + "'>是</label><input type='radio'  value = 0 name='" + outRadioName + "' id='" + outRadioName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + outRadioName + "s'>否</label></td>"
                        + "<td id = 'alarmInDriver'><input type='radio' value = 1 checked name='" + inDriverName + "' id='" + inDriverName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + inDriverName + "'>是</label><input type='radio' value = 0 name='" + inDriverName + "' id='" + inDriverName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + inDriverName + "s'>否</label></td>"
                        + "<td id = 'alarmOutDriver'><input type='radio'  value = 1 checked name='" + outDriverName + "' id='" + outDriverName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + outDriverName + "'>是</label><input type='radio'  value = 0 name='" + outDriverName + "' id='" + outDriverName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + outDriverName + "s'>否</label></td>"
                        + "<td id='speed'><input type= 'text' name = 'speed' class='form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" /></td>";

                    // 圆形区域、矩形区域、多边形区域、路线绑定、修改时，增加夜间最高速度(km/h)(night_top_speed)、夜间限速时间段(night_limit_time)列
                    if (fenceId == "zw_m_line" || fenceId == "zw_m_rectangle" || fenceId == "zw_m_circle" || fenceId == "zw_m_polygon") {
                        tr += "<td id='nightMaxSpeed'><input type= 'text' name = 'nightMaxSpeed' class='form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" /></td>"
                            + "<td id='nightLimitTime'><input style='width:135px;cursor: pointer;background-color: #fafafa;' class='dateTime2 form-control layer-date laydate-icon valid' id='nightLimitTime" + i + "' name='nightLimitTime' readonly></td>";
                    }

                    tr += "<td id='overSpeedLastTime'><input type= 'text' name = 'overSpeedLastTime' class='form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" /></td>"
                        +
                        fenceOperation.travelLongAndSmallTime(fencetype)
                        +
                        fenceOperation.otherInfo(fencetype, openDoorName, communicationFlagName, gnssFlagName)
                        + "<td class='hidden' id='fenceId'>"
                        + fenceInfoId
                        + "</td><td class='hidden' id = 'vehicleId'>"
                        + vehicleNode[i].id + "</td><td id = 'monitorType' class='hidden'>" + vehicleNode[i].type + "</td></tr>";
                    $("#tableList tbody").append(tr);

                    laydate.render({
                        elem: '#nightLimitTime' + i
                        , type: 'time'
                        , range: '--'
                        , format: 'HH:mm'
                        , noSecond: true
                        , trigger: 'click'
                    });

                    var checkRadio;
                    if (fencetype === "zw_m_administration" || fencetype === "zw_m_travel_line") {
                        checkRadio = $("input[name=" + alarmSourceName + "]")[1];
                    } else {
                        checkRadio = $("input[name=" + alarmSourceName + "]")[0];
                    }
                    if ($(checkRadio).val() == 1) {
                        $("#tableList tbody tr").find("#sendFenceType,#alarmInDriver,#alarmOutDriver,#overSpeedLastTime,#travelLongTime,#travelSmallTime,#openDoor,#communicationFlag,#gnssFlag").css('opacity', '0');
                    }
                }
            }
            fenceOperation.accordingToFenceTypeSHTable(fencetype);
            // 区分终端报警平台报警
            $("input[name*='alarmSourceName']").change(function () {
                var value = $(this).val();
                var p_tr = $(this).closest('tr');
                if (value === '1') { // 若为平台报警，禁用与终端报警相关的参数
                    p_tr.children("td#sendFenceType").css('opacity', '0');
                    p_tr.children("td#alarmInDriver").css('opacity', '0');
                    p_tr.children("td#alarmOutDriver").css('opacity', '0');
                    p_tr.children("td#overSpeedLastTime").css('opacity', '0');
                    p_tr.children("td#travelLongTime").css('opacity', '0');
                    p_tr.children("td#travelSmallTime").css('opacity', '0');
                    p_tr.children("td#openDoor").css('opacity', '0');
                    p_tr.children("td#communicationFlag").css('opacity', '0');
                    p_tr.children("td#gnssFlag").css('opacity', '0');
                } else {
                    p_tr.children("td#sendFenceType").css('opacity', '1');
                    p_tr.children("td#alarmInDriver").css('opacity', '1');
                    p_tr.children("td#alarmOutDriver").css('opacity', '1');
                    p_tr.children("td#overSpeedLastTime").css('opacity', '1');
                    p_tr.children("td#travelLongTime").css('opacity', '1');
                    p_tr.children("td#travelSmallTime").css('opacity', '1');
                    p_tr.children("td#openDoor").css('opacity', '1');
                    p_tr.children("td#communicationFlag").css('opacity', '1');
                    p_tr.children("td#gnssFlag").css('opacity', '1');
                }
            });
        }
    },
    // 根据围栏类型显示隐藏数据表格内容
    accordingToFenceTypeSHTable: function (fenceType) {
        if (fenceType == "zw_m_rectangle" || fenceType == "zw_m_circle" || fenceType == "zw_m_polygon" || fenceType == "zw_m_administration") {
            $("#openDoor,#communicationFlag,#gnssFlag").removeClass("hidden");
            $("#tableList thead tr th:nth-child(21)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(22)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(23)").removeClass("hidden");
            $("#travelLongTime,#travelSmallTime").addClass("hidden");
            $("#tableList thead tr th:nth-child(19)").addClass("hidden");
            $("#tableList thead tr th:nth-child(20)").addClass("hidden");
        }
        else if (fenceType === "zw_m_line" || fenceType === "zw_m_travel_line") {
            $("#travelLongTime,#travelSmallTime").removeClass("hidden");
            $("#tableList thead tr th:nth-child(19)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(20)").removeClass("hidden");
            $("#openDoor,#communicationFlag,#gnssFlag").addClass("hidden");
            $("#tableList thead tr th:nth-child(21)").addClass("hidden");
            $("#tableList thead tr th:nth-child(22)").addClass("hidden");
            $("#tableList thead tr th:nth-child(23)").addClass("hidden");
        }
        else {
            $("#travelLongTime,#travelSmallTime,#openDoor,#communicationFlag,#gnssFlag").removeClass("hidden");
            $("#tableList thead tr th:nth-child(19)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(20)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(21)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(22)").removeClass("hidden");
            $("#tableList thead tr th:nth-child(23)").removeClass("hidden");
        }
    },
    //点击解绑按围栏
    removeBtnClick: function () {
        if ($("#checkAll").attr("checked") === "checked") {
            for (var k = 0; k <= vehicleNode.length; k++) {
                trid.push($("#list" + k));
                $("#list" + k).remove();
            }
            $("#checkAll").attr("checked", false)
        } else {
            if (trid.length === 0) {
                layer.msg(userDeleteChooseNull, {move: false});
            }
            for (var i = 0; i < trid.length; i++) {
                $("#" + trid[i]).remove();
            }
        }
        trid = [];
    },
    //依例全设
    setAllClick: function () {
        var i = 0;
        var setSendFenceType = '';
        var setalarmSource = '';
        var setalarmIn = '';
        var setalarmOut = '';
        var setalarmInDriver = '';
        var setalarmOutDriver = '';
        var setOpenDoor = '';
        var setCommunicationFlag = '';
        var setgnssFlag = '';
        var startTimeVal = '';
        var endTimeVal = '';
        var nameSendFenceType = '';
        var nameAlarmSource = '';
        var namein = '';
        var nameout = '';
        var nameinDriver = '';
        var nameoutDriver = '';
        var nameOpenDoor = '';
        var nameCommunicationFlag = '';
        var namegnssFlag = '';
        var startDateVal = '';
        var endDateVal = '';
        var speedVal = '';
        var nightMaxSpeedVal = '';
        var nightLimitTimeVal = '';
        var overSpeedLastTimeVal = '';
        var travelLongTimeVal = '';
        var travelSmallTimeVal = '';
        var fenceType = $("#" + trid[0]).children("td").eq(3).find("label").text();
        /*if (trid.indexOf('list1') !== '-1') {
            console.log(1111);
        }*/
        if (trid.length < 1) {
            layer.msg("请选择一项！")
        } else if (trid.length > 1) {
            layer.msg("只能选择一项！")
        } else {
            if ($("#" + trid[0]).children("td").eq(4).id = "sendFenceType") {
                nameSendFenceType = $("#" + trid[0]).children("td").eq(4).find("input").attr("name");
                setSendFenceType = $('input[name="' + nameSendFenceType + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(5).id = "alarmSource") {
                nameAlarmSource = $("#" + trid[0]).children("td").eq(5).find("input").attr("name");
                setalarmSource = $('input[name="' + nameAlarmSource + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(6).find("input").val() != null) {
                startTimeVal = $("#" + trid[0]).children("td").eq(6).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(7).find("input").val() != null) {
                endTimeVal = $("#" + trid[0]).children("td").eq(7).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(8).find("input").val() != null) {
                startDateVal = $("#" + trid[0]).children("td").eq(8).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(9).find("input").val() != null) {
                endDateVal = $("#" + trid[0]).children("td").eq(9).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(10).id = "alarmIn") {
                namein = $("#" + trid[0]).children("td").eq(10).find("input").attr("name");
                setalarmIn = $('input[name="' + namein + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(11).id = "alarmOut") {
                nameout = $("#" + trid[0]).children("td").eq(11).find("input").attr("name");
                setalarmOut = $('input[name="' + nameout + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(12).id = "alarmInDriver") {
                nameinDriver = $("#" + trid[0]).children("td").eq(12).find("input").attr("name");
                setalarmInDriver = $('input[name="' + nameinDriver + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(13).id = "alarmOutDriver") {
                nameoutDriver = $("#" + trid[0]).children("td").eq(13).find("input").attr("name");
                setalarmOutDriver = $('input[name="' + nameoutDriver + '"]:checked').val();
            }

            if ($("#" + trid[0]).children("td").eq(14).find("input").val() != null) {
                speedVal = $("#" + trid[0]).children("td").eq(14).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(15).find("input").val() != null) {
                nightMaxSpeedVal = $("#" + trid[0]).children("td").eq(15).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(16).find("input").val() != null) {
                nightLimitTimeVal = $("#" + trid[0]).children("td").eq(16).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(17).find("input").val() != null) {
                overSpeedLastTimeVal = $("#" + trid[0]).children("td").eq(17).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(18).find("input").val() != null) {
                travelLongTimeVal = $("#" + trid[0]).children("td").eq(18).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(19).find("input").val() != null) {
                travelSmallTimeVal = $("#" + trid[0]).children("td").eq(19).find("input").val();
            }
            if ($("#" + trid[0]).children("td").eq(20).id = "openDoor") {
                nameOpenDoor = $("#" + trid[0]).children("td").eq(20).find("input").attr("name");
                setOpenDoor = $('input[name="' + nameOpenDoor + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(21).id = "communicationFlag") {
                nameCommunicationFlag = $("#" + trid[0]).children("td").eq(21).find("input").attr("name");
                setCommunicationFlag = $('input[name="' + nameCommunicationFlag + '"]:checked').val();
            }
            if ($("#" + trid[0]).children("td").eq(22).id = "gnssFlag") {
                namegnssFlag = $("#" + trid[0]).children("td").eq(22).find("input").attr("name")
                setgnssFlag = $('input[name="' + namegnssFlag + '"]:checked').val();
            }
            for (i = 0; i <= $("#tableList tbody tr").length; i++) {
                var tds = $("#tableList tbody tr")[i];
                $(tds).each(function () {
                    var td = this;
                    var index = i + 1;
                    if (fenceType !== "路线" && fenceType !== "多边形") {
                        if (setSendFenceType === '0') {
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(0).prop("checked", true);
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(1).prop("checked", false);
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(2).prop("checked", false)
                        } else if (setSendFenceType === '1') {
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(0).prop("checked", false);
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(1).prop("checked", true);
                            $(td).find('input[name="sendFenceType' + i + '"][type="radio"]').eq(2).prop("checked", false)
                        } else if (setSendFenceType === '2') {
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(0).prop("checked", false);
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(1).prop("checked", false);
                            $(td).find('input[name="sendFenceType' + index + '"][type="radio"]').eq(2).prop("checked", true)
                        }
                    }
                    if (setalarmIn === '1') {
                        $(td).find('input[name="Inradio' + index + '"][type="radio"]').eq(0).prop("checked", true);
                        $(td).find('input[name="Inradio' + index + '"][type="radio"]').eq(1).prop("checked", false)
                    } else if (setalarmIn === '0') {
                        $(td).find('input[name="Inradio' + index + '"][type="radio"]').eq(1).prop("checked", true);
                        $(td).find('input[name="Inradio' + index + '"][type="radio"]').eq(0).prop("checked", false)
                    }
                    if (setalarmOut === '1') {
                        $(td).find('input[name="Outradio' + index + '"][type="radio"]').eq(0).prop("checked", true);
                        $(td).find('input[name="Outradio' + index + '"][type="radio"]').eq(1).prop("checked", false)
                    } else if (setalarmOut === '0') {
                        $(td).find('input[name="Outradio' + index + '"][type="radio"]').eq(1).prop("checked", true);
                        $(td).find('input[name="Outradio' + index + '"][type="radio"]').eq(0).prop("checked", false)
                    }
                    if (setalarmInDriver === '1') {
                        $(td).find('input[name="InDriver' + index + '"][type="radio"]').eq(0).prop("checked", true);
                        $(td).find('input[name="InDriver' + i + '"][type="radio"]').eq(1).prop("checked", false)
                    } else if (setalarmInDriver === '0') {
                        $(td).find('input[name="InDriver' + index + '"][type="radio"]').eq(1).prop("checked", true);
                        $(td).find('input[name="InDriver' + index + '"][type="radio"]').eq(0).prop("checked", false)
                    }
                    if (setalarmOutDriver === '1') {
                        $(td).find('input[name="OutDriver' + index + '"][type="radio"]').eq(0).prop("checked", true);
                        $(td).find('input[name="OutDriver' + index + '"][type="radio"]').eq(1).prop("checked", false)
                    } else if (setalarmOutDriver === '0') {
                        $(td).find('input[name="OutDriver' + index + '"][type="radio"]').eq(1).prop("checked", true);
                        $(td).find('input[name="OutDriver' + index + '"][type="radio"]').eq(0).prop("checked", false)
                    }
                    if (startTimeVal != null) {
                        $(td).find('input[name="startTime"]').val(startTimeVal);
                    }
                    if (endTimeVal != null) {
                        $(td).find('input[name="endTime"]').val(endTimeVal);
                    }
                    if (startDateVal != null) {
                        $(td).find('input[name="alarmStartDateTD"]').val(startDateVal);
                    }
                    if (endDateVal != null) {
                        $(td).find('input[name="alarmEndDateTD"]').val(endDateVal);
                    }
                    if (speedVal != null) {
                        $(td).find('input[name="speed"]').val(speedVal);
                    }
                    if (nightMaxSpeedVal != null) {
                        $(td).find('input[name="nightMaxSpeed"]').val(nightMaxSpeedVal);
                    }
                    if (nightLimitTimeVal != null) {
                        $(td).find('input[name="nightLimitTime"]').val(nightLimitTimeVal);
                    }
                    if (overSpeedLastTimeVal != null) {
                        $(td).find('input[name="overSpeedLastTime"]').val(overSpeedLastTimeVal);
                    }
                    if (fenceType === "路线") {
                        if (travelLongTimeVal != null) {
                            $(td).find('input[name="travelLongTime"]').val(travelLongTimeVal);
                        }
                        if (travelSmallTimeVal != null) {
                            $(td).find('input[name="travelSmallTime"]').val(travelSmallTimeVal);
                        }
                    }
                    if (fenceType !== "路线") {
                        if (setOpenDoor === '0') {
                            $(td).find('input[name="openDoor' + index + '"][type="radio"]').eq(0).prop("checked", true);
                            $(td).find('input[name="openDoor' + index + '"][type="radio"]').eq(1).prop("checked", false)
                        } else if (setOpenDoor === '1') {
                            $(td).find('input[name="openDoor' + index + '"][type="radio"]').eq(1).prop("checked", true);
                            $(td).find('input[name="openDoor' + index + '"][type="radio"]').eq(0).prop("checked", false)
                        }
                        if (setCommunicationFlag === '0') {
                            $(td).find('input[name="communicationFlag' + index + '"][type="radio"]').eq(0).prop("checked", true);
                            $(td).find('input[name="communicationFlag' + index + '"][type="radio"]').eq(1).prop("checked", false)
                        } else if (setCommunicationFlag === '1') {
                            $(td).find('input[name="communicationFlag' + index + '"][type="radio"]').eq(1).prop("checked", true);
                            $(td).find('input[name="communicationFlag' + index + '"][type="radio"]').eq(0).prop("checked", false)
                        }
                        if (setgnssFlag === '0') {
                            $(td).find('input[name="gnssFlag' + index + '"][type="radio"]').eq(0).prop("checked", true);
                            $(td).find('input[name="gnssFlag' + index + '"][type="radio"]').eq(1).prop("checked", false)
                        } else if (setgnssFlag === '1') {
                            $(td).find('input[name="gnssFlag' + index + '"][type="radio"]').eq(1).prop("checked", true);
                            $(td).find('input[name="gnssFlag' + index + '"][type="radio"]').eq(0).prop("checked", false)
                        }
                    }

                    if (setalarmSource === '0' || setalarmSource === '2') { // 终端报警
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(1).prop("checked", true);
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(0).prop("checked", false);
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(2).prop("checked", false);

                        var y_tr = $(td).parent();
                        y_tr.find("td#sendFenceType").css('opacity', '1');
                        y_tr.find("td#alarmInDriver").css('opacity', '1');
                        y_tr.find("td#alarmOutDriver").css('opacity', '1');
                        y_tr.find("td#overSpeedLastTime").css('opacity', '1');
                        y_tr.find("td#travelLongTime").css('opacity', '1');
                        y_tr.find("td#travelSmallTime").css('opacity', '1');
                        y_tr.find("td#openDoor").css('opacity', '1');
                        y_tr.find("td#communicationFlag").css('opacity', '1');
                        y_tr.find("td#gnssFlag").css('opacity', '1');
                        if (setalarmSource === '2') {// 平台与终端
                            $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(0).prop("checked", true);
                            $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(1).prop("checked", false);
                        }
                    } else if (setalarmSource === '1') { // 平台报警
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(2).prop("checked", true);
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(0).prop("checked", false);
                        $(td).find('input[name="alarmSourceName' + index + '"][type="radio"]').eq(1).prop("checked", false);

                        var y_tr = $(td).parent();
                        y_tr.find("td#sendFenceType").css('opacity', '0');
                        y_tr.find("td#alarmInDriver").css('opacity', '0');
                        y_tr.find("td#alarmOutDriver").css('opacity', '0');
                        y_tr.find("td#overSpeedLastTime").css('opacity', '0');
                        y_tr.find("td#travelLongTime").css('opacity', '0');
                        y_tr.find("td#travelSmallTime").css('opacity', '0');
                        y_tr.find("td#openDoor").css('opacity', '0');
                        y_tr.find("td#communicationFlag").css('opacity', '0');
                        y_tr.find("td#gnssFlag").css('opacity', '0');
                    }
                });
            }
        }
    },
    fencetypepid: function (fencetype) {
        if (fencetype === "zw_m_marker") {
            return "标注";
        } else if (fencetype === "zw_m_line") {
            return "路线";
        } else if (fencetype === "zw_m_rectangle") {
            return "矩形";
        } else if (fencetype === "zw_m_circle") {
            return "圆形";
        } else if (fencetype === "zw_m_polygon") {
            return "多边形";
        } else if (fencetype === "zw_m_administration") {
            return "行政区划";
        } else if (fencetype === "zw_m_travel_line") {
            return "导航路线";
        }
    },
    laydateTime: function (e) {
        id = e.id;
        var offset = $("#" + id).offset();
        var height = $("#" + id).height();
        $("#hmsTime").show().css({
            "position": "absolute",
            "top": offset.top + height + 10 + "px",
            "left": offset.left + "px"
        });
    },
    checkboxis: function (checkbox) {
        $("#checkAll").attr("checked", false);
        if (checkbox.checked === true) {
            trid.push($("#" + checkbox.id).parents("tr").attr("id"));
        } else {
            trid = $.grep(trid, function (value, i) {
                return value !== $("#" + checkbox.id).parents("tr").attr("id");
            });
            return trid;
        }
    },

    bodyClickEvent: function (event) {
        if ($(event.target).parents("#hmsTime").length === 0 && event.target.id !== "hmsTime" && event.target.id.indexOf('TimeHMS') === -1) {
            $("#hmsTime").hide();
        }
        if ($(event.target).className !== 'ztreeModelBox' && $(event.target).parents(".ztreeModelBox").length === 0 && event.target.id.indexOf('FenceEnterprise') === -1) {
            $('.ztreeModelBox').hide();
        }
        var len = $(event.target).closest('#onlineSettingMenu').length;
        if (event.target.id !== 'onlineSetting' && len < 1 && event.target.id !== 'onlineSettingMenu') {
            $('#onlineSettingMenu').slideUp();
        }
    },
    hourseSelectClick: function () {
        hourseSelect = $(this).text();
        $("#hourseSelect").hide();
        $("#minuteSelect").show();
    },
    minuteSelectClick: function () {
        minuteSelect = $(this).text();
        $("#minuteSelect").hide();
        $("#secondSelect").show();
    },
    secondSelectClick: function () {
        secondSelect = $(this).text();
        $("#secondSelect").hide();
        $("#hourseSelect").show();
        $("#hmsTime").hide();
        time = hourseSelect + ":" + minuteSelect + ":" + secondSelect;
        $("#" + id).val(time);
    },
    // 提交(按围栏)
    fenceSaveBtnClick: function () {
        var arr = [];
        var errFlag = 1;
        var errMsg = "";
        var i = 0;
        // 遍历表格，组装Json
        $("#tableList tr").each(function () {
            var tr = this;
            var obj = {};
            $(tr).children("td").each(function () {
                var td = this;
                if (td.id === "fenceId") {
                    obj.fenceId = $(td).html();
                } else if (td.id === "vehicleId") {
                    obj.vehicleId = $(td).html();
                } else if (td.id === "monitorType") {
                    obj.monitorType = $(td).html();
                } else if (td.id === "sendFenceType") {
                    obj.sendFenceType = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "alarmSource") {
                    obj.alarmSource = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "alarmIn") {
                    obj.alarmInPlatform = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "alarmOut") {
                    obj.alarmOutPlatform = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "alarmInDriver") {
                    obj.alarmInDriver = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "alarmOutDriver") {
                    obj.alarmOutDriver = $(td).find("input[type='radio']:checked ").attr("value");
                } else if (td.id === "startTime") {
                    obj.alarmStartTime = $(td).find("input").val();
                } else if (td.id === "endTime") {
                    obj.alarmEndTime = $(td).find("input").val();
                } else if (td.id === "alarmStartDateTD") {
                    var time = $(td).find("input").val();
                    if (time != null && time !== "") {
                        obj.alarmStartDate = "2016-01-01 " + time;
                    } else {
                        obj.alarmStartDate = "";
                    }
                } else if (td.id === "alarmEndDateTD") {
                    var time = $(td).find("input").val();
                    if (time != null && time !== "") {
                        obj.alarmEndDate = "2016-01-01 " + time;
                    } else {
                        obj.alarmEndDate = "";
                    }
                } else if (td.id === "speed") {
                    obj.speed = $(td).find("input").val();
                } else if (td.id === "nightMaxSpeed") {
                    obj.nightMaxSpeed = $(td).find("input").val();
                } else if (td.id === "nightLimitTime") {
                    obj.nightLimitTime = $(td).find("input").val();
                } else if (td.id === "overSpeedLastTime") {
                    obj.overSpeedLastTime = $(td).find("input").val();
                } else if (td.id === "travelLongTime") {
                    var travelLongTimeVal = $(td).find("input").val();
                    if (travelLongTimeVal !== undefined && travelLongTimeVal != null) {
                        obj.travelLongTime = travelLongTimeVal;
                    } else {
                        obj.travelLongTime = "";
                    }
                } else if (td.id === "travelSmallTime") {
                    var travelSmallTimeVal = $(td).find("input").val();
                    if (travelSmallTimeVal !== undefined && travelSmallTimeVal != null) {
                        obj.travelSmallTime = travelSmallTimeVal;
                    } else {
                        obj.travelSmallTime = "";
                    }
                } else if (td.id === "openDoor") {
                    var openDoorVal = $(td).find("input[name='openDoor" + i + "']:checked ").attr("value");
                    if (openDoorVal !== undefined && openDoorVal != null && openDoorVal !== '') {
                        obj.openDoor = openDoorVal;
                    } else {
                        obj.openDoor = 2;
                    }
                } else if (td.id === "communicationFlag") {
                    var communicationFlagVal = $(td).find("input[type='radio']:checked ").attr("value");
                    if (communicationFlagVal !== undefined && communicationFlagVal != null && communicationFlagVal !== '') {
                        obj.communicationFlag = communicationFlagVal;
                    } else {
                        obj.communicationFlag = 2;
                    }
                } else if (td.id == "gnssFlag") {
                    var gnssFlagVal = $(td).find("input[type='radio']:checked ").attr("value");
                    if (gnssFlagVal != undefined && gnssFlagVal != null && gnssFlagVal != '') {
                        obj.gnssFlag = gnssFlagVal;
                    } else {
                        obj.gnssFlag = 2;
                    }
                }
            });
            var a = i;
            // 开始日期和结束日期要么都有，要么都没有
            if (obj.alarmStartTime !== "" && obj.alarmEndTime === "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请选择结束日期！<br/>';
            } else if (obj.alarmStartTime === "" && obj.alarmEndTime !== "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请选择开始日期！<br/>';
            } else if (obj.alarmStartTime !== "" && obj.alarmEndTime !== "") { // 结束日期必须大于开始日期
                if (fenceOperation.compareDate(obj.alarmStartTime, obj.alarmEndTime)) {
                    errFlag = 0;
                    errMsg += '第' + (a) + '条数据结束日期必须大于开始日期！<br/>';
                }
            }

            // 开始时间和结束时间要么都有，要么都没有
            if (obj.alarmStartDate !== "" && obj.alarmEndDate === "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请选择结束时间！<br/>';
            } else if (obj.alarmStartDate === "" && obj.alarmEndDate !== "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请选择开始时间！<br/>';
            } else if (obj.alarmStartDate !== "" && obj.alarmEndDate !== "") { // 结束日期必须大于开始日期
                if (fenceOperation.compareDate(obj.alarmStartDate, obj.alarmEndDate)) {
                    errFlag = 0;
                    errMsg += '第' + (a) + '条数据结束时间必须大于开始时间！<br/>';
                }
            }

            if (obj.speed < 0 || obj.speed > 65535) { // 限速校验最大值最小值
                errFlag = 0;
                errMsg += '第' + (a) + '条数限速的最小值为0，最大值为65535！<br/>';
            }
            if (obj.nightMaxSpeed < 0 || obj.nightMaxSpeed > 65535) { // 限速校验最大值最小值
                errFlag = 0;
                errMsg += '第' + (a) + '条数限速的最小值为0，最大值为65535！<br/>';
            }
            if (obj.overSpeedLastTime < 0 || obj.overSpeedLastTime > 65535) { // 超速持续时长校验最大值最小值
                errFlag = 0;
                errMsg += '第' + (a) + '条数超速持续时长的最小值为0，最大值为65535！<br/>';
            }
            // 行驶过长阈值，行驶不足阈值，要么都有，要么都没有
            if (obj.travelLongTime !== "" && obj.travelSmallTime === "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请输入行驶不足阈值！<br/>';
            } else if (obj.travelLongTime === "" && obj.travelSmallTime !== "") {
                errFlag = 0;
                errMsg += '第' + (a) + '条数据请输入行驶过长阈值！<br/>';
            }
            if (!jQuery.isEmptyObject(obj)) {
                arr.push(obj);
            }
            i++;
        });
        // ajax访问后端
        if (arr == null || arr.length === 0) {
            layer.msg(fenceOperationFenceBound, {move: false});
        } else if (errFlag === 0) {
            layer.msg(errMsg);
        } else {
            var url = "/clbs/m/functionconfig/fence/bindfence/saveBindFence";
            var parameter = {"data": JSON.stringify(arr)};
            json_ajax("POST", url, "json", true, parameter, fenceOperation.saveBindCallback);
        }
        // 清空勾选框
        vehicleFenceList = "";
    },
    fenceCancelBtnClick: function () {
        // 清空勾选框
        vehicleFenceList = "";
        $("#fenceBind").modal('hide');
        myTable.filter();
    },
    //保存围栏绑定回调方法
    saveBindCallback: function (data) {
        if (data != null) {
            if (data.success) {
                if (data.obj.flag === 1) {
                    $("#fenceBind").modal('hide');
                    var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
                    fenceOperation.getcheckFenceNode(zTree);
                    myTable.filter();
                    layer.msg(fenceOperationFenceBoundSuccess, {closeBtn: 0}, function () {
                        layer.close();
                    });
                } else if (data.obj.flag === 2) {
                    // layer.msg(data.obj.errMsg,{move:false});
                    layer.alert(data.obj.errMsg, {
                        id: "promptMessage"
                    });
                }
            } else {
                layer.msg(data.msg, {move: false});
            }
        }
    },
    //比较时间大小 a > b true
    compareDate: function (a, b) {
        var dateA = new Date(a);
        var dateB = new Date(b);
        if (isNaN(dateA) || isNaN(dateB)) {
            return false;
        }
        if (dateA > dateB) {
            return true;
        }
        return false;

    },
    TabCarBox: function () {
        monitoringObjMapHeight = $("#MapContainer").height();
        $("#MapContainer,#dimensionalMapContainer").css('height', fenceMapHeight + 'px');
        // $("#realTimeVideoReal").css('height', fenceMapHeight + 'px');
        $("#carInfoTable").hide();
        $("#dragDIV").hide();
        $("#fenceBindTable").show();
        // var bingLength = $('#dataTableBind tbody tr').length;
        // var treeObj = $.fn.zTree.getZTreeObj("fenceDemo");
        /*var checkNode = treeObj.getCheckedNodes(true);
        if (checkNode.length === 0) {
            $("#MapContainer").css("height", newMapHeight + 'px');
        } else {
            if ($('#bingListClick i').hasClass('fa fa-chevron-down')) {
                if (bingLength === 0) {
                    $("#MapContainer").css("height", newMapHeight + 'px');
                } else {
                    $("#MapContainer").css('height', (newMapHeight - 80 - 44 * bingLength - 105) + 'px');
                }
            } else {
                $("#MapContainer").css("height", newMapHeight + 'px');p
            }
        }*/
        // 订阅电子围栏
        if (clickFenceCount === 0) {
            webSocket.subscribe(headers, "/user/topic/electronic_fence", fenceOperation.updataFenceData, "", null);
        }
        clickFenceCount = 1;
    },
    TabFenceBox: function () {
        $("#dragDIV").show();
        fenceMapHeight = $("#MapContainer").height();
        $("#MapContainer,#dimensionalMapContainer").css('height', monitoringObjMapHeight + 'px');
        // $("#realTimeVideoReal").css('height', monitoringObjMapHeight + 'px');
        $("#carInfoTable").show();
        $("#fenceBindTable").hide();
        $("body").css("overflow", 'hidden');
        $(document).scrollTop(0);
    },
    parametersTrace: function (data) {
        if (data.success) {
            layer.msg("开始跟踪！");
            $("#goTrace").modal("hide");
        } else {
            layer.msg(data.msg);
        }
    },
    updataFenceData: function (msg) {
        if (msg != null) {
            var result = $.parseJSON(msg.body);
            if (result != null) {
                myTable.refresh();
            }
        }
    },
    // 下发围栏 （单个）
    sendFenceOne: function (id, paramId, vehicleId, fenceId) {
        var arr = [];
        var obj = {};
        obj.fenceConfigId = id;
        obj.paramId = paramId;
        obj.vehicleId = vehicleId;
        obj.fenceId = fenceId;
        arr.push(obj);
        var jsonStr = JSON.stringify(arr);
        fenceOperation.sendFence(jsonStr);
    },
    // 下发围栏
    sendFence: function (sendParam) {
        var url = "/clbs/m/functionconfig/fence/bindfence/sendFence";
        var parameter = {"sendParam": sendParam};
        json_ajax("POST", url, "json", true, parameter, fenceOperation.sendFenceCallback);
    },
    // 围栏下发回调
    sendFenceCallback: function (data) {
        layer.msg(fenceOperationFenceIssue, {closeBtn: 0}, function (refresh) {
            //取消全选勾
            $("#checkAll").prop('checked', false);
            $("input[name=subChk]").prop("checked", false);
            myTable.refresh(); //执行的刷新语句
            layer.close(refresh);
        });
    },
    //数据表格围栏显示
    tableFence: function (id) {
        var treeObj = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodesArray = [];
        var nodes = treeObj.getNodeByParam("id", id, null);
        nodesArray.push(nodes);
        fenceOperation.getFenceDetail(nodesArray, map);
    },
    // 批量下发
    sendModelClick: function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum === 0) {
            layer.msg(fenceOperationDataNull);
            return
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj);
        });
        // 下发
        fenceOperation.sendFence(JSON.stringify(checkedList));
    },
    //批量删除
    delModelClick: function () {
        //判断是否至少选择一项
        var chechedNum = $("input[name='subChk']:checked").length;
        if (chechedNum === 0) {
            layer.msg(fenceOperationDataNull);
            return
        }
        var checkedList = new Array();
        $("input[name='subChk']:checked").each(function () {
            var jsonStr = $(this).val();
            var jsonObj = $.parseJSON(jsonStr);
            checkedList.push(jsonObj.fenceConfigId);
        });
        myTable.deleteItems({
            'deltems': checkedList.toString()
        });
    },
    updateFollowPath: function (id, location) {
        if (!id || id !== followId) {
            return;
        }
        followLocation.push(location);
        followPath.setPath(followLocation);
    },
    //跟踪
    goTrace: function (id) {
        // 清空尾迹
        followId = id;
        followLocation = [];
        var currentMarker = carNameMarkerMap.get(id);
        if (currentMarker) {
            followLocation.push(currentMarker.getPosition());
        }
        if (followPath) {
            map.remove(followPath);
        }
        followPath = map.polyline({
            map: map,
            path: followLocation,
            strokeColor: "#428bca", //线颜色
            strokeOpacity: 0.9, //线透明度
            strokeWeight: 6, //线宽
            strokeStyle: "solid", //线样式
            zIndex: 60,
            showDir: true
        });

        parametersID = id;
        var listParameters = [];
        listParameters.push(parametersID);
        var validity = $("#validity").val();
        var interval = $("#interval").val();
        listParameters.push(interval);
        listParameters.push(validity);
        var url = "/clbs/v/monitoring/parametersTrace";
        var parameters = {"parameters": listParameters};
        ajax_submit("POST", url, "json", true, parameters, true, fenceOperation.parametersTrace);
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    //F3跟踪
    goF3Trace: function (id) {
        parametersID = id;
        var validity = $("#validity").val();
        var interval = $("#interval").val();
        var url = "/clbs/v/monitoringLong/sendParam";
        var parameters = {"vid": parametersID, "longValidity": validity, "longInterval": interval, "orderType": 19};
        ajax_submit("POST", url, "json", true, parameters, true, fenceOperation.parametersTrace);
        setTimeout("dataTableOperation.logFindCilck()", 500);
    },
    // 围栏绑定列表模糊搜索
    searchBindTable: function () {
        myTable.requestData();
    },
    //修改矩形取消
    rectangleEditClose: function () {
        mouseTool.close(true);
        mouseToolEdit.close(true);
    },
    //更新围栏预处理函数
    ajaxFenceDataFilter: function (treeId, parentNode, responseData) {
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = false;
                responseData[i].name = html2Escape(responseData[i].name);
                if (responseData[i].type === "fence" && fenceIdArray.indexOf(responseData[i].id) !== -1) {
                    responseData[i].checked = true;
                }
                if (responseData[i].type === "fenceParent" && fenceOpenArray.indexOf(responseData[i].id) !== -1) {
                    responseData[i].open = true;
                }
                if (responseData[i].type === "fenceParent" && responseData[i].id === saveFenceType) {
                    responseData[i].open = true;
                }
                if (responseData[i].markIcon === 1) {
                    responseData[i].iconSkin = 'zw_m_marker_circle_skin';
                }
            }
            // 图层
            var fenceLayer = {name: "图层", pId: "0", id: "zw_m_travel_layer", type: "fenceParent", open: false};
            // 图层1
            var firstFence = {
                // fenceInfoId: "7537030d-7a0e-4853-9102-8efb480305c0",
                iconSkin: "zw_m_rectangle_skin",
                name: "图层1",
                pId: "zw_m_travel_layer",
                // id: "25a69a35-f7f5-4609-8c31-f215cba7c78e",
                type: "fence",
                open: false,
            };
            var secondFence = {
                // fenceInfoId: "7537030d-7a0e-4853-9102-8efb480305c0",
                iconSkin: "zw_m_rectangle_skin",
                name: "图层2",
                pId: "zw_m_travel_layer",
                // id: "25a69a35-f7f5-4609-8c31-f215cba7c78e",
                type: "fence",
                open: false,
            };
            responseData.push(fenceLayer);
            responseData.push(firstFence);
            responseData.push(secondFence);
        }
        return responseData;
    },
    //更新围栏成功函数
    zTreeOnAsyncFenceSuccess: function (event, treeId, treeNode, msg) {
        var rectang_fenceId = $("#rectangleId").val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getNodesByParam("id", rectang_fenceId, null);
        if (nodes.length !== 0) {
            fenceOperation.getFenceDetail(nodes, map);
        }
        $("#rectangleId").val('');
        var fenceNode = zTree.getNodesByParam('id', saveFenceType, null);
        if (fenceNode !== undefined && fenceNode != null && fenceNode.length > 0) {
            var childrenNode = fenceNode[0].children;
            if (childrenNode !== undefined) {
                for (var i = 0, len = childrenNode.length; i < len; i++) {
                    if (saveFenceName === childrenNode[i].name) {
                        zTree.checkNode(childrenNode[i], true, true);
                        zTree.selectNode(childrenNode[i]); //选中第一个父节点下面第一个子节点
                        fenceCheckLength = zTree.getCheckedNodes(true).length;
                        childrenNode[i].checkedOld = true;
                        fenceOperation.getFenceDetail([childrenNode[i]], map);
                    }
                }
            }
        }
    },
    //标注取消
    markFenceClose: function () {
        mouseTool.close(true);
        var markFenceID = $("#markerId").val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getNodesByParam("id", markFenceID, null);
        fenceOperation.getFenceDetail(nodes, map);
    },
    moveMarker: function (e) {
        $("#addOrUpdateMarkerFlag").val("1"); // 修改标注，给此文本框赋值为1
        $("#markerId").val(moveMarkerFenceId); // 标注id
        // 标注修改框弹出时给文本框赋值
        $("#markerName").val(moveMarkerBackData.name);
        $("#markerType").val(moveMarkerBackData.type);
        $("#markerDescription").val(moveMarkerBackData.description);
        if (map.currentMap === 'baidu') {// 将百度地图经纬度转换为高德地图经纬度
            var newPoint = map.bMapTransAMap(e.latLng.lng, e.latLng.lat);
            e.lnglat = {
                lng: newPoint[0],
                lat: newPoint[1]
            }
        } else if (map.currentMap === 'tMap') {// 将天地图经纬度转换为高德地图经纬度
            var newPoint = map.tianToGaoLnglat(e.lnglat.lng, e.lnglat.lat);
            e.lnglat = {
                lng: newPoint[0],
                lat: newPoint[1]
            }
        }
        $("#mark-lng").attr("value", e.lnglat.lng);
        $("#mark-lat").attr("value", e.lnglat.lat);
        $('#markerLng').val(e.lnglat.lng);
        $('#markerLat').val(e.lnglat.lat);
        pageLayout.closeVideo();
        $('#mark').modal('show');
        polyFence.off("mouseup", fenceOperation.moveMarker);
    },
    //线路取消
    lineEditClose: function () {
        mouseTool.close(true);
        map.off(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", amendLine);
        var lineFenceID = $("#lineId").val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getNodesByParam("id", lineFenceID, null);
        isEdit = true;
        fenceOperation.getFenceDetail(nodes, map);
    },
    //圆取消
    circleFenceClose: function () {
        mouseTool.close(true);
        var circleFenceID = $("#circleId").val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getNodesByParam("id", circleFenceID, null);
        fenceOperation.getFenceDetail(nodes, map);
    },
    //多边形取消
    polygonFenceClose: function () {
        mouseTool.close(true);
        var polygonFenceID = $("#polygonId").val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var nodes = zTree.getNodesByParam("id", polygonFenceID, null);
        fenceOperation.getFenceDetail(nodes, map);
    },
    //数组大小排序
    sortNumber: function (a, b) {
        return a - b;
    },
    // 报警区分平台
    alarmSourceCheck: function (fencetype, alarmSourceName) {
        if (fencetype === "zw_m_administration" || fencetype === "zw_m_travel_line") {
            return "<td id = 'alarmSource'>" +
                "<label style='margin-bottom:0px;cursor:pointer'>" +
                "<input type='radio' value='2' name='" + alarmSourceName + "'>平台与终端 </label>" +
                "<input type='radio' disabled value='0' name='" + alarmSourceName + "' id='" + alarmSourceName + "'>" +
                "<label style='margin-bottom:0px;cursor:pointer' for='" + alarmSourceName + "'>终端报警 </label>" +
                "<input type='radio' value='1' checked='checked' name='" + alarmSourceName + "' id='" + alarmSourceName + "s'>" +
                "<label style='margin-bottom:0px;cursor:pointer' for='" + alarmSourceName + "s'>平台报警 </label>" +
                "</td>"
        } else {
            return "<td id = 'alarmSource'>" +
                "<label style='margin-bottom:0px;cursor:pointer'>" +
                "<input type='radio' value='2' name='" + alarmSourceName + "'>平台与终端</label>" +
                "<input type='radio' value='0' checked='checked' name='" + alarmSourceName + "' id='" + alarmSourceName + "'>" +
                "<label style='margin-bottom:0px;cursor:pointer' for='" + alarmSourceName + "'>终端报警</label>" +
                "<input type='radio' value='1' name='" + alarmSourceName + "' id='" + alarmSourceName + "s'>" +
                "<label style='margin-bottom:0px;cursor:pointer' for='" + alarmSourceName + "s'>平台报警</label>" +
                "</td>"
        }
    },
    sendFenceTypeTd: function (fencetype, sendFenceTypeName) {
        if (fencetype !== "zw_m_line" && fencetype !== "zw_m_polygon" && fencetype !== "zw_m_travel_line" && fencetype !== "zw_m_administration") {
            return "<td id = 'sendFenceType'><input type='radio' value = 0 checked name='" + sendFenceTypeName + "' id='" + sendFenceTypeName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + sendFenceTypeName + "'>更新</label><input type='radio' value = 1 name='" + sendFenceTypeName + "' id='" + sendFenceTypeName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + sendFenceTypeName + "s'>追加</label><input type='radio' value = 2 name='" + sendFenceTypeName + "' id='" + sendFenceTypeName + "ss'><label style='margin-bottom:0px;cursor:pointer' for='" + sendFenceTypeName + "ss'>修改</label></td>";
        }
        return "<td id = 'sendFenceType'><input type='radio' disabled value = 0 checked name='" + sendFenceTypeName + "'>更新</td>"

    },
    //判断绑定详细列表中行驶时间阈值过长或者不足是否需要填写
    travelLongAndSmallTime: function (fencetype) {
        if (fencetype === "zw_m_line" || fencetype === "zw_m_travel_line") {
            return "<td id='travelLongTime'><input type= 'text' name = 'travelLongTime' class='form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" /></td>" + "<td id='travelSmallTime'><input type= 'text' name = 'travelSmallTime' class='form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" /></td>";
        }
        return "<td id='travelLongTime'></td>" + "<td id='travelSmallTime'></td>";

    },
    //判断绑定详细列表中最后三项是否需要显示
    otherInfo: function (fencetype, openDoorName, communicationFlagName, gnssFlagName) {
        if (fencetype != "zw_m_line" && fencetype != "zw_m_travel_line") {
            return "<td id = 'openDoor'><input type='radio' value = 0 checked name='" + openDoorName + "' id='" + openDoorName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + openDoorName + "'>是</label><input type='radio' value =1 name='" + openDoorName + "' id='" + openDoorName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + openDoorName + "s'>否</lebal></td>"
                + "<td id = 'communicationFlag'><input type='radio'  value = 0 checked name='" + communicationFlagName + "' id='" + communicationFlagName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + communicationFlagName + "'>是</label><input type='radio'  value = 1 name='" + communicationFlagName + "' id='" + communicationFlagName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + communicationFlagName + "s'>否</lebal></td>"
                + "<td id = 'gnssFlag'><input type='radio' value = 0 checked name='" + gnssFlagName + "' id='" + gnssFlagName + "'><label style='margin-bottom:0px;cursor:pointer' for='" + gnssFlagName + "'>是</label><input type='radio' value = 1 name='" + gnssFlagName + "' id='" + gnssFlagName + "s'><label style='margin-bottom:0px;cursor:pointer' for='" + gnssFlagName + "s'>否</lebal></td>";
        }
        return "<td id = 'openDoor'></td>"
            + "<td id = 'communicationFlag'></td>"
            + "<td id = 'gnssFlag'></td>";

    },
    //收缩绑定列表
    bingListClick: function () {
        if ($(this).children('i').hasClass('fa-chevron-down')) {
            $(this).children('i').removeClass('fa-chevron-down').addClass('fa-chevron-up');
            $("#MapContainer,#dimensionalMapContainer").animate({'height': newMapHeight + "px"});
        } else {
            $(this).children('i').removeClass('fa-chevron-up').addClass('fa-chevron-down');
            var trLength = $('#dataTableBind tbody tr').length;
            $("#MapContainer,#dimensionalMapContainer").animate({'height': (winHeight - 80 - trLength * 46 - 220) + "px"});
        }
    },
    addaskQuestions: function () {
        addaskQuestionsIndex++;
        var html = '<div class="form-group" id="answer-add_' + addaskQuestionsIndex + '"><label class="col-md-3 control-label">答案：</label><div class="col-md-5"><input type="text" placeholder="请输入答案" class="form-control" name="value" id=""/><label class="error">请输入答案</label></div><div class="col-md-1"><button type="button" class="btn btn-danger answerDelete deleteIcon"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button></div></div>';
        $("#answer-add-content").append(html);
        $(".answerDelete").on("click", function () {
            $(this).parent().parent().remove();
        });
    },
    //分段限速取消
    sectionRateLimitingClose: function (id) {
        fenceOperation.clearErrorMsg();
        var sectionLineID;
        if (typeof(id) === 'object') {
            sectionLineID = $("#lineIDms1").attr("value");
        } else {
            sectionLineID = id;
        }
        if (sectionMarkerPointArray.containsKey(sectionLineID)) {
            var thisValue = sectionMarkerPointArray.get(sectionLineID);
            thisValue[1] = false;
            sectionMarkerPointArray.remove(sectionLineID);
        }
        if (fenceSectionPointMap.containsKey(sectionLineID)) {
            fenceSectionPointMap.remove(sectionLineID);
        }
        if (sectionPointMarkerMap.containsKey(sectionLineID)) {
            var pointArray = sectionPointMarkerMap.get(sectionLineID);
            map.remove(pointArray);
            sectionPointMarkerMap.remove(sectionLineID);
            $("#tenples").html('');
            $("#pagecontent").html('');
        }
    },
    //围栏详情
    fenceDetails: function (data) {
        var fenceData = data[0].fenceData;
        var fenceType = data[0].fenceType;
        var detailsFenceShape = fenceOperation.fencetypepid(fenceType);
        var detailsFenceName;
        var detailsFenceType;
        var detailsFenceCreateName;
        var detailsFenceCreateTime;
        if (fenceType === 'zw_m_line') {
            detailsFenceType = data[0].line.type;
            detailsFenceDescribe = data[0].line.description;
            if (detailsFenceDescribe === "" || detailsFenceDescribe == null) {
                detailsFenceDescribe = "无任何描述"
            }
            detailsFenceCreateName = data[0].line.createDataUsername;
            detailsFenceCreateTime = data[0].line.createDataTime;
        } else if (fenceType === 'zw_m_polygon') {
            detailsFenceType = data[0].polygon.type;
            detailsFenceDescribe = data[0].polygon.description;
            if (detailsFenceDescribe === "" || detailsFenceDescribe == null) {
                detailsFenceDescribe = "无任何描述"
            }
            detailsFenceCreateName = data[0].polygon.createDataUsername;
            detailsFenceCreateTime = data[0].polygon.createDataTime;
        } else if (fenceType === "zw_m_administration") {
            detailsFenceType = "行政区域";
            detailsFenceDescribe = data[0].administration.description === '' ? '无任何描述' : data[0].administration.description;
            detailsFenceCreateName = data[0].administration.createDataUsername;
            detailsFenceCreateTime = data[0].administration.createDataTime;
        } else if (fenceType === "zw_m_travel_line") {
            detailsFenceShape = '导航路线';
            detailsFenceType = data[0].travelLine.lineType;
            detailsFenceCreateName = data[0].travelLine.createDataUsername;
            detailsFenceCreateTime = data[0].travelLine.createDataTime;
            detailsFenceDescribe = data[0].travelLine.description;
        } else {
            detailsFenceType = fenceData.type;
            detailsFenceCreateName = fenceData.createDataUsername;
            detailsFenceCreateTime = fenceData.createDataTime;
            detailsFenceDescribe = fenceData.description === '' ? '无任何描述' : fenceData.description;
        }
        $("#detailsFenceShape").text(detailsFenceShape || '');
        $("#detailsFenceType").text(detailsFenceType || '');
        $("#detailsFenceCreateName").text(detailsFenceCreateName || '');
        $("#detailsFenceCreateTime").text(detailsFenceCreateTime);
        $("#detailsFenceDescribe").text(detailsFenceDescribe || '');
    },
    //清空围栏绑定
    clearFenceBind: function () {
        $("#searchVehicle").val('');
        $("#tableList tbody").html('');
    },
    //添加拐点
    addLngLat: function () {
        var id = $(this).parent('.sectionLngLat').parent('div').parents('div').attr('id');
        var thisArea = $(this).parent('div.sectionLngLat').clone(true);
        var lastSectionLngLat = $(this).parent('.sectionLngLat').parent('div').children('.sectionLngLat:last-child');
        var this_lng_id = lastSectionLngLat.children('.sectionLng').children('input').attr('id') + 1;
        var this_lat_id = lastSectionLngLat.children('.sectionLat').children('input').attr('id') + 1;
        thisArea.children('div.sectionLng').children('input').attr('value', '').val('').attr('id', this_lng_id).siblings('label.error').remove();
        thisArea.children('div.sectionLat').children('input').attr('value', '').val('').attr('id', this_lat_id).siblings('label.error').remove();
        thisArea.children('button').attr('class', 'btn btn-danger removeLngLat').children('span').attr('class', 'glyphicon glyphicon-trash');
        $("#" + id).children('div.pointList').append(thisArea);
        $(".removeLngLat").unbind("click").bind("click", fenceOperation.removeLngLat);
    },
    //删除拐点
    removeLngLat: function () {
        $(this).parent('.sectionLngLat').remove();
    },
    //是否显示拐点
    isQueryShow: function () {
        if ($(this).next('div.pointList').is(':hidden')) {
            $(this).children('label').children('span').attr('class', 'fa fa-chevron-down');
            $(this).next('div.pointList').slideDown();
        } else {
            $(this).children('label').children('span').attr('class', 'fa fa-chevron-up');
            $(this).next('div.pointList').slideUp();
        }
    },
    //围栏经纬度区域显示
    lngLatTextShow: function () {
        var $pointList = $(this).parent('div').next('div.pointList');
        if ($pointList.is(':hidden')) {
            $(this).children('span').attr('class', 'fa fa-chevron-down');
            $pointList.slideDown();
        } else {
            $(this).children('span').attr('class', 'fa fa-chevron-up');
            $pointList.slideUp();
        }
    },
    //行政区域选择
    administrativeAreaSelect: function (obj) {
        var provin = $("#province").val();
        if (provin === "province") {
            $("#provinceError").css("display", "none");
        }
        else if (provin === "--请选择--") {
            $("#provinceError").css("display", "block");
        }
        for (var i = 0, l = administrativeAreaFence.length; i < l; i++) {
            administrativeAreaFence[i].setMap(null);
        }
        var option = obj[obj.options.selectedIndex];
        var keyword = option.text; //关键字
        var adcode = option.adcode;
        district.setLevel(option.value); //行政区级别
        district.setExtensions('all');
        //行政区查询
        //按照adcode进行查询可以保证数据返回的唯一性
        district.search(keyword, function (status, result) {
            if (status === 'complete') {
                fenceOperation.getData(result.districtList[0]);
            }
        });
    },
    //行政区域选择后数据处理
    getData: function (data) {
        var bounds = data.boundaries;
        if (bounds) {
            $('#administrativeLngLat').val(bounds.join('-'));
            for (var i = 0, l = bounds.length; i < l; i++) {
                var polygon = map.polygon({
                    map: map,
                    strokeWeight: 1,
                    strokeColor: '#CC66CC',
                    fillColor: '#CCF3FF',
                    fillOpacity: 0.5,
                    path: bounds[i]
                });
                administrativeAreaFence.push(polygon);
                map.setFitView(polygon);//地图自适应
            }
        }
        var subList = data.districtList;
        var level = data.level;
        //清空下一级别的下拉列表
        if (level === 'province') {
            document.getElementById('city').innerHTML = '';
            document.getElementById('district').innerHTML = '';
        } else if (level === 'city') {
            document.getElementById('district').innerHTML = '';
        } else if (level === 'district') {
        }
        if (subList) {
            var contentSub = new Option('--请选择--');
            for (var i = 0, l = subList.length; i < l; i++) {
                var name = subList[i].name;
                var levelSub = subList[i].level;
                if (levelSub === 'street') {
                    return false;
                }
                var cityCode = subList[i].citycode;
                if (i === 0) {
                    document.querySelector('#' + levelSub).add(contentSub);
                }
                contentSub = new Option(name);
                contentSub.setAttribute("value", levelSub);
                contentSub.center = subList[i].center;
                contentSub.adcode = subList[i].adcode;
                document.querySelector('#' + levelSub).add(contentSub);
            }
        }
    },
    //行政区域保存
    administrativeSave: function () {
        var province = $("#province").find('option:selected').text();
        $("#provinceVal").val(province);
        var city = $("#city").find('option:selected').text();
        $("#cityVal").val(city);
        var district = $("#district").find('option:selected').text();
        $("#districtVal").val(district);
        var provin = $("#province").val();
        if (provin === "--请选择--") {
            $("#provinceError").css("display", "block");
            return false;
        }
        if (fenceOperation.validate_administration()) {
            $("#administrativeSave").attr("disabled", "disabled");
            $("#administrativeSave").text("保存中");
            layer.load(2);
            $("#administration").ajaxSubmit(function (data) {
                var datas = eval("(" + data + ")");
                if (datas.success === true) {
                    layer.closeAll('loading');
                    $("#administrativeArea").modal("hide");
                    saveFenceName = $('#administrationName').val();
                    saveFenceType = 'zw_m_administration';
                    $(".fenceA").removeClass("fenceA-active");
                    mouseTool.close(true);
                    fenceOperation.addNodes();
                    $("#administrationName").val("");
                    $("#administrationDistrict").val("");
                    $("#administrativeSave").text("保存");
                    $("#administrativeSave").removeAttr("disabled");
                    fenceOperation.administrativeClose();
                } else {
                    datas.msg === null ? layer.msg(fenceOperationJudgementASExist) : layer.msg(datas.msg, {move: false});
                    $("#administrativeSave").text("保存");
                    $("#administrativeSave").removeAttr("disabled");
                    layer.closeAll('loading');
                }
            });
        }
    },
    //行政区域添加时验证
    validate_administration: function () {
        return $("#administration").validate({
            rules: {
                province: {
                    required: true,

                },
                name: {
                    required: true,
                    maxlength: 20
                },
                description: {
                    maxlength: 50
                },
                groupName: {
                    required: true
                }
            },
            messages: {
                name: {
                    required: areaNameNull,
                    maxlength: publicSize20
                },
                description: {
                    maxlength: publicSize50
                },
                groupName: {
                    required: publicSelectGroupNull
                }
            }
        }).form();
    },
    //行政区域取消
    administrativeClose: function () {
        for (var i = 0, l = administrativeAreaFence.length; i < l; i++) {
            administrativeAreaFence[i].setMap(null);
        }
        $("#provinceError").hide();
    },
    //显示行政区域
    drawAdministration: function (data, aId, showMap) {
        var polygonAarry = [];
        if (administrationMap.containsKey(aId)) {
            var this_fence = administrationMap.get(aId);
            map.remove(this_fence);
            administrationMap.remove(aId);
        }
        for (var i = 0, l = data.length; i < l; i++) {
            var polygon = map.polygon({
                map: map,
                strokeWeight: 1,
                strokeColor: '#CC66CC',
                fillColor: '#CCF3FF',
                fillOpacity: 0.5,
                path: data[i]
            });
            polygonAarry.push(polygon);
            administrativeAreaFence.push(polygon);
        }
        administrationMap.put(aId, polygonAarry);
        map.setFitView(polygonAarry);//地图自适应
    },
    //添加途经点
    addWayToPoint: function (msg) {
        var length = $('#wayPointArea').children('div').length;
        var searchId = 'wayPoint' + (length + 1);
        var html = '<div class="form-group">'
            + '<div class="col-md-10">'
            + '<input type="text" id="' + searchId + '" placeholder="请输入途经点(或右键地图)" class="form-control wayPoint" name="wayPoint" />'
            + '</div>'
            + '<button type="button" class="btn btn-danger padBottom deleteWayPoint"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>'
            + '</div>';
        $(html).appendTo($('#wayPointArea'));
        $('#' + searchId).inputClear().on('onClearEvent', fenceOperation.wayPointInputClear);
        if (Array.isArray(msg)) {
            $('#' + searchId).val(msg[0]).attr('data-address', msg[0]).attr('data-lnglat', msg[2]);
        }
        var wayPoint = map.autoComplete({
            input: searchId
        });
        wayPoint.on('select', fenceOperation.dragRoute);
        $('.deleteWayPoint').off('click').on('click', fenceOperation.deleteWayPoint);
    },
    //途经点删除
    deleteWayPoint: function () {
        $(this).parent('div.form-group').remove();
        fenceOperation.dragRoute(null);
    },
    //隐藏区域划分
    hideFence: function (id) {
        if (administrationMap.containsKey(id)) {
            var this_fence = administrationMap.get(id);
            map.remove(this_fence);
            administrationMap.remove(id);
        }
        //行驶路线travelLineMap
        if (travelLineMap.containsKey(id)) {
            var this_fence = travelLineMap.get(id);
            map.remove(this_fence);
            travelLineMap.remove(id);
        }
    },
    //路径规划
    dragRoute: function (data) {
        var addressArray = [];
        if (data != null && data !== 'drag') {
            var this_input_id = $(this)[0].id || $(this)[0].input.id;
            $("#" + this_input_id).attr('data-address', data.poi.district + data.poi.name).removeAttr('data-lnglat');
        }
        var startAddress = $('#startPoint').attr('data-address');
        var start_lnglat = $('#startPoint').attr('data-lnglat');
        var endAddress = $('#endPoint').attr('data-address');
        var end_lnglat = $('#endPoint').attr('data-lnglat');
        if (startAddress !== '' && endAddress !== '' && startAddress !== undefined && endAddress !== undefined) {
            if (lineRoute !== undefined) {
                lineRoute.destroy();//销毁拖拽导航插件
            }
            if (start_lnglat !== undefined) {
                addressArray.push(start_lnglat);
            } else {
                addressArray.push(startAddress);
            }
            $('#wayPointArea input').each(function () {
                var this_value = $(this).val();
                if (this_value !== '') {
                    var value = $(this).attr('data-address');
                    var lnglat = $(this).attr('data-lnglat');
                    if (lnglat !== undefined) {
                        addressArray.push(lnglat);
                    } else {
                        addressArray.push(value);
                    }
                } else {
                    $(this).parent('div').parent('div').remove();
                }
            });
            if (end_lnglat !== undefined) {
                addressArray.push(end_lnglat);
            } else {
                addressArray.push(endAddress);
            }
            var lngLatArray = [];
            fenceOperation.getAddressLngLat(addressArray, 0, lngLatArray);
        }
    },
    //地理编码
    getAddressLngLat: function (addressArray, index, lngLatArray) {
        var this_address = addressArray[index];
        if (fenceOperation.isChineseChar(this_address)) {
            var geocoder = map.geocoder({
                city: "全国", //城市，默认：“全国”
                radius: 500 //范围，默认：500
            });
            geocoder.getLocation(this_address);
            geocoder.on('complete', function (GeocoderResult) {
                if (GeocoderResult.type === 'complete') {
                    var this_lng = GeocoderResult.geocodes[0].location.lng;
                    var this_lat = GeocoderResult.geocodes[0].location.lat;
                    lngLatArray.push([this_lng, this_lat]);
                    index++;
                    if (index === addressArray.length) {
                        fenceOperation.madeDragRoute(lngLatArray);
                    } else {
                        fenceOperation.getAddressLngLat(addressArray, index, lngLatArray);
                    }
                }
            });
        } else {
            index++;
            lngLatArray.push(this_address.split(';'));
            if (index === addressArray.length) {
                fenceOperation.madeDragRoute(lngLatArray);
            } else {
                fenceOperation.getAddressLngLat(addressArray, index, lngLatArray);
            }
        }
    },
    //开始路径规划
    madeDragRoute: function (array) {
        isDragRouteFlag = false;
        lineRoute = map.dragRoute(map, array); //构造拖拽导航类
        lineRoute.search(); //查询导航路径并开启拖拽导航
        //路径规划完成
        lineRoute.on('complete', fenceOperation.dragRouteComplete);
    },
    //行驶路线关闭
    lineDragRouteClose: function () {
        var dragRouteId = $('#travelLineId').val();
        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
        var node = zTree.getNodeByParam('id', dragRouteId, null);
        fenceOperation.getFenceDetail([node], map);
        fenceOperation.closeDragRoute();
        if (lineRoute !== undefined) {
            lineRoute.destroy();
        }
        fenceOperation.clearLineDragRoute();
        isDragRouteFlag = false;
    },
    //清空行驶路线input
    clearLineDragRoute: function () {
        $('#drivenRoute').hide();
        $('#drivenRoute input').each(function () {
            $(this).val('').attr('data-address', '').removeAttr('data-lnglat');
        });
        $('#wayPointArea').html('');
        $('#dragRouteDescription').val('');
        var start_point = dragPointMarkerMap.get('0');
        var end_point = dragPointMarkerMap.get('2');
        var wayPoint = dragPointMarkerMap.get('1');
        if (start_point !== undefined) {
            map.remove([start_point]);
        }
        if (end_point !== undefined) {
            map.remove([end_point]);
        }
        if (wayPoint !== undefined) {
            map.remove([wayPoint]);
        }
        dragPointMarkerMap.clear();
    },
    //行驶路线保存
    lineDragRouteSave: function () {
        if (isDragRouteFlag) {
            if (fenceOperation.validate_dragRoute()) {
                $("#dragRouteLine").ajaxSubmit(function (data) {
                    var datas = eval("(" + data + ")");
                    if (datas.success === true) {
                        var dragRouteId = $('#travelLineId').val();
                        saveFenceName = $('#dragRouteLineName').val();
                        saveFenceType = 'zw_m_travel_line';
                        var zTree = $.fn.zTree.getZTreeObj("fenceDemo");
                        var node = zTree.getNodeByParam('id', dragRouteId, null);
                        fenceOperation.getFenceDetail([node], map);
                        fenceOperation.closeDragRoute();
                        fenceOperation.addNodes();
                        fenceOperation.clearLineDragRoute();
                        if (lineRoute !== undefined) {
                            lineRoute.destroy();//销毁拖拽导航插件
                        }
                    } else {
                        if (datas.msg == null) {
                            layer.msg(fenceOperationTravelLineExist);
                        } else {
                            layer.msg(datas.msg, {move: false});
                        }
                    }
                });
            }
        } else {
            layer.msg(fenceOperationTravelLineError);
        }
    },
    //预览行驶路线
    drawTravelLine: function (data, thisMap, travelLine, wayPointArray) {
        $('#drivenRoute').hide();
        if (lineRoute !== undefined) {
            lineRoute.destroy();
        }
        var lineID = travelLine.id;
        var path = [];
        var start_point_value = [travelLine.startLongitude, travelLine.startLatitude];
        var end_point_value = [travelLine.endLongitude, travelLine.endLatitude];
        var wayValue = [];
        if (wayPointArray !== undefined) {
            for (var j = 0, len = wayPointArray.length; j < len; j++) {
                wayValue.push([wayPointArray[j].longitude, wayPointArray[j].latitude]);
            }
        }
        for (var i = 0, len = data.length; i < len; i++) {
            path.push([data[i].longitude, data[i].latitude]);
        }
        if (travelLineMap.containsKey(lineID)) {
            var this_line = travelLineMap.get(lineID);
            map.remove([this_line]);
            travelLineMap.remove(lineID);
        }
        var polyFencec = map.polyline({
            path: path, //设置线覆盖物路径
            strokeColor: "#3366FF", //线颜色
            strokeOpacity: 1, //线透明度
            strokeWeight: 5, //线宽
            strokeStyle: "solid", //线样式
            strokeDasharray: [10, 5],
            zIndex: 51,
            map: map,
        });
        // polyFencec.setMap(map);
        map.setFitView(polyFencec);
        travelLineMap.put(lineID, polyFencec);
    },
    //路线规划逆地理编码
    getAddressValue: function (array, index, addressArray) {
        var this_lnglat = array[index];
        var geocoder = map.geocoder({
            radius: 1000,
            extensions: "all"
        });
        geocoder.getAddress(this_lnglat);
        geocoder.on('complete', function (GeocoderResult) {
            if (GeocoderResult.type === 'complete') {
                var this_address_value = GeocoderResult.regeocode.addressComponent.township;
                var this_address = GeocoderResult.regeocode.formattedAddress;
                if (!this_address_value) return;
                addressArray.push([this_address, this_address_value]);
                index++;
                if (index === array.length) {
                    // return addressArray;
                    var html = '';
                    for (var i = 1, len = addressArray.length - 1; i < len; i++) {
                        html += '<div class="form-group">'
                            + '<div class="col-md-10">'
                            + '<input type="text" id="wayPoint' + i + '" placeholder="请输入途经点(或右键地图)" class="form-control wayPoint" name="wayPoint" />'
                            + '</div>'
                            + '<button type="button" class="btn btn-danger padBottom deleteWayPoint"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button>'
                            + '</div>';
                    }
                    $('#wayPointArea').html(html);
                    $('#startPoint').val(addressArray[0][0]).attr('data-address', addressArray[0][0]);
                    $('#endPoint').val(addressArray[addressArray.length - 1][0]).attr('data-address', addressArray[addressArray.length - 1][0]);
                    for (var j = 1, len = addressArray.length - 1; j < len; j++) {
                        var id = 'wayPoint' + j;
                        $('#' + id).val(addressArray[j][0]).attr('data-address', addressArray[j][0]).attr('data-lnglat', array[j][0] + ';' + array[j][1]);
                        var wayPoint = map.autoComplete({
                            input: id
                        });
                        wayPoint.on('select', fenceOperation.dragRoute);
                        $('#' + id).inputClear().on('onClearEvent', fenceOperation.wayPointInputClear);
                    }
                    $('.deleteWayPoint').off('click').on('click', fenceOperation.deleteWayPoint);
                } else {
                    fenceOperation.getAddressValue(array, index, addressArray);
                }
            }
        });
    },
    //路径规划完成回调函数
    dragRouteComplete: function (data) {
        isDragRouteFlag = true;
        fenceOperation.clearPointMarker();
        var dragRouteArray = [];
        var start_lnglat = [data.data.start.location.lng, data.data.start.location.lat];
        var wayPointValue = data.data.waypoints;
        var end_lnglat = [data.data.end.location.lng, data.data.end.location.lat];
        dragRouteArray.push(start_lnglat);
        for (var j = 0, len = wayPointValue.length; j < len; j++) {
            dragRouteArray.push([wayPointValue[j].location.lng, wayPointValue[j].location.lat]);
        }
        dragRouteArray.push(end_lnglat);
        fenceOperation.getAddressValue(dragRouteArray, 0, []);
        var startToEndLngString = '', startToEndLatString = '', wayPointLngString = '', wayPointLatString = '';
        for (var i = 0, len = dragRouteArray.length; i < len; i++) {
            if (i === 0 || i === len - 1) {
                startToEndLngString += dragRouteArray[i][0] + ';';
                startToEndLatString += dragRouteArray[i][1] + ';';
            } else {
                wayPointLngString += dragRouteArray[i][0] + ';';
                wayPointLatString += dragRouteArray[i][1] + ';';
            }
        }
        $('#startToEndLng').val(startToEndLngString);
        $('#startToEndLat').val(startToEndLatString);
        $('#wayPointLng').val(wayPointLngString);
        $('#wayPointLat').val(wayPointLatString);
        //所有点
        var allPointLngLat = lineRoute.getRoute();
        var lngString = '';
        var latString = '';
        for (var i = 0, len = allPointLngLat.length; i < len; i++) {
            lngString += allPointLngLat[i].lng + ';';
            latString += allPointLngLat[i].lat + ';';
        }
        $('#allPointLng').val(lngString);
        $('#allPointLat').val(latString);
    },
    validate_dragRoute: function () {
        return $("#dragRouteLine").validate({
            rules: {
                name: {
                    required: true,
                    maxlength: 20
                },
                startPoint: {
                    required: true,
                },
                endPoint: {
                    required: true,
                },
                excursion: {
                    required: true,
                    maxlength: 10
                },
                description: {
                    maxlength: 100
                },
                groupName: {
                    required: true
                }
            },
            messages: {
                name: {
                    required: lineNameNull,
                    maxlength: publicSize20
                },
                startPoint: {
                    required: fenceOperationTravelLineStart
                },
                endPoint: {
                    required: fenceOperationTravelLineEnd
                },
                excursion: {
                    required: fenceOperationOffsetNull,
                    maxlength: publicSize10
                },
                description: {
                    maxlength: publicSize100
                },
                groupName: {
                    required: publicSelectGroupNull
                }
            }
        }).form();
    },
    //添加右键菜单
    addItem: function () {
        $('#addOrUpdateTravelFlag').val('0');
        isAddDragRoute = true;
        //创建右键菜单
        var this_point_lnglat;
        contextMenu = map.contextMenu();
        contextMenu.addItem("<i class='menu-icon menu-icon-from'></i>&ensp;&ensp;&ensp;<span>起点</span>", function (e) {
            fenceOperation.itemCallBack(this_point_lnglat, 0);
        }, 0);
        contextMenu.addItem("<i class='menu-icon menu-icon-via'></i>&ensp;&ensp;&ensp;<span>途经点</span>", function () {
            fenceOperation.itemCallBack(this_point_lnglat, 1);
        }, 1);
        contextMenu.addItem("<i class='menu-icon menu-icon-to'></i>&ensp;&ensp;&ensp;<span>终点</span>", function () {
            fenceOperation.itemCallBack(this_point_lnglat, 2);
        }, 2);
        contextMenu.addItem("<i class='icon-clearmap'></i>&ensp;&ensp;&ensp;<span>清除路线</span>", function () {
            fenceOperation.itemCallBack(this_point_lnglat, 3);
        }, 3);
        //地图绑定鼠标右击事件——弹出右键菜单
        map.on(map.currentMap === 'tMap' ? 'contextmenu' : "rightclick", function (e) {
            if (isAddDragRoute) {
                if (map.currentMap === 'baidu') {
                    e.lnglat = e.latlng;
                }
                this_point_lnglat = [e.lnglat.lng, e.lnglat.lat];
                contextMenu.open(map, e.lnglat);
                contextMenuPositon = e.lnglat;
            }
        });
    },
    //右键菜单选择回调函数
    itemCallBack: function (lnglat, type) {
        if (type !== 3) {
            var iconType;
            if (type === 0) { // 起点
                iconType = '../../resources/img/start_point.png';
            } else if (type === 1) {// 途经
                iconType = '../../resources/img/mid_point.png';
            } else if (type === 2) {// 终点
                iconType = '../../resources/img/end_point.png';
            }
            var dragRouteMarker = map.marker({
                map: map,
                position: map.currentMap === 'baidu' ? {lng: lnglat[0], lat: lnglat[1]} : lnglat,
                icon: map.icon({
                    size: map.size(40, 40), //图标大小
                    image: iconType
                }),
                size: {w: 32, h: 32}
            });
            if (type === 0) {
                if (dragPointMarkerMap.containsKey(type)) {
                    var this_marker = dragPointMarkerMap.get(type);
                    map.remove(this_marker);
                    dragPointMarkerMap.remove(type);
                }
                dragPointMarkerMap.put(type, dragRouteMarker);
            } else if (type === 2) {
                if (dragPointMarkerMap.containsKey(type)) {
                    var this_marker = dragPointMarkerMap.get(type);
                    map.remove(this_marker);
                    dragPointMarkerMap.remove(type);
                }
                dragPointMarkerMap.put(type, dragRouteMarker);
            } else if (type === 1) {
                var this_marker_array = [];
                if (dragPointMarkerMap.containsKey(type)) {
                    this_marker_array = dragPointMarkerMap.get(type);
                    dragPointMarkerMap.remove(type);
                }
                this_marker_array.push(dragRouteMarker);
                dragPointMarkerMap.put(type, this_marker_array);
            }
            fenceOperation.getAddressOneInfo(lnglat, type);
        } else {
            isDragRouteFlag = false;
            fenceOperation.clearLineDragRoute();
            fenceOperation.addItem();
            if (lineRoute !== undefined) {
                lineRoute.destroy();
            }
            $('#drivenRoute').show();
        }
    },
    //单独一条信息逆地理编码
    getAddressOneInfo: function (array, type) {
        var arrayString = array[0] + ';' + array[1];
        var geocoder = map.geocoder({
            city: "全国", //城市，默认：“全国”
            radius: 500 //范围，默认：500
        });
        geocoder.getAddress(array);
        geocoder.on('complete', function (GeocoderResult) {
            if (GeocoderResult.type === 'complete') {
                var this_address_value = GeocoderResult.regeocode.addressComponent.township;
                var this_address = GeocoderResult.regeocode.formattedAddress;
                if (!this_address_value) return;
                if (type === 0) {
                    $('#startPoint').val(this_address).attr('data-address', this_address).attr('data-lnglat', arrayString);
                }
                if (type === 2) {
                    $('#endPoint').val(this_address).attr('data-address', this_address).attr('data-lnglat', arrayString);
                }
                if (type === 1) {
                    fenceOperation.addWayToPoint([this_address, this_address_value, arrayString]);
                }
                fenceOperation.dragRoute('drag');
            }
        });
    },
    //清空右键规划的marker
    clearPointMarker: function () {
        if (dragPointMarkerMap !== undefined) {
            if (dragPointMarkerMap.containsKey('0')) {
                var this_marker = dragPointMarkerMap.get('0');
                map.remove([this_marker]);
            }
            if (dragPointMarkerMap.containsKey('2')) {
                var this_marker = dragPointMarkerMap.get('2');
                map.remove([this_marker]);
            }
            if (dragPointMarkerMap.containsKey('1')) {
                var this_marker_array = dragPointMarkerMap.get('1');
                map.remove(this_marker_array);
            }
            dragPointMarkerMap.clear();
        }
    },
    //关闭路径规划
    closeDragRoute: function () {
        isAddDragRoute = false;
        if (contextMenu !== undefined) {
            contextMenu.close();
        }
        $('#drivenRoute').hide();
    },
    //判断是否还有中文
    isChineseChar: function (str) {
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    },
    //途经点文本框清除事件
    wayPointInputClear: function (e, data) {
        var id = data.id;
        $('#' + id).attr('data-address', '').removeAttr('data-lnglat');
    },
    //关闭围栏修改功能
    closeFenceEdit: function () {
        fenceOperation.lineDragRouteClose();
        if (polyFence !== undefined && polyFence.CLASS_NAME === 'AMap.Marker') {
            polyFence.setDraggable(false);
            polyFence.off("mouseup", fenceOperation.moveMarker);
            polyFence = undefined;
        }
        mouseToolEdit.close(true);
        var polyEditorArray = PolyEditorMap.values();
        if (Array.isArray(polyEditorArray)) {
            for (var i = 0, len = polyEditorArray.length; i < len; i++) {
                if (Array.isArray(polyEditorArray[i])) {
                    for (var j = 0, fenceLength = polyEditorArray[i].length; j < fenceLength; j++) {
                        polyEditorArray[i][j].close();
                    }
                } else {
                    polyEditorArray[i].close();
                }
            }
        } else {
            polyEditorArray.close();
        }
        PolyEditorMap.clear()
    },
    // //围栏所属企业
    // fenceEnterprise: function(){
    //     var setting = {
    //         async : {
    //             url : "/clbs/m/basicinfo/enterprise/professionals/tree",
    //             tyoe : "post",
    //             enable : true,
    //             autoParam : [ "id" ],
    //             contentType : "application/json",
    //             dataType : "json",
    //         },
    //         view : {
    //             dblClickExpand : false
    //         },
    //         data : {
    //             simpleData : {
    //                 enable : true
    //             }
    //         },
    //         callback : {
    //             onClick : fenceOperation.enterpriseonClick
    //         },
    //     };
    //     $.fn.zTree.init($("#markerFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#lineFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#rectangleFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#circleFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#polygonFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#areaFenceEnterprise-tree"), setting, null);
    //     $.fn.zTree.init($("#dragRouteFenceEnterprise-tree"), setting, null);
    // },
    //属于企业选择
    enterpriseonClick: function (event, treeId, treeNode) {
        var this_tId = treeNode.tId;
        if (this_tId.indexOf('markerFenceEnterprise-tree') !== -1) {//标注
            $('#markerFenceEnterprise').val(treeNode.name);
            $('#markerGroupId').val(treeNode.id);
            $('#markerFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('lineFenceEnterprise-tree') !== -1) {//线
            $('#lineFenceEnterprise').val(treeNode.name);
            $('#lineGroupId').val(treeNode.id);
            $('#lineFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('rectangleFenceEnterprise-tree') !== -1) {//矩形
            $('#rectangleFenceEnterprise').val(treeNode.name);
            $('#rectangleGroupId').val(treeNode.id);
            $('#rectangleFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('circleFenceEnterprise-tree') !== -1) {//圆形
            $('#circleFenceEnterprise').val(treeNode.name);
            $('#circleGroupId').val(treeNode.id);
            $('#circleFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('polygonFenceEnterprise-tree') !== -1) {//多边形
            $('#polygonFenceEnterprise').val(treeNode.name);
            $('#polygonGroupId').val(treeNode.id);
            $('#polygonFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('areaFenceEnterprise-tree') !== -1) {//行政区划
            $('#areaFenceEnterprise').val(treeNode.name);
            $('#areaGroupId').val(treeNode.id);
            $('#areaFenceEnterprise-content').hide();
        } else if (this_tId.indexOf('dragRouteFenceEnterprise-tree') !== -1) {//行驶路线
            $('#dragRouteFenceEnterprise').val(treeNode.name);
            $('#dragRouteGroupId').val(treeNode.id);
            $('#dragRouteFenceEnterprise-content').hide();
        }
    },
    //电子围栏预处理的函数
    FenceAjaxDataFilter: function (treeId, parentNode, responseData) {
        if (responseData) {
            for (var i = 0; i < responseData.length; i++) {
                var data = responseData[i];
                data.open = false;
                data.name = html2Escape(data.name);
                if (data.markIcon === 1) {
                    data.iconSkin = 'zw_m_marker_circle_skin';
                }
            }
            // 图层
            var fenceLayer = {name: "图层", pId: "0", id: "zw_m_travel_layer", type: "fenceParent", open: false};
            // 图层1
            var firstFence = {
                // fenceInfoId: "7537030d-7a0e-4853-9102-8efb480305c0",
                iconSkin: "zw_m_rectangle_skin",
                name: "图层1",
                pId: "zw_m_travel_layer",
                // id: "25a69a35-f7f5-4609-8c31-f215cba7c78e",
                type: "fence",
                open: false,
            };
            var secondFence = {
                // fenceInfoId: "7537030d-7a0e-4853-9102-8efb480305c0",
                iconSkin: "zw_m_rectangle_skin",
                name: "图层2",
                pId: "zw_m_travel_layer",
                // id: "25a69a35-f7f5-4609-8c31-f215cba7c78e",
                type: "fence",
                open: false,
            };
            responseData.push(fenceLayer);
            responseData.push(firstFence);
            responseData.push(secondFence);
        }
        return responseData;
    },
    searchBindFenceTree: function (param) {
        var setQueryChar = {
            async: {
                url: "/clbs/a/search/monitorTreeFuzzy",
                type: "post",
                enable: true,
                autoParam: ["id"],
                dataType: "json",
                sync: false,
                otherParam: {"type": "multiple", "queryParam": param, "webType": "1"},
                dataFilter: fenceOperation.ajaxQueryDataFilter
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
                dblClickExpand: false,
                nameIsHTML: true,
                fontCss: setFontCss_ztree,
                countClass: "group-number-statistics"
            },
            data: {
                simpleData: {
                    enable: true
                }
            },
            callback: {
                beforeClick: fenceOperation.beforeClickFenceVehicle,
                onAsyncSuccess: fenceOperation.fuzzyZTreeOnAsyncSuccess,
                //beforeCheck: fenceOperation.fuzzyZTreeBeforeCheck,
                onCheck: fenceOperation.fuzzyOnCheckVehicle,
                //onExpand: fenceOperation.zTreeOnExpand,
                //onNodeCreated: fenceOperation.zTreeOnNodeCreated,
            }
        };
        $.fn.zTree.init($("#treeDemoFence"), setQueryChar, null);
    },
    fuzzyZTreeOnAsyncSuccess: function (event, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        zTree.expandAll(true);
        // var treeNodes = getAllChildNodes(zTree);
        // if (treeNodes) {
        //     for (var i = 0, l = treeNodes.length; i < l; i++) {
        //         zTree.checkNode(treeNodes[i], false, true);
        //         if ($.inArray(treeNodes[i].id, oldFencevehicleIds) != -1) {
        //             zTree.checkNode(treeNodes[i], true, true);
        //         }
        //     }
        // }
    },
    fuzzyOnCheckVehicle: function (e, treeId, treeNode) {
        //获取树结构
        var zTree = $.fn.zTree.getZTreeObj("treeDemoFence");
        //获取勾选状态改变的节点
        var changeNodes = zTree.getChangeCheckedNodes();
        if (treeNode.checked) { //若是取消勾选事件则不触发5000判断
            var checkedNodes = zTree.getCheckedNodes(true);
            var nodesLength = 0;
            for (var i = 0; i < checkedNodes.length; i++) {
                if (checkedNodes[i].type === "people" || checkedNodes[i].type === "vehicle" || checkedNodes[i].type === "thing") {
                    nodesLength += 1;
                }
            }

            if (nodesLength > 5000) {
                //zTree.checkNode(treeNode,false,true);
                layer.msg(treeMaxLength5000);
                for (var i = 0; i < changeNodes.length; i++) {
                    changeNodes[i].checked = false;
                    zTree.updateNode(changeNodes[i]);
                }
            }
        }
        //获取勾选状态被改变的节点并改变其原来勾选状态（用于5000准确校验）
        for (var i = 0; i < changeNodes.length; i++) {
            changeNodes[i].checkedOld = changeNodes[i].checked;
        }
        // 记录勾选的节点
        var v = "", nodes = zTree.getCheckedNodes(true);
        for (var i = 0, l = nodes.length; i < l; i++) {
            if (nodes[i].type === "vehicle" || nodes[i].type === "people" || nodes[i].type === "thing") {
                v += nodes[i].id + ",";
            }
        }
        vehicleFenceList = v;
    },
    selectDate: function (node) {
        var id = $(node).attr('id');
        laydate.render({elem: '#' + id, theme: '#6dcff6', show: true});
    },
    selectTime: function (node) {
        var id = $(node).attr('id');
        laydate.render({elem: '#' + id, theme: '#6dcff6', type: 'time', show: true});
    }
};

$('#refreshTable').on('click', function () {
    $('#simpleQueryParam').val('')
    myTable.init()
})

function getAllChildNodes(treeObj) {
    var nodes = new Array();
    var treeNode = treeObj.getNodes();
    getAllChildNodesFn(treeNode, nodes);
    return nodes;
}

function getAllChildNodesFn(treeNode, nodes) {
    $(treeNode).each(function (e, q) {
        if (q.children) {
            getAllChildNodesFn(q.children, nodes);
        } else {
            nodes.push(q);
        }
    })
}
