

import java.sql.* ;
import java.text.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

class TooManyRows extends SQLException{
  private static final long serialVersionUID = -5974212752424774901L;
  public TooManyRows(){ super();}
  public TooManyRows(String a){ super(a);}
}

class NoDataFound extends SQLException{
  private static final long serialVersionUID = -1903043275223394230L;
  public NoDataFound(){ super();}
  public NoDataFound(String a){ super(a);}
}

class IllegalNull extends SQLException{
  private static final long serialVersionUID = 2313225366076133142L;
  public IllegalNull(){ super();}
  public IllegalNull(String a){ super(a);}
}

/**
* Ist der Connector für eine verbindung zu einber Odbc-Datenbank. Damit das ganze läuft, muß der Treiber integriert sein. Die einzelnen Befehle
* in dieser Klasse können zu Debugg-Zwecken loggen; im Konstruktor gibt es ein Präfix dafür.
*/
class SqConn{
/** Der Connectionhandle aus java.sql*/
  Connection con;

/** Merkt sich, ob die Verbindung zur Datenbank offen ist*/
  boolean istOffen;

/** Das Präfix im Debugging*/
  String DebName;

/**
* Wenn man nicht über die Bind-Methoden einen Zeitstempel haben will, sollte man dies nutzen. Aus einem Zeitstempel wird ein to_date('wert','format'),
* gerade so, daß es perfekt in oracle paßt.
* @param t Der Zeitstempel, der umgewandelt werden soll
* @return Ein String, der direkt für Abfragen oder Befehle genutzt werden kann
*/
  public static String oraZeit(Timestamp t){
	    SimpleDateFormat df=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	    String erg="to_date('"+df.format(t)+"','dd.mm.yyyy HH24:MI:SS')";
	    return erg;
	  }

  /**
  * Wenn man nicht über die Bind-Methoden einen Zeitstempel haben will, sollte man dies nutzen. Aus einem Zeitstempel wird ein Format,
  * gerade so, daß es perfekt in Mickeydoof-Sql paßt.
  * @param t Der Zeitstempel, der umgewandelt werden soll
  * @return Ein String, der direkt für Abfragen oder Befehle genutzt werden kann
  */
  public static String mickeyZeit(Timestamp t){
	    SimpleDateFormat df=new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
	    String erg="'"+df.format(t)+"'";
	    return erg;
	  }

/**
* Der Konstruktor merkt sich nur den Debugnamen und initialisiert die Klasse
* @param aDebName der name, der als Präfix im Debug gebraucht wird
*/
  SqConn(String aDebName){
    DebName=aDebName;
    istOffen=false;
  }

/**
* Schreibt über den globalen Debugkanal via sqd eine Debugmeldung. Es wird das Präfix mit übergeben
* @param wer Der Ort, indem das Ganze statt findet (Bef, open etc.)
* @param was Ein Text, der erhellend sein soll
*/
  public void deb(String wer,String was){
    //Deb.sqd(DebName,wer,was);
  }

/**
* Öffne die Datenbankverbindung
* @param jdcon Der Jdcb-Conectionstring
* @param un der Benutzername
* @param pw das Kennwort
* @return true, wenn wir Erfolg haten, false, wenn nicht
*/
  public boolean open(String jdcon,String un,String pw){
    try{
      //System.out.println("Conn:"+jdcon+"\nUn:"+un+"\npw:"+pw+".");
      con=DriverManager.getConnection(jdcon,un,pw);
      istOffen=true;
    }
    catch (SQLException e){ e.printStackTrace(); System.exit(2); }
    return true;
  }
  public boolean open(String jdcon){
    try{
      //System.out.println("Conn:"+jdcon+".");
      con=DriverManager.getConnection(jdcon);
      istOffen=true;
    }
    catch (SQLException e){ e.printStackTrace(); System.exit(2); }
    return true;
  }

/**
* Schließe eine geöffnete Datenbankverbindung. War sie schon geschlossen, passiert gar nichts.
*/
  public void close() throws SQLException{
    if (istOffen) con.close();
    istOffen=false;
    deb("Datenbank geschlossen","");
  }

/**
* Übertrage einen direkten Befehl. Geht etwas schief, wird dies protokolliert
* @param b der Sql-Befehl
* @return Wievel Sätze wurden geupdated
*/
  public int Bef(String b) throws SQLException{
    int anz;
    Statement stmt;
    try{
      stmt=con.createStatement();
      anz=stmt.executeUpdate(b);
      stmt.close();
      deb("Befehl",b);
    }
    catch (SQLException e){
      String s;
      s=b+"\nMessage:"+e.getMessage();
      deb("Befehl Exception",s);
      throw e;
    }
    return anz;
  }

