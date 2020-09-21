"use strict";

var db_option = require('../lib/config').pg_config.db_config;
var caminte = require('caminte');

var Schema = caminte.Schema;

var schema = new Schema(db_option.driver, db_option);

module.exports = schema;
