create table agent (
       id serial primary key,
       handle varchar,
       display_name varchar
       );
--;;
alter table boost add column agent int references agent;
