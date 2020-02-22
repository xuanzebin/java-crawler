create table LINKS_TO_BE_PROCESSED(link varchar(2000));

create table LINKS_ALREADY_PROCESSED(link varchar(2000));

create table NEWS(
id bigint primary key auto_increment,
link varchar(2000),
title text,
content text,
created_at timestamp default now(),
update_at timestamp default now()
);