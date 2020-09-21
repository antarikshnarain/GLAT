var redis = require("redis");
var chance = require('chance');
var prefix = 'glat_prefix_';
var timeout = 3600*24*7;
var cache = {
    createToken: function(phone,cb){
                    var client = redis.createClient();
                    var redisKey = prefix + phone;
                    var token = new chance().string({length:32,pool:"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"});
                    client.setex(redisKey,timeout,token,function(err,reply){
                        client.end(true);
                        if (err){ 
                            console.log("Redis Error : "+ err);
                            cb('redis_error');
                        }    
                        else cb(null,token);
                    });
    },
    getToken: function(phone,cb){
                var client = redis.createClient();
                var redisKey=prefix+phone;
                client.get(redisKey,function(err,token){
                    
                    if (err){ 
                        console.log("Redis Error : "+ err);
                        client.end(true);
                        cb('redis_error');
                    }    
                    else if (token){
                        client.setex(redisKey,timeout,token,function(err,reply){
                            client.end(true);
                            if (err){ 
                                console.log("Redis Error : "+ err);
                                cb('redis_error');
                            }    
                            else cb(null,token);
                        });
                    }
                    else{
                        cb(null,null);//user key doesn't exist!
                    } 
                });
    }
}


module.exports = cache;

//--------------------------------------------------------------------------------------

if(require.main===module){
    (function(){
        cache.setToken('9990001112',function(err){
            if(err)console.log(err);
            else console.log("Done");
        })
        cache.getToken('9990001110',function(err,reply){
            if(err)console.log(err);
            else console.log(reply);
        })
    })();
}