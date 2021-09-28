var DispatchTable = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.tableSerialnumber = 1;
  this.init();
};

DispatchTable.prototype = {
  /**
   * 初始化数据
   */
  init: function () {
    var $this = this;
    $this.alarmInfoSubscribe();
    $('#tableAllow').on('click', $this.tableAllowHandler.bind($this));
    $('#submitAlarmHandle').on('click', $this.submitAlarmHandle.bind($this));
    // $('#dealWithAlarm').on('click', $this.dealWithAlarmHandler.bind($this));
  },
  /**
   * table表展示和隐藏
   */
  tableAllowHandler: function () {
    var $tableList = $('#tableList');
    if ($tableList.hasClass('active')) {
      $tableList.removeClass('active');
      $('#tableAllow').removeClass('active');
    } else {
      $tableList.addClass('active');
      $('#tableAllow').addClass('active');
    }
  },
  /**
   * 报警处理弹窗显示
   */
  dealWithAlarmHandler: function (e) {
    var monitorId = $(e.target).attr('data-id');
    var monitorName = $(e.target).attr('data-monitorName');
    var alarmType = $(e.target).attr('data-alarmType');
    var time = $(e.target).attr('data-time');
    var trId = $(e.target).attr('data-trId');
    $('#submitAlarmHandle').attr('data-trId', trId).attr('data-id', monitorId).attr('data-monitorName', monitorName).attr('data-alarmType', alarmType).attr('data-time', time);
    $('#sendTextContent').val('');
    $('#tempMsg').hide();
    $('#submitAlarmHandle').prop('disabled', false);

    if (alarmType.split(',').length > 1) {
      $('#submitAlarmHandle').prop('disabled', true);
      $('#tempMsg').show();
    }

    $('#dealWithAlarmModal').modal('show');
  },
  /**
   * 报警信息订阅
   */
  alarmInfoSubscribe: function () {
    var $this = this;
    setTimeout(function () {
      if (webSocket.conFlag) {
        webSocket.subscribe(
          headers,
          '/user/' + $('#userName').text() + '/sosAlarm',
          $this.updataAlarmMessage.bind($this),
          '/app/vehicle/subscribeStatus',
          null
        );
      } else {
        $this.alarmInfoSubscribe();
      }
    }, 1500);
  },
  /**
   * 报警信息更新
   * @param data
   */
  updataAlarmMessage: function (data) {
    var $this = this;
    var body = JSON.parse(data.body);
    var msgBody = body.data.msgBody;
    // var time = $this.timeChange(msgBody.alarmStartTime);
    var time = parseDate2Str(msgBody.gpsTime);
    var alarmTypeList = msgBody.globalAlarmSet.split(',');
    var alarmStartTime = time;
    if (alarmTypeList.length === 1) {
      if (alarmTypeList.indexOf('0') !== -1) {
        alarmStartTime = time;
      } else {
        alarmStartTime = formatDateAll(msgBody.dispatchAlarmStartTime);
      }
    }
    var tableData = {
      id: msgBody.monitorInfo.monitorId + msgBody.gpsTime,
      serialNumber: $this.tableSerialnumber,
      monitorName: msgBody.monitorInfo.monitorName,
      alarmName: msgBody.alarmName,
      alarmStartTime: alarmStartTime,// msgBody.alarmStartTime,
      monitorId: msgBody.monitorInfo.monitorId,
      alarmType: msgBody.globalAlarmSet,
      latitude: msgBody.latitude,
      longitude: msgBody.longitude
    };
    $this.tableSerialnumber += 1;
    $('#alarmIcon').addClass('active');
    setTimeout(function () {
      $('#alarmIcon').removeClass('active');
    }, 1000);
    $this.setAlarmMessageToTable(tableData);
  },
  /**
   * 时间格式转换
   */
  timeChange: function (time) {
    var date = new Date(time);
    return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
  },
  /**
   * 报警信息添加到table表
   */
  setAlarmMessageToTable: function (data) {
    var tr = '<tr id="' + data.id + '">'
      + '<td>' + data.serialNumber + '</td>'
      + '<td>' + data.monitorName + '</td>'
      + '<td>' + data.alarmName + '</td>'
      + '<td>' + data.alarmStartTime + '</td>'
      + '<td><span data-trId="' + data.id + '" class="address-info" data-latitude="' + data.latitude + '" data-longitude="' + data.longitude + '">点击查看位置信息</span></td>'
      + '<td>'
      + '<button data-trId="' + data.id + '" data-alarmType="' + data.alarmType + '" data-time="' + data.alarmStartTime + '" data-monitorName="' + data.monitorName + '" data-id="' + data.monitorId + '" class="dispatch-btn dispatch-primary alarm-handle" type="button">处理</button>'
      + '</td>'
      + '<td></td>'
      + '<td></td>'
      + '<td></td>'
      + '</tr>';
    $('#alarmTab table tbody').append(tr);
    $('.alarm-handle').on('click', this.dealWithAlarmHandler.bind(this));
    $('.address-info').on('click', this.getAddress.bind(this));
    this._dispatchModule.get('dispatchAmap').realTimeAlarmInfoCalcFn();
  },
  /**
   * 提交报警处理
   */
  submitAlarmHandle: function (e) {
    var monitorId = $(e.target).attr('data-id');
    var monitorName = $(e.target).attr('data-monitorName');
    var alarmType = $(e.target).attr('data-alarmType');
    var time = $(e.target).attr('data-time');
    var content = $('#sendTextContent').val();
    var data = {
      vehicleId: monitorId,
      plateNumber: monitorName,
      alarm: alarmType,
      startTime: time,
      remark: content
    };
    this._dispatchModule.get('dispatchServices').alarmHandle(
      data,
      this.alarmHandleCallback.bind(this)
    );
  },
  /**
   * 报警处理完成事件
   */
  alarmHandleCallback: function (data) {
    if (data.success) {
      var flag = data.obj.flag;
      var alarm = data.obj.alarm;
      var personName = alarm.personName;
      var handleTime = formatDateAll(alarm.handleTime * 1000);
      var remark = alarm.remark;
      var id = $('#submitAlarmHandle').attr('data-trId');
      $('#' + id + ' td:nth-child(6)').html('已处理');
      $('#' + id + ' td:nth-child(7)').text(personName);
      $('#' + id + ' td:nth-child(8)').text(handleTime);
      $('#' + id + ' td:nth-child(9)').text(remark == null ? '' : (remark.length > 40 ? remark.substring(0, 40) + '...' : remark)).attr('title', remark);
      if (flag === 1) {
        layer.msg('该报警已被其他用户处理', {offset: 't'});
      }
    }
    $('#dealWithAlarmModal').modal('hide');
    // 报警处理完毕后，延迟3秒进行结果查询
    setTimeout(pagesNav.gethistoryno, 3000);
  },
  /**
   * 获取位置信息
   */
  getAddress: function (e) {
    var latitude = Number($(e.target).attr('data-latitude'));
    var longitude = Number($(e.target).attr('data-longitude'));
    var id = $(e.target).attr('data-trId');
    this._dispatchModule.get('dispatchAmap').getAddress(
      [longitude, latitude],
      function (address) {
        $('#' + id + ' td:nth-child(5)').html(address);
      }
    );
  }
};
