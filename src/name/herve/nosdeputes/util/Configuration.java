package name.herve.nosdeputes.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
	private final static String PROPS_FILE = "nosdeputes.properties";
	private Properties properties;
	
	public Configuration() {
		super();
	}
	
	public void init() throws IOException {
		properties = new Properties();
		InputStream is = ClassLoader.getSystemResourceAsStream(PROPS_FILE);
		if (is == null) {
			throw new IOException("Unable to find " + PROPS_FILE);
		}
		properties.load(is);
		is.close();
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public String getMysqlDriver() {
		return getProperty("MYSQL_DRIVER");
	}
	
	public String getMysqlURL() {
		return getProperty("MYSQL_URL");
	}
	
	public String getMysqlUser() {
		return getProperty("MYSQL_USER");
	}
	
	public String getMysqlPassword() {
		return getProperty("MYSQL_PASSWORD");
	}

	
}
