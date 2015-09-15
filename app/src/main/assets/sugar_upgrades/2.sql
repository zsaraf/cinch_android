alter table available_block drop column start_time;
alter table available_block drop column end_time;
alter table available_block add column start_time bigint; 
alter table available_block add column end_time  bigint;