'use strict';

angular.module('smlHreg.version').factory('Version', function () {

    var Version = function (data) {
        this.buildSha = data.build;
        this.buildDate = data.date;
    };

    Version.prototype.getBuildSha = function () {
        return this.buildSha;
    };

    Version.prototype.getBuildDate = function () {
        return this.buildDate;
    };

    return Version;
});

