angular.module('smlHreg.doclist').factory('DocListService', function ($http) {
    var service = {};


    service.get = function () {
        return $http.get('api/documents/list').then(success);
    };


    function success(response) {
        return response.data;
    }
    return service;
});