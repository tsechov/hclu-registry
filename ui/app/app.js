'use strict';

angular.module('smlHreg.common.directives', []);
angular.module('smlHreg.common.filters', []);
angular.module('smlHreg.common.services', []);
angular.module('smlHreg.common', ['smlHreg.common.filters', 'smlHreg.common.directives', 'smlHreg.common.services']);
angular.module('smlHreg.notifications', []);
angular.module('smlHreg.version', []);
angular.module('smlHreg.session', ['ngCookies', 'ngResource']);
var smlHreg = angular.module('smlHreg', ['smlHreg.templates', 'smlHreg.profile', 'smlHreg.session', 'smlHreg.common', 'smlHreg.notifications', 'smlHreg.version', 'ngSanitize', 'ui.router', 'smlHreg.userlist']);
var profile = angular.module('smlHreg.profile', ['ui.router', 'smlHreg.session', 'smlHreg.common', 'smlHreg.notifications']);
angular.module('smlHreg.userlist',[]);

profile.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('', '/');
    $stateProvider
        .state('login', {
            url: '/login',
            controller: 'LoginCtrl',
            templateUrl: 'profile/login/login.html',
            params: {
                // these are used to redirect to a secure page if the user is not logged in
                // see the $stateChangeStart handler below and LoginCtrl
                targetState: null,
                targetParams: null
            }
        })
        .state('register', {
            url: '/register',
            controller: 'RegisterCtrl',
            templateUrl: 'profile/register/register.html'
        })
        .state('recover-lost-password', {
            url: '/recover-lost-password',
            controller: 'PasswordRecoveryCtrl',
            templateUrl: 'profile/password/recover-lost-password.html'
        })
        .state('password-reset', {
            url: '/password-reset?code',
            controller: 'PasswordRecoveryCtrl',
            templateUrl: 'profile/password/password-reset.html'
        })
        .state('profile', {
            url: '/profile',
            controller: 'ProfileCtrl',
            templateUrl: 'profile/profile/profile.html',
            resolve: {
                //this is a kind of constructor injection to controller, since ProfileCtrl require logged user.
                user: function (UserSessionService) {
                    return UserSessionService.getLoggedUserPromise();
                }
            },
            data: {
                auth: true
            }
        })
        ;
});

smlHreg.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/error404');

    $stateProvider
        .state('error404', {
            url: '/error404',
            templateUrl: 'common/errorpages/error404.html'
        })
        .state('main', {
            url: '/main',
            templateUrl: 'common/private.html',
            data: {
                auth: true
            }
        })
        .state('home', {
            url: '/',
            templateUrl: 'common/public.html'
        }).state('users', {
            url: '/users',
            controller: 'UserListCtrl',
            templateUrl: 'users/list/userlist.html'
        });
});

smlHreg.config(['$httpProvider', function ($httpProvider) {
    var interceptor = ['$rootScope', '$q', '$injector', '$log', 'NotificationsService', function ($rootScope, $q, $injector, $log, NotificationsService) {

        function redirectToState(stateName) {
            // Because $httpProvider is a factory for $http which is used by $state we can't inject it directly
            // (this way we will get circular dependency error).
            // Using $injector.get will lead to having two instances of $http in our app.
            // By calling $injector.invoke we can delay injection to the moment when application is up & running,
            // therefore we will be injecting existing (and properly configured) $http instance.
            $injector.invoke(function ($state) {
                $state.go(stateName);
            });
        }

        function success(response) {
            return response;
        }

        function error(response) {
            if (response.status === 401) { // user is not logged in
                $rootScope.$emit('401');
            } else if (response.status === 403) {
                $log.warn(response.data);
                // do nothing, user is trying to modify data without privileges
            } else if (response.status === 404) {
                redirectToState('error404');
            } else if (response.status === 409) {
                NotificationsService.showError(response);
            } else {
                NotificationsService.showError('Something went wrong..', 'Unexpected error');
            }
            return $q.reject(response);
        }

        return {
            response: success,
            responseError: error
        };

    }];
    $httpProvider.interceptors.push(interceptor);
}]);

smlHreg.run(function ($rootScope, UserSessionService, FlashService, $state) {

    function requireAuth(targetState) {
        return targetState && targetState.data && targetState.data.auth;
    }

    $rootScope.$on('$stateChangeStart', function (ev, targetState, targetParams) {
        if (requireAuth(targetState) && UserSessionService.isNotLogged()) {
            ev.preventDefault();
            UserSessionService.getLoggedUserPromise().then(function () {
                $state.go(targetState, targetParams);
            }, function () {
                UserSessionService.saveTarget(targetState, targetParams);
                $state.go('login');
            });
        }
    });

    $rootScope.$on('401', function () {
        if (UserSessionService.isLogged()) {
            UserSessionService.resetLoggedUser();
            FlashService.set('Your session timed out. Please login again.');
        }
    });
});

smlHreg.run(function ($rootScope, $timeout, FlashService, NotificationsService) {
    $rootScope.$on('$stateChangeSuccess', function () {
        var message = FlashService.get();
        NotificationsService.showInfo(message);
    });
});

