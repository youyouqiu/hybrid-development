(function (window, $) {
    window.settingUtil = {
        /**
         * 设置属性
         * @param node Element 元素
         * @param className String 样式类名
         */
        setC: function (element, className) {
            element.setAttribute('class', className)
        },
        /**
         * 创建元素
         * @param tagName String 元素名称
         * @param className String 样式类名
         * @returns Element
         */
        create: function (tagName, className) {
            var element = document.createElement(tagName ? tagName : 'div')
            if (className) {
                settingUtil.setC(element, className)
            }
            return element
        },
        /**
         * 渲染公共参数
         * @param renderData
         * @param riskFunctionId
         * @returns domNode
         */
        renderCommonData: function (renderData, paramType) {
            var commonDomList = []
            renderData.data.forEach(function (item) {
                paramType = item.riskFunctionId ? item.riskFunctionId : paramType
                var itemDom = settingUtil.create('div', 'col-md-4 noPadding')
                var itemDom_title = settingUtil.create('label', 'col-md-6 control-label')
                var itemDom_content = settingUtil.create('div', 'col-md-6')
                itemDom_title.innerHTML = item.titleRender ? item.titleRender() : item.name + '：'
                itemDom.appendChild(itemDom_title)
                if (item.contentRender) {
                    var res = item.contentRender()
                    if (typeof res == 'string') {
                        itemDom_content.innerHTML = res
                    } else {
                        itemDom_content.appendChild(res)
                    }
                    itemDom.appendChild(itemDom_content)
                } else {
                    switch (item.tagName) {
                        case 'select':
                            itemDom_content.appendChild(settingUtil.renderSelect(item, paramType))
                            break;
                        case 'radio':
                            itemDom_content.appendChild(settingUtil.renderRadio(item, paramType))
                            break;
                        default :
                            itemDom_content.appendChild(settingUtil.renderInput(item, paramType))
                            break;
                    }
                    itemDom.appendChild(itemDom_content)
                }
                if (item.name == '占位') {
                    itemDom.style.display = 'none'
                    itemDom.innerHTML = ''
                }
                commonDomList.push(itemDom)
            })
            return settingUtil.domStructureAdjust(commonDomList, renderData.threshold ? renderData.threshold : 3)
        },
        /**
         * 渲染其它参数
         * @param renderData
         * @param riskFunctionId
         * @returns domNode
         */
        renderOtherData: function (renderData, riskFunctionId) {
            var otherDomList = []
            renderData.data.forEach(function (item) {
                riskFunctionId = item.riskFunctionId ? item.riskFunctionId : riskFunctionId
                var itemDom = settingUtil.create('div', 'col-md-6 noPadding')
                var itemDom_title = settingUtil.create('label', 'col-md-4 control-label')
                var itemDom_content = settingUtil.create('div', 'col-md-6')
                if (renderData.threshold == 3) { // 三列时需要改变布局
                    itemDom = settingUtil.create('div', 'col-md-4 noPadding')
                    itemDom_title = settingUtil.create('label', 'col-md-6 control-label')
                    itemDom_content = settingUtil.create('div', 'col-md-6')
                }
                itemDom_title.innerHTML = item.titleRender ? item.titleRender() : item.name + '：'
                itemDom.appendChild(itemDom_title)
                if (item.contentRender) {
                    var res = item.contentRender()
                    if (typeof res == 'string') {
                        itemDom_content.innerHTML = res
                    } else {
                        itemDom_content.appendChild(res)
                    }
                    itemDom.appendChild(itemDom_content)
                } else {
                    switch (item.tagName) {
                        case 'select':
                            itemDom_content.appendChild(settingUtil.renderSelect(item, riskFunctionId))
                            break;
                        case 'radio':
                            itemDom_content.appendChild(settingUtil.renderRadio(item, riskFunctionId))
                            break;
                        default :
                            itemDom_content.appendChild(settingUtil.renderInput(item, riskFunctionId))
                            break;
                    }
                    itemDom.appendChild(itemDom_content)
                }
                if (item.name == '占位') {
                    itemDom.style.display = 'none'
                    itemDom.innerHTML = ''
                }
                otherDomList.push(itemDom)
            })
            return settingUtil.domStructureAdjust(otherDomList, renderData.threshold ? renderData.threshold : 2)
        },
        /**
         * input框渲染
         * @param renderData
         * @param paramType
         * @param riskFunctionId
         * @returns domNode
         */
        renderInput: function (renderData, id) {
            var innerDiv = document.createDocumentFragment()
            var input = settingUtil.create('input', 'form-control')
            input.setAttribute('type', 'text')
            input.setAttribute('name', renderData.id + '_' + id)
            input.setAttribute('id', renderData.id + '_' + id)
            input.setAttribute('value', renderData.value)
            if (renderData.placeholder) {
                input.setAttribute('placeholder', renderData.placeholder)
            }
            innerDiv.appendChild(input)
            return innerDiv
        },
        /**
         * select渲染
         * @param renderData
         * @param paramType
         * @param riskFunctionId
         * @returns domNode
         */
        renderSelect: function (renderData, id) {
            var innerDiv = document.createDocumentFragment()
            var select = settingUtil.create('select', 'form-control')
            select.setAttribute('name', renderData.id + '_' + id)
            select.setAttribute('id', renderData.id + '_' + id)
            if (renderData.placeholder) {
                select.setAttribute('placeholder', renderData.placeholder)
            }
            var htmlStr = ''
            renderData.value.forEach(function (item) {
                if (typeof item == 'object') {
                    if (renderData.default == item.value) {
                        htmlStr += '<option selected value="' + item.value + '">' + item.name + '</option>'
                    } else {
                        htmlStr += '<option value="' + item.value + '">' + item.name + '</option>'
                    }
                } else {
                    if (renderData.default == item) {
                        htmlStr += '<option selected value="' + item + '">' + item + '</option>'
                    } else {
                        htmlStr += '<option value="' + item + '">' + item + '</option>'
                    }
                }

            })
            select.innerHTML = htmlStr
            innerDiv.appendChild(select)
            return innerDiv
        },
        /**
         * radio渲染
         * @param renderData
         * @param paramType
         * @param riskFunctionId
         * @returns domNode
         */
        renderRadio: function (renderData, id) {
            var innerDiv = document.createDocumentFragment()
            renderData.value.forEach(function (item) {
                var labelWrap = settingUtil.create('label', 'col-md-4 control-label radioLabel')
                var radio = settingUtil.create('input', renderData.id + '_' + id)
                radio.setAttribute('type', 'radio')
                radio.setAttribute('name', renderData.id + '_' + id)
                radio.setAttribute('class', renderData.id + '_' + id + ' ' + 'alarmSwitch')
                radio.setAttribute('value', item.value)
                if (renderData.default == item.value) {
                    radio.setAttribute('checked', 'checked')
                }
                labelWrap.appendChild(radio)
                var text = document.createTextNode(item.name)
                labelWrap.appendChild(radio)
                labelWrap.appendChild(text)
                innerDiv.appendChild(labelWrap)
            })
            return innerDiv
        },
        /**
         * 提示语渲染
         * @param tips String[]
         * @returns Element
         */
        renderTips: function (tips) {
            var res = settingUtil.create('div', "form-group")
            var div, span, text
            if (Object.prototype.toString.call(tips) != "[object Array]") return res
            tips.forEach(function (item, index) {
                div = settingUtil.create('div', "col-md-10 col-md-offset-2")
                if (index == 0) {
                    div.style.marginTop = '20px'
                    div.style.padding = '0px'
                } else {
                    div.style.marginTop = '10px'
                    div.style.padding = '0px'
                }
                span = settingUtil.create('span', "glyphicon glyphicon-asterisk yellow-flower")
                text = document.createTextNode(item)
                div.append(span)
                div.append(text)
                res.append(div)
            })
            return res
        },
        /**
         * 调整dom结构,方便交互效果的实现
         * @param nodeArr node[] 注意这里不是指nodeList类型，而是一个普通数组，里面放的node类型的item
         * @param threshold 阈值 每几个item放在一个dom结构内
         * @returns Node
         */
        domStructureAdjust: function (nodeArr, threshold) {
            var wrapperItem = null
            var res = document.createDocumentFragment()
            threshold = threshold ? threshold : 2
            for (var i = 0; i < nodeArr.length; i++) {
                if (i % threshold == 0) {
                    if (wrapperItem) {
                        res.appendChild(wrapperItem)
                        wrapperItem = settingUtil.create('div', 'col-md-12 noPadding')
                        wrapperItem.style.marginTop = '13px'
                    } else {
                        wrapperItem = settingUtil.create('div', 'col-md-12 noPadding')
                    }
                    wrapperItem.appendChild(nodeArr[i])
                } else {
                    wrapperItem.appendChild(nodeArr[i])
                }
            }
            if (wrapperItem) {
                res.appendChild(wrapperItem)
            }
            // 把除报警开关的所有列包裹在一个div里面，方便与开关的联动
            settingUtil.withBox(res)
            return res
        },
        withBox: function (res) {
            var addBox
            var divs = res.querySelectorAll('.col-md-12')
            if (divs.length > 2) {
                addBox = settingUtil.create('div', 'col-md-12 noPadding')
                divs.forEach(function (item, idx) {
                    if (idx > 0) {
                        addBox.appendChild(item)
                    }
                })
            }
            if (addBox) {
                res.appendChild(addBox)
            }
        },
        // 修复和公共参数不一样得个别字段
        dataFix: function (dataOrigin, index, correctData) {
            if (correctData) {
                dataOrigin[index] = correctData
            }
            return dataOrigin
        },
        // 从公共/普通参数集合中选取需要的数据
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
        /**
         * 从页面提取参数设置  输入框值为空时,设置该值为-1
         * @param form form元素，( $('#xxxForm') )
         * @returns {{otherParamSettings: [], commonParamSetting: {vehicleId: (jQuery|string|undefined), protocolType}}}
         */
        getFormData: function (form, protocolType) {
            var paramList = form.serializeArray();
            var paramType = form.find('.paramType').val();
            var result
            var commonParamSettingObj = {// 通用参数
                vehicleId: $('#vehicleId').val(),
                protocolType: protocolType,
                paramType: paramType
            };
            var otherParamSettings = [];// 其它参数
            var alarmObj = {};
            for (var i = 0, len = paramList.length; i < len; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                if (attrName.split('_')[1] == undefined) continue
                if (attrName.split('_')[1].length == 2) {// 组装通用参数
                    if ((attrName.indexOf('timeDistanceThreshold') != -1
                        || attrName.indexOf('timeThreshold') != -1
                        || attrName.indexOf('photographTime') != -1
                    ) && attrValue != '-1') {// 时距阈值需要*10
                        attrValue = attrValue * 10;
                    }
                    commonParamSettingObj[attrName.split('_')[0]] = attrValue;
                } else {// 组装报警参数
                    var alarmName = attrName.split('_')[0];
                    var alarmId = attrName.split('_')[1];
                    if (!alarmObj[alarmId]) {
                        alarmObj[alarmId] = {
                            'riskFunctionId': alarmId,
                            'vehicleId': $('#vehicleId').val(),
                            'paramType': paramType,
                        };
                    }
                    if ((alarmName.indexOf('timeDistanceThreshold') != -1
                        || alarmName.indexOf('timeThreshold') != -1
                        || alarmName.indexOf('photographTime') != -1
                    ) && attrValue != '-1') {// 时距阈值需要*10
                        attrValue = attrValue * 10;
                    }
                    if (alarmName.indexOf('warningTimeThreshold') != -1 && attrValue != '-1') {// 预警时间阈值*10
                        attrValue = parseInt(attrValue) * 10;
                    }
                    alarmObj[alarmId][alarmName] = attrValue;
                }
            }
            for (var key in alarmObj) {
                if (alarmObj.hasOwnProperty(key)) {
                    otherParamSettings.push(alarmObj[key]);
                }
            }
            result = {
                'commonParamSetting': commonParamSettingObj,
                'adasAlarmParamSettings': otherParamSettings
            };
            return result;
        },
        /**
         * 从页面提取【平台】参数设置  输入框值为空时,设置该值为-1
         * @param form form元素，( $('#xxxForm') )
         * @returns object
         */
        getPlatformFormData: function (curForm) {
            var paramList = curForm.serializeArray();
            var alarmObj = {};
            var paramType = curForm.find('.paramType').val();
            for (var i = 1; i < paramList.length; i++) {
                var item = paramList[i];
                var attrName = item.name;
                var attrValue = (item.value == '' ? '-1' : item.value);
                var alarmName = attrName.split('_')[0];
                var alarmId = attrName.split('_')[1];
                if (!alarmObj[alarmId]) {
                    alarmObj[alarmId] = {
                        'riskFunctionId': alarmId,
                        'vehicleId': $('#vehicleId').val(),
                        'protocolType': paramType,
                    };
                }
                if (alarmName.indexOf('processingIntervalOne') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }
                if (alarmName.indexOf('processingIntervalTwo') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }
                if (alarmName.indexOf('timeThreshold') != -1 && attrValue != '-1') {
                    attrValue = attrValue * 60;
                }

                alarmObj[alarmId][alarmName] = attrValue;
            }
            return alarmObj;
        },


        /**
         * 根据id值产生验证规则
         * @param riskIds
         * @param origin
         * @returns {{messages: {}, rules: {}}}
         */
        generateValidateObj: function (riskIds, validateList) {
            var validateObj = {};
            var validateMsg = {};
            var generate = function (obj, riskId) {
                var ruleObj = settingUtil.generateRuleAndMsg(obj)
                validateObj[obj.name + '_' + riskId] = ruleObj.rule
                validateMsg[obj.name + '_' + riskId] = ruleObj.message
            }
            for (var i = 0; i < riskIds.length; i++) {
                var riskId = riskIds[i];
                for (var j = 0; j < validateList.length; j++) {
                    generate(validateList[j], riskId)
                }
            }
            return {rules: validateObj, messages: validateMsg};
        },
        /**
         * 根据ruleObj产生验证对象
         * @param ruleObj
         * @returns {{rule: {}, message: {}}}
         */
        generateRuleAndMsg: function (ruleObj) {
            var message = {}
            var rule = {}
            ruleObj.ruleTitle.forEach(function (item, index) {
                message[item] = ruleObj.message
                rule[item] = ruleObj.rule[index]
            })
            return {
                rule: rule,
                message: message
            }
        },
        /**
         * 修正一些有差异的数据校验
         * @param origin
         * @param fixMsg
         */
        fixValidateInfo: function (origin, fixMsg) {
            var riskFunctionId = ''
            var key = ''
            var res = null
            if (typeof fixMsg.dataIndex == 'string') {
                if (Object.prototype.toString.call(fixMsg.riskFunctionId) == '[object Array]') {
                    fixMsg.riskFunctionId.forEach(function (riskId, index) {
                        res = settingUtil.generateRuleAndMsg(fixMsg.data)
                        origin.messages[fixMsg.dataIndex + '_' + riskId] = res.message
                        origin.rules[fixMsg.dataIndex + '_' + riskId] = res.rule
                    })
                } else {
                    riskFunctionId = fixMsg.riskFunctionId || ''
                    key = fixMsg.dataIndex
                    res = settingUtil.generateRuleAndMsg(fixMsg.data)
                    if (riskFunctionId) {
                        origin.messages[key + '_' + riskFunctionId] = res.message
                        origin.rules[key + '_' + riskFunctionId] = res.rule
                    } else {
                        origin.messages[key] = res.message
                        origin.rules[key] = res.rule
                    }
                }
            } else if (Object.prototype.toString.call(fixMsg.dataIndex) == '[object Array]') {
                fixMsg.dataIndex.forEach(function (item, index) {
                    riskFunctionId = fixMsg.riskFunctionId[index]
                    res = settingUtil.generateRuleAndMsg(fixMsg.data)
                    if (riskFunctionId) {
                        origin.messages[item + '_' + riskFunctionId] = res.message
                        origin.rules[item + '_' + riskFunctionId] = res.rule
                    } else {
                        origin.messages[item] = res.message
                        origin.rules[item] = res.rule
                    }
                })
            }
        },


        // 报警开关切换时的联动
        alarmChangeEvent: function (target) {
            var _this = target.target ? $(this) : target;
            var sibCheckedRadios = _this.closest('.col-md-12').find('input[type="radio"]:checked');
            var next = _this.closest('.col-md-12').next('.col-md-12');
            var valArr = []
            sibCheckedRadios.each(function (idx, item) {
                valArr.push($(item).val())
            })
            if (valArr.every(function (value) {
                return value == 0
            })) { // 都为关闭状态,隐藏相关参数
                next.slideUp();
                next.find('input,select').prop('disabled', true);
            } else {
                next.slideDown();
                next.find('input,select').prop('disabled', false);
            }
        },
    }
    window.settingRenderer = {
        init: function (allSetting) {
            allSetting.forEach(function (item, index) {
                var $commonContent, $otherContent
                // 公共参数渲染
                $commonContent = $('#' + item.id).find('.commonContent')
                $commonContent.html('')
                $commonContent.append(settingRenderer.renderCommonSetting(item.commonSetting, index))
                // 其它参数渲染
                // $otherContent = $('#' + item.id).find('.otherContent')
                $otherContent = $('#' + item.id).find('.adasContent')
                $otherContent.html('')
                $otherContent.append(settingRenderer.renderOtherSetting(item.otherSetting, index))
                // 提示语渲染
                if (item.tips) {
                    $commonContent.append(settingUtil.renderTips(item.tips))
                }
            })
        },
        // 渲染公共参数入口
        renderCommonSetting: function (commonSettings, index) {
            var commonSettingDom = document.createDocumentFragment()
            commonSettings.forEach(function (item) {
                try {
                    var groupDom, groupDom_title, groupDom_content
                    groupDom = settingUtil.create('div', "form-group")
                    // 标题渲染
                    groupDom_title = settingUtil.create('div', 'col-md-2')
                    groupDom_title.innerHTML = '<label class="control-label">' + item.label + ' </label>'
                    groupDom.appendChild(groupDom_title)
                    // 内容渲染
                    groupDom_content = settingUtil.create('div', 'col-md-10 noPadding')
                    if (item.itemRender) {
                        var res = item.itemRender(item.data, item.riskFunctionId)
                        if (typeof res == 'string') {
                            groupDom_content.innerHTML = res
                        } else {
                            groupDom_content.appendChild(res)
                        }
                    } else {
                        groupDom_content.appendChild(settingUtil.renderCommonData(item, item.riskFunctionId))
                    }
                    groupDom.appendChild(groupDom_content)
                    if (item.notShow) {
                        groupDom.style.display = 'none'
                        // 该组下的所有input select radio 添加disabled属性
                        $(groupDom).find('input,select').prop('disabled', true);
                    }
                    // 添加到临时的dom集合中
                    commonSettingDom.appendChild(groupDom)
                } catch (e) {
                    console.error('页签' + index + '【公共参数】渲染出错', item)
                }
            })
            return commonSettingDom
        },
        // 渲染其它参数入口
        renderOtherSetting: function (otherSettings, index) {
            var otherSettingDom = document.createDocumentFragment()
            otherSettings.forEach(function (item) {
                try {
                    var groupDom, groupDom_title, groupDom_content
                    groupDom = settingUtil.create('div', "form-group")
                    if (item.notShow) {
                        groupDom.style.display = 'none'
                    }
                    // 标题渲染
                    groupDom_title = settingUtil.create('div', 'col-md-2')
                    groupDom_title.innerHTML = '<label class="control-label">' + item.label + ' </label>'
                    groupDom.appendChild(groupDom_title)
                    // 内容渲染
                    groupDom_content = settingUtil.create('div', 'col-md-10 noPadding')
                    if (item.itemRender) {
                        var res = item.itemRender(item.data, item.riskFunctionId)
                        if (typeof res == 'string') {
                            groupDom_content.innerHTML = res
                        } else {
                            groupDom_content.appendChild(res)
                        }
                    } else {
                        groupDom_content.appendChild(settingUtil.renderOtherData(item, item.riskFunctionId))
                    }
                    groupDom.appendChild(groupDom_content)
                    // 添加到临时的dom集合中
                    otherSettingDom.appendChild(groupDom)
                } catch (e) {
                    console.error('页签' + index + '【其它参数】渲染出错', item)
                }
            })
            return otherSettingDom
        },
        // 平台参数渲染
        platformInit: function (allSetting, containerId) {
            var $container = $('#' + containerId)
            var getLiDom = function (riskFunctionId, name, type) {
                var li = settingUtil.create('li', 'clearfix')
                var div1 = settingUtil.create('div', 'form-group')
                li.appendChild(div1)
                var div2_1 = settingUtil.create('div', 'col-md-1')
                div2_1.innerHTML =
                    ('<div class="leftselectbutton leftselectbutton2">\n' +
                        '    <input name="alarmSwitch_@riskFunctionId@" value="0" class="selectvalue alarmSwitch_@riskFunctionId@ valid" hidden="true">\n' +
                        '    <span class="selectbutton" style="width: 20px; height: 20px; position: absolute; left: 55px; top: 7px;"></span>\n' +
                        '    <span class="button0 button0flag"></span>\n' +
                        '    <span class="button1 button1flag"></span>\n' +
                        '</div>').replace(/@riskFunctionId@/g, riskFunctionId)
                div1.appendChild(div2_1)
                var div2_2 = settingUtil.create('div', 'col-md-1 alarmSet')
                div2_2.innerHTML =
                    ('<div class="checkbox-outline" style="text-align: center;">\n' +
                        '    <input type="checkbox" value="@riskFunctionId@" hidden="">\n' +
                        '    <label class="typeName" readonly="" >@name@</label>\n' +
                        '</div>').replace(/@riskFunctionId@/g, riskFunctionId).replace(/@name@/g, name)
                div1.appendChild(div2_2)
                var div2_3 = settingUtil.create('div', 'col-md-10')
                div2_3.style.display = 'none'
                if (type == 1) {
                    div2_3.innerHTML =
                        ('<div class="form-group" style="margin-top:7px">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-12 " style="font-weight: 600;text-align: center;">一级报警</label>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-12 " style="font-weight: 600;text-align: center;">二级报警</label>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="alarmRemindOne_@riskFunctionId@" class="form-control valid" aria-invalid="false">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="alarmRemindTwo_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">处理间隔(min):</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <input type="text" class="form-control valid" name="processingIntervalOne_@riskFunctionId@" placeholder="请输入处理时间，范围10~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">处理间隔(min):</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <input type="text" class="form-control valid" name="processingIntervalTwo_@riskFunctionId@" placeholder="请输入处理时间，范围10~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="单位时间或单位里程内接收到来自同一辆车的相同报警超过一定数量,可改变提醒方式">\n' +
                            '    </i>\n' +
                            '    <label class="control-label">其他策略设置</label>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">时间阈值(min):</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="timeThreshold_@riskFunctionId@" placeholder="范围：1~1440">\n' +
                            '        </div>\n' +
                            '        <label class="col-md-2 control-label" style="margin-left:20px">报警数量:</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="timeAlarmNumThreshold_@riskFunctionId@" placeholder="范围：1~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="timeAlarmRemind_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">距离阈值(km):</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="distanceThreshold_@riskFunctionId@" placeholder="范围：1~960">\n' +
                            '        </div>\n' +
                            '        <label class="col-md-2 control-label" style="margin-left:20px">报警数量:</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="distanceAlarmNumThreshold_@riskFunctionId@" placeholder="范围：1~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="distanceAlarmRemind_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>').replace(/@riskFunctionId@/g, riskFunctionId)
                } else {
                    div2_3.innerHTML =
                        ('<div class="form-group" style="margin-top:7px">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-12 " style="font-weight: 600;text-align: center;">报警提示</label>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="alarmRemindTwo_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">处理间隔(min):</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <input type="text" class="form-control valid" name="processingIntervalTwo_@riskFunctionId@" placeholder="请输入处理时间，范围10~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <i class="fa fa-question-circle fa-lg infoTitle" data-toggle="tooltip" data-placement="top" data-original-title="单位时间或单位里程内接收到来自同一辆车的相同报警超过一定数量,可改变提醒方式">\n' +
                            '    </i>\n' +
                            '    <label class="control-label">其他策略设置</label>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">时间阈值(min):</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="timeThreshold_@riskFunctionId@" placeholder="范围：1~1440">\n' +
                            '        </div>\n' +
                            '        <label class="col-md-2 control-label" style="margin-left:20px">报警数量:</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="timeAlarmNumThreshold_@riskFunctionId@" placeholder="范围：1~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="timeAlarmRemind_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>\n' +
                            '<div class="form-group">\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">距离阈值(km):</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="distanceThreshold_@riskFunctionId@" placeholder="范围：1~960">\n' +
                            '        </div>\n' +
                            '        <label class="col-md-2 control-label" style="margin-left:20px">报警数量:</label>\n' +
                            '        <div class="col-md-3" style="padding-right:0;">\n' +
                            '            <input type="text" class="form-control valid" name="distanceAlarmNumThreshold_@riskFunctionId@" placeholder="范围：1~2880">\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '    <div class="col-md-6">\n' +
                            '        <label class="col-md-3 control-label">报警提醒方式:</label>\n' +
                            '        <div class="col-md-9 noRightPadding">\n' +
                            '            <select name="distanceAlarmRemind_@riskFunctionId@" class="form-control valid">\n' +
                            '                <option value="0" selected="">无</option>\n' +
                            '                <option value="1">闪烁</option>\n' +
                            '                <option value="2">提示音</option>\n' +
                            '                <option value="3">闪烁+提示音</option>\n' +
                            '                <option value="4">弹窗提醒</option>\n' +
                            '                <option value="5">短信提醒</option>\n' +
                            '            </select>\n' +
                            '        </div>\n' +
                            '    </div>\n' +
                            '</div>').replace(/@riskFunctionId@/g, riskFunctionId)
                }
                div1.appendChild(div2_3)
                return li
            }
            var all = document.createDocumentFragment()
            allSetting.forEach(function (item) {
                all.appendChild(getLiDom(item.id, item.name, item.type))
            })
            $container.append(all)
            settingRenderer.bindPlatformEvent()
        },
        bindPlatformEvent: function () {
            // lable点击移动滑块
            var labelClickFn = function () {
                var left = $(this).parent().parent().parent().find(".selectbutton").css('left');
                if (left == '55px') {
                    $(this).parent().parent().parent().find('.selectbutton').animate({left: "9px"}, "fast");
                    $(this).parent().parent().parent().find('.selectvalue').val('1');
                    $(this).parent().parent().parent().find('.col-md-10').slideDown();
                } else if (left == '9px') {
                    $(this).parent().parent().parent().find('.selectbutton').animate({left: "55px"}, "fast");
                    $(this).parent().parent().parent().find('.selectvalue').val('0');
                    $(this).parent().parent().parent().find('.col-md-10').slideUp();
                }
            }
            // 开关开
            var open = function () {
                $(this).parent().parent().find('.selectbutton').animate({left: '9px'}, 'fast');
                $(".selectvalue").val('1');
                $('.clearfix .col-md-10').slideDown();
            }
            // 开关关
            var off = function () {
                $(this).parent().parent().find('.selectbutton').animate({left: '55px'}, 'fast');
                $('.selectvalue').val('0');
                $('.clearfix .col-md-10').slideUp();
            }
            // 滑块切换
            var selectSwitch = function () {
                var left = $(this).css('left');
                var leveAlarmInfo = $(this).parent().parent().parent().find('.col-md-10');
                var className = $(this).attr('class');
                if (className == 'button0 button0flag') {
                    $(this).parent().find('.selectvalue').val('1');
                    leveAlarmInfo.slideDown();
                }
                if (className == 'button1 button1flag') {
                    $(this).parent().find('.selectvalue').val('0');
                    leveAlarmInfo.slideUp();
                }

                $(this).siblings('.selectbutton').animate({left: left}, 'fast');
            }
            $(".typeName").bind('click', labelClickFn);
            $('.open').on('click',open)
            $('.off').on('click',off)
            $(".leftselectbutton span").on('click', selectSwitch)
        }
    }

    // 示例 参考粤标 defineYueSetting.js
    // 说明文档参见

    // 渲染
    // settingRenderer.init(domData)

    // 验证规则
    // settingUtil.generateValidateObj(validateArr, otherValidateMsg)

    // 获取页面参数
    // settingUtil.getFormData($form, protocolType)
}(window, $));