  protected void chksqm(int sqm,String q) throws SQLException{
    switch(sqm){
      case 1: throw new IllegalNull(q);
      case 2: throw new TooManyRows(q);
      case 3: throw new NoDataFound(q);
    }
  }

/** führe Abfrage aus und hole Ergebnis als int.
* @param q Die Abfrage
* @return Das Ergebnis. Ist der Wert Null in der Datenbank, gibts eine IllegalNull-Exception
*/
  public int geti(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    Integer erg=0;
    int sqm=0;
    stmt=con.createStatement();
    rs=stmt.executeQuery(q);
    if (rs.next()){
      erg=rs.getInt(1);
      if (rs.wasNull()) sqm=1;
      else if (rs.next()) sqm=2;
    } else sqm=3;
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

/** führe Abfrage aus und hole Ergebnis als boolean.
* @param q Die Abfrage
* @return Das Ergebnis. Ist der Wert Null in der Datenbank, gibts eine IllegalNull-Exception
*/
  public boolean getb(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    boolean erg=false;
    int sqm=0;
    stmt=con.createStatement();
    rs=stmt.executeQuery(q);
    if (rs.next()){
      erg=rs.getBoolean(1);
      if (rs.wasNull()) sqm=1;
      else if (rs.next()) sqm=2;;
    } else sqm=3;
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

/** führe Abfrage aus und hole Ergebnis als Integer.
* @param q Die Abfrage
* @return Das Ergebnis. Ist der Wert Null in der Datenbank wird eine null zurück gegeben
*/
  public Integer getI(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    Integer erg=null;
    int sqm=0;
    stmt=con.createStatement();
    rs=stmt.executeQuery(q);
    if (rs.next()){
      erg=rs.getInt(1);
      if (rs.wasNull()) erg=null;
      if (rs.next()) sqm=2;
    } else sqm=3;
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

/** führe Abfrage aus und hole Ergebnis als String.
* @param q Die Abfrage
* @return Das Ergebnis. Ist der Wert Null in der Datenbank wird eine null zurück gegeben
*/
  public String getS(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    String erg=null;
    int sqm=0;

    try{
      stmt=con.createStatement();
      rs=stmt.executeQuery(q);
    } catch (SQLException e){ throw e;}

    if (rs.next()){
      erg=rs.getString(1);
      if (rs.wasNull()) erg="";
      if (rs.next()) sqm=2;
    } else sqm=3;
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

  public String getAscii(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    String erg=new String("");
    String rein;
    int sqm=0;
    BufferedReader reader;
    InputStream ins;

    try{
      stmt=con.createStatement();
      rs=stmt.executeQuery(q);
    } catch (SQLException e){
      throw e;
    }

    try{
      if (rs.next()){
        ins=rs.getAsciiStream(1);
        if (ins!=null){
          if (!rs.wasNull()){
            reader=new BufferedReader(new InputStreamReader(ins));
            while ((rein=reader.readLine())!=null) erg=erg+rein;
          }
        }
        if (rs.next()) sqm=2;
      } else sqm=3;
    } catch (Exception e){ e.printStackTrace(); System.exit(9);}
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

/** führe Abfrage aus und hole Ergebnis als Timestamp.
* @param q Die Abfrage
* @return Das Ergebnis. Ist der Wert Null in der Datenbank wird eine null zurück gegeben
*/
  public Timestamp getTs(String q) throws SQLException{
    Statement stmt;
    ResultSet rs;
    Timestamp erg=null;
    int sqm=0;
    stmt=con.createStatement();
    rs=stmt.executeQuery(q);
    if (rs.next()){
      erg=rs.getTimestamp(1);
      if (rs.wasNull()) erg=null;
      if (rs.next()) sqm=2;
    } else sqm=3;
    rs.close(); stmt.close();
    chksqm(sqm,q);
    return erg;
  }

}

/**
* Lese Daten von einer Datenbank als Cursor
*/
class SqCursor{
  protected SqConn con;
  protected boolean istOffen;
  protected Statement stmt;
  protected ResultSet rs;
  protected String frage;

/**
* Initialisiere den Cursor, indem er mit einer (offenen) Datenbankverbindung verbunden wird.
* @param aCon Die Verbindung
*/
  public SqCursor(SqConn aCon){
    con=aCon; istOffen=false;
  }

/**
* Destruktor, zur Sicherheit
*/

  protected void finalize(){
    if (istOffen){
      try{
        rs.close(); stmt.close();
      }
      catch(SQLException e){ e.printStackTrace();}
      istOffen=false;
      deb("Cursor finalize",frage);
    }
  }

  private void deb(String wer,String was){ con.deb(wer,was); }

/**
* Öffne den Cursor und führe die Abfrage aus
* @param q Die Abfrage
*/
  public void open(String q) throws SQLException{
    close();
    frage=q;
    deb("Cursor open",frage);
    try{
      stmt=con.con.createStatement();
      rs=stmt.executeQuery(q);
      istOffen=true;
    } catch (SQLException e){
      String s;
      s=q+"\nMessage:"+e.getMessage();
      deb("Cursor open Exception",s);
      throw e;
    }
  }

/**
* Schließt den Cursor
*/
  public void close(){
    if (istOffen){
      try{
        rs.close(); stmt.close();
      }
      catch(SQLException e){ e.printStackTrace();}
      istOffen=false;
      deb("Cursor close",frage);
    }
  }

/**
* Holt den nächsten Datensatz
* @return Gibt true zurück, wenn etwas gelesen wurde, false, wenn nichts mehr zum Lesen da ist
*/
  public boolean next() throws SQLException{
    boolean b=rs.next();
//    if (b) deb("Cursor next ist wahr",frage); else deb("Cursor next ist falsch",frage);
    return b;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte. Wenn der Wert in der Db Null ist, gibt's die IllegalNull-Exception
* @return Der Wert als int
*/
  public int geti(String p) throws SQLException{
    Integer erg;
    erg=rs.getInt(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",geti("+p+")");
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte. Wenn der Wert in der Db Null ist, gibt's die IllegalNull-Exception
* @return Der Wert als boolean
*/
  public boolean getb(String p) throws SQLException{
    boolean erg;
    erg=rs.getBoolean(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",getb("+p+")");
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte.
* @return Der Wert als Integer
*/
  public Integer getI(String p) throws SQLException{
    Integer erg;
    erg=rs.getInt(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte.
* @return Der Wert als String
*/
  public String getS(String p) throws SQLException{
    String erg;
    erg=rs.getString(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte.
* @return Der Wert als Timestamp
*/
  public Timestamp getTs(String p) throws SQLException{
    Timestamp erg;
    erg=rs.getTimestamp(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte. Wenn der Wert in der Db Null ist, gibt's die IllegalNull-Exception
* @return Der Wert als float
*/
  public float getf(String p) throws SQLException{
    Float f;
    f=rs.getFloat(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",getf("+p+")");
    return f;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Der Name der Spalte.
* @return Der Wert als Float
*/
  public Float getF(String p) throws SQLException{
    Float erg;
    erg=rs.getFloat(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert. Wenn der Wert in der Db Null ist, gibt's die IllegalNull-Exception
* @return Der Wert als int
*/
  public int geti(int p) throws SQLException{
    Integer erg;
    erg=rs.getInt(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",geti("+p+")");
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert. Wenn der Wert in der Db Null ist, gibt's die IllegalNull-Exception.
* @return Der Wert als boolean
*/
  public boolean getb(int p) throws SQLException{
    boolean erg;
    erg=rs.getBoolean(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",getb("+p+")");
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert.
* @return Der Wert als Integer
*/
  public Integer getI(int p) throws SQLException{
    Integer erg;
    erg=rs.getInt(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert.
* @return Der Wert als String
*/
  public String getS(int p) throws SQLException{
    String erg;
    erg=rs.getString(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert.
* @return Der Wert als Timestamp
*/
  public Timestamp getTs(int p) throws SQLException{
    Timestamp erg;
    erg=rs.getTimestamp(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert. Auch hier kann es eine IllegalNull geben
* @return Der Wert als float.
*/
  public float getf(int p) throws SQLException{
    float f;
    f=rs.getFloat(p);
    if (rs.wasNull()) throw new IllegalNull("Cursor:"+frage+",getf("+p+")");
    return f;
  }

/**
* Holt einen Wert aus dem aktuellen Ergebnistupel
* @param p Die wievielte Spalte wird gefragt. 1-basiert.
* @return Der Wert als Float
*/
  public Float getF(int p) throws SQLException{
    Float erg;
    erg=rs.getFloat(p);
    if (rs.wasNull()) erg=null;
    return erg;
  }

}

/**
* Erzeugt befehle, um die Datenbank mit gebundenen Statements zu verarbeiten
* Zuerst wird der Sql-Befehlstext geschickt, zur Not gestückelt durch addText, wobei die Parameter durch ? gekennzeichnet sind,
* danach wird mit prepare das Statement zur Datenbank geschickt. Nun kann man den Befehlstext nicht mehr ändern.
* Schließlich die Parameter mnit den add()-Befehlen und /oder den set/(-Befehlen binden und dann mit exec ausführen.
*/
class BindBef{
  private SqConn co;
  private PreparedStatement stmt;
  private String Txt;
  private int Posi;

/**
* Initialisiere Klasse und merke den Connector
* @param aco Der Connector zur Datenbank
*/
  BindBef(SqConn aco){ co=aco; Posi=0; }

/**
* Initialisiere Klasse, merke den Connector und setze direkt den Text fest. Es wird ein prepare geamcht, sodaß wir direkt mit add/set binden
* @param aco Der Connector zur Datenbank
* @param aTxt Der Sql-Text
*/
  BindBef(SqConn aco,String aTxt) throws SQLException{
    co=aco; Txt=aTxt;
    this.prepare();
  }

  private void deb(String wer,String was){ co.deb(wer,was); }

/**
* Wenn der einparametrige Konstruktor genommen wurde, wird hiermit der Sql-Text gesetzt
* @param aTxt der Text, der zum Sql-Text hinzu gefügt wird
*/
  void addText(String aTxt){ Txt=Txt+aTxt; }

/**
* Schickt den Text zur Datenbank und läßt ihn dort parsen. Zugleich wird ein Positionszähler auf den ersten Parameter gesetzt.
*/
  void prepare() throws SQLException{
    deb("Bindbef prepare",Txt);
    try{
      stmt=co.con.prepareStatement(Txt);
      Posi=1;
    }
    catch(SQLException e){
      String s;
      s=Txt+"\nMessage:"+e.getMessage();
      deb("Befehl Exception",s);
      throw(e);
    }
  }

/**
* Setzt den aktuellen, durch den internen Positionszeiger bestimmten Bind-Parameter auf den Wert w
* @param w der zu setzende Wert
*/
  void add(int w) throws SQLException{
//    deb("intBind","Posi:"+Posi+", Wert:"+w);
    stmt.setInt(Posi++,w);
  }

/**
* Setzt den aktuellen, durch den internen Positionszeiger bestimmten Bind-Parameter auf den Wert w
* @param w der zu setzende Wert
*/
  void add(String w) throws SQLException{
//    deb("StringBind","Posi:"+Posi+", Wert:"+w);
    if (w==null) stmt.setNull(Posi,java.sql.Types.VARCHAR);
    else stmt.setString(Posi,w);
    Posi++;
  }

/**
* Setzt den aktuellen, durch den internen Positionszeiger bestimmten Bind-Parameter auf den Wert w
* @param w der zu setzende Wert
*/
  void add(Integer w) throws SQLException{
//    deb("IntegerBind","Posi:"+Posi+", Wert:"+w);
    if (w==null) stmt.setNull(Posi,java.sql.Types.INTEGER);
    else stmt.setInt(Posi,w);
    Posi++;
  }

/**
* Setzt den aktuellen, durch den internen Positionszeiger bestimmten Bind-Parameter auf den Wert w
* @param w der zu setzende Wert
*/
  void add(Timestamp w) throws SQLException{
//    deb("TimestampBind","Posi:"+Posi+", Wert:"+w);
    if (w==null) stmt.setNull(Posi,java.sql.Types.TIMESTAMP);
    else stmt.setTimestamp(Posi,w);
    Posi++;
  }

/**
* Setzt den durch p bestimmten bestimmten Bind-Parameter auf den Wert w.
* @param p der (1-basierte) Positionswert
* @param w der zu setzende Wert
*/
  void set(int p,int w) throws SQLException{ Posi=p; add(w); }

  /**
* Setzt den durch p bestimmten bestimmten Bind-Parameter auf den Wert w.
* @param p der (1-basierte) Positionswert
* @param w der zu setzende Wert
*/
  void set(int p,String w) throws SQLException{ Posi=p; add(w); }

/**
* Setzt den durch p bestimmten bestimmten Bind-Parameter auf den Wert w.
* @param p der (1-basierte) Positionswert
* @param w der zu setzende Wert
*/
  void set(int p,Timestamp w) throws SQLException{ Posi=p; add(w); }

/**
* Führe den Befehl aus
*/
  void exec(){
//    deb("BindBef exec","");
    try{ stmt.executeUpdate();}
    catch(SQLException e){
//      idl.pr("BindBef Exception:"+e.getMessage());
    }
    Posi=1;
  }

}
