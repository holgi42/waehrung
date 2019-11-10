

/**
* Einfache Hilfsklasse, um die Kommandozeile etwas besser zu verarbeiten
*/
public class CmdLine{
  String[] cm;

/**
* Der Konstruktor wird mit den Argumenten von main aufgerufen
* @param acm Das Stringarray aus main
*/
  public CmdLine(String[] acm){
    cm=acm.clone();
  }

  protected int iSuch(String such){
    int i;
    for (i=0;i<cm.length;i++)
      if (such.equals(cm[i])) return i;
    return -1;
  }

/**
* Ist ein Schlüssel in der Kommandozeile vorhanden ?
* @param such Der zu suchende Wert. Casesensitive
* @return true, wenn gefunden, false wenn nicht gefunden
*/
  public boolean Hat(String such){
    if (iSuch(such)>-1) return true;
    return false;
  }

/**
* Sucht nach einem Schlüssel und gibt den folgenden Parameter als int zurück
* @param such Der Schlüsseleintrag
* @param o Der Wert, falls der Eintrag nicht gefunden wurde. Ein Ersatz für den Call-by-Reference
* @return Der int-Wert, der dem Schlüssel in der Cmdline folgt oder der Wert von o
*/
  public int geti(String such,int o) throws NumberFormatException{
    int erg=o;
    int i=iSuch(such);
    try{
      if (i!=-1) erg=Integer.parseInt(cm[i+1]);
    } catch(NumberFormatException e){}
    return erg;
  }

/**
* Analog zu geti allerdings nun mit String.
* @param such Der Schlüsseleintrag
* @param o Der Wert, falls der Eintrag nicht gefunden wurde. Ein Ersatz für den Call-by-Reference
* @return Der String-Wert, der dem Schlüssel in der Cmdline folgt oder der Wert von o
*/
  public String getS(String such,String o){
    int i=iSuch(such);
    if (i==-1) return o;
    return cm[i+1];
  }

  /**
  * Liest den Parameter an der Position.
  * @param pos Die Position des Parameters; 0-basiert
  * @return Der String-Wert, der dem Parameter entspricht
  */
  public String getS(int pos) {
	  String erg=null;
	  if ((pos>=0)&&(pos<cm.length)) erg=cm[pos];
	  return erg;
  }

  /**
  * Gibt den positional bestimmten Parameter als int zurück
  * @param pos Die Position des Parameters; 0-basiert
  * @return Der int-Wert, der dem Parameter entspricht
  */
    public Integer getI(int pos) throws NumberFormatException{
      Integer erg=null;
      String s=getS(pos);
      try{
        if (s!=null) erg=Integer.parseInt(s);
      } catch(NumberFormatException e){}
      return erg;
    }
    
    public Integer getI(String such) {
    	int i=iSuch(such);
    	if (i==-1) return null;
    	return getI(i+1);
    }
}
