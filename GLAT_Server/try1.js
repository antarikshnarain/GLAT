var express = require('express');
var routes = require('./routes');


var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.json());
app.use(function(req,res,next){
                    console.log(req.method+" request from "+req.ip+" "+JSON.stringify(req.body)+" "+req.url);
                    next();
});

app.use('/',routes);
//app.use(routes.mw.logErrors);
//app.use(routes.mw.errorHandler);

app.listen('8080','0.0.0.0');
