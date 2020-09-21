"use strict";

var solr = require('solr-client');
var jobs = require('./jobs');
var Message = require('../models/message');

var solrclient = solr.createClient({ core: 'gpsShit' });

var messageHandler = {
  createMessage: function (msgObj, cb) {
    if (msgObj && Number(msgObj.gid) && msgObj.uid && msgObj.lat && msgObj.long && msgObj.sensorData && msgObj.body) {
      var sqlObj = {
        gid: msgObj.gid,
        latlong: msgObj.lat + "," + msgObj.long,
        sensor_data: msgObj.sensorData,
        body: msgObj.body,
        uid: msgObj.uid
      }
      Message.create(sqlObj, function (err, msg) {
        if (err) {
          cb(err);
        } else {
          cb(null,msg);
          sqlObj['mid'] = msg.id;
          jobs.msgSolrAdd(sqlObj);
        }
      });
    } else {
      cb('Missing Arguments');
    }
  },
  createMessageSolr: function (msgObj, cb) {
    if (msgObj && Number(msgObj.gid) && Number(msgObj.mid) && msgObj.latlong && msgObj.sensor_data) {
      var solrObj = {
        msg_s: msgObj.body, 
        location_p: msgObj.latlong, 
        gid_i: msgObj.gid,
        mid_i: msgObj.mid,
        uid_s: msgObj.uid,
        sensordata_txt: JSON.stringify(msgObj.sensor_data) 
      };
      solrclient.add(solrObj,
        function (err, response) {
        if (err) {
          cb(err);
        } else {
          console.log(response);
          solrclient.commit(function (err, res) {
            if (err) {
              cb(err);
            } else {
              cb(null, `Added message (gid-mid) (${msgObj.gid}-${msgObj.mid}))`);
            }
          });
        }
      });
    } else {
      cb('Missing Arguments');
    }
  }
}

module.exports = messageHandler;

//-----------------------------------------------------

if (require.main === module) {
  var msgObj = {
    gid: 24,
    uid: '1234567890',
    sensorData: {
      acc: 123.45,
      gyro: 22
    },
    lat: 23.45,
    long: 45.66,
    body: "This was the next msg stored"
  }
  var solrObj = {
    gid: 24,
    latlong: '23.45,45.66',
    sensor_data: { acc: 123.45, gyro: 22 },
    body: 'This was the first msg stored',
    uid: '1234567890',
    mid: 1
  }
  messageHandler.createMessage(msgObj, function (err, res) {
    console.log(err || res);
  });
}
