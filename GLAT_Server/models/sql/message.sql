create table message(
  id serial primary key,
  gid int not null,
  uid varchar(20) not null,
  body text,
  latlong varchar(40),
  sensor_data json ,
  visible_status int default 0,
  created_at timestamp default current_timestamp,
  modified_at timestamp default current_timestamp
);

create index message_gid on message(gid);
create index message_id on message(id);