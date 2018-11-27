drop trigger if exists update_proposal_history on proposal ;
drop function if exists copy_to_history();
drop table if exists proposal_history;
