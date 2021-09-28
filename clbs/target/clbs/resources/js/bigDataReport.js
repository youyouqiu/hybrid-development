//# sourceURL=bigDataReport.js
(function ($, window) {
    var dateForMonth = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'];
    var hostMyChart;
    var mileageStatisticsData = [];
    var thisMouthData = [];
    var lastMouthData = [];
    var vehicleIds = [];
    var mileageDate;
    var mileageVSData;
    var barWidth;
    var hotspoteChartData = [];
    var cycleDate;
    var sum = 0;
    var point = 0;
    var geoCoordMap = {
        '北京市': [116.410, 39.907],
        '上海市': [121.471, 31.224],
        '天津市': [117.192, 39.123],
        '重庆市': [106.502, 29.525],
        '河北省': [115.557, 38.073],
        '山东省': [118.018, 36.393],
        '辽宁省': [122.589, 41.647],
        '黑龙江省': [128.302, 46.986],
        '吉林省': [126.283, 43.552],
        '甘肃省': [104.178, 34.998],
        '青海省': [96.070, 35.840],
        '河南省': [113.604, 33.930],
        '江苏省': [119.819, 33.017],
        '湖南省': [111.733, 27.789],
        '江西省': [115.644, 27.556],
        '浙江省': [120.170, 28.988],
        '云南省': [101.713, 24.396],
        '台湾省': [120.961, 23.553],
        '海南省': [109.755, 19.009],
        '山西省': [112.040, 37.150],
        '四川省': [102.900, 30.476],
        '陕西省': [109.360, 35.271],
        '贵州省': [107.514, 26.695],
        '哈尔滨市': [126.641, 45.753],
        '齐齐哈尔市': [123.964, 47.340],
        '石家庄市': [114.503, 38.050],
        '唐山市': [118.176, 39.633],
        '邯郸市': [114.485, 36.608],
        '秦皇岛市': [119.587, 39.942],
        '保定市': [115.477, 38.867],
        '张家口市': [114.879, 40.811],
        '承德市': [117.939, 40.971],
        '廊坊市': [116.702, 39.524],
        '沧州市': [116.852, 38.307],
        '衡水市': [115.663, 37.734],
        '邢台市': [114.501, 37.070],
        '鸡西市': [130.973, 45.295],
        '鹤岗市': [130.278, 47.332],
        '双鸭山市': [131.164, 46.637],
        '大庆市': [125.112, 46.590],
        '伊春市': [128.906, 47.725],
        '佳木斯市': [130.367, 46.805],
        '七台河市': [131.003, 45.770],
        '牡丹江市': [129.623, 44.577],
        '黑河市': [127.502, 50.246],
        '绥化市': [126.992, 46.632],
        '大兴安岭地区': [124.702, 52.335],
        '长春市': [125.327, 43.880],
        '吉林市': [126.552, 43.839],
        '四平市': [124.375, 43.167],
        '辽源市': [125.150, 42.898],
        '通化市': [125.937, 41.715],
        '白山市': [126.417, 41.935],
        '松原市': [124.825, 45.116],
        '白城市': [122.833, 45.618],
        '延边朝鲜族自治州': [129.515, 42.894],
        '沈阳市': [123.429, 41.790],
        '大连市': [121.622, 38.908],
        '鞍山市': [122.986, 41.108],
        '抚顺市': [123.923, 41.872],
        '本溪市': [123.771, 41.293],
        '丹东市': [124.383, 40.118],
        '锦州市': [121.133, 41.115],
        '营口市': [122.234, 40.664],
        '阜新市': [121.648, 42.017],
        '辽阳市': [123.186, 41.271],
        '盘锦市': [122.070, 41.115],
        '铁岭市': [123.842, 42.290],
        '朝阳市': [120.447, 41.575],
        '葫芦岛市': [120.850, 40.755],
        '呼和浩特市': [111.661, 40.814],
        '包头市': [109.839, 40.653],
        '乌海市': [106.824, 39.666],
        '赤峰市': [118.950, 42.273],
        '通辽市': [122.255, 43.611],
        '鄂尔多斯市': [109.981, 39.818],
        '呼伦贝尔市': [119.751, 49.215],
        '巴彦淖尔市': [107.412, 40.755],
        '乌兰察布市': [113.105, 41.037],
        '兴安盟': [122.065, 46.078],
        '锡林郭勒盟': [116.084, 43.940],
        '阿拉善盟': [105.722, 38.835],
        '郑州市': [113.620, 34.747],
        '开封市': [114.340, 34.796],
        '洛阳市': [112.442, 34.653],
        '平顶山市': [113.298, 33.734],
        '安阳市': [114.351, 36.102],
        '鹤壁市': [114.293, 35.748],
        '新乡市': [113.877, 35.302],
        '焦作市': [113.236, 35.235],
        '濮阳市': [115.040, 35.765],
        '许昌市': [115.040, 35.765],
        '漯河市': [114.028, 33.572],
        '三门峡市': [111.1920, 34.783],
        '南阳市': [112.540, 32.997],
        '商丘市': [115.649, 34.439],
        '信阳市': [114.070, 32.121],
        '周口市': [114.648, 33.618],
        '驻马店市': [114.018, 32.972],
        '济源市': [112.594, 35.087],
        '济南市': [117.007, 36.675],
        '青岛市': [120.379, 36.067],
        '淄博市': [118.045, 36.815],
        '枣庄市': [117.560, 34.853],
        '东营市': [118.664, 37.435],
        '烟台市': [121.386, 37.536],
        '潍坊市': [119.104, 36.708],
        '济宁市': [116.585, 35.415],
        '泰安市': [117.128, 36.193],
        '威海市': [122.110, 37.508],
        '日照市': [119.455, 35.426],
        '莱芜市': [117.677, 36.212],
        '临沂市': [118.321, 35.061],
        '德州市': [116.302, 37.450],
        '聊城市': [115.978, 36.456],
        '滨州市': [118.012, 37.380],
        '菏泽市': [115.461, 35.245],
        '太原市': [112.544, 37.857],
        '大同市': [113.290, 40.090],
        '阳泉市': [113.581, 37.857],
        '长治市': [113.111, 36.191],
        '晋城市': [112.846, 35.498],
        '朔州市': [112.432, 39.333],
        '晋中市': [112.731, 37.692],
        '运城市': [110.997, 35.021],
        '忻州市': [112.724, 38.413],
        '临汾市': [111.507, 36.083],
        '吕梁市': [111.132, 37.520],
        // '西安市':[111.95,21.85],
        '西安市': [108.947237, 34.269841],
        '铜川市': [108.972, 34.916],
        '宝鸡市': [107.138, 34.369],
        '咸阳市': [108.700, 34.331],
        '渭南市': [109.502, 34.497],
        '延安市': [109.486, 36.594],
        '汉中市': [107.026, 33.077],
        '玉林市': [110.152, 22.630],
        '安康市': [109.027, 32.687],
        '商洛市': [109.939, 33.864],
        '南京市': [118.766, 32.040],
        '无锡市': [120.303, 31.574],
        '徐州市': [117.185, 34.261],
        '常州市': [119.946, 31.775],
        '苏州市': [120.613, 31.295],
        '南通市': [120.859, 32.013],
        '连云港市': [119.174, 34.599],
        '淮安市': [119.015, 33.600],
        '盐城市': [120.137, 33.375],
        '扬州市': [119.418, 32.391],
        '湛江市': [110.358, 21.274],
        '泰州市': [119.914, 32.484],
        '宿迁市': [118.270, 33.959],
        '南昌市': [115.884, 28.675],
        '景德镇市': [117.213, 29.291],
        '萍乡市': [113.850, 27.618],
        '九江市': [115.988, 29.712],
        '新余市': [114.930, 27.810],
        '鹰潭市': [114.930, 27.810],
        '赣州市': [114.938, 25.847],
        '吉安市': [114.980, 27.107],
        '宜春市': [114.390, 27.802],
        '抚州市': [116.356, 27.980],
        '上饶市': [117.971, 28.445],
        '杭州市': [120.148, 30.285],
        '宁波市': [121.623, 29.855],
        '温州市': [120.670, 27.998],
        '嘉兴市': [120.751, 30.760],
        '湖州市': [120.097, 30.864],
        '绍兴市': [120.574, 29.994],
        '金华市': [119.648, 29.092],
        '衢州市': [118.871, 28.942],
        '舟山市': [122.100, 30.015],
        '台州市': [121.423, 28.658],
        '丽水市': [119.916, 28.447],
        '长沙市': [112.977, 28.191],
        '株洲市': [113.151, 27.831],
        '湘潭市': [112.941, 27.827],
        '衡阳市': [112.605, 26.894],
        '邵阳市': [111.463, 27.237],
        '岳阳市': [113.132, 29.366],
        '常德市': [111.689, 29.040],
        '张家界市': [110.472, 29.125],
        '益阳市': [112.348, 28.565],
        '郴州市': [113.027, 25.790],
        '永州市': [111.600, 26.435],
        '怀化市': [109.973, 27.547],
        '娄底市': [112.006, 27.727],
        '湘西土家族苗族自治州': [109.739, 28.310],
        '广州市': [113.275, 23.122],
        '韶关市': [113.590, 24.798],
        '深圳市': [114.079, 22.550],
        '珠海市': [113.551, 22.223],
        '汕头市': [116.706, 23.369],
        '佛山市': [113.120, 23.024],
        '江门市': [113.094, 22.593],
        '茂名市': [110.914, 21.660],
        '肇庆市': [112.470, 23.050],
        '惠州市': [114.406, 23.076],
        '梅州市': [116.110, 24.299],
        '汕尾市': [115.358, 22.773],
        '河源市': [114.692, 23.740],
        '阳江市': [111.973, 21.857],
        '清远市': [113.050, 23.684],
        '东莞市': [113.746, 23.050],
        '中山市': [113.380, 22.517],
        '潮州市': [116.632, 23.658],
        '揭阳市': [116.349, 23.539],
        '云浮市': [112.042, 22.934],
        '南宁市': [108.317, 22.819],
        '柳州市': [109.415, 24.311],
        '桂林市': [110.294, 25.271],
        '梧州市': [111.291, 23.470],
        '北海市': [109.119, 21.470],
        '防城港市': [108.336, 21.613],
        '钦州市': [108.623, 21.965],
        '贵港市': [109.602, 23.092],
        '百色市': [106.611, 23.893],
        '贺州市': [111.552, 24.417],
        '河池市': [108.060, 24.691],
        '来宾市': [109.215, 23.731],
        '崇左市': [107.347, 22.399],
        '海口市': [110.326, 20.028],
        '三亚市': [109.503, 18.243],
        '儋州市': [109.576, 19.523],
        '乌鲁木齐市': [87.610, 43.788],
        '克拉玛依市': [87.610, 43.788],
        '昌吉回族自治州': [87.277, 44.015],
        '博尔塔拉蒙古自治州': [82.078, 44.896],
        '巴音郭楞蒙古自治州': [86.148, 41.759],
        '阿克苏地区': [80.265, 41.174],
        '克孜勒苏柯尔克孜自治州': [76.162, 39.710],
        '喀什地区': [75.989, 39.471],
        '和田地区': [79.923, 37.106],
        '伊犁哈萨克自治州': [81.313, 43.917],
        '塔城地区': [82.979, 46.748],
        '阿勒泰地区': [88.141, 47.843],
        '新疆维吾尔自治区': [86.048, 44.300],
        '银川市': [106.273, 38.464],
        '石嘴山市': [106.371, 39.010],
        '吴忠市': [106.201, 37.979],
        '固原市': [106.280, 36.001],
        '中卫市': [105.191, 37.508],
        '西宁市': [101.776, 36.620],
        '海东市': [102.103, 36.501],
        '海北藏族自治州': [100.882, 36.953],
        '黄南藏族自治州': [102.002, 35.516],
        '海南藏族自治州': [100.607, 36.280],
        '果洛藏族自治州': [100.227, 34.468],
        '玉树藏族自治州': [96.983, 32.997],
        '海西蒙古族藏族自治州': [97.371, 37.373],
        '兰州市': [103.824, 36.058],
        '嘉峪关市': [98.275, 39.782],
        '广东省': [113.031, 23.653],
        '金昌市': [102.181, 38.516],
        '白银市': [104.168, 36.542],
        '天水市': [105.725, 34.577],
        '武威市': [102.643, 37.927],
        '张掖市': [100.449, 38.931],
        '平凉市': [106.679, 35.538],
        '酒泉市': [98.508, 39.742],
        '庆阳市': [107.630, 35.731],
        '定西市': [104.624, 35.577],
        '陇南市': [104.928, 33.389],
        '临夏回族自治州': [103.194, 35.597],
        '甘南藏族自治州': [102.885, 34.980],
        '拉萨市': [91.120, 29.660],
        '昌都市': [97.179, 31.130],
        '山南市': [91.758, 29.230],
        '日喀则市': [88.877, 29.264],
        '那曲地区': [92.058, 31.476],
        '阿里地区': [80.099, 32.499],
        '林芝市': [94.423, 29.550],
        '成都市': [104.059, 30.662],
        '自贡市': [104.771, 29.349],
        '攀枝花市': [101.713, 26.574],
        '泸州市': [105.434, 28.886],
        '德阳市': [104.396, 31.127],
        '绵阳市': [104.727, 31.469],
        '广元市': [105.829, 32.431],
        '遂宁市': [105.573, 30.505],
        '内江市': [105.058, 29.584],
        '乐山市': [103.766, 29.581],
        '南充市': [106.083, 30.791],
        '眉山市': [103.840, 30.047],
        '宜宾市': [104.635, 28.752],
        '广安市': [106.635, 30.460],
        '达州市': [107.502, 31.201],
        '恩施土家族苗族自治州': [109.504, 30.275],
        '武汉市': [114.292, 30.587],
        '黄石市': [115.072, 30.217],
        '十堰市': [110.788, 32.647],
        '荆州市': [112.233, 30.326],
        '宜昌市': [111.279, 30.689],
        '襄阳市': [112.139, 32.041],
        '鄂州市': [114.889, 30.392],
        '荆门市': [112.202, 31.033],
        '孝感市': [113.920, 30.922],
        '黄冈市': [114.874, 30.448],
        '咸宁市': [114.328, 29.828],
        '随州市': [113.385, 31.687],
        '湖北省': [113.461, 30.369],
        '福州市': [119.306, 26.075],
        '厦门市': [118.105, 24.493],
        '莆田市': [119.008, 25.430],
        '三明市': [117.632, 26.263],
        '泉州市': [118.587, 24.904],
        '漳州市': [117.662, 24.511],
        '南平市': [118.177, 26.639],
        '龙岩市': [117.015, 25.074],
        '宁德市': [119.520, 26.657],
        '福建省': [117.830, 26.335],
        '合肥市': [117.282, 31.860],
        '芜湖市': [118.375, 31.323],
        '蚌埠市': [117.362, 32.940],
        '淮南市': [117.012, 32.642],
        '马鞍山市': [118.501, 31.671],
        '淮北市': [116.784, 33.972],
        '铜陵市': [117.815, 30.930],
        '安庆市': [117.042, 30.511],
        '黄山市': [118.312, 29.708],
        '滁州市': [118.319, 32.303],
        '宣城市': [118.751, 30.946],
        '阜阳市': [115.805, 32.902],
        '六安市': [116.509, 31.752],
        '宿州市': [116.976, 33.634],
        '亳州市': [116.976, 33.634],
        '池州市': [117.487, 30.656],
        '安徽省': [117.186, 31.267],
        '雅安市': [102.993, 29.983],
        '巴中市': [106.751, 31.855],
        '资阳市': [104.641, 30.116],
        '阿坝藏族羌族自治州': [101.707, 32.904],
        '甘孜藏族自治州': [101.950, 30.051],
        '凉山彝族自治州': [102.260, 27.892],
        '贵阳市': [106.714, 26.570],
        '六盘水市': [104.839, 26.587],
        '遵义市': [106.937, 27.707],
        '安顺市': [105.931, 26.244],
        '铜仁市': [109.185, 27.718],
        '黔西南布依族苗族自治州': [104.893, 25.091],
        '毕节市': [105.282, 27.299],
        '黔东南苗族侗族自治州': [107.956, 26.592],
        '黔南布依族苗族自治州': [107.517, 26.263],
        '昆明市': [102.710, 25.041],
        '曲靖市': [103.786, 25.507],
        '玉溪市': [102.541, 24.346],
        '保山市': [99.167, 25.108],
        '昭通市': [103.717, 27.335],
        '丽江市': [100.233, 26.867],
        '普洱市': [100.974, 22.772],
        '临沧市': [100.086, 23.892],
        '楚雄彝族自治州': [101.538, 25.036],
        '红河哈尼族彝族自治州': [103.373, 23.359],
        '文山壮族苗族自治州': [104.225, 23.363],
        '西双版纳傣族自治州': [100.791, 22.01],
        '大理白族自治州': [100.219, 25.576],
        '德宏傣族景颇族自治州': [98.585, 24.442],
        '怒江傈僳族自治州': [98.852, 25.854],
        '迪庆藏族自治州': [99.700, 27.832]

    };
    var bigDataReport = {
        //测试数据
        ceshi: function () {
            bigDataReport.hotspoteChart(hotspoteChartData, geoCoordMap);
        },
        //初始化
        init: function () {
            //初始化文件树
            var treeSetting = {
                async: {
                    url: "/clbs/m/basicinfo/enterprise/professionals/tree",
                    tyoe: "post",
                    enable: true,
                    autoParam: ["id"],
                    dataType: "json",
                    otherParam: { // 是否可选  Organization
                        "isOrg": "1"
                    },
                },
                check: {
                    enable: true,
                    chkStyle: "radio",
                    chkboxType: {
                        "Y": "s",
                        "N": "s"
                    },
                    radioType: "all"
                },
                view: {
                    selectedMulti: false,
                    nameIsHTML: true,
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                callback: {
                    onClick: bigDataReport.zTreeOnClick,
                    onCheck: bigDataReport.onCheck,
                    onAsyncSuccess: bigDataReport.zTreeOnAsyncSuccess
                }
            };
            $.fn.zTree.init($("#treeDemo"), treeSetting, null);
        },
        //组织树点击事件
        zTreeOnClick: function (event, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var isFlag = treeNode.checked;
            zTree.checkNode(treeNode, true, null, true);
            if (isFlag) {
                bigDataReport.onCheck(event, treeId, treeNode);
            }
            ;
        },
        onCheck: function (e, treeId, treeNode) {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo"), nodes = zTree
                .getCheckedNodes(true), v = "";
            zTree.checkNode(treeNode, true, null, true);
            for (var i = 0, l = nodes.length; i < l; i++) {
                v += nodes[i].name + ",";
            }
            ;

            if ($(".panel-body").is(":hidden")) {
                $(".panel-body").show();
            }
            // 初始化企业数据
            var checkGroupNode = zTree.getCheckedNodes();
            if (checkGroupNode != null && checkGroupNode.lengtt != 0) {
                $("#selectGroup").html(checkGroupNode[0].name);
            }
            //调用后台取数据
            var url = "getBigDataReportData";
            var parameter = {"groupId": checkGroupNode[0].uuid};
            json_ajax("POST", url, "json", true, parameter, bigDataReport.reportDataCallback, 60000);

            $("#monment").hide();
        },
        zTreeOnAsyncSuccess: function (event, treeId, msg) {
            // 默认选择第一个节点
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getNodes();
            zTree.expandNode(nodes[0], true);// 展看第一个节点
            var nodeArr = zTree.transformToArray(nodes);
            zTree.checkNode(nodes[0], true, true);
            // 初始化企业数据
            var checkGroupNode = zTree.getCheckedNodes();
            if (checkGroupNode != null && checkGroupNode.length != 0) {
                $("#selectGroup").html(checkGroupNode[0].name);
            } else {
                $("#selectGroup").html(nodes[0].name);
            }
            //调用后台取数据
            var url = "getBigDataReportData";
            var parameter = {"groupId": checkGroupNode[0].uuid};
            json_ajax("POST", url, "json", true, parameter, bigDataReport.reportDataCallback, 60000);
        },
        //周期对比
        cycleVS: function (date, thisMouthData, lastMouthData) {
            var myChart = echarts.init(document.getElementById('cycleVS'));
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var date = new Date();
                        var nowYear = date.getFullYear();
                        var nowMouth = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);
                        var relVal = '';
                        relVal = nowYear + "-" + nowMouth + "-" + (a[0].name < 10 ? "0" + a[0].name : a[0].name);
                        if (a[0].data == null) {
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + "无相关数据" + "";
                        } else {
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + a[0].value + " km";
                        }
                        if (a[1].data == null) {
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[1].color + "'></span>" + a[1].seriesName + "：" + "无相关数据" + "";
                        } else {
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[1].color + "'></span>" + a[1].seriesName + "：" + a[1].value + " km";
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: ['本月', '上月'],
                    left: 'auto',
                },
                toolbox: {
                    show: false
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    name: "日期",
                    nameLocation: "end",
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '里程(km)',
                        scale: true,
                        position: '',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                ],
                series: [
                    {
                        name: '本月',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbol: 'none',
                        symbolSize: 7,
                        sampling: 'average',
                        showAllSymbol: true,
                        smooth: false,					//是否平滑
                        symbol: 'emptyCircle',			//标记类型
                        itemStyle: {
                            normal: {
                                color: '#6dcff6'
                            }
                        },
                        data: thisMouthData
                    },
                    {
                        name: '上月',
                        yAxisIndex: 0,
                        type: 'line',
                        smooth: true,
                        symbolSize: 7,
                        showAllSymbol: true,
                        symbol: 'none',
                        sampling: 'average',
                        smooth: false,					//是否平滑
                        symbol: 'emptyCircle',			//标记类型
                        itemStyle: {
                            normal: {
                                color: 'rgb(248, 123, 0)'
                            }
                        },
                        data: lastMouthData
                    },
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        //里程对比
        mileageVS: function (date, mileageData) {
            // wjk
            //date = bigDataReport.platenumbersplitFun(date);

            var bgArray = [];
            var maxValue = Math.max.apply(null, mileageData);
            for (var i = 0, len = mileageData.length; i < len; i++) {
                bgArray.push(maxValue);
            }
            ;
            var start;
            var end;
            var length = mileageData.length;
            if (length < 4) {
                barWidth = "30%";
            } else if (length < 6) {
                barWidth = "20%";
            } else {
                barWidth = null;
            }
            ;
            if (length <= 20) {
                start = 0;
                end = 100;
            } else {
                start = 0;
                end = 100 * (20 / length);
            }
            ;
            var myChart = echarts.init(document.getElementById('mileageVS'));
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var relVal = date[a[1].dataIndex];
                        if (a[1].name != "") {
                            var mileValue = bigDataReport.fiterNumber(a[1].value);
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[1].color + "'></span>" + a[1].seriesName + "：" + mileValue + " km";
                        } else {
                            relVal = "无相关数据";
                        }
                        return relVal;

                    },
                },
                legend: {
                    data: ['里程'],
                    left: 'auto',
                },
                toolbox: {
                    show: false
                },
                grid: {
                    left: '100',
                    bottom: '100'
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: true,
                    name: "车牌号",
                    axisLabel: {
                        show: true,
                        interval: 0,
                        rotate: 45
                    },
                    data: bigDataReport.platenumbersplitFun(date)
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '里程(km)',
                        scale: false,
                        position: '',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                ],
                dataZoom: [{
                    type: 'inside',
                    start: start,
                    end: end
                }, {

                    show: true,
                    height: 20,
                    type: 'slider',
                    top: 'top',
                    xAxisIndex: [0],
                    start: 0,
                    end: 10,
                    showDetail: false,
                }],
                series: [
                    {
                        type: 'bar',
                        itemStyle: {
                            normal: {color: 'rgba(0,0,0,0.05)'}
                        },
                        barGap: '-100%',
                        barCategoryGap: '40%',
                        data: bgArray,
                        animation: false
                    },
                    {
                        name: '里程',
                        yAxisIndex: 0,
                        type: 'bar',
                        smooth: true,
                        symbol: 'none',
                        barWidth: barWidth,
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#6dcff6'
                            }
                        },
                        data: mileageData
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
            myChart.on('click', bigDataReport.chartsEvent);
        },
        //里程对比图例点击事件，点击柱形查询单车的里程统计数据
        chartsEvent: function (params) {
            if ($(".panel-body").is(":hidden")) {
                $(".panel-body").show();
            }
            ;
            var brand = params.name;
            $("#curCar").text("：" + brand);
            // 树节点选中的id
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var checkGroupNode = zTree.getCheckedNodes();
            var url = "getMileData"; // 大数据月表接口
            // var url = "getMileCompareData"; // 平台原接口
            var vehicleId = params.id;
            if (vehicleId == undefined || vehicleId == "" || vehicleId == null) {
                var dataIndex = params.dataIndex;//点击柱状图的数值序列(下标)
                vehicleId = vehicleIds[dataIndex];
            }
            var data = {"vehicleId": vehicleId, "groupId": checkGroupNode[0].uuid};
            // var data = {"vehicleId":vehicleId,"brand":brand,"groupId":checkGroupNode[0].uuid};
            json_ajax("POST", url, "json", true, data, bigDataReport.ajaxListCallback);
        },
        // 里程对比图，点击柱形查询单车的里程统计数据回调
        ajaxListCallback: function (data) {
            if (data != null && data != "null" && data != undefined && data.success == true) {
                var dayDataByVehicle = data.obj.dayDataByVehicle; // 车辆一月每天里程
                var monthDataByVehicle = data.obj.monthDataByVehicle; // 车辆一月总数据
                var smallMileCount = data.obj.smallMileCount; // 里程小于当前车辆一个月里程的车辆数量
                $("#plateNumber").html(monthDataByVehicle != null ? monthDataByVehicle.plateNumber : "无"); // 车牌号
                $("#travelTimeByVehicle").html(monthDataByVehicle != null ? bigDataReport.formatSeconds(monthDataByVehicle.travelTime, true) : "<font style='font-size:18px'>0</font>秒"); // 行驶时长
                $("#downTimeByVehicle").html(monthDataByVehicle != null ? bigDataReport.formatSeconds(monthDataByVehicle.downTime, true) : "<font style='font-size:18px'>0</font>秒"); // 停驶时长
                var monthGpsMile, downMile;
                if (monthDataByVehicle == null) {
                    monthGpsMile = 0.0;
                    downMile = 0.0;
                }
                else {
                    monthGpsMile = monthDataByVehicle.travelMile ? bigDataReport.fiterNumber(monthDataByVehicle.travelMile.toFixed(1)) : 0.0;
                    downMile = monthDataByVehicle.downMile ? bigDataReport.fiterNumber(monthDataByVehicle.downMile.toFixed(1)) : 0.0;
                    // monthGpsMile= bigDataReport.fiterNumber(monthDataByVehicle.gpsMile.toFixed(1));
                }
                var downCount = 0;
                if (monthDataByVehicle && monthDataByVehicle.downTimes) {
                    downCount = monthDataByVehicle.downTimes;
                }
                $("#mileByVehicle").html(monthDataByVehicle != null ? "<font style='font-size:18px'> " + monthGpsMile + " </font><font style='font-size:12px'>km</font>" : "<font style='font-size:18px'>0.0</font>km"); // 行驶里程
                $("#downMile").html(monthDataByVehicle != null ? "<font style='font-size:18px'> " + downMile + " </font><font style='font-size:12px'>km</font>" : "<font style='font-size:18px'>0.0</font>km"); // 怠速里程
                $("#travelTimesByVehicle").html(monthDataByVehicle != null ? "<font style='font-size:18px'> " + monthDataByVehicle.travelTimes + " </font><font style='font-size:12px'>次</font>" : "<font style='font-size:18px'>0</font>次"); // 行驶次数
                $("#downCount").html(monthDataByVehicle != null ? "<font style='font-size:18px'> " + downCount + " </font><font style='font-size:12px'>次</font>" : "<font style='font-size:18px'>0</font>次"); // 停止次数
                $("#alarmTimesByVehicle").html(monthDataByVehicle != null ? "<font style='font-size:18px'> " + monthDataByVehicle.overSpeedTimes + " </font><font style='font-size:12px'>次</font>" : "<font style='font-size:18px'>0</font>次"); // 报警次数
                var totalVehicleCountStr = $("#selectTotalVehicleCount").text();
                var totalVehicleCount = totalVehicleCountStr.substring(0, totalVehicleCountStr.length - 1);
                var percentCount = smallMileCount + (totalVehicleCount - validVehicleCount); // 里程小的车辆数量 + 没有数据的车辆数量
                var percent = 0;
                if (monthDataByVehicle != null && monthDataByVehicle != undefined && monthDataByVehicle != 0) {
                    if (monthDataByVehicle.gpsMile != 0 && monthDataByVehicle.gpsMile != 0.0) {
                        percent = Math.round(percentCount / totalVehicleCount * 10000) / 100.00;
                    }
                }
                $("#milePercent").html(percent + "%"); // 里程百分比
                mileageStatisticsData = [];
                var today = new Date();
                today = today.getDate();
                for (var j = 0; j < dateForMonth.length; j++) {
                    var flagDate = false; // 标识日期
                    if (dayDataByVehicle != null && dayDataByVehicle != "null" && dayDataByVehicle != undefined && dayDataByVehicle.length > 0) {
                        for (var i = 0; i < dayDataByVehicle.length; i++) {
                            var timeStr = new Date(parseInt(dayDataByVehicle[i].dayTime) * 1000).getDate();
                            if (dateForMonth[j] == timeStr) {
                                flagDate = true;
                                mileageStatisticsData.push(dayDataByVehicle[i].gpsMile.toFixed(1));
                                break;
                            }
                        }
                    }
                    if (!flagDate && parseInt(dateForMonth[j]) <= parseInt(today)) {
                        mileageStatisticsData.push(0);
                    }
                    if (parseInt(dateForMonth[j]) > parseInt(today)) {
                        mileageStatisticsData.push(null);
                    }
                }
                bigDataReport.mileageStatistics(dateForMonth, mileageStatisticsData);
            } else {
                layer.msg(data.msg, {move: false});
            }
        },
        //里程月统计
        mileageStatistics: function (date, mileageStatisticsData) {
            var myChart = echarts.init(document.getElementById('mileageStatistics'));
            var option = {
                tooltip: {
                    trigger: 'axis',
                    textStyle: {
                        fontSize: 20
                    },
                    formatter: function (a) {
                        var date = new Date();
                        var nowYear = date.getFullYear();
                        var nowMouth = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);
                        var relVal = '';
                        relVal = nowYear + "-" + nowMouth + "-" + (a[0].name < 10 ? "0" + a[0].name : a[0].name);
                        if (a[0].data == null) {
                            relVal = "无相关数据";
                        } else {
                            var mileValue = bigDataReport.fiterNumber(a[0].value);
                            relVal += "<br/><span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a[0].color + "'></span>" + a[0].seriesName + "：" + mileValue + " km";
                        }
                        return relVal;
                    }
                },
                legend: {
                    data: ['里程'],
                    left: 'auto',
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: {show: false}
                    }
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    name: "日期",
                    nameLocation: "end",
                    axisLabel: {
                        show: true,
                        interval: 0
                    },
                    inverse: false,
                    data: date
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '里程(km)',
                        scale: false,
                        position: '',
                        axisLabel: {
                            formatter: '{value}'
                        },
                        splitLine: {
                            show: false
                        }
                    },
                ],
                series: [
                    {
                        name: '里程',
                        yAxisIndex: 0,
                        type: 'line',
                        symbolSize: 7,
                        showAllSymbol: true,
                        smooth: false,				//是否平滑
                        symbol: 'emptyCircle',			//标记类型
                        sampling: 'average',
                        itemStyle: {
                            normal: {
                                color: '#6dcff6'
                            }
                        },
                        data: mileageStatisticsData
                    }
                ]
            };
            myChart.setOption(option);
            window.onresize = myChart.resize;
        },
        //热点图
        hotspoteChart: function (data, geoCoordMap) {
            var convertData = function (data) {
                var res = [];
                for (var i = 0; i < data.length; i++) {
                    var geoCoord = geoCoordMap[data[i].name];
                    if (geoCoord) {
                        res.push({
                            name: data[i].name,
                            value: geoCoord.concat(data[i].value)
                        });
                    }
                }
                return res;
            };
            hostMyChart = echarts.init(document.getElementById('hotspoteChart'));
            var option = {
                backgroundColor: '#404a59',
                title: {
                    text: '所有轨迹信息点月分布图',
                    left: 'center',
                    textStyle: {
                        color: 'rgb(64, 74, 89)'
                    }
                },
                tooltip: {
                    trigger: 'item',
                    formatter: function (a) {
                        var relVal = "";
                        if (point == 1) {
                            relVal = "<span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a.color + "'></span>" + a.name + ":" + Math.round(a.value[2] / 100 * sum) + "";
                        } else if (point < 10 && point > 1) {
                            relVal = "<span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a.color + "'></span>" + a.name + ":" + Math.round(a.value[2] / 500 * sum) + "";
                        } else {
                            relVal = "<span style='display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:" + a.color + "'></span>" + a.name + ":" + Math.round(a.value[2] / 1000 * sum) + "";
                        }
                        return relVal;
                    }

                },
                legend: {
                    orient: 'vertical',
                    y: 'bottom',
                    x: 'right',
                    data: ['全部展示'],
                    textStyle: {
                        color: '#ff1320'
                    }
                },
                geo: {
                    map: 'china',
                    label: {
                        emphasis: {
                            show: false
                        }
                    },
                    zoom: 0.9,
                    roam: true,
                    itemStyle: {
                        normal: {
                            areaColor: '#323c48',
                            borderColor: '#111'
                        },
                        emphasis: {
                            areaColor: '#2a333d'
                        }
                    }
                },
                series: [
                    {
                        name: '全部展示',
                        type: 'scatter',
                        coordinateSystem: 'geo',
                        data: convertData(data),
                        symbolSize: function (val) {
                            return val[2] / 10;
                        },
                        label: {
                            normal: {
                                formatter: '{b}',
                                position: 'right',
                                show: true
                            },
                            emphasis: {
                                show: true
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#ddb926'
                            }
                        }
                    },
                    {
                        type: 'effectScatter',
                        coordinateSystem: 'geo',
                        data: convertData(data.sort(function (a, b) {
                            return b.value - a.value;
                        }).slice(0, 5)),
                        symbolSize: function (val) {
                            return val[2] / 10;
                        },
                        showEffectOn: 'render',
                        rippleEffect: {
                            brushType: 'stroke'
                        },
                        hoverAnimation: true,
                        label: {
                            normal: {
                                formatter: '{b}',
                                position: 'right',
                                show: true
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#f4e925',
                                shadowBlur: 10,
                                shadowColor: '#333'
                            }
                        },
                        zlevel: 1
                    }
                ]
            };
            hostMyChart.setOption(option);
            window.onresize = hostMyChart.resize;
        },
        //选择组织
        checkGroup: function () {
            $("#monment").show();
        },
        reportDataCallback: function (data) {
            if (data != null) {
                if (data.success) { // 成功！
                    cycleDate = [];
                    var obj = data.obj;
                    var vehicleCount = obj.vehicleCount; // 车辆数量
                    var totalMile = obj.totalMile; // 里程
                    var totalTravelTime = obj.totalTravelTime; // 行驶时长
                    var totalDownTime = obj.totalDownTime; // 停驶时长
                    var totalOverSpeedTimes = obj.totalOverSpeedTimes; // 超速报警次数
                    var lastTotalMile = obj.lastTotalMile; // 里程
                    var lastTotalTravelTime = obj.lastTotalTravelTime; // 行驶时长
                    var lastTotalDownTime = obj.lastTotalDownTime; // 停驶时长
                    var lastTotalOverSpeedTimes = obj.lastTotalOverSpeedTimes; // 超速报警次数
                    var mostDiligent = obj.mostDiligent; // 最勤奋的车
                    var mostLazy = obj.mostLazy; // 最懒惰的车
                    var mostFar = obj.mostFar; // 开得最远的车
                    var mintFar = obj.mintFar; // 几乎没动的车
                    var safe = obj.safe; // 最安全的车
                    var danger = obj.danger; // 最危险的车
                    var maxMile = obj.maxMile; // 最大里程
                    var minMile = obj.minMile; // 最小里程
                    cycleDate = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'];
                    thisMouthData = [];
                    lastMouthData = [];
                    var dailyMiles = obj.dailyMile;
                    var lastDailyMiles = obj.lastDailyMile;
                    var today = new Date();
                    today = today.getDate();
                    for (var i = 0; i < cycleDate.length; i++) {
                        var flagJ = false;
                        var flagK = false;
                        if (i < today) {
                            if (dailyMiles != null && dailyMiles != undefined && dailyMiles.length > 0) {
                                for (var j = 0; j < dailyMiles.length; j++) {
                                    // 访问接口从大数据月表接口修改为平台原来接口时,monthDay字段要修改为day.反之修改为monthDay
                                    if ((dailyMiles[j].monthDay) == i) {
                                        flagJ = true;
                                        var dailyMilesGpsMile = bigDataReport.fiterNumber(dailyMiles[j].gpsMile.toFixed(1))
                                        thisMouthData.push(dailyMilesGpsMile);
                                    }
                                }
                            }
                            if (!flagJ) {
                                thisMouthData.push(0);
                            }
                        } else {
                            thisMouthData.push(null);
                        }
                        if (lastDailyMiles != null && lastDailyMiles != undefined && lastDailyMiles.length > 0) {
                            for (var k = 0; k < lastDailyMiles.length; k++) {
                                // 访问接口从大数据月表接口修改为平台原来接口时,monthDay字段要修改为day.反之修改为monthDay
                                if ((lastDailyMiles[k].monthDay) == i) {
                                    flagK = true;
                                    var dailyMilesGpsMile = bigDataReport.fiterNumber(lastDailyMiles[k].gpsMile.toFixed(1))
                                    lastMouthData.push(dailyMilesGpsMile);
                                }
                            }
                        }
                        if (!flagK) {
                            lastMouthData.push(0);
                        }
                    }
                    bigDataReport.cycleVS(cycleDate, thisMouthData, lastMouthData);
                    $("#selectTotalVehicleCount").html("<font style='font-size:18px'> " + vehicleCount + "</font> 辆");
                    totalMile = bigDataReport.fiterNumber(totalMile.toFixed(1));
                    totalCount = bigDataReport.fiterNumber((totalMile / vehicleCount).toFixed(1));
                    $("#selectTotalMile").html(totalMile + "<font style='font-size:12px'> km</font>");
                    $("#mileageAvg").html(vehicleCount != 0 ? "<font style='font-size:18px'> " + totalCount + "</font>km" : "<font style='font-size:18px'>0</font>km");
                    $("#selectTotalTravelTime").html(bigDataReport.formatSeconds(totalTravelTime, true));
                    $("#avgTravelTime").html(bigDataReport.formatSeconds(vehicleCount != 0 ? (totalTravelTime / vehicleCount) : 0, true));
                    $("#selectTotalDownTime").html(bigDataReport.formatSeconds(totalDownTime, true));
                    $("#avgDownTime").html(bigDataReport.formatSeconds(vehicleCount != 0 ? (totalDownTime / vehicleCount) : 0, true));
                    $("#selectTotalOverSpeedTimes").html(totalOverSpeedTimes + "<font style='font-size:12px'> 次</font>");
                    reportSpeedAvgCount = bigDataReport.fiterNumber((totalOverSpeedTimes / vehicleCount).toFixed(1));
                    $("#reportSpeedAvg").html(vehicleCount != 0 ? "<font style='font-size:18px'> " + reportSpeedAvgCount + "</font>次" : "<font style='font-size:18px'> " + 0 + "</font>次");
                    $("#mostDiligent").html(mostDiligent != "" ? mostDiligent : "无");
                    $("#mostLazy").html(mostLazy != "" ? mostLazy : "无");
                    $("#mostFar").html(mostFar != "" ? mostFar : "无");
                    $("#mintFar").html(mintFar != "" ? mintFar : "无");
                    $("#safe").html(safe != "" ? safe : "无");
                    $("#danger").html(danger != "" ? danger : "无");
                    maxMile = bigDataReport.fiterNumber(maxMile);
                    minMile = bigDataReport.fiterNumber(minMile);
                    $("#maxMile").html(maxMile);
                    $("#minMile").html(minMile);
                    lastTotalMile = bigDataReport.fiterNumber(lastTotalMile.toFixed(1));
                    $("#lastTotalMile").html(lastTotalMile);
                    $("#lastTotalTravelTime").html(bigDataReport.formatSeconds(lastTotalTravelTime, false));
                    $("#lastTotalDownTime").html(bigDataReport.formatSeconds(lastTotalDownTime, false));
                    $("#lastTotalOverSpeedTimes").html(lastTotalOverSpeedTimes);
                    totalMile = bigDataReport.fiterNumber(totalMile.toFixed(1));
                    $("#totalMile").html(totalMile);
                    $("#totalTravelTime").html(bigDataReport.formatSeconds(totalTravelTime, false));
                    $("#totalDownTime").html(bigDataReport.formatSeconds(totalDownTime, false));
                    $("#totalOverSpeedTimes").html(totalOverSpeedTimes);
                    var differTotalMile = totalMile - lastTotalMile; // 里程
                    var differTotalTravelTime = totalTravelTime - lastTotalTravelTime; // 行驶时长
                    var differTotalDownTime = totalDownTime - lastTotalDownTime; // 停驶时长
                    var differTotalOverSpeedTimes = totalOverSpeedTimes - lastTotalOverSpeedTimes; // 超速报警次数
                    if (differTotalTravelTime < 0) {
                        differTotalTravelTime = "，比上月少" + bigDataReport.formatSeconds(Math.abs(differTotalTravelTime), true);
                    } else if (differTotalTravelTime == 0) {
                        differTotalTravelTime = "";
                    } else {
                        differTotalTravelTime = "，比上月多" + bigDataReport.formatSeconds(differTotalTravelTime, true);
                    }

                    if (differTotalDownTime < 0) {
                        differTotalDownTime = "，比上月少" + bigDataReport.formatSeconds(Math.abs(differTotalDownTime), true);
                    } else if (differTotalDownTime == 0) {
                        differTotalDownTime = "";
                    } else {
                        differTotalDownTime = "，比上月多" + bigDataReport.formatSeconds(differTotalDownTime, true);

                    }
                    if (differTotalMile < 0) {
                        differTotalMile = "，比上月少<font style='font-size:18px;color:#6dcff6'>" + Math.abs(differTotalMile.toFixed(1)) + "</font>km";
                    } else if (differTotalMile == 0) {
                        differTotalMile = "";
                    } else {
                        differTotalMile = "，比上月多<font style='font-size:18px;color:#6dcff6;'> " + differTotalMile.toFixed(1) + " </font>km";

                    }
                    if (differTotalOverSpeedTimes < 0) {
                        differTotalOverSpeedTimes = "，比上月少<font style='font-size:18px;color:#960ba3'> " + Math.abs(differTotalOverSpeedTimes) + " </font>次";
                    } else if (differTotalOverSpeedTimes == 0) {
                        differTotalOverSpeedTimes = "";
                    } else {
                        differTotalOverSpeedTimes = "，比上月多<font style='font-size:18px'>" + differTotalOverSpeedTimes + "</font>次";
                    }

                    $("#differTotalMile").html(differTotalMile);
                    $("#differTotalTravelTime").html(differTotalTravelTime);
                    $("#differTotalDownTime").html(differTotalDownTime);
                    $("#differTotalOverSpeedTimes").html(differTotalOverSpeedTimes);
                    //里程对比
                    mileageDate = obj.mileCompareBrands;
                    mileageVSData = obj.mileCompareMiles;
                    vehicleIds = obj.vehicleIds;
                    bigDataReport.mileageVS(mileageDate, mileageVSData);
                    validVehicleCount = obj.validVehicleCount; // 有数据的车辆数量
                    // 里程月统计图表：默认显示里程对比图表中的第一辆车的数据
                    if (mileageDate != null && mileageDate != "" && typeof(mileageDate) != undefined && typeof(mileageDate) != "undefined") {
                        var params = new Object();
                        params.name = mileageDate[0];
                        params.id = vehicleIds[0];
                        bigDataReport.chartsEvent(params);
                    } else {
                        // 里程对比按车清空
                        $("#travelTimeByVehicle").html("<font style='font-size:18px'> 0</font>秒"); // 行驶时长
                        $("#downTimeByVehicle").html("<font style='font-size:18px'> 0</font>秒"); // 停驶时长
                        $("#mileByVehicle").html("<font style='font-size:18px'>0</font>km"); // 行驶里程
                        $("#downMile").html("<font style='font-size:18px'>0</font>km"); // 怠速里程
                        $("#travelTimesByVehicle").html("<font style='font-size:18px'> 0</font>次"); // 行驶次数
                        $("#downCount").html("<font style='font-size:18px'> 0</font>次"); // 停止次数
                        $("#alarmTimesByVehicle").html("<font style='font-size:18px'> 0</font>次"); // 报警次数
                        $("#milePercent").html("0%"); // 里程百分比
                        //里程月统计
                        $("#curCar").text("");
                        var mileageStatisticsDate = [];
                        var mileageStatisticsData = [];
                        bigDataReport.mileageStatistics(mileageStatisticsDate, mileageStatisticsData);
                    }
                    var log = obj.result;
                    sum = obj.sum;
                    $("#east").text(obj.east);
                    $("#west").text(obj.west);
                    $("#north").text(obj.north);
                    $("#south").text(obj.south);
                    var list = [];
                    if (log.length == 0) {
                        $("#one").text("");
                        $("#two").text("");
                        $("#three").text("");
                        $("#four").text("");
                        $("#five").text("");
                    } else {
                        point = log.length;
                        for (var i = 0; i < log.length; i++) {
                            var name = log[i].name;
                            var count;
                            if (log.length == 1) {
                                count = (log[i].count / sum) * 100;
                                count = count < 1 ? 1 : count;
                            } else if (log.length < 10 && log.length > 1) {
                                count = (log[i].count / sum) * 500;
                                count = count < 1 ? 1 : count;
                            } else {
                                count = (log[i].count / sum) * 1000;
                                count = count < 1 ? 1 : count;
                            }
                            var map = {};
                            map.name = name;
                            map.value = count;
                            list.push(map)
                            if (i == 0) {
                                $("#one").text("1." + name);
                                if (log.length == 1) {
                                    $("#two").text("");
                                    $("#three").text("");
                                    $("#four").text("");
                                    $("#five").text("");
                                }
                            } else if (i == 1) {
                                $("#two").text("2." + name);
                                if (log.length == 2) {
                                    $("#three").text("");
                                    $("#four").text("");
                                    $("#five").text("");
                                }
                            } else if (i == 2) {
                                $("#three").text("3." + name);
                                if (log.length == 3) {
                                    $("#four").text("");
                                    $("#five").text("");
                                }
                            } else if (i == 3) {
                                $("#four").text("4." + name);
                                if (log.length == 4) {
                                    $("#five").text("");
                                }
                            } else if (i == 4) {
                                $("#five").text("5." + name);
                            }

                        }
                    }
                    hotspoteChartData = list;
                    bigDataReport.hotspoteChart(hotspoteChartData, geoCoordMap);
                } else {
                    layer.msg(data.msg, {move: false});
                }
            }
        },
        formatSeconds: function (value, isFormate) { // 秒转时分秒
            var theTime = parseInt(value);// 秒
            var theTime1 = 0;// 分
            var theTime2 = 0;// 小时
            if (theTime > 60) {
                theTime1 = parseInt(theTime / 60);
                theTime = parseInt(theTime % 60);
                if (theTime1 > 60) {
                    theTime2 = parseInt(theTime1 / 60);
                    theTime1 = parseInt(theTime1 % 60);
                }
            }
            if (isFormate) {
                var result = "<font class='dateColr' style='font-size:18px'> " + parseInt(theTime) + " </font><font style='font-size:12px'>秒</font>";
                if (theTime1 > 0) {
                    result = "<font class='dateColr' style='font-size:18px'> " + parseInt(theTime1) + " </font><font style='font-size:12px'>分</font>" + result;
                }
                if (theTime2 > 0) {
                    result = "<font class='dateColr' style='font-size:18px'> " + parseInt(theTime2) + " </font><font style='font-size:12px'>小时</font>" + result;
                }
            } else {
                var result = parseInt(theTime) + "秒";
                if (theTime1 > 0) {
                    result = parseInt(theTime1) + "分" + result;
                }
                if (theTime2 > 0) {
                    result = parseInt(theTime2) + "小时" + result;
                }
            }
            return result;
        },
        //隐藏组织树
        hideGroup: function (event) {
            if (!(event.target.id == "treeDemo" || event.target.id == "monment" || $(event.target).parents("#checkGroup").length > 0 || $(event.target).parents("#monment").length > 0)) {
                $("#monment").hide();
            }
            ;
        },
        windowResize: function () {
            bigDataReport.cycleVS(cycleDate, thisMouthData, lastMouthData);
            bigDataReport.mileageVS(mileageDate, mileageVSData);
            bigDataReport.mileageStatistics(dateForMonth, mileageStatisticsData);
            bigDataReport.hotspoteChart(hotspoteChartData, geoCoordMap);
        },
        fiterNumber: function (data) {
            if (data == null || data == undefined || data == "") {
                return data;
            }
            var data = data.toString();
            data = parseFloat(data);
            return data;

        },
        // wjk 车牌号太长显示不完截取
        platenumbersplitFun: function (arr) {
            var newArr = [];
            arr.forEach(function (item) {
                if (item.length > 8) {
                    item = item.substring(0, 7) + '...'
                }
                newArr.push(item)
            })
            return newArr
        }
    }
    $(function () {
        var validVehicleCount = 0; // 有数据的车辆数量
        bigDataReport.init();
        // bigDataReport.ceshi();
        $("#checkGroup").bind("click", bigDataReport.checkGroup);
        $(document).bind('click', bigDataReport.hideGroup);
        $(window).resize(bigDataReport.windowResize);
    })
}($, window))