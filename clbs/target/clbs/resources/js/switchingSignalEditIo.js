(function (window, $) {
  var typeDataList;
  var sesecltTypeId = "";
  var temp_signal_ids = "";
  var isResploseState = false;
  var clearResPlose;
  var delIds = new Array();
  var typeList = [];
  var editWebSocketFlag = true;
  ioEditParSetting = {
    init: function () {
      // 请求后台，获取所有订阅的车
      // webSocket.init('/clbs/vehicle');
      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function () {
        var cancelStrS = {
          "desc": {
            "MsgId": 40964,
            "UserName": $("#userName").text()
          },
          "data": params || ''
        };
        webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
      }
      json_ajax("POST", "/clbs/m/switching/type/addAllowlist", "json", true, null, ioEditParSetting.initTypeCallback);
      var referVehicleList = JSON.parse($("#referVehicleList").attr("value"));
      // 初始化车辆数据
      var dataList = {value: []};
      if (referVehicleList != null && referVehicleList.length > 0) {
        var brands = $("#brand").val();
        for (var i = 0; i < referVehicleList.length; i++) {
          var obj = {};
          //删除相同车牌信息
          if (referVehicleList[i].brand == brands) {
            referVehicleList.splice(referVehicleList[i].brand.indexOf(brands), 1);
          }
          //处理参考车牌只存在一条数据时防止浏览器抛出错误信息
          if (referVehicleList[i] == undefined) {
            dataList.value.push(obj);
          } else {
            obj.id = referVehicleList[i].vehicleId;
            obj.name = referVehicleList[i].brand;
            dataList.value.push(obj);
          }
        }
        //取消全选勾
        $("#checkAll").prop('checked', false);
        $("input[name=subChk]").prop("checked", false);
      }
      $("#referBrands").bsSuggest({
        indexId: 1,  //data.value 的第几个数据，作为input输入框的内容
        indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
        idField: "id",
        keyField: "name",
        effectiveFields: ["name"],
        searchFields: ["id"],
        data: dataList
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        // 当选择参考车牌
        var vehicleId = keyword.id;
        //加载数据
        ioEditParSetting.loadData(vehicleId);
        ioEditParSetting.setTypeCallback();
        ioEditParSetting.sendParams();
      }).on('onUnsetSelectValue', function () {
      });
    },
    loadData: function (vehicleId, flag) {
      $.ajax({
        type: 'POST',
        url: '/clbs/m/io/config/getVehicleBindIos?vehicleId=' + vehicleId,
        async: false,
        dataType: 'json',
        success: function (data) {
          if (data.success) {
            ioEditParSetting.hideErrorMsg();
            var deviceIos = data.obj.deviceIos;
            var deviceIoNode = new Array();
            if (deviceIos.length == 0) {
              for (var i = 0; i < 4; i++) {
                deviceIoNode.push(ioEditParSetting.addIoNodeText(i, 1, null, flag).join(''));
              }
            } else {
              var index = deviceIos[deviceIos.length - 1].ioSite;
              index = index < 4 ? 4 : index;
              var _index = 0;
              for (var i = 0; i < index; i++) {
                if (deviceIos[_index].ioSite == i) {
                  deviceIoNode.push(ioEditParSetting.addIoNodeText(i, 2, deviceIos[_index], flag).join(''));
                  deviceIos.length - 1 > _index ? _index++ : _index;
                } else {
                  deviceIoNode.push(ioEditParSetting.addIoNodeText(i, 2, null, flag).join(''));
                }
              }
            }
            $("#home1 .widget-todo li input[name='id']").each(function (index, node) {
              if ($(node).val()) {
                delIds.push($(node).val());
              }
            })
            $("#home1 .widget-todo li").remove();
            $("#home1 .widget-todo").append(deviceIoNode.join(""));
            var collectionOneIos = data.obj.collectionOneIos;
            var collectionOneNode = new Array();
            if (collectionOneIos.length == 0) {
              for (var i = 0; i < 16; i++) {
                collectionOneNode.push(ioEditParSetting.addIoNodeText(i, 2, null, flag).join(''));
              }
            } else {
              var index = collectionOneIos[collectionOneIos.length - 1].ioSite;
              index = index < 16 ? 15 : index;
              var _index = 0;
              for (var i = 0; i <= index; i++) {
                if (collectionOneIos[_index].ioSite == i) {
                  collectionOneNode.push(ioEditParSetting.addIoNodeText(i, 2, collectionOneIos[_index], flag).join(''));
                  collectionOneIos.length - 1 > _index ? _index++ : _index;
                } else {
                  collectionOneNode.push(ioEditParSetting.addIoNodeText(i, 2, null, flag).join(''));
                }
              }
            }
            $("#profile1 .widget-todo li input[name='id']").each(function (index, node) {
              if ($(node).val()) {
                delIds.push($(node).val());
              }
            })
            $("#profile1 .widget-todo li").remove();
            $("#profile1 .widget-todo").append(collectionOneNode.join(""));
            var collectionTwoIos = data.obj.collectionTwoIos;
            var collectionTwoNode = new Array();
            if (collectionTwoIos.length == 0) {
              for (var i = 0; i < 16; i++) {
                collectionTwoNode.push(ioEditParSetting.addIoNodeText(i, 3, null, flag).join(''));
              }
            } else {
              var index = collectionTwoIos[collectionTwoIos.length - 1].ioSite;
              index = index < 16 ? 15 : index;
              var _index = 0;
              for (var i = 0; i <= index; i++) {
                if (collectionTwoIos[_index].ioSite == i) {
                  collectionTwoNode.push(ioEditParSetting.addIoNodeText(i, 3, collectionTwoIos[_index], flag).join(''));
                  collectionTwoIos.length - 1 > _index ? _index++ : _index;
                } else {
                  collectionTwoNode.push(ioEditParSetting.addIoNodeText(i, 3, null, flag).join(''));
                }
              }
            }
            $("#profile2 .widget-todo li input[name='id']").each(function (index, node) {
              if ($(node).val()) {
                delIds.push($(node).val());
              }
            })
            $("#profile2 .widget-todo li").remove();
            $("#profile2 .widget-todo").append(collectionTwoNode.join(""));
          } else {
            layer.msg(data.msg);
          }
        },
        error: function () {
          layer.msg(systemError, {move: false});
        }
      });
    },
    initInput: function (typeid, type, name, dataid, sesctType) {
      $("#" + typeid).val("");
      $("#" + typeid).removeAttr("alt");
      $("#" + typeid).removeAttr("data-id");
      $("#" + typeid).siblings().find("ul").html("");
      if (dataid.length > 5) {
        $("#" + typeid).val(name);
        $("#" + typeid).attr("alt", name);
        $("#" + typeid).attr("data-id", dataid);
      }
      $("#" + type).find("option").removeAttr("selected");
      $("#" + type).find("option[value='" + sesctType + "']").attr("selected", "selected");
    },
    //初始化类型设置
    initTypeCallback: function (data) {
      var datas = data.obj;
      typeDataList = {
        value: []
      }, i = 0;
      while (i < datas.typeList.length) {
        typeDataList.value.push({
          name: html2Escape(datas.typeList[i].name),
          id: datas.typeList[i].id,
        });
        i++;
      }
      typeList = datas.typeList;
      var vehicleId = $("#vehicleId").val();
      ioEditParSetting.loadData(vehicleId, true);
      ioEditParSetting.setTypeCallback();
    },
    //初始化类型设置
    setTypeCallback: function () {
      $(".sensorType").bsSuggest("destroy"); // 销毁事件
      $(".sensorType").bsSuggest({
        indexId: 1,
        indexKey: 0,
        idField: "id",
        keyField: "name",
        effectiveFields: ["name"],
        searchFields: ["name"],
        data: typeDataList
      }).on('onDataRequestSuccess', function (e, result) {
      }).on('onSetSelectValue', function (e, keyword, data) {
        $("#referBrands").val("");
        ioEditParSetting.hideErrorMsg();
        var types = $(".sensorType");
        var sesecltTypeId = "";
        $(this).prev().val(keyword.id);
        for (var i = 0; i < types.length; i++) {
          sesecltTypeId += $(types[i]).attr("id") + "_" + $(types[i]).attr("data-id") + "#";
        }
        sesecltTypeId = sesecltTypeId.replace($(this).attr("id") + "_" + keyword.id, "");
        if (sesecltTypeId.indexOf(keyword.id) > -1) {
          var node = $(e.currentTarget).parent().parent().parent().find('select');
          $(node).find('option').remove();
          var nodeHtml = '<option value="">--状态--</option>';
          $(node).append(nodeHtml);
          ioEditParSetting.showErrorMsg(signalChannelExist, $(e.currentTarget));
          // return;
        }
        for (var i = 0; i < typeList.length; i++) {
          if (keyword.id == typeList[i].id) {
            var node = $(e.currentTarget).parent().parent().parent().find('select');
            $(node).find('option').remove();
            var nodeHtml = '<option value="">--状态--</option>';
            nodeHtml += '<option value="1">' + typeList[i].stateOne + '</option>';
            nodeHtml += '<option value="2">' + typeList[i].stateTwo + '</option>'
            // $(node).find('option').remove();
            $(node).append(nodeHtml);
            return;
          }
        }
      }).on('onUnsetSelectValue', function (e, keyword, data) {
        $(e.currentTarget).prev().val('');
        $(e.currentTarget).parent().parent().parent().find('select option').remove();
      });
      // 解决IE浏览器一键删除按钮显示问题
      setTimeout(function () {
        $('i.delIcon').hide();
      }, 300);
    },
    //提交
    doSubmits: function () {

      ioEditParSetting.hideErrorMsg();
      var ioVehicleConfigs = new Array();
      var homeLi = $('#home1 .widget-todo li');
      var profile1Li = $('#profile1 .widget-todo li');
      var profile2Li = $('#profile2 .widget-todo li');
      var vehicleId = $("input[name='vehicleId']").val();
      var isSubmit = true;
      var checkArr = new Array();
      //终端IO
      $(homeLi).each(function (index, node) {
        var functionId = $(node).find(".sensorType").attr("data-id");
        if (!functionId) {
          return true;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="highSignalType"]'))) {
          isSubmit = false;
          return false;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="lowSignalType"]'))) {
          isSubmit = false;
          return false;
        }
        var ioSite = $(node).find('input[name="ioSite"]').val();
        var id = $(node).find('input[name="id"]').val();
        var highSignalType = $(node).find('select[name="highSignalType"]').val();
        var lowSignalType = $(node).find('select[name="lowSignalType"]').val();
        ioVehicleConfigs.push({
          'functionId': functionId,
          'vehicleId': vehicleId,
          'ioSite': ioSite,
          'ioType': 1,
          'id': id,
          'highSignalType': highSignalType,
          'lowSignalType': lowSignalType
        })
        checkArr.push(functionId);
      })
      //IO采集1
      $(profile1Li).each(function (index, node) {
        var functionId = $(node).find(".sensorType").attr("data-id");
        if (!functionId) {
          return true;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="highSignalType"]'))) {
          isSubmit = false;
          return false;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="lowSignalType"]'))) {
          isSubmit = false;
          return false;
        }
        var ioSite = $(node).find('input[name="ioSite"]').val();
        var id = $(node).find('input[name="id"]').val();
        var highSignalType = $(node).find('select[name="highSignalType"]').val();
        var lowSignalType = $(node).find('select[name="lowSignalType"]').val();
        ioVehicleConfigs.push({
          'functionId': functionId,
          'vehicleId': vehicleId,
          'ioSite': ioSite,
          'ioType': 2,
          'id': id,
          'highSignalType': highSignalType,
          'lowSignalType': lowSignalType
        })
        checkArr.push(functionId);
      })
      //IO采集2
      $(profile2Li).each(function (index, node) {
        var functionId = $(node).find(".sensorType").attr("data-id");
        if (!functionId) {
          return true;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="highSignalType"]'))) {
          return false;
        }
        if (!ioEditParSetting.checkStatus($(node).find('select[name="lowSignalType"]'))) {
          return false;
        }
        var ioSite = $(node).find('input[name="ioSite"]').val();
        var id = $(node).find('input[name="id"]').val();
        var highSignalType = $(node).find('select[name="highSignalType"]').val();
        var lowSignalType = $(node).find('select[name="lowSignalType"]').val();
        ioVehicleConfigs.push({
          'functionId': functionId,
          'vehicleId': vehicleId,
          'ioSite': ioSite,
          'ioType': 3,
          'id': id,
          'highSignalType': highSignalType,
          'lowSignalType': lowSignalType
        })
        checkArr.push(functionId);
      })
      if (!isSubmit) {
        return;
      }
      for (var i = 0; i < checkArr.length - 1; i++) {
        var pre = checkArr[i];
        for (var j = i + 1; j < checkArr.length; j++) {
          var next = checkArr[j];
          if (pre == next) {
            layer.msg("存在相同的信号位通道，请检查", {move: false});
            return;
          }
        }
      }
      $("#ioVehicleConfigStr").val(JSON.stringify(ioVehicleConfigs));
      $("#delIds").val(delIds.join(','));
      layer.load(2);
      addHashCode($("#bindForm"));
      $("#bindForm").ajaxSubmit(function (data) {
        layer.closeAll();
        data = JSON.parse(data);
        if (data.success) {
          $("#commonWin").modal("hide");
          //关闭弹窗
          myTable.refresh()
        } else {
          layer.msg(data.msg, {move: false});
          return;
        }
      });
    },
    checkStatus: function (node) {
      if ($(node).val() == "") {
        ioEditParSetting.showErrorMsg("请选择状态", $(node));
        return false;
      } else {
        return true;
      }
    },
    checkTypeChange: function (typeid, type) {
      var oneFlage = false;
      var one = $("#" + typeid).attr("data-id");
      if (one != "" && one != undefined) {
        if (temp_signal_ids.indexOf(one) > -1) {
          ioEditParSetting.showErrorMsg(signalChannelExist, typeid);//"sensorType1"
          return false;
        }
        oneFlage = true;
      }
      var otval = $("#" + type).val();
      var oneTypeFlage = true;
      if (otval == 0) {
        oneTypeFlage = false;
      }
      if (oneFlage && !oneTypeFlage) {
        ioEditParSetting.showErrorMsg("请设置为常开/常闭或取消该设置信号口", type);// "oneType"
        return false;
      }
      if (oneTypeFlage && !oneFlage) {
        ioEditParSetting.showErrorMsg(signalChannelSetNull, typeid);
        return false;
      }
      return true;
    },
    showErrorMsg: function (msg, inputId) {
      if ($("#error_label_add").is(":hidden")) {
        $("#error_label_add").text(msg);
        $("#error_label_add").insertAfter($(inputId));
        $("#error_label_add").show();
      } else {
        $("#error_label_add").is(":hidden");
      }
    },
    //错误提示信息隐藏
    hideErrorMsg: function () {
      $("#error_label_add").hide();
    },
    //下发8102
    sendParams: function () {
      // 订阅
      if (editWebSocketFlag) {
        webSocket.subscribe(headers, "/user/topic/realLocationP", ioEditParSetting.setDeviceData, null, null);
        editWebSocketFlag = false;
      }

      var vehicleId = $("#vehicleId").val();
      json_ajax("POST", "/clbs/m/switching/signal/sendPosition", "json", false, {
        "vehicleId": vehicleId
      }, ioEditParSetting.getSendParamCall);
    },
    getSendParamCall: function (data) {
      if (!data.success) {
        layer.msg(data.msg);
      } else {
        var msg = $.parseJSON(data.msg);
        cmsgSN = msg.msgId;
        clearTimeout(clearResPlose);
        clearResPlose = window.setTimeout(function () {
          if (!isResploseState) {
            isResploseState = true;
            layer.closeAll();
            // layer.msg("暂未获取到信号信息!");
          }
        }, 15000);
        isResploseState = false;
      }
    },
    setDeviceData: function (data) {
      $('.col-md-1 div').attr('class', 'signal-high');
      var data = JSON.parse(data.body).data;
      if (data.msgHead.msgId = 513 && cmsgSN == data.msgBody.msgSNAck) {
        isResploseState = true;
        var msgBody = data.msgBody;
        var oilMassData = msgBody.gpsInfo.cirIoCheckData;
        var ioSignalData = msgBody.gpsInfo.ioSignalData;
        if ((oilMassData != null && oilMassData.length > 0) || (ioSignalData != null && ioSignalData.length > 0)) {
          $(ioSignalData).each(function (i, p) {
            if (p.id == 144) {
              var signal0 = p.signal0;
              var signal1 = p.signal1;
              var signal2 = p.signal2;
              var signal3 = p.signal3;
              $("#home1 .widget-todo li").each(function (index, node) {
                if (index == 0) {
                  if (signal0 == 0) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (signal0 == 1) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  } else {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  }
                }
                if (index == 1) {
                  if (signal1 == 0) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (signal1 == 1) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  } else {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  }
                }
                if (index == 2) {
                  if (signal2 == 0) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (signal2 == 1) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  } else {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  }
                }
                if (index == 3) {
                  if (signal3 == 0) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (signal3 == 1) {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  } else {
                    $($(this).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(this).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  }
                }

              })
            }
          })
          $(oilMassData).each(function (i, p) {
            var ioCount = p.ioCount > 32 ? 32 : p.ioCount;
            var statusList = p.statusList[0].ioStatus.toString(2).split('');
            statusList = (statusList.length > 32 ? statusList.slice(0, 32) : statusList).reverse();
            for (var j = 0; j < ioCount; j++) {
              if (p.id == 145) { //IO采集1
                var node = $("#profile1 .widget-todo li")[j];
                if (!node) {
                  $("#profile1 .widget-todo").append(ioEditParSetting.addIoNodeText(j, 2).join(''));
                  node = $("#profile1 .widget-todo li")[j];
                }
                if (statusList.length > j) {
                  if (statusList[j] == 0) {
                    $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (statusList[j] == 1) {
                    $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  }
                } else {
                  $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                  $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                }

              } else if (p.id == 146) { //IO采集2
                var node = $("#profile2 .widget-todo li")[j];
                if (!node) {
                  $("#profile2 .widget-todo").append(ioEditParSetting.addIoNodeText(j, 3).join(''));
                  node = $("#profile2 .widget-todo li")[j];
                }
                if (statusList.length > j) {
                  if (statusList[j] == 0) {
                    $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                    $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                  } else if (statusList[j] == 1) {
                    $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-high');
                    $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-low');
                  }
                } else {
                  $($(node).find('.col-md-1 div')[0]).attr('class', 'signal-low');
                  $($(node).find('.col-md-1 div')[1]).attr('class', 'signal-high');
                }
              }
            }

          })
          ioEditParSetting.setTypeCallback();
        } else {
          layer.msg("暂未获取到信号信息!");
          return;
        }
      }
    },
    //常开 常闭选择
    IOTypeChangeFn: function (node) {
      ioEditParSetting.hideErrorMsg();
      var ztval = $(node).val();
      var name = $(node).attr('name');
      if (name == 'highSignalType' && ztval == 1) {
        $(node).parent().parent().find('select[name="lowSignalType"]').val(2);
        return;
      }
      if (name == 'highSignalType' && ztval == 2) {
        $(node).parent().parent().find('select[name="lowSignalType"]').val(1);
        return;
      }
      if (name == 'lowSignalType' && ztval == 1) {
        $(node).parent().parent().find('select[name="highSignalType"]').val(2);
        return;
      }
      if (name == 'lowSignalType' && ztval == 2) {
        $(node).parent().parent().find('select[name="highSignalType"]').val(1);
        return;
      }
    },
    addIoNode: function (ioType) {
      var ioSites;
      var id;
      if (ioType == 2) {
        ioSites = $("#profile1 .io-site");
        id = 'profile1';
      } else if (ioType == 3) {
        ioSites = $("#profile2 .io-site");
        id = 'profile2';
      }
      if (ioSites.length == 32) {
        layer.msg("最多只能选择32个I/O采集参数");
        return false;
      }
      if (ioSites.length - 1 == $(ioSites[ioSites.length - 1]).val()) {
        $('#' + id + " .widget-todo").append(ioEditParSetting.addIoNodeText(ioSites.length, null, null).join(''))
      } else {
        $(ioSites).each(function (index, node) {
          if (index != $(node).val()) {
            $(node).parents('li').before(ioEditParSetting.addIoNodeText(index, null, null).join(''))
            return false;
          }
        })
      }
      ioEditParSetting.setTypeCallback();
    },
    addIoNodeText: function (index, type, io, flag) {
      var node = new Array();
      node.push('<li>');
      if (flag && io) {
        node.push('<input name="id" value="' + io.id + '" type="hidden">');
      }
      node.push('<input class="io-site" value="' + index + '" name="ioSite" type="hidden">');
      node.push('<div class="form-group">');
      node.push(' <label class="col-md-2 control-label">I/O ' + index + '：<input name="signalOne" type="hidden" ></label>');
      node.push(' <div class="col-md-3">');
      node.push('     <div class="input-group">');
      node.push('         <input type="hidden" name="functionId" value="' + (io ? io.functionId : '') + '"><input type="text" data-id="' + (io ? io.functionId : '') + '"  value="' + (io ? io.name : '') + '"  placeholder="请选择检测功能类型！" class="form-control sensorType" >');
      node.push('         <div class="input-group-btn">');
      node.push('             <button type="button" class="btn btn-white dropdown-toggle" data-toggle="dropdown"><span class="caret"></span></button>');
      node.push('             <ul class="dropdown-menu dropdown-menu-right" role="menu" style="width:100%!important"></ul>');
      node.push('         </div>');
      node.push('     </div>');
      node.push(' </div>');
      node.push(' <label class="col-md-1 control-label"><div class="signal-high"></div></label>');
      node.push(' <div class="col-md-2" style="padding-left: 0px;padding-right: 0px;">');
      node.push('     <select class="form-control" name="highSignalType" onchange="ioEditParSetting.IOTypeChangeFn(this)">');
      if (io) {
        for (var i = 0; i < typeList.length; i++) {
          if (io.functionId == typeList[i].id) {
            node.push('         <option value="">--状态--</option>');
            node.push('         <option value="1" ' + (io.highSignalType == 1 ? 'selected' : '') + '>' + typeList[i].stateOne + '</option>');
            node.push('         <option value="2" ' + (io.highSignalType == 2 ? 'selected' : '') + '>' + typeList[i].stateTwo + '</option>');
            break;
          }
        }
      } else {
        node.push('         <option value="">--状态--</option>');
      }
      node.push('     </select>');
      node.push(' </div>');
      node.push(' <label class="col-md-1 control-label"><div class="signal-high"></div></label>');
      node.push(' <div class="col-md-2" style="padding-left: 0px;padding-right: 0px;">');
      node.push('     <select class="form-control" name="lowSignalType" onchange="ioEditParSetting.IOTypeChangeFn(this)">');
      if (io) {
        for (var i = 0; i < typeList.length; i++) {
          if (io.functionId == typeList[i].id) {
            node.push('         <option value="">--状态--</option>');
            node.push('         <option value="1" ' + (io.lowSignalType == 1 ? 'selected' : '') + '>' + typeList[i].stateOne + '</option>');
            node.push('         <option value="2" ' + (io.lowSignalType == 2 ? 'selected' : '') + '>' + typeList[i].stateTwo + '</option>');
            break;
          }
        }
      } else {
        node.push('         <option value="">--状态--</option>');
      }
      node.push('     </select>');
      node.push(' </div>');
      if ((type != 1 || type == null) && index >= 15) {
        node.push(' <div class="col-md-1">');
        if (index == 15) {
          node.push('     <i class="fa fa-plus plus-io" onclick="ioEditParSetting.addIoNode(' + type + ')"></i>');
        } else {
          node.push('     <i class="fa fa-trash remove-io" onclick="ioEditParSetting.removeIo(null,this);"></i>');
        }
        node.push(' </div>');
      }
      node.push('</div>');
      node.push('</li>');
      return node;
    },
    removeIo: function (node, currentNode) {
      var removeNode;
      if (node != null) {
        removeNode = $(node.target).parents('li');
      } else {
        removeNode = $(currentNode).parents('li');
      }
      var delId = $(removeNode).find('input[name="id"]').val();
      if (delId) {
        delIds.push(delId);
      }
      $(removeNode).remove();
    }
  }
  $(function () {
    ioEditParSetting.init();
    setTimeout(function () {
      $('input').inputClear().on('onClearEvent', function (e, data) {
        var id = data.id;
        if (id == 'sensorType0' || id == 'sensorType1' || id == 'sensorType2' || id == 'sensorType3') {
          $("#" + id).attr("data-id", "");
        };
        if($(this).attr("id") != "referBrands"){
            var node = $(this).parent().parent().parent().find('select');
            $(node).find('option').remove();
            var nodeHtml = '<option value="">--状态--</option>';
            $(node).append(nodeHtml);
        };
      });
    },0);
    //去掉输入框一键删除按钮
    $('input[type="text"]').on('blur', function () {
      var $this = $(this);
      $this.siblings('i.delIcon').remove();
    });
    $("#doSubmits").bind("click", ioEditParSetting.doSubmits);
    $(".remove-io").bind("click", ioEditParSetting.removeIo);
    $("#ioEditParSettingRefresh").bind("click", ioEditParSetting.sendParams);
    ioEditParSetting.sendParams();
  })
})(window, $)