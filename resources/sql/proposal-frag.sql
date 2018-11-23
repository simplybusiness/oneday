select p.*,
       s.handle  as proposer,
       (select count(comment.id) from comment where nullif(comment.text,'') is not null and comment.proposal_id = p.id)
       	       as comments,
       (select count(kudosh.sponsor_id) from kudosh where kudosh.proposal_id = p.id)
       	       as sponsors_count
from (proposal p left join subscriber s on s.id=p.proposer_id)
where p.created_at is not null
