package com.ab.selenium.dataprovider.db;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Database data provider to read from database. Can provide a select statement.
 */
public class DataProvider {
	/**
	 * Configuration file property to connect to the database. e.g. for mySQL:
	 * {@code jdbc:mysql://<host>:<port>/<database>}
	 */
	private static final String PROPERTY_DB_URL = "url";
	/**
	 * JDBC driver class to be used for connection. Depends on used database. e.g. for mySQL:
	 * {@code com.mysql.jdbc.Driver}
	 */
	private static final String PROPERTY_DB_DRIVER = "driver";
	/** User for database access. */
	private static final String PROPERTY_DB_USER = "user";
	/** Password for database access. */
	private static final String PROPERTY_DB_PASSWORD = "passwd";

	/**
	 * Convenience method to read the first column using {@link #readFromDatabase(File, String, int)}.
	 * 
	 * @param connectionDetails
	 *            .properties file containing following parameters:
	 *            <ul>
	 *            <li><b>url</b>: to connect to the database. e.g. for mySQL:
	 *            {@code jdbc:mysql://<host>:<port>/<database>}</li>
	 *            <li><b>driver</b>: class to be used for connection. Depends on used database. e.g. for mySQL
	 *            {@code com.mysql.jdbc.Driver}</li>
	 *            <li><b>user</b>: used for access.</li>
	 *            <li><b>passwd</b>: used for access.</li>
	 *            </ul>
	 * @param select
	 *            may be one of
	 *            <ol>
	 *            <li>property name(key) to retrieve the statement from the file with connection details. String should
	 *            not contain any white space.</li>
	 *            or
	 *            <li>select statement to be executed in the database. Contains multiple white spaces.</li>
	 *            </ol>
	 * @return column read using the specified statement.
	 * @throws IOException
	 *             if there are any problems reading from file with connection details or there are problems with
	 *             connection to the database
	 * @throws SQLException
	 *             if the database is returning an error
	 */
	public final String[] readFromDatabase(final File connectionDetails, final String select) throws IOException,
			SQLException {
		return readFromDatabase(connectionDetails, select, 1);
	}

	/**
	 * Read the properties file and execute select statement. The results (if containing several columns, then only of
	 * the first column) are returned as String array.
	 * 
	 * @param connectionDetails
	 *            .properties file containing following parameters:
	 *            <ul>
	 *            <li><b>url</b>: to connect to the database. e.g. for mySQL:
	 *            {@code jdbc:mysql://<host>:<port>/<database>}</li>
	 *            <li><b>driver</b>: class to be used for connection. Depends on used database. e.g. for mySQL
	 *            {@code com.mysql.jdbc.Driver}</li>
	 *            <li><b>user</b>: used for access.</li>
	 *            <li><b>passwd</b>: used for access.</li>
	 *            </ul>
	 * @param select
	 *            may be one of
	 *            <ol>
	 *            <li>property name(key) to retrieve the statement from the file with connection details. String should
	 *            not contain any white space.</li>
	 *            or
	 *            <li>select statement to be executed in the database. Contains multiple white spaces.</li>
	 *            </ol>
	 * @param readColumnIdx
	 *            index of the column to read and return as results
	 * @return column read using the specified statement.
	 * @throws IOException
	 *             if there are any problems reading from file with connection details or there are problems with
	 *             connection to the database
	 * @throws SQLException
	 *             if the database is returning an error
	 */
	public final String[] readFromDatabase(final File connectionDetails, final String select, final int readColumnIdx)
			throws IOException, SQLException {
		String[] results = null;

		try {
			Properties properties = new Properties();
			FileReader connectionPropertiesReader = new FileReader(connectionDetails);
			properties.load(connectionPropertiesReader);
			// clean up after reading
			connectionPropertiesReader.close();

			Connection jdbcConnection = openConnection(properties);

			String selectStatement = getSelectStatement(properties, select);
			Statement statement = jdbcConnection.createStatement();
			ResultSet result = statement.executeQuery(selectStatement);

			results = readResults(result, readColumnIdx);
			// clean up db connection
			result.close();
			statement.close();
			jdbcConnection.close();
		} catch (ConnectException e) {
			throw new RuntimeException("Unable to connect to the database.", e);
		}

		return results;
	}

	/**
	 * Go through all retrieved results and get the specified column values.
	 * 
	 * @param result
	 *            ResultSet with the data from connection
	 * @param columnOfInterest
	 *            where to get the results from. First column has index {@code 1}.
	 * @return values from specified column as array.
	 * @throws SQLException
	 *             if not able to read the results
	 */
	private static String[] readResults(final ResultSet result, final int columnOfInterest) throws SQLException {
		List<String> results = new ArrayList<String>();
		while (result.next()) {
			results.add(result.getString(columnOfInterest));
		}
		return results.toArray(new String[] {});
	}

	/**
	 * Get the SQL SELECT statement.
	 * <ol>
	 * There are two options for select statement:
	 * <li>It may be passed as the parameter {@code select}, or</li>
	 * <li>It may be the property name.</li>
	 * </ol>
	 * 
	 * @param properties
	 *            as read from the configuration file
	 * @param select
	 *            statement or property name
	 * @return statement, which should be ready to be executed against the database.
	 */
	private static String getSelectStatement(final Properties properties, final String select) {
		if (select.contains(" ")) {
			return select;
		}
		String selectFromProperties = properties.getProperty(select);
		if (selectFromProperties != null) {
			return selectFromProperties;
		}
		throw new IllegalArgumentException("unable to obtain property by key [" + select + "].");
	}

	/**
	 * Open connection to the database using the connection details from properties.
	 * 
	 * @param properties
	 *            with connection details
	 * @return opened connection
	 * @throws FileNotFoundException
	 * @throws IOException
	 *             if there were any problems connecting to the database
	 * @throws SQLException
	 *             if the database itself returns an error
	 */
	private Connection openConnection(final Properties properties) throws SQLException {
		checkPropertiesPresent(properties, new String[] {PROPERTY_DB_DRIVER, PROPERTY_DB_URL, PROPERTY_DB_USER,
				PROPERTY_DB_PASSWORD});
		// This will load the driver, each DB has its own driver
		String jdbcDriverClass = properties.getProperty(PROPERTY_DB_DRIVER);
		try {
			Class.forName(jdbcDriverClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to find the jdbc driver on the classpath. [" + jdbcDriverClass
					+ "]");
		}

		String jdbcUrl = properties.getProperty(PROPERTY_DB_URL);
		String user = properties.getProperty(PROPERTY_DB_USER);
		String password = properties.getProperty(PROPERTY_DB_PASSWORD);
		return DriverManager.getConnection(jdbcUrl, user, password);
	}

	/**
	 * Convenience method to check that all properties are present, rather than checking every one of them separately.
	 * 
	 * @param properties
	 *            as read from configuration file
	 * @param keys
	 *            to be checked
	 */
	@SuppressWarnings("static-method")
	private void checkPropertiesPresent(final Properties properties, final String[] keys) {
		boolean[] propertyFound = new boolean[keys.length];
		String errorMessage = "Expecting to find following properties: " + Arrays.toString(keys) + "\n Not found: ";
		boolean errorFound = false;
		for (int i = 0; i < keys.length; i++) {
			if (!properties.containsKey(keys[i])) {
				errorFound = true;
				errorMessage += " " + keys[i];
			}
			propertyFound[i] = properties.containsKey(keys[i]);
		}
		if (errorFound) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

}
