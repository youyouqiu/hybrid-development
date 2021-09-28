(function ($, window) {
  isRead = true;
  var _timeout;
  perStateObj = {
    init: function (sensorID) {
      // 请求后台，获取所有订阅的车
      // webSocket.init('/clbs/vehicle');
      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function () {
        var cancelStrS = {
          "desc": {
            "MsgId": 40964,
            "UserName": $("#userName").text()
          },
          "data": params
        };
        webSocket.unsubscribealarm(headers, "/app/vehicle/unsubscribestatus", cancelStrS);
      };
      var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
      if (sensorID == "225") {//ADAS
        webSocket.subscribe(headers, "/user/topic/per61921Info", function (data) {
          perStateObj.getSensor0104Param(data, sensorID);
        }, null, null);
      } else {//DSM
        webSocket.subscribe(headers, "/user/topic/per61922Info", function (data) {
          perStateObj.getSensor0104Param(data, sensorID);
        }, null, null);
      }
      var vid = $("#vehicleId").val();
      json_ajax("POST", url, "json", false, {
        "vid": vid,
        "sensorID": sensorID,
        "commandType": 241,
      }, perStateObj.getF3BaseParamCall);
    },
    //处理平台设置参数-初始化
    clearInputTextValue: function (data) {
      for (var i = 0; i < data.length; i++) {
        var id = "#" + data[i];
        $(id).val("");
      }
    },
    //外设状态-下发获取外设状态返回处理方法
    getF3BaseParamCall: function (data) {
      if (!data.success) {
        if (data.msg) {
          layer.msg(data.msg);
        }
      } else {
        layer_time = setTimeout(function () {
          if (isRead) {
            layer.load(2);
          }
        }, 0);
        perStateObj.createSocket0104InfoMonitor(data.msg);
      }
    },
    //外设状态-刷新
    readInformationRefreshClick: function () {
      isRead = true;
      var url = '/clbs/r/riskManagement/DefineSettings/getPeripheralInfo';
      var vid = $("#vehicleId").val();
      json_ajax("POST", url, "json", false, {//ADAS
        "vid": vid,
        "sensorID": 225,
        "commandType": 241
      }, perStateObj.getF3BaseParamCall);
      json_ajax("POST", url, "json", false, {//DSM
        "vid": vid,
        "sensorID": 226,
        "commandType": 241
      }, perStateObj.getF3BaseParamCall);
    },
    //创建消息监听
    createSocket0104InfoMonitor: function (msg) {
      var msg = $.parseJSON(msg);
      temp_send_vehicle_msg_id = msg.msgId;
      headers = {"UserName": msg.userName};
      clearTimeout(_timeout);
      _timeout = setTimeout(function () {
        if (isRead) {
          isRead = false;
          clearTimeout(layer_time);
          layer.closeAll();
          layer.msg("获取设备数据失败!");
        }
      }, 60000);
    },
    //处理获取设备上传数据
    getSensor0104Param: function (msg, sensorID) {
      if (msg == null)
        return;
      var result = $.parseJSON(msg.body);
      var msgSNAck = result.data.msgBody.msgSNAck;
      /*if (msgSNAck != temp_send_vehicle_msg_id) {
          return;
      }*/
      isRead = false;
      clearTimeout(_timeout);
      layer.closeAll();
      if (layer_time) {
        clearTimeout(layer_time);
      }
      var status = result.data.msgBody.result;
      if (status == 1) {
        layer.msg("获取设备数据失败!");
        return;
      }
      var id = result.data.msgBody.params[0].id;
      if (id == "61921" || id == "61922") {//外设状态
        perStateObj.queryF3BaseParamCall(result, sensorID);
        return;
      }
    },
    //外设状态-上报获取外设状态返回处理方法
    queryF3BaseParamCall: function (data, sensorID) {
      var value = data.data.msgBody.params[0].value.sensorStatus;
      var workStatus = value.workStatus;//工作状态
      var alarmStatus = value.alarmStatus.toString(2);//报警状态
      alarmStatus = (Array(32).join(0) + alarmStatus).slice(-32);//高位补零

      var cameraState = alarmStatus.substr(-1, 1);//摄像头状态
      var mainMemory = alarmStatus.substr(-2, 1);//主存储器状态
      var auxiliaryStorage = alarmStatus.substr(-3, 1);//辅存储器状态
      var infraredFinish = alarmStatus.substr(-4, 1);//红外线补光状态
      var speakerState = alarmStatus.substr(-5, 1);//扬声器状态
      var battery = alarmStatus.substr(-6, 1);//电池状态
      var communicationModule = alarmStatus.substr(-11, 1);//通讯模块状态
      var positioningModule = alarmStatus.substr(-12, 1);//定位模块状态
      var stateArr = [{
        'name': 'cameraState',
        'data': cameraState
      }, {
        'name': 'mainMemory',
        'data': mainMemory
      }, {
        'name': 'auxiliaryStorage',
        'data': auxiliaryStorage
      }, {
        'name': 'infraredFinish',
        'data': infraredFinish
      }, {
        'name': 'speakerState',
        'data': speakerState
      }, {
        'name': 'battery',
        'data': battery
      }, {
        'name': 'communicationModule',
        'data': communicationModule
      }, {
        'name': 'positioningModule',
        'data': positioningModule
      }];
      switch (workStatus) {
        case 0x01:
          workStatus = '正常工作';
          break;
        case 0x02:
          workStatus = '待机状态';
          break;
        case 0x03:
          workStatus = '升级维护';
          break;
        case 0x04:
          workStatus = '设备异常';
          break;
        case 0x05:
          workStatus = '断开连接';
          break;
        default:
          workStatus = '';
          break;
      }
      var curTarget = '';
      if (sensorID == '225') {//ADAS
        curTarget = 'adas';
        $("#adas-workState").val(workStatus);
      } else {//DSM
        curTarget = 'dsm';
        $("#dsm-workState").val(workStatus);
      }
      var len = stateArr.length;
      for (var i = 0; i < len; i++) {
        if (stateArr[i].data == '0') {
          $("#" + curTarget + '-' + stateArr[i].name).val('正常');
        } else {
          $("#" + curTarget + '-' + stateArr[i].name).val('异常');
        }
      }
    },
  }
  $(function () {
    perStateObj.init(225);//ADAS
    perStateObj.init(226);//DSM
    //刷新
    $("#readPerStateRefresh").on("click", perStateObj.readInformationRefreshClick);
  })
})($, window)