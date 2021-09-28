
class AACDecoder
{
    constructor()
    {
        //this._flush = this._flush.bind(this);
        this._audioCtx = new (window.AudioContext || window.webkitAudioContext)();

        this._samplesBuffer = new ArrayBuffer();
        this._packagesCount = 0;


        this._lastPackage = new ArrayBuffer();

    };

    close()
    {
        this._audioCtx.close();
    };

    concat(buf1, buf2)
    {
        let tmpRawBuffer = new Uint8Array(buf1.byteLength + buf2.byteLength);
        tmpRawBuffer.set(new Uint8Array(buf1), 0);
        tmpRawBuffer.set(new Uint8Array(buf2), buf1.byteLength);
        return tmpRawBuffer.buffer;
    };
    concat2(buf1, buf2, buf3)
    {
        let tmpRawBuffer = new Uint8Array(buf1.byteLength + buf2.byteLength + buf3.byteLength);
        tmpRawBuffer.set(new Uint8Array(buf1), 0);
        tmpRawBuffer.set(new Uint8Array(buf2), buf1.byteLength);
        tmpRawBuffer.set(new Uint8Array(buf3), buf1.byteLength+buf2.byteLength);
        return tmpRawBuffer.buffer;
    };

    decode(data, callback) {
        if(!data || data.byteLength<=0) return;


        this._samplesBuffer = this.concat(this._samplesBuffer, data);
        this._packagesCount ++;
        if(this._packagesCount<8) return;


        if(this._lastPackage && this._lastPackage.byteLength > 0)
        {
            this._samplesBuffer = this.concat(this._lastPackage, this._samplesBuffer);
        }

        //this._lastPackage = data;
        let decodeData = this._samplesBuffer;
        this._samplesBuffer = new ArrayBuffer();
        this._packagesCount=0;

        // console.log('append decode data');
        this._audioCtx.decodeAudioData(decodeData, function(decodeBuffer){
                // console.log('decode data ok');
                if (callback){
                    let audioData = decodeBuffer.getChannelData(0);
                    //let audioData2 = audioData.slice(512*4);
                    let pcmFloat32Data = Std.downsampleBuffer(audioData, 16000, decodeBuffer.sampleRate);
                    callback(pcmFloat32Data, 16000, decodeBuffer.numberOfChannels);
                }
            }.bind(this),
            function(e){ // only on error attempt to sync on frame boundary
                console.log("Error with decoding audio data：" + e.message);
            }.bind(this));

    };


}

window.AACDecoder = AACDecoder;
