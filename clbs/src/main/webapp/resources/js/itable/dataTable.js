// dataTabale
function _a () {

};

$(document).ready( function () {
console.log(dataTableColumns, 'dataTableColumns');
  var row = [];
  // row.push(dataTableOperation.dataTableColumnsDatass);

  $('#table_id_example').DataTable({
    "searching": false, // 是否允许检索
    // "ordering": true, // 是否允许排序
    "info": true, // 是否显示情报 就是"当前显示1/100记录"这个信息
    "paging": true, // 是否允许翻页，设成false，翻页按钮不显示
    "lengthChange": false, // 件数选择功能
    // "lengthMenu": [10, 25, 50, 75, 100], // 件数选择下拉框内容
    "pageLength": 15, // 每页的初期件数 用户可以操作lengthMenu上的值覆盖
    "pagingType": "simple_numbers", // 翻页按钮样式
    "autoWidth": true, // 自动列宽
    "destroy": true, // 每次创建是否销毁以前的DataTable
    "processing": true, // 是否表示 "processing" 加载中的信息，这个信息可以修改
    "language": {
      "emptyTable": "No data", // 没有数据的显示（可选），如果没指定，会用zeroRecords的内容
    },
    "data": [
      // dataTableOperation.dataTableColumnsDatass,
      {
        monitorName: '111111',
        id: 'cd0fd693-d497-4d09-8353-34e6323da8b6',
        serviceSystemTime: '2020-11-11',
      },
    ],
    "columns": [
      {
        "data": 'monitorName',
        "title": 'Name',
      },
      {
        "data": 'id',
        "title": 'ID',
      },
      {
        "data": 'serviceSystemTime',
        "title": 'Time',
      }
    ]
  });
} );

