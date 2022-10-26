package controller;

import lombok.Getter;
import lombok.SneakyThrows;
import model.User;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import java.io.InputStream;

class DatabaseTestConfigurator {

    private static final String JDBC_DRIVER_SQREL = "org.postgresql.Driver";
    private static final String DB_UNIT_CONNECTION_SGREL_SHORT = "jdbc:postgresql://localhost:5431/";

    private final static String USERNAME = "postgres";
    private final static String PASSWORD = "admin";

    private static final String MOCK_DATABASE_DIR = "/users_mock.xml";

    private final static String USERS_TABLE_NAME = "users";

    @Getter
    private final ITable userTable;

    @Getter
    private final CRUD<User> userCrud;

    @Getter
    private final IDatabaseConnection databaseConnection;

    @SneakyThrows
    DatabaseTestConfigurator() {
        setDatabaseSystemProperties();
        IDataSet dataSet = getDataSetFromResource();
        userTable = dataSet.getTable(USERS_TABLE_NAME);
        databaseConnection = getConnectionToDatabase(dataSet);
        userCrud = createCrudController();
    }

    private static IDataSet getDataSetFromResource() throws DataSetException {
        InputStream mockUserXmlStream = DatabaseTestConfigurator.class.getResourceAsStream(MOCK_DATABASE_DIR);
        return new FlatXmlDataSetBuilder().build(
                mockUserXmlStream);
    }

    private static IDatabaseConnection getConnectionToDatabase(IDataSet dataSet) throws Exception {
        final var databaseTester = new JdbcDatabaseTester(JDBC_DRIVER_SQREL, DB_UNIT_CONNECTION_SGREL_SHORT, USERNAME, PASSWORD);
        databaseTester.setDataSet(dataSet);
        databaseTester.onSetup();

        final var connection = databaseTester.getConnection();

        DatabaseConfig dbConfig = connection.getConfig();
        dbConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());

        return connection;
    }

    private static CRUD<User> createCrudController() {
        return new CrudController(DB_UNIT_CONNECTION_SGREL_SHORT, USERNAME, PASSWORD);
    }

    private static void setDatabaseSystemProperties() {
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, JDBC_DRIVER_SQREL);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, DB_UNIT_CONNECTION_SGREL_SHORT);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, USERNAME);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, PASSWORD);
    }

}
