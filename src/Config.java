import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Element;

class WaehDef{
	String name,kDe,kEn,kFr,lDe,lEn,lFr;
	WaehDef(Element x){
		name=x.getAttributeValue("Kurz");
		List<Element> lk=x.getChildren("Ktxt");
		for (Element k:lk) {
			String sp=k.getAttributeValue("lg");
			if (sp==null) Config.xerr("Kurztext-Daten müssen ein lg-Tag für die Sprache haben");
			switch(sp) {
			case "de": kDe=k.getValue(); break;
			case "en": kEn=k.getValue(); break;
			case "fr": kFr=k.getValue(); break;
			default: Config.xerr("Als Sprache (lg) sind nur \"de\",\"en\" und \"fr\" zugelassen");
			}
		}
		lk=x.getChildren("Txt");
		for (Element k:lk) {
			String sp=k.getAttributeValue("lg");
			if (sp==null) Config.xerr("Text-Daten müssen ein lg-Tag für die Sprache haben");
			switch(sp) {
			case "de": lDe=k.getValue(); break;
			case "en": lEn=k.getValue(); break;
			case "fr": lFr=k.getValue(); break;
			default: Config.xerr("Als Sprache (lg) sind nur \"de\",\"en\" und \"fr\" zugelassen");
			}
		}
	}
	public void zeige() {
		System.out.println(String.format("  %-5s %-20s %s", name,kDe,lDe));
	}
}

public class Config {
	String confDatei,dbConn,halterSchema,mbNummer,mbAbsender,datUrl,datAblage,datAbWann;
	boolean dbOben,useMb;
	ArrayList<WaehDef> wd;
	public Config(CmdLine cmd) {
		try {
			confDatei="/home/willms/git/waehrung/config/Config.xml";
			confDatei=cmd.getS("-conf",confDatei);
			Document doc=null;
            doc=new SAXBuilder().build(confDatei);
            Element x=doc.getRootElement();
            if (x==null) xerr("Xml-File \""+confDatei+"\" opened but no root element found. This is strange!");
            if(!x.getName().equals("Config")) xerr("\""+confDatei+"\" is no config file.");
            
            Element d=x.getChild("Datenbank");
            if (d==null) xerr("Sektion Datenbank ist nicht vorhanden.");
            dbConn=env("WaehrungDatenbank",d.getChildText("Connect"));
            halterSchema=d.getChildText("Schema");
            dbOben=true;
            if (d.getChild("NoConnect")!=null) dbOben=false;
            System.out.println("dbConn:"+dbConn+", halter:"+halterSchema);
            
            d=x.getChild("Mailbox");
            if (d==null) xerr("Sektion Mailbox ist nicht vorhanden.");
            if (d.getChild("BenutzeBox")==null) useMb=false; else useMb=true;
            mbNummer=d.getChildText("MbNummer");
            mbAbsender=d.getChildText("MbAbsender");
            if (useMb) {
            	if (mbNummer==null) xerr("Mailbox soll genutzt werden; dann bitte MbNummer festelegen.");
            	if (mbAbsender==null) xerr("Mailbox soll genutzt werden; dann bitte MbAbsender festelegen.");
            }
            
            d=x.getChild("Datei");
            if (d==null) xerr("Sektion Datei ist nicht vorhanden.");
            datUrl=d.getChildText("Url");
            datAblage=d.getChildText("Ablage");
            datAbWann=d.getChildText("LeseAb");
            if ((datUrl==null)) xerr("Die Url in Sektion Datei fehlt.");
            if ((datAblage==null)) xerr("Die Ablage in Sektion Datei fehlt.");
            if ((datAbWann==null)) xerr("LeseAb in Sektion Datei fehlt.");
            
            List<Element> lw;
            wd=new ArrayList<>();
            lw=x.getChildren("Waehrung");
            for (Element k:lw) wd.add(new WaehDef(k));
            
		} catch(Exception e) { Waehrung.prex("Configuration fehlgeschlagen", e,-2);}
	}
	
	public static void xerr(String m) {
		System.out.println("Fehler in der Xml-Configuration:\n"+m);
		System.exit(-4);
	}
	
	public static void pr(String s) {
		System.out.print(s);
	}
	
	public void zeige() {
		pr("Configuration Währungssystem\n============================\n");
		pr("Konfigurationsdatei:"+confDatei+"\n\n");
		pr("Datenbankverbindung: "+dbConn+"\n");
		pr("       Halterschema: "+halterSchema+"\n\n");
		pr("    Benutze Mailbox: ");
		if (useMb) pr("JA\n"); else pr("Nein\n");
		pr("     Mailbox Nummer: "+mbNummer+"\n");
		pr("   Mailbox Absender: "+mbAbsender+"\n\n");
		pr("           Lade Url: "+datUrl+"\n");
		pr("             Ablage: "+datAblage+"\n");
		pr("          Startzeit: "+datAbWann+"\n\n");
		for (WaehDef k:wd) {
			k.zeige();
		}
	}
	
	protected String env(String was,String ersatz) {
		String h=System.getenv(was);
		if (h!=null) return h;
		return ersatz;
	}

}
