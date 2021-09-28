

// 使用方法
// var swiper = new Swiper('.swiper',{
    // srcArr: ['./11.png','./22.png','./33.png']
    // tagName: 'img', // 渲染的标签名 img | vedio
    // indicatorType:'dot', // 指示器类型 dot | number(e.g., 1/6)
    // indicator: false, // 是否展示指示器（小圆点）
    // button: true, // 是否显示左右按钮
    // dotClass:'', // 小圆点样式名
    // dotActiveClass:'', // 小圆点激活样式名
    // leftDivClass:'leftDivClass', // 左边盒子样式名
    // rightDivClass:'rightDivClass', // 右边盒子样式名
    // indicatorDivClass:'indicatorDiv', // 小圆点外面的盒子的样式名
    // imgWrapperClass:'wrapper-content', // 小圆点外面的盒子的样式名
    // space:10, // 间距
    // widthRatio: 0.8, // 占外面盒子宽度的百分比
    // empty: '<p>暂无视频</p>'
// })

/**
 * 普通构造函数版
 */
function Swiper(selector,options){
    options = options || {}
    if(!selector) throw 'You must specify a selector to define the swiper container'
    // 不提供样式时的默认样式
    this.localStyles = {
        buttonStyles:{},
        indicatorStyles:'width:100%;height:30px;justify-content: center;display: flex;align-items: center;margin-top: 10px;',
        dotStyle:'border-radius: 100px;display: inline-block;height: 16px;width: 16px;text-align: center;line-height: 16px;font-size: 12px;margin: 0 5px;color: #666666;cursor: pointer;border: 1px solid #666666;',
        dotActiveStyle: 'border-radius: 100px;display: inline-block;height: 16px;width: 16px;text-align: center;line-height: 16px;font-size: 12px;margin: 0 5px;color: #666666;cursor: pointer;background: #0d88fb;color: #fff;border: 1px solid #0d88fb;',
        leftDivStyle:'',
        rightDivStyle:'',
    }
    // 默认配置
    this.defaultSettings = {
        tagName: 'img', // 渲染的标签名 img | vedio
        indicatorType:'number', // 指示器类型 dot | number(e.g., 1/6)
        indicator: true, // 是否展示指示器（小圆点）
        button: true, // 是否显示左右按钮
        dotClass:'', // 小圆点样式名
        dotActiveClass:'', // 小圆点激活样式名
        leftDivClass:'leftDivClass', // 左边盒子样式名
        rightDivClass:'rightDivClass', // 右边盒子样式名
        indicatorDivClass:'indicatorDiv', // 小圆点外面的盒子的样式名
        imgWrapperClass:'wrapper-content', // 小圆点外面的盒子的样式名
        space: 0, // 间距
        widthRatio: 1, // 占外面盒子宽度的百分比
        height:''
    }

    this.options = Object.assign(this.defaultSettings,options)
    this.outerBox = $(selector)
    this.outerBox.empty()
    this.outerBox.css('position','relative')

    this.wrapperDiv = null
    this.imgWrapperDiv = null
    this.leftDiv = null
    this.rightDiv = null
    this.indicatorDiv = null

    this.outerWidth = this.outerBox.width()
    this.outerHeight = this.outerBox.height()
    this.wrapperWidth = ~~(this.outerWidth * this.options.widthRatio)
    this.wrapperHeight = ~~(this.outerHeight - 30)

    this.length = 0
    this.current = 0
    this.render()
}
// 左右箭头图片 base64
Swiper.prototype.rightArrowImg = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAATSElEQVR4Xu2de5RdVX3Hf/vePDAgJooiKD5BFAVFHmpEARVEXorIjFYpohAw4Z6z95kxDYowFAQDSfY+M0kDo2DAiESqCFis2kprS6GVVqjWtlZb7cNHX5nUJk3CzPxce60dGyGTuY/zPfc8fmctFn9k7+93n+/vfNaeve++9ygq+WWMeToRncXMRxPRiUqpFxLRg8z8LaXUY9bau0t+izL8Piag+ujds7XW+r1EdBURHbYXsS8R0dXOuUd7NhSB2iVQWkC01huJyAPSzjURIHHtNJY2ksCuBEoJiNZ6GRGt7aKMfiYZ6aKfdKlpAqUDJI7jo/zaood6XeOcu7KH/tK1RgmUDhCt9VeJ6NReaqSUutZa+7FeNKRvPRIoFSBRFB3TaDQeyag0H3fOXZGRlshUNIFSAWKMWcLMN2dYi+udcx/JUE+kKpZAqQDRWns4lmRZA2b+RJqml2epKVrVSaBsgNxDRGdnHb9SaqW1dkXWuqJX/gTKBshyIlqJiJ2Zb0zT1OvLJQn8KoFSAWKMOZWZ/S4W6lrlnPswSlx0y5dAqQCJ4/hApdTPkDErpVZba4eRHqJdngRKBYiPVWvtPwn3569gFzPbNE0TmIEIlyaB0gESIHmAiE4Cp+yccwbsIfIFT6CsgHg4PCToK3XOabSJ6Bc3gVICEmaRdxHRJiJqgOMdc85FYA+RL2gCpQUkQPJOIvo8ETWR+Sql1lprW0gP0S5mAqUGxEdqjDmHmf1MMhcc8Trn3GVgD5EvWAKlByRA8nZm9jPJPHC+651zS8EeIl+gBCoBiM8zjuOzG43GJmbeB5kvM9+UpumHkB6iXZwEKgOIjzRJkjOnp6f9TPIUcMQ3O+cuBXuIfAESqBQgYeF+Rtjd2hec77hz7hKwh8j3OYHKARIgeVvY3doPnO+nnHMXgz1Evo8JVBKQsHA/Lexu7Y/MVyl1i7X2IqSHaPcvgcoCEiDxp3/9muRp4Ihvdc59EOwh8n1IoNKAhN2tU8Lu1iJwvhuccxeCPUQ+5wQqD0jY3Xpz2N3yP1OKvG5zzr0faSDa+SZQC0DCwv1NYXfrAHDEtzvnLgB7iHxOCdQGkACJPwXs1yTPRObLzBvTND0f6SHa+SRQK0DCwv3EsLt1IDjizzrn3gf2EHlwArUDJEDyBr8mUUo9G5mvUuoOa227P7CNHIpod5lALQHxWUVRdILf3SKig7vMrq1uzHxnmqbvaauxNCpcArUFxFciSZLFYXfrOeDKbHLOvRvsIfKABGoNSFi4vy7sbh0CyHd3yc875wbBHiKfcQK1B8TnGcfxa5RSfnfreRnn+0S533XODRARg31EPqMEBJAQpDHm+LC79YKMst2jDDN/YdGiRQMjIyPTSB/RziYBAWS3HI0xx4bdLf8iUOT1xYULFw6OjIxMIk1Eu/cEBJAnZBjeQeJ3t17ce7wzKyil7t66devg+Pj440gf0e4tAQFkD/klSXJ02N06tLd4Z+19z8KFC/2fWztnbSkN+pKAADJD7FrrV4XdrZeAK3PvxMTE4IYNG7aDfUS+iwQEkL2EFl4Y6ne3Du8i20663KeUGrTW/l8nnaQtPgEBZJaMjTFHht2tl4HL8Xvbtm0bGB8f3wb2EfkOEhBA2gjLGPPysLt1RBvNu26ilLq/2WwOrFq1amvXItIx0wQEkDbjbLVaRzSbTb+79Yo2u3Tb7Cvz5s0bvOGGG37RrYD0yy4BAaSDLIeGhl46NTXl1yRHdtCtm6ZfnZqaGhgbG/ufbjpLn+wSEEA6zDKO48OVUn4meWWHXTtt/rX58+cPrFy5ckunHaV9dgkIIF1kGUXRYY1Gw88kfisYeX19x44dg+vXr9+MNBHtmRMQQLp8Oowxh4bdrVd3KdFWN2b+w0ajMWCt/e+2OkijTBMQQHqI0xjzorC7dUwPMu10/cbjjz8+sG7duv9qp7G0yS4BAaTHLLXW/vSv/3PruB6l9tpdKfXA5OTk4NjY2H8gfUT71xMQQDJ4IoaGhp4fdreOz0BubxJ/PGfOHP85yb+DfUQ+JCCAZPQoJElyiIdEKfXajCRnkvkmMw+kafpzsI/IE5EAkuFj0Gq1nttsNv2fW/5rvMjrT4ho0Dn3U6SJaAsgmT8Dy5cvP3jnzp0ektdnLv7rgg9OTk4OrF279idgn1rLywwCKL/W+iD/q/JKqRMA8r+SZOY/C1vA/4b0qbO2AAKqfhzHB4YfgngjyGKX7EPhWMq/gn1qKS+AAMs+PDz8rMnJSf/n1olAG1JKPTw1NTU4Ojr6z0ifOmoLIOCqDw0NHeA/TGTmk8FWf+GPyq9evfrHYJ9ayQsgOZR72bJlz5g7d66fSfwrGJDXt4howDn3I6RJnbQFkJyqvWLFikXbt2/3kLwFbPlI+PruP4J9aiEvgORYZq31wnAs5RSw7V+FhfsPwT6VlxdAci5xq9XaP3yY+FakNTN/O2wB/wDpU3VtAaQPFV6+fPlTd+zY4T8nOQ1s/2ij0Rhcs2bN98E+lZUXQPpU2uHh4X392S1mPh08hL8OZ7f+HuxTSXkBpI9lXbJkyYIFCxb4hfsZ4GF8J2wB/x3Yp3LyAkifSzoyMrLPxMSEh+Qs8FC+6z9MHBsb+x7Yp1LyAkgBytlqteaHhfvZyOEw8/fCwv1vkD5V0hZAClLNJUuWzA1/br0DPKS/nZ6eHhgdHf0u2KcS8gJIgco4MjIyZ/PmzX536xzksJRSfi3ifwjiO0ifKmgLIAWr4sjISGPLli1+d+tc8NC+7//cWrNmzWNgn1LLCyDFLJ8yxmxi5vPAw/uHcHbrUbBPaeUFkAKXTmvtf8HRv/QTef0gzCTfRpqUVVsAKXjljDGfY2b0O9Z/OD097b9P8pcFjyP34QkguUfeuaHW+g4iek/nPdvvwcz/FLaAH2m/V/VbCiAlqbHWeiMRvRc83B+FYyn+eyVyyc/+lOsZMMZ8hpnfhxy1UurH/s+tNE3/HOlTFm2ZQcpSqTBOrfVtRPSb4GH/S1i4Pwz2Kby8AFL4Ej15gFrrDUR0AXjo/ldS/Nd3HwL7FFpeACl0eWYenDHmVma+EDx8/3tb/hccHwT7FFZeAClsaWYfmNb6FiL6wOwte2rx03B26097UilpZwGkpIXbNWyt9SeJ6CLkbTDzz8IWsP9N4FpdAkgFym2MGWfmi8G38vOwBfxNsE+h5AWQQpWj+8ForW8ioku6V2irp395j1+4/1FbrSvQSACpQBF33UIcx+uVUpeCb+k/wxbwA2CfQsgLIIUoQ3aDMMasY+al2SnuUcm/K9HPJN8A+/RdXgDpewmyH4DWei0RLcte+f8VmXlzWLj/AdKn39oCSL8rAPLXWo8SUQskv0t2Iizcvw726Zu8ANK36PHGxpiUmSOkk1JqS/j67teQPv3SFkD6lXxOvlprS0QabPeL8GHi74N9cpcXQHKPPH/DOI7XKKUM2Pl/w8L9K2CfXOUFkFzj7p+ZMWYVMw+BR7A1bAHfD/bJTV4AyS3q/htprW8komHwSLYxs/8+yZfBPrnICyC5xFwcE631SiJajhwRM28PW8D3IX3y0BZA8ki5YB7GmOuZeQV4WDvCFvC9YB+ovAACjbe44lrr64jocuQIlVI7wxbwPUgfpLYAgky34Npa62uJ6KPgYU6GLeC7wT4QeQEEEmt5RI0x1zDzFeART4Ut4C+CfTKXF0Ayj7R8gsaYq5n5SvDIp8MW8BfAPpnKCyCZxlleMa31CBFdhb6DsHC/C+2Tlb4AklWSJdYxxrx8enrav3bhCORtKKXustaif2s401sQQDKNs3xiHg7/7nZmhsLhPZxzg2VLSAApW8UyHG8URa9oNBr+/Ygvy1B2T1KlhMPfiAACfjKKKp8jHJucc+hfp4fFLIDAoi2usDHmyLDmeCl4lKWGQ2YQ8NNRRHkPR1hzQOFQSt1prYW+siGPfGUGySPlgnjEcXyUUsqvOQ5HDomZ70zTtPRwyAyCfEoKpi1wdFcQmUG6y61UvZIkeaVfcxDRS8AD/5xz7jfAHrnKCyC5xp2/mcDRW+YCSG/5Fbq31vpVfkFORIchB6qUusNai349HPIWZtQWQPoSO940LziY+Y40TSsJhyzS8c9pXxxardbRzWbTzxyHggfwWecc9J2J4PHPKi8zyKwRlatBkiRHhwW5wJFB6QSQDEIsioTW+tVhzfFi8JgqP3Psyk8AAT9JecnnBYdSaqO19vy87qvfPgJIvyuQgX8URceEU7kvykBuRglm3pimaW3gkEU68mnKSXtoaOiYqakpvyCHwkFEn3HOod/PnlNq7dvIDNJ+VoVraYw5NpzKfSF4cLWEQ2YQ8FOFlPdwhFO5aDhud85dgLyXImvLDFLk6swwtjiOjwuncl8AHn6t4ZAZBPx0IeRzhOM259z7EfdQJk2ZQUpULWPM8czsF+TPBw9b4AgBCyDgJy0reQ9HWHMIHFmF2oaOANJGSP1uEsfxa8Ka43ngsWxwzl0I9iiVvABS8HIJHP0tkADS3/z36p4kyWvDwcNDkMNUSn3aWvsBpEdZtQWQglYuLziY+dNpmgocMzwHAkgBAdFavy6cyn0ueHi3Ouc+CPYotbwAUrDyCRzFKogAUqB6RFG0OJzKfQ54WDJztBmwANJmUOhmSZIsDgtyKBxKqVustReh76cq+gJIASqptX59WHMcjBwOM9+SpqnA0UHIAkgHYSGa5gUHEX3KOXcx4h6qrCmA9LG6URSdENYcB4GHIXB0GbAA0mVwvXYbGho6IXwTEAqHUuqT1tolvY63rv0FkD5U3hjzhvBNwGcj7QWO3tMVQHrPsCMFD0c4lQuFg4jGnXOXdDQ4afykBASQHB+KOI7fGE7lHgi2FTgyClgAySjI2WQEjtkSKua/CyA51MUYc2L4JuCzwHY3O+cuBXvUSl4AAZfbwxHWHAIHOGuEvACCSDVoaq1PCp+QPxNoQ8x8U5qmH0J61FVbAAFVPoqikxuNxiYiEjhAGechK4AAUk6S5ORw8PAAgPzukuudc0vBHrWWF0AyLr/W+k1E5GcOgSPjbPshJ4BkmHqAw/9u1TMylN2TlMwc4IB3yQsgGQUdRdGbw5oDCgcz/06apssyGrbIzJKAAJLBIxLg8DPH0zOQm1FC4ECmu2dtAaTHzI0xb2Fmv+aAwkFE65xzl/U4XOneYQICSIeB7d7cwxE+BFzUg0w7XQWOdlICtBFAugw1juNT/JqDmaFwKKXWWmtbXQ5TuvWYgADSRYAejnAqd2EX3dvuInC0HRWsoQDSYbTGmFPDmgMKBxGNOeeiDocnzTNOQADpINAAh9+teloH3bppKnB0kxqgjwDSZqhxHL81rDkEjjYzq0IzAaSNKno4wppj/zaad91EKTVqrY27FpCOmScggMwSaRRFp4VPyAWOzB+/4gsKIHupkTHmtPBNwKeCS5k65zTYQ+S7SEAAmSE0rfXbwqlcgaOLB6sqXQSQPVQywOF3q/YDF9o55wzYQ+R7SEAAeUJ4URSdHtYcAkcPD1ZVugogu1UySZLTwzcB9wUXWGYOcMBZyQsgIUmt9RlhzQGFg5ltmqZJVgUUHWwCAggRBTj8mmMBMm6BA5kuRrv2gMRxfKZSyn+fAwoHEa1xzg1hyiiqqARqDUiAw88cT0EFHHQFDnDAKPnaAmKMOSucyoXCoZRaba0dRhVQdLEJ1BIQD0d4P8c+yHiZeXWapgIHMmSwdu0AieP47HAqV+AAP1xVkK8VIB6OcCp3Prh4q5xzHwZ7iHwOCdQGEGPM28OaQ+DI4cGqikUtAAlw+N2qecjCKaVutNYuR3qIdr4JVB4QrfU7/OcczCxw5PtsVcKt0oB4OML7OeaCq3WDc+63wB4i34cEKgtIFEXnhFO5AkcfHqyqWFYSEGPMOeGbgHOQhVJKrbTWrkB6iHZ/E6gcIFrrd4ZTuQJHf5+tSrhXCpAAh9+taiKrIzMHMt1iaVcGkDiOzw2ncqFwMPMn0jS9vFhllNGgEqgEIEmSnBu+CdhABeV1BQ5kusXULj0gWut3hTUHFA4iut4595FillFGhUqg1IAEOPyaA30fAgfqCSy4LvrBgt1+HMfnhTUH9B6UUtdZaz8KuxERLnQC0IcLdeda65OI6AGU/i5dZr4uTVOBAx10gfXLCoiHw0MCuwQOWLSlEi4dIFrrESK6Cpzyx51zV4A9RL4ECZQKEK31QUT0E3CuAgc44DLJlwqQJEnOnJ6evg8VsFLqWmvtx1D6olu+BEoFCPLPK2a+Nk1TgaN8zzB0xGUD5F4iOguQyDXOuSsBuiJZ8gRKBYgxZoyZL8s4c4Ej40CrJFcqQLTW5xPR7RkWQODIMMwqSpUKkDiOj1JKPZZFIZRSv22tRW8XZzFU0ehjAqUCxOdkjHmQmRf3kpnA0Ut69epbOkDC69Hu76FMVzvn/IeNckkCsyZQOkD8HWmtR4moNevdPbmBwNFFaHXuUkpAAiSriajdNzVNEJGHw9W52HLvnSdQWkD8rUZRtLjZbN44y5rkSwGORzuPR3rUPYFSA+KLt3Tp0v3mzJlzXrPZPJaZjyMi/99DzPyw/3+apnfVvchy/90n8EtVMfkUypqSAAAAAABJRU5ErkJggg=='
Swiper.prototype.leftArrowImg = 'data:image/png;base64,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'
// 框架渲染
Swiper.prototype.renderFrame = function(){
    // 图片视频盒子 outer
    this.wrapperDiv = document.createElement('div')
    this.wrapperDiv.setAttribute("style", `width:${this.wrapperWidth}px;height:${this.options.height}px;overflow:hidden;margin:0 auto;position:relative`)
    this.wrapperDiv.className='wrapper'
    this.outerBox.append(this.wrapperDiv)
    // 图片视频盒子 inner
    this.imgWrapperDiv = document.createElement('div')
    this.imgWrapperDiv.setAttribute("style",`width:${this.wrapperWidth * this.length}px;height:100%;overflow:hidden;margin:0 auto;margin-left:0;transition:all ease 0.3s`)
    $(this.wrapperDiv).append(this.imgWrapperDiv)
    this.outerBox.append(this.indicatorDiv)
    // 左右按钮
    if(this.options.button){
        // 左
        this.leftDiv = document.createElement('div')
        this.leftDiv.setAttribute("style",`width:${~~(this.outerWidth*0.1)}px;position:absolute;left:0;top:0;bottom: 0px;display: flex;justify-content: center;align-items: center;opacity:0.6;cursor:not-allowed`)
        this.leftDiv.classList.add(this.options.leftDivClass)
        const leftArrow = document.createElement('img')
        leftArrow.style.width = '48%'
        leftArrow.src = this.leftArrowImg
        this.leftDiv.append(leftArrow)
        // 右
        this.rightDiv = document.createElement('div')
        this.rightDiv.setAttribute("style",`width:${~~(this.outerWidth*0.1)}px;position:absolute;right:0;top:0;bottom: 0px;display: flex;justify-content: center;align-items: center;`)
        this.rightDiv.classList.add(this.options.rightDivClass)
        if(this.length <= 1){
            this.rightDiv.style.opacity = 0.6
            this.rightDiv.style.cursor = 'not-allowed'
        }
        const rightArrow = document.createElement('img')
        rightArrow.style.width = '48%'
        rightArrow.src = this.rightArrowImg
        this.rightDiv.append(rightArrow)
        this.outerBox.append(this.leftDiv)
        this.outerBox.append(this.rightDiv)
        // this.wrapperDiv.append(this.leftDiv)
        // this.wrapperDiv.append(this.rightDiv)
    }
    if(this.options.indicator){
        // 指示器
        this.indicatorDiv = document.createElement('div')
        this.indicatorDiv.setAttribute("style",this.localStyles.indicatorStyles)
        this.indicatorDiv.classList.add(this.options.rightDivClass)
        this.outerBox.append(this.indicatorDiv)
    }
}
// 渲染数据
Swiper.prototype.render = function(){
    var srcArr = this.options.srcArr
    if(!(srcArr instanceof Array)) return
    this.length = srcArr.length
    this.renderFrame()

    let wrapperHtml = ''
    let indicatorHtml = ''
    const _this = this
    srcArr.forEach(function (item,index) {
        let space = `0 ${_this.options.space}px`
        wrapperHtml += `<div class="${_this.options.imgWrapperClass}" style="width:${_this.wrapperWidth - (_this.options.space * 2)}px;overflow: hidden;float: left;margin:${space};box-sizing: border-box;">
                            <div style="display: flex;justify-content: center;align-items: center;">
                                <${_this.options.tagName} controls src="${item}" style="height:${_this.options.height}px;width:auto"/></${_this.options.tagName}>
                            </div>
                        </div>`
        if(_this.options.indicatorType == 'dot'){
            if(_this.options.dotClass){
                indicatorHtml += index == 0 ? `<span class="${_this.options.dotActiveClass}">${index + 1}</span>` : `<span class="${_this.options.dotClass}">${index + 1}</span>`
            }else{
                indicatorHtml += index == 0 ? `<span style="${_this.localStyles.dotActiveStyle}">${index + 1}</span>` : `<span style="${_this.localStyles.dotStyle}">${index + 1}</span>`
            }
        }
        if(_this.options.indicatorType == 'number'){
            indicatorHtml = `- <span>1</span>/<span>${_this.length}</span> -`
        }
    })
    if(srcArr.length == 0){
        $(this.wrapperDiv).html(`<div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: center;">${this.options.empty}</div>`)
        indicatorHtml = `- <span>0</span>/<span>0</span> -`
    }
    $(this.imgWrapperDiv).html(wrapperHtml)
    $(this.indicatorDiv).html(indicatorHtml)
    this.bindEvent()
}
// 事件绑定
Swiper.prototype.bindEvent = function(){
    if(this.options.button){
        this.leftDiv.addEventListener('click',() => {this.left()})
        this.rightDiv.addEventListener('click',() => {this.right()})
    }
    if(this.options.indicator){
        this.indicatorDiv.addEventListener('click',(e) => {
            var index = $(e.target).html()
            this.swiperTo(index - 1)
        })
    }
}
Swiper.prototype.left = function(){
    this.swiperTo(this.current -1)
}
Swiper.prototype.right = function(){
    this.swiperTo(this.current + 1)
}
// 切换功能
Swiper.prototype.swiperTo = function(index) {
    if(index < 0 || index + 1 > this.length) return
    this.current = index
    var marginLeft = (-index * this.wrapperWidth) + 'px'
    $(this.imgWrapperDiv).css('margin-left',marginLeft)
    if(!this.leftDiv) return
    if(index == 0){
        this.leftDiv.style.opacity = 0.6
        this.leftDiv.style.cursor = 'not-allowed'
    }else {
        this.leftDiv.style.opacity = 1
        this.leftDiv.style.cursor = 'default'
    }
    if(index + 1 == this.length){
        this.rightDiv.style.opacity = 0.6
        this.rightDiv.style.cursor = 'not-allowed'
    }else {
        this.rightDiv.style.opacity = 1
        this.rightDiv.style.cursor = 'default'
    }
    this.dotActive(index)
}
// 指示器高亮
Swiper.prototype.dotActive = function(index) {
    if(this.options.indicatorType == 'dot'){
        const $dotList = $(this.indicatorDiv).find('span')
        const _this = this
        $dotList.each(function (i) {
            const {dotClass,dotActiveClass} = _this.options
            if(dotClass && dotActiveClass){
                $(this).removeClass(dotActiveClass)
                i == index && $(this).addClass(dotActiveClass)
            }else{
                $(this)[0].setAttribute('style',_this.localStyles.dotStyle)
                i == index && $(this)[0].setAttribute('style',_this.localStyles.dotActiveStyle)
            }
        })
    }else if(this.options.indicatorType == 'number'){
        $(this.indicatorDiv).find('span:first-child').html(index + 1)
    }
}
// 销毁
Swiper.prototype.destroy = function () {
    this.outerBox.empty()
}
/**
 *  class 版本
 */
