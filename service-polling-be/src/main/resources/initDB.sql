create table service_info
(
    id     int auto_increment primary key,
    name   tinytext null,
    url    tinytext not null,
    status tinytext null
);

insert into service_info(name, url) values ('0', 'http://localhost:3000');
insert into service_info(name, url) values ('1', 'http://localhost:3001');
insert into service_info(name, url) values ('2', 'http://localhost:3002');
insert into service_info(name, url) values ('3', 'http://localhost:3003');
insert into service_info(name, url) values ('4', 'http://localhost:3004');
insert into service_info(name, url) values ('5', 'http://localhost:3005');
insert into service_info(name, url) values ('6', 'http://localhost:3006');
insert into service_info(name, url) values ('7', 'http://localhost:3007');
insert into service_info(name, url) values ('8', 'http://localhost:3008');
insert into service_info(name, url) values ('9', 'http://localhost:3009');