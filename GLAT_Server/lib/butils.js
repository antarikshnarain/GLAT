"use strict";

var butils = {
  cleanPhone: function (phone) {      
    if (phone && (phone = phone.match(/\d+/g))) {
      phone = phone.join("").replace(/^0+/, '');
      phone = (phone.length == 10 && (['9','8','7'].indexOf(phone[0])> -1)) ? "91" + phone : phone;
      return phone.length > 5 ? phone : false;
    } else {
      return false;  
    }     
  },
  cleanLatLong: function (latOrLong, type) {
    var rangeDict = {
      lat: [-90, +90],
      long: [-180, +180]
    };
    function between(v, rangeArr) {
      return parseFloat(v) >= rangeArr[0] && parseFloat(v) <= rangeArr[1] ? v : false;
    }
    try {
      return between(String(latOrLong).match(/^[-+]?\d+([.]\d+)?$/g)[0], rangeDict[type]);
    } catch (e) {
      return false;
    } 
  },
  cleanObject: function (testObj) {
    return testObj === Object(testObj) ? testObj : false;
  }
};

module.exports = butils;

//---------------------------------------------

if (require.main === module) {
  console.log(butils.cleanLatLong(process.argv[2] || '+91-99620-36295', process.argv[3] || 'lat'));
}
