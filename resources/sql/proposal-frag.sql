select p.*,
       s.handle  as proposer,
       (select count(id) from comment where comment.proposal_id = p.id)
       	       as comments,
       (select coalesce(sum(points),0) from kudosh where kudosh.proposal_id = p.id)
       	       as kudosh
from (proposal p left join subscriber s on s.id=p.proposer_id)
where p.created_at is not null
