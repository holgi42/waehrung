import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

public class Kurse {
	static Document doc;
	static Element xRoot,dSet;
	static Namespace ns2;
	static SqConn co=null;
	public static void holeKurse() {
		try {
			BufferedInputStream ein = new BufferedInputStream(new URL(Waehrung.cnf.datUrl).openStream());
			FileOutputStream aus = new FileOutputStream(Waehrung.cnf.datAblage);
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ( (bytesRead = ein.read(dataBuffer, 0, 1024)) != -1) {
			       aus.write(dataBuffer, 0, bytesRead);
			}
			ein.close();
			aus.close();
		} catch (IOException e) {
			Waehrung.prex("Einlesen der Datei \""+Waehrung.cnf.datUrl+"\" nach \""+Waehrung.cnf.datAblage+"\" fehlgeschlagen",e,-2);
		}
	}
	
	public static void openXmlDatei() {
		try {
			ns2=Namespace.getNamespace("http://www.ecb.europa.eu/vocabulary/stats/exr/1");
			doc=new SAXBuilder().build(Waehrung.cnf.datAblage);
			xRoot=doc.getRootElement();
			if (xRoot==null) Waehrung.prex("Sehr seltsam, Xml-Datei enthält keinen Root",-4);
			if (!xRoot.getName().equals("CompactData")) Waehrung.prex("Root-Element heißt nicht CompactData",-4);
//			zeigeEbene(xRoot);
			dSet=xRoot.getChild("DataSet",ns2);
			if (dSet==null) Waehrung.prex("Konnte DataSet nicht finden",-4);
		} catch(Exception e) {Waehrung.prex("Öffnen der Xml-Datei fehlgeschlagen",e,-4);}
	}
	
	public static void zeigeEbene(Element x) {
		List<Element> l=x.getChildren();
		System.out.println("Liste aller Tags auf dieser Ebene:"+x.getName()+", Namespace:"+x.getNamespaceURI());
		for (Element k:l) System.out.println(k.toString());
		System.exit(0);
	}
	public static void zeigeAtts(Element x) {
		List<Attribute> l=x.getAttributes();
		System.out.println("Liste aller Attribute für "+x.getName()+", Namespace:"+x.getNamespaceURI());
		for (Attribute k:l) System.out.println(k.toString());
		System.exit(0);
	}
	
	public static void zeigeWaeh() {
		openXmlDatei();
		System.out.println("In Datei enthaltene Währungen\n-------------------------------------");
		List <Element> ls;
		//zeigeEbene(dSet);
		ls=dSet.getChildren("Group",ns2);
		for (Element k:ls) {
			//zeigeAtts(k);
			String cu=k.getAttributeValue("CURRENCY")+" - "+k.getAttributeValue("TITLE_COMPL");
			System.out.println(cu);
		}
	}
	
	public static void einlesen() throws SQLException {
		openXmlDatei();
		List <Element> ls;
		ls=dSet.getChildren("Series",ns2);
		for (Element k:ls) {
			String cu=k.getAttributeValue("CURRENCY");
			if (cu==null) Waehrung.prex("Konnte CURRENCY nicht in Series finden?",-4);
			WaehDef w=Waehrung.cnf.getWaeh(cu);
			System.out.print("Währung \""+cu+"\" ");
			if (w!=null) {
				openDb();
				co.con.setAutoCommit(false);
				w.regelDb(k);
				co.con.setAutoCommit(true);
				System.out.println("eingelesen");
			} else System.out.println("nicht gewünscht.");
		}
	}
	
	public static void openDb(){
		co=new SqConn(null);
		co.open(Waehrung.cnf.dbConn);
	}
	
}
