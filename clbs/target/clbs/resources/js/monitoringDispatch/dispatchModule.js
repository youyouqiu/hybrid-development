var DispatchModule = function () {
  this._modules = {};
};

DispatchModule.prototype = {
  constructor: DispatchModule,
  set: function (key, value) {
    this._modules[key] = value;
  },
  get: function (key) {
    return this._modules[key]
  }
};