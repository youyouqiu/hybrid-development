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

function ajax (url, method, body, requestType, callback, errCallback) {
  var xmlHttpRequest = Singleton.getInstance();
  xmlHttpRequest.onload = function (event) {
    if (xmlHttpRequest.readyState === 4 && xmlHttpRequest.status === 200) {
      callback(JSON.parse(xmlHttpRequest.responseText));
    } else {
      errCallback(xmlHttpRequest.response);
    }

  }

  xmlHttpRequest.open(method, url);
  addHeader(xmlHttpRequest, requestType);
  xmlHttpRequest.send(body);
}

function addHeader (xmlHttpRequest, type) {
  // formData 什么头也不用添加
  if (!type) {
    type = 'json';
  }
  if (type === 'json') {
    xmlHttpRequest.setRequestHeader('Accept', 'application/json, text/plain, */*');
    xmlHttpRequest.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
  }
}

/**
 * 
 * @param {*} lnglatArray 可视范围边界经纬度，【东北经度，东北维度，西南经度，西南维度】
 */
let timer = null;
function getVehicles (lnglatArray) {
  var monitorIds = null;
  var postType = 'json';
  if (lnglatArray) {
    var visibleIds = '';
    for (var i = 0; i < globalPoints.length; i++) {
      const point = globalPoints[i];
      if (point.longitude <= lnglatArray[0] && point && point.longitude >= lnglatArray[2] &&
        point.latitude <= lnglatArray[1] && point && point.latitude >= lnglatArray[3]
      ) {
        visibleIds += point.vehicleId + ',';
      }
    }
    if (visibleIds.length > 0) {
      monitorIds = new FormData();
      monitorIds.set('monitorIds', visibleIds);
      postType = 'formData';
    }
  }
  ajax('/clbs/lkyw/v/monitoring/getVehiclePositional', 'POST', monitorIds, postType, function (res) {
    if (res.success) {
      var result = analyzeVehicles(res.obj);
      self.postMessage({
        cmd: 'updateVehicle',
        data: result
      });
      if (timer) {
        clearTimeout(timer);
        timer = null;
      }
      timer = setTimeout(getMapBounds, 30 * 1000);
    } else {
      console.error(res);
    }
  }, function (res) {
    console.error(res);
  });
}

/**
 * 向主线程发起请求，询问地图的边界，用于过滤可视范围内的监控对象ID，
 * 随后用这些id向服务器请求最新的位置数据
 * 并替换 globalPoints 中的点
 */
function getMapBounds () {
  self.postMessage({
    cmd: 'getMapBounds',
  });
}

/**
 * 存储服务器返回的点
 */
var globalPoints = null;
/**
 * 分析服务器返回数据，得出样式数组
 * @param {*} mapAllPoints 服务器返回的点
 */
function analyzeVehicles (mapAllPoints) {
  // 计算styleObject 和 data
  const data = [];
  const styleObject = {};
  const styleArray = [];

  if (mapAllPoints) {
    globalPoints = mapAllPoints;
    for (let i = 0, len = mapAllPoints.length; i < len; i++) {
      const point = mapAllPoints[i];

      const key = point.vehicleIcon + '-' + point.direction.toString();
      const icon = point.vehicleIcon ? point.vehicleIcon : 'v_21.png';
      let styleIndex;
      if (!styleObject[key]) {
        styleIndex = styleArray.length
        styleObject[key] = styleIndex;
        styleArray.push({
          // anchor: new AMap.Pixel(21, 14),
          url: '/clbs/resources/img/vico/' + icon,
          // size: new AMap.Size(50, 28),
          icon: icon,
          rotation: point.direction + 270
        });
      } else {
        styleIndex = styleObject[key];
      }
      data.push({
        lnglat: [point.longitude, point.latitude],
        style: styleIndex,
        extra: point,
      });
    }
  }
  return {
    mapAllPoints,
    data: data,
    styleArray: styleArray
  }
}

self.onmessage = function (e) {
  var data = e.data;
  var cmd = data.cmd;
  switch (cmd) {
    case 'getVehicle':
      getVehicles(data.data);
      break;

    default:
      break;
  }
}