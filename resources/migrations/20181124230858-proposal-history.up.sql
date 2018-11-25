alter table proposal add column updated timestamptz  default now();
create table proposal_history ( like proposal including all );
alter table proposal_history drop constraint proposal_history_pkey;
alter table proposal_history add primary key (id, updated);

create function copy_to_history() returns trigger as $cp$
begin
  insert into proposal_history values(new.*);
  return new;
end;
$cp$ language plpgsql;

create trigger update_proposal_history
 after insert or update on proposal
 for each row
 execute procedure copy_to_history();
