// 值

define(function() {
  var a = {
    var cid = '';
    var brandValB;
    var deviceValB;
    var simValB;
    var groupingNum = 2;
    var people = 2;
    var intervalFlag = true;
    var id = 2;
    var enterFlag = false;
    var flag1 = false; // 选择还是录入的车牌号
    var flag2 = true; // 选择还是录入的终端号
    var flag3 = true; // 选择还是录入的终端手机号
    var datas;
    var objType = 0;
    // 第一次进页面默认查询的数据
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = [];
    var deviceInfoListForPeople = [];
    var simCardInfoList = [];
    var professionalsInfoList = [];
    var professionalDataList;
    var orgId = "";
    var orgName = "";
    var terminalManufacturerInfoList = []; // 终端厂商信息
    var processSubmitFlag = true;// 防止表单重复提交
    var initFlag = true;// 第一次进入页面标识
    var agreementType; //协议类型
    var publicFun = processPublicFun.publicFun;
  };

  return {
    a: a,
  }
})
