var DispatchAmap = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.options = options;
  this.aMap = null;
  this.mouseTool = null;
  this.infoWindow = null;
  this.geocoder = null;
  this.realTimeTraffic = null;
  this.googleMapLayer = null;
  this.mass = null;
  this.init();
  this.initHandler();
};

DispatchAmap.prototype = {
  constructor: DispatchAmap,
  // 初始化方法
  init: function () {
    var $this = this;
    var options = $this.options;
    var elementId = options.elementId;
    $this.aMap = new AMap.Map(elementId, {
      resizeEnable: true,
      zoom: 4
    });
    $this.mouseTool = new AMap.MouseTool($this.aMap);
    $this.mouseTool.on('draw', $this.createSuccess.bind($this));
    $this.loadMassMarks();
    $this.infoWindow = new AMap.InfoWindow();
    $this.geocoder = new AMap.Geocoder();
    /**
     * 路况
     * @type {AMap.TileLayer.Traffic}
     */
    $this.realTimeTraffic = new AMap.TileLayer.Traffic({
      zIndex: 1000
    });
    $this.realTimeTraffic.setMap($this.aMap);
    $this.realTimeTraffic.hide();

    /**
     * 报警声音文件
     */
    if (navigator.userAgent.indexOf('MSIE') >= 0) {
      $('#alarmMsgBox').html('<embed id="IEalarmMsg" src="/clbs/file/music/alarm.wav" autostart="false"/>');
    } else {
      $('#alarmMsgBox').html('<audio id="alarmMsgAutoOff" src="/clbs/file/music/alarm.wav"></audio>');
    }
    $this.initUserFence();
  },
  initHandler: function () {
    var $this = this;
    $('#toolClick').on('click', $this.toolClickHandler.bind($this));
    $('#magnifyClick').on('click', $this.magnifyClickHandler.bind($this));
    $('#shrinkClick').on('click', $this.shrinkClickHandler.bind($this));
    $('#countClick').on('click', $this.countClickHandler.bind($this));
    $('#mapDropSetting').on('click', $this.mapDropSettingHandler.bind($this));
    $('#realTimeRC').on('click', $this.realTimeRCHandler.bind($this));
    $('#googleGeograpyMap').on('click', $this.googleGeograpyMapHandler.bind($this));
    $('#googleSatelliteMap').on('click', $this.googleSatelliteMapHandler.bind($this));
    $('#amapMap').on('click', $this.amapMapHandler.bind($this));
    $('#showAlarmWin').on('click', $this.showAlarmWindow.bind($this));
    $('#showAlarmWinMark').on('click', $this.showAlarmWinMarkRight.bind($this));
    //屏蔽浏览器右键菜单
    $('.contextMenuContent,#showAlarmWin').bind('contextmenu', function (e) {
      return false;
    });
    $('#showAlarmWinMark').contextmenu();
    //最小化
    $('.alarmSettingsSmall').bind('click', $this.alarmToolMinimize.bind($this));
    //关闭声音
    $('.alarmSound').bind('click', $this.alarmOffSound.bind($this));
    //关闭闪烁
    $('.alarmFlashes').bind('click', $this.alarmOffFlashes.bind($this));
  },
  /**
   * 地图工具，画圆
   */
  createCircular: function () {
    this.mouseTool.circle();
  },
  /**
   * 圆形绘制成功
   */
  createSuccess: function (data) {
    var $this = this;
    var type = data.obj['CLASS_NAME'];
    if (type == 'AMap.Circle') {
      $this.mouseTool.close(false);
      var params = {
        center: data.obj.getCenter(),
        radius: data.obj.getRadius()
      };
      $this._dispatchModule.get('data').setTaskGroupDrawCircleData(params);
    } else if (type == 'AMap.Polygon') {
      $this.mouseTool.close(false);
      var params = {
        bounds: data.obj.getBounds()
      };
      $this._dispatchModule.get('data').setTaskGroupDrawRectangleData(params);
    }
  },
  /**
   * 地图工具，画矩形
   */
  createRectangular: function (event) {
    this.mouseTool.rectangle();
  },
  /**
   * 清楚鼠标画图
   */
  clearMouseTool: function () {
    this.mouseTool.close(true);
  },
  /**
   * 加载海量点
   */
  loadMassMarks: function () {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getMassPoint(
      function (data) {
        if (data.success) {
          var obj = JSON.parse(ungzip(data.obj));
          var style = [];
          obj.sortIcon.map(function (item) {
            style.push({
              // url: '../../../resources/img/job/' + item,
              url: '../../../resources/img/dispatchLocation.png',
              anchor: new AMap.Pixel(10, 10),
              size: new AMap.Size(30, 35)
            });
          });
          var massData = [];
          obj.data.map(function (item) {
            massData.push({
              lnglat: [item.longitude, item.latitude],
              style: item.jobIcon,
              monitorId: item.monitorId,
              userId: item.userId
            });
          });
          // 清空海量点
          if ($this.mass !== null) {
            $this.mass.clear();
            $this.mass = null;
          }
          // 创建海量点
          $this.mass = new AMap.MassMarks(massData, {
            opacity: 0.8,
            zIndex: 111,
            cursor: 'pointer',
            style: style
          });
          $this.mass.on('click', $this.massMarksClickHandler.bind($this));
          $this.mass.setMap($this.aMap);
          setTimeout(function () {
            $this.loadMassMarks();
          }, 30000);
          // $this.mass.on('complete', $this.massMarksLoadSuccess.bind($this));
        }
      }
    );
  },
  /**
   * 海量点加载成功事件
   */
  // massMarksLoadSuccess: function () {
  //   var $this = this;
  //   setTimeout(function () {
  //     $this.loadMassMarks();
  //   }, 30000);
  // },
  /**
   * 海量点标注单击事件
   */
  massMarksClickHandler: function (e) {
    var id = e.data.userId;
    this.getMapPonitInfo(id);
  },
  /**
   * 点击监控对象地图海量点弹框信息
   */
  getMapPonitInfo: function (id) {
    var $this = this;
    $this._dispatchModule.get('dispatchServices').getMapPointInfo(
      {userID: id},
      $this.mapPointInfoCallback.bind($this)
    );
  },
  /**
   * 地图海量点弹框信息获取成功事件
   */
  mapPointInfoCallback: function (data) {
    var $this = this;
    if (data.success) {
      var obj = data.obj;
      if (obj.longitude !== null && obj.latitude !== null) {
        $this.aMap.setZoom(18);
        $this.aMap.setCenter(new AMap.LngLat(obj.longitude, obj.latitude));

        var html = '<div class="info-window">' +
          '<ul>' +
          '<li>' + obj.monitorName + '</li>' +
          '<li class="info-table">' +
          '<div class="info-row"><div class="info-cell">时间：</div><div class="info-cell">' + obj.gpsTime + '</div></div>' +
          '<div class="info-row"><div class="info-cell">位置：</div><div class="info-cell">' + obj.address;

        if (obj.hasTrackPlaybackPermissions) {
          html += '<i id="goTrackPlayback">' +
            '<a href="/clbs/v/monitoring/trackPlayback" class="go-track-playback"></a>' +
            '</i>';
        }

        html += '</div></div></li></ul></div>';

        $this.infoWindow.setContent(html);
        $this.infoWindow.open($this.aMap, new AMap.LngLat(obj.longitude, obj.latitude));
        // $('#goTrackPlayback').on('click', $this.goTrackPlaybackHandler);
      }
    }
  },
  /**
   * 逆地理解析
   */
  getAddress: function (lnglat, callback) {
    this.geocoder.getAddress(lnglat, function (status, result) {
      if (status === 'complete' && result.regeocode) {
        var address = result.regeocode.formattedAddress;
        callback(address);
      } else {
        callback('未定位');
      }
    });
  },
  /**
   * 工具栏点击事件
   */
  toolClickHandler: function () {
    if ($('#toolOperateClick').hasClass('active')) {
      $('#toolOperateClick').removeClass('active');
      $('#mapDropSettingMenu').hide();
      $('#mapDropSetting').removeClass('active');
    } else {
      $('#toolOperateClick').addClass('active');
    }
  },
  /**
   * 拉框放大
   */
  magnifyClickHandler: function () {
    var $this = this;
    $('#shrinkClick,#countClick').removeClass('active');
    if (!$('#magnifyClick').hasClass('active')) {
      $('#magnifyClick').addClass('active');
      $this.mouseTool.rectZoomIn();
    } else {
      $('#magnifyClick').removeClass('active');
      $this.mouseTool.close();
    }
  },
  /**
   * 拉框放小
   */
  shrinkClickHandler: function () {
    var $this = this;
    $('#magnifyClick, #countClick').removeClass('active');
    if (!$('#shrinkClick').hasClass('active')) {
      $('#shrinkClick').addClass('active');
      $this.mouseTool.rectZoomOut();
    } else {
      $('#shrinkClick').removeClass('active');
      $this.mouseTool.close();
    }
  },
  /**
   * 距离量算
   */
  countClickHandler: function () {
    var $this = this;
    $('#shrinkClick, #magnifyClick').removeClass('active');
    if (!$('#countClick').hasClass('active')) {
      $('#countClick').addClass('active');
      $this.mouseTool.rule();
    } else {
      $('#countClick').removeClass('active');
      $this.mouseTool.close(true);
    }
  },
  /**
   * 地图设置视图显示
   */
  mapDropSettingHandler: function () {
    if ($('#mapDropSetting').hasClass('active')) {
      $('#mapDropSettingMenu').slideUp();
      $('#mapDropSetting').removeClass('active');
    } else {
      $('#mapDropSettingMenu').slideDown();
      $('#mapDropSetting').addClass('active');
    }
  },
  /**
   * 路况开启
   */
  realTimeRCHandler: function (e) {
    if ($('#realTimeRC').is(':checked')) {
      $('#realTimeRCLab').addClass('active');
      this.realTimeTraffic.show();
    } else {
      $('#realTimeRCLab').removeClass('active');
      this.realTimeTraffic.hide();
    }
  },
  /**
   * 谷歌地形图
   */
  googleGeograpyMapHandler: function () {
    var $this = this;
    $('#googleSatelliteMapLab, #amapMapLab').removeClass('active');
    $('#googleGeograpyMapLab').addClass('active');
    if ($this.googleMapLayer) {
      $this.googleMapLayer.setMap(null);
    }
    var url = 'https://mt{1,2,3,0}.google.cn/maps/vt?lyrs=p@194&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]';
    $this.googleMapLayer = new AMap.TileLayer({
      tileUrl: url,
      zIndex: 100
    });
    $this.googleMapLayer.setMap($this.aMap);
  },
  /**
   * 谷歌卫星图
   */
  googleSatelliteMapHandler: function () {
    var $this = this;
    $('#googleGeograpyMapLab, #amapMapLab').removeClass('active');
    $('#googleSatelliteMapLab').addClass('active');
    if ($this.googleMapLayer) {
      $this.googleMapLayer.setMap(null);
    }
    var url = 'https://mt{1,2,3,0}.google.cn/vt/?lyrs=y&hl=zh-CN&gl=cn&x=[x]&y=[y]&z=[z]&s=Galile';
    $this.googleMapLayer = new AMap.TileLayer({
      tileUrl: url,
      zIndex: 100
    });
    $this.googleMapLayer.setMap($this.aMap);
  },
  /**
   * 显示高德地图
   */
  amapMapHandler: function () {
    var $this = this;
    $('#googleSatelliteMapLab, #googleGeograpyMapLab, #amapMapLab').removeClass('active');
    $('#amapMapLab').addClass('active');
    if ($this.googleMapLayer) {
      $this.googleMapLayer.setMap(null);
    }
  },
  /**
   * 报警提示相关方法
   * */
  //报警信息(数量显示  声音  闪烁)
  realTimeAlarmInfoCalcFn: function () {
    var alarmLength = $('#alarmTab table tbody tr').length;
    var alarmNum = alarmLength;
    $('#showAlarmNum').text(alarmNum);
    var $alarmSoundSpan = $('.alarmSound span');
    var $alarmFlashesSpan = $('.alarmFlashes span');
    var $showAlarmWinMark = $('#showAlarmWinMark');
    if (alarmNum > 0) {
      //声音
      if (navigator.userAgent.indexOf('MSIE') >= 0) {
        if ($alarmSoundSpan.hasClass('soundOpen')) {
          // $alarmMsgBox.html('<embed id="IEalarmMsg" src="../../file/music/alarm.wav" autostart="true"/>');
          document.querySelector('#IEalarmMsg').play();
        } else {
          // $alarmMsgBox.html('<embed id="IEalarmMsg" src=""/>');
        }
      } else {
        if ($alarmSoundSpan.hasClass('soundOpen')) {
          // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav" autoplay="autoplay"></audio>');
          document.querySelector('#alarmMsgAutoOff').play();
        } else {
          // $alarmMsgBox.html('<audio id="alarmMsgAutoOff" src="../../file/music/alarm.wav"></audio>');
        }
      }
      //闪烁
      if ($alarmFlashesSpan.hasClass('flashesOpen')) {
        $showAlarmWinMark.css('background-position', '0px -134px');
        setTimeout(function () {
          $showAlarmWinMark.css('background-position', '0px 0px');
        }, 1500);
      } else {
        $showAlarmWinMark.css('background-position', '0px 0px');
      }
      this.showAlarmWindow();
    }
  },
  showAlarmWindow: function () {
    $('#showAlarmWinMark').show();
    $('#showAlarmWin').hide();
    var $showAlarmNum = $('#showAlarmNum');
    if ($showAlarmNum.text() == '0') {
      $showAlarmNum.hide();
    } else {
      $showAlarmNum.show();
    }
  },
  // 报警信息表格联动
  showAlarmWinMarkRight: function () {
    $(this).css('background-position', '0px -67px');
    setTimeout(function () {
      $('#showAlarmWinMark').css('background-position', '0px 0px');
    }, 100);
    $('#alarmLiTab').addClass('active').siblings().removeClass('active');
    $('#alarmTab').addClass('active').siblings().removeClass('active');
    if (!$('#tableAllow').hasClass('active')) {
      $('#tableAllow').click();
    }
  },
  alarmToolMinimize: function () {
    $('#context-menu').removeAttr('class');
    $('#showAlarmWin').show();
    $('#showAlarmWinMark').hide();
  },
  //开启关闭声音
  alarmOffSound: function () {
    var $alarmSoundSpan = $('.alarmSound span');
    var $alarmSoundFont = $('.alarmSound font');
    var $alarmMsgAutoOff = $('#alarmMsgAutoOff');
    var alarmNum = $('#alarmTab table tbody tr').length;
    if (navigator.userAgent.indexOf('MSIE') >= 0) {
      //IE浏览器
      if ($alarmSoundSpan.hasClass('soundOpen')) {
        $alarmSoundSpan.addClass('soundOpen-off');
        $alarmSoundSpan.removeClass('soundOpen');
        $alarmSoundFont.css('color', '#a8a8a8');
      } else {
        $alarmSoundSpan.removeClass('soundOpen-off');
        $alarmSoundSpan.addClass('soundOpen');
        $alarmSoundFont.css('color', '#fff');
        document.querySelector('#IEalarmMsg').play();
      }
    } else {
      //其他浏览器
      if ($alarmSoundSpan.hasClass('soundOpen')) {
        $alarmSoundSpan.addClass('soundOpen-off');
        $alarmSoundSpan.removeClass('soundOpen');
        $alarmSoundFont.css('color', '#a8a8a8');
        if (alarmNum > 0) {
          $('#alarmMsgAutoOff')[0].pause();
        }
        $alarmMsgAutoOff.removeAttr('autoplay');
      } else {
        $alarmSoundSpan.removeClass('soundOpen-off');
        $alarmSoundSpan.addClass('soundOpen');
        $alarmSoundFont.css('color', '#fff');
        if (alarmNum > 0) {
          $('#alarmMsgAutoOff')[0].play();
        }
      }
    }
  },
  //开启关闭闪烁
  alarmOffFlashes: function () {
    var $alarmFlashesSpan = $('.alarmFlashes span');
    var $alarmFlashesFont = $('.alarmFlashes font');
    var alarmNum = $('#alarmTab table tbody tr').length;
    if ($alarmFlashesSpan.hasClass('flashesOpen')) {
      $alarmFlashesSpan.addClass('flashesOpen-off');
      $alarmFlashesSpan.removeClass('flashesOpen');
      $alarmFlashesFont.css('color', '#a8a8a8');
      $('#showAlarmWinMark').css('background-position', '0px 0px');
    } else {
      $alarmFlashesSpan.removeClass('flashesOpen-off');
      $alarmFlashesSpan.addClass('flashesOpen');
      $alarmFlashesFont.css('color', '#fff');
      if (alarmNum > 0) {
        $('#showAlarmWinMark').css('background-position', '0px -134px');
        setTimeout(function () {
          $('#showAlarmWinMark').css('background-position', '0px 0px');
        }, 1500);
      } else {
        $('#showAlarmWinMark').css('background-position', '0px 0px');
      }
    }
  },

  // 跳转到轨迹回放
  goTrackPlaybackHandler: function () {
    location.href = '/clbs/v/monitoring/trackPlayback';
  },
  // 获取围栏覆盖物数据
  initUserFence: function () {
    var $this = this;
    this._dispatchModule.get('dispatchServices').getFenceData(function (r) {
      if (r.success) {
        var dataList = r.obj;
        if (dataList != null && dataList.length > 0) {
          for (var i = 0; i < dataList.length; i++) {
            var fenceData = dataList[i].fenceData;
            var fenceType = dataList[i].fenceType;

            if (fenceType == 'zw_m_marker') { // 标注
              $this.drawMark(fenceData);
            } else if (fenceType == 'zw_m_line') { // 线
              $this.drawLine(fenceData);
            } else if (fenceType == 'zw_m_polygon') { // 多边形
              $this.drawPolygon(fenceData);
            } else if (fenceType == 'zw_m_circle') { // 圆形
              $this.drawCircle(fenceData);
            }
          }
        }
      } else {
        layer.msg('请求围栏数据出错：' + r.exceptionDetailMsg);
      }
    });
  },
  // 画标注物
  drawMark: function (data) {
    var $this = this;
    var latLng = [data.longitude, data.latitude];

    var marker = new AMap.Marker({
      position: latLng,
      offset: new AMap.Pixel(-9, -23)
    });
    marker.setMap($this.aMap);
  },
  // 画线
  drawLine: function (data) {
    var $this = this;
    var dataArr = new Array();
    if (data != null && data.length > 0) {
      for (var i in data) {
        if (data[i].type == '0') {
          dataArr[i] = [data[i].longitude, data[i].latitude];
        }
      }
      $.each(dataArr, function (index, item) {
        if (item == undefined) {
          dataArr.splice(index, 1);
        }
      });
    }
    var line = new AMap.Polyline({
      path: dataArr, //设置线覆盖物路径
      strokeColor: '#' + data[0].colorCode, //线颜色
      strokeOpacity: data[0].transparency / 100, //线透明度
      strokeWeight: 5, //线宽
      strokeStyle: 'solid', //线样式
      strokeDasharray: [10, 5],
      zIndex: 51
    });

    line.setMap($this.aMap);
  },
  // 画多边形
  drawPolygon: function (data) {
    var $this = this;
    var dataArr = new Array();
    if (data != null && data.length > 0) {
      for (var i = 0; i < data.length; i++) {
        dataArr.push([data[i].longitude, data[i].latitude]);
      }
    }

    var polygon = new AMap.Polygon({
      path: dataArr,//设置多边形边界路径
      strokeWeight: 3, //线宽
      fillColor: '#' + data[0].colorCode, //填充色
      fillOpacity: data[0].transparency / 100
    });

    polygon.setMap($this.aMap);
  },
  // 画圆形
  drawCircle: function (data) {
    var $this = this;
    var circle = new AMap.Circle({
      center: new AMap.LngLat(data.longitude, data.latitude),// 圆心位置
      radius: data.radius, //半径
      strokeWeight: 3, //线粗细度
      fillColor: '#' + data.colorCode, //填充颜色
      fillOpacity: data.transparency / 100
      //填充透明度
    });

    circle.setMap($this.aMap);
  }
};

$(function () {
  if (navigator.userAgent.indexOf('MSIE') >= 0) {
    $("#alarmMsgBox").html('<embed id="IEalarmMsg" src="../../../file/music/alarm.wav" autostart="false"/>');
  } else {
    // 如果移动了js的位置需要注意路径问题
    $("#alarmMsgBox").html('<audio id="alarmMsgAutoOff" src="../../../file/music/alarm.wav"></audio>');
  }
});