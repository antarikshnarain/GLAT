var pgclient = require('../../models/pgclient');
var Chance = require('chance');

var sendotp = function (req, res) {
  var resp = { 'status': 'fail', 'err': '', 'resp': {} };
  if (req && req.data.phone && req.data.name) {
    pgclient.execute(`select * from users where phone = '${req.data.phone}'`, function (err, result) {
      if (err) {
        resp.err = err;
        res.json(resp);
      } else {
        if (result.length) {
          var otp = new Chance().string({ length: 4, pool: '0123456789' });
          console.log(otp);//send sms from here too!!
          resp.status = 'success';
          if (result[0].is_verified) {
            resp.resp = { 'msg': 'User exists', 'name': result[0].dname, 'otp': otp };
          }
          else {
            resp.resp = { 'msg': 'otp resend', 'name': result[0].dname, 'otp': otp };
          }
          res.json(resp);
        }
        else {
          var query = `insert into users values('${req.data.phone}','${req.data.name}',0)`;
          pgclient.execute(query, function (err, result) {
            if (err) {
              resp.err = err;
              res.json(resp);
            } else {
              var otp = new Chance().string({ length: 4, pool: '0123456789' });
              console.log(otp);//send sms from here
              resp.status = 'success';
              resp.resp = { 'msg': 'Added user', 'otp': otp };
              res.json(resp);
            }
          })
        }
      }
    })
  } else {
    resp.err = 'Missing arguments';
    resp.resp = { 'api_hit': req.url };
    res.json(resp);
  }
}

module.exports = sendotp;