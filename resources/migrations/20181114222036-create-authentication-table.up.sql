create table authentication (
       subscriber_id int references subscriber,
       iss varchar not null,
       sub varchar not null,
       display_name varchar,
       payload jsonb,
       primary key (iss, sub));
