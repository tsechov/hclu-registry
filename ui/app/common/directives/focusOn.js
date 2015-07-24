'use strict';

angular.module('smlHreg.common.directives').directive('focusOn', function () {
    return function (scope, elem) {
        elem[0].focus();
    };
});