'use strict';

angular.module('smlHreg.version').factory('Version', function () {

    var Version = function (data) {
        this.buildSha = data.build;
        this.buildDate = data.date;
        this.version = data.version;
        this.branch = data.branch;
        this.buildNumber = data.buildNumber;
    };

    Version.prototype.getBuildSha = function () {
        return this.buildSha;
    };

    Version.prototype.getBuildDate = function () {
        return this.buildDate;
    };

    Version.prototype.getVersion = function () {
        return this.version;
    };

    Version.prototype.getBranch = function () {
        return this.branch;
    };

    Version.prototype.getBuildNumber = function () {
        return this.buildNumber;
    };

    return Version;
});

