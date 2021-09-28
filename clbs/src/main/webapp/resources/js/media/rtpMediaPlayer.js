/**
 * MSE操作视频播放和websocket连接
 * @param opt
 * @constructor
 */
class RTPMediaPlayer {
  /**
   * defaults为整个插件的默认参数
   * 'video/webm; codecs=“vorbis,vp8”’ webm类型
   * 'video/mp4; codecs=“avc1.42E01E,mp4a.40.2”’ MP4类型
   * 'video/mp2t; codecs=“avc1.42E01E,mp4a.40.2”’ ts类型
   * 'video/mp4; codecs="avc1.420028"'
   * @type {{}}
   */
  constructor(opt) {
    // console.log(11111111);
    this.defaults = {
      /**
       * 改版新增的回调
       */
      // 接收到业务指令的回调
      onMessage:null,

      /**
       *  mediaElement 回调事件 播放/暂停/结束/错误
       */
      onPlaying: null,
      onPause: null,
      onEnded: null,
      onError: null,

      /**
       * 音视频编解码器初始化的事件回调
       */
      // 音频打开成功事件
      openAudioSuccess: null,
      // 音频打开失败事件
      openAudioFail: null,
      // 缺少设备回调函数
      onDevicePresentFunc: null,

      // socket回调
      socketCloseFun: null,
      socketErrorFun: null,
      socketOpenFun: null,
      // 0：普通；1：360全景
      panoramaType: 0,
      // vr提示图片
      vrImageSrc:'/clbs/resources/img/1.png',
      // vrImageSrc:'',

      mimeCodec: 'video/mp4; codecs="avc1.420028"',
      url: '',
      domId: '',
      // 外部提供的数据
      data: null,
      // 视频播放类型
      // 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
      playType: 'REAL_TIME',

      // 是否开启音频
      audioEnabled: false,
      // 是否开启视频
      videoEnabled: true,
      // 视频开启录音
      recordEnabled: false,

      // webrtc
      rtcEnabled:false,

      imgBack: '',
      imgSrc: true
    };
    this.streamInfo = {
      target:1,
      ip:'',
      port:0,
      state:-1,
      timestampStart:0,
      timestampEnd:0,
      message:'',
      sendBytes:0,
      recvBytes:0,
      simNumber:'',
      channel:0,
      uuid:'',
    };


    this._addAssign();

    this.options = Object.assign({}, this.defaults, opt);
    if(opt.data&&opt.data.playType){
      this.options.playType = opt.data.playType;
    }


    // 测试用
    this._h264Cache = new Uint8Array(1024*50);
    this._h264CacheIndex = 0;
    this._savefile = false;
    // 测试用
    this._rtpCache = new Array;
    this._rtpCacheMax = 100;
    this._rtpCacheIndex = 0;
    this._rtpCacheEnabled = false;
    // 360
    this._context360 = {};
    this._context360.material = null;
    this._context360.geometry = null;
    this._context360.mesh = null;
    this._context360.camera = null;
    this._context360.scene = null;
    this._context360.renderer = null;
    this._context360.container = null;
    this._context360.fov = 100;
    this._context360.width = 500;
    this._context360.height = 500;
    this._context360.radius = 100;
    this._context360.lat = 0;
    this._context360.lon = 0;
    this._context360.orientLat = 1;
    this._context360.orientLon = 1;
    this._context360.factor = 1;
    this._context360.control = null;

    // this._360opt.container = document.getElementById("container");

    // 回放
    this._isPlayback = this.options.playType == 'TRACK_BACK';
    this._playbackStartTime = 0;        // 播放开始时间

    // webrtc
    this._rtcConnection = null;
    this._rtcChannel = null;

    /**
     * pendingRemoveRanges为source缓存数据清空定时器
     */
    this.pendingRemoveRanges = null;
    /**
     * this.mediaElement为video标签
     */
    this.mediaElement = null;
    /**
     * this.mediaSource为MSE sourceBuffer
     */
    this.mediaSource = null;
    this.websocket = null;
    this.sourceBuffer = null;

    /**
     * 初始化h264转fmp4方法类
     */
    this.fmp4 = null;

    /**
     * socket连接成功后，是否有消息接收
     */
    this.messageState = false;

    /**
     * 监听超时定时器
     */
    this.timeOutMonitoring = null;

    /**
     * 当前页面是否聚焦
     */
    this.pageFocus = true;

    /**
     * 视频是否已经播放
     */
    this.isVideoPlay = false;

    /**
     * socket是否被关闭
     */
    this.socketState = true;

    /**
     * create url
     */
    this.mediaSourceObjectURL = null;

    /**
     * 心跳代码
     */
    this.heartbeat = null;

    this.startTime = 0;

    /**
     * 视频声音开启状态
     */
    this.audioEnabled = this.options.audioEnabled;
    this.videoEnabled = this.options.videoEnabled;
    this.recordEnabled = this.options.recordEnabled;

    this.paused = false;

    this.rtp = new Rtp();

    /**
     * 缓存清理参数
     */
    this.clearFlag = 0;
    this.clearTime = 0;

    /**
     * 空闲超时
     */
    this.freeTimeout = null;

    /**
     * 超时重连计数
     */
    this.timeoutNumber = 0;

    this.isPFrame = 0;

    this.videoCurrentTime = 0;

    this.audioCodec = null;
    this.pcmPlayer = null;
    this.audioRecorder = null;
    this.recvPackageTimeArr = new Array();
    this.payloadTypeAudio = -1;
    this.firstAudioPlayData = true;
    this.firstAudioRecordeData = true;
    this.audioPlayerSuccess = false;
    this.audioRecordeSuccess = false;

    this._initStreamInfo();


    if (this.videoEnabled){
      this._createMediaSource();
    }
    this.audioOpt = {
      onDecodeFunc : this._onDecodeDataAudio.bind(this),
      onEncodeFunc : this._onEncodeDataAudio.bind(this),
    };
    if (this.audioEnabled){
      this.pcmPlayer = new PCMPlayer(1, 8000);
      this.audioCodec = new AudioCodec(this.audioOpt);
    }
    this.recordOpt = {
      onDataFunc : this._onRecordDataAudio.bind(this),
      onDevicePresentFunc : this._onDevicePresent.bind(this),
      onReadyFunc : this._onRecordReady.bind(this),
    };
    if (this.recordEnabled){
      this.audioRecorder = new AudioRecorder(this.recordOpt);
    } else {
      this._websocketInit();
    }
  }

