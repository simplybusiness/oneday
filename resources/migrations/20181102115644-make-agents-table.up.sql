create table agent (
       id serial primary key,
       handle varchar,
       display_name varchar
       );
--;;
alter table proposal add column agent int references agent;
