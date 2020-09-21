
var config = {
  pg_config: {
    pg_conn_string: "postgres://glat:yummy@localhost/glatstuff",
    db_config: {
      driver: 'postgres',
      username: 'glat',
      database: 'glatstuff',
      password: 'yummy',
      host: 'localhost',
      port: 5432,
      pool: false
    }
  },
  kue_config : {
     prefix: 'glat_q_',
     redis: {
       port: 6379,
       host: 'localhost',
       auth: '',
       db: 3
     }
  }
};

module.exports = config;