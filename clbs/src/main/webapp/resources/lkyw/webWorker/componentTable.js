importScripts('./pako.min.js');
importScripts('./ungzip.js');

// 单例模式xmlHttpRequest
var Singleton = (function () {
  var instance;

  function createInstance () {
    var object = new XMLHttpRequest();
    return object;
  }

  return {
    getInstance: function () {
      if (!instance) {
        instance = createInstance();
      }
      return instance;
    }
  };
})();

function ajax (url, method, body, callback, errCallback) {
  var xmlHttpRequest = Singleton.getInstance();
  xmlHttpRequest.onload = function (event) {
    if (xmlHttpRequest.readyState === 4 && xmlHttpRequest.status === 200) {
      callback(JSON.parse(xmlHttpRequest.responseText));
    } else {
      errCallback(xmlHttpRequest.response);
    }

  }
  xmlHttpRequest.timeout = 90000;

  xmlHttpRequest.ontimeout = function () {
    callback({ success: false });
  }

  xmlHttpRequest.open(method, url);
  // xmlHttpRequest.setRequestHeader('Accept', 'application/json, text/plain, */*');
  // xmlHttpRequest.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
  xmlHttpRequest.send(body);
}

function postMonitors (cmd, params) {
  const formData = new FormData();
  formData.set('monitorIds', params.monitorIds);
  formData.set('userName', params.userName);

  ajax('/clbs/lkyw/v/monitoring/monitors', 'POST', formData, function (res) {
    if (res.success) {
      const msg = JSON.parse(ungzip(res.msg));
      const obj = msg ? msg : [];
      const statusData = this.getStatusDatas(obj);
      self.postMessage({
        cmd: cmd,
        res: statusData
      });
    } else {
      self.postMessage({
        cmd: 'timeout',
      });
    }
  }, function (res) {
    console.error(res);
  });
}

self.onmessage = function (e) {
  var data = e.data;
  const cmd = data.cmd
  const params = data.params;

  switch (cmd) {
    case 'statusDatas'://全部车辆
    case 'partStatusDatas'://局部车辆
    case 'statusSort'://车辆排序
      postMonitors(cmd, params);
    default:
      break;
  }
}

