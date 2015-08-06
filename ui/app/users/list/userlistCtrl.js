'use strict';

angular.module('smlHreg.userlist').controller('UserListCtrl', function UserListCtrl($q, $scope, UserListService) {


    UserListService.get().then(function (users) {
        $scope.users = users;

    });

    $scope.edit = function (user) {
        var u = user;
        u.toString();
    };


});