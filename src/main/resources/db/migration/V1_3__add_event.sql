create table event (
  raid varchar2,
  player varchar2,
  type varchar2,
  comment varchar2,
  time varchar2,
  primary key (raid, time),
  foreign key (raid) references raid (start),
  foreign key (player) references player (name)
);