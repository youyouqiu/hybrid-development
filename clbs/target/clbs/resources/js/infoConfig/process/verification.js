// 公共验证函数

define(function() {
  var verification = {
    //流程验证
    validates: function () {
      return $("#addForm1").validate({
        // ignore: [],
        rules: {
          car_groupName: {
            required: true
          },
          device_pgroupName: {
            required: true
          },
          car_groupId: {
            required: true
          },
          peo_groupId: {
            required: true
          },
          thing_groupId: {
            required: true
          },
          device_groupId: {
            required: true
          },
          sim_groupId: {
            required: true
          },
          vehicleNumber: {
            required: false,
            maxlength: 20
          },
          vehicleOwner: {
            checkPeopleName: true
          },
          vehicleOwnerPhone: {
            isLandline: true
          },
          aliases: {
            maxlength: 20
          },
          vehicleType: {
            required: true
          },
          chassisNumber: {
            required: false,
            maxlength: 50
          },
          engineNumber: {
            required: false,
            maxlength: 20
          },
          plateColor: {
            required: false
          },
          areaAttribute: {
            required: false
          },
          city: {
            required: false
          },
          province: {
            required: false
          },
          fuelType: {
            required: false
          },
          citySel: {
            required: true
          },
          //人员验证
          peo_groupName: {
            required: true
          },
          name: {
            checkPeopleName: true
          },
          identity: {
            isIdCardNo: true,
          },
          phone: {
            isTel: true
          },
          email: {
            email: true,
            maxlength: 20
          },
          // 终端验证
          device_groupName: {
            required: true
          },
          deviceName: {
            required: false,
            maxlength: 50
          },
          deviceType: {
            required: true
          },
          functionalType: {
            required: true
          },
          isVideo: {
            required: false
          },
          barCode: {
            required: false,
            maxlength: 64
          },
          channelNumber: {
            required: false
          },
          isStart: {
            required: true
          },
          manuFacturer: {
            required: false,
            maxlength: 100
          },
          terminalManufacturer: {
            required: true
          },
          terminalType: {
            required: true
          },
          // sim验证
          realId: {
            digits: true,
            minlength: 7,
            maxlength: 20
          },
          sim_groupName: {
            required: true
          },
          isStart: {
            required: false,
            maxlength: 6
          },
          operator: {
            required: false,
            maxlength: 50
          },
          openCardTime: {
            required: false
          },
          capacity: {
            required: false,
            maxlength: 20
          },
          simFlow: {
            required: false,
            maxlength: 20
          },
          useFlow: {
            maxlength: 20
          },
          alertsFlow: {
            required: false,
            maxlength: 20
          },
          endTime: {
            required: false,
            // compareDate: "#openCardTime"
          },
          correctionCoefficient: {
            required: false,
            isRightNumber: true,
            isInt1tov: 200,
          },
          forewarningCoefficient: {
            required: false,
            isRightNumber: true,
            isInt1tov: 200,
          },
          monthThresholdValue: {
            isFloat: true
          },
          hourThresholdValue: {
            range: [0, 6553]
          },
          dayThresholdValue: {
            range: [0, 429496729]
          },
          monthThresholdValue: {
            range: [0, 429496729]
          },
          iccid: {
            checkICCID: true
          },
          imsi: {
            required: false,
            maxlength: 50
          },
          imei: {
            required: false,
            maxlength: 20
          },
          dueDate: {
            compareDate: "#billingDate"
          },
          //物品校验
          thingName: {
            maxlength: 20
          },
          thingModel: {
            maxlength: 20
          },
          thingManufacturer: {
            maxlength: 20
          },
          groupPid: {
            required: true
          },
          thing_groupName: {
            required: true
          }
        },
        messages: {
          thing_groupName: {
            required: '所属企业不能为空'
          },
          groupPid: {
            required: '请选择分组'
          },
          car_groupName: {
            required: '所属企业不能为空'
          },
          device_pgroupName: {
            required: '所属企业不能为空'
          },
          sim_groupName: {
            required: '所属企业不能为空'
          },
          device_groupId: {
            required: '请选择所属企业'
          },
          realId: {
            digits: '请输入数字，范围：7~20位',
            minlength: '请输入数字，范围：7~20位',
            maxlength: '请输入数字，范围：7~20位'
          },
          sim_groupId: {
            required: '请选择所属企业'
          },
          car_groupId: {
            required: '请选择所属企业'
          },
          peo_groupId: {
            required: '请选择所属企业'
          },
          thing_groupId: {
            required: '请选择所属企业'
          },
          vehicleNumber: {
            required: vehicleBrandNull,
            maxlength: vehicleNumberMaxlength
          },
          vehicleOwner: {
            checkPeopleName: "只能输入最多8位的中英文字符"
          },
          vehicleOwnerPhone: {
            isLandline: telPhoneError
          },
          aliases: {
            maxlength: vehicleAlisasMaxlength
          },
          vehicleType: {
            required: '请选择车辆类型'
          },
          chassisNumber: {
            required: vehiclChassisNumberNull,
            maxlength: vehicleChassisMaxlength
          },
          engineNumber: {
            required: vehicleEngineNumber,
            maxlength: vehicleEngineNumberMaxlength
          },
          plateColor: {
            required: vehiclePlateColorNull
          },
          areaAttribute: {
            required: vehicleAreaAttributeNull
          },
          city: {
            required: vehicleCityNull
          },
          province: {
            required: vehicleProvinceNull
          },
          fuelType: {
            required: vehicleFuelTypeNull
          },
          citySel: {
            required: groupNameNull
          },
          // 人员验证
          peo_groupName: {
            required: '所属企业不能为空'
          },
          name: {
            checkPeopleName: "只能输入最多8位的中英文字符"
          },
          identity: {
            isIdCardNo: "请输入正确的身份证号",
          },
          phone: {
            isTel: telPhoneError
          },
          email: {
            email: "邮件格式错误",
            maxlength: publicSize20
          },
          // 终端验证
          device_groupName: {
            required: deviceGroupNameNul
          },
          deviceName: {
            required: deviceNumberNull,
            maxlength: deviceNameMaxlength
          },
          deviceType: {
            required: deviceTypeNull
          },
          functionalType: {
            required: "功能类型不能为空"
          },
          isVideo: {
            required: deviceIsVideoNull
          },
          barCode: {
            required: deviceBarCodeNull,
            maxlength: deviceBarCodeMaxlength
          },
          channelNumber: {
            required: deviceChannelNumberNull
          },
          isStart: {
            required: deviceIsStartNull
          },
          manuFacturer: {
            required: deviceManuFacturerNull,
            maxlength: deviceManuFacturerMaxlength
          },
          terminalManufacturer: {
            required: "请选择终端厂商"
          },
          terminalType: {
            required: "请选择终端型号"
          },
          isStart: {
            required: "不能为空",
            maxlength: "长度不超过6位"
          },
          operator: {
            required: "不能为空",
            maxlength: "长度不超过50位"
          },
          openCardTime: {
            required: "不能为空",
          },
          capacity: {
            required: "不能为空",
            maxlength: "长度不超过20位"
          },
          simFlow: {
            required: "不能为空",
            maxlength: "长度不超过20位"
          },
          useFlow: {
            maxlength: "长度不超过20位"
          },
          alertsFlow: {
            required: "不能为空",
            maxlength: "长度不超过20位"
          },
          endTime: {
            required: "不能为空",
            compareDate: "到期时间要大于激活日期"
          },
          forewarningCoefficient: {
            isRightNumber: "请输入正整数",
            isInt1tov: "超过了系数范围，请输入1到200的整数",
          },
          monthThresholdValue: {
            isFloat: "请输入数字"
          },
          correctionCoefficient: {
            isRightNumber: "请输入正整数",
            isInt1tov: "超过了系数范围，请输入1到200的整数",
          },
          hourThresholdValue: {
            range: "输入的数字必须在0-6553之间"
          },
          dayThresholdValue: {
            range: "输入的数字必须在0-429496729之间"
          },
          monthThresholdValue: {
            range: "输入的数字必须在0-429496729之间"
          },
          iccid: {
            checkICCID: "请输入20位的数字或大写字母"
          },
          imsi: {
            maxlength: "长度不超过50位"
          },
          imei: {
            maxlength: "长度不超过20位"
          },
          //物品校验
          thingName: {
            maxlength: publicSize20
          },
          thingModel: {
            maxlength: publicSize20
          },
          thingManufacturer: {
            maxlength: publicSize20
          }
        }
      }).form();
    },
  };
  return {
    verification: verification,
  }
})
