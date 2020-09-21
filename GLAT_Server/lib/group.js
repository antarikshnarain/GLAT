var pgclient = require('../models/pgclient');
var as = require('async');

var group = {
  'addMembers': function (gid, mems, cb) {
    if (mems.constructor !== Array) mems = [mems];
    as.forEach(mems,
      function (mem, c) {
        pgclient.execute(`insert into group_members(gid,uid) values('${gid}','${mem}')`,
          function (err, result) {
            if (err) c(err);
            else c(null);
          });
      }, cb);
  },
  'setAdmin': function (gid, phone, cb) {
    group.isMember(gid, phone, function (err, result) {
      if (err) cb(err);
      else {
        if (result) {
          pgclient.execute(`update group_members set is_admin = true where uid='${phone}' and gid=${gid}`,
            function (err, res) {
              if (err) cb(err);
              else {
                cb(null, '1');
              }
            });
        } else cb(null, '0');
      }
    });
  },
  'isMember': function (gid, phone, cb) {
    pgclient.execute(`select count(*) from group_members where uid = '${phone}' and gid = ${gid}`,
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
  'filterMembersAsync': function (gid, phones, cb) {
    function isM(p, rcb) {
      group.isMember(gid, p, rcb);
    }
    as.filter(phones, isM, function (err, u) {
      if (err) cb(err);
      else {
        as.reject(phones, isM, function (err, nu) {
          if (err) cb(err);
          else {
            cb(null, { 'u': u, 'nu': nu });
          }
        });
      }
    });
  },
  'filterMembers': function (gid, users, cb) {
    var orderDict = users.map(function (user, index) {
      return "('" + user + "'," + index + ")";
    });
    var query = `select (case when gm.uid is not null then true else false end) as is_user,x.id as phone 
    from (select * from group_members where gid = ${gid}) as gm right join (values ${orderDict.join(",")}) 
    as x(id,ordering) on x.id = gm.uid order by (case when gm.uid is not null then true else false end) desc,x.ordering;`;
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
  }
}

module.exports = group;

//----------------------------------------------

if (require.main === module) {
  (function () {
    group.setAdmin(18, '1234567890', function (err, result) {
      if (err) console.log(err);
      else console.log(result);
    });
    group.filterMembers(1,['1234567890','111','918566','123'], function(err, result) {
      console.log(err, result);
    })
  })()
}