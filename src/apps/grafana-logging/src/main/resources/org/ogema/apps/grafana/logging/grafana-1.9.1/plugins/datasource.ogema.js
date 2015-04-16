/*! grafana - v1.9.1 - 2014-12-29
 * Copyright (c) 2014 Torkel Ödegaard; Licensed Apache License */

define([
        'angular',
        'lodash',
        'kbn',
        'moment'
      ],
      function (angular, _, kbn) {
        'use strict';

        var module = angular.module('grafana.services');
        var instances = [];
        module.factory('OgemaDatasource', function($q) {
        	
        
          // the datasource object passed to constructor
          // is the same defined in config.js
          function OgemaDatasource(datasource) {
        	  console.log("Reading Ogema datasource",this);
            this.name = datasource.name;
            this.supportMetrics = true;
            this.url = datasource.url;  
            instances.push(this);
          }

    //      OgemaDatasource.prototype.query = function(filterSrv, options) {
          OgemaDatasource.prototype.query = function(options) {
            // do your thing, return promise with the time series    
        	  console.log("Ogema query being sent",options);
            return $q.when({data: [] });
          };

      /*    var testInstance = new OgemaDatasource({type: 'OgemaDatasource', name: 'hello OGEMA', url: "https://localhost:8443/apps/ogema/grafanatestapp/fake_influxdb"});
          console.log("Calling testinstance",testInstance.query({})); */
          
          return OgemaDatasource;

        });
       /*  setTimeout(function(){ 
        	for (var i=0;i<instances.length;i++) {
        		var inst = instances[i];
        		inst.query({});
        	}
        	
        }, 10000); */

      });