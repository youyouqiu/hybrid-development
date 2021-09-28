// 值

define(function() {
  var a = {
    var quickMonitorType = 0;//0:选择车,1:选择人,2:选择物
    // 第一次进页面默认查询的数据
    var vehicleInfoList = [];
    var peopleInfoList = [];
    var thingInfoList = [];
    var deviceInfoList = [];//终端信息集合
    var simCardInfoList = [];//终端手机号信息集合
    var speedDeviceInfoList = [];//极速录入终端信息集合
    var ais = [];//还能存入的分组id
    var orgId = "";
    var orgName = "";
    var flag1 = false; // 选择还是录入的车牌号
    var flag2 = true; // 选择还是录入的终端号
    var flag3 = true; // 选择还是录入的终端手机号
    var flag4 = false; // 极速 是否是选择的终端号
    var flag5 = true; // 极速 选择还是录入的监控对象
    var flag6 = true; // 极速 选择还是录入的终端手机号
    var hasFlag = true, hasFlag1 = true; // 是否有该唯一标识
    var quickRefresh = true;//快速录入信息是否刷新
    var fastRefresh = true;//极速录入信息是否刷新
    var processInput = processInputA.processInput;
  };
  return {
    a: a,
  }
})
