create table kudosh (
       points integer,
       created_at timestamptz default now(),
       proposal_id int references proposal,
       sponsor_id int references agent
       );
       
