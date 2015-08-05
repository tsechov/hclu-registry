'use strict';

describe('Version Controller', function () {
    beforeEach(module('smlHreg.version'));

    var scope, $httpBackend;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;
        $controller('VersionCtrl', {$scope: scope});
    }));

    it('should retrieve application version', function () {
        // given
        $httpBackend.expectGET('api/version').respond({build: 'foo', date: 'bar', version: 'baz', branch: 'branch', buildNumber: 'buildnumber', buildUrl: 'buildUrl'});

        // when
        $httpBackend.flush();

        // then
        expect(scope.buildSha).toEqual('foo');
        expect(scope.buildDate).toEqual('bar');
        expect(scope.version).toEqual('baz');
        expect(scope.branch).toEqual('branch');
        expect(scope.buildNumber).toEqual('buildnumber');
        expect(scope.buildUrl).toEqual('buildUrl');

    });
});