class Swiper2{
    // 左右箭头的base64编码
    rightArrowImg = 'data:image/png;base64,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'
    leftArrowImg = 'data:image/png;base64,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'
    // 不提供样式时的默认样式
    localStyles = {
        buttonStyles:{},
        indicatorStyles:'width:100%;height:30px;justify-content: center;display: flex;align-items: center;margin-top: 10px;',
        dotStyle:'border-radius: 100px;display: inline-block;height: 16px;width: 16px;text-align: center;line-height: 16px;font-size: 12px;margin: 0 5px;color: #666666;cursor: pointer;border: 1px solid #666666;',
        dotActiveStyle: 'border-radius: 100px;display: inline-block;height: 16px;width: 16px;text-align: center;line-height: 16px;font-size: 12px;margin: 0 5px;color: #666666;cursor: pointer;background: #0d88fb;color: #fff;border: 1px solid #0d88fb;',
        leftDivStyle:'',
        rightDivStyle:'',
    }
    // 默认配置
    defaultSettings = {
        srcArr:null,
        tagName: 'img', // 渲染的标签名 img | vedio
        indicatorType:'number', // 指示器类型 dot | number(e.g., 1/6)
        indicator: true, // 是否展示指示器（小圆点）
        button: true, // 是否显示左右按钮
        dotClass:'', // 小圆点样式名
        dotActiveClass:'', // 小圆点激活样式名
        leftDivClass:'leftDivClass', // 左边盒子样式名
        rightDivClass:'rightDivClass', // 右边盒子样式名
        indicatorDivClass:'indicatorDiv', // 小圆点外面的盒子的样式名
        imgWrapperClass:'wrapper-content', // 小圆点外面的盒子的样式名
        space: 0, // 间距
        widthRatio: 1, // 占外面盒子宽度的百分比
    }
    constructor(selector,options = {}){
        if(!selector) throw 'You must specify a selector to define the swiper container'
        this.options = Object.assign(this.defaultSettings,options)
        this.outerBox = $(selector)
        this.outerBox.css('position','relative')

        this.wrapperDiv = null
        this.imgWrapperDiv = null
        this.leftDiv = null
        this.rightDiv = null
        this.indicatorDiv = null
        this.outerWidth = this.outerBox.width()
        this.outerHeight = this.outerBox.height()
        this.wrapperWidth = ~~(this.outerWidth * this.options.widthRatio)
        this.wrapperHeight = ~~(this.outerHeight - 30)
        this.length = 0
        this.current = 0

        this.render()
    }
    // 渲染整体框架
    renderFrame(){
        // 图片视频盒子 outer
        this.wrapperDiv = document.createElement('div')
        this.wrapperDiv.setAttribute("style", `width:${this.wrapperWidth}px;overflow:hidden;margin:0 auto;background:#eee;position:relative`)
        this.wrapperDiv.className='wrapper'
        this.outerBox.append(this.wrapperDiv)
        // 图片视频盒子 inner
        this.imgWrapperDiv = document.createElement('div')
        this.imgWrapperDiv.setAttribute("style",`width:${this.wrapperWidth * this.length}px;height:100%;overflow:hidden;margin:0 auto;margin-left:0;transition:all ease 0.3s`)
        $(this.wrapperDiv).append(this.imgWrapperDiv)
        this.outerBox.append(this.indicatorDiv)
        // 左右按钮
        if(this.options.button){
            // 左
            this.leftDiv = document.createElement('div')
            this.leftDiv.setAttribute("style",`width:${~~(this.outerWidth*0.1)}px;position:absolute;left:0;top:0;bottom: 0px;display: flex;justify-content: center;align-items: center;opacity:0.6;cursor:not-allowed`)
            this.leftDiv.classList.add(this.options.leftDivClass)
            const leftArrow = document.createElement('img')
            leftArrow.style.width = '48%'
            leftArrow.src = this.leftArrowImg
            this.leftDiv.append(leftArrow)
            // 右
            this.rightDiv = document.createElement('div')
            this.rightDiv.setAttribute("style",`width:${~~(this.outerWidth*0.1)}px;position:absolute;right:0;top:0;bottom: 0px;display: flex;justify-content: center;align-items: center;`)
            this.rightDiv.classList.add(this.options.rightDivClass)
            if(this.length <= 1){
                this.rightDiv.style.opacity = 0.6
                this.rightDiv.style.cursor = 'not-allowed'
            }
            const rightArrow = document.createElement('img')
            rightArrow.style.width = '48%'
            rightArrow.src = this.rightArrowImg
            this.rightDiv.append(rightArrow)
            this.outerBox.append(this.leftDiv)
            this.outerBox.append(this.rightDiv)
            // this.wrapperDiv.append(this.leftDiv)
            // this.wrapperDiv.append(this.rightDiv)
        }
        if(this.options.indicator){
            // 指示器
            this.indicatorDiv = document.createElement('div')
            this.indicatorDiv.setAttribute("style",this.localStyles.indicatorStyles)
            this.indicatorDiv.classList.add(this.options.rightDivClass)
            this.outerBox.append(this.indicatorDiv)
        }
    }
    render(){
        const srcArr = this.options.srcArr
        if(!(srcArr instanceof Array) && srcArr.length < 1) return
        this.length = srcArr.length
        this.renderFrame()

        let wrapperHtml = ''
        let indicatorHtml = ''
        if(srcArr instanceof Array){
            srcArr.forEach((item,index) => {
                let space = `0 ${this.options.space}px`
                wrapperHtml += `<div class="${this.options.imgWrapperClass}" style="width:${this.wrapperWidth - (this.options.space * 2)}px;overflow: hidden;float: left;margin:${space};box-sizing: border-box;"><${this.options.tagName} src="${item}"/></${this.options.tagName}></div>`
                if(this.options.indicatorType == 'dot'){
                    if(this.options.dotClass){
                        indicatorHtml += index == 0 ? `<span class="${this.options.dotActiveClass}">${index + 1}</span>` : `<span class="${this.options.dotClass}">${index + 1}</span>`
                    }else{
                        indicatorHtml += index == 0 ? `<span style="${this.localStyles.dotActiveStyle}">${index + 1}</span>` : `<span style="${this.localStyles.dotStyle}">${index + 1}</span>`
                    }
                }
                if(this.options.indicatorType == 'number'){
                    indicatorHtml = `- <span>1</span>/<span>${this.length}</span> -`
                }
            })
        }
        $(this.imgWrapperDiv).html(wrapperHtml)
        $(this.indicatorDiv).html(indicatorHtml)
        this.bindEvent()
    }
    // 事件绑定
    bindEvent(){
        if(this.options.button){
            this.leftDiv.addEventListener('click',() => {this.left()})
            this.rightDiv.addEventListener('click',() => {this.right()})
        }
        if(this.options.indicator){
            this.indicatorDiv.addEventListener('click',(e) => {
                var index = $(e.target).html()
                this.swiperTo(index - 1)
            })
        }
    }
    left(){
        this.swiperTo(this.current -1)
    }
    right() {
        this.swiperTo(this.current + 1)
    }
    // 滑动到哪一页
    swiperTo(index) {
        if(index < 0 || index + 1 > this.length) return
        this.current = index
        var marginLeft = (-index * this.wrapperWidth) + 'px'
        $(this.imgWrapperDiv).css('margin-left',marginLeft)
        if(!this.leftDiv) return
        if(index == 0){
            this.leftDiv.style.opacity = 0.6
            this.leftDiv.style.cursor = 'not-allowed'
        }else {
            this.leftDiv.style.opacity = 1
            this.leftDiv.style.cursor = 'default'
        }
        if(index + 1 == this.length){
            this.rightDiv.style.opacity = 0.6
            this.rightDiv.style.cursor = 'not-allowed'
        }else {
            this.rightDiv.style.opacity = 1
            this.rightDiv.style.cursor = 'default'
        }
        this.dotActive(index)
    }
    // 指示器高亮
    dotActive (index) {
        if(this.options.indicatorType == 'dot'){
            const $dotList = $(this.indicatorDiv).find('span')
            $dotList.each((i) => {
                const {dotClass,dotActiveClass} = this.options
                if(dotClass && dotActiveClass){
                    $(this).removeClass(dotActiveClass)
                    i == index && $(this).addClass(dotActiveClass)
                }else{
                    $(this)[0].setAttribute('style',this.localStyles.dotStyle)
                    i == index && $(this)[0].setAttribute('style',this.localStyles.dotActiveStyle)
                }
            })
        }else if(this.options.indicatorType == 'number'){
            $(this.indicatorDiv).find('span:first-child').html(index + 1)
        }
    }
}