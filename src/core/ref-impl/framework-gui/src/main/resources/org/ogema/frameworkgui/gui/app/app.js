'use strict';

// Declare app level module which depends on filters, and services

var ngOGFrGui = angular.module('ngOGFrGui', ['ngRoute', 'ui.bootstrap', 'angular-loading-bar',]).
    config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/applications', {templateUrl: 'partials/applications.html', controller: 'BundlesCtrl'});
            $routeProvider.otherwise({redirectTo: '/applications'});
    }]);
