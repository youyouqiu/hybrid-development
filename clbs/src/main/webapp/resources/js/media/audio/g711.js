
class G711
{
    constructor(wasm, memory)
    {
        this._wasm = wasm;
        this._memory = new Uint8Array(memory.buffer);
    }

    // 100 ~ 10240：解码前音频  10240 ~ 20480：解码后音频
    decodeG711a(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 100);
        this._wasm.instance.exports._decodeG711a(10240, 100, data.byteLength);
        return new Int16Array(this._memory.buffer, 10240, data.byteLength);
    }

    // 100 ~ 10240：解码前音频  10240 ~ 20480：解码后音频
    decodeG711u(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 100);
        let outLen = this._wasm.instance.exports._decodeG711u(10240, 100, data.byteLength);
        return new Int16Array(this._memory.buffer, 10240, data.byteLength);
    }

    // 20480 ~ 30960：编码前音频  30960 ~ 41920：编码后音频
    encodeG711a(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 20480);
        this._wasm.instance.exports._encodeG711a(30960, 20480, data.byteLength / 2);
        return new Uint8Array(this._memory.buffer, 30960, data.byteLength/2);
    }

    // 20480 ~ 30960：编码前音频  30960 ~ 41920：编码后音频
    encodeG711u(data)
    {
        this._memory.set(new Uint8Array(data.buffer, data.byteOffset, data.byteLength), 20480);
        this._wasm.instance.exports._encodeG711u(30960, 20480, data.byteLength / 2);
        return new Uint8Array(this._memory.buffer, 30960, data.byteLength/2);
    }
}

window.G711 = G711;