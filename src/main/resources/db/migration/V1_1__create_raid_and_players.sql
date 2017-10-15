create table player (
  name VARCHAR primary key,
  class varchar,
  roles varchar
);

insert into player values ('Twiniings', 'Druid', 'Tank');
insert into player values ('Gussie', 'Paladin', 'Tank');

insert into player values ('Lashin', 'Deathknight', 'Melee');
insert into player values ('Talltheuh', 'Deathknight', 'Melee,Tank');
insert into player values ('Drahc', 'Druid', 'Tank, Melee');
insert into player values ('Frozzenfire', 'Druid', 'Tank,Melee');
insert into player values ('Oxymortisai', 'Warrior', 'Melee');
insert into player values ('Rza', 'Warrior', 'Melee');
insert into player values ('Nestyyw', 'Warrior', 'Melee');
insert into player values ('Eliias', 'Rogue', 'Melee');
insert into player values ('Slip', 'Rogue', 'Melee');
insert into player values ('Bujumbura', 'DemonHunter', 'Melee');

insert into player values ('Rathhal', 'Druid', 'Healer, Ranged');
insert into player values ('Markant', 'Shaman', 'Healer');
insert into player values ('Furo', 'Paladin', 'Healer');
insert into player values ('Cowstyle', 'Priest', 'Healer,Ranged');
insert into player values ('Drizz', 'Shaman', 'Healer');

insert into player values ('Dunder', 'Priest', 'Ranged');
insert into player values ('Crispy', 'Priest', 'Ranged');
insert into player values ('Gainsborough', 'Warlock', 'Ranged');
insert into player values ('Serthii', 'Warlock', 'Ranged');
insert into player values ('Infuszes', 'Mage', 'Ranged');
insert into player values ('Mattis', 'Druid', 'Ranged,Tank');
insert into player values ('Zikura', 'Druid', 'Ranged,Healer,Tank');
insert into player values ('Jorgypewpew', 'Hunter', 'Ranged');

create table raid (
  start varchar2 primary key
);

create table signup (
  raid varchar2,
  time varchar2,
  player varchar2,
  type varchar2,
  comment varchar2,
  primary key (raid,player),
  foreign key (raid) references raid(start),
  foreign key (player) references player(name)
);

create table encounter (
  raid varchar2,
  boss varchar2,
  foreign key (raid) references raid(start)
);

create table encounter_player(
  raid varchar2,
  boss varchar2,
  player varchar2,
  role varhchar2,
  primary key (raid, boss, player),
  foreign key (raid) references encounter(raid),
  foreign key (boss) references encounter(boss),
  foreign key (player) references player(name)
);