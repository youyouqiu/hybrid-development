
class Std
{
    constructor()
    {}

    static memmem(data1, data1Offset, data2)
    {
        for (let i = 0; i <= data1.byteLength - data2.byteLength - data1Offset; ++i)
        {
            let j = 0;
            for (; j != data2.byteLength; ++j)
            {
                if(data1[i + j + data1Offset] != data2[j])
                {
                    break;
                }
            }
            if(j >= data2.byteLength)
            {
                return i + data1Offset;
            }
        }
        return -1;
    }

    static memcmp(data1, data1Offset, data2)
    {
        for(let i = 0; i != data2.byteLength; ++i)
        {
            if(data1[i + data1Offset] != data2[i])
            {
                return -1;
            }
        }
        return 0;
    }

    static memcpy(data1, data1Offset, data2, data2Begin, data2End)
    {
        data1.set(data2.subarray(data2Begin, data2End), data1Offset);
    }

    static milliSecondTime()
    {
        return new Date().getTime();
    }

    static shortToFloatData(input)
    {
        let inputSamples = input.length;
        let output = new Float32Array(inputSamples);
        for(let i = 0; i != inputSamples; ++i)
        {
            output[i] = input[i] / 32768;
        }
        return output;
    }

    static floatToShortData(input)
    {
        let inputSamples = input.length;
        let output = new Int16Array(inputSamples);
        for(let i = 0; i != inputSamples; ++i)
        {
            output[i] = input[i] * 32768;
        }
        return output;
    }

    static downsampleBuffer(buffer, rate, sampleRate)
    {
        if(rate === sampleRate)
        {
            return buffer;
        }
        else if(rate > sampleRate)
        {
            throw "rate > sampleRate error !!";
        }
        let sampleRateRatio = sampleRate / rate;
        let newLength = Math.ceil(buffer.length / sampleRateRatio) & 0xFFFC;
        let result = new Float32Array(newLength);
        let offsetResult = 0;
        let offsetBuffer = 0;
        while (offsetResult !== result.length)
        {
            let nextOffsetBuffer = offsetBuffer + sampleRateRatio;
            let accum = 0;
            let count = 0;
            let currentOffset = Math.ceil(offsetBuffer);
            let currentNextOffset = Math.ceil(nextOffsetBuffer);
            for (let i = currentOffset; i !== currentNextOffset && i !== buffer.length; ++i)
            {
                accum += buffer[i];
                ++count;
            }
            result[offsetResult] = accum / count;
            ++offsetResult;
            offsetBuffer = nextOffsetBuffer;
        }
        return result;
    }

    static changeByteEndian(data)
    {
        let buffer = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
        for(let i = 0; i !== buffer.byteLength; ++i)
        {
            buffer[i] = ((buffer[i] & 0x0F) << 4) + ((buffer[i] >>> 4) & 0x0F);
        }
    }
}

class Result
{
    constructor(data, type, time, errorCode, duration = 20)
    {
        this.data = data;
        this.type = type;
        this.time = time;
        this.duration = duration;
        this.errorCode = errorCode;
    }

    static makeErrorResult(errorCode)
    {
        return new Result(null, -1, -1, errorCode);
    }     
}

Result.ErrorCode = class
{
    constructor() 
    {}
}

Result.ErrorCode.SUCCESS = 0;
Result.ErrorCode.PARAM_ERROR = 1000;
Result.ErrorCode.PARAM_ERROR_SPS_BEGIN = 1001;
Result.ErrorCode.PARAM_ERROR_SPS_END = 1002;
Result.ErrorCode.PARAM_ERROR_PPS_BEGIN = 1003;
Result.ErrorCode.PARAM_ERROR_PPS_END = 1004;
Result.ErrorCode.PARAM_ERROR_SPS_PARSE = 1005;
Result.ErrorCode.PARAM_ERROR_SIZE = 1006;
Result.ErrorCode.PARAM_ERROR_LENGTH = 1007;
Result.ErrorCode.PARAM_CHANGE = 2000;
Result.ErrorCode.FAIL = 3000;
Result.ErrorCode.NO_INIT_ERROR = Result.ErrorCode.FAIL + 1;
Result.ErrorCode.CACHE_MAX_ERROR = Result.ErrorCode.FAIL + 2;
Result.ErrorCode.DATA_DEFICIENT = 4001;

Result.ErrorCode.GetMessage = function(code){
    switch (code) {
        case 1001:
            return 'H264?????????????????????SPS??????';
        case 1003:
            return 'H264?????????????????????PPS??????';
        case 3001:
            return 'H264??????????????????';
        default:
            return code+'';
    }
}

Result.Type = class
{
    constructor()
    {}
}

Result.Type.H264_I_FRAME = 0;
Result.Type.H264_P_FRAME = 1;
Result.Type.H264_B_FRAME = 2;
Result.Type.AUDIO = 3;
Result.Type.TRANS_DATA = 4;
Result.Type.FMP4_HEAD = 5;
Result.Type.FMP4_BODY = 6;
Result.Type.RTP_PACKAGE_COMPLETE = 0;
Result.Type.RTP_PACKAGE_HEAD = 1;
Result.Type.RTP_PACKAGE_TAIL = 2;
Result.Type.RTP_PACKAGE_BODY = 3;

window.Result = Result;
window.Std = Std;