'use strict';

// Declare app level module which depends on filters, and services

var ngOGFrAdminApp = angular.module('ngOGFrAdminApp', ['ngRoute', 'ui.bootstrap', 'ngGrid', 'xeditable', 'angular-loading-bar', 'treeControl', 'hljs', 'checklist-model', 'angularFileUpload']).
        config(['$routeProvider', function ($routeProvider) {
                $routeProvider.when('/applications', {templateUrl: 'partials/applications.html', controller: 'BundlesCtrl'});
                $routeProvider.when('/loggers', {templateUrl: 'partials/loggers.html', controller: 'getLoggersCtrl'});
                $routeProvider.when('/users', {templateUrl: 'partials/users.html', controller: 'getUserListCtrl'});
                $routeProvider.when('/resources', {templateUrl: 'partials/resources.html', controller: 'ResourcesCtrl'});
                $routeProvider.otherwise({redirectTo: '/applications'});
            }]).
        run(function (editableOptions) {
            editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
        });