  /**
   * 初始化当前流信息
   * @private
   */
  _initStreamInfo(){
    try {
      let startIndex = this.options.url.indexOf('//');
      let portStartIndex = this.options.url.indexOf(':',startIndex);
      let lastIndex = this.options.url.lastIndexOf('/');
      this.streamInfo.ip = this.options.url.substr(startIndex+2, portStartIndex-startIndex-2)
      this.streamInfo.port = parseInt(this.options.url.substr(portStartIndex+1, lastIndex-portStartIndex-1));

      if (this.options.data){
        this.streamInfo.simNumber = this.options.data.mobile;
        this.streamInfo.channel = parseInt(this.options.data.channelNum);
        this.streamInfo.uuid = this.options.data.vehicleId;
      }
    }
    catch(err){
      console.error(err);
    }
  }

  /**
   * 判断浏览器是否支持Object.assign方法
   * 不支持就进行手动添加
   */
  _addAssign () {
    if (typeof Object.assign != 'function') {
      Object.defineProperty(Object, "assign", {
        value: function assign(target, varArgs) {
          'use strict';
          if (target == null) {
            throw new TypeError('Cannot convert undefined or null to object');
          }

          var to = Object(target);

          for (var index = 1; index < arguments.length; index++) {
            var nextSource = arguments[index];

            if (nextSource != null) {
              for (var nextKey in nextSource) {
                if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                  to[nextKey] = nextSource[nextKey];
                }
              }
            }
          }
          return to;
        },
        writable: true,
        configurable: true
      });
    }
  }


  /**
   * 判断浏览器是否支持MediaSource和指定要解码播放的视频文件编码和类型
   * @returns {boolean|*}
   */
  _isMseSupport () {
    return 'MediaSource' in window && MediaSource.isTypeSupported(this.options.mimeCodec);
  }

  /**
   * 初始化MediaSource
   */
  _createMediaSource () {
    if (!this._isMseSupport()) {
      throw new Error('浏览器不支持MediaSource或指定的编码类型');
    }
    if (!this.options.domId){
      throw new Error('domId不能为空');
    }
    this.mediaSource = new window.MediaSource();
    /**
     * 如果获取的video状态为未播放状态就替换节点
     */

    let newVideo = document.createElement('video');
    newVideo.id = this.options.domId;
    newVideo.style.width = '100%';
    newVideo.style.height = '100%';

    // if(this.options.imgSrc){
    //   newVideo.style.cssText = "width:100%; height:100%; background-image: url(../../../clbs/resources/img/videoCarousel/video_request.png)!important";
    // }else{
    //   newVideo.style.cssText = "width:100%; height:100%; background-image: url(/clbs/resources/img/videoCarousel/video_request.png)!important";
    // }


    newVideo.autoplay = true;
    if (this.options.imgBack)
      newVideo.poster = this.options.imgBack;
    // newVideo.innerHTML = '<img src="'+this.options.imgPlay+'" style="width: 100%; height: 100%" />';
    var nowVideo = document.getElementById(this.options.domId);
    if (nowVideo.muted === true) {
      newVideo.muted = true;
    }
    nowVideo.parentNode.replaceChild(newVideo, nowVideo);
    this.mediaElement = document.getElementById(this.options.domId);
    this.mediaElement.setAttribute('data-channel-up', 'true');

    this.mediaSourceObjectURL = window.URL.createObjectURL(this.mediaSource);
    this.mediaElement.src = this.mediaSourceObjectURL;

    if (this.options.panoramaType == 1){
      let newContainer = document.createElement('div');
      newContainer.style.width = '100%';
      newContainer.style.height = '100%';
      newVideo.parentNode.appendChild(newContainer);
      newVideo.style.display = 'none';
      this._context360.width = newContainer.offsetWidth;
      this._context360.height = newContainer.offsetHeight;
      this._init360(newContainer, this.mediaElement);

      // vr悬浮图片样式
      if (this.options.vrImageSrc){
        newVideo.parentNode.style.position = 'relative';
        let image = document.createElement('img');
        image.src = this.options.vrImageSrc;
        image.style.width = '30px';
        image.style.height = '30px';
        image.style.position = 'absolute';
        image.style.right = '5px';
        image.style.bottom = '5px';
        newVideo.parentNode.appendChild(image);
      }

    }

    /**
     * 给video标签绑定一系列监听事件
     * playing 在媒体开始播放时触发
     * pause 播放暂停时触发
     * ended 播放结束时触发
     * error 发生错误时触发
     */
    this.mediaElement.onplaying = this._onPlaying.bind(this);
    this.mediaElement.onpause = this._onPause.bind(this);
    this.mediaElement.onended= this._onEnded.bind(this);
    this.mediaElement.onerror = this._onError.bind(this);


    /**
     * mediaSource的一系列监听事件
     */
    this.mediaSource.onsourceopen = this._onMediaSourceOpen.bind(this);
    this.mediaSource.onsourceended = this._onMediaSourceEnded.bind(this);
    this.mediaSource.onerror = this._onUpdateError.bind(this);

    /**
     * 判断浏览器类型，调用视频play方法
     */
    this._mediaElementPlay();
  }

   _init360(container, mediaElement) {
    this._context360.camera = new THREE.PerspectiveCamera(this._context360.fov, this._context360.width / this._context360.height, 1, 10000);
    // this._context360.camera.target = new THREE.Vector3(0, 0, 0);
    this._context360.camera.position.z = 0.01;

    this._context360.geometry = new THREE.SphereBufferGeometry(this._context360.radius, 60, 60);
    this._context360.geometry.scale(-1, 1, 1);

    let texture = new THREE.VideoTexture(mediaElement);
    texture.wrapS = texture.wrapT = THREE.ClampToEdgeWrapping;
    texture.minFilter = THREE.LinearFilter;
    this._context360.material = new THREE.MeshBasicMaterial( { map: texture } );

    this._context360.mesh = new THREE.Mesh(this._context360.geometry, this._context360.material);
    this._context360.scene = new THREE.Scene();
    this._context360.scene.background = new THREE.Color( 0x000 );
    this._context360.scene.add(this._context360.mesh);

    this._context360.renderer = new THREE.WebGLRenderer();
    this._context360.renderer.setPixelRatio(window.devicePixelRatio);
    this._context360.renderer.setSize(this._context360.width, this._context360.height);
    let canvas = this._context360.renderer.domElement;
    canvas.style.width = '100%';
    canvas.style.height = '100%';


    this._context360.container = container;
    this._context360.container.appendChild(canvas);

    this._context360.control = new THREE.OrbitControls( this._context360.camera, this._context360.renderer.domElement );
    this._context360.control.enableZoom = false;
    this._context360.control.enablePan = false;
    this._context360.control.enableDamping = true;
    this._context360.control.rotateSpeed = - 0.25;
    this._context360._mousedowned = false;
  }
  MouseDown360(event) {
    if (this.options.panoramaType != 1) return;
    if (event.button != 0) return;
    this._context360.mousedowned = true;
    this._context360.control.onMouseDown(event);
  }

