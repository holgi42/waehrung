import java.sql.SQLException;

public class Waehrung {
	static Config cnf; 
	public static void main(String[] args) {
		CmdLine cmd=new CmdLine(args);
		cnf=new Config(cmd);
		String bef=cmd.getS(0);
		if (bef==null) prex("Kein Befehl angegeben.",-1);
		try {
			switch(bef) {
			case "showconf": cnf.zeige(); break;
			case "holexml": Kurse.holeKurse(); break;
			case "showwaeh": Kurse.zeigeWaeh(); break;
			case "einlesen": Kurse.einlesen(); break;
			case "resync":
				Kurse.openDb();
				cnf.resync();
				break;
			default: prex("Unbekannter Befehl:"+bef,-1);
			}			
		} catch(SQLException e) {prex("SQL-Exception",e,-9);}
	}
	
	public static void prex(String m,Exception e,int status) {
		System.out.println("\n"+m);
		e.printStackTrace();
		System.exit(status);
	}
	public static void prex(String m,Exception e) {
		prex(m,e,-4);
	}

	public static void prex(String m,int status) {
		System.out.println("\n"+m);
		System.exit(status);
	}

	public static void prex(String m) {
		prex(m,-4);
	}
	
}
