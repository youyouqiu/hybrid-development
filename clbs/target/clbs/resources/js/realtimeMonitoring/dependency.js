var Dependency = function (initialDependency) {
    this._dependencies = initialDependency ? initialDependency : {};
}

Dependency.prototype.set = function (key,val) {
    this._dependencies[key] = val;
}

Dependency.prototype.get = function (key) {
    return this._dependencies[key];
}