function getStatusDatas (obj) {
  const statusDatas = [];
  for (let i = 0; i < obj.length; i++) {
    const item = obj[i];
    let row = {};
    const monitorInfo = item.monitorInfo;
    if (monitorInfo) {
      // 对象状态转换
      let objectState;
      if (item.durationTime !== undefined && item.durationTime !== null) {
        objectState = Number(item.gpsSpeed) <= 1
          ? "停止(" + this.formatDuring(item.durationTime) + ")"
          : "行驶(" + this.formatDuring(item.durationTime) + ")"
      } else if (item.gpsSpeed !== undefined) {
        objectState = Number(item.gpsSpeed) <= 1 ? "停止" : "行驶";
      } else {
        objectState = '-';
      }

      // 卫星定位时的信号强度转换
      let signalStrength = (
        item.signalStrength == 'null'
        || item.signalStrength == null
        || item.signalStrength == undefined
        || item.signalStrength == -1
      )
        ? '-'
        : item.signalStrength;
      if (item.locationPattern == 2) {
        signalStrength = this.signalStrength(item);
      }

      // 当日油耗
      var todayFuelConsumption = item.dayOilWear;
      // 修改单位p
      todayFuelConsumption = parseFloat(todayFuelConsumption);
      todayFuelConsumption = todayFuelConsumption.toFixed(2);

      row = {
        sequenceNumber: 0,
        monitorName: this.isEmpty(monitorInfo.monitorName),//监控对象
        plateColorName: this.isEmpty(monitorInfo.plateColorName),//标识颜色
        gpsTime: item.gpsTime,//定位时间
        uploadTime: item.uploadtime,//服务器时间
        assignmentName: this.isEmpty(monitorInfo.assignmentName),//所属分组
        groupName: this.isEmpty(monitorInfo.groupName),//所属企业
        vehicleType: this.isEmpty(monitorInfo.vehicleType),//对象类型
        objectState: objectState,//对象状态item.objectState
        simcardNumber: monitorInfo.simcardNumber,//sim
        professionalsName: this.isEmpty(monitorInfo.professionalsName),//从业人员
        acc: item.acc,//acc
        deviceNumber: monitorInfo.deviceNumber,//终端号
        signalState: item.signalState,//信号状态
        gpsSpeed: item.gpsSpeed !== undefined ? item.gpsSpeed : '-',//速度
        direction: item.direction,//方向
        batteryVoltage: item.batteryVoltage,//电池电压
        elecData: item.batteryVoltage,//电量电压
        signalStrength: signalStrength,//信号强度
        locationType: item.locationPattern,//定位方式
        satellitesNumber: item.satellitesNumber,//卫星颗数
        terminalDayMileage: item.deviceDayMile !== undefined && item.deviceDayMile < 0 ? '-' : this.toFixed(item.deviceDayMile),//终端当日里程
        terminalTotalMileage: this.toFixed(item.gpsMileage),//终端总里程
        gpsOil: item.gpsOil,//CAN油量
        altitude: item.altitude !== undefined ? item.altitude : '-',//高程
        roadLimitSpeed: this.toFixed(item.roadLimitSpeed),//路网限速
        roadType: this.isEmpty(item.roadTypeStr),//道路类型
        longitude: item.longitude,//经度
        latitude: item.latitude,//纬度
        grapherSpeed: this.toFixed(item.grapherSpeed),//记录仪速度
        positionDescription: 0,//位置
        oilExpend: item.oilExpend,//传感器总油耗
        temperatureSensor: item.temperatureSensor,//传感器温度
        temphumiditySensor: item.temphumiditySensor,//传感器湿度
        positiveNegative: item.positiveNegative,//传感器正负转状态
        sensorDayMileage: item.dayMileageSensor < 0 ? '-' : item.dayMileageSensor,//传感器当日里程
        dayOilWear: todayFuelConsumption < 0 ? '-' : todayFuelConsumption,//传感器当日油耗
        sensorTotalMileage: item.mileageSensor && item.mileageSensor.mileage,//传感器总里程
        oilMass: item.oilMass,//传感器液体量
        loadInfos: item.loadInfos,//传感器超负荷重量
        workHourSensor: item.workHourSensor,//传感器工时状态
        terminalIOStatus: item.terminalIOStatus,//终端IO状态
        sensorIOStatus: item.sensorIOStatus,//传感器IO状态
        tyreInfos: item.tyreInfos,//传感器轮胎气压
        driverSpeedLimit: item.driverSpeedLimit,//限速值
        groupId: monitorInfo.groupId, // 组织ID
        assignmentId: monitorInfo.assignmentId, // 分组ID
        deviceType: monitorInfo.deviceType, // 协议类型
        id: monitorInfo.monitorId,//车id
        vehicleId: monitorInfo.monitorId,
      }

      statusDatas.push(row);
    }
  }

  return statusDatas;
}

//当定位方式为“卫星定位”时，信号强度使用 gpsAttachInfoList 中 signalIntensity 字段
function signalStrength (msgBody) {
  let signalStrength = '-';
  if (msgBody.gpsAttachInfoList) {
    for (var i = 0; i < msgBody.gpsAttachInfoList.length; i++) {
      var item = msgBody.gpsAttachInfoList[i];
      if (item.signalIntensity !== undefined) {
        signalStrength = item.signalIntensity;
        break;
      }
    }
  }

  return signalStrength;
}

function formatDuring (mss) {
  var days = parseInt((mss / (1000 * 60 * 60 * 24)).toString());
  var hours = parseInt(((mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toString());
  var minutes = parseInt(((mss % (1000 * 60 * 60)) / (1000 * 60)).toString());
  var seconds = parseInt(((mss % (1000 * 60)) / 1000).toString());
  if (days === 0 && hours === 0 && minutes == 0) {
    return seconds + " 秒 ";
  } else if (days === 0 && hours === 0 && minutes !== 0) {
    return minutes + " 分 " + seconds + " 秒 ";
  } else if (days === 0 && hours !== 0) {
    return hours + " 小时 " + minutes + " 分 " + seconds + " 秒 ";
  } else if (days !== 0) {
    return days + " 天 " + hours + " 小时 " + minutes + " 分 " + seconds + " 秒 ";
  }
}

function toFixed (value, a = 1) {
  if (value || value == 0) {
    return parseFloat(Number(value).toFixed(1));
  }
}

function isEmpty (value, placeholder = '-') {
  if (value == undefined || value == null || value == '') {
    return placeholder;
  }

  return value;
}