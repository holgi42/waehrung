create or replace view AktKurse as
select Waeh,Kurs from Kurse
 where (Waeh,Datum) in(select Waeh,max(datum) from Kurse group by Waeh)
/
