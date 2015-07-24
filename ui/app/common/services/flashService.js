'use strict';

angular.module('smlHreg.common.services').factory('FlashService', function () {

    var queue = [];

    return {
        set: function (message) {
            queue.push(message);
        },
        get: function () {
            return queue.shift();
        }
    };
});
