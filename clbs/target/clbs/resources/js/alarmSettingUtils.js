(function (window, $) {
    let key = '' //当前标签的id
    let currentMonitorId = '' // 当前车辆id
    let currentMonitorName = '' // 当前车辆名称
    let currentTabId = ''
    let IOSetting = {} // 选择参考对象时不复制设置，所以用该变量保存上一个设置的值
    let lastHigh
    const util = {
        create: function (className, elmentName = 'div') {
            const el = document.createElement(elmentName)
            className && el.classList.add(className)
            return el
        },
        addClass: function (el, className) {
            el.classList.add(className)
            return el
        },
        deepClone: function(target){
            let res = null
            if(target == null){
                return null
            }
            if(typeof target != 'object'){
                return target
            }
            const type = Object.prototype.toString.call(target)
            if(type == "[object Array]"){
                res = []
                target.forEach(item => res.push(util.deepClone(item)))
            }
            if(type == "[object Object]"){
                res = {}
                for(const i in target){
                    if(target.hasOwnProperty(i)){
                        res[i] = util.deepClone(target[i])
                    }
                }
            }
            if(type == "[object RegExp]"){
                res = new RegExp(target)
            }
            return res
        }
    }
    window.config = null
    let configWithDefaultValue = null // 默认值
    window.originConfig = null // 原始config副本

    // 报警渲染
    class Item {
        constructor(item, sliderNum, index) {
            Object.assign(this, item)
            this.sliderValue = item.alarmPush
            this.alarmSettingType = item.alarmSettingType
            this.alarmSettingName = item.name
            this.slider = new Item_Slider({
                initPos: item.alarmPush,
                nodeNumber: sliderNum ? sliderNum : 4,
                space: 50,
                onSlide:(idx, alarmSettingType) => {
                    this.sliderValue = idx
                    const s = config[index].data.find(item => item.alarmSettingType == alarmSettingType )
                    if(s){
                        s.alarmPush = idx
                    }
                },
                alarmSettingType: item.alarmSettingType
            })
            this.title = new Item_Title(item.name, this.slider)
            this.rightAll = new Item_Right(item.right || [])
        }
        getNode(){
            const box = util.create('box1')
            box.appendChild(this.slider.getNode())
            box.appendChild(this.title)
            box.appendChild(this.rightAll.getNode())
            return box
        }
        validate(){
            return this.rightAll.validate()
        }
    }
    // 滑块类
    class Item_Slider {
        constructor({initPos = -1, nodeNumber = 4, space = 50, onSlide = () => {}, alarmSettingType}) {
            this.nodeNumber = nodeNumber
            this.currentPos = initPos
            this.space = space
            this.onSlide = onSlide
            this.alarmSettingType = alarmSettingType

            this.dotBig = document.createElement('span')
            this.dotBig.classList.add('slider-big-dot')
            if(nodeNumber == 3){
                this.dotBig.style.left = (initPos * space) + 'px'
            }else {
                this.dotBig.style.left = ((initPos + 1) * space) + 'px'
            }
            this.dotBig.style.transform = 'translateX(-50%)'
        }
        eventHandler(e){
            if(e.target.classList.contains('small-dot')){
                const idx = e.target.getAttribute('data-index')
                this.slideTo(idx)
            }
        }
        getNode() {
            const all = document.createElement('div')
            all.classList.add('slider-box')
            all.style.width = (this.nodeNumber - 1) * this.space + 'px'
            const line = document.createElement('div')
            line.classList.add('slider-line')
            for (let i = 0; i < this.nodeNumber; i++) {
                const dotSmall = document.createElement('span')
                dotSmall.setAttribute('data-index', i)
                dotSmall.classList.add('small-dot')
                line.appendChild(dotSmall)
            }
            line.appendChild(this.dotBig)
            line.addEventListener('click', this.eventHandler.bind(this))
            all.appendChild(line)
            return all
        }
        next() {
            if(this.nodeNumber == 3){
                if(this.currentPos < this.nodeNumber - 1){
                    this.currentPos = this.currentPos + 1
                    this.onSlide(this.currentPos, this.alarmSettingType)
                    this.dotBig.style.left = (this.currentPos) * this.space + 'px'
                }else{
                    this.currentPos = 0
                    this.onSlide(0, this.alarmSettingType)
                    this.dotBig.style.left = '0px'
                }
            }else{
                if(this.currentPos < this.nodeNumber - 2){
                    this.currentPos = this.currentPos + 1
                    this.onSlide(this.currentPos, this.alarmSettingType)
                    this.dotBig.style.left = (this.currentPos + 1) * this.space + 'px'
                }else{
                    this.currentPos = -1
                    this.onSlide(-1, this.alarmSettingType)
                    this.dotBig.style.left = '0px'
                }
            }
        }
        slideTo(index) {
            index = Number(index)
            if(index < this.nodeNumber && index >= 0){
                if(this.nodeNumber == 3){
                    this.onSlide(index, this.alarmSettingType)
                }else{
                    this.onSlide(index - 1, this.alarmSettingType)
                }
                this.currentPos = index
                this.dotBig.style.left = index * this.space + 'px'
            }
        }
    }
    // 左侧标题
    class Item_Title {
        constructor(name, slider) {
            const div = document.createElement('div')
            div.classList.add('middle-box')
            const label = document.createElement('label')
            label.classList.add('label-title')
            label.innerHTML = name
            label.addEventListener('click', function(){
                slider.next()
            })
            div.appendChild(label)
            return div
        }
    }
    // 右侧数据
    class Item_Right {
        constructor(rightItems) {
            this.divBox = util.create('rightBox')
            this.items = []
            rightItems.forEach(rightItem => {
                this.divBox.appendChild(this.renderItemRight(rightItem))
            })
        }
        getNode(){
            return this.divBox
        }
        validate(){
            return this.items.every(({el, validate}) => {
                const input = el.querySelector('input')
                if(!validate || !input){ //没有校验规则时默认通过校验
                    return true
                }
                const inputValue = input.value
                const validateRes = this.doValidate(inputValue, validate.rule)
                if(!validateRes.validate){
                    if(validate.message){
                        if(typeof validate.message == 'string'){
                            this.appendErrorLabel(input.parentNode, validate.message)
                        }else{
                            this.appendErrorLabel(input.parentNode, validate.message[validateRes.reason] || '参数错误')
                        }
                    }else{
                        this.appendErrorLabel(input.parentNode, this.getErrMsg(validateRes.reason, validate.rule[validateRes.reason]))
                    }
                }else{
                    this.deleteErrorLabel(input.parentNode)
                }
                return validateRes.validate
            })
        }
        doValidate(value, rule){
            if(!rule){ //没有校验规则时默认通过校验
                return {
                    validate: true
                }
            }
            if(!rule.required && !value){return {
                validate: true
            }}
            if(rule.required && !value){ //非空校验
                return {
                    validate: false,
                    reason: 'empty'
                }
            }
            if(rule instanceof RegExp){
                return {
                    validate: rule.test(value),
                }
            }
            if(value.length > 1 && value.indexOf('0') == 0){
                return {
                    validate: false,
                    reason: 'integer'
                }
            }
            if(typeof rule == 'function'){
                return {
                    validate: rule(value)
                }
            }
            if(!rule.decimal){ //正整数校验
                if(!/^-?[0-9]*$/.test(value)) return {
                    validate: false,
                    reason: 'integer'
                }
            }

            value = value * 1
            if(!rule.min){rule.min = 0}
            if(value > rule.max){
                return {
                    validate: false,
                    reason: 'max'
                }
            }
            if(value < rule.min){
                return {
                    validate: false,
                    reason: 'min'
                }
            }
            if(value <= rule.max && value >= rule.min){
                return {
                    validate: true,
                }
            }
            return {
                validate: false,
            }
        }

        renderItemRight(rightItem) {
            const rightItemBox = util.create('rightItemBox')
            rightItem.type = rightItem.type || 'input'
            let res = null

            switch (rightItem.type) {
                case 'input':
                    res = this.createInput(rightItem)
                    break
                case 'option':
                    res = this.createOption(rightItem)
                    break
                case 'IO':
                    res = this.createIO(rightItem)
                    break
                case 'date-hms':
                    res = this.createDateInput(rightItem)
                    this.renderDate(1,res)
                    break
                case 'date-mm':
                    res = this.createDateInput(rightItem)
                    this.renderDate(2,res)
                    break
            }

            let title = ''
            if(rightItem.titleRender){
                title = util.create()
                title.innerHTML = rightItem.titleRender()
                title = title.firstElementChild
            }else{
                title = util.create('', 'label')
                title.innerHTML = rightItem.title
            }
            const wrapRes = util.create()
            wrapRes.appendChild(res)
            this.items.push({
                el: wrapRes,
                validate: rightItem.validate,
                paramName: rightItem.paramName
            })
            rightItemBox.appendChild(wrapRes)
            rightItemBox.appendChild(title)
            if (rightItem.hidden) {
                rightItemBox.style.display = 'none'
            }
            return rightItemBox
        }
        // IO渲染
        createIO() {

        }
        // 获取报错提示语
        getErrMsg(reason, value) {
            const reasonMap = {
                min: function (v) {
                    v = v || 0
                    return '请输入不小于 ' + v + ' 的数值'
                },
                max: function (v) {
                    return '请输入不大于 ' + v + ' 的数值'
                },
                integer: function(){
                    return '请输入整数'
                },
                empty: function (v) {
                    return '请输入整数'
                }
            }
            return reasonMap[reason] ? reasonMap[reason](value) : '请输入整数'
        }
        // 普通输入框渲染
        createInput(rightItem) {
            const input = util.create('form-control', 'input')
            input.setAttribute('placeholder', rightItem.placeholder || '请输入参数值')
            if(rightItem.value == undefined){
                input.setAttribute('value', '')
            }else {
                input.setAttribute('value', rightItem.value)
            }
            // 输入合法性校验
            if (rightItem.validate){
                const eventHandler = function(e){
                    if(e.type == 'blur'){
                        setTimeout(function () {
                            $(input).parent().find('.delIcon').remove()
                        },150)
                    }
                    if(e.type == 'input' || e.type == 'focus'){
                        if(e.target.value){
                            if($(input).parent().find('.delIcon').length == 0){
                                const del = $('<i class="delIcon" style="right: 8px"></i>')
                                del.on('click', clearInput)
                                del.insertAfter($(input))
                            }
                        }else {
                            $(input).parent().find('.delIcon').remove()
                        }
                    }
                    const validateRes = this.doValidate(e.target.value, rightItem.validate.rule)
                    if(!validateRes.validate){
                        if(rightItem.validate.message){
                            if(typeof rightItem.validate.message == 'string'){
                                this.appendErrorLabel(e.target.parentNode, rightItem.validate.message)
                            }else{
                                this.appendErrorLabel(e.target.parentNode, rightItem.validate.message[validateRes.reason] || '参数错误')
                            }
                        }else{
                            this.appendErrorLabel(e.target.parentNode, this.getErrMsg(validateRes.reason, rightItem.validate.rule[validateRes.reason]))
                        }
                    }else{
                        this.deleteErrorLabel(e.target.parentNode)
                    }
                }
                input.addEventListener('input',eventHandler.bind(this))
                input.addEventListener('blur',eventHandler.bind(this))
                input.addEventListener('focus',eventHandler.bind(this))
            }
            return input
        }
        // 下拉框渲染
        createOption(rightItem) {
            const renderData = rightItem.data
            const	defaultValue = rightItem.value
            const select = util.create('form-control', 'select')
            let htmlStr = ''
            renderData.forEach(function (item) {
                if (defaultValue == item.value) {
                    htmlStr += '<option selected value="' + item.value + '">' + item.name + '</option>'
                } else {
                    htmlStr += '<option value="' + item.value + '">' + item.name + '</option>'
                }
            })
            if(rightItem.onChange){
                select.addEventListener('change', rightItem.onChange)
            }
            select.innerHTML = htmlStr
            return select
        }
        // 日期渲染1
        createDateInput(rightItem) {
            const input = util.create('form-control', 'input')
            input.setAttribute('value', rightItem.value || '')
            input.classList.add('layer-date')
            input.classList.add('laydate-icon')
            input.setAttribute('readonly', 'readonly')
            input.style.backgroundColor = '#fafafa'
            input.style.cursor = 'pointer'
            return input
        }
        // 日期渲染2
        renderDate(type, inputEle) {
            setTimeout(() => {
                if(type == 1){
                    laydate.render({
                        elem: inputEle,
                        type: 'time',
                        range: '--',
                        format: 'HH:mm',
                        noSecond: true,
                        trigger: 'click'
                    });
                }else {
                    laydate.render({
                        elem: inputEle
                        , type: 'date'
                        , range: '--'
                        , format: 'yyyy-MM-dd'
                        , trigger: 'click'
                    });
                }
            },0)
        }

        appendErrorLabel(node, msg){
            this.deleteErrorLabel(node)
            if($(node).find('label.error-info').length == 0){
                $(node).append('<label class="error error-info" >'  + msg + '</label>')
            }
        }
        deleteErrorLabel(node){
            $(node).find('label.error-info').remove()
        }
    }

    // 所有参数设置
    window.allSetting = {}
    function render(config){
        config.forEach((element,idx) => {
            const target = document.querySelector('#' + element.divId)
            const tempDiv = util.create()
            key = element.divId
            allSetting[key] = []
            element.data.forEach((data) => {
                const item = new Item(data, element.sliderNum, idx)
                allSetting[key].push(item)
                tempDiv.appendChild(item.getNode())
            })
            target.appendChild(tempDiv)
        });
    }
    // 验证输入合法性
    function checkAll(){
        // return $('#settingBody .error-info').length == 0
        for(const key in allSetting){
            const res = allSetting[key].every((item) => {
                return item.validate()
            })
            if(res){
                continue
            }else {
                return false
            }
        }
        return true
    }
    // 下拉框渲染函数
    function renderSuggest(data){
        $("#brands").bsSuggest({
            indexId: 1, //data.value 的第几个数据，作为input输入框的内容
            indexKey: 0, //data.value 的第几个数据，作为input输入框的内容
            idField: "moId",
            keyField: "name",
            effectiveFields: ["name"],
            searchFields: ["moId"],
            // data: data,
            data: {value: data.obj}
        }).on('onDataRequestSuccess', function (e, result) {
        }).on('onSetSelectValue', function (e, keyword, data) {
            // 当选择参考车牌
            var vehicleId = keyword.id;
            $.ajax({
                type: 'get',
                url: '/clbs/a/alarmSetting/getAlarmParameterSettingDetails/' + vehicleId,
                async: false,
                dataType: 'json',
                success: function (data) {
                    alarmSettingUtil.destroy(true)
                    Object.assign(data.obj, IOSetting)
                    data.obj.highPrecisionAlarmList && data.obj.highPrecisionAlarmList.forEach(item => {
                        const withTen = ["18811","18711","18712","18719"] //需要乘10的参数 设备电量报警 急加速报警 急减速报警 碰撞报警
                        if(withTen.includes(item.alarmSettingType)){
                            item.parameterValue && (item.parameterValue.param1 = item.parameterValue.param1/10)
                        }
                    })
                    dataTransform(data.obj)
                    config.forEach(( item, idx) => {
                        const key = '#' + item.divId
                        $("a[href='" + key + "']").parent().show()
                        if(key == '#deviceIoAlarmList'){
                            $("a[href='#IOAlarm']").parent().show()
                        }
                        if(idx == 0){
                            $("a[href='" + key + "']").parent().addClass('active').siblings().removeClass('active')
                            $(key).addClass('active').siblings().removeClass('active')
                        }
                    })
                    alarmSettingUtil.init()
                },
                error: function () {
                    layer.msg('获取接口出错', {move: false});
                }
            });
        }).on('onUnsetSelectValue', function () {
        });
    }

    //清空输入框
    function clearInput(e){
        $(e.target).siblings("input").val('')
        $(e.target).siblings(".error-info").remove()
    }

    /**
     * 数据转换函数，把服务器的数据格式转换成本地的渲染函数需要的数据格式
     * @param origin 服务器拉取的数据
     */
    function dataTransform(origin){
        for(let key in origin){
            const alertList = origin[key]
            const alertListDefault = config.find((item) => item.divId == key)
            if(!alertList) continue
            // 终端IO设置特殊处理
            if(['deviceIoAlarmList','ioCollectionOneAlarmList','ioCollectionTwoAlarmList'].includes(key)){
                alertListDefault.data = alertList.map(item => {
                    return {
                        ...item,
                        name: item.alarmSettingName,
                        right: [
                            {
                                title: '状态',
                                type: 'option',
                                value: item.parameterValue ? item.parameterValue.param1 : 0,
                                data: [
                                    { value: 0, name: '— 状态 —'},
                                    { value: 1, name: (item.highSignalType == 1 ? item.stateOne : item.stateTwo)},
                                    { value: 2, name: (item.lowSignalType == 1 ? item.stateOne : item.stateTwo)}
                                ],
                                onChange: function(e){
                                    const relatedInput = e.target.parentNode.nextElementSibling
                                    if(e.target.value == 1){
                                        relatedInput.value = '高电平'
                                        return
                                    }
                                    if(e.target.value == 2){
                                        relatedInput.value = '低电平'
                                        return
                                    }
                                    relatedInput.value = ''
                                },
                                titleRender: function () {
                                    const value = item.parameterValue ? ['', '高电平', '低电平'][item.parameterValue.param1 || 0] : ''
                                    return '<input value="' + value + '" style="width: 40%; margin-right: 42%;margin-left: 20px;" class="form-control" type="text" readonly="">'
                                }
                            }
                        ],
                    }
                })
            }
            else {
                alertList.forEach((setting) => {
                    if(!alertListDefault) return
                    const toModifyItem = alertListDefault.data.find((item2) => item2.alarmSettingType == setting.alarmSettingType)
                    if(!alertListDefault || !toModifyItem) return
                    toModifyItem.alarmPush = setting.alarmPush
                    toModifyItem.alarmSettingType = setting.alarmSettingType
                    if('parameterValue' in setting && !setting.parameterValue && toModifyItem.right){
                        toModifyItem.right = toModifyItem.right.map((item) => ({...item,value: ''}))
                    }
                    if(setting.parameterValue){
                        for(const key in setting.parameterValue){
                            let i = key.slice(5)
                            if(key == 'param11'){
                                if(setting.parameterValue.param11 == 1){
                                    setTimeout(() => {
                                        $('#roadNetSpeedLimit').click()
                                    },0)
                                }
                                continue
                            }
                            if(toModifyItem.right){
                                var indexItem = toModifyItem.right.findIndex(item => item.paramName == key)
                                if(indexItem != -1){
                                    // 平台报警的超速报警
                                    toModifyItem.right[indexItem].value = setting.parameterValue['param' + i]
                                }else {
                                    toModifyItem.right[i - 1].value = setting.parameterValue['param' + i]
                                }

                            }
                        }
                    }
                })
            }
        }
        return config
    }
    window.readUtils = {
        //F3高精度报警
        getF3alarmParamCall: function (data) {
            if (data.success) {
                $("#readParam").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                var msg = $.parseJSON(data.msg);
                headers = {"UserName": msg.userName};
                webSocket.subscribe(headers, "/user/topic/highPrecisionAlarm", readUtils.handleF3Param, null, null);
            } else {
                layer.msg('终端离线');
            }
        },
        handleF3Param: function (msg) {
            webSocket.unsubscribe('/user/topic/highPrecisionAlarm')
            if (msg == null) return;
            var result = $.parseJSON(msg.body);
            layer.closeAll();
            $("#readParam").html("读取").prop('disabled', false);
            var dataVal = result.data.msgBody.params;
            readUtils.setF3Val(dataVal)
        },
        //基本信息-下发获取基本信息返回处理方法
        getF3BaseParamCall: function (data) {
            if (data.success) {
                $("#readParam").html("<i class='fa fa-spinner loading-state'></i>").prop('disabled', true);
                var msg = $.parseJSON(data.msg);
                headers = {"UserName": msg.userName};
                webSocket.subscribe(headers, "/user/topic/oil9999Info", readUtils.handle0104Param, null, null);
            } else {
                layer.msg('终端离线');
            }
        },
        //处理获取设备上传数据
        handle0104Param: function (msg) {
            webSocket.unsubscribe('/user/topic/oil9999Info')
            if (msg == null) return;
            var result = $.parseJSON(msg.body);
            var status = result.data.msgBody.result;
            if (status == 1) {
                layer.closeAll();
                $("#readParam").html("读取").prop('disabled', false);
                layer.msg("获取设备数据失败!");
                return;
            }
            layer.closeAll();
            $("#readParam").html("读取").prop('disabled', false);
            var dataVal = result.data.msgBody.alarmSettingList;
            readUtils.set0104Value(dataVal)
        },
        // f3高精度 => 根据读取参数值设置界面数据
        setF3Val: function (result) {
            var f3Config = config.find((item) => item.name == 'F3高精度报警')
            var valueMapping = {
                deviceElectricity: {
                    name: '设备电量报警',
                    value: ''
                },
                speedCutAlarm: {
                    name: '急减速报警',
                    value: ''
                },
                speedUpAlarm: {
                    name: '急加速报警',
                    value: ''
                },
                swerveAlarm: {
                    name: '急转弯报警',
                    value: ''
                },
                collisionAlarm: {
                    name: '碰撞报警',
                    value: ''
                },
            }
            if (result.length != 0) {
                for (var i = 0; i < result.length; i++) {
                    var id = result[i].id;
                    switch (id) {
                        case 62543:
                            valueMapping.deviceElectricity.value = Math.floor(result[i].value.electricitySet.deviceElectricity / 10);
                            break;
                        case 62544:
                            valueMapping.speedCutAlarm.value = result[i].value.terminalSet.speedCutAlarm / 10;
                            valueMapping.speedUpAlarm.value = result[i].value.terminalSet.speedUpAlarm / 10;
                            valueMapping.swerveAlarm.value = result[i].value.terminalSet.swerveAlarm;
                            valueMapping.collisionAlarm.value = result[i].value.terminalSet.collisionAlarm / 10;
                            break;
                    }
                }
            }
            for(var key in valueMapping){
                var temp = f3Config.data.find(item => item.name == valueMapping[key].name)
                temp.right[0].value = valueMapping[key].value
            }
            $('.toolbarBox').siblings().remove()
            alarmSettingUtil.init()
        },
        // 其它参数 => 根据读取参数值设置界面数据
        set0104Value: function (result) {
            const deviceType = $("input[name='deviceCheck']:checked").val();
            for(const key in result){
                result[key].forEach((item) => {
                    item.alarmPush = readUtils.getAlarmPushValue(result[key], key, item.alarmSettingType, item)
                })
                if(deviceType == -1 && key == 'alertList'){
                    // 2013版：从终端读取值的时候，预警=>碰撞预警=>碰撞时间 的单位为4
                    const res = result[key].find(item => item.alarmSettingType == 29)
                    res && res.parameterValue && res.parameterValue.param1 && (res.parameterValue.param1 = res.parameterValue.param1 * 4)
                }
            }
            dataTransform(result)
            $('.toolbarBox').siblings().remove()
            alarmSettingUtil.init()
        },
        getAlarmPushValue: function (source, key, alarmSettingType, item) {
            const target = config.find((item) => item.divId == key)
            const targetValue = target.data.find((item) => item.alarmSettingType == alarmSettingType)
            if(!targetValue) return 0
            if(item.alarmPush == -2){
                return -1
            }else if(item.alarmPush == -3){
                return targetValue.alarmPush == -1 ? 0 : targetValue.alarmPush
            }
            return 0
        }
    }
    window.alarmSettingUtil = {
        init: function(){
            render(config)
            // 提示框渲染
            setTimeout(function () {
                $("[data-toggle='tooltip']").tooltip();
            },500)

            alarmSettingUtil.bindEvent()
            $("a[href='" + currentTabId + "']").click()
            // $('input').inputClear()
        },
        bindEvent: function(){
            $('.alarmLevel').on('click', alarmSettingUtil.onSlideAll)

            // 平台报警 超速报警.路网限速 勾选事件
            $('#roadNetSpeedLimit').on('change',function (e) {
                $(allSetting.platAlarmList[9].rightAll.items[1].el.parentNode).toggle()
                $(allSetting.platAlarmList[9].rightAll.items[2].el.parentNode).toggle()
                $(allSetting.platAlarmList[9].rightAll.items[3].el.parentNode).toggle()
                $(allSetting.platAlarmList[9].rightAll.items[4].el.parentNode).toggle()
                $(allSetting.platAlarmList[9].rightAll.items[5].el.parentNode).toggle()
                $(allSetting.platAlarmList[9].rightAll.items[6].el.parentNode).toggle()
            })
            // 平台报警 超速报警.夜间限速 显示更多事件
            $('#overSpeedHide').on('click',function (e) {
                $(allSetting.platAlarmList[9].rightAll.items[8].el.parentNode).slideToggle()
                if(this.firstElementChild.classList.contains('fa-chevron-down')){
                    this.innerHTML = '隐藏参数<span aria-hidden="true" class="fa fa-chevron-up"></span>'
                }else {
                    this.innerHTML = '显示更多<span aria-hidden="true" class="fa fa-chevron-down"></span>'
                }
            })
            // 记录当前页签
            $('#panel_liBox a').on('click',this.recordTabId)
        },
        recordTabId: function(e){
            currentTabId = '#' + e.target.href.split('#')[1]
        },
        unbindEvent: function(){
            $('.alarmLevel').off('click', alarmSettingUtil.onSlideAll)
            $('#panel_liBox a').off('click',this.recordTabId)
        },
        /**
         * 显示弹窗
         * @param vehicleId 车辆id
         * @param type 1设置  2 修改
         */
        showModal: function(vehicleId, type, monitorName){
            if(vehicleId.split(',').length > 1){
                $('#readParam').prop('disabled', true)
            }
            currentMonitorId = vehicleId
            currentMonitorName = monitorName
            $("input#brands").bsSuggest("destroy")
            // 下拉框渲染初始化
            setTimeout(function () {
                json_ajax("get", "/clbs/a/alarmSetting/referentList/" + $('#Ul-menu-text-v input:checked').val(), "json", false, null, renderSuggest)
            },0)
            $("#brands")[0].dataset.id = ''
            $("#brands").val('')
            $("#alarmSettingModal").modal("show");
            $('#monitorName').val(monitorName)
            $("#alarmSettingModal").on("hidden.bs.modal", function () {
                $('#readParam').prop('disabled', false)
                alarmSettingUtil.destroy()
                currentTabId = ''
                IOSetting = {}
                $("a[href='#deviceIoAlarmList']").click()
                $("#readParam").html("读取").prop('disabled', false);
                $('#ioTip').show()
                $('#brands')[0].style.background = '#fafafa'
                webSocket.unsubscribe('/user/topic/highPrecisionAlarm')
                webSocket.unsubscribe('/user/topic/oil9999Info')
            });
            if(!vehicleId){ return }
            $.ajax({
                type: 'get',
                url: '/clbs/a/alarmSetting/getAlarmParameterSettingDetails/' + vehicleId,
                async: false,
                dataType: 'json',
                success: function (data) {
                    const all = alarmSettingUtil.getConfig()
                    config = all[0]
                    config.forEach(( item, idx) => {
                        const key = '#' + item.divId
                        $("a[href='" + key + "']").parent().show()
                        if(key == '#deviceIoAlarmList'){
                            $("a[href='#IOAlarm']").parent().show()
                        }
                        if(idx == 0){
                            $("a[href='" + key + "']").parent().addClass('active').siblings().removeClass('active')
                            $(key).addClass('active').siblings().removeClass('active')
                        }
                    })
                    configWithDefaultValue = all[1]
                    originConfig = util.deepClone(config)
                    for(const key in data.obj){
                        const keys = ['deviceIoAlarmList', 'ioCollectionOneAlarmList', 'ioCollectionTwoAlarmList']
                        if(keys.includes(key)){
                            IOSetting[key] = data.obj[key]
                            if(IOSetting[key]){
                                $('#ioTip').hide()
                            }
                        }
                    }
                    data.obj.highPrecisionAlarmList && data.obj.highPrecisionAlarmList.forEach(item => {
                        const withTen = ["18811","18711","18712","18719"] //需要乘10的参数 设备电量报警 急加速报警 急减速报警 碰撞报警
                        if(withTen.includes(item.alarmSettingType)){
                            item.parameterValue && (item.parameterValue.param1 = item.parameterValue.param1/10)
                        }
                    })
                    dataTransform(data.obj)
                    alarmSettingUtil.init()
                },
                error: function () {
                    layer.msg('获取接口出错', {move: false});
                }
            });
        },
        // 获取配置信息
        getConfig: function(){
            // 当前协议类型 -1:交通部JT/T808-2013  11:交通部JT/T808-2019  5：BDTD-SM  9：ASO   10:F3超长待机
            let deviceType = $("input[name='deviceCheck']:checked").val();
            let config = null
            let configWithDefaultValue = null
            switch (Number(deviceType)) {
                case -1: /* 交通部JT/T808-2013 */
                    config= util.deepClone(alarmSettingConfig.jt2013) ;
                    configWithDefaultValue= util.deepClone(alarmSettingConfig.jt2013Default)
                    break
                case 11: /* 交通部JT/T808-2019 */
                    config= util.deepClone(alarmSettingConfig.jt2019);
                    configWithDefaultValue= util.deepClone(alarmSettingConfig.jt2019Default)
                    break
                case 5: /* BDTD-SM */
                    config= util.deepClone(alarmSettingConfig.bdtd);
                    configWithDefaultValue= util.deepClone(alarmSettingConfig.bdtdDefault)
                    break
                case 9: /* ASO */
                    config= util.deepClone(alarmSettingConfig.aso);
                    configWithDefaultValue= util.deepClone(alarmSettingConfig.asoDefault)
                    break
                case 10: /* F3超长待机 */
                    config= util.deepClone(alarmSettingConfig.f3Long);
                    configWithDefaultValue= util.deepClone(alarmSettingConfig.f3LongDefault)
                    break
            }
            if(deviceType == -1 || deviceType == 11){
                $('#defaultParam').show()
                $('#readParam').show()
            }else {
                $('#defaultParam').hide()
                $('#readParam').hide()
            }
            return [config, configWithDefaultValue]
        },
        // 提交
        onSubmit: function(){
            const res = alarmSettingUtil.getData()
            if(!checkAll()){
                return layer.msg('参数设置错误')
            }
            json_ajax("POST", '/clbs/a/alarmSetting/saveAlarmParameterSetting', "json", true, {
                moIds: currentMonitorId,
                alarmParameterSettingJsonStr: JSON.stringify(res),
                // 防止重复提交，根据提交参数生成hash字符串
                resubmitToken: (currentMonitorId + JSON.stringify(res)).split("").reduce(function (a, b) { a = ((a << 5) - a) + b.charCodeAt(0); return a & a }, 0)
            }, function (data) {
                if(data.success){
                    layer.msg('修改成功')
                    $("#alarmSettingModal").modal("hide");
                    myTable.refresh();
                }else {
                    layer.msg('修改失败')
                }
            });

        },
        // 所有滑块滑动
        onSlideAll: function(e){
            const idx = $(e.target).index()
            const allparents = $(e.target).parentsUntil('.tab-pane')
            const key = allparents[allparents.length - 1].parentNode.id
            allSetting[key].forEach((item) => {
                item.slider.slideTo(idx)
            })
        },
        // 获取页面设置的值
        getData: function () {
            const res = {}
            for(let itemsKey in allSetting){
                res[itemsKey] = []
                allSetting[itemsKey].forEach(function (item) {
                    const temp = {}
                    temp.alarmPush = item.sliderValue
                    temp.alarmSettingType = item.alarmSettingType

                    //IO独有
                    if('highSignalType' in item){
                        temp.parameterValue = {
                            param1: item.rightAll.items[0].el.firstElementChild.value
                        }
                    }else {
                        temp.parameterValue = null
                        item.rightAll.items.forEach(function (rightItem, index) {
                            if(!temp.parameterValue){ temp.parameterValue = {} }
                            const el = rightItem.el
                            if(rightItem.paramName){
                                temp.parameterValue[rightItem.paramName] = el.firstElementChild.value
                                temp.parameterValue.param11 = $('#roadNetSpeedLimit').prop('checked') ? 1 : 0
                            }else {
                                temp.parameterValue['param' + (index + 1)] = el.firstElementChild.value
                            }
                        })
                    }
                    res[itemsKey].push(temp)
                })
            }
            res.highPrecisionAlarmList && res.highPrecisionAlarmList.forEach(item => {
                const withTen = ["18811","18711","18712","18719"] //需要乘10的参数 设备电量报警 急加速报警 急减速报警 碰撞报警
                if(withTen.includes(item.alarmSettingType)){
                    if(item.parameterValue){
                        if(item.parameterValue.param1 !== ''){
                            item.parameterValue.param1 = item.parameterValue.param1*10
                        }
                    }
                }
            })
            return res
        },
        // 销毁
        destroy: function(flag){
            config = util.deepClone(originConfig)
            allSetting = {}
            $('#panel_liBox li').hide()
            if(!flag){
                $('#monitorName').val('')
                $('#brands').val('')
                // IOSetting = {}
            }
            $('.toolbarBox').siblings().remove()
            alarmSettingUtil.unbindEvent()
        },
        // 默认值
        getDefaultParam: function () {
            alarmSettingUtil.destroy()
            $('#monitorName').val(currentMonitorName)
            config = util.deepClone(configWithDefaultValue)
            dataTransform(IOSetting)
            config.forEach(( item, idx) => {
                const key = '#' + item.divId
                $("a[href='" + key + "']").parent().show()
                if(key == '#deviceIoAlarmList'){
                    $("a[href='#IOAlarm']").parent().show()
                }
                if(idx == 0){
                    $("a[href='" + key + "']").parent().addClass('active').siblings().removeClass('active')
                    $(key).addClass('active').siblings().removeClass('active')
                }
            })
            alarmSettingUtil.init()
        },
        // 读取终端参数值
        onReadTerminal: function () {
            var urlF3 = "/clbs/a/alarmSetting/sendParameter";
            var paramF3 = {
                "vehicleId": currentMonitorId,
                "paramIds": "0xf44f,0xf450"
            };
            json_ajax("POST", urlF3, "json", false, paramF3, readUtils.getF3alarmParamCall);
            // webSocket.subscribe(headers, "/user/topic/highPrecisionAlarm", readUtils.handleF3Param, null, null);

            var url = "/clbs/a/alarmSetting/getDeviceAlarmParam";
            var param = {
                "monitorId": currentMonitorId,
                "deviceType": $('input[name="deviceCheck"]:checked').val()
            };
            json_ajax("POST", url, "json", false, param, readUtils.getF3BaseParamCall);
            // webSocket.subscribe(headers, "/user/topic/oil9999Info", readUtils.handle0104Param, null, null);
        },
        // 把界面的值更新到config
        setValueToConfig: function () {

        }
    }
    // 提交
    $('#submitButton').on('click', alarmSettingUtil.onSubmit)
    // 读取
    $('#readParam').on('click', alarmSettingUtil.onReadTerminal)
    // 默认值
    $('#defaultParam').on('click', alarmSettingUtil.getDefaultParam)

}(window, $))
