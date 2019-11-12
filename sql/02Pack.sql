create or replace package Waehrung as

procedure BaueHist;

end;
/

create or replace package body Waehrung as

procedure BaueHist is
  cursor c1 is
    select Waeh
      from WaehDef
     order by 1;
  cursor c2(aWaeh in varchar2) is
  select *
    from Kurse
   where Waeh=aWaeh
   order by Datum;
   r HistKurse%rowtype;
  begin
    for a in c1 loop
      delete from HistKurse where Waeh=a.Waeh;
      r.Waeh:=a.Waeh;
      r.Von:=null;
      for b in c2(a.Waeh) loop
        if r.Von is not null then
          r.Bis:=trunc(b.Datum)-1/86400;
          insert into HistKurse values r;
        end if;
        r.Von:=b.Datum; r.Kurs:=b.Kurs;
      end loop;
      r.Bis:=to_date('31.12.2069','DD.MM.YYYY');
      insert into HistKurse values r;
    end loop;
    commit;
  end BaueHist;

end;
/

show err
