select p.*,
       a.handle  as sponsor,
       (select count(id) from comment where comment.proposal_id = p.id)
       	       as comments
from (proposal p left join agent a on a.id=p.agent)
where p.created_at is not null
