//# sourceURL=infoinputFriendsSetForDispatch.js
(function ($, window) {
  var dispatcherInit = true; // 调度员树是否已初始化
  var curShowTreeId = 'intercomTree'; // 左侧当前显示的组织树ID('intercomTree':对讲对象;'dispatcherTree':调度员)
  var friendsTreeArr = JSON.parse($('#friends').val()); // 已有好友树
  var maxFriendsNum = parseInt($('#maxFriendNum').val()); // 最多好友数量
  var hiddenNodes = '';
  var searchFlag = true;
  var intercomTreeExpandFlag = true; // 对讲对象树初始展开第一个分组

  window.friendsSet = {
    init: function () {
      friendsSet.intercomTreeInit();
      friendsSet.friendsTreeInit();
      $('#checkedNum').text(friendsTreeArr.length);
    },
    /**
     * 页面左侧组织树
     */
    // 对讲对象树
    intercomTreeInit: function () {
      var settting = {
        async: {
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          url: friendsSet.getIntercomTreeUrl,
          dataFilter: friendsSet.ajaxDataFilter,
          otherParam: {'queryParam': null, 'queryType': null}
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 'ps',
            'N': 'ps'
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
          beforeClick: friendsSet.beforeClickVehicle,
          onCheck: friendsSet.onCheckVehicle,
          onAsyncSuccess: friendsSet.onVehicleAsyncSuccess
        }
      };
      $.fn.zTree.init($('#intercomTree'), settting);
    },
    getIntercomTreeUrl: function (treeId, treeNode) {
      if (treeNode == null) {
        return '/clbs/talkback/inconfig/intercomObject/getIntercomObjectFuzzy';
      } else if (treeNode.type === 'assignment') {
        return '/clbs/talkback/inconfig/intercomObject/getAssignmentIntercomObject?assignmentId=' + treeNode.id;
      }
    },
    // 数据筛选过滤
    ajaxDataFilter: function (treeId, parentNode, responseData) {
      var rData;
      if (!responseData.msg) {
        rData = responseData;
      } else {
        rData = responseData.msg;
      }
      var obj = JSON.parse(ungzip(rData));
      var data;
      if (obj.tree !== null && obj.tree !== undefined) {
        data = obj.tree;
        size = obj.size;
      } else {
        data = obj;
      }
      var ztreeObj = null;
      if (treeId === 'intercomTree') {
        ztreeObj = $.fn.zTree.getZTreeObj(treeId);
      }
      var friendsTree = $.fn.zTree.getZTreeObj('friendsTree');
      var hasFriendsArr = friendsTree ? friendsTree.getNodes() : friendsTreeArr;
      var friendsLen = hasFriendsArr ? hasFriendsArr.length : 0;
      if ((friendsLen > 0 || ztreeObj) && treeId !== 'friendsTree') {
        for (var i = 0; i < data.length; i++) {
          var item = data[i];
          if (item.type === 'assignment') continue;
          if (item.type === 'group') {
            item.nocheck = true;
            continue;
          }
          // 单独请求分组下的节点时,如果分组或其他分组下的本节点被勾选,则子节点也要勾选
          if (item.type === 'people' || item.type === 'vehicle' || item.type === 'thing') {
            var pid = item.pId;
            var parentNode = ztreeObj.getNodesByParam('id', pid, null);
            var checkFlag = false;
            var sameNode = ztreeObj.getNodesByParam('userId', item.userId, null);
            for (var k = 0; k < sameNode.length; k++) {
              // 一个对讲对象可在多个分组下,查看其他分组下的对讲对象是否有被勾选的
              if (sameNode[k].checked) {
                checkFlag = true;
                break;
              }
            }
            if (parentNode[0] && parentNode[0].checked || checkFlag) {
              item.checked = true;
            }
          }
          // 已有好友在左侧对象树中禁止选中
          for (var j = 0; j < friendsLen; j++) {
            if (+item.userId === hasFriendsArr[j].friendId) {
              item.checked = true;
              item.chkDisabled = true;
            }
          }
        }
      }
      return data;
    },
    onVehicleAsyncSuccess: function (event, treeId, node) {
      if (treeId === 'intercomTree') {
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        if (intercomTreeExpandFlag) {// 默认展开对讲对象树第一个分组
          var nodes = treeObj.getNodesByParam('type', 'assignment', null);
          if (nodes[0]) {
            intercomTreeExpandFlag = false;
            treeObj.expandNode(nodes[0], true, true, false, true);
          }
        }
        // 隐藏当前正在分配好友的对讲对象
        var curObjNode = treeObj.getNodesByParam('userId', $('#userId').val(), null);
        if (curObjNode && curObjNode.length > 0) {
          treeObj.hideNodes(curObjNode);
          /*for (var i = 0; i < curObjNode.length; i++) {
           treeObj.removeNode(curObjNode[i]);
           }*/
        }
      }
    },
    beforeClickVehicle: function (treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      if (!treeNode.checked) {
        zTree.selectNode(treeNode, false, true);
      } else {
        zTree.cancelSelectedNode(treeNode);
      }
      zTree.checkNode(treeNode, !treeNode.checked, true, true);
      return false;
    },
    onCheckVehicle: function (e, treeId, treeNode) {
      var zTree = $.fn.zTree.getZTreeObj(treeId);
      if (treeNode.checked) {
        searchFlag = false;
        zTree.selectNode(treeNode, false, true);
        zTree.expandNode(treeNode, true, true, false, true);
      }
      if (treeId === 'intercomTree') { // 勾选对讲对象时，联动勾选其他分组下相同的对讲对象
        var nodeType = ['people', 'vehicle', 'thing'];
        var nodes = [];
        if (nodeType.indexOf(treeNode.type) !== -1) {
          nodes = zTree.getNodesByParam('userId', treeNode.userId, null);
        } else {
          var peopleNodes = zTree.getNodesByParam('type', 'people', treeNode);
          var vehicleNodes = zTree.getNodesByParam('type', 'vehicle', treeNode);
          var thingNodes = zTree.getNodesByParam('type', 'thing', treeNode);
          var ns = [peopleNodes, vehicleNodes, thingNodes];

          for (var j = 0; j < ns.length; j++) {
            for (var i = 0; i < ns[j].length; i++) {
              nodes = nodes.concat(zTree.getNodesByParam('userId', ns[j][i].userId, null));
            }
          }
        }

        for (var i = 0; i < nodes.length; i++) {
          zTree.checkNode(nodes[i], treeNode.checked, true);
        }
      }
      if (treeId === 'friendsTree') {
        if (!treeNode.checked) {
          $('.allMonitor').prop('checked', false);
        } else {
          var hasFriendsNum = zTree.getNodes().length;
          var checkFriendsNum = zTree.getCheckedNodes().length;
          if (hasFriendsNum !== 0 && hasFriendsNum === checkFriendsNum) {
            $('.allMonitor').prop('checked', true);
          }
        }
      }
    },
    //模糊查询树
    searchIntercomTree: function (param) {

      if (param === null || param === undefined || param === '') {
        intercomTreeExpandFlag = true;
        friendsSet.intercomTreeInit();
      } else {
        var querySetting = {
          async: {
            url: friendsSet.getIntercomTreeUrl,
            type: 'post',
            enable: true,
            autoParam: ['id'],
            dataType: 'json',
            otherParam: {'type': 'multiple', 'queryParam': param},
            dataFilter: friendsSet.searchAjaxDataFilter
          },
          check: {
            enable: true,
            chkStyle: 'checkbox',
            radioType: 'all',
            chkboxType: {
              'Y': 'ps',
              'N': 'ps'
            }
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
            beforeClick: friendsSet.beforeClickVehicle,
            onCheck: friendsSet.onCheckVehicle,
            onAsyncSuccess: friendsSet.onVehicleAsyncSuccess
          }
        };
        $.fn.zTree.init($('#intercomTree'), querySetting, null);
      }
    },
    searchAjaxDataFilter: function (treeId, parentNode, responseData) {
      var rData;
      if (!responseData.msg) {
        rData = responseData;
      } else {
        rData = responseData.msg;
      }
      var obj = JSON.parse(ungzip(rData));
      var data;
      if (obj.tree) {
        data = obj.tree;
        size = obj.size;
      } else {
        data = obj;
      }
      var ztreeObj = null;
      if (treeId === 'intercomTree') {
        ztreeObj = $.fn.zTree.getZTreeObj(treeId);
      }
      var friendsTree = $.fn.zTree.getZTreeObj('friendsTree');
      var hasFriendsArr = friendsTree ? friendsTree.getNodes() : friendsTreeArr;
      var friendsLen = hasFriendsArr ? hasFriendsArr.length : 0;
      if ((friendsLen > 0 || ztreeObj) && treeId !== 'friendsTree') {
        for (var i = 0; i < data.length; i++) {
          var item = data[i];
          if (item.type === 'group') continue;
          if (item.type === 'assignment') {
            item.open = true;
            continue;
          }
          // 单独请求分组下的节点时，如果分组被勾选，则子节点也要勾选
          if (item.type === 'people' || item.type === 'vehicle' || item.type === 'thing') {
            var pid = item.pId;
            var parentNode = ztreeObj.getNodesByParam('id', pid, null);
            if (parentNode[0] && parentNode[0].checked) {
              item.checked = true;
            }
          }
          // 已有好友在左侧对象树中禁止选中
          for (var j = 0; j < friendsLen; j++) {
            if (item.userId === hasFriendsArr[j].friendId) {
              item.checked = true;
              item.chkDisabled = true;
            }
          }
        }
      }
      for (var i = 0; i < data.length; i++) {
        // 移除当前正在分配好友的对讲对象
        if (data[i].userId === $('#userId').val()) {
          data.splice(i, 1);
          break;
        }
      }
      return filterQueryResult(data);
    },
    /**
     * 调度员树
     * */
    dispatcherTreeInit: function () {
      var settting = {
        async: {
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          url: '/clbs/talkback/inconfig/intercomObject/getDispatcherFuzzy',
          dataFilter: friendsSet.ajaxDataFilter,
          otherParam: {'queryParam': null, 'queryType': null}
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 'ps',
            'N': 'ps'
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
          beforeClick: friendsSet.beforeClickVehicle,
          onCheck: friendsSet.onCheckVehicle,
          onAsyncSuccess: friendsSet.onVehicleAsyncSuccess,
          beforeCheck: friendsSet.onBeforeCheck
        }
      };
      $.fn.zTree.init($('#dispatcherTree'), settting);
    },
    //模糊查询树
    searchDispatcherTree: function (param) {
      if (!param) {
        friendsSet.dispatcherTreeInit();
      } else {
        var querySetting = {
          async: {
            url: '/clbs/talkback/inconfig/intercomObject/getDispatcherFuzzy',
            type: 'post',
            enable: true,
            autoParam: ['id'],
            dataType: 'json',
            otherParam: {'type': 'multiple', 'queryParam': param},
            dataFilter: friendsSet.ajaxDataFilter
          },
          check: {
            enable: true,
            chkStyle: 'checkbox',
            radioType: 'all',
            chkboxType: {
              'Y': 'ps',
              'N': 'ps'
            }
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
            beforeClick: friendsSet.beforeClickVehicle,
            onCheck: friendsSet.onCheckVehicle,
            onAsyncSuccess: friendsSet.onVehicleAsyncSuccess
          }
        };
        $.fn.zTree.init($('#dispatcherTree'), querySetting, null);
      }
    },
    // 加入好友
    addFriend: function () {
      var treeObj = $.fn.zTree.getZTreeObj(curShowTreeId);
      var friendsTree = $.fn.zTree.getZTreeObj('friendsTree');
      var nodeType = ['people', 'vehicle', 'thing'];
      if (curShowTreeId === 'dispatcherTree') {
        nodeType = ['user'];
      }
      var checkedNodes = treeObj.getNodesByFilter(function (node) {
        return (nodeType.indexOf(node.type) !== -1 && node.checked && !node.chkDisabled && !node.isHidden);
      });
      if (checkedNodes.length === 0) {
        layer.msg('至少勾选一个对象');
        return;
      }
      var newcheckedNodes = objArrRemoveRepeat(checkedNodes, 'userId');// 数组去重
      var hasFriendsNum = friendsTree.getNodes() ? friendsTree.getNodes().length : 0;
      if (newcheckedNodes.length + hasFriendsNum > maxFriendsNum) {
        layer.msg('超过最大好友数量');
        return;
      }
      $('.curSelectedNode').removeClass('curSelectedNode');
      for (var i = 0; i < checkedNodes.length; i++) {
        var itemNode = checkedNodes[i];
        var newNode = {
          id: itemNode.id,
          friendId: itemNode.userId,
          name: itemNode.name,
          iconSkin: itemNode.iconSkin,
          type: curShowTreeId === 'intercomTree' ? 1 : 0,
          checked: true
        };
        var hasNode = friendsTree.getNodesByParam('friendId', itemNode.userId, null);
        if (!hasNode.length) {
          friendsTree.addNodes(null, newNode);// 已有好友中添加相关节点
        }
        treeObj.setChkDisabled(checkedNodes[i], true);// 左侧树中的相关节点禁止勾选
      }
      var hasFriendsNum = friendsTree.getNodes() ? friendsTree.getNodes().length : 0;
      var checkFriendsNum = friendsTree.getCheckedNodes() ? friendsTree.getCheckedNodes().length : 0;
      if (hasFriendsNum !== 0 && hasFriendsNum === checkFriendsNum) {
        $('.allMonitor').prop('checked', true);
      } else {
        $('.allMonitor').prop('checked', false);
      }
      $('#checkedNum').text(friendsTree.getNodes().length);
    },
    // 对讲对象与调度员tab切换
    tabChange: function () {
      if ($(this).hasClass('active')) return;
      var id = $(this).attr('id');
      $('#searchAllVehicle').val('');
      if (id === 'dispatcher') {// 调度员
        dispatcherInit = false;
        friendsSet.dispatcherTreeInit();
        curShowTreeId = 'dispatcherTree';
        $('#intercomTree').hide();
        $('#dispatcherTree').show();
      } else {// 对讲对象
        intercomTreeExpandFlag = true;
        friendsSet.intercomTreeInit();
        curShowTreeId = 'intercomTree';
        $('#intercomTree').show();
        $('#dispatcherTree').hide();
      }
      $(this).addClass('active').siblings('li').removeClass('active');
    },
    /**
     * 页面右侧已有好友树
     * */
    friendsTreeInit: function () {
      var settting = {
        async: {
          type: 'post',
          enable: true,
          autoParam: ['id'],
          dataType: 'json',
          // url: friendsSet.getUrl,
          dataFilter: friendsSet.ajaxDataFilter,
          otherParam: {'queryParam': null, 'queryType': null}
        },
        check: {
          enable: true,
          chkStyle: 'checkbox',
          chkboxType: {
            'Y': 'ps',
            'N': 'ps'
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
          beforeClick: friendsSet.beforeClickVehicle,
          onCheck: friendsSet.onCheckVehicle,
          onAsyncSuccess: friendsSet.onVehicleAsyncSuccess,
          beforeCheck: friendsSet.onBeforeCheck
        }
      };
      
      $.fn.zTree.init($('#friendsTree'), settting, friendsTreeArr);
    },
    // 已有好友树结构模糊搜索
    searchFriendTree: function (txtObj) {
      var zTreeObj = $.fn.zTree.getZTreeObj('friendsTree');
      if (txtObj.value.length > 0) {
        //显示上次搜索后被隐藏的结点
        if (hiddenNodes !== '') {
          zTreeObj.showNodes(hiddenNodes);
        }

        //查找不符合条件的子节点
        function filterFunc(node) {
          var _keywords = txtObj.value;
          return node.name.indexOf(_keywords) === -1;
        }

        //获取不符合条件的子结点
        hiddenNodes = zTreeObj.getNodesByFilter(filterFunc);
        //隐藏不符合条件的子结点
        zTreeObj.hideNodes(hiddenNodes);
      } else {
        friendsTreeArr = zTreeObj.getNodes();
        zTreeObj.showNodes(friendsTreeArr);
      }
      var checked = $('.allMonitor').is(':checked');
      zTreeObj.checkAllNodes(checked);
    },
    // 移除好友
    removeFriend: function () {
      if (dispatcherInit) {// 如果调度员树还未初始化
        friendsSet.dispatcherTreeInit();
        dispatcherInit = false;
      }
      var friendsTree = $.fn.zTree.getZTreeObj('friendsTree');
      var checkedNodes = friendsTree.getCheckedNodes(true);
      var treeObj = $.fn.zTree.getZTreeObj(curShowTreeId);
      if (checkedNodes.length === 0) {
        layer.msg('至少勾选一个好友');
        return;
      }
      for (var i = 0; i < checkedNodes.length; i++) {
        var itemNode = checkedNodes[i];
        var leftNodes = treeObj.getNodesByParam('userId', itemNode.friendId, null);
        if (leftNodes.length <= 0) {
          var newTreeObj = $.fn.zTree.getZTreeObj(curShowTreeId === 'intercomTree' ? 'dispatcherTree' : 'intercomTree');
          leftNodes = newTreeObj.getNodesByParam('userId', itemNode.friendId, null);
          if (leftNodes[0]) {
            for (var j = 0; j < leftNodes.length; j++) {
              newTreeObj.setChkDisabled(leftNodes[j], false);// 左侧树中的相关节点取消禁止勾选
            }
          }
        } else {
          for (var j = 0; j < leftNodes.length; j++) {
            treeObj.setChkDisabled(leftNodes[j], false);// 左侧树中的相关节点取消禁止勾选
          }
        }
        friendsTree.removeNode(itemNode);// 已有好友中移除相关节点
      }
      $('#checkedNum').text(friendsTree.getNodes() ? friendsTree.getNodes().length : 0);
    },
    // 全选操作
    checkAllFriends: function () {
      var checked = $(this).is(':checked');
      var treeObj = $.fn.zTree.getZTreeObj('friendsTree');
      treeObj.checkAllNodes(checked);
    },
    doSubmit: function () {
      //询问框
      layer.confirm('您确定本次操作吗？', {
        btn: ['确定', '取消'] //按钮
      }, function () {
        var userId = $('#userId').val();
        var friendsTree = $.fn.zTree.getZTreeObj('friendsTree');
        var friendsArr = friendsTree.getNodes();
        var friendsLen = friendsArr ? friendsArr.length : 0;
        var newFriendsArr = [];
        for (var i = 0; i < friendsLen; i++) {
          var obj = {
            'friendId': friendsArr[i].friendId,
            'type': friendsArr[i].type,
            'userId': userId
          };
          newFriendsArr.push(obj);
        }
        $('#userForm').val(JSON.stringify(newFriendsArr));
        $('#friendsSetForm').ajaxSubmit(function (data) {
          layer.closeAll();
          var data = JSON.parse(data);
          if (data.success) {
            $('#commonWin').modal('hide');
            myTable.refresh();
          } else if (data.msg) {
            layer.msg(data.msg);
          }
        });
      });
    }
  };
  $(function () {
    friendsSet.init();
    $('#myTab').on('click', 'li', friendsSet.tabChange);
    $('.allMonitor').on('change', friendsSet.checkAllFriends);
    $('#addMoveBtn').on('click', friendsSet.addFriend);
    $('#removeMoveBtn').on('click', friendsSet.removeFriend);
    $('#friendDoSubmit').on('click', friendsSet.doSubmit);

    $('input').inputClear().on('onClearEvent', function (e, data) {
      var id = data.id;
      if (id === 'searchAllVehicle') {
        if (curShowTreeId === 'intercomTree') {
          intercomTreeExpandFlag = true;
          friendsSet.intercomTreeInit();
        } else {
          friendsSet.dispatcherTreeInit();
        }
      }
      if (id === 'searchCurVehicle') {
        // $(".allMonitor").prop("checked",false);
        var zTreeObj = $.fn.zTree.getZTreeObj('friendsTree');
        friendsTreeArr = zTreeObj.getNodes();
        zTreeObj.showNodes(friendsTreeArr);
        var checked = $('.allMonitor').is(':checked');
        zTreeObj.checkAllNodes(checked);
      }
    });
    /**
     * 监控对象树模糊查询
     */
    var inputChange;
    $('#searchAllVehicle').on('input propertychange', function (value) {
      if (inputChange !== undefined) {
        clearTimeout(inputChange);
      }
      if (!(window.ActiveXObject || 'ActiveXObject' in window)) {
        searchFlag = true;
      }
      inputChange = setTimeout(function () {
        if (searchFlag) {
          var param = $('#searchAllVehicle').val();
          if (curShowTreeId === 'intercomTree') {
            friendsSet.searchIntercomTree(param);
          } else {
            friendsSet.searchDispatcherTree(param);
          }
        }
        searchFlag = true;
      }, 500);
    });
  });
})($, window);