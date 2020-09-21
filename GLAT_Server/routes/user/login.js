var pgclient = require('../../models/pgclient');
var cache = require('../../lib/cache');
var chance = require('chance');
var login =
  function (req, res) {
    var resp = { 'status': 'fail', 'err': '', 'resp': {} };
    if (req && req.data.phone) {
      pgclient.execute(`select count(*) from users where phone = '${req.data.phone}'`,
        function (err, result) {
          if (err) {
            resp.err = err;
            res.json(resp);
          } else {
            if (result.length && result[0].count !== '0') {
              pgclient.execute(
                `update users set is_verified = 1 where phone = '${req.data.phone}'`,
                function (err, result) {
                  if (err) {
                    resp.err = err;
                    res.json(resp);
                  } else {
                    cache.createToken(req.data.phone,
                      function (err, token) {
                        if (err) {
                          resp.err = err;
                          res.json(resp);
                        } else {
                          resp.status = 'success';
                          resp.resp = { 'token': token };
                          res.json(resp);
                        }
                      });
                  }
                }
              );
            } else {
              resp.err = 'no such user';
              res.json(resp);
            }
          }
        }
      );
    } else {
      resp.err = 'Missing arguments'
      res.json(resp);
    }
  }
module.exports = login;