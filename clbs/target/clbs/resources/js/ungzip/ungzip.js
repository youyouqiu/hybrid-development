/**
 * 解压缩字符串
 *
 * @param zipData
 *            经过 gzip压缩和base64编码的字符串
 * @param callback
 *            回调函数 用解压缩后的数据进行处理后续操作
 * @author wangying
 */
function ungzip(zipData) {
  try {
    var punzipstr = zipData;
    // punzipstr =decodeURIComponent(punzipstr);
    var restored = pako.inflate(punzipstr, {to: 'string'}); // 解 压
  } catch (err) {
    alert('error');
    return;
  }
  return restored;

  /*	var strData     = atob(zipData);

   // Convert binary string to character-number array
   var charData    = strData.split('').map(function(x){return x.charCodeAt(0);});


   // Turn number array into byte-array
   var binData     = new Uint8Array(charData);


   // // unzip
   var data        = pako.inflate(binData);


   // Convert gunzipped byteArray back to ascii string:
   strData     = String.fromCharCode.apply(null, new Uint16Array(data));
   return strData;*/

}


function unzip(b64Data) {
  var binaryString = pako.gzip(b64Data, {to: 'string'});

  return binaryString;

  /*var strData = atob(b64Data);
   //
   // Convert binary string to character-number array
   var charData = strData.split('').map(function(x){return x.charCodeAt(0);});


   // Turn number array into byte-array
   var binData = new Uint8Array(charData);


   // // unzip
   var data = pako.inflate(binData);


   // Convert gunzipped byteArray back to ascii string:
   strData = String.fromCharCode.apply(null, new Uint16Array(data));
   return strData;*/
}