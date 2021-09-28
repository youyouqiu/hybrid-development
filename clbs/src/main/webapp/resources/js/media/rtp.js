
class Rtp
{
  constructor()
  {
    this._currentType = -1;             // 数据类型
    this._currentPackType = -1;         // 分包标记
    this._currentTime = -1;             // 直接记录8字节大端序数组
    this._numberRecv = -1;              // 收到的rtp包号
    this._duration = -1;
    this._simNumber = null;
    this._channel = null;
    this._number = 0;
    this._t = -1;
    this.isCompleted = 0;
    this.payloadType = 0;
    this.flow = 0;

    this._buffer = new Uint8Array();        // 未合包的数据缓存
    this._bufferVideo = new Uint8Array();   // 已解析未完整的视频帧缓存
    this._bufferAudio = new Uint8Array();   // 已解析未完整的音频帧缓存
  }

  getFlow()
  {
    return this.flow;
  }

  getPayloadType() {
    return this.payloadType;
  }

  appendFreeBuffer(data){
    this._buffer = this.appendBuffer(this._buffer, data);
    return this._buffer;
  }
  appendBuffer(buffer, data){
    if (buffer&&buffer.byteLength>0){
      let tmp = new Uint8Array(buffer.byteLength + data.byteLength);
      tmp.set(buffer, 0);
      tmp.set(new Uint8Array(data), buffer.length);
      buffer = tmp;
    } else {
      buffer = new Uint8Array(data);
    }
    return buffer;
  }

  findrtpHead(data){
    let start = Std.memmem(data, 0, new Uint8Array([0x30,0x31,0x63,0x64]));
    return start;
  }

  parse(frameData)
  {
    // rtp 包查找
    let data = this.appendFreeBuffer(frameData);
    let rtpIndex = this.findrtpHead(data);
    if (rtpIndex != 0){
      console.warn('error rtp index.');
      if (rtpIndex<0){
        this._buffer =  this._buffer.slice(this._buffer.length-4);
      } else {
        this._buffer =  this._buffer.slice(rtpIndex);
      }
    }
    if (this._buffer.length<30){
      return Result.makeErrorResult(Result.ErrorCode.DATA_DEFICIENT);
    }
    data = this._buffer;

    // chuli
    this._currentType = (data[15] & 0xf0) >>> 4;
    this._currentPackType = (data[15] & 0x0f);
    if(this._currentType > 4 || data.byteLength < 12 || data.byteLength > 5000000)
    {
      return Result.makeErrorResult(Result.ErrorCode.FAIL);
    }

    if(this._simNumber == null)
    {
      this._simNumber = new Uint8Array(6);
      this._simNumber.set(data.slice(8, 8 + 6));
      this._channel = data[14];
    }

    this.isCompleted = data[5] >>> 7;
    this.payloadType = data[5] & 0x7f;
    this._numberRecv = data[6] << 8 | data[7];

    let frameHeadLen = 30 - (this._currentType === Result.Type.AUDIO ? 4 : (this._currentType === Result.Type.TRANS_DATA ? 12 : 0));
    let timeIndex = 16;
    this._currentTime = (data[timeIndex+5] << 16) + (data[timeIndex+6] << 8) + data[timeIndex+7] +
        data[timeIndex+4] * 16777216 + data[timeIndex+3] * 4294967296 + data[timeIndex+2] * 1099511627776;
    // this._currentTime = data.slice(16,24);

    this._duration = this._currentType < 3 ? (data[frameHeadLen - 4] << 8) + data[frameHeadLen - 3] : 0;
    let dataLen = data.byteLength;
    let offset = 0;
    let trueLen = 0;
    let rtpLen = 0;
    let resultData = new Uint8Array(data.byteLength);
    let isComplete = false;
    while(true)//(rtpLen <= 950 && offset < dataLen)
    {
      if (dataLen-offset-frameHeadLen < 30) {
        return Result.makeErrorResult(Result.ErrorCode.DATA_DEFICIENT);
      }
      rtpLen = (data[offset + frameHeadLen - 2] << 8) + data[offset + frameHeadLen - 1];
      if (dataLen-offset-frameHeadLen < rtpLen) {
        return Result.makeErrorResult(Result.ErrorCode.DATA_DEFICIENT);
      }


      resultData.set(data.subarray(offset + frameHeadLen, offset + frameHeadLen + rtpLen), trueLen);
      trueLen += rtpLen;

      let rtpNumber = data[offset+6] << 8 | data[offset+7];
      let packtype = data[offset+15] & 0x0f;
      let curentType = (data[offset+15] & 0xf0) >>> 4;
      offset += rtpLen + frameHeadLen;
      // curentType - 0=I,1=P,2=B,3=A,4=T
      // packtype - 0=原子包，1=开始，2=结束，3=中间
      // console.log('rtpNumber='+rtpNumber+',packtype='+packtype+',curentType='+curentType+',rtpLen='+rtpLen);
      // 包类型变了
      if (curentType != this._currentType){
        console.warn('Different types');
        break;
      }
      // 当前包完整了
      if (packtype == 0 || packtype == 2){
        isComplete = true;
        break;
      }
    }
    this.flow += frameData.byteLength;

    this._buffer = this._buffer.slice(offset);

    // 输出缓存
    let outputBuffer = null;
    if(this._currentType === Result.Type.H264_I_FRAME ||
        this._currentType === Result.Type.H264_P_FRAME ||
        this._currentType === Result.Type.H264_B_FRAME) {
      outputBuffer = this._bufferVideo;
      this._bufferVideo = new Uint8Array();
    } else {
      outputBuffer = this._bufferAudio;
      this._bufferAudio = new Uint8Array();
    }
    outputBuffer = this.appendBuffer(outputBuffer, resultData.subarray(0, trueLen));


    if (isComplete){
      // console.log('number = '+this._numberRecv+', frame = '+(this._currentType === Result.Type.H264_I_FRAME?'I':this._currentType === Result.Type.H264_P_FRAME?'P':this._currentType === Result.Type.H264_B_FRAME?'B':'Unknow '+this._currentType)
      //     +', package = '+(this._currentPackType==Result.Type.RTP_PACKAGE_HEAD?'Head':this._currentPackType==Result.Type.RTP_PACKAGE_BODY?'Body':this._currentPackType==Result.Type.RTP_PACKAGE_BODY?'Body':this._currentPackType==Result.Type.RTP_PACKAGE_COMPLETE?'Complete':'Unknow '+this._currentPackType));
      return new Result(outputBuffer, this._currentType, this._currentTime, Result.ErrorCode.SUCCESS, this._duration);
    } else {
      if(this._currentType === Result.Type.H264_I_FRAME ||
          this._currentType === Result.Type.H264_P_FRAME ||
          this._currentType === Result.Type.H264_B_FRAME) {
        this._bufferVideo = outputBuffer;
      } else {
        this._bufferAudio = outputBuffer;
      }
      return Result.makeErrorResult(Result.ErrorCode.DATA_DEFICIENT);
    }


  }

