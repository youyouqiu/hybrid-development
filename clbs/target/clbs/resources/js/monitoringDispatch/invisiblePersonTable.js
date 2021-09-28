var InvisiblePersonTable = function (options) {
  this._dispatchModule = options.dispatchModule;
  this.init();
};

InvisiblePersonTable.prototype = {
  /**
   * 初始化数据
   */
  init: function () {
    var $this = this;
    $('#invisiblePersonShow').on('click', $this.invisiblePersonShowHandler.bind($this));
    $('#detailsShow, #invisiblePersonDetailsClose').on('click', $this.detailsShowHandler.bind($this));
  },
  /**
   * 不可见人员列表显示与隐藏
   */
  invisiblePersonShowHandler: function () {
    var $this = this;
    var $invisiblePersonTable = $('#invisiblePersonTable');
    if ($invisiblePersonTable.hasClass('active')) {
      $invisiblePersonTable.removeClass('active');
      $('#invisiblePersonShow').removeClass('active');
      $('#invisiblePersonDetails').removeClass('active');
    } else {
      $('#invisiblePersonShow').addClass('loading');
      $this._dispatchModule.get('dispatchServices').getInvisiblePersonList(
        $this.invisiblePersonListCallback.bind($this)
      );
    }
  },
  /**
   * 获取不可见人员列表回调事件
   */
  invisiblePersonListCallback: function (data) {
    if (data.success) {
      var list = data.obj;
      var html = '';
      list.map(function (item) {
        html += '<tr>'
          + '<td><p title="' + item.name + '">' + item.name + '</p></td>'
          + '<td><p title="' + item.groupName + '">' + item.groupName + '</p></td>'
          + '<td class="person-details" data-id="' + item.id + '"></td>'
          + '</tr>';
      });
      $('#invisiblePersonListTable tbody').html(html);
      $('#invisiblePersonNumber').text('(' + list.length + ')');
      $('.person-details').on('click', this.getInvisiblePersonDetails.bind(this));
      $('#invisiblePersonTable').addClass('active');
      $('#invisiblePersonShow').removeClass('loading');
      $('#invisiblePersonShow').addClass('active');
    }
  },
  /**
   * 获取不可见人员详情
   * @param e
   */
  getInvisiblePersonDetails: function (e) {
    var $this = this;
    var id = $(e.target).attr('data-id');
    $this._dispatchModule.get('dispatchServices').getInvisiblePersonDetails(
      {id: id},
      $this.invisiblePersonDetailsCallback.bind($this)
    );
  },
  /**
   * 获取不可见人员详情回调事件
   */
  invisiblePersonDetailsCallback: function (data) {
    if (data.success) {
      var obj = data.obj;
      var html = '<ul>'
        + '<li>人员名称：' + obj.name + '</li>'
        + '<li>所属组织：' + obj.groupName + '</li>'
        + '<li>当前状态：' + (obj.status === 0 ? '不在线' : '在线') + '</li>'
        + '<li>当前在组：' + (obj.assignmentName === null ? '' : obj.assignmentName) + '</li>'
        + '<li>当前在组所属组织：' + (obj.assignmentGroupName === null ? '' : obj.assignmentGroupName) + '</li>'
        + '<li class="cancel-area" id="personDetailsCancel"><i class="person-details-cancel"></i></li>'
        + '</ul>';
      $('#invisiblePersonDetails').html(html);
      $('#personDetailsCancel').on('click', this.detailsShowHandler.bind(this));
      this.detailsShowHandler();
    } else {
      layer.msg(data.msg);
    }
  },
  /**
   * 不可见人员详情
   */
  detailsShowHandler: function () {
    var $invisiblePersonDetails = $('#invisiblePersonDetails');
    if ($invisiblePersonDetails.hasClass('active')) {
      $invisiblePersonDetails.removeClass('active');
    } else {
      $invisiblePersonDetails.addClass('active');
    }
  }
};