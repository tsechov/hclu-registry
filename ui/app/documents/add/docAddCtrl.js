'use strict';

angular.module('smlHreg.docadd').controller('DocAddCtrl', function DocAddCtrl($q, $scope, DocAddService, NotificationsService) {
    $scope.master = {};
    $scope.doc = {};
    $scope.remoteErrors = {};

    var self = this;

    $scope.addDocument = function () {
        if ($scope.addForm.$valid) {
            DocAddService.addDocument($scope.doc).then(function (response) {
                NotificationsService.showSuccess('Document stored with registration number: ' + response.regId);
                self.reset();
            }, function (response) {
                if (response.status === 400 && angular.isArray(response.data.validationErrors)) {
                    angular.forEach(response.data.validationErrors, function (value) {
                        $scope.addForm[value.field].$setValidity("remote", false);
                        $scope.remoteErrors[value.field] = value.msg;
                    });
                }else {
                    NotificationsService.showError("Something went wrong...");
                    self.reset();
                }
            });
        }

    };

    this.reset = function(){
        $scope.doc=angular.copy($scope.master);
        $scope.remoteErrors=angular.copy($scope.master);
        $scope.addForm.$setPristine();
        $scope.addForm.$setUntouched();
    };
});