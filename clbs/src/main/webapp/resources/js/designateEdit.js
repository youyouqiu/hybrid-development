//@ sourceURL=designateEdit.js
(function(window,$){
    //任务列表
    var dataList = JSON.parse($('#taskTree').val());

    //日期
    var dateRange = 10//最多选择日期范围为10年;

    //组织树
    var checkMaxSize = 500;//支持最多选择5000个监控对象
    var checkFlag = false;
    var inputChange;
    var existPeopleIds = [];// 已经存在的用户id集合
    var peopleNum = 0;
    var peopleIds= [];
    var isSearch = false;
    var isGetCount = false;
    var nodeCount = 0;
    var designateEdit = {
        init:function(){
            designateEdit.readOnly();
            //获取peopleId
            designateEdit.getPeopleId();
            //任务列表
            designateEdit.getTaskList();
            designateEdit.setDisabled();
        },
        setDisabled: function(){
            var elem = $('#repeatType');
            var start = $('#startDate').val(),
                end = $('#endDate').val();

            var days = (new Date(end).getTime() - new Date(start).getTime()) / 1000 / 3600 / 24;
            if(days < 6){
                elem.attr('disabled','disabled');
            }
        },
        getPeopleId: function(){
            var peopleObj = $('#treeTypeDemo').data('value');
            for (var i = 0, len = peopleObj.length; i < len; i++) {
                var item = peopleObj[i];
                var peopleId = item.peopleId;
                if (peopleId === undefined || peopleId === null || peopleId === "") {
                    continue;
                }
                existPeopleIds.push(peopleId);
            }
            peopleIds = existPeopleIds;
            peopleNum = peopleIds.length;

            $('#selectNum').text(peopleIds.length);
            $('#peopleIds').val(peopleIds.length);

            designateEdit.monitorTreeInit();
        },
        //执行中只能修改新增人员
        readOnly:function(){
            if(editStatus == '执行中'){
                $('.readonly').attr('disabled', 'disabled');
            }else{
                //日期初始化
                designateEdit.renderDate('#startDate',designateEdit.changeDate);
                designateEdit.renderDate('#endDate',designateEdit.changeDate);
            }
        },
        //获取任务列表
        getTaskList: function(){
            var select = $('#taskList');
            var html = '';
            for(var i=0,len = dataList.length;i<len;i++){
                var item = dataList[i];
                html += '<option value="'+item.id+'">'+item.taskName+'</option>'
            }
            select.html(html);
            select.val(select.data('value'));
        },
        //任务列表改变
        taskListChange: function(){
            var detailDom = $('#schedule-list');
            if(!detailDom.is(':hidden')){
                detailDom.slideUp();
            }
        },
        //获取任务详情
        showTaskDetail: function(){
            var taskId = $('#taskList').val();

            var paramer = {
                "id": taskId,
            }
            json_ajax("POST", '/clbs/a/taskManagement/getTaskDetail', "json", true, paramer,designateEdit.taskDetailCB);
        },
        taskDetailCB: function(data){
            var detailDom = $('#schedule-list');
            if(data.success){
                var taskItems = data.obj.taskItems;
                designateEdit.taskDetailDom(taskItems);

                if(detailDom.is(':hidden')){
                    detailDom.slideDown();
                }else{
                    detailDom.slideUp();
                }
            }else{
                layer.msg(data.msg)
            }
        },
        taskDetailDom:function(data){
            var html='';
            for(var i=0,len=data.length;i<len;i++){
                var item = data[i];
                html += '<li class="item">\n' +
                    '                        <div class="zw-title clearfix">\n' +
                    '                            <label class="control-label pull-left"><label\n' +
                    '                                    class="text-danger">*</label> 任务项 </label>\n' +
                    '                        </div>\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 围栏： </label>\n' +
                    '                            <div class="col-md-10 has-feedback">\n' +
                    '                                <input value="'+item.fenceName+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 开始时间： </label>\n' +
                    '                            <div class="col-md-4">\n' +
                    '                                <input value="'+item.startTime+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                            <label class="col-md-2 control-label"><label\n' +
                    '                                    class="text-danger">*</label> 结束时间： </label>\n' +
                    '                            <div class="col-md-4">\n' +
                    '                                <input value="'+item.endTime+'" readonly="readonly" type="text" class="form-control inputStyleHide" />\n' +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '                        <div class="form-group">\n' +
                    '                            <label class="col-md-2 control-label"> 关联报警： </label>\n' +
                    '                            <div class="col-md-10">\n' +
                    '                                <label class="checkbox-inline">'+(item.relationAlarm.indexOf('1') != -1 ? '<input checked class="relationAlarm" value="1" type="checkbox" disabled/> 任务未到岗</label>' : '<input class="relationAlarm" value="1" type="checkbox" disabled/> 任务未到岗</label>') +
                    '                                <label class="checkbox-inline">'+(item.relationAlarm.indexOf('2') != -1 ? '<input checked class="relationAlarm" value="2" type="checkbox" disabled/> 任务离岗</label>' : '<input class="relationAlarm" value="1" type="checkbox" disabled/> 任务离岗</label>') +
                    '                            </div>\n' +
                    '                        </div>\n' +
                    '                    </li>';

                $('#schedule-list').html(html);
            }

        },
        //日期
        renderDate:function(id,doneCB){
            var date = new Date();
            var today = date.getFullYear()+'-'+ (date.getMonth()+1) +'-'+(date.getDate()+1);
            var dateInstance = laydate.render({
                elem: id
                ,theme: '#6dcff6'
                ,min: today
                ,done: doneCB
                ,btns: ['clear', 'confirm']
            });
        },
        changeDate: function(value){
            var elem = $(this)[0].elem.selector;
            var compareDate = elem == "#startDate" ? $('#endDate').val() : $('#startDate').val();

            if(!compareDate) return;

            //改变日期重复类型
            var repeatType = $('#repeatType');
            var day = Math.abs(new Date(value).getTime() - new Date(compareDate).getTime()) / 1000 / 3600 / 24;
            if(day >= 0 && day < 6){
                repeatType.val(0).attr('disabled','disabled');
                $('#weeks').slideUp();
            }else{
                repeatType.removeAttr('disabled');
            }
        },
        limitYear: function(){
            var start = $('#startDate').val(),
                end = $('#endDate').val();

            //日期范围判断
            var startYear = new Date(start).getFullYear(),
                endYear = new Date(end).getFullYear();
            var maxDate =  new Date(start).setFullYear(startYear + dateRange),
                minDate =  new Date(end).setFullYear(endYear - dateRange);
            if(new Date(end).getTime() > maxDate || new Date(start).getTime() < minDate){
                layer.msg('选择范围最多支持开始日期后'+dateRange+'年的日期范围');
                return false;
            }
            return true;
        },
        //人员下拉组织树
        monitorTreeInit: function(){
            var setting = {
                async: {
                    url: designateEdit.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: designateEdit.monitorAjaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "checkbox",
                    radioType: "all",
                    chkboxType: {
                        "Y": "ps",
                        "N": "ps"
                    }
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
                    onAsyncSuccess: designateEdit.onAsyncSuccessMonitor,
                    onCheck: designateEdit.onCheckMonitor,
                    beforeCheck: designateEdit.beforeCheckMonitor,
                    beforeClick: designateEdit.beforeClickMonitor,
                }
            };
            if (!isGetCount) {
                $.ajax({
                    async: false,
                    type: "post",
                    url:  "/clbs/m/basicinfo/enterprise/assignment/vehiclePer.gsp/count?aid=",
                    success: function (msg) {
                        isGetCount = true;
                        nodeCount = parseInt(msg);
                    }
                });
            }
            $.fn.zTree.init($("#treeTypeDemo"), setting, null);
        },
        getTreeUrl : function (treeId, treeNode){
            if (treeNode == null) {
                return "/clbs/m/functionconfig/fence/bindfence/alarmSearchTree";
            }else if(treeNode.type == "assignment") {
                return "/clbs/m/functionconfig/fence/bindfence/putMonitorByAssign?assignmentId="+treeNode.id+"&isChecked="+treeNode.checked+"&monitorType=monitor";
            }
        },
        searchVehicleTree: function (param) {
            if (param == null || param == undefined || param == '') {
                designateEdit.monitorTreeInit();
            } else {
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type":"vehicle", "queryParam": param, "queryType":"multiple",},
                        dataFilter: designateEdit.ajaxQueryDataFilter
                    },
                    check: {
                        enable: true,
                        chkStyle: "checkbox",
                        radioType: "all",
                        chkboxType: {
                            "Y": "s",
                            "N": "s"
                        }
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
                        onAsyncSuccess: designateEdit.onAsyncSuccessMonitor,
                        onCheck: designateEdit.onCheckMonitor,
                        beforeCheck: designateEdit.beforeCheckMonitor,
                        beforeClick: designateEdit.beforeClickMonitor,
                    }
                };
                $.fn.zTree.init($("#treeTypeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            isSearch = true;
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr = filterQueryResult(responseData, peopleIds);
            for (var i = 0; i < nodesArr.length; i++) {
                var item = nodesArr[i];
                item.open = true;
                if(peopleIds.indexOf(item.id) != -1){
                    item.checked = true;
                }
                //执行中已存在的人员禁止选择
                if(editStatus=='执行中' && existPeopleIds.indexOf(item.id)!=-1){
                    item.chkDisabled = true;
                }
            }
            return nodesArr;
        },
        monitorAjaxDataFilter: function(treeId, parentNode, responseData){
            isSearch = false;
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            if (responseData.msg) {
                var obj = JSON.parse(ungzip(responseData.msg));
                var ret;
                if (obj.tree != null && obj.tree != undefined) {
                    ret = obj.tree;
                    size = obj.size;
                }else{
                    ret = obj
                }

                //搜索结果保持勾选状态
                var data = [];
                if(peopleIds.length>0){
                    for(var i = 0;i<ret.length;i++){
                        var item = ret[i];
                        if(peopleIds.indexOf(item.id) != -1){
                            item.checked=true;
                        }
                        //执行中已存在的人员禁止选择
                        if(editStatus=='执行中' && existPeopleIds.indexOf(item.id)!=-1){
                            item.chkDisabled = true;
                        }
                        data.push(item);
                    }
                }else{
                    data = ret;
                }

                return data;
            }
        },
        onAsyncSuccessMonitor: function(event, treeId, treeNode){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var childrenCheckArr = [];
            var childrenArr = treeNode !== undefined && treeNode !== null ? treeNode.children : [];
            var length = peopleIds.length;
            var isNotEmpty = childrenArr !== undefined && childrenArr !== null && childrenArr.length > 0;
            if (isNotEmpty) {
                for (var i = 0; i < childrenArr.length; i++) {
                    var children = childrenArr[i];
                    if (children.checked) {
                        var monitorId = children.id;
                        if (peopleIds.indexOf(monitorId) === -1 && childrenCheckArr.indexOf(monitorId) === -1) {
                            childrenCheckArr.push(monitorId);
                        }
                    }
                }
            }
            if (length + childrenCheckArr.length > checkMaxSize) {
                layer.msg('最多勾选' + checkMaxSize + '个监控对象');
                if (isNotEmpty) {
                    for (var i = 0; i < childrenArr.length; i++) {
                        var children = childrenArr[i];
                        children.checked = false
                        treeObj.updateNode(children, true);
                    }
                }
                return;
            }
            peopleIds = peopleIds.concat(childrenCheckArr);
            if (peopleIds.length !== length) {
                $('#selectNum').text(peopleIds.length);
            }
        },
        beforeClickMonitor: function (treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            zTree.checkNode(treeNode, !treeNode.checked, true, true);
            return false;
        },
        beforeCheckMonitor: function (treeId, treeNode) {
                var flag = true;
                if (!treeNode.checked) {
                    var nodesLength = 0;
                    if (treeNode.type == "group" || treeNode.type == "assignment") { //若勾选的为组织或分组
                        var zTree = $.fn.zTree.getZTreeObj(treeId);
                        if (treeNode.type === "group") {
                            if (nodeCount > 5000) {
                                layer.msg("请单独勾选对应分组", {move: false});
                                return false;
                            }
                        }
                        //获取选中分组下未勾选的监控对象
                        var nodes = zTree.getNodesByFilter(function (item) {
                            return (item.type=='people' || item.type=='vehicle' || item.type=='thing') && !item.checked;
                        }, false, treeNode);

                        //去重
                        var ns = [];
                        for (var i = 0; i < nodes.length; i++) {
                            var nodeId = nodes[i].id;
                            if (nodes[i].type == "people" || nodes[i].type == "vehicle" || nodes[i].type == "thing") {
                                if ($.inArray(nodeId, ns) === -1 && peopleIds.indexOf(nodeId) === -1) {
                                    ns.push(nodeId);
                                }
                            }
                        }
                        nodesLength = peopleIds.length + ns.length;
                    } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
                        if (peopleIds.indexOf(treeNode.id) === -1) {
                            nodesLength = peopleIds.length + 1;
                        }
                    }

                    if (nodesLength > checkMaxSize) {
                        layer.msg('最多勾选'+checkMaxSize+'个监控对象');
                        flag = false;
                    }
                }
                if (flag) {
                    //若组织节点已被勾选，则是勾选操作，改变勾选操作标识
                    if (treeNode.type == "group" && !treeNode.checked) {
                        checkFlag = true;
                    }
                }
                return flag;
        },
        onCheckMonitor: function(event, treeId, treeNode){
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            //若为取消勾选则不展开节点
            if (treeNode.checked) {
                zTree.expandNode(treeNode, true, true, true, true); // 展开节点
            }

            if(treeNode.type != "assignment" || (treeNode.type == "assignment" && treeNode.children != undefined)){
                designateEdit.getCharSelect(zTree);
            }
        },
        getCharSelect: function (treeObj) {
            //不同分组下的相同监控对象联动
            var changes = treeObj.getChangeCheckedNodes();
            if(changes.length>0){
                for(var i=0;i<changes.length;i++){
                    var item = changes[i];
                    var filter = treeObj.getNodesByFilter(function(node){
                        return node.id == item.id;
                    });
                    for(var j=0;j<filter.length;j++){
                        treeObj.checkNode(filter[j], item.checked);
                        filter[j].checkedOld = item.checked;
                    }
                }
            }

            //获取勾选的监控对象
            if(!isSearch){
                var nodes = treeObj.getCheckedNodes(true);
                var vid=[];
                for(var i=0;i<nodes.length;i++){
                    if(nodes[i].type=="vehicle" || nodes[i].type=="people" || nodes[i].type=="thing"){
                        var item = nodes[i];
                        vid.push(item.id)
                    }
                }
                //去重
                vid = editStatus == '执行中' ? vid.concat(existPeopleIds) : vid;//当状态在执行中时,需要加上被禁用选择的人员(treeObj.getCheckedNodes(true)无法获取到禁用人员)
                //去重
                peopleIds = designateEdit.unique(vid);
            }else{// 搜索
                designateEdit.getSearchChange(changes);
            }

            var total = peopleIds.length;
            $('#selectNum').text(total);
            $('#peopleIds').val(total);
        },
        getSearchChange: function(changes){
            for(var i=0; i<changes.length; i++){
                var item = changes[i];
                if(isSearch && (item.type == 'people' || item.type == 'vehicle' ||item.type == 'thing')){
                    var inx = peopleIds.indexOf(item.id);
                    if (inx === -1) {
                        if (item.checked) {
                            peopleIds.push(item.id)
                        }
                    } else {
                        if (!item.checked) {
                            peopleIds.splice(inx, 1)
                        }
                    }
                }
            }
        },
        unique: function(arr){
            var result = [], hash = {};
            for (var i = 0, elem; (elem = arr[i]) != null; i++) {
                if (!hash[elem]) {
                    result.push(elem);
                    hash[elem] = true;
                }
            }
            return result;
        },
        // 日期重复类型
        repeatTypeSelect:function(){
            var value = $(this).val();
            var week = $('#weeks');
            if(value == 1){
                week.slideDown();
            }else{
                week.slideUp();
            }
        },
        //日期重复类型(星期选择)
        checkWeeks:function(){
            var checkbox = $('input[name="week"]:checked');
            var repeat = $('#repeatType');
            var len = checkbox.length;

            len==1 ? checkbox.attr('disabled','disabled') : checkbox.removeAttr('disabled');
            len==7 ? repeat.val(0) : repeat.val(1);
        },
        //表单验证
        validates:function(){
            return $('#editForm').validate({
                rules:{
                    name: {
                        required: true,
                        isJobNameCanNull: true,
                        remote:{
                            url: '/clbs/a/taskManagement/checkDesignateName',
                            type: "post",
                            dataType: "json",
                            async: false,
                            data:{
                                id: function() {
                                    return $("#id").val();
                                }
                            }
                        }
                    },
                    peopleIds: {
                      min: 1
                    },
                    startDateStr:{
                        required: true,
                    },
                    endDateStr:{
                        required: true,
                        compareDate: "#startDate"
                    },
                },
                messages:{
                    name:{
                        required: nameNull,
                        isJobNameCanNull: nameError,
                        remote: nameExists,
                    },
                    peopleIds:{
                        min: peopleIdsNull
                    },
                    startDateStr:{
                        required: startDateStrNull
                    },
                    endDateStr:{
                        required: endDateStrNull,
                        compareDate: endDateError
                    },
                }
            }).form();
        },
        //表单提交
        onSubmit:function(){
            if(!designateEdit.limitYear()) return false;
            if(!designateEdit.validates()){return;}

            //周期重复类型
            var repeatType = $('#repeatType').val();
            var dateDuplicateType = [8];

            if(repeatType == 1){
                dateDuplicateType=[];
                $.each($('input[name="week"]:checked'),function(){
                    dateDuplicateType.push($(this).val());
                })
            }

            var designatePeopleInfos = [];
            for(var i=0;i<peopleIds.length;i++){
                var item = peopleIds[i];

                var obj = {
                    designateInfoId: $('#id').val(),
                    peopleId:item
                }

                designatePeopleInfos.push(obj);
            }

            //数据组装
            var paramer = {
                "id": $('#id').val(),
                "designateName": $('#designateName').val(),
                "remark": $('#remarks').val(),
                "startDate": $('#startDate').val(),
                "endDate": $('#endDate').val(),
                "dateDuplicateType": dateDuplicateType.join(','),
                "taskId": $('#taskList').val(),
                "designatePeopleInfosStr": JSON.stringify(designatePeopleInfos)
            }

            json_ajax("POST", '/clbs/a/taskManagement/editDesignate', "json", false, paramer, designateEdit.submitCallBack);
        },
        submitCallBack:function(data){
            if(data.success){
                layer.msg("修改成功！", {
                    move : false
                });
                $('#commonLgWin').modal('hide');
                myTable.requestData();
                myTable2.requestData();
            }else{
                var confList = JSON.parse(data.msg);
                if(confList) {
                    var html = '<table id="dataTable"\n' +
                        '      class="table table-striped table-bordered table-hover text-center btnTable"\n' +
                        '      cellspacing="0" width="100%">\n' +
                        '      <thead>\n' +
                        '             <tr>\n' +
                        '                 <td>监控对象</td>\n' +
                        '                 <td>冲突的指派名称</td>\n' +
                        '             </tr>\n' +
                        '       </thead>\n' +
                        '  <tbody id="conflictsList">\n';

                    for (var key in confList) {
                        var value = confList[key];
                        html += '<tr>' +
                            '  <td>' + key + '</td>' +
                            '  <td>' + value + '</td>' +
                            '</tr>'
                    }
                    html += '</tbody></table>';

                    layer.alert(html, {
                        id: "promptMessage",
                        skin: 'zw-ct-dialog'
                    });
                }
            }
        }
    }

    $(function(){
        designateEdit.init();
        //事件
        $('#repeatType').on('change', designateEdit.repeatTypeSelect);
        $('#weeks .checkbox-inline').on('click', designateEdit.checkWeeks);
        $('#doSubmits').on('click', designateEdit.onSubmit);
        $('#groupSelect').on('click', showMenuContent);
        $('#taskDetail').on('click', designateEdit.showTaskDetail);
        $('#taskList').on('change', designateEdit.taskListChange);

        //人员选择模糊搜索
        $('input').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                designateEdit.searchVehicleTree(param);
            }
        });
        $("#groupSelect").on('input propertychange', function () {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    designateEdit.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
    })
})(window,$)