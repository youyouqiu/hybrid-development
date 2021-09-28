(function ($, window) {
  var flag = true;
  var treeData;
  var assignId = $('#assignmentId').val();
  var allAssignNode = null; // 左边树结构当前选中的节点
  var vehiclePerAdd = [];
  var vehiclePerDelete = [];
  var nodeCount = 0;
  var validSubmit = true;
  var requestRootURL = '/clbs/talkback/basicinfo/enterprise/assignment/vehiclePer.gsp';
  var treeDefaultScorllTop;
  var isGetCount = false;
  var isHideNode = true;
  var changMonitorObj = {};// 记录用户操作过的群组
  var changeAssignmentArr = [];// 节点产生变化的群组
  var assignmentVehiclePer = {
    //初始化
    initGroup: function () {
      //人权限
      var setVehicleGroup = {
        async: {
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json'
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 's',
            'N': 's'
          },
          radioType: 'all'
        },
        view: {
          dblClickExpand: false,
          nameIsHTML: true,
          countClass: 'group-number-statistics'
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: assignmentVehiclePer.beforeClickVehicleGroup,
          onCheck: assignmentVehiclePer.onCheckVehicleGroup
        }
      };
      treeData = $('#vehicleTreeData').attr('value');
      $.fn.zTree.init($('#vehicleGroupDemo'), setVehicleGroup, JSON.parse(treeData));
    },
    initAssign: function () {
      //车辆树
      var setVehicleAssign = {
        async: {
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          url: assignmentVehiclePer.getUrl,
          dataFilter: assignmentVehiclePer.ajaxDataFilter,
          otherParam: {'queryParam': null, 'queryType': null}
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 's',
            'N': 's'
          },
          radioType: 'all'
        },
        view: {
          dblClickExpand: false,
          nameIsHTML: true,
          countClass: 'group-number-statistics'
        },
        data: {
          simpleData: {
            enable: true
          }
        },
        callback: {
          beforeClick: assignmentVehiclePer.beforeClickVehicleAssign,
          onCheck: assignmentVehiclePer.onCheckVehicleAssign,
          onAsyncSuccess: assignmentVehiclePer.onVehicleAsyncSuccess,
          onAsyncError: assignmentVehiclePer.onVehicleAsyncError,
          beforeCheck: assignmentVehiclePer.onBeforeCheck
        }
      };
      if (!isGetCount) {
        $.ajax({
          async: false,
          type: 'post',
          url: requestRootURL + '/count?aid=' + assignId,
          success: function (msg) {
            isGetCount = true;
            nodeCount = parseInt(msg);
          }
        });
      }
      if (isGetCount) {
        if (nodeCount > 0 && nodeCount <= 5000) {
          setVehicleAssign.async.url = requestRootURL + '/all?aid=' + assignId;
        }
      }
      $.fn.zTree.init($('#vehicleAssignmentDemo'), setVehicleAssign);
    },
    getUrl: function (treeId, treeNode) {
      if (treeNode) {
        return requestRootURL + '/vehicles?aid=' + treeNode.id;
      }
      return requestRootURL + '/org?aid=' + assignId;
    },
    beforeClickVehicleGroup: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      if (!treeNode.checked) {
        zTree.selectNode(treeNode);
      } else {
        zTree.cancelSelectedNode(treeNode);
      }
      zTree.checkNode(treeNode, !treeNode.checked, null, true);
      return false;
    },
    onCheckVehicleGroup: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      var param = treeNode.id;
      var nodes = zTree.getNodesByParam('id', param, null);
      if (treeNode.checked) {
        for (var i = 0; i < nodes.length; i++) {
          zTree.checkNode(nodes[i], true, true);
        }
        flag = false;
      } else {
        for (i = 0; i < nodes.length; i++) {
          zTree.checkNode(nodes[i], false, true);
        }
        flag = true;
      }
    },
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      if (!responseData || !responseData.length) {
        return responseData;
      }
      responseData = JSON.parse(ungzip(responseData));
      return responseData;
    },
    onVehicleAsyncSuccess: function (event, treeId, node) {
      if (node === null) {
        return;
      }
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      if (node !== undefined && node.checked) {
        for (var i = 0; i < node.children.length; i++) {
          zTree.checkNode(node.children[i], true);
        }
      }
      if (nodeCount > 0 && nodeCount <= 5000) {
        zTree.expandAll(true);
      }
    },
    onVehicleAsyncError: function () {
      layer.msg('获取数据出现异常，请联系管理员。', {move: false});
    },
    onBeforeExpand: function (treeId, treeNode) {
      var queryType = $('#searchType').val();
      var zTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      if (treeNode && treeNode.type === 'assignment' && ('assignName' === queryType || 'groupName' === queryType)) {
        isHideNode = false;
        zTree.setting.async.url = requestRootURL + '/vehicles?aid=' + treeNode.id;
      }
    },
    onBeforeCheck: function (treeId, treeNode) {
      var queryType = $('#searchType').val();
      var zTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      if (treeNode) {
        if (treeNode.type === 'assignment' && ('assignName' === queryType || 'groupName' === queryType)) {
          isHideNode = false;
          zTree.setting.async.url = requestRootURL + '/vehicles?aid=' + treeNode.id;
        }
        if (treeNode.type === 'group') {
          if (nodeCount > 5000) {
            layer.msg('请单独勾选对应群组', {move: false});
            return false;
          }
        }
      }

    },
    beforeClickVehicleAssign: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      if (!treeNode.checked) {
        zTree.selectNode(treeNode, false, true);
      } else {
        zTree.cancelSelectedNode(treeNode);
      }
      zTree.checkNode(treeNode, !treeNode.checked, true, true);
      allAssignNode = treeNode;
      $('#allTree').scrollTop(treeDefaultScorllTop);
      return false;
    },
    onCheckVehicleAssign: function (e, treeId, treeNode) {
      isHideNode = false;
      var zTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      zTree.selectNode(treeNode, false, true);
      zTree.expandNode(treeNode, true);
      $('#allTree').scrollTop(treeDefaultScorllTop);
      // if(treeNode != undefined && treeNode != null && treeNode.type == 'group' && nodeCount > 5000 && (treeNode.checked)){
      //     var checkedAssignNodes = zTree.getNodesByFilter(function(node){
      //         return node.type == 'assignment' && node.count > 0 && node;
      //     });
      //     for (var i = 0; i < checkedAssignNodes.length; i++) {
      //         var node = checkedAssignNodes[i];
      //         if(node.open == false && node.count > 0){
      //             zTree.expandNode(node, true, true, false, true);
      //         }
      //     }
      // }
    },
    // 车辆权限  左边移到右边
    leftMoveRight: function () {
      // 获取左边选中的节点
      var allzTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      var curzTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      var curNodes = curzTree.getNodes();
      var curAssignNode = curNodes[0]; // 当前操作的群组 的节点
      var vehicleAssignCheckNodes = allzTree.getCheckedNodes();
      //右边的监控对象
      var rightMonitorNodes = curzTree.getNodesByFilter(function (node) {
        return node.type === 'vehicle' || node.type === 'people' || node.type === 'thing';
      });
      var allCheckMonitorNodes = rightMonitorNodes;
      //左边选择的监控对象
      var leftMonitorNodes = allzTree.getNodesByFilter(function (node) {
        return (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') && node.checked === true;
      });
      if (leftMonitorNodes && leftMonitorNodes.length > 0) {
        for (var i = 0; i < leftMonitorNodes.length; i++) {
          var leftMonitorNode = leftMonitorNodes[i];
          if (allCheckMonitorNodes.length === 0) {
            allCheckMonitorNodes.push(leftMonitorNode);
          } else {
            if (!assignmentVehiclePer.ztreeContains(leftMonitorNode.id, allCheckMonitorNodes)) {
              allCheckMonitorNodes.push(leftMonitorNode);
            }
          }
        }
      }
      if (allCheckMonitorNodes.length > TREE_MAX_CHILDREN_LENGTH) {
        layer.msg('分配后的群组中监控对象将超限（' + TREE_MAX_CHILDREN_LENGTH + '个），请重新选择', {move: false});
        return;
      }
      var checkVehicleList = []; // 选中的车辆节点
      if (vehicleAssignCheckNodes !== null && vehicleAssignCheckNodes.length > 0) {
        for (var i = 0; i < vehicleAssignCheckNodes.length; i++) {
          var node = vehicleAssignCheckNodes[i];

          if (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') {
            checkVehicleList.push(node);
            var checkNodes = curzTree.getNodesByParam('pId', curAssignNode.id, curAssignNode);// 右边已有节点
            if ((!assignmentVehiclePer.ztreeContains(node.id, checkNodes)) && checkNodes !== null && checkNodes.length >= TREE_MAX_CHILDREN_LENGTH) {
              layer.msg('群组下已存在' + TREE_MAX_CHILDREN_LENGTH + '个监控对象', {move: false});
              break;
            }
            var pNode = node.getParentNode();
            var parentName = pNode.name;
            var deleteObj = {}; // 删除的群组与车辆关系
            deleteObj.vehicleId = node.id;
            deleteObj.assignmentId = node.pId;
            deleteObj.assignmentName = parentName;
            deleteObj.sourceAssignId = node.pId;
            deleteObj.sourceAssignName = parentName;
            deleteObj.monitorType = node.monitorType;
            vehiclePerDelete.push(deleteObj);
            // 左边树结构移除节点
            allzTree.removeNode(node);
            allzTree.updateNodeCount(pNode);
            if (!assignmentVehiclePer.ztreeContains(node.id, checkNodes)) { // 右边树没有的节点，添加
              var addObj = {}; // 新增的群组与车辆关系
              addObj.vehicleId = node.id;
              addObj.assignmentId = assignId; // 当前操作的群组id
              addObj.assignmentName = curAssignNode.name;
              addObj.sourceAssignId = node.pId;
              addObj.sourceAssignName = parentName;
              addObj.monitorType = node.monitorType;
              vehiclePerAdd.push(addObj);
              // 右边树结构添加节点
              var objIconType;
              if (node.type === 'vehicle') {
                objIconType = 'vehicleSkin';
              } else if (node.type === 'people') {
                objIconType = 'peopleSkin';
              } else if (node.type === 'thing') {
                objIconType = 'thingSkin';
              }
              var newNode = {
                id: node.id,
                name: node.name,
                pId: assignId,
                iconSkin: objIconType,
                type: node.type,
                checked: true
              };
              curzTree.addNodes(curAssignNode, -1, newNode, false);
              curzTree.checkNode(newNode, true);
            }
          }
        }
        curzTree.updateNodeCount(curAssignNode);
        var newNodesCount = curAssignNode.children.length;
        validSubmit = newNodesCount <= TREE_MAX_CHILDREN_LENGTH;
      }
      if (checkVehicleList === null || checkVehicleList.length <= 0) {
        layer.msg('至少选择一个监控对象！', {move: false});
      }
    },
    // 车辆权限  左边复制到右边
    leftCopyRight: function () {
      // 获取左边选中的节点
      var allzTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      var curzTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      var curNodes = curzTree.getNodes();
      var curAssignNode = curNodes[0]; // 当前操作的群组 的节点
      var vehicleAssignCheckNodes = allzTree.getCheckedNodes();
      //右边的监控对象
      var rightMonitorNodes = curzTree.getNodesByFilter(function (node) {
        return node.type === 'vehicle' || node.type === 'people' || node.type === 'thing';
      });
      var allCheckMonitorNodes = rightMonitorNodes;
      //左边选择的监控对象
      var leftMonitorNodes = allzTree.getNodesByFilter(function (node) {
        return (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') && node.checked === true;
      });
      if (leftMonitorNodes && leftMonitorNodes.length > 0) {
        for (var i = 0; i < leftMonitorNodes.length; i++) {
          var leftMonitorNode = leftMonitorNodes[i];
          if (allCheckMonitorNodes.length === 0) {
            allCheckMonitorNodes.push(leftMonitorNode);
          } else {
            if (!assignmentVehiclePer.ztreeContains(leftMonitorNode.id, allCheckMonitorNodes)) {
              allCheckMonitorNodes.push(leftMonitorNode);
            }
          }
        }
      }
      if (allCheckMonitorNodes.length > TREE_MAX_CHILDREN_LENGTH) {
        layer.msg('分配后的群组中监控对象将超限（' + TREE_MAX_CHILDREN_LENGTH + '个），请重新选择', {move: false});
        return;
      }
      var checkVehicleList = [];
      if (vehicleAssignCheckNodes !== null && vehicleAssignCheckNodes.length > 0) {
        for (var i = 0; i < vehicleAssignCheckNodes.length; i++) {
          var node = vehicleAssignCheckNodes[i];
          if (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') {
            checkVehicleList.push(node);
            var checkNodes = curzTree.getNodesByParam('pId', curAssignNode.id, null);// 右边已有节点
            if ((!assignmentVehiclePer.ztreeContains(node.id, checkNodes)) && checkNodes !== null && checkNodes.length >= TREE_MAX_CHILDREN_LENGTH) {
              layer.msg('群组下已存在' + TREE_MAX_CHILDREN_LENGTH + '个监控对象！', {move: false});
              break;
            }
            if (!assignmentVehiclePer.ztreeContains(node.id, checkNodes)) {// 右边树没有的节点，添加
              var addObj = {}; // 新增的群组与车辆关系
              addObj.vehicleId = node.id;
              addObj.assignmentId = assignId; // 当前操作的群组id
              addObj.assignmentName = curAssignNode.name;
              addObj.sourceAssignId = node.pId;
              addObj.sourceAssignName = node.getParentNode().name;
              addObj.monitorType = node.monitorType;
              vehiclePerAdd.push(addObj);
              // 右边树结构添加节点
              var objIconType;
              if (node.type === 'vehicle') {
                objIconType = 'vehicleSkin';
              } else if (node.type === 'people') {
                objIconType = 'peopleSkin';
              } else if (node.type === 'thing') {
                objIconType = 'thingSkin';
              }
              var newNode = {
                id: node.id,
                name: node.name,
                pId: assignId,
                iconSkin: objIconType,
                type: node.type,
                checked: true
              };
              curzTree.addNodes(curAssignNode, -1, newNode, false);
            }
          }
        }
        var newNodesCount = curAssignNode.children.length;
        curzTree.updateNodeCount(curAssignNode);
        validSubmit = newNodesCount <= TREE_MAX_CHILDREN_LENGTH;
      }
      if (checkVehicleList === null || checkVehicleList.length <= 0) {
        layer.msg('至少选择一个监控对象！', {move: false});
      }
    },
    // 车辆权限  右边移到左边
    rightMoveLeft: function () {
      // 获取左边选中的节点
      var node;
      var allzTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      var curzTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      var vehicleCurCheckNodes = curzTree.getCheckedNodes();//得到所有右边选择的节点
      //右边的监控对象
      var rightMonitorNodes = curzTree.getNodesByFilter(function (node) {
        return (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') && node.checked === true;
      });
      var allLeftAssignNode = allzTree.getCheckedNodes();//得到所有左边选择的节点
      var checkGroupList = [];//选中的群组节点
      var checkVehicleList = []; // 选中的车辆节点
      var isb = false;
      var tipsMsg = '';
      if (allLeftAssignNode !== null && allLeftAssignNode.length > 0) {
        for (var j = 0; j < allLeftAssignNode.length; j++) {
          node = allLeftAssignNode[j];
          if (node.type === 'assignment') {
            if (node.children && node.children.length > 0) {
              var rightNodeCount = [];
              for (var i = 0; i < rightMonitorNodes.length; i++) {
                var nd = rightMonitorNodes[i];
                rightNodeCount.push(nd);
              }
              for (var i = 0; i < node.children.length; i++) {
                var monitorNode = node.children[i];
                if (!assignmentVehiclePer.ztreeContains(monitorNode.id, rightNodeCount)) {
                  rightNodeCount.push(monitorNode);
                }
              }
              if (rightNodeCount.length > TREE_MAX_CHILDREN_LENGTH) {
                isb = true;
                tipsMsg += (node.name + '，');
              }
            }
            checkGroupList.push(node);
          }
        }
      }
      if (checkGroupList === null || checkGroupList === undefined || checkGroupList.length <= 0) {
        layer.msg('请选择群组！', {move: false});
        return;
      }
      var queryParam = $('#searchAllVehicle').val();
      var queryType = $('#searchType').val();
      if (queryType === 'name' && !(queryParam === '' || queryParam === undefined || queryParam === null)) {
        layer.msg('请先清空监控对象过滤条件，重新选择群组后再操作', {move: false});
        return;
      }
      if (isb) {
        tipsMsg = tipsMsg.substring(0, tipsMsg.length - 1);
        layer.msg('分配后的群组中监控对象将超限（' + TREE_MAX_CHILDREN_LENGTH + '），请重新选择：' + tipsMsg, {move: false});
        return;
      }
      if (vehicleCurCheckNodes !== null && vehicleCurCheckNodes.length > 0) {
        for (var i = 0; i < vehicleCurCheckNodes.length; i++) {
          node = vehicleCurCheckNodes[i];
          if (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') {
            checkVehicleList.push(node);
            for (var k = 0; k < checkGroupList.length; k++) {
              var onClickLeftChildNodes = allzTree.getNodesByParam('pId', checkGroupList[k].id, checkGroupList[k]);
              // 获取左边选中节点的子节点，判断若左边群组已存在的群组车辆关系，则不添加
              if (!assignmentVehiclePer.ztreeContains(node.id, onClickLeftChildNodes)) {// 左边树没有的节点，添加
                if (onClickLeftChildNodes !== null && onClickLeftChildNodes.length >= TREE_MAX_CHILDREN_LENGTH) {
                  layer.msg('群组下已存在' + TREE_MAX_CHILDREN_LENGTH + '个监控对象！', {move: false});
                  var rightAssignNode = curzTree.getNodesByFilter(function (node) {
                    return node.type === 'assignment';
                  }, true);
                  curzTree.updateNodeCount(rightAssignNode);
                  allzTree.updateNodeCount(checkGroupList[k]);
                  return;
                }
                var addObj = {}; // 新增的群组与车辆关系
                addObj.vehicleId = node.id;
                addObj.assignmentId = checkGroupList[k].id; // 当前操作的群组id
                addObj.assignmentName = checkGroupList[k].name;
                addObj.sourceAssignId = node.pId;
                addObj.sourceAssignName = node.getParentNode().name;
                addObj.monitorType = node.monitorType;
                vehiclePerAdd.push(addObj);
                // 左边边树结构添加节点
                var objIconType;
                if (node.type === 'vehicle') {
                  objIconType = 'vehicleSkin';
                } else if (node.type === 'people') {
                  objIconType = 'peopleSkin';
                } else if (node.type === 'thing') {
                  objIconType = 'thingSkin';
                }
                var newNode = {
                  id: node.id,
                  name: node.name,
                  pId: checkGroupList[k].id,
                  iconSkin: objIconType,
                  type: node.type,
                  checked: true
                };
                allzTree.addNodes(checkGroupList[k], -1, newNode, false);
                allzTree.checkNode(newNode, true);
              }
            }
            var deleteObj = {}; // 删除的群组与车辆关系
            deleteObj.vehicleId = node.id;
            deleteObj.assignmentId = node.pId;
            deleteObj.assignmentName = node.getParentNode().name;
            deleteObj.sourceAssignId = node.pId;
            deleteObj.sourceAssignName = node.getParentNode().name;
            deleteObj.monitorType = node.monitorType;
            vehiclePerDelete.push(deleteObj);
            // 右边树结构移除节点
            curzTree.removeNode(node);
          }
        }
        var rightNode = curzTree.getNodes()[0];
        curzTree.updateNodeCount(rightNode);
        var invalidNodesCount = 0;
        checkGroupList.forEach(function (node) {
          allzTree.updateNodeCount(node);
          if (node.children && node.children.length > TREE_MAX_CHILDREN_LENGTH) {
            invalidNodesCount++;
          }
        });
        validSubmit = invalidNodesCount <= 0;
      }
      if (checkVehicleList === null || checkVehicleList.length <= 0) {
        layer.msg('至少选择一个监控对象！', {move: false});
      }
    },
    // 车辆权限  右边复制到左边
    rightCopyLeft: function () {
      var node;
      // 获取左边选中的节点
      var allzTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      var curzTree = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
      var vehicleCurCheckNodes = curzTree.getCheckedNodes();//得到所有右边选择的节点
      //右边的监控对象
      var rightMonitorNodes = curzTree.getNodesByFilter(function (node) {
        return (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') && node.checked === true;
      });
      var allLeftAssignNode = allzTree.getCheckedNodes();//得到所有左边选择的节点
      var checkGroupList = [];//选中的群组节点
      var checkVehicleList = []; // 选中的车辆节点
      var isb = false;
      var tipsMsg = '';
      if (allLeftAssignNode !== null && allLeftAssignNode.length > 0) {
        for (var j = 0; j < allLeftAssignNode.length; j++) {
          node = allLeftAssignNode[j];
          if (node.type === 'assignment') {
            if (node.children && node.children.length > 0) {
              var rightNodeCount = [];
              for (var i = 0; i < rightMonitorNodes.length; i++) {
                var nd = rightMonitorNodes[i];
                rightNodeCount.push(nd);
              }
              for (var i = 0; i < node.children.length; i++) {
                var monitorNode = node.children[i];
                if (!assignmentVehiclePer.ztreeContains(monitorNode.id, rightNodeCount)) {
                  rightNodeCount.push(monitorNode);
                }
              }
              if (rightNodeCount.length > TREE_MAX_CHILDREN_LENGTH) {
                isb = true;
                tipsMsg += (node.name + '，');
              }
            }
            checkGroupList.push(node);
          }
        }
      }
      if (checkGroupList === null || checkGroupList === undefined || checkGroupList.length <= 0) {
        layer.msg('请选择群组！', {move: false});
        return;
      }
      var queryParam = $('#searchAllVehicle').val();
      var queryType = $('#searchType').val();
      if (queryType === 'name' && !(queryParam === '' || queryParam === undefined || queryParam === null)) {
        layer.msg('请先清空监控对象过滤条件，重新选择群组后再操作', {move: false});
        return;
      }
      if (isb) {
        tipsMsg = tipsMsg.substring(0, tipsMsg.length - 1);
        layer.msg('分配后的群组中监控对象将超限（' + TREE_MAX_CHILDREN_LENGTH + '个），请重新选择：' + tipsMsg, {move: false});
        return;
      }
      if (vehicleCurCheckNodes !== null && vehicleCurCheckNodes.length > 0) {
        for (var i = 0; i < vehicleCurCheckNodes.length; i++) {
          node = vehicleCurCheckNodes[i];
          if (node.type === 'vehicle' || node.type === 'people' || node.type === 'thing') {
            checkVehicleList.push(node);
          }
        }
      }
      if (checkVehicleList === null || checkVehicleList.length <= 0) {
        layer.msg('至少选择一个监控对象！', {move: false});
        return;
      }
      for (var k = 0; k < checkGroupList.length; k++) {
        for (i = 0; i < checkVehicleList.length; i++) {
          node = checkVehicleList[i];
          var onClickLeftChildNodes = allzTree.getNodesByParam('pId', checkGroupList[k].id, checkGroupList[k]);
          // 获取左边选中节点的子节点，判断若左边群组已存在的群组车辆关系，则不添加
          if (!assignmentVehiclePer.ztreeContains(node.id, onClickLeftChildNodes)) {// 左边树没有的节点，添加
            if (onClickLeftChildNodes !== null && onClickLeftChildNodes.length >= TREE_MAX_CHILDREN_LENGTH) {
              layer.msg('群组下已存在' + TREE_MAX_CHILDREN_LENGTH + '个监控对象！', {move: false});
              allzTree.updateNodeCount(checkGroupList[k]);
              return;
            }
            var addObj = {}; // 新增的群组与车辆关系
            addObj.vehicleId = node.id;
            addObj.assignmentId = checkGroupList[k].id; // 当前操作的群组id
            addObj.assignmentName = checkGroupList[k].name;
            addObj.sourceAssignId = node.pId;
            addObj.sourceAssignName = node.getParentNode().name;
            addObj.monitorType = node.monitorType;
            vehiclePerAdd.push(addObj);
            // 左边边树结构添加节点
            var objIconType;
            if (node.type === 'vehicle') {
              objIconType = 'vehicleSkin';
            } else if (node.type === 'people') {
              objIconType = 'peopleSkin';
            } else if (node.type === 'thing') {
              objIconType = 'thingSkin';
            }
            var newNode = {
              id: node.id,
              name: node.name,
              pId: checkGroupList[k].id,
              iconSkin: objIconType,
              type: node.type,
              checked: true
            };
            allzTree.addNodes(checkGroupList[k], -1, newNode, false);
            allzTree.checkNode(newNode, true);
          }
        }
        allzTree.updateNodeCount(checkGroupList[k]);
      }
      var invalidNodesCount = 0;
      checkGroupList.forEach(function (node) {
        if (node.children.length > TREE_MAX_CHILDREN_LENGTH) {
          invalidNodesCount++;
        }
      });
      validSubmit = invalidNodesCount <= 0;
    },
    ztreeContains: function (vid, nodes) { // 群组下是否包含车辆
      if (nodes !== null && nodes !== undefined && nodes !== '') {
        for (var i = 0; i < nodes.length; i++) {
          if (vid === nodes[i].id) {
            return true;
          }
        }
      }
      return false;
    }, //按围栏 树结构搜索
    searchAllVehicle: function () {
      search_ztree('vehicleAssignmentDemo', 'searchAllVehicle', 'vehicle');
    },
    searchAllVehicleAsync: function () {
      var queryParam = $('#searchAllVehicle').val();
      var queryType = $('#searchType').val();
      isHideNode = true;
      if (queryParam !== '') {
        var setSearchTree = {
          async: {
            type: 'post',
            enable: true,
            autoParam: ['id'],
            dataType: 'json',
            url: requestRootURL + '/all?aid=' + assignId,
            dataFilter: assignmentVehiclePer.ajaxQueryFilter,
            otherParam: {'queryParam': queryParam, 'queryType': queryType}
          },
          check: {
            enable: true,
            chkStyle: 'checkbox',
            chkboxType: {
              'Y': 's',
              'N': 's'
            },
            radioType: 'all'
          },
          view: {
            dblClickExpand: false,
            nameIsHTML: true,
            countClass: 'group-number-statistics'
          },
          data: {
            simpleData: {
              enable: true
            }
          },
          callback: {
            beforeClick: assignmentVehiclePer.beforeClickVehicleAssign,
            onCheck: assignmentVehiclePer.onCheckVehicleAssign,
            onAsyncSuccess: assignmentVehiclePer.onSearchAsyncSuccess,
            onAsyncError: assignmentVehiclePer.onVehicleAsyncError,
            beforeExpand: assignmentVehiclePer.onBeforeExpand,
            beforeCheck: assignmentVehiclePer.onBeforeCheck
          }
        };
        if (queryType && ('assignName' === queryType || 'groupName' === queryType)) {
          setSearchTree.async.url = requestRootURL + '/org?aid=' + assignId;
        }
        if (nodeCount > 0 && nodeCount <= 5000) {
          setSearchTree.async.url = requestRootURL + '/all?aid=' + assignId;
        }
        $.fn.zTree.init($('#vehicleAssignmentDemo'), setSearchTree);
      } else {
        assignmentVehiclePer.initAssign();
      }
    },
    searchcurVehicle: function () {
      search_ztree('vehicleGroupDemo', 'searchCurVehicle', 'vehicle');
    },
    ajaxQueryFilter: function (treeId, parentNode, responseData) {
      if (!responseData || !responseData.length) {
        return responseData;
      }
      responseData = JSON.parse(ungzip(responseData));
      var queryType = $('#searchType').val();
      if ('assignName' === queryType || 'groupName' === queryType) {
        return responseData;
      }
      return filterQueryResult(responseData);
    },
    onSearchAsyncSuccess: function (event, treeId, node) {
      var queryParam = $('#searchAllVehicle').val();
      var queryType = $('#searchType').val();
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      if (queryType === 'name') {
        // 第一次加载树后默认展开200个子结点
        var initLen = 0;
        notExpandNodeInit = zTree.getNodesByFilter(assignmentNotExpandFilter);
        for (i = 0; i < notExpandNodeInit.length; i++) {
          zTree.expandNode(notExpandNodeInit[i], true, true, false, true);
          initLen += notExpandNodeInit[i].children.length;
          if (initLen >= 200) {
            break;
          }
        }
      }
      if (queryType === 'groupName' && isHideNode) {
        zTree.expandAll(true);
        var nodesByParamFuzzy = zTree.getNodesByFilter(function (node) {
          return node.type === 'group' && node.name.indexOf(queryParam) > -1;
        });
        for (var i = 0; i < nodesByParamFuzzy.length; i++) {
          zTree.expandNode(nodesByParamFuzzy[i], false, true, false, false);
        }
      }
      if (queryType === 'assignName' && nodeCount > 0 && nodeCount <= 5000) {
        zTree.expandAll(true);
        var nodesByParamFuzzy = zTree.getNodesByFilter(function (node) {
          return node.type === 'assignment' && node.name.indexOf(queryParam) > -1;
        });
        for (var i = 0; i < nodesByParamFuzzy.length; i++) {
          zTree.expandNode(nodesByParamFuzzy[i], false, true, false, false);
        }
      }
      if (nodeCount > 5000 && node !== undefined && node.checked) {
        for (var i = 0; i < node.children.length; i++) {
          zTree.checkNode(node.children[i], true);
        }
      }
    },
    doSubmit: function () {
      //询问框
      layer.confirm('您确定本次操作吗？', {
        btn: ['确定', '取消'] //按钮
      }, function () {
        if (!validSubmit) {
          layer.msg('群组下不能超过100个监控对象，请检查', {move: false});
          return;
        }
        $('#vehiclePerAdd').val(JSON.stringify(vehiclePerAdd));
        $('#vehiclePerDelete').val(JSON.stringify(vehiclePerDelete));

        $('#vehiclePerForm').ajaxSubmit(function () {
          $('#commonWin').modal('hide');
          /* 关闭弹窗 */
          myTable.refresh();
        });
        layer.closeAll();
      });
    }
  };
  $(function () {
    var width = $(window).width();
    if (width < 1400) {
      $('#resourceAll1').children().eq(0).removeClass('col-md-10').addClass('col-md-9');
      $('#resourceAll2').children().eq(0).removeClass('col-md-10').addClass('col-md-9');
    }
    assignmentVehiclePer.initGroup();
    assignmentVehiclePer.initAssign();
    // 自定义组织树placeholder效果(用以解决IE中输入框聚焦触发input事件)
    initPlaceholder('#searchAllVehicle');

    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      if (id === 'searchAllVehicle') {
        assignmentVehiclePer.searchAllVehicleAsync();
      }
      if (id === 'searchCurVehicle') {
        search_ztree('vehicleGroupDemo', id, 'vehicle');
      }
    });
    myTable.add('commonSmWin', 'vehiclePerForm', null, null);
    var treeObj = $.fn.zTree.getZTreeObj('vehicleGroupDemo');
    treeObj.expandAll(true);
    $('#addMoveBtn').on('click', assignmentVehiclePer.leftMoveRight);
    $('#addCopyBtn').on('click', assignmentVehiclePer.leftCopyRight);
    $('#removeMoveBtn').on('click', assignmentVehiclePer.rightMoveLeft);
    $('#removeCopyBtn').on('click', assignmentVehiclePer.rightCopyLeft);
    $('#doSubmitVehiclePer').on('click', assignmentVehiclePer.doSubmit);
    $('#allTree').scroll(function () {
      treeDefaultScorllTop = 0;
      treeDefaultScorllTop = $('#allTree').scrollTop();
      var zTree = $.fn.zTree.getZTreeObj('vehicleAssignmentDemo');
      var queryType = $('#searchType').val();
      if (queryType === 'name') {
        zTreeScroll(zTree, this);
      }
    });
    $('#searchType').change(function () {
      var queryParam = $('#searchAllVehicle').val();
      if (queryParam !== '') {
        assignmentVehiclePer.searchAllVehicleAsync();
      }
    });
    // 树结构模糊搜索
    var searchCurVehicle = $('#searchCurVehicle');
    var inputChange;
    $('#searchAllVehicle').on('input', function () {
      // 在IE浏览器下，设置placeholder会触发input事件
      /*if ($(this).val() === $(this).get(0).defaultValue) {
       return;
       }*/
      if (inputChange !== undefined) {
        clearTimeout(inputChange);
      }
      inputChange = setTimeout(assignmentVehiclePer.searchAllVehicleAsync, 500);
    });
    searchCurVehicle.on('input', assignmentVehiclePer.searchcurVehicle);
    //IE9
    var search;
    if (navigator.appName === 'Microsoft Internet Explorer' &&
      navigator.appVersion.split(';')[1].replace(/[ ]/g, '') === 'MSIE9.0') {
      $('#searchAllVehicle').bind('focus', function () {
        search = setInterval(function () {
          search_ztree('vehicleAssignmentDemo', 'searchAllVehicle', 'vehicle');
        }, 500);
      }).bind('blur', function () {
        clearInterval(search);
      });
    }
    //IE9 end
    if (navigator.appName === 'Microsoft Internet Explorer' &&
      navigator.appVersion.split(';')[1].replace(/[ ]/g, '') === 'MSIE9.0') {
      searchCurVehicle.bind('focus', function () {
        search = setInterval(function () {
          search_ztree('vehicleGroupDemo', 'searchCurVehicle', 'vehicle');
        }, 500);
      }).bind('blur', function () {
        clearInterval(search);
      });
    }
    //IE9 end
  });
})($, window);