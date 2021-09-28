//# sourceURL=luParamInfo.js
(function ($, window) {
    var vid = $("#vehicleId").val();//车辆id
    //按钮模板
    var template =
        '<div class="col-md-3 col-md-offset-2">\n' +
        '    <botton class="btn btn-primary btn-oc-width sensorBtn" disabled="">以传感器为准</botton>\n' +
        '</div>\n' +
        '<div class="col-md-3 col-md-offset-2">\n' +
        '    <botton class="btn btn-primary btn-oc-width platformBtn" disabled="">以平台设置为准</botton>\n' +
        '</div>\n'

    window.readParamUtil = {
        initDom: function (domData) {
            domData.forEach(function (item) {
                readParamUtil.renderDom(item.allData, item.domId)
            })
            readParamUtil.addEventHandler()
        },
        renderDom: function (data, domId) {
            data.forEach(function (item) {
                var target = $('#' + domId + ' ' + '.dsm-content')
                if (item.label == '全局参数') {
                    target = $('#' + domId)
                    target.prepend(readParamUtil.renderContent(item.data, true, item.riskFunctionId))
                    target.prepend(readParamUtil.renderTitle(item.label, true))
                } else {
                    target.append(readParamUtil.renderTitle(item.label))
                    target.append(readParamUtil.renderContent(item.data, false, item.riskFunctionId))
                }
            })
        },
        renderContent: function (data, show, riskFunctionId) {
            var infoContent = readParamUtil.create('div', 'info-content commonParamSetting')
            if (!show) infoContent.setAttribute('style', 'display:none')
            data.forEach(function (detail) {
                var lastId = detail.riskFunctionId || riskFunctionId
                var formGroup = readParamUtil.create('div', 'form-group')
                //传感器
                var label = readParamUtil.create('label', 'col-md-2 control-label')
                label.innerText = detail.name + '：'
                var div = readParamUtil.create('div', 'col-md-3')
                var sensorInputId = detail.id + '_' + lastId + '_sensor'
                div.innerHTML = '<input class="form-control sensor-' + detail.id + '" id="' + sensorInputId + '" readonly>'
                //平台
                var label2 = readParamUtil.create('label', 'col-md-2 control-label')
                label2.innerText = detail.name + '：'
                var div2 = readParamUtil.create('div', 'col-md-3')
                var platformInputId = detail.id + '_' + lastId + '_platform'
                div2.innerHTML = '<input class="form-control platform-' + detail.id + '" id="' + platformInputId + '" readonly>'
                formGroup.appendChild(label)
                formGroup.appendChild(div)
                formGroup.appendChild(label2)
                formGroup.appendChild(div2)
                infoContent.appendChild(formGroup)
            })
            var btns = readParamUtil.create('div', 'form-group')
            btns.innerHTML = template
            infoContent.appendChild(btns)
            return infoContent
        },
        renderTitle: function (name, show) {
            var title = readParamUtil.create('div', 'form-group')
            if (show) {
                title.innerHTML =
                    ' <h4 class="col-md-6">' + name + '</h4>\n' +
                    ' <div class="col-md-6 control-label text-right">\n' +
                    '     <div class="info-span">\n' +
                    '         <font>隐藏信息</font>\n' +
                    '         <span aria-hidden="true" class="fa fa-chevron-up"></span>\n' +
                    '     </div>\n' +
                    ' </div>'
            } else {
                title.innerHTML =
                    ' <h4 class="col-md-6">' + name + '</h4>\n' +
                    ' <div class="col-md-6 control-label text-right">\n' +
                    '     <div class="info-span">\n' +
                    '         <font>显示更多</font>\n' +
                    '         <span aria-hidden="true" class="fa fa-chevron-down"></span>\n' +
                    '     </div>\n' +
                    ' </div>'
            }
            return title
        },
        selectData: function (arr, origin) {
            if (!arr) return origin
            return origin
                .filter(function (item) {
                    return arr.indexOf(item.index) != -1
                })
                .sort(function (a, b) {
                    return arr.indexOf(a.index) - arr.indexOf(b.index)
                })
        },
        create: function (tagName, className) {
            var node = document.createElement(tagName ? tagName : 'div')
            if (className) {
                readParamUtil.setC(node, className)
            }
            return node
        },
        setC: function (node, className) {
            node.setAttribute('class', className)
        },

        //组装下发参数
        getAllData: function (protocolType) {
            var tempObj = {}
            var allInput = $('.tab-content input')
            allInput.each(function (index, item) {
                // id格式： 字段名 + paramType/riskFunctionId + 'platform' eg:cameraResolution_64_platform
                var id = $(item).attr('id')
                if (!id) return
                if (id.indexOf('platform') == -1) return
                tempObj[id] = $(item).attr('data-value')
            })
            return readParamUtil.parserData(tempObj, protocolType)
        },

        // 解析id中的数据
        parserData: function (obj, protocolType) {
            var res = []
            var allCommonParam = {}
            var allAdasParam = {}
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    var s = key.split('_')
                    var id = s[1]
                    var dataIndex = s[0]
                    var value = obj[key]
                    if (id.length == 2) {
                        if (allCommonParam[id]) {
                            allCommonParam[id][dataIndex] = value
                        } else {
                            allCommonParam[id] = {}
                            allCommonParam[id][dataIndex] = value
                        }
                    } else {
                        if (allAdasParam[id]) {
                            allAdasParam[id][dataIndex] = value
                        } else {
                            allAdasParam[id] = {}
                            allAdasParam[id][dataIndex] = value
                        }
                    }
                }
            }
            var findItem = function (item, key) {
                return item.commonParamSetting && item.commonParamSetting.paramType == key.substr(2, 2)
            }
            for (var key in allAdasParam) {
                if (allAdasParam.hasOwnProperty(key)) {
                    var target = res.find(function (item) {
                        return item.commonParamSetting && item.commonParamSetting.paramType == key.substr(2, 2)
                    })
                    if (target) {
                        target.adasAlarmParamSettings.push(Object.assign({}, allAdasParam[key], {
                            riskFunctionId: key,
                            vehicleId: vid,

                        }))
                    } else {
                        var temp = {
                            adasAlarmParamSettings: [],
                            commonParamSetting: {}
                        }
                        temp.adasAlarmParamSettings.push(Object.assign({}, allAdasParam[key], {
                            riskFunctionId: key,
                            vehicleId: vid,

                        }))
                        var paramType = key.substr(2, 2) == '23' ? '233' : key.substr(2, 2)
                        temp.commonParamSetting = Object.assign({}, allCommonParam[key.substr(2, 2)], {
                            paramType: paramType,
                            protocolType: protocolType,
                            vehicleId: vid
                        })
                        res.push(temp)
                    }
                }
            }
            return res
        },


        //传感器常规参数对比后赋值(以传感器为准)
        sensorBtnClick: function () {
            var allInput = $(this).parents('.info-content').find('input');
            allInput.each(function (index, input) {
                // 单数为传感器参数 双数为平台参数
                if ((index + 1) % 2 == 1) {
                    var sensorValue = $(input).val()
                    var sensorDataValue = $(input).attr('data-value')
                    var platformInput = $(input).parents('.form-group').find('input')[1]
                    $(platformInput).val(sensorValue)
                    $(platformInput).attr('data-value', sensorDataValue)
                }
            })
        },
        //传感器常规参数对比后赋值(以平台设置为准)
        platformBtnClick: function () {
            $("#dealType").val("pt");
            var allInput = $(this).parents('.info-content').find('input');
            allInput.each(function (index, input) {
                // 单数为传感器参数 双数为平台参数
                if ((index + 1) % 2 == 0) {
                    var sensorValue = $(input).val()
                    $($(input).parents('.form-group').find('input')[0]).val(sensorValue)
                }
            })
        },
        //信息隐藏显示
        hiddenparameterFn: function () {
            var self = $(this);
            var content = $(this).parents('.form-group').next('.info-content');
            if (!content.is(":hidden")) {
                content.slideUp();
                self.children("font").text("显示更多");
                self.children("span").removeAttr("class").addClass("fa fa-chevron-down");
            } else {
                content.slideDown();
                self.children("font").text("隐藏信息");
                self.children("span").removeAttr("class").addClass("fa fa-chevron-up");
            }
        },
        addEventHandler: function () {
            $(".info-span").on("click", readParamUtil.hiddenparameterFn);//点击显示隐藏信息
            $(".sensorBtn").on("click", readParamUtil.sensorBtnClick);//以传感器为准
            $(".platformBtn").on("click", readParamUtil.platformBtnClick);//以平台设置为准
        }
    }
}($, window))