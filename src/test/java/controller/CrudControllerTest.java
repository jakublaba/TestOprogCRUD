package controller;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.FileInputStream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CrudControllerTest {
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_UNIT_DRIVER_CONNECTION_URL = "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;init=runscript from 'tu coś.sql'";
    private static final String DB_UNIT_CONNECTION_URL_SHORT = "jdbc:h2:mem:default";
    private static final String MOCK_DATABASE_DIR = "";
    private final static String USERNAME = "sa";
    private final static String PASSWORD = "sa";
    private final static String TABLE_NAME = "";
    ITable defaultTable;
    private IDatabaseTester databaseTester;

    protected CrudControllerTest() {
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, JDBC_DRIVER);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, DB_UNIT_CONNECTION_URL_SHORT);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, USERNAME);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, PASSWORD);

    }

    @BeforeAll
    public void setup() throws Exception {
        databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, DB_UNIT_DRIVER_CONNECTION_URL, USERNAME, PASSWORD);
        IDataSet dataSet = getDataSet();
        defaultTable = dataSet.getTable(TABLE_NAME);

        databaseTester.setDataSet(dataSet);

        databaseTester.onSetup();
    }

    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(
                new FileInputStream(MOCK_DATABASE_DIR));
    }
}
