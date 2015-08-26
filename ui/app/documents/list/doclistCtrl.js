'use strict';

angular.module('smlHreg.doclist').controller('DocListCtrl', function DocListCtrl($q, $scope, UserListService) {


    UserListService.get().then(function (users) {
        $scope.users = users;

    });

    $scope.edit = function (user) {
        var u = user;
        u.toString();
    };


});