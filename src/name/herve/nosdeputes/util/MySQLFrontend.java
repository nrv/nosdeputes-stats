package name.herve.nosdeputes.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MySQLFrontend {
	private final static String MAIN_KEY = "main";
	private final static String PERS_KEY = "pers";
	private final static String PARL_KEY = "parl";

	private Configuration conf;
	private Connection conn;
	private boolean displayEnabled;
	private int displayEvery;
	private Map<String, PreparedStatement> pstmt;
	private Map<String, ResultSet> res;
	private Map<String, Integer> row;
	
	private Map<Integer, String> cachePersonnalite;
	private Map<Integer, String> cacheParlementaire;

	public MySQLFrontend() throws SQLException {
		super();
		setDisplayEnabled(true);
		setDisplayEvery(1000);
		conf = new Configuration();
		try {
			conf.init();
		} catch (IOException e) {
			throw new SQLException(e);
		}
		
		cachePersonnalite = new HashMap<Integer, String>();
		cacheParlementaire = new HashMap<Integer, String>();
	}

	public String getPersonnalite(int id) throws SQLException {
		ResultSet r = null;
		try {
			if (cachePersonnalite.containsKey(id)) {
				return cachePersonnalite.get(id);
			}
			
			if (!pstmt.containsKey(PERS_KEY)) {
				pstmt.put(PERS_KEY, conn.prepareStatement("select nom from personnalite where id=?"));
			}
			
			PreparedStatement s = pstmt.get(PERS_KEY);
			s.setInt(1, id);
			r = s.executeQuery();
			String nom = "inconnu";
			if (r.next()) {
				nom = r.getString(1);
			}
			cachePersonnalite.put(id, nom);
			return nom;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (r != null) {
				r.close();
			}
		}
	}
	
	public String getParlementaire(int id) throws SQLException {
		ResultSet r = null;
		try {
			if (cacheParlementaire.containsKey(id)) {
				return cacheParlementaire.get(id);
			}
			
			if (!pstmt.containsKey(PARL_KEY)) {
				pstmt.put(PARL_KEY, conn.prepareStatement("select nom from parlementaire where id=?"));
			}
			
			PreparedStatement s = pstmt.get(PARL_KEY);
			s.setInt(1, id);
			r = s.executeQuery();
			String nom = "inconnu";
			if (r.next()) {
				nom = r.getString(1);
			}
			cacheParlementaire.put(id, nom);
			return nom;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (r != null) {
				r.close();
			}
		}
	}
	
	public void connect() throws SQLException {
		try {
			if (conn != null) {
				disconnect();
			}
			Class.forName(conf.getMysqlDriver());
			conn = DriverManager.getConnection(conf.getMysqlURL(), conf.getMysqlUser(), conf.getMysqlPassword());
			pstmt = new HashMap<String, PreparedStatement>();
			res = new HashMap<String, ResultSet>();
			row = new HashMap<String, Integer>();
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	}

	public void disconnect() throws SQLException {
		if (conn != null) {
			conn.close();
			conn = null;
		}
	}

	public void endQuery() {
		endQuery(MAIN_KEY);
	}
	
	public void endQuery(String key) {
		if (res.containsKey(key)) {
			try {
				res.get(key).close();
			} catch (SQLException e) {
				// ignored
			}
			res.remove(key);
		}

		if (pstmt.containsKey(key)) {
			try {
				pstmt.get(key).close();
			} catch (SQLException e) {
				// ignored
			}
			pstmt.remove(key);
		}
		
		if (row.containsKey(key)) {
			row.remove(key);
		}
	}

	public Date getDate(String key, int columnIndex) throws SQLException {
		return res.get(key).getDate(columnIndex);
	}

	public Date getDate(String columnLabel) throws SQLException {
		return getDate(MAIN_KEY, columnLabel);
	}
	
	public Date getDate(String key, String columnLabel) throws SQLException {
		return res.get(key).getDate(columnLabel);
	}

	public int getDisplayEvery() {
		return displayEvery;
	}

	public int getInt(String key, String columnLabel) throws SQLException {
		return res.get(key).getInt(columnLabel);
	}
	
	public int getInt(String columnLabel) throws SQLException {
		return getInt(MAIN_KEY, columnLabel);
	}
	
	public String getString(String key, int columnIndex) throws SQLException {
		return res.get(key).getString(columnIndex);
	}

	public String getString(String columnLabel) throws SQLException {
		return getString(MAIN_KEY, columnLabel);
	}
	
	public String getString(String key, String columnLabel) throws SQLException {
		return res.get(key).getString(columnLabel);
	}

	public Timestamp getTimestamp(String key, String columnLabel) throws SQLException {
		return res.get(key).getTimestamp(columnLabel);
	}
	
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(MAIN_KEY, columnLabel);
	}
	
	public boolean isConnected() {
		return conn != null;
	}

	public boolean isDisplayEnabled() {
		return displayEnabled;
	}

	private void log(String msg) {
		if (isDisplayEnabled()) {
			System.out.println("[MySQL] " + msg);
		}
	}
	
	public boolean next() throws SQLException {
		return next(MAIN_KEY);
	}
	
	public boolean next(String key) throws SQLException {
		boolean n = res.get(key).next();
		if (n) {
			row.put(key, row.get(key) + 1);
			if (row.get(key) % displayEvery == 0) {
				log("row " + row.get(key));
			}
		}
		return n;
	}

	public void setDisplayEnabled(boolean displayEnabled) {
		this.displayEnabled = displayEnabled;
	}

	public void setDisplayEvery(int displayEvery) {
		this.displayEvery = displayEvery;
	}

	public void startQuery(String q) throws SQLException {
		startQuery(MAIN_KEY, q);
	}
	
	public void startQuery(String key, String q) throws SQLException {
		endQuery(key);

		try {
			pstmt.put(key, conn.prepareStatement(q));
			res.put(key, pstmt.get(key).executeQuery());
			row.put(key, 0);
		} catch (SQLException e) {
			endQuery(key);
			throw e;
		}
	}
}
