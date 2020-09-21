var pgclient = require('../../models/pgclient');
var group = require('../../lib/group');
var user = require('../../lib/user');

var add = function (req, res) {
  var resp = { 'status': 'fail', 'err': '', 'resp': {} };
  if (req.data && req.data.phone && req.data.gname && req.data.mems) {
    pgclient.execute(`select count(*) from groups where uid='${req.data.phone}' and gname = '${req.data.gname}'`,
      function (err, result) {
        if (err) {
          resp.err = err;
          res.json(resp);
        } else if (result && result.length && result[0].count != '0') {
          resp.err = 'Group already exists';//checked conflicting groups
          res.json(resp);
        }
        else {
          pgclient.execute(`insert into groups(uid,gname) values('${req.data.phone}','${req.data.gname}')`,
            function (err, result) {
              if (err) {
                resp.err = err;
                res.json(resp);
              } else {
                var gid;
                pgclient.execute(`select id as gid from groups where uid = '${req.data.phone}' and gname = '${req.data.gname}'`,
                  function (err, result) {
                    if (err) {
                      resp.err = err;
                      res.json(resp);
                    } else {
                      gid = result[0].gid;
                      if (req.data.mems.constructor !== Array) req.data.mems = [req.data.mems];
                      req.data.mems.push(req.data.phone);
                      var uni = Array.from(new Set(req.data.mems));
                      user.filterUsers(uni,
                        function (err, U) {
                          if (err) {
                            resp.err = err;
                            res.json(resp);
                          } else {
                            var toAdd = U.u;
                            group.addMembers(gid, toAdd,
                              function (err) {
                                if (err) {
                                  resp.err = err;
                                  res.json(resp);
                                } else {
                                  group.setAdmin(gid, req.data.phone,
                                    function (err, r) {
                                      if (err) {
                                        resp.err = err;
                                      } else {
                                        resp.status = 'success';
                                        resp.resp.gid = gid;
                                        resp.resp.added = toAdd;
                                        resp.resp.notAdded = U.nu;
                                      }
                                      res.json(resp);
                                    });
                                }
                              });
                          }

                        });
                    }
                  });

              }
            });
        }
      });
  } else {
    resp.err = 'Missing Arguments';
    res.json(resp);
  }
}

module.exports = add;   