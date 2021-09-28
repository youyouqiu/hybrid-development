var m_vehId;
var m_vehLic;
var m_vehChannel;
var m_vehColor;
var m_deviceNo;
var m_streamType;       // 0-主码流，1-子码流
var m_timeBarString;    // 时间轴的搜索结果
var m_videoFlag = 0;  //标识视频窗口是否打开
var m_imgFlag = 0;    //标识是否多通道同时抓图
var m_trans = 0;
var m_videoHost;
var m_videoPort;
var m_isVideo = 0;
var m_func;
var m_usec;
var m_vcid = [];
var m_voiceid;
var m_stime = [];
var m_riskNumber;
var m_vuuid;
var taskList = {};
var channels = [1, 5, 6, 7, 8]; // 1 5 6 7 - 视频通道, 8 - 音频通道
$(function () {
  m_videoHost = $("#videoHost").val();
  m_videoPort = $("#videoPort").val();
});

realTimeVideo = {

  generalTransNo: function () {
    m_trans = m_trans + 1;
    return m_trans;
  },
  getVideoOcx: function () {
    return document.getElementById("videoplayer");
  },
  //设置视频铺满窗口
  windowSet: function () {
    var cmdstr = '{"PARAM":{"WINNAME":"PREVIEW","VSMODE":2}}';
    realTimeVideo.getVideoOcx().WindowSet(cmdstr);
  },
  //查询是否IE浏览器
  ieExplorer: function () {
    var explorer = window.navigator.userAgent;
    var result = false;
    if (explorer.indexOf("MSIE") !== -1) {
      result = true;
    }
    return result;
  },
  downloadVideoOcx: function () {
    if (!realTimeVideo.ieExplorer()) {
      layer.alert("请使用IE浏览器才能观看视频哦");
    }
    /*if(realTimeVideo.getVideoOcx().object == null) {
        layer.alert("视频插件加载失败，请先下载安装插件<br>请不要使用迅雷下载", {btn : ['确定', '取消']},
        function(){location.href="/clbs/file/videoOcx/videocx.exe";layer.closeAll();},
        function(){layer.msg('取消');} );
    }	*/
  },
  showtask: function (strmsg) {
    var obj = document.getElementById("taskmsg");
    obj.innerHTML = "视频控件功能演示，当前业务：" + strmsg
  },
  showmsg: function (strmsg) {
    var obj = document.getElementById("locmsg");
    obj.innerHTML = strmsg + '<br>' + obj.innerHTML;
  },
  //设置车辆信息
  setVehicleInfo: function (vehicleParam) {
    var param = vehicleParam;
    m_vehLic = param.brand;
    m_streamType = 1;
    m_vehColor = parseInt(param.plateColor);
    m_deviceNo = param.deviceNumber;
    m_vehId = 0;
    m_isVideo = param.isVideo;
    m_func = param.func;
    m_usec = param.usec;
    m_riskNumber = param.riskNumber;
    m_vuuid = param.vuuid;
    //视频窗口打开才请求视频
    // if (m_func === 1 && realTimeVideo.ieExplorer()) {
    //     realTimeVideo.windowSet();
    //     realTimeVideo.beventLiveView();
    // } else if (m_func === 3) {
    //     realTimeVideo.beventLiveIpTalk();
    // }

    // if(m_videoFlag==1 && realTimeVideo.ieExplorer()){
    // 	realTimeVideo.windowSet();
    // 	// realTimeVideo.beventLiveView();

    // 	// wjk
    // 	realTimeVideo.beventLiveView(pageLayout.computingTimeIntFun);
    // }
  },
  //getchn：0 - 不用取通道号，1 - 取通道号
  // getstreamtype：0 - 不用取码流类型，1 - 取码流类型
  getVehicleInfo: function (getchn, getstreamtype) {
    var cmd = {
      VEHICLELICENSE: m_vehLic,
      PLATECOLOR: m_vehColor,
      DEVICENO: m_deviceNo,
      VEHICLEID: 0
    };
    if (getstreamtype === 1) {
      cmd.STREAMTYPE = m_streamType;
    }
    return cmd;
  },
  openVideo: function (vehicleInfo) {
    realTimeVideo.setVehicleInfo(vehicleInfo);
    realTimeVideo.windowSet();
    realTimeVideo.beventLiveView();
  },
  closeVideo: function (channel) {
    if (channel !== 0) {
      realTimeVideo.beventMediaStop(channel);
      return;
    }
    for (var i = 0; i < 4; i++) {
      realTimeVideo.beventMediaStop(channels[i]);
    }
  },
  openAudio: function (vehicleInfo) {
    realTimeVideo.setVehicleInfo(vehicleInfo);
    realTimeVideo.beventLiveIpTalk();
  },
  closeAudio: function () {
    realTimeVideo.beventMediaStop(8);
  },
  getRestChannels: function (channel) {
    var restChannels = [];
    if (!channel) {
      return restChannels;
    }

    for (var i = 0; i < 4; i++) {
      if (channels[i] === channel) {
        continue;
      }
      restChannels.push(channels[i]);
    }
    return restChannels;
  },
  // streamType: 0 - 不用取码流类型，1 - 取码流类型
  // dataType： 0 - 音视频，1 - 视频，2 - 双向对讲，3 - 监听
  // channelNumber: 通道号
  getVideoCmdStr: function (streamType, dataType, channelNumber) {
    var cmd = realTimeVideo.getCmd(streamType, dataType, channelNumber);
    cmd.DEVICETYPE = 0; // 锐明视频设备
    return JSON.stringify(cmd);
  },
  getAudioCmdStr: function (streamType, dataType, channelNumber) {
    var cmd = realTimeVideo.getCmd(streamType, dataType, channelNumber);
    cmd.DEVICETYPE = 53248; // 锐明音频设备
    cmd.STOREMEDIAFLAG = 0;
    // cmd.ALARMUUID = combat.alarmUUID;
    cmd.ALARMUUID = new Date().getTime()
    return JSON.stringify(cmd);
  },
  getCmd: function (streamType, dataType, channelNumber) {
    var cmd = {
      VEHICLELICENSE: m_vehLic,
      PLATECOLOR: m_vehColor,
      DEVICENO: m_deviceNo,
      VEHICLEID: 0,
      DATATYPE: dataType,
      RECORDFLAG: 0,
      CHANNEL: channelNumber
    };
    if (streamType === 1) {
      cmd.STREAMTYPE = m_streamType;
    }
    return cmd;
  },
  //直通业务, taskType：0 - 直通预览(音视频)，1 - 直通预览(仅视频)，2 - 对讲，3 - 监听
  beventLiveTask: function (taskType) { //wjk,回调定时器函数设置时间限制 ,callback 先注释掉
    //判断打开视频窗口是否有车辆信息
    if (typeof(m_vehLic) === "undefined") {
      return;
    }
    if (m_isVideo === 0) {
      layer.alert("当前设备未有视频哦");
      return;
    }
    var cmdList = [];
    if (m_usec === 0) {
      for (var i = 0; i < 4; i++) {
        var channel = channels[i];
        if (taskList[channel]) {
          // 如果该通道已经打开，则跳过，不重复打开
          continue;
        }
        var cmdStr = realTimeVideo.getVideoCmdStr(1, taskType, channels[i]);
        var taskId = realTimeVideo.generalTransNo();

        cmdList.push({taskId: taskId, cmdStr: cmdStr});
        m_vcid.push(taskId);
        taskList[channel] = taskId; // 记录通道号对应的任务id
      }
    } else if (m_usec === 8) {
      cmdStr = realTimeVideo.getAudioCmdStr(1, taskType, m_usec);
      taskId = realTimeVideo.generalTransNo();

      cmdList.push({taskId: taskId, cmdStr: cmdStr});
      m_voiceid = taskId;
      taskList[m_usec] = taskId; // 记录通道号对应的任务id
    } else {
      cmdStr = realTimeVideo.getVideoCmdStr(1, taskType, m_usec);
      taskId = realTimeVideo.generalTransNo();

      cmdList.push({taskId: taskId, cmdStr: cmdStr});
      m_vcid.push(taskId);
      taskList[m_usec] = taskId; // 记录通道号对应的任务id
    }
    var n = cmdList.length;
    for (i = 0; i < n; i++) {
      var retv = realTimeVideo.getVideoOcx().StartLiveTask(cmdList[i].taskId, m_videoHost, m_videoPort, cmdList[i].cmdStr);
      if (retv !== 0) {
        if (retv.toString(16) === "80f00003") {
          //layer.alert("请不要重复请求音频服务");
          realTimeVideo.showtask("");
        } else {
          layer.alert("开始实时音视频业务失败，错误号码:0x" + retv.toString(16));
        }
      }
    }
    // wjk
    // 先注释掉
    // if (callback) {
    // 	callback()
    // }
  },
  //根据任务号抓取图片
  snapAPicture: function (trans_no) {
    if (trans_no === 0) {
      layer.alert("还没开始视频，不能抓图哦");
      return;
    }
    if (m_imgFlag === 1) {//抓取所有通道图片
      var channelStr = "",
          channelError = "",
          message = "",
          transNo,
          retv;
      for (var i = 0; i < 4; i++) {
        transNo = i + 1;
        retv = realTimeVideo.cmdPictureParam(transNo);
        if (retv === "0") {
          channelStr += "," + transNo;
        } else {
          channelError += "," + transNo;
        }
        //组织抓取图片提示语言
        message = "";
        if (channelStr !== "") {
          message += "已成功抓取【" + channelStr.substring(1) + "】通道图片\n";
        }
        if (channelError !== "") {
          message += "抓取失败【" + channelError.substring(1) + "】通道图片，请确定视频请求成功了吗";
        }
        layer.alert(message);
      }
    } else { //抓取当前通道图片
      retv = realTimeVideo.cmdPictureParam(trans_no);
      if (retv === "0") {
        layer.alert("抓图成功,已存在" + $("#photoPath").val() + "目录");
      } else {
        layer.alert("请确定视频请求成功了吗");
      }
    }
    //realTimeVideo.showmsg('ScreenControl() return 0x' + retv.toString(16));
  },
  //执行抓图命令
  cmdPictureParam: function (trans_no) {
    var myDate = new Date();
    var strTime = "" + parseInt(myDate.getMonth() + 1) + myDate.getDate() + myDate.getHours() + myDate.getMinutes() + myDate.getSeconds();
    var path = $("#photoPath").val();
    var fileName = '"' + path + '/' + m_vehLic + '-' + strTime + '[' + trans_no + '].bmp"';
    var cmdstr = '{"DISPLAY":"' + m_vehLic + '","CAPWAY":2,"TIME":1,"SAVETO":' + fileName + '}';
    return realTimeVideo.getVideoOcx().ScreenControl(trans_no, 0x10000E, cmdstr);
  },

  //录像
  saveVidio: function (type) {

    var myDate = new Date();
    if (type === 1) {
      m_stime = [];
    }
    var strTime = "" + parseInt(myDate.getMonth() + 1) + myDate.getDate() + myDate.getHours() + myDate.getMinutes() + myDate.getSeconds();
    for (var i = 0; i < m_vcid.length; i++) {
      var fileName = '"C:/clbsVideo/' + m_riskNumber + '/' + m_vuuid + '/' + strTime + '[' + m_vcid[i] + '].avi"';
      if (type === 1) {
        m_stime.push(fileName.substring(65, fileName.length - 1))
      }
      var cmdstr = '{"VEHICLELICENSE":"' + m_vehLic + '","PLATECOLOR":2,"DEVICENO":"' + m_deviceNo + '","VEHICLEID":0,"CHANNEL":' + m_vcid[i] + ',"SAVETO":' + fileName + ',"STATE":' + type + '}';
      realTimeVideo.getVideoOcx().ScreenControl(m_vcid[i], 0x100013, cmdstr);
    }
  },

  //录音
  saveVoice: function (type) {
    var myDate = new Date();
    var strTime = "" + parseInt(myDate.getMonth() + 1) + myDate.getDate() + myDate.getHours() + myDate.getMinutes() + myDate.getSeconds();
    var fileName = '"' + path + '/' + m_vehLic + '-' + strTime + '[' + trans_no + '].avi"';
    var cmdstr = '{"VEHICLELICENSE":"' + m_vehLic + '","PLATECOLOR":2,"DEVICENO":"' + m_deviceNo + '","VEHICLEID":0,"CHANNEL":' + trans_no + ',"SAVETO":' + fileName + ',"STATE":"' + type + '"}';
    return realTimeVideo.getVideoOcx().ScreenControl($("#channel").val(), 0x100015, cmdstr);
  },

  //imgFlag标识是否多通道抓图:1多通道(用于页面下抓图按钮)，0单通道(用于窗口上的抓图按钮)
  beventCapture: function () {
    realTimeVideo.snapAPicture(m_trans);

  },
  //直通预览
  beventLiveView: function (callback) { //wjk ,加一个回调
    m_vcid = [];
    m_usec = 0;
    realTimeVideo.showtask("直通预览");
    realTimeVideo.beventLiveTask(0, callback);
  },
  // 监听，需要用到通道
  beventLiveMonitor: function () {
    realTimeVideo.showtask("监听");
    realTimeVideo.beventLiveTask(3);
  },
  //对讲，和通道无关
  beventLiveIpTalk: function (callback) { //wjk 加回调定时器
    m_usec = 8;
    realTimeVideo.showtask("对讲");
    realTimeVideo.beventLiveTask(2, callback);
  },
  //由于使用了固定的m_trans，所以在调用此接口之前，不能对m_trans的值进行改变
  //channel: 通道号 1 5 6 7 - 视频通道, 8 - 音频通道
  beventMediaStop: function (channel) {
    var taskId = taskList[channel];
    if (!taskId) {
      return;
    }
    var retv = realTimeVideo.getVideoOcx().StopMediaTask(taskId);
    realTimeVideo.showtask("");
    realTimeVideo.showmsg('StopMediaTask() return 0x' + retv.toString(16));

    if (retv === 0) {
      delete taskList[channel];
    }
  },
  //停止所有媒体业务，这里采用直接关闭。如果要异步关闭，则传入0并处理关闭事件，在关闭事件中进行视频关闭
  beventAllMediaStop: function () {
    if (realTimeVideo.getVideoOcx().CloseAllMedia) {
      var retv = realTimeVideo.getVideoOcx().CloseAllMedia(1);
      realTimeVideo.showtask("");
      realTimeVideo.showmsg('CloseAllMedia() return 0x' + retv.toString(16));
    }

    m_trans = 0;
    for (var i = 0; i < 5; i++) {
      delete taskList[channels[i]];
    }
  },
  video_event: function (msg_type, trans_no, data_type, server_ip, server_port, cmd_str, cmd_len) {

    switch (msg_type) {
        // 响应视频窗口右键菜单中的打开声音命令
      case 0x100001:
        realTimeVideo.getVideoOcx().OpenSound();
        break;

        // 响应视频窗口右键菜单中的关闭声音命令
      case 0x100002:
        realTimeVideo.getVideoOcx().CloseSound();
        break;

        // 响应视频窗口右键菜单中的关闭视频或者视频窗口右上角的“x”号，用于关闭一个视频
      case 0x100007:
        realTimeVideo.getVideoOcx().StopMediaTask(trans_no);
        break;

        // 抓图，需要调用抓图接口开始抓图
      case 0x10000E:
        m_imgFlag = 0; //标识当前通道抓图
        //$("#videoPhotograph").modal('show');
        realTimeVideo.snapAPicture(trans_no);
        break;
      case 0x100015:
        layer.alert("系统暂不支持此功能");
        break;
      default:
        break;

    }
  }
};
$("#btnPhotoWindow").click(function () {
  m_imgFlag = 1; //标识启动多通道抓图
});
$("#btnPhoto").click(function () {
  realTimeVideo.beventCapture(); //调用抓图
});
