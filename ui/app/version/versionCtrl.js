'use strict';

angular.module('smlHreg.version').controller('VersionCtrl', function VersionCtrl($scope, VersionService) {

    VersionService.getVersion().then(function (version) {
        $scope.buildSha = version.getBuildSha();
        $scope.buildDate = version.getBuildDate();
        $scope.version = version.getVersion();
        $scope.branch = version.getBranch();
        $scope.buildNumber = version.getBuildNumber();
    });
});