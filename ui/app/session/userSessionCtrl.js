'use strict';

angular.module('smlHreg.session').controller('UserSessionCtrl', function UserSessionCtrl($scope, UserSessionService) {

    $scope.getLoggedUserName = function () {
        return UserSessionService.getLoggedUserName();
    };

    $scope.logout = function () {
        UserSessionService.logout();
    };
});