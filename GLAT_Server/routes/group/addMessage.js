"use strict";

var Groups = require('../../models/groups');
var messageHandler = require('../../lib/message');
var butils = require('../../lib/butils');

var addMsg = function (req,res) {
  var gid = req.data.gid || 0;
  var uid = req.data.phone || '';
  var lat = butils.cleanLatLong(req.data.lat, 'lat');
  var long = butils.cleanLatLong(req.data.long, 'long');
  var sensorData = butils.cleanObject(req.data.sensorData);
  var body = req.data.body || '';
  var resp = {status: 'fail', err: '', resp: ''};
  if (gid && uid && lat && long && sensorData && body) {
    Groups.findOne({where: {id: gid}}, function (err, group) {
      if (!err && group) {
        var msgObj = {
          gid: gid,
          uid: uid,
          lat: lat,
          long: long,
          sensorData: sensorData,
          body: body
        };
        messageHandler.createMessage(msgObj, function (err, response) {
          if (err) {
            resp.err = 'execution_error';
          } else {
            resp.status = 'success';
            resp.resp = 'Message Added';
          }
          res.json(resp);
        });
      } else {
        resp.err = err ? 'execution_error' : 'No such group';
        res.json(resp); 
      }     
    });
  } else {
    resp.err = 'Incorrect parameters';
    res.json(resp);
  }
};

module.exports = addMsg;
