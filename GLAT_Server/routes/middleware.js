"use strict";

var butils = require('../lib/butils');
var cache = require('../lib/cache'); 
var solr = require('solr-client');

var solrclient = solr.createClient({ core: 'gpsShit' });

var middleware = {
  checkToken: function (req, res, next) {
    var resp = {'status': 'fail','err':'','resp': ''};
    var phone = req.data.phone || '';
    var token = req.data.token || '';

    if (phone && token) {
      cache.getToken (phone, function (err, result) {
        if (!err && result) {
          if (result === token) {
            next();
          } else {
            resp.err = 'incorrect token';
            res.json(resp); 
          }
        } else {
          resp.err = err || 'login_again';
          res.json(resp);          
        }
      });
    } else {
      resp.err = 'Missing Authentication token!';
      res.json(resp);
    }
  },
  gpsStore: function (req, res, next) {
    solrclient.add({ name_s: req.body.name, location_p: req.body.lat + "," + req.body.long },
      function (err, obj) {
      if (err) {
        console.log(err);
      } else {
        console.log('Solr response:', obj);
        solrclient.commit(function (err, res) {
          if (err) console.log(err);
          if (res) console.log(res);
        });
        next();
      }
    });
  },

  getAll: function (req, res, next) {
    var chunks = [];
    console.log(JSON.stringify("Starting getAll"));
    req.on("data", function (chunk) {
      chunks.push(chunk);
    });
    req.on("end", function () {
      var buff = Buffer.concat(chunks);
      req.body = buff;
      next();
    });
    req.on("error", function (err) {
      console.error(err);
      res.status(500);
    });
  },
  navigate: function (req, res, next) {
    if (req.method == 'POST' && req.body) {
      var data = {};
      Object.keys(req.body).forEach(function (key) {
        if (key === 'phone') {
          req.body[key] = butils.cleanPhone(req.body[key]);
        } 
        data[key] = req.body[key];
      });
      req.data = data;
      next();
    } else {
      res.json({ 'status': 'fail', 'err': 'You ain\'t posting it right' });
    }
  }

};

module.exports = middleware; 