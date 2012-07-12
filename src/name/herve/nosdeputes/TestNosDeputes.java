package name.herve.nosdeputes;

import java.sql.SQLException;

import name.herve.nosdeputes.parser.Amendements;
import name.herve.nosdeputes.util.MySQLFrontend;

public class TestNosDeputes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MySQLFrontend front = new MySQLFrontend();
			front.setDisplayEnabled(false);
			front.connect();
			
			Amendements a = new Amendements();
			a.setFront(front);
			
			// a.amendementsTotal();
			// a.amendementsAdoptes();
			
			a.avisAmendementsGrenelleIIAdoptes();
			
			front.disconnect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
