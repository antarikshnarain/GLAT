"use strict";
var schema = require('./schema');

var message = schema.define('message', {
  id: {type: schema.Number},
  gid: {type: schema.Number},
  uid: {type: schema.String, limit: 20},
  latlong: {type: schema.String, limit: 40},
  body: {type: schema.Text},
  visible_status: {type: schema.Number, default: 0},
  sensor_data: {type: schema.JSON},
  created_at: {type: schema.Date, default: Date.now},
  modified_at: {type: schema.Date, default: Date.now}
});

module.exports = message;

//-----------------------------------------

if (require.main === module) {
  (function () {
    message.all({}, function(err,res) {
      console.log(err || res);
    });
  })();
}
