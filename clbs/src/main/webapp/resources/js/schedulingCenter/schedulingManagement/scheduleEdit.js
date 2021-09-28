//@ sourceURL=scheduleEdit.js
(function(window,$){
    //排班项
    var scheduleIndex = $('#schedule-list .item').length,// 排班项个数
        scheduleMax = 5;//最多添加排班个数
    var addIndex = scheduleIndex-1;//排班项索引

    //日期
    var dateRange = 10//最多选择日期范围为10年222;

    //时间
    var startTimeInstance=[],//开始时间实例集合
        endTimeInstance=[];//结束时间实例集合

    // 组织树
    var checkMaxSize = TREE_MAX_CHILDREN_LENGTH;//支持最多选择5000个监控对象
    var checkFlag = false;
    var inputChange;
    var existPeopleIds = $('#treeTypeDemo').data('value');// 已经存在的用户id集合
    var peopleNum = existPeopleIds.length;
    var monitorIds=  existPeopleIds || [];
    $('#selectNum').text(monitorIds.length);
    $('#monitorIds').val(monitorIds.length);
    // 组织树模糊搜索
    var isSearch = false;
    var isGetCount = false;
    var nodeCount = 0;
    var scheduleEdit = {
        init: function(){
            scheduleEdit.readOnly();
            //人员组织树
            scheduleEdit.monitorTreeInit();

            //判断排班项个数
            if(scheduleIndex==1){
                $('#schedule-list').addClass('del-hide');
            }

            //循环遍历初始化排班项列表
            for(var i=0;i<scheduleIndex;i++){
                //围栏组织树
                scheduleEdit.fenceTreeInit('#treeTypeDemo'+i);
            }
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
        //执行中只能修改新增人员
        readOnly:function(){
            if(editStatus == 3){
                $('.readonly').attr('disabled', 'disabled');
                $('#schedule-list').addClass('add-hide del-hide');
            }else{
                //日期初始化
                scheduleEdit.renderDate('startDate',scheduleEdit.changeDate);
                scheduleEdit.renderDate('endDate',scheduleEdit.changeDate);
                //循环遍历初始化排班项列表
                for(var i=0;i<scheduleIndex;i++){
                    //时间初始化
                    startTimeInstance[i] = scheduleEdit.renderTime('startTime'+i, scheduleEdit.startTimeCB);
                    endTimeInstance[i] = scheduleEdit.renderTime('endTime'+i, scheduleEdit.endTimeCB);
                }
                $('.fenceGroupSelect').on('click',showMenuContent);
                scheduleEdit.setDisabled();
            }
        },
        //日期
        renderDate:function(id,doneCB){
            var date = new Date();
            var today = date.getFullYear()+'-'+ (date.getMonth()+1) +'-'+(date.getDate()+1);
            var dateInstance = laydate.render({
                elem: '#'+id
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
        //时间
        renderTime: function (id, doneCB) {
            var timeInstance = laydate.render({
                elem: '#'+id
                ,type: 'time'
                ,theme: '#6dcff6'
                ,done: doneCB
                ,format: 'HH:mm'
                ,btns: ['clear', 'confirm']
            });
            return timeInstance;
        },
        endTimeCB:function (value, date) {
            var elem = $(this)[0].elem.selector;
            var index = elem.substr(elem.length-1);

            //清空判断
            if(!value) {
                var max = scheduleEdit.clearLimit('max');
                startTimeInstance[index].config.max = max;
                return;
            }

            date.month = date.month - 1;
            startTimeInstance[index].config.max = date;
        },
        startTimeCB:function (value, date) {
            var elem = $(this)[0].elem.selector;
            var index = elem.substr(elem.length-1);

            //清空判断
            if(!value) {
                var min = scheduleEdit.clearLimit('min');
                endTimeInstance[index].config.min = min;
                return;
            }

            date.month = date.month - 1;
            endTimeInstance[index].config.min = date;
        },
        clearLimit: function(type){
            var date = new Date();
            var max = {
                date:date.getDate(),
                hours:type == 'max' ? 23 : 0,
                minutes:type == 'max' ? 59 : 0,
                month:date.getMonth(),
                seconds:type == 'max' ? 59 : 0,
                year:date.getFullYear(),
            }
            return max;
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
        //围栏下拉组织树
        fenceTreeInit:function(id){
            var fenceAll = {
                async: {
                    url: "/clbs/m/regionManagement/fenceManagement/getFenceTree",
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple"},
                    dataFilter: scheduleEdit.FenceAjaxDataFilter
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    radioType: "all",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    }
                },
                view: {
                    dblClickExpand: false
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onAsyncSuccess: scheduleEdit.onAsyncSuccessFence,
                    onClick: scheduleEdit.onClickFence,
                    onCheck: scheduleEdit.onCheckFence
                }
            };
            $.fn.zTree.init($(id), fenceAll, null);
        },
        FenceAjaxDataFilter: function (treeId, parentNode, responseData) {
            var ret = [],
                data = [];
            var id = $('#'+treeId).data('value');
            var inx = treeId.substr(treeId.length-1);

            if (responseData && responseData.msg){
                ret = JSON.parse(responseData.msg);
            }
            for (var i = 0; i < ret.length; i++) {
                var item = ret[i];
                if(item.fenceType != 'zw_m_marker'){
                    if (item.pId === ""){
                        item.pId = 'top';
                    }

                    if(item.type=='fenceParent'){
                        item.nocheck = true;
                    }

                    if(id == item.id){
                        item.checked = true;
                        $('#groupSelect'+inx).val(item.name);
                    }
                    data.push(item);
                }
            }

            return data;
        },
        onAsyncSuccessFence: function(event, treeId, treeNode){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.expandAll(true);
        },
        onClickFence: function(event, treeId, treeNode){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.checkNode(treeNode, true, true);
            scheduleEdit.onCheckFence(event, treeId, treeNode);
        },
        onCheckFence: function(event, treeId, treeNode){
            var inx = treeId.substr(treeId.length-1);
            var name = treeNode.name,
                id = treeNode.id;
            var groupSelect = $('#groupSelect'+inx);

            if(treeNode.type!='fenceParent' && name && id){
                groupSelect.val(name);
                groupSelect.attr('data-id', id);
            }
        },
        //人员下拉组织树
        monitorTreeInit: function(){
            var setting = {
                async: {
                    url: scheduleEdit.getTreeUrl,
                    type: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: {"type": "multiple", "icoType": "0"},
                    dataFilter: scheduleEdit.monitorAjaxDataFilter
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
                    onAsyncSuccess: scheduleEdit.onAsyncSuccessMonitor,
                    onCheck: scheduleEdit.onCheckMonitor,
                    beforeCheck: scheduleEdit.beforeCheckMonitor,
                    beforeClick: scheduleEdit.beforeClickMonitor,
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
                scheduleEdit.monitorTreeInit();
            } else {
                var querySetting = {
                    async: {
                        url: "/clbs/a/search/reportFuzzySearch",
                        type: "post",
                        enable: true,
                        autoParam: ["id"],
                        dataType: "json",
                        otherParam: {"type":"vehicle", "queryParam": param, "queryType":"multiple",},
                        dataFilter: scheduleEdit.ajaxQueryDataFilter
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
                        onAsyncSuccess: scheduleEdit.onAsyncSuccessMonitor,
                        onCheck: scheduleEdit.onCheckMonitor,
                        beforeCheck: scheduleEdit.beforeCheckMonitor,
                        beforeClick: scheduleEdit.beforeClickMonitor,
                    }
                };
                $.fn.zTree.init($("#treeTypeDemo"), querySetting, null);
            }
        },
        ajaxQueryDataFilter: function (treeId, parentNode, responseData) {
            isSearch = true;
            responseData = JSON.parse(ungzip(responseData));
            var nodesArr = filterQueryResult(responseData, monitorIds);
            for (var i = 0; i < nodesArr.length; i++) {
                var item = nodesArr[i];
                item.open = true;
                if(monitorIds.indexOf(item.id) != -1){
                    item.checked = true;
                }
                //执行中已存在的人员禁止选择
                if(editStatus==3 && existPeopleIds.indexOf(item.id)!=-1){
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

                //勾选已经存在的人员
                var data = [];
                if(monitorIds.length>0){
                    for(var i = 0;i<ret.length;i++){
                        var item = ret[i];

                        if(monitorIds.indexOf(item.id)!=-1){
                            item.checked=true;
                        }

                        //执行中已存在的人员禁止选择
                        if(editStatus==3 && existPeopleIds.indexOf(item.id)!=-1){
                            item.chkDisabled = true;
                        }
                        data.push(item);
                    }
                }else{
                    data = ret;
                }

                $('#selectNum').text(monitorIds.length);
                $('#monitorIds').val(monitorIds.length);
                return data;
            }
        },
        onAsyncSuccessMonitor: function(event, treeId, treeNode){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var childrenCheckArr = [];
            var childrenArr = treeNode !== undefined && treeNode !== null ? treeNode.children : [];
            var length = monitorIds.length;
            var isNotEmpty = childrenArr !== undefined && childrenArr !== null && childrenArr.length > 0;
            if (isNotEmpty) {
                for (var i = 0; i < childrenArr.length; i++) {
                    var children = childrenArr[i];
                    if (children.checked) {
                        var monitorId = children.id;
                        if (monitorIds.indexOf(monitorId) === -1 && childrenCheckArr.indexOf(monitorId) === -1) {
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
            monitorIds = monitorIds.concat(childrenCheckArr);
            if (monitorIds.length !== length) {
                $('#selectNum').text(monitorIds.length);
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
                            if ($.inArray(nodeId, ns) === -1 && monitorIds.indexOf(nodeId) === -1) {
                                ns.push(nodeId);
                            }
                        }
                    }
                    nodesLength = monitorIds.length + ns.length;
                } else if (treeNode.type == "people" || treeNode.type == "vehicle" || treeNode.type == "thing") { //若勾选的为监控对象
                    if (monitorIds.indexOf(treeNode.id) === -1) {
                        nodesLength = monitorIds.length + 1;
                    }
                }

                if (nodesLength > checkMaxSize) {
                    layer.msg('最多勾选'+checkMaxSize+'个监控对象');
                    flag = false;
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
                scheduleEdit.getCharSelect(zTree);
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
            if(!isSearch){// 非搜索
                var nodes = treeObj.getCheckedNodes(true);
                var vid=[];
                for(var i=0;i<nodes.length;i++){
                    if(nodes[i].type=="vehicle" || nodes[i].type=="people" || nodes[i].type=="thing"){
                        var item = nodes[i];
                        vid.push(item.id);
                    }
                }
                //去重
                vid = editStatus == 3 ? vid.concat(existPeopleIds) : vid;//当状态在执行中时,需要加上被禁用选择的人员(treeObj.getCheckedNodes(true)无法获取到禁用人员)
                monitorIds = scheduleEdit.unique(vid);
            }else{// 搜索
                scheduleEdit.getSearchChange(changes);
            }

            var total = monitorIds.length;
            $('#selectNum').text(total);
            $('#monitorIds').val(total);
        },
        getSearchChange: function(changes){
            for(var i=0; i<changes.length; i++){
                var item = changes[i];
                if (isSearch && (item.type == 'people' || item.type == 'vehicle' || item.type == 'thing')) {
                    var inx = monitorIds.indexOf(item.id);
                    if (inx === -1) {
                        if (item.checked) {
                            monitorIds.push(item.id)
                        }
                    } else {
                        if (!item.checked) {
                            monitorIds.splice(inx, 1)
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
        // 添加排班
        addSchedule:function(){
            if(scheduleIndex>=scheduleMax){
                tg_alertError('信息', '系统最多支持5个排班项');
                return;
            }

            var html = scheduleEdit.scheduleDom();
            $('.schedule-list').append(html);
            $("#groupSelectSpan" + addIndex).on("click",function (){
                if ($(this).next().is(":hidden")) {
                    $(this).siblings('input').trigger("focus");
                    $(this).siblings('input').trigger("click");
                }
            });
            $('#schedule-list').removeClass('del-hide');
            scheduleIndex+=1;

            //日期
            startTimeInstance[addIndex] = scheduleEdit.renderTime('startTime'+addIndex, scheduleEdit.startTimeCB);
            endTimeInstance[addIndex] = scheduleEdit.renderTime('endTime'+addIndex, scheduleEdit.endTimeCB);

            //围栏组织树
            scheduleEdit.fenceTreeInit("#treeTypeDemo"+addIndex);
            $('#groupSelect'+addIndex).on('click',showMenuContent);
        },
        scheduleDom:function(){
            addIndex += 1;
            var html = '';

            html += '<li class="item">'+
                '                        <div class="zw-title clearfix">'+
                '                            <label class="control-label pull-left"><label'+
                '                                    class="text-danger">*</label> 排班项 </label>'+
                '                            <button type="button"'+
                '                                    class="btn btn-primary pull-right add-btn">'+
                '                                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>'+
                '                            </button>'+
                '                            <button type="button" class="btn btn-danger pull-right del-btn">'+
                '                                <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>'+
                '                            </button>'+
                '                        </div>'+
                '                        <div class="form-group">'+
                '                            <label class="col-md-2 control-label"><label'+
                '                                    class="text-danger">*</label> 控制类型： </label>'+
                '                            <div class="col-md-10">'+
                '                                <label class="radio-inline"><input class="controlType" value="1" type="radio" name="controlType'+addIndex+'" checked>围栏</label>'+
                '                                <label class="radio-inline"><input class="controlType" value="2" type="radio" name="controlType'+addIndex+'" disabled>RFID</label>'+
                '                                <label class="radio-inline"><input class="controlType" value="3" type="radio" name="controlType'+addIndex+'" disabled>NFC</label>'+
                '                                <label class="radio-inline"><input class="controlType" value="4" type="radio" name="controlType'+addIndex+'" disabled>二维码</label>'+
                '                            </div>'+
                '                        </div>'+
                '                        <div class="form-group">'+
                '                            <label class="col-md-2 control-label"><label'+
                '                                    class="text-danger">*</label> 围栏： </label>'+
                '                            <div class="col-md-4 has-feedback">'+
                '                                <!--组织树复选框-->'+
                '                                <input style="cursor: pointer; background-color: #fafafa;"'+
                '                                       placeholder="请选择排班围栏" class="form-control"'+
                '                                       id="groupSelect'+addIndex+'" name="fenceInfoId" readonly/>'+
                '                                <span class="fa fa-chevron-down form-control-feedback"'+
                '                                      style="top: 0; right: 15px;cursor:pointer;" aria-hidden="true"'+
                '                                      id="groupSelectSpan'+addIndex+'"></span>'+
                '                                <div id="menuContent'+addIndex+'" class="menuContent">'+
                '                                    <ul id="treeTypeDemo'+addIndex+'" class="ztree"></ul>'+
                '                                </div>'+
                '                            </div>'+
                '                        </div>'+
                '                        <div class="form-group">'+
                '                            <label class="col-md-2 control-label"><label'+
                '                                    class="text-danger">*</label> 开始时间： </label>'+
                '                            <div class="col-md-4">'+
                '                                <input id="startTime'+addIndex+'" readonly="readonly" name="startDate" placeholder="请选择排班开始时间" type="text"'+
                '                                       class="form-control layer-date laydate-icon startTime" autocomplete="off" style="cursor: pointer; background-color: rgb(250, 250, 250);" />'+
                '                            </div>'+
                '                            <label class="col-md-2 control-label"><label'+
                '                                    class="text-danger">*</label> 结束时间： </label>'+
                '                            <div class="col-md-4">'+
                '                                <input id="endTime'+addIndex+'" readonly="readonly" name="endDate" placeholder="请选择排班结束时间" type="text"'+
                '                                       class="form-control layer-date laydate-icon endTime" autocomplete="off" style="cursor: pointer; background-color: rgb(250, 250, 250);" />'+
                '                            </div>'+
                '                        </div>'+
                '                        <div class="form-group">'+
                '                            <label class="col-md-2 control-label">关联报警： </label>'+
                '                            <div class="col-md-10">'+
                '                                <label class="checkbox-inline"><input class="relationAlarm" value="1" name="relationAlarm'+addIndex+'" type="checkbox" checked/> 上班未到岗</label>'+
                '                                <label class="checkbox-inline"><input class="relationAlarm" value="2" name="relationAlarm'+addIndex+'" type="checkbox" checked/> 上班离岗</label>'+
                '                                <label class="checkbox-inline">'+
                '                                    <input class="relationAlarm" value="3" name="relationAlarm'+addIndex+'" type="checkbox" /> 超时长停留'+
                '                                    <input class="form-control stayMinute" type="text" value="30">'+
                '                                    分钟'+
                '                                </label>'+
                '                                <input id="residenceTime' +addIndex+ '" class="form-control residenceTime" name="residenceTime" type="text" value="30">' +
                '                            </div>'+
                '                        </div>'+
                '                    </li>';

            return html;
        },
        //删除排班
        delSchedule:function(){
            var self =$(this);
            tg_confirmDialog(null, null,function(){
                self.parents('.item').remove();
                scheduleIndex-=1;
                if(scheduleIndex==1){
                    $('#schedule-list').addClass('del-hide');
                }
            });
        },
        //表单验证
        validates:function(){
            var longTime = $('#longTime').prop('checked');
            return $('#editForm').validate({
                rules:{
                    name: {
                        required: true,
                        isJobNameCanNull: true,
                        remote:{
                            url: '/clbs/m/schedulingCenter/schedulingManagement/judgeScheduledNameIsCanBeUsed',
                            type: "post",
                            dataType: "json",
                            async: false,
                            data:{
                                scheduledInfoId: function() {
                                    return $("#scheduleId").val();
                                },
                                scheduledName: function() {
                                    return $("#scheduledName").val();
                                }
                            }
                        }
                    },
                    monitorIds: {
                        min: 1
                    },
                    startDate:{
                        required: true,
                    },
                    endDateStr:{
                        required: true,
                        compareDate: "#startDate"
                    },
                    fenceInfoId: {
                        required: true,
                    },
                    residenceTime: {
                        required:  true,
                        range: [10, 60],
                        digits:true
                    },
                    startDate:{
                        required: true,
                        repeatEndCheck: true
                    },
                    endDate:{
                        required: true,
                        compareDates: ".startTime"
                    },
                },
                messages:{
                    name:{
                        required: nameNull,
                        isJobNameCanNull: nameError,
                        remote: nameExists,
                    },
                    monitorIds:{
                        min: peopleIdsNull
                    },
                    startDate:{
                        required: startDateStrNull
                    },
                    endDateStr:{
                        required: endDateStrNull,
                        compareDate: endDateError
                    },
                    fenceInfoId:{
                        required: fenceInfoIdNull
                    },
                    residenceTime:{
                        required: residenceTimeNull,
                        range: residenceTimeRangeError,
                        digits: residenceTimeDigitsError
                    },
                    startDate:{
                        required: startDateNull
                    },
                    endDate:{
                        required: endDateNull
                    },
                }
            }).form();
        },
        //表单提交
        onSubmit:function(){
            if(!scheduleEdit.limitYear()) return false;
            if(!scheduleEdit.validates()){return false;}

            //周期重复类型
            var repeatType = $('#repeatType').val();
            var dateDuplicateType = [8];
            var schedulingItemInfoList = [];

            if(repeatType == 1){
                dateDuplicateType=[]
                $.each($('input[name="week"]:checked'),function(){
                    dateDuplicateType.push($(this).val());
                })
            }
            //排班项
            for(var i=0;i<scheduleIndex;i++){
                var obj = {};
                var item = $('#schedule-list .item').eq(i);
                var relationAlarm = [];
                //关联报警
                $.each(item.find('.relationAlarm:checked'),function(){
                    relationAlarm.push($(this).val());
                })

                obj={
                    "id": item.find('.itemId').val(),
                    "controlType": item.find('.controlType:checked').val(),
                    "fenceInfoId": item.find('input[name="fenceInfoId"]').data('id'),
                    "startTime": item.find('.startTime').val(),
                    "endTime": item.find('.endTime').val(),
                    "relationAlarm": relationAlarm.join(','),
                    "residenceTime": item.find('.residenceTime').val(),
                }
                schedulingItemInfoList.push(obj);
            }

            //数据组装
            var paramer = {
                "scheduledName": $('#scheduledName').val(),
                "startDate": $('#startDate').val(),
                "endDate": $('#endDate').val(),
                "dateDuplicateType": dateDuplicateType.join(','),
                "monitorIds": monitorIds.join(','),
                "remark": $('#remarks').val(),
                "schedulingItemInfos": JSON.stringify(schedulingItemInfoList)
            }

            //排班冲突验证
            var checkParamer = $.extend({}, paramer, {
                "isUpdate":true,
                "id": $("#scheduleId").val()
            });
            var submitParamer = $.extend({}, paramer, {
                "groupId": $('#groupId').val(),
                "id": $("#scheduleId").val()
            })

            json_ajax("POST", '/clbs/m/schedulingCenter/schedulingManagement/checkSchedulingConflicts', "json", true, checkParamer, function(data){
                scheduleEdit.submitCallBack(data, submitParamer);
            });
        },
        submitCallBack:function(data, paramer){
            if(data.success){
                var dataList = data.obj;
                if(dataList &&　dataList.length>0){
                    var html='<table id="dataTable"\n' +
                        '      class="table table-striped table-bordered table-hover text-center btnTable"\n' +
                        '      cellspacing="0" width="100%">\n' +
                        '      <thead>\n' +
                        '             <tr>\n' +
                        '                 <td>监控对象</td>\n' +
                        '                 <td>冲突的排班名称</td>\n' +
                        '             </tr>\n' +
                        '       </thead>\n' +
                        '  <tbody id="conflictsList">\n';

                    for(var i=0;i<dataList.length;i++){
                        var item = dataList[i];
                        html += '<tr>' +
                            '  <td>'+item.monitorName+'</td>' +
                            '  <td>'+item.scheduledName+'</td>' +
                            '</tr>';
                    }
                    html+='</tbody></table>';

                    layer.alert(html, {
                        id: "promptMessage",
                        skin: 'zw-ct-dialog'
                    });
                }else{
                    json_ajax("POST", '/clbs/m/schedulingCenter/schedulingManagement/updateScheduling', "json", true, paramer, function(data){
                        if(data.success){
                            layer.msg("修改成功！", {
                                move : false
                            });
                            $('#commonLgWin').modal('hide');
                            myTable.requestData();
                        }else{
                            layer.msg(data.msg, {
                                move : false
                            });
                        }
                    });
                }
            }else{
                layer.msg(data.msg);
            }
        },
        //超时长验证信息控制
        stayMinute: function(){//输入框改变
            var value = $(this).val();
            var parent = $(this).parents('.item');

            var checked = parent.find('.relationAlarm').eq(2).prop('checked'),
                residenceTime = parent.find('.residenceTime');

            if(checked){
                residenceTime.val(value);
            }else{
                residenceTime.val(30);
            }
        },
        longTime: function(){//复选框改变
            var checked = $(this).prop('checked');
            var parent = $(this).parents('.item');

            var value = parent.find('.stayMinute').val(),
                residenceTime = parent.find('.residenceTime');

            if(checked){
                residenceTime.val(value);
            }else{
                residenceTime.val(30);
            }
        }
    }

    $(function(){
        scheduleEdit.init();
        $('#repeatType').on('change', scheduleEdit.repeatTypeSelect);
        $('#weeks .checkbox-inline').on('click', scheduleEdit.checkWeeks);
        $('#schedule-list').on('click','.add-btn', scheduleEdit.addSchedule);
        $('#schedule-list').on('click','.del-btn', scheduleEdit.delSchedule);
        $('#doSubmits').on('click', scheduleEdit.onSubmit);
        $('#groupSelect').on('click',showMenuContent);
        $('#schedule-list').on('blur', '.stayMinute', scheduleEdit.stayMinute);
        $('#schedule-list').on('change', '.relationAlarm', scheduleEdit.longTime);

        //人员选择模糊搜索
        $('input').not('.stayMinute').inputClear().on('onClearEvent', function (e, data) {
            var id = data.id;
            if (id == 'groupSelect') {
                var param = $("#groupSelect").val();
                scheduleEdit.searchVehicleTree(param);
            }
        });
        $("#groupSelect").on('input propertychange', function (value) {
            if (inputChange !== undefined) {
                clearTimeout(inputChange);
            }
            if (!(window.ActiveXObject || "ActiveXObject" in window)) {
                isSearch = true;
            }
            inputChange = setTimeout(function () {
                if (isSearch) {
                    var param = $("#groupSelect").val();
                    scheduleEdit.searchVehicleTree(param);
                }
                isSearch = true;
            }, 500);
        });
    })
})(window,$)