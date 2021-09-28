// 公共方法

define(function() {
  var searchFlag = true;
  var checkedAssginment = [];//快速录入已被选中的分组
  var speedCheckedAssginment = [];//极速录入已被选中的分组
  var sweepCheckedAssginment = [];//扫码录入已被选中的分组
  var selectTreeId = '';
  var selectTreeType = '';

  var publicFun = {
    setSelectTreeId: function(id) { selectTreeId = id },
    getSelectTreeId: function() { return selectTreeId },
    setSelectTreeType: function(Type) { selectTreeType = Type },
    getSelectTreeType: function() { return selectTreeType },
    //左侧树
    leftTree: function () {
      var treeSetting = {
        async: {
          url: "/clbs/m/basicinfo/enterprise/assignment/assignmentTree",
          tyoe: "post",
          enable: true,
          autoParam: ["id"],
          dataType: "json",
          otherParam: {  // 是否可选  Organization
            "isOrg": "1"
          },
          dataFilter: function (treeId, parentNode, responseData) {
            // var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
            if (responseData) {
              for (var i = 0; i < responseData.length; i++) {
                responseData[i].open = true;
              }
            }
            return responseData;
          },
        },
        view: {
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
          onClick: publicFun.leftTreeOnClick
        }
      };
      $.fn.zTree.init($("#treeDemo"), treeSetting, null);
    },
    //左侧树-点击节点
    leftTreeOnClick: function (event, treeId, treeNode) {
      if (treeNode.type == "group") {
        selectTreepId = treeNode.id;
        publicFun.setSelectTreeId(treeNode.uuid);
      } else {
        selectTreepId = treeNode.pId;
        publicFun.setSelectTreeId(treeNode.id);
      }
      publicFun.setSelectTreeType(treeNode.type);
      myTable.requestData();
    },
    //分组树
    groupTreeInit: function (type) {
      var setting = {
        async: {
          url: "/clbs/m/infoconfig/infoinput/tree",
          type: "post",
          enable: true,
          autoParam: ["id"],
          contentType: "application/json",
          dataType: "json",
          dataFilter: publicFun.ajaxDataFilter
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
          dblClickExpand: false
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: publicFun.beforeClick,
          onClick: publicFun.onClick,
          onCheck: publicFun.onClick,
          onAsyncSuccess: publicFun.onAsyncSuccess
        }
      };
      if (type == 'quick') {
        $.fn.zTree.init($("#quickTreeDemo"), setting, null);
      } else if (type == 'fast') {
        $.fn.zTree.init($("#fastTreeDemo"), setting, null);
      } else {
        $.fn.zTree.init($("#sweepTreeDemo"), setting, null);
      }
    },
    onAsyncSuccess: function (event, treeId, treeNode, msg) {
      //初始化快速录入节点选中数组
      var nodes1 = $.fn.zTree.getZTreeObj(treeId).getCheckedNodes(true);
      for (var i = 0; i < nodes1.length; i++) {
        if (nodes1[i].type == "assignment") {
          if (treeId == 'quickTreeDemo') {
            checkedAssginment.push(nodes1[i]);
          } else if (treeId == 'fastTreeDemo') {
            speedCheckedAssginment.push(nodes1[i]);
          } else {
            sweepCheckedAssginment.push(nodes1[i]);
          }
        }
      }
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      var flag = true;
      if (responseData) {
        for (var i = 0; i < responseData.length; i++) {
          if (flag && responseData[i].type == 'assignment' && searchFlag) {
            if (responseData[i].canCheck < TREE_MAX_CHILDREN_LENGTH) {
              responseData[i].checked = true;
              if (treeId == 'quickTreeDemo') {
                $("#quickGroupId").val(responseData[i].name);
                $("#quickCitySelidVal").val(responseData[i].id);
              } else if (treeId == 'fastTreeDemo') {
                $("#speedGroupid").val(responseData[i].name);
                $("#speedCitySelidVal").val(responseData[i].id);
              } else {
                responseData[i].checked = true;
                $("#sweepCodeGroupid").val(responseData[i].name);
                $("#sweepCodeCitySelidVal").val(responseData[i].id);
              }
              flag = false;
            }
          }
          responseData[i].open = true;
        }
      }
      return responseData;
    },
    //树点击之前事件
    beforeClick: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      if (!treeNode.checked && !infoFastInput.checkGroupNum(treeNode.id) && treeNode.type == "assignment") {
        return false;
      }
      zTree.checkNode(treeNode, !treeNode.checked, treeNode, true);
      return false;
    },
    //树点击事件
    onClick: function (e, treeId, treeNode) {
      if (treeId != undefined) {//快速录入与极速录入
        var zTree = $.fn.zTree.getZTreeObj(treeId);
        var v = "";
        var t = "";
        if (treeNode.checked) { //勾选操作才进行验证，取消勾选不进行验证
          //获取分组和企业ID信息
          var caId;
          //定义分组和企业唯一标识
          var caIdentification;
          var nodes = [];
          if (treeNode.type == "assignment") { //为分组节点直接校验
            caIdentification = 1;
            caId = treeNode.id;
            nodes.push(treeNode);
          } else if (treeNode.type == "group") { //为企业节点获取勾选节点然后去除校验过的节点
            caIdentification = 2;
            caId = treeNode.id;
            nodes = zTree.getCheckedNodes(true);
            var curArr = [];
            if (treeId == 'quickTreeDemo') {
              curArr = checkedAssginment;
            } else if (treeId == 'fastTreeDemo') {
              curArr = speedCheckedAssginment;
            } else {
              curArr = sweepCheckedAssginment;
            }
            for (var i = 0; i < curArr.length; i++) {
              nodes.remove(curArr[i]);
            }
          }
          //获取还可录入的分组id
          publicFun.getAllAssignmentVehicleNumber(caId, caIdentification);
          nodes.sort(function compare(a, b) {
            return a.id - b.id;
          });
          var amtNames = ""; // 车辆数超过100的分组
          for (var i = 0, l = nodes.length; i < l; i++) {
            if (nodes[i].type == "assignment") { // 选择的是分组，才组装值
              if (!publicFun.checkMaxVehicleCountOfAssignment(nodes[i].id)) {
                nodes[i].checked = false;
                amtNames += nodes[i].name + ",";
              }
            }
          }
          // 判断系统是否出问题
          if (systemErrorFlag) {
            layer.msg(systemError, {
              time: 1500,
            });
            return;
          }
          if (amtNames.length > 0) {
            amtNames = amtNames.substr(0, amtNames.length - 1);
            layer.msg("【" + amtNames + "】" + assignmentMaxCarNum);
          } else {
            publicFun.clearErrorMsg();
          }
        }
        //组装校验通过的值，初始化节点选中数组
        if (treeId == 'quickTreeDemo') {
          checkedAssginment = [];
        } else if (treeId == 'fastTreeDemo') {
          speedCheckedAssginment = [];
        } else {
          sweepCheckedAssginment = [];
        }
        var checkedNodes = zTree.getCheckedNodes(true);
        for (var i = 0; i < checkedNodes.length; i++) {
          if (checkedNodes[i].type == "assignment") {
            t += checkedNodes[i].name + ",";
            v += checkedNodes[i].id + ";";
            if (treeId == 'quickTreeDemo') {
              checkedAssginment.push(checkedNodes[i]);
            } else if (treeId == 'fastTreeDemo') {
              speedCheckedAssginment.push(checkedNodes[i]);
            } else {
              sweepCheckedAssginment.push(checkedNodes[i]);
            }
          }
        }
        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (t.length > 0) t = t.substring(0, t.length - 1);
        if (treeId == 'quickTreeDemo') {
          var cityObj = $("#quickGroupId");
          cityObj.val(t);
          $("#quickCitySelidVal").val(v);
        } else if (treeId == 'fastTreeDemo') {
          var cityObj = $("#speedGroupid");
          cityObj.val(t);
          $("#speedCitySelidVal").val(v);
        } else if (treeId == 'sweepTreeDemo') {
          var cityObj = $("#sweepCodeGroupid");
          cityObj.val(t);
          $("#sweepCodeCitySelidVal").val(v);
        }
      }
    },
    //获取还可录入的分组id(校验分组车辆上限用)
    getAllAssignmentVehicleNumber: function (cdId, identifier) {
      if (cdId != "" && identifier != "") {
        $.ajax({
          type: 'POST',
          url: '/clbs/m/infoconfig/infoinput/getAssignmentCount',
          dataType: 'json',
          data: { "id": cdId, "type": identifier },
          async: false,
          success: function (data) {
            if (data.success) {
              ais = data.obj.ais;
            } else if (data.msg) {
              layer.msg(data.msg);
              systemErrorFlag = true;
            }
          },
          error: function () {
            layer.msg(systemError, {
              time: 1500,
            });
            systemErrorFlag = true;
          }
        });
      }
    },
    // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkSweepMaxVehicleCountOfAssignment: function (assignmentId, assignmentName) {
      var b = true;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
        data: { "assignmentId": assignmentId, "assignmentName": assignmentName },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            b = data.obj.success;
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
          b = false;
          systemErrorFlag = true;
        }
      });
      return b;
    },
    // 校验当前分组下的车辆数是否已经达到最大值（最大值目前设定为：100台）
    checkMaxVehicleCountOfAssignment: function (assignmentId) {
      var b = true;
      if ($.inArray(assignmentId, ais) == -1) {
        b = false;
      }
      return b;
    },
    // 校验单个分组下的车辆数是否已经达到最大值（主要用于默认分组勾选校验）
    checkSingleMaxVehicle: function (assignmentId, assignmentName) {
      var b = false;
      $.ajax({
        type: 'POST',
        url: '/clbs/m/infoconfig/infoinput/checkMaxVehicleCount',
        data: { "assignmentId": assignmentId, "assignmentName": assignmentName },
        dataType: 'json',
        async: false,
        success: function (data) {
          if (data.success) {
            b = data.obj.success;
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg(systemError, {
            time: 1500,
          });
          b = false;
          systemErrorFlag = true;
        }
      });
      return b;
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label").hide();
    },
    // 清除错误信息
    clearErrorMsg: function () {
      $("label.error").hide();
    },
  };
  return {
    publicFun: publicFun,
  }
})
