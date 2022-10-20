package controller;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileInputStream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CrudControllerTest {
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_UNIT_DRIVER_CONNECTION_URL = "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1;init=runscript from 'resources/users_schema.sql'";
    private static final String DB_UNIT_CONNECTION_URL_SHORT = "jdbc:h2:mem:default";
    private static final String MOCK_DATABASE_DIR = "resources/users_mock.xml";
    private final static String USERNAME = "sa";
    private final static String PASSWORD = "sa";
    private final static String TABLE_NAME = "users";
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
    @Test
    public void create_WithUser_Correct()
    {

    }
    @Test
    public void create_WithUser_Null()
    {

    }
    @Test
    public void create_WithUser_Name_Null()
    {

    }
    @Test
    public void create_WithUser_Name_SqlInjection()
    {

    }
    /*
    Read operation
     */
    @Test
    public void read_WithId_Existing()
    {

    }
    @Test
    public void read_WithId_Nonexistent()
    {

    }
    @Test
    public void read_WithId_Negative()
    {

    }
    @Test
    public void read_WithId_MAX_INTEGER_And_MIN_INTEGER()
    {

    }
    /*
    Update operation
     */
    @Test
    public void update_WithId_Existing()
    {

    }
    @Test
    public void update_WithId_Nonexistent()
    {

    }
    @Test
    public void update_WithId_Negative()
    {

    }
    @Test
    public void update_WithId_MAX_INTEGER_And_MIN_INTEGER()
    {

    }
    @Test
    public void update_WithUser_Incorrect()
    {

    }
    @Test
    public void update_WithUser_Name_Incorrect()
    {

    }
    @Test
    public void update_WithUser_Incorrect_Id_Correct()
    {

    }
    @Test
    public void update_WithUser_Correct_Id_Correct()
    {

    }
    // wg olszewskiego powinno być tych testów ( gdzie test dla danego parametru liczy się jako pojedynczy test)
    // powinno być 4 * 4, wszystkie możliwe kombinacje dla złego id i złęgo usera

    @Test
    public void delete_WithId_Existing()
    {

    }
    @Test
    public void delete_WithId_Nonexistent()
    {

    }
    @Test
    public void delete_WithId_Negative()
    {

    }
    @Test
    public void delete_WithId_MAX_INTEGER_And_MIN_INTEGER()
    {

    }

}
