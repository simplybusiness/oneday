select p.*,
       a.handle  as sponsor,
       (select count(id) from comment where comment.proposal_id = p.id)
       	       as comments,
       (select coalesce(sum(points),0) from kudosh where kudosh.proposal_id = p.id)
       	       as kudosh
from (proposal p left join agent a on a.id=p.agent)
where p.created_at is not null
