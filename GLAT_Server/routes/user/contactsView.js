"use strict";

var user = require('../../lib/user');

var contactsView = function (req, res) {
  var resp = {'status':'fail','err':'','resp':[]};
  var contacts = req.data.contacts || '';
  contacts = contacts.split(',');
  if (contacts && Array.isArray(contacts)) {
    user.getNames(contacts, function (err, result) {
      if (!err) {
        resp.status = 'success';
        resp.resp = result;
      } else {
        resp.err = err;
      }
      res.json(resp);
    });
  } else {
    resp.err('Missing arguments');
    res.json(resp);
  }
};

module.exports = contactsView;
