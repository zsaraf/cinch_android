alter table AVAILABLE_JOB add LOCATION_NOTES TEXT;
drop table MESSAGE;
alter table SESH add CHATROOM INTEGER;
alter table STUDENT drop HOURS_LEARNED;
alter table TUTOR drop HOURS_TUTORED;
alter table STUDENT add HOURS_LEARNED FLOAT;
alter table TUTOR add HOURS_TUTORED FLOAT;
