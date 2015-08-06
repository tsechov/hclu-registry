angular.module('smlHreg.userlist').factory('UserListService', function ($http) {
    var service = {};


    service.get = function () {
        return $http.get('api/users/list').then(success);
    };


    function success(response) {
        return response.data;
    }
    return service;
});