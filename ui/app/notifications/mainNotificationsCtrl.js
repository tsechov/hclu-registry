'use strict';

angular.module('smlHreg.notifications')
    .controller('MainNotificationsCtrl', function MainNotificationsCtrl($scope, NotificationsService) {

        $scope.notificationsService = NotificationsService;

    });
