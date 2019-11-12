create table WaehDef(
  Waeh varchar2(5) not null,
  kTxtDe nvarchar2(50),
  kTxtEn nvarchar2(50),
  kTxtFr nvarchar2(50),
  TxtDe nvarchar2(50),
  TxtEn nvarchar2(50),
  TxtFr nvarchar2(50)
);
create unique index WaehDef_pk_prim on WaehDef(Waeh);

create table Kurse(
  Waeh varchar2(5) not null,
  Datum date not null,
  Kurs float not null
);
create unique index Kurse_pk_prim on Kurse(Waeh,Datum);

create table HistKurse(
  Waeh varchar2(5) not null,
  Von date not null,
  Bis date not null,
  Kurs float not null
);
create unique index HistKurse_pk_prim on HistKurse(Waeh,Von);
