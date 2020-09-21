var sendotp = require('./sendotp');
var login = require('./login');
var viewContacts = require('./contactsView');

var user = {
    'sendotp': sendotp,
    'login': login,
    'viewContacts': viewContacts
};
module.exports = user;