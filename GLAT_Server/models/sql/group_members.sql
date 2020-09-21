create table group_members(
  id serial primary key,
  gid int not null,
  uid varchar(20) not null,
  is_admin boolean default false  
);

create index gmem_gid on group_members(gid);