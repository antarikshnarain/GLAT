"use strict";

var kue = require('kue');
var config = require('./lib/config');
var msgHandler = require('./lib/message');

var jobs = kue.createQueue(config.kue_config);

jobs.on('job complete', function (id, result) {
  kue.Job.get(id, function (err, job) {
    if (err) {
      console.error('fail to get job ' + id + 'error ' + err);
    } else {
      job.remove(function (err) {
        if (err) {
          console.error('fail removed completed job ' + job.id + 'error ' + err);
        } else {
          console.info('removed completed job ' + job.id);
        }
      });
    }
  });
});

jobs.process('msgSolrAdd', function (job, done) {
  var data = job.data;
  if (data) {
    msgHandler.createMessageSolr(data, function (err, res) {
      if (err) {
        console.error(`[ERR msgSolrAdd] ${err}`);
      } else {
        console.info(`[INFO msgSolrAdd] ${res}`);
      }
      done();
    });
  } else {
    console.error(`[ERR msgSolrAdd] Wrong Job received`);
    done();
  }
});