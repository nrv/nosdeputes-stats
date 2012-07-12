package name.herve.nosdeputes.parser;

import java.sql.Date;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import name.herve.nosdeputes.util.MySQLFrontend;

public class Amendements {
	private final static GregorianCalendar D_2007_06_01 = new GregorianCalendar(2007, 6, 1);
	private final static GregorianCalendar D_2007_09_30 = new GregorianCalendar(2007, 9, 30);
	private final static GregorianCalendar D_2008_09_30 = new GregorianCalendar(2008, 9, 30);
	private final static GregorianCalendar D_2009_09_30 = new GregorianCalendar(2009, 9, 30);
	private final static GregorianCalendar D_2010_09_30 = new GregorianCalendar(2010, 9, 30);
	private final static GregorianCalendar D_2011_09_30 = new GregorianCalendar(2011, 9, 30);
	private final static GregorianCalendar D_2012_03_30 = new GregorianCalendar(2012, 3, 30);
	private final static String S_2006_2007 = "20062007";
	private final static String S_2007_2008 = "20072008";
	private final static String S_2008_2009 = "20082009";
	private final static String S_2009_2010 = "20092010";
	private final static String S_2010_2011 = "20102011";
	private final static String S_2011_2012 = "20112012";
	private final static String S_INCONNUE = "inconnue";

	private MySQLFrontend front;

	public Amendements() {
		super();
	}

	public MySQLFrontend getFront() {
		return front;
	}

	public void setFront(MySQLFrontend front) {
		this.front = front;
	}

	public String getSessionParlementaire(Date date) {
		// http://www.assemblee-nationale.fr/histoire/sessions.asp

		if (date == null) {
			return S_INCONNUE;
		}

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);

		if (D_2007_06_01.compareTo(gc) <= 0) {
			if (D_2007_09_30.compareTo(gc) >= 0) {
				return S_2006_2007;
			} else if (D_2008_09_30.compareTo(gc) >= 0) {
				return S_2007_2008;
			} else if (D_2009_09_30.compareTo(gc) >= 0) {
				return S_2008_2009;
			} else if (D_2010_09_30.compareTo(gc) >= 0) {
				return S_2009_2010;
			} else if (D_2011_09_30.compareTo(gc) >= 0) {
				return S_2010_2011;
			} else if (D_2012_03_30.compareTo(gc) >= 0) {
				return S_2011_2012;
			} else {
				return S_INCONNUE;
			}
		} else {
			return S_INCONNUE;
		}
	}

	private void amedementsStats(String q, String t) {
		try {
			if (!front.isConnected()) {
				front.connect();
			}

			Map<String, Integer> stats = new HashMap<String, Integer>();

			front.startQuery(q);
			while (front.next()) {
				int nb = front.getInt("nb_multiples");
				Date date = front.getDate("date");
				String session = getSessionParlementaire(date);
				if (stats.containsKey(session)) {
					stats.put(session, stats.get(session) + nb);
				} else {
					stats.put(session, nb);
				}
			}

			System.out.println(t);
			Set<String> sessions = new TreeSet<String>();
			sessions.addAll(stats.keySet());

			for (String sort : sessions) {
				System.out.println(sort + " : " + stats.get(sort));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			front.endQuery();
		}
	}

	public void amendementsTotal() {
		amedementsStats("select date, nb_multiples from amendement where sort<>'Rectifié'", "Nombre total d'amendements : ");
	}

	public void amendementsAdoptes() {
		amedementsStats("select date, nb_multiples from amendement where sort='Adopté'", "Amendements adoptés : ");
	}

	public void avisAmendementsGrenelleIIAdoptes() {
		String SUB = "SUB_KEY";
		try {
			if (!front.isConnected()) {
				front.connect();
			}

			front.startQuery("select a.id as aid, a.texteloi_id as atlid, a.numero as anum, a.date as adate from amendement a, texteloi tl where a.texteloi_id=tl.id and a.sort='Adopté' and a.texteloi_id='2449'");
			int max = 50;
			while (front.next() && (max > 0)) {
				int amendementId = front.getInt("aid");
				String texteloiId = front.getString("atlid");
				String amendementNumero = front.getString("anum");
				Date amendementDate = front.getDate("adate");

				System.out.println("--------------------------------------------------------------------------------");
				System.out.println("Amendement " + amendementNumero + " (" + amendementId + ") au texte " + texteloiId + " éxaminé le " + amendementDate);

				String sub = "select i.id as iid, i.intervention as interv, i.fonction as fonc, i.personnalite_id as persid, i.parlementaire_id as parlid ";
				sub += "from tagging tg, tag t, intervention i ";
				sub += "where t.triple_namespace='loi' ";
				sub += "and t.triple_key='numero'  ";
				sub += "and t.triple_value='" + texteloiId + "'  ";
				sub += "and tg.taggable_model='Intervention' ";
				sub += "and tg.tag_id=t.id ";
				sub += "and tg.taggable_id in ( ";
				sub += "       select tg2.taggable_id from tagging tg2, tag t2 ";
				sub += "       where t2.triple_namespace='loi' ";
				sub += "       and t2.triple_key='amendement' ";
				sub += "       and t2.triple_value='" + amendementNumero + "' ";
				sub += "       and tg2.taggable_model='Intervention' ";
				sub += "       and tg2.tag_id=t2.id ";
				sub += ") ";
				sub += "and i.id=tg.taggable_id ";
				sub += "order by i.timestamp";

				front.startQuery(SUB, sub);

				while (front.next(SUB)) {
					int interventionId = front.getInt(SUB, "iid");
					int personnaliteId = front.getInt(SUB, "persid");
					int parlementaireId = front.getInt(SUB, "parlid");
					String intervention = front.getString(SUB, "interv");
					String fonction = front.getString(SUB, "fonc");
					
					System.out.print(" - (" + interventionId + ") ");
					if ((fonction != null) && (fonction.length() > 0)) {
						System.out.print("[" + fonction + "] ");
					}
					if (personnaliteId > 0) {
						System.out.print(front.getPersonnalite(personnaliteId) + " ");
					} else if (parlementaireId > 0) {
						System.out.print(front.getParlementaire(parlementaireId) + " ");
					}
					System.out.println(" : " + intervention);
				}

				front.endQuery(SUB);

				max--;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			front.endQuery();
		}
	}
}