  makeAudio(data, time, payloadType = -1, simNumber = null)
  {
    if(payloadType === -1)
    {
      payloadType = this.payloadType;
    }
    if(this._simNumber == null && simNumber == null)
    {
      return Result.makeErrorResult(Result.ErrorCode.NO_INIT_ERROR);
    }
    let simNumberData = this._simNumber;
    if(simNumber != null && simNumber.byteLength === 12)
    {
      simNumberData = new Uint8Array(6);
      for(let i = 0; i !== 12; ++i)
      {
        simNumberData[i / 2] = ((simNumber[i] << 4) & 0xF0) + (simNumberData[i + 1] & 0x0F)
      }
    }
    let packetLen = Math.ceil(data.byteLength / 950.0);
    let rtpData = new Uint8Array(data.byteLength + packetLen * 26);
    let currentOffset = 0;
    for(let i = 0; i !== packetLen; ++i)
    {
      let partLen = i === packetLen - 1 ? data.byteLength % 950 : 950;
      let partType = packetLen === 1 ? 0x00 : (i === 0 ? 0x01 : (i === packetLen - 1 ? 0x02 : 0x03));
      rtpData.set(this._makeHead(0x03, partLen, partType, time, payloadType, simNumberData), currentOffset);
      this._number = (this._number + 1) & 0xFFFF;
      currentOffset += 26;
      rtpData.set(data.slice(i * 950, i * 950 + partLen), currentOffset);
      currentOffset += partLen;
    }
    return new Result(rtpData, Result.Type.AUDIO, 0, Result.ErrorCode.SUCCESS, 0);
  }

  _makeHead(type, partLen, partType, time, payloadType, simNumber)
  {
    if (this._t<=0){
      this._t = Std.milliSecondTime();
    } else {
      this._t += 40;  // 每次320采样
    }
    time = this._t;
    // 只用了6字节数据（time长度不定，一次位移1字节）
    let timeArr = [
      (time >>> 8 >>> 8 >>> 8 >>> 8 >>> 8) & 0xFF,
      (time >>> 8 >>> 8 >>> 8 >>> 8) & 0xFF,
      (time >>> 8 >>> 8 >>> 8) & 0xFF,
      (time >>> 8 >>> 8) & 0xFF,
      (time >>> 8) & 0xFF,
      (time) & 0xFF,
    ];

    return new Uint8Array([
      0x30, 0x31, 0x63, 0x64,
      0x81, (this.isCompleted << 7) | payloadType,
      (this._number >>> 8) & 0xFF,
      this._number & 0xFF,
      simNumber[0], simNumber[1],
      simNumber[2], simNumber[3],
      simNumber[4], simNumber[5], this._channel,
      (type << 4) + (partType & 0x0F),
      0x00, 0x00, timeArr[0], timeArr[1],
      timeArr[2], timeArr[3], timeArr[4], timeArr[5],
      (partLen >>> 8) & 0xFF, partLen & 0xFF
    ]);
  }
}

window.Rtp = Rtp;