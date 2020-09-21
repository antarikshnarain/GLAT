"use strict";
var schema = require('./schema');

var users = schema.define('users', {
  id: {type: schema.Number},
  phone: {type: schema.String, limit: 20},
  dname: {type: schema.String, limit: 100},
  is_verified: {type: schema.Boolean, default: false}
});

module.exports = users;

//-----------------------------------------

if (require.main === module) {
  (function () {
    users.findOrCreate({phone: '12121', dname: 'alala'}, function(err,res) {
      console.log(err || res);
    });
  })();
}
