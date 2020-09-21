"use strict";
var schema = require('./schema');

var groups = schema.define('groups', {
  id: {type: schema.Number},
  uid: {type: schema.String, limit: 20},
  gname: {type: schema.String, limit: 40},
  is_active: {type: schema.Boolean, default: false}
});

module.exports = groups;

//-----------------------------------------

if (require.main === module) {
  (function () {
    groups.all({}, function (err, res) {
      console.log(err || res);
    });
  })();
}
