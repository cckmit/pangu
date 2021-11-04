drop table if exists MultiTypeTable;
create table MultiTypeTable
(
    iInt             int(11)      not null primary key ,
    bBit             bit(1)       NOT NULL,
    vVarchar         varchar(100) not null,
    `dDateTime`      datetime     NOT NULL,
    `bBigint`        bigint(20)   NOT NULL,
    `lLongText`      longtext,
    `lLongblob` longblob

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO `MultiTypeTable` (iInt, bBit,vVarchar,dDateTime,bBigint,lLongText,lLongblob)
VALUES
(1,b'0','v-char','2021-11-03 10:01:01',1001,'long-text',X'45');
