var express = require('express');
var mw = require('./middleware');
var user = require('./user');
var group = require('./group');
var routes = express.Router();

routes.post('/user/sendotp',mw.navigate,user.sendotp);
routes.post('/user/login',mw.navigate,user.login);
routes.post('/group/add',mw.navigate,mw.checkToken,group.add);
routes.post('/group/message/add',mw.navigate,mw.checkToken,group.addMessage);
routes.post('/user/contacts/view',mw.navigate, mw.checkToken, user.viewContacts);
routes.post('/swap',function(req,res){
    console.log(req.ip+" "+req.body.name);
    res.send(JSON.stringify({"Name":req.body.age,"Age":req.body.name}));
});

routes.post('/printBody',function(req,res){
    res.send(req.body);
    console.log(req.body);
});
routes.post('/hello',function(req,res){
    console.log(req.ip+" "+req.body.name);
    res.send(JSON.stringify({"Msg":"Hello "+req.body.name,"Square":Math.pow(req.body.age,2)}));
});

routes.post('/gps',mw.gpsStore,function(req,res){
    console.log("Probably Done?");
    res.send("Done...");
});

module.exports = routes;