create type proposal_status as enum ('Draft','Open','Completed', 'Withdrawn');
alter table proposal add column status proposal_status;
alter table proposal_history add column status proposal_status;
