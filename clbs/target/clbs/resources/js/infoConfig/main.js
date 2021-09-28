// main.js

// 模块路径配置
require.config({
  paths: {
    "processInputEntry": "processInputEntry",
    "infoPageList": "infoPageList",
  },
  shim: {
    'processInputEntry':{
      // deps: [],
      exports: 'pIE'
    },
    'infoPageList': {
      deps: ['processInputEntry'],
      exports: 'iPL'
    }
  }
});

// 加载模块
require(['processInputEntry', 'infoPageList'], function (pIE, iPL) {
  // alert("加载成功-1！");
});
