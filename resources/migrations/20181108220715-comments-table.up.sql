create table comment (
       id serial primary key,
       proposal_id int references proposal,
       text varchar,
       author_id int references agent,
       interested boolean
)
