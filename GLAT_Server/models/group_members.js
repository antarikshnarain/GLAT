"use strict";
var schema = require('./schema');

var groupMembers = schema.define('group_members', {
  gid: {type: schema.Number},
  uid: {type: schema.String, limit: 20},
  is_admin: {type: schema.Boolean, default: false}
});

module.exports = groupMembers;

//-----------------------------------------

if (require.main === module) {
  (function () {
    groupMembers.all({}, function (err, res) {
      console.log(err || res);
    });
  })();
}
