"use strict";

var butils = require('../lib/butils');
var pgclient = require('../models/pgclient');
var as = require('async');
var user = {
  'is_user': function (user, cb) {
    pgclient.execute(`select count(*) from users where phone = '${user}'`,
      function (err, result) {
        if (err) {
          cb(err);
        } else if (result && result.length) {
          if (result[0].count == '0')
            cb(null, false);
          else cb(null, true);
        }
      });
  },
  'are_users': function (users, cb) {
    as.map(users, user.is_user, cb);
  },
  'filterUsersAsync': function (users, cb) {
    as.filter(users, user.is_user, function (err, u) {
      if (err) cb(err);
      else {
        as.reject(users, user.is_user, function (err, nu) {
          if (err) cb(err);
          else {
            cb(null, { 'u': u, 'nu': nu });
          }
        });
      }
    });
  },
  'filterUsers': function (users, cb) {
    var orderDict = users.map(function (user, index) {
      return "('" + user + "'," + index + ")";
    });
    var query = `select (case when u.phone is not null then true else false end) as is_user,x.id as phone 
    from users u right join (values ${orderDict.join(",")}) as x(id,ordering) on x.id = u.phone order by 
    (case when u.phone is not null then true else false end) desc,x.ordering;`
    pgclient.execute(query, function (err, rows) {
      if (!err && rows) {
        var ret = {
          u: [],
          nu: []
        };
        ret.u = rows.reduce(function (list, row) {
          if (row.is_user) list.push(row.phone);
          return list
        }, []);
        ret.nu = rows.reduce(function (list, row) {
          if (!row.is_user) list.push(row.phone);
          return list
        }, []);
        cb(null, ret);
      } else {
        cb('execution_error');
      }

    });
  },
  'getNames': function (users, cb) {
    var map_dict = {};
    users.forEach(function(number) {
      var temp_key = butils.cleanPhone(number);
      if (temp_key) map_dict[temp_key] = number;
    });
    var query = `with a as (select unnest(ARRAY['${Object.keys(map_dict).join("','")}']) as num)
    select a.num as phone,u.dname from a left join users u on u.phone = a.num order by u.dname;`
    pgclient.execute(query, function (err, result) {
      if (!err && result) {
        result = result.map((retObj) => ({phone: map_dict[retObj.phone],dname: retObj.dname}));
        cb(null, result);
      } else {
        cb('Missing Parameters');
      }
    });
  }
}

module.exports = user;

//-------------------------------------------

if (require.main === module) {
  (function () {
    user.filterUsers(['1234567890', '918566', '123', '2313211212122'], function (err, result) {
      if (err) console.log(err);
      else console.log(result);
    });
    user.getNames(['1234567890', '918566', '9962036295', '2313211212122'], function (err, result) {
      console.log(err, result);
    });
  })();
}