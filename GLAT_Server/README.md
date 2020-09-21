//For GLAT

APIS:

    /user/sendotp
    ->first api to hit
        req:
            {
                phone,name
            }
        res:
            {
                status: (success or fail),
                err: (empty on success else err msg) ['User exists','execution_error','conn_error','Missing Arguments']
                resp:{
                    contains otp on success.//To resend otp hit same api again 
                    on error 'User exists' contains name of user.
                    errors execution_error and conn_error thrown on pgclient errors.
                    //resp also contains msg, which could be displayed on client side** (not for all errors - see code)
                }
            }

    /user/login
    ->To hit after signup
        req:
            {
                phone
            }
        res:
            {
                status:['success','fail'],
                err:['no such user','redis_error','Missing Arguments','execution_error','conn_error'],
                resp:{
                    contains token on success
                }
            }

    /group/add
    ->hit to add group
        req:
            {
                phone,gname,mems (Array of phone numbers),token
            }
        res:
            {
                status: ['success','fail'],
                err: ['Group already exists','Missing arguments','execution_error','login_again','redis_error']
                resp: {
                    added: [array of numbers added to group],
                    notAdded: [array of numbers not added to group],
                    gid: unique group identifier
                }                    
            }

    /user/contacts/view
    ->hit for viewing contacts list
        req:
            {
                phone,token,contacts array
            }
        res:
            {
                status: ['success','fail'],
                err: ['execution_error','Missing arguments'],
                resp: [{dname,phone}]
            }

    /group/message/add
    ->hit for adding message
        req:
            {
                phone,token,lat(-90 to 90),long(-180 to 180),body,sensorData(object),gid
            }
        res:
            {
                status: status: ['success','fail'],
                err: ['exec_error', 'login_again', 'Incorrect parameters', 'No such group']
                resp: "Message added" (on success)
            }