// 滑动
  MouseMove360(event) {
    if(!this._context360.mousedowned) return;
    this._context360.control.onMouseMove(event);
  }
// 结束
  MouseUp360(event) {
    if (this._context360.mousedowned) this._context360.mousedowned = false;
    this._context360.control.onMouseUp(event);
  }

  _render360(){
    if (this.isVideoPlay){
      requestAnimationFrame(this._render360.bind(this));
    }
    if (this._context360.camera) {
      if (this._context360.container.offsetWidth != this._context360.width || this._context360.container.offsetHeight != this._context360.height){
        this._context360.width = this._context360.container.offsetWidth;
        this._context360.height = this._context360.container.offsetHeight;
        this._context360.renderer.setSize(this._context360.width, this._context360.height);
        this._context360.camera.aspect = this._context360.width / this._context360.height;
        this._context360.camera.updateProjectionMatrix();
      }

      // console.log('control.update');
      this._context360.control.update();
      this._context360.renderer.render(this._context360.scene, this._context360.camera);
    }

  }

  /**
   * 目前是获取是火狐浏览器就进行播放
   */
  _mediaElementPlay() {
    // if (navigator.userAgent.indexOf("Firefox") > 0)
    {
      this.mediaElement.play();
    }
  }

  /**
   * mediaElement-视频开始播放时
   */
  _onPlaying () {
    // console.log('_onPlaying');
    if (this.pageFocus) {
      if (this.options.onPlaying && !this.isVideoPlay) {
        this.isVideoPlay = true;
        this.options.onPlaying(this.options.data, this);
      }
    } else {
      this.pageFocus = true;
    }
    if (this.options.panoramaType == 1){
      this._render360();
    }
  }

  /**
   * mediaElement-视频播放暂停时
   */
 _onPause () {
    console.log('_onPause');
    if (this.pageFocus) {
      if (this.options.onPause) {
        this.options.onPause();
      }
      // 播放暂停时，保持连接，后台继续获取并解码数据
      // this.websocket.close();
    }
  }

  /**
   * mediaElement-视频播放结束时
   */
 _onEnded () {
    console.log('_onEnded');
    if (this.options.onEnded) {
      this.options.onEnded();
    }
    this._closeSocket('End of play!');
  }

  /**
   * mediaElement-视频播放发生错误时
   */
  _onError (err) {
    const videoElement = err.target;
    console.log('_onError code: ' + videoElement.error.code, ', message: ' + videoElement.error.message);

    // this.destroyMSE();
    //
    // this.createMediaSource();
    this._closeSocket('Codec error， code: ' + videoElement.error.code, ', message: ' + videoElement.error.message)
    if (this.options.onError) {
      this.options.onError(this.options);
    }
  }

  /**
   * mediaSource监听打开事件
   */
  _onMediaSourceOpen () {
    console.log('onMediaSourceOpen');
    if (!this.sourceBuffer && this.mediaSource){
      this.sourceBuffer = this.mediaSource.addSourceBuffer(this.options.mimeCodec);
      this.sourceBuffer.onerror = this._onSourceBufferError.bind(this);
      this.sourceBuffer.onabort = this._onSourceBufferAbort.bind(this);
      this.sourceBuffer.onupdate = this._onSourceBufferUpdate.bind(this);
      this.sourceBuffer.onupdateend = this._onSourceBufferUpdateend.bind(this);
      this.sourceBuffer.onupdatestart = this._onSourceBufferUpdatestart.bind(this);
    }
  }
  /**
   * mediaSource监听结束事件
   */
  _onMediaSourceEnded () {
    console.log('MediaSource onSourceEnded');
  }

  /**
   * mediaSource监听错误事件
   */
  _onUpdateError (error) {
    console.warn(error);
  }

  /**
   * source监听一段chunk播放完毕事件
   */

  /**
   * 组装buffered并内存释放
   * 1分钟清一次视频缓存
   */
  _doCleanupSourceBuffer () {
    const that = this;
    this.pendingRemoveRanges = setInterval(function () {
      if (that.sourceBuffer) {
        if(!this.sourceBuffer.updating) {
          let buffered = that.sourceBuffer.buffered;
          if (buffered.length > 0) {
            let start = buffered.start(0);
            let end = buffered.end(0);
            try {
              that.sourceBuffer.remove(start, end - 10);
            } catch (err) {
              console.warn(err.message);
            }
          }
        }
      }
    }, 40000);
  }

  /**
   * source监听错误事件
   */
  _onSourceBufferError (error) {
    console.warn(error);
  }
  _onSourceBufferAbort () {
    // console.log('source buffer abort');
  }
  _onSourceBufferUpdate () {
    // console.log('source buffer update');
  }
  _onSourceBufferUpdateend () {
    // console.log('source buffer update end');
  }
  _onSourceBufferUpdatestart () {
    // console.log('source buffer update start');
  }


  /**
   * 监听页面是否失去焦点
   * 失去焦点后，页面播放视频暂停
   * 获取焦点后，视频跳转到最新
   */
  _visibilitychangeFun () {
    const isHidden = document.hidden;
    if (!isHidden) {
      // 根据测试反馈，回放时取消跳转到最新时间
      if (this.options.playType == "REAL_TIME") {
        this._videoUpdateTime();
      }
    } else {
      this.pageFocus = false;
    }
  }

  /**
   * 测试
   * 直接添加视频片段segment
   */
  _doAppendSegments (segment) {
    if (this.sourceBuffer && !this.sourceBuffer.updating && this.socketState && this.messageState) {
      this.sourceBuffer.appendBuffer(segment);
    }
  }

  /**
   * websocket 链接
   */
  _websocketInit() {
    /**
     * 判断是否支持websocket且url地址不为空
     */
    if (this.websocket == null || this.websocket.readyState == 3){
      console.log('websocketInit');
      if ('WebSocket' in window && this.options.url !== '') {
        this.websocket = new WebSocket(this.options.url);
        /**
         * 设置接收的二进制数据格式
         * @type {string}
         */
        this.websocket.binaryType = 'arraybuffer';

        this.websocket.onopen = this._socketOpen.bind(this);
        this.websocket.onmessage = this._socketMessage.bind(this);
        this.websocket.onerror = this._socketError.bind(this);
        this.websocket.onclose = this._socketClose.bind(this);
      } else {
        throw new Error('浏览器不支持websocket或url为空');
      }
    }
  }

  _visibilityEvent () {
    RTPMediaPlayer.arr.forEach(function(ele){
      ele.call();
    });
  }


  /**
   * websocket成功建立的回调函数
   */
  _socketOpen () {
    console.log('video _socketOpen,rtc='+this.options.rtcEnabled);
    this.streamInfo.timestampStart = new Date().getTime();
    this.streamInfo.state = '0';
    // this._doCleanupSourceBuffer();
    // this._monitoringMessage();
    document.onvisibilitychange = this._visibilityEvent;
    RTPMediaPlayer.arr.push(this._visibilitychangeFun.bind(this))
    this.fmp4 = new Fmp4(10 * 1024 * 1024);

    /**
     * 发送心跳
     * @type {number}
     */
    var that = this;
    this.heartbeat = setInterval(function () {
      let senddata = '0';
      if (this.sourceBuffer && this.mediaElement && this.sourceBuffer.buffered.length>0){
        let endTime = this.sourceBuffer.buffered.end(0);
        let startTime = this.sourceBuffer.buffered.start(0);
        let currentTime = this.mediaElement.currentTime;
        // console.log('currentTime='+currentTime+',startTime='+startTime+',endTime='+endTime);
        if (endTime-currentTime>600){
          // 未播放的缓存超过10分钟通知后台暂停
          if (this.options.playType == 'TRACK_BACK'){
            senddata = '1';
          }
        }
      }
      if (that.websocket.readyState == 1){
        this.streamInfo.sendBytes += senddata.length;
        that.websocket.send(senddata);
      } else {
        console.error('Heartbeat error!');
      }
    }.bind(this), 5000);

    if (this.options.rtcEnabled){
      if(this._initRtc()){
        return;
      }
      console.log('video _initRtc false');
    }
    if (this.options.socketOpenFun) {
      this.options.socketOpenFun(this.options.data, this);
    }


    // this.play();
  }

  _closeRtc(){
    try {
      if (this._rtcConnection){
        this._rtcConnection.close();
        this._rtcConnection = null;
      }
    } catch (e) {
      console.warn('_closeRtc _rtcConnection', e);
    }
    try {
      if (this._rtcChannel){
        this._rtcChannel.close();
        this._rtcChannel = null;
      }
    } catch (e) {
      console.warn('_closeRtc _rtcChannel', e);
    }
  }
  /**
   * 初始化webrtc
   * @returns {boolean}
   * @private
   */
  _initRtc(){
    do {
      this._rtcConnection = new RTCPeerConnection(null);
      if (!this._rtcConnection){
        console.warn('fail rtc connection!!');
        break;
      }
      this._rtcChannel = this._rtcConnection.createDataChannel('rtp808');
      if (!this._rtcChannel){
        console.warn('fail rtc channel!!');
        break;
      }

      this._rtcConnection.onicecandidate = this._onRtcConnectionIceCandidate.bind(this);
      this._rtcChannel.onopen = this._onRtcChannelOpened.bind(this);
      this._rtcChannel.onclose = this._onRtcChannelClosed.bind(this);
      this._rtcChannel.onmessage = this._onRtcChannelMessage.bind(this);

      this._rtcConnection.createOffer().then(
          this._onOfferCreate.bind(this),
          this._onCreateSessionDescriptionError.bind(this)
      );
      return true;
    }while(false);
    this._closeRtc();
    return false;
  }
  _onOfferCreate(desc){
    console.log('_onOfferCreate ', desc);
    let offerMsg = {};
    offerMsg.offer = desc.sdp;
    if (!this.sendMessage(50003, offerMsg)){
      this._closeRtc();
    }
  }
  _onCreateSessionDescriptionError(error){
    console.warn('_onCreateSessionDescriptionError ', error);
    this._closeRtc();
  }
  _onRtcConnectionIceCandidate(event){
    console.log('_onRtcConnectionIceCandidate', event.candidate);
    let candidateMsg = {};
    candidateMsg.candidate = event.candidate;
    candidateMsg.mid = 0;
    if (!this.sendMessage(50004, candidateMsg)){
      this._closeRtc();
    }
  }
  _onRtcChannelOpened(){
    console.log('_onRtcChannelOpened');
    if (this.options.socketOpenFun) {
      this.options.socketOpenFun(this.options.data, this);
    }
  }
  _onRtcChannelClosed(){
    console.log('_onRtcChannelClosed');
    this._closeSocket('rtc close');
  }
  _onServerAnswer(data){
    console.log('_onServerAnswer ',data);
    if (this._rtcConnection){
      let an = {};
      an.type = "answer"
      an.sdp = data.answer;
      this._rtcConnection.setRemoteDescription(an);
    }
  }
  _onServerCandidate(data){
    console.log('_onServerCandidate ',data);
    let pthis = this;
    if (this._rtcConnection){
      this._rtcConnection.addIceCandidate({
            candidate:data.candidate,
            sdpMid:data.mid,
            sdpMLineIndex:data.midIndex,
          })
          .then(
              () => pthis._onAddIceCandidateSuccess(),
              err => pthis._onAddIceCandidateError(err)
          );
    }
  }
  _onAddIceCandidateSuccess(){
    console.log('_onAddIceCandidateSuccess ');
  }
  _onAddIceCandidateError(err){
    console.warn('_onCreateSessionDescriptionError ', err);
    this._closeRtc();
  }
  _onRtcChannelMessage(event){
    console.log('_onRtcChannelMessage ', event);
  }
  /**
   * websocket发生错误的回调函数
   */
  _socketError (error) {
    console.warn('video _socketError:'+error);
    this.streamInfo.message = 'socket error:'+error;
    if (this.options.socketErrorFun) {
      this.options.socketErrorFun(this.options.data);
    }
  }

  /**
   * websocket关闭的回调函数
   */
  _socketClose () {
    console.log('video _socketClose');
    // if (this.websocket) {
      this.streamInfo.timestampEnd = new Date().getTime();
      this.streamInfo.state = '2';
      this.socketState = false;
      clearTimeout(this.timeOutMonitoring);
      if (this.options.socketCloseFun) {
        this.options.socketCloseFun(this.options.data);
      }
      clearInterval(this.pendingRemoveRanges);
      this.pendingRemoveRanges = null;
      this._destroy();
    // }
  }
  /**
   * websocket接收消息的回调函数
   */
  _socketMessage (event) {
    if (!this.messageState) {
      this.messageState = true;
    }
    if (this.paused){
      return;
    }

    if(typeof(event.data) == 'string'){
      this._onMessage(event.data);
      return;
    }
    /**
     * 对socket发送过来的数据进行处理转换成fmp4
     * 然后调用MSE方法塞进video，进行视频播放
     *
     */
    /**
     * 解析rtp数据，获得H264数据
     *
     */
    this.streamInfo.recvBytes += event.data.byteLength;
    let rtpFrame = this.rtp.parse(event.data);
    let payloadType = this.rtp.getPayloadType();
    if(rtpFrame.errorCode === Result.ErrorCode.SUCCESS) {
      this.streamInfo.state = '1';
      /**
       * 判断是否是视频数据
       */
      // rtpFrame.duration = rtpFrame.duration == 0 ? 40 : rtpFrame.duration;
      if(rtpFrame.type === Result.Type.H264_I_FRAME ||
          rtpFrame.type === Result.Type.H264_P_FRAME ||
          rtpFrame.type === Result.Type.H264_B_FRAME
      ) {
        if (!this.videoEnabled || !this.sourceBuffer){
          return;
        }
        // console.log('video time='+rtpFrame.time+',duration='+rtpFrame.duration);
        /**
         * 清除视频缓存
         * @type {number}
         */
        if (rtpFrame.type === Result.Type.H264_I_FRAME) {
          ++this.clearFlag;
        }

        // if(rtpFrame.type == Result.Type.H264_P_FRAME)
        // {
        if (this.options.remoteMode === 2 || this.options.remoteMode === 3) {
          this.isPFrame = 0;
        } else {
          this.isPFrame = 1;
        }
        // }
        rtpFrame.duration = this.startTime === 0 ? 0 : (rtpFrame.duration > 0 ?
            rtpFrame.duration : rtpFrame.time - this.startTime);
        if(this.isPFrame === 1)
        {
          rtpFrame.duration = (rtpFrame.duration > 0 && rtpFrame.duration < 2000) ?
              rtpFrame.duration : ((this.startTime === 0 && rtpFrame.duration === 0) ? 0 : 500);
        }

        if(this.sourceBuffer && !this.sourceBuffer.updating) {
          /**
           * 清除视频缓存
           * @type {number}
           */
          if (this.options.playType === 'REAL_TIME') {
            if (this.clearFlag > 10) {
              if(this.clearTime !== 0) {
                this.sourceBuffer.remove(0, this.clearTime);
                if(this.mediaElement.currentTime<this.clearTime) {
                  this.mediaElement.currentTime = this.clearTime;
                }
              }
              this.clearTime = this.fmp4.getLastIFrameTime() / 1000;
              this.clearFlag = 0;
            }
          }
          if (this.sourceBuffer && this.mediaElement && this.sourceBuffer.buffered.length>0){
            let startTime = this.sourceBuffer.buffered.start(0);
            let currentTime = this.mediaElement.currentTime;
            if (currentTime - startTime > 60) {
              // 暂时只保留1分钟已播放的缓存
              this.sourceBuffer.remove(0, currentTime-60);
            }
          }
        }

        if(this.sourceBuffer && !this.sourceBuffer.updating) {
          /**
           * 将H264数据，封装为Fmp4数据，frameDuration为延迟，可通过rtpFrame.time计算
           */
          let fmp4Frame = this.fmp4.makeVideoFrame(rtpFrame, rtpFrame.duration);
          if(fmp4Frame.errorCode === Result.ErrorCode.SUCCESS) {
            if(fmp4Frame.type === Result.Type.FMP4_HEAD) {
              /**
               * 初始化完成，打印视频宽高
               */
              const width = this.fmp4.getWidth();
              const height = this.fmp4.getHeight();
              console.log('Update size:'+width+'x'+height);
            }
            /**
             * 将Fmp4数据，传入MSE对象
             */
            this._doAppendSegments(fmp4Frame.data);
          }
          else {
            /**
             * 封装Fmp4出错，进行错误处理
             */
            console.error('makeVideoFrame:'+Result.ErrorCode.GetMessage(fmp4Frame.errorCode));
          }
        }
        else {
          /**
           * MSE对象繁忙，进行缓存
           */
          let result = this.fmp4.saveVideoFrame(rtpFrame, rtpFrame.duration);
          if(result !== Result.ErrorCode.SUCCESS) {
            /**
             * 缓存出错，进行错误处理
             */
            console.error('saveVideoFrame:'+result+'; data:');
            console.log(rtpFrame.data);
          }
        }
        this.startTime = rtpFrame.time;
      }
      else if(
          rtpFrame.type === Result.Type.AUDIO &&
          payloadType !== 'OFF' &&
          payloadType !== null
      ) {
        // console.log('audio time='+rtpFrame.time+',duration='+rtpFrame.duration);
        /**
         * 处理音频数据
         */
        let failMessage = '';
        if (this.audioEnabled) {
          if (!this.pcmPlayer){
            this.pcmPlayer = new PCMPlayer(1, 8000);
          }
          if (!this.audioCodec){
            this.audioCodec = new AudioCodec(this.audioOpt);
          }

          if (this.audioRecorder){
            this.recvPackageTimeArr.push(rtpFrame.time);
          }
          this.payloadTypeAudio = payloadType;
          let errorMsg = this.audioCodec.decode(rtpFrame.data, payloadType);
          let bret = !(errorMsg&&errorMsg.length>0);
          if (bret){
            this.audioPlayerSuccess = true;
          } else {
            failMessage = errorMsg;
          }
        }

        if (failMessage && failMessage.length>0){
          if (this.firstAudioPlayData || this.firstAudioRecordeData){
            this.firstAudioPlayData = false;
            this.firstAudioRecordeData = false;
            if (this.options.openAudioFail){
              this.options.openAudioFail(failMessage);
            }
          }
        } else {

          if(this.options.playType == 'BOTH_WAY') {
            if (this.audioPlayerSuccess && this.audioRecordeSuccess){
              if (this.firstAudioPlayData || this.firstAudioRecordeData){
                this.firstAudioPlayData = false;
                this.firstAudioRecordeData = false;
                if (this.options.openAudioSuccess){
                  console.log('openAudioSuccess');
                  this.options.openAudioSuccess(this.options.data);
                }
              }
            }
          } else if(this.options.playType == 'UP_WAY') {
            if (this.audioPlayerSuccess){
              if (this.firstAudioPlayData){
                this.firstAudioPlayData = false;
                if (this.options.openAudioSuccess){
                  console.log('openAudioSuccess');
                  this.options.openAudioSuccess(this.options.data);
                }
              }
            }
          } else if(this.options.playType == 'DOWN_WAY') {
            if (this.audioRecordeSuccess){
              if (this.firstAudioRecordeData){
                this.firstAudioRecordeData = false;
                if (this.options.openAudioSuccess){
                  console.log('openAudioSuccess');
                  this.options.openAudioSuccess(this.options.data);
                }
              }
            }
          } else {
            if (this.audioPlayerSuccess){
              if (this.firstAudioPlayData){
                this.firstAudioPlayData = false;
                if (this.options.openAudioSuccess){
                  console.log('openAudioSuccess');
                  this.options.openAudioSuccess(this.options.data);
                }
              }
            }
          }
        }

      }
      else {
        /**
         * 处理其他数据
         */
      }
    }
    else {
      /**
       * 解析rtp数据出错，进行错误处理
       */
    }
    // }
  }
  _onDecodeDataAudio(pcmFloat32Data, sampleRate, channels){
    if (this.audioRecorder){
      this.audioRecorder.pushAudioFrameSampleCount(pcmFloat32Data.length*8000/sampleRate);
    }
    if (this.pcmPlayer){
      this.pcmPlayer.setChannels(channels);
      this.pcmPlayer.setSampleRate(sampleRate);
      this.pcmPlayer.feed(pcmFloat32Data);
    }
  }
  _onEncodeDataAudio(data){

    let timestamp = this.recvPackageTimeArr.shift();

    let payloadType = this.payloadTypeAudio;
    // aac改为g711a
    if (payloadType == 19){
      payloadType = 6;
    }
    let rtpData = this.rtp.makeAudio(data, timestamp, payloadType);

    if(rtpData.errorCode === Result.ErrorCode.SUCCESS) {
      // 音频片段发送给socket
      if (this.websocket && this.websocket.readyState === 1) {
        this.websocket.send(rtpData.data);
      }
    }
  }


  _onRecordDataAudio(pcmPacketData){
    if (!this.audioCodec){
      this.audioCodec = new AudioCodec(this.audioOpt);
    }
    // if (!this.audioCodec) return;

    this.audioCodec.encode(pcmPacketData, this.payloadTypeAudio);
  }
  _onDevicePresent(){
    if (this.options.onDevicePresentFunc){
      this.options.onDevicePresentFunc();
    }
  }
  _onRecordReady(){
    if (this.audioRecorder && this.audioRecorder.hasMediaStream()){
      let bret = this.audioRecorder.startRecord();
      if (bret){
        this.audioRecordeSuccess = true;
        this._websocketInit();
      } else {
        this.audioRecorder = null;
        if (this.options.openAudioFail){
          this.options.openAudioFail('录音设备初始化失败');
        }
      }
    }
  }


  /**
   * 销毁mse
   * @private
   */
  _destroyMSE(){
    console.log('destroyMSE');
    if (this.mediaSource) {
      if (this.mediaSource.readyState === 'open') {
        try {
          this.sourceBuffer.abort();
        } catch (err) {
          console.log(err.message);
        }
      }

      if (this.mediaSource.readyState !== 'closed') {
        try {
          this.mediaSource.removeSourceBuffer(this.sourceBuffer);
        } catch (err) {
          console.log(err.message);
        }
      }

      if (this.mediaSource.readyState === 'open') {
        this.mediaSource.endOfStream();
      }

      /**
       * 注销sourceBuffer
       */
      if (this.sourceBuffer) {
        if (this.sourceBuffer.readyState !== undefined) {
          this.mediaSource.removeSourceBuffer(this.sourceBuffer);
        }

        this.sourceBuffer = null;
      }

      /**
       * 注销mediasource
       */
      this.mediaSource = null;

      /**
       * 清空其它数据
       */
      clearInterval(this.pendingRemoveRanges);
      this.pendingRemoveRanges = null;
    }

    /**
     * video标签清空src属性
     */
    if (this.mediaElement) {
      this.videoCurrentTime = this.mediaElement.currentTime;
      this.mediaElement.setAttribute('data-currenttime', this.videoCurrentTime);
      if (!this.mediaElement.getAttribute('data-video-stop')) {
        this.mediaElement.volume = 0;
        this.mediaElement.removeAttribute('src');
        this.mediaElement.removeAttribute('data-channel-up');
      }
      this.mediaElement = null;
    }

    if (this.mediaSourceObjectURL) {
      window.URL.revokeObjectURL(this.mediaSourceObjectURL);
      this.mediaSourceObjectURL = null;
    }
  }

  /**
   * 视频跳转到最新缓存时间点
   */
  _videoUpdateTime() {
    if (this.mediaElement && this.sourceBuffer) {
      if (!this.sourceBuffer.updating) {
        if (this.mediaSource.readyState === 'open'&&this.sourceBuffer.buffered.length>0) {
          this.mediaElement.currentTime = this.sourceBuffer.buffered.end(0);
        }
      }
    }

    if (this.sourceBuffer) {
      let buffered = this.sourceBuffer.buffered;
      if (buffered.length > 0) {
        let start = buffered.start(0);
        let end = buffered.end(0);
        try {
          this.sourceBuffer.remove(start, end - 10);
        } catch (err) {
          // console.warn(err.message);
        }
      }
    }
  }
  /**
   * 销毁事件
   * 关闭视频
   */
  _destroy () {

    this._destroyMSE();

    document.removeEventListener('visibilitychange', this._visibilitychangeFun.bind(this));


    this.messageState = false;
    this.isVideoPlay = false;
    this.pageFocus = true;
    this.timeOutMonitoring = null;

    /**
     * 清空音频数据
     * @type {Rtp}
     */
    this.rtp = new Rtp();


    if (this.heartbeat) {
      clearInterval(this.heartbeat);
      this.heartbeat = null;
    }

    this.startTime = 0;

    if (this.freeTimeout) {
      clearTimeout(this.freeTimeout);
      this.freeTimeout = null;
    }

    // 销毁音频
    this.closeVideoVoice();

    this.isPFrame = 0;
    this.videoCurrentTime = 0;

    if (this.options.panoramaType == 1){
      this._context360.material = null;
      this._context360.geometry = null;
      this._context360.mesh = null;
      this._context360.camera = null;
      this._context360.scene = null;
      this._context360.renderer = null;
      if (this._context360.container) {
        this._context360.container.remove();
        this._context360.container = null;
      }
    }

    this._closeRtc();
  }
  // 关闭socket
  _closeSocket (message='') {
    console.log('video socket colose:'+message);
    if (this.websocket) {
      this.streamInfo.message = message;
      this.websocket.close();
      this.websocket = null;

      this.socketState = false;
      clearTimeout(this.timeOutMonitoring);
      clearInterval(this.pendingRemoveRanges);
      this.pendingRemoveRanges = null;
      this._destroy();
    }
  }
  _onMessage(data){
    console.log('_onMessage:'+data);
    let message = JSON.parse(data);
    if (message && message.data && message.data.msgHead && message.data.msgHead.msgID) {
      // rtc answer
      if (message.data.msgHead.msgID == 50103){
        this._onServerAnswer(message.data.msgBody);
      }
      // rtc candidate
      if (message.data.msgHead.msgID == 50104){
        this._onServerCandidate(message.data.msgBody);
      }
    }
    if (this.options.onMessage){
      this.options.onMessage(this.options.data, data)
    }
  }



  // 发送业务指令
  sendMessage(code, values){
    if (this.websocket&&this.websocket.readyState == 1){
      let message = {};
      message.data = {};
      message.data.msgHead = {};
      message.data.msgHead.msgID = code;
      message.data.msgBody = {};
      message.data.msgBody = values;
      // this.streamInfo.sendBytes += senddata.length;
      this.websocket.send(JSON.stringify(message));
      return true;
    } else {
      console.warn('Miss websocket');
      return false;
    }
  }

  /**
   * 播放
   * @param setting
   */
  play(setting){
    console.log(setting);
    if (this._isPlayback){
      let hour = parseInt(setting.startTime.substr(6,2));
      let minute = parseInt(setting.startTime.substr(8,2));
      let second = parseInt(setting.startTime.substr(10,2));
      this._playbackStartTime = hour*60*60+minute*60+second;
      console.log('play-'+this._playbackStartTime+' - '+setting.startTime);
    }
    // let values = {};
    // if (setting){
    //   values = setting;
    // } else {
    //   values.vehicleId = '1';                     // 车辆ID
    //   values.simcardNumber  = '14875300001';      // sim卡号
    //   values.channelNumber  = '3';                // 通道号
    //   values.sampleRate  = '8000';                // 采样率
    //   values.channelCount  = '1';                 // 声道数
    //   values.audioFormat  = 'g726-32k';           // 编码格式
    //   values.playType = this.options.playType;    // 播放类型 实时 REAL_TIME，回放 TRACK_BACK，对讲 BOTH_WAY，监听 UP_WAY，广播 DOWN_WAY
    //   values.dataType = 0;                        // 数据类型0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
    //   values.userID = 123;                        // 用户ID
    //   values.deviceID = 123;                      // 终端ID
    //   values.streamType = 1;                      // 码流类型0：主码流，1：子码流
    // }
    setting.rawPackage = '1';
    if (this.audioCodec){
      this.audioCodec.setAudioFormat(setting.audioFormat);
    }
    this.sendMessage(50001, setting);
  }

  /**
   * 暂停
   */
  pause(){
    this.paused = true;
    if (this.mediaElement && this.sourceBuffer) {
      this.mediaElement.currentTime = this.sourceBuffer.buffered.end(0);
    }
    if (this.options.commandEnabled){

    }
  }

  /**
   * 恢复播放
 */
  resume(){
    this.paused = false;
  }
  stop(){
    this._closeSocket('call stop');
  }
  // 关闭socket
  closeSocket (message='') {
    this._closeSocket('call stop '+message);
  }


  /**
   * 开启声音
   */
  openVideoVoice() {
    this.firstAudioPlayData = true;
    this.audioEnabled = true;
  }
  /**
   * 关闭声音
   */
  closeVideoVoice() {
    if (this.audioCodec !== null){
      this.audioCodec.destroy();
      this.audioCodec = null;
    }
    if (this.pcmPlayer !== null) {
      this.pcmPlayer.close();
      this.pcmPlayer = null;
    }
    if (this.audioRecorder){
      this.audioRecorder.stopRecord();
      this.audioRecorder = null;
    }
    this.audioEnabled = false;
  }

  /**
   * 设置音频的播放声音
   */
  setAudioVoice(index) {
    if (this.pcmPlayer !== null) {
      this.pcmPlayer.setVolume(index);
    }
  }

  /**
   * 空闲超时断开
   */
  freeTimeoutClose(time, callBack) {
    this.closeFreeTimeout();
    var that = this;
    if (that.websocket) {
      that.freeTimeout = setTimeout(() => {
        if (callBack && typeof callBack === 'function') {
          var id = that.mediaElement.getAttribute('vehicle-id');
          var channelType = that.mediaElement.getAttribute('channel-type');
          var channelNum = that.mediaElement.getAttribute('channel-num');
          callBack(id, channelNum, channelType);
        }
        that._closeSocket("free timeout");
      }, time);
    }
  }

  /**
   * 关闭空闲超时
   */
  closeFreeTimeout() {
    if (this.isOpenFreeTime()) {
      clearTimeout(this.freeTimeout);
      this.freeTimeout = null;
    }
  }

  /**
   * 判断是否触发了空闲超时
   */
  isOpenFreeTime() {
    return this.freeTimeout !== null;
  }

  /**
   * 获取当前的播放时间，相对与开始播放时的时间(单位:s)
   */
  currentTime() {
    if (this.mediaElement){
      let ctime =this._playbackStartTime+this.mediaElement.currentTime;
      let hour = Math.floor(ctime / 60 / 60);
      let minute = Math.floor(ctime / 60) - hour*60;
      let seconde = Math.floor(ctime) - hour*60*60-minute*60;
      console.log(this.options.url+' - '+hour+':'+minute+':'+seconde+'  '+this._playbackStartTime+'-'+this.mediaElement.currentTime);
      return this.mediaElement.currentTime;
    } else {
      return 0;
    }
  }
  /**
   * 获取视频流量值
   */
  getVideoTraffic () {
    return this.rtp.getFlow();
  }

  /**
   * 抓图
   * 使用canvas
   */
  videoScreenshots (canvasId, width, height) {
    /**
     * 创建canvasu节点
     * @type {Element}
     */
    if (this.mediaElement) {
      //创建canvasu节点
      const canvasElement = document.getElementById(canvasId);
      var canvasCtx = canvasElement.getContext('2d');
      //进行抓图
      canvasCtx.drawImage(this.mediaElement, 0, 0, width, height);

      //转换成图像
      var formData = new FormData();
      canvasElement.toBlob(function(blob){
        formData.append('file', blob);
      });
      return formData;
    }
  }

  /*     播放控制，指令下发        */
  /**
   * 切换码流
   * 0：主码流，1：子码流
   */
  cmdSwitchStream(streamType){
    let values = {};
    values.changeStreamType = streamType;
    values.closeVideoType = '0';
    values.control = '1';
    this.sendMessage(50002, values);
  }

  /**
   * 指令关闭终端
   */
  cmdCloseVideo(){
    let values = {};
    if (this._isPlayback){
      values.forwardOrRewind = '0';
      values.dragPlaybackTime = '000000000000';
      values.remote = '2';
      this.sendMessage(50002, values);
    } else {
      values.changeStreamType = '0';
      values.closeVideoType = '0';
      values.control = '0';
      if (this.options.playType == 'BOTH_WAY'){
        values.control = '4';
      }
      this.sendMessage(50002, values);
    }
  }

  /**
   * 指令关键帧播放
   */
  cmdIFramePlay(){
    if (this._isPlayback){
      let values = {};
      values.forwardOrRewind = '0';
      values.dragPlaybackTime = '000000000000';//'201120100000';
      values.remote = '6';
      this.sendMessage(50002, values);
      this._videoUpdateTime();                     // 跳转到最新时间
    }
  }  /**
   * 指令关键帧快退
   */
  cmdRewindPlay(rewindValue){
    if (this._isPlayback){
      let values = {};
      values.forwardOrRewind = rewindValue+'';
      values.dragPlaybackTime = '000000000000';//'201120100000';
      values.remote = '4';
      this.sendMessage(50002, values);
      this._videoUpdateTime();                     // 跳转到最新时间
    }
  }
  /**
   * 指令快进
   */
  cmdForwardPlay(forwardValue){
    if (this._isPlayback){
      let values = {};
      values.forwardOrRewind = forwardValue+'';
      values.dragPlaybackTime = '000000000000';//'201120100000';
      values.remote = '3';
      this.sendMessage(50002, values);
      this._videoUpdateTime();                     // 跳转到最新时间
    }
  }
  /**
   * 指令seek回放进度
   */
  cmdSeek(time){
    if (this._isPlayback){
      let hour = parseInt(time.substr(6,2));
      let minute = parseInt(time.substr(8,2));
      let second = parseInt(time.substr(10,2));
      this._playbackStartTime = hour*60*60+minute*60+second;
      console.log('seek-'+this._playbackStartTime+' - '+time);
    }

    if (this._isPlayback){
      let values = {};
      values.forwardOrRewind = '0';
      values.dragPlaybackTime = time;//'201120100000';
      values.remote = '5';
      this.sendMessage(50002, values);
      this._videoUpdateTime();                     // 跳转到最新时间
    }
  }

  /**
   * 指令静音
   */
  cmdMute(){
    let values = {};
    values.changeStreamType = '0';
    values.closeVideoType = '1';
    values.control = '0';
    this.sendMessage(50002, values);
  }

  /**
   * 指令暂停
   */
  cmdPause(){
    let values = {};
    if (this._isPlayback){
      values.forwardOrRewind = '0';
      values.dragPlaybackTime = '000000000000';
      values.remote = '1';
      this.sendMessage(50002, values);
    }
    else{
      values.changeStreamType = '0';
      values.closeVideoType = '0';
      values.control = '2';
      this.sendMessage(50002, values);
    }
  }
  /**
   * 指令恢复
   */
  cmdResume(){
    let values = {};
    if (this._isPlayback){
      values.forwardOrRewind = '0';
      values.dragPlaybackTime = '000000000000';
      values.remote = '0';
      this.sendMessage(50002, values);
    }
    else{
      values.changeStreamType = '0';
      values.closeVideoType = '0';
      values.control = '3';
      this.sendMessage(50002, values);
    }
  }


  /**
   * test
   * @param value
   * @param type
   * @param name
   */
  doSave(value, type, name) {
    var blob;
    if (typeof window.Blob == "function") {
      blob = new Blob([value], {type: type});
    } else {
      var BlobBuilder = window.BlobBuilder || window.MozBlobBuilder || window.WebKitBlobBuilder || window.MSBlobBuilder;
      var bb = new BlobBuilder();
      bb.append(value);
      blob = bb.getBlob(type);
    }
    var URL = window.URL || window.webkitURL;
    var bloburl = URL.createObjectURL(blob);
    var anchor = document.createElement("a");
    if ('download' in anchor) {
      anchor.style.visibility = "hidden";
      anchor.href = bloburl;
      anchor.download = name;
      document.body.appendChild(anchor);
      var evt = document.createEvent("MouseEvents");
      evt.initEvent("click", true, true);
      anchor.dispatchEvent(evt);
      document.body.removeChild(anchor);
    } else if (navigator.msSaveBlob) {
      navigator.msSaveBlob(blob, name);
    } else {
      location.href = bloburl;
    }
  }
}

RTPMediaPlayer.arr = [];
window.RTPMediaPlayer = RTPMediaPlayer;
//export default RTPMediaPlayer;