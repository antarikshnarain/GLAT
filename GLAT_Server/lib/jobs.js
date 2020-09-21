"use strict";

var kue = require('kue');
var config = require('./config');

var jobs = kue.createQueue(config.kue_config);

var jobHandler = {
  msgSolrAdd: function (msgObj) {
    jobs.create('msgSolrAdd', msgObj).save();
  } 
};

module.exports = jobHandler;
