package controller;

import lombok.val;
import model.User;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CrudControllerTest {
    private static final String JDBC_DRIVER_SQREL = "org.postgresql.Driver";
    private static final String DB_UNIT_CONNECTION_SGREL_SHORT = "jdbc:postgresql://localhost:5431/";
    private static final String MOCK_DATABASE_DIR = "/users_mock.xml";
    private final static String USERNAME = "admin";
    private final static String PASSWORD = "admin";
    private final static String TABLE_NAME = "users";
    private ITable defaultTable;
    private IDatabaseTester databaseTester;
    private CrudController controller;
    private InputStream inputStream;

    @BeforeAll
    void setup() throws Exception {
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, JDBC_DRIVER_SQREL);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, DB_UNIT_CONNECTION_SGREL_SHORT);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, USERNAME);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, PASSWORD);

        inputStream = this.getClass().getResourceAsStream(MOCK_DATABASE_DIR);
        databaseTester = new JdbcDatabaseTester(JDBC_DRIVER_SQREL, DB_UNIT_CONNECTION_SGREL_SHORT, USERNAME, PASSWORD);
        IDataSet dataSet = getDataSet();
        defaultTable = dataSet.getTable(TABLE_NAME);

        databaseTester.setDataSet(dataSet);

        databaseTester.onSetup();
        controller = new CrudController(DB_UNIT_CONNECTION_SGREL_SHORT, USERNAME, PASSWORD);
    }

    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(
                inputStream);
    }

    @Test
    public void create_WithUser_Correct() throws Exception {
        User correctUser = new User("Test");
        controller.create(correctUser);
        ITable resultingTable = databaseTester.getConnection().createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
        Assertions.assertEquals(resultingTable.getRowCount(), defaultTable.getRowCount() + 1);
    }

    @Test
    public void create_WithUser_Null() {

    }

    @Test
    public void create_WithUser_Name_Null() {

    }

    @Test
    public void create_WithUser_Name_SqlInjection() {

    }

    /*
    Read operation
     */
    @ParameterizedTest
    @ValueSource(longs = {Integer.MIN_VALUE, -1, 0})
    void read_shouldThrow_whenIdInvalid(long id) {
        assertThrows(RuntimeException.class, () -> controller.read(id));
    }

    @ParameterizedTest
    @MethodSource("validReadRecordsFactory")
    void read_shouldReturnNonEmptyOptional_whenIdExists(long id, User expectedUser) {
        assertDoesNotThrow(() -> controller.read(id));
        val readResult = controller.read(id);
        assertTrue(readResult.isPresent());
        assertEquals(expectedUser, readResult.get());
    }

    private Stream<Arguments> validReadRecordsFactory() {
        return Stream.of(
            Arguments.of(1, new User("Krabelard")),
            Arguments.of(2, new User("Gordon")),
            Arguments.of(3, new User("Sysy")),
            Arguments.of(4, new User("Szniok")),
            Arguments.of(5, new User("Gniok")),
            Arguments.of(6, new User("Craig")),
            Arguments.of(7, new User("MrZaroweczka")),
            Arguments.of(8, new User("Grypsztal's")),
            Arguments.of(9, new User("Dziok"))
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {100, Integer.MAX_VALUE})
    void read_shouldReturnEmptyOptional_whenIdDoesNotExist (long id) {
        assertDoesNotThrow(() -> controller.read(id));
        val readResult = controller.read(id);
        assertTrue(readResult.isEmpty());
    }

    /*
    Update operation
     */
    @Test
    public void update_WithId_Existing() {

    }

    @Test
    public void update_WithId_Nonexistent() {

    }

    @Test
    public void update_WithId_Negative() {

    }

    @Test
    public void update_WithId_MAX_INTEGER_And_MIN_INTEGER() {

    }

    @Test
    public void update_WithUser_Incorrect() {

    }

    @Test
    public void update_WithUser_Name_Incorrect() {

    }

    @Test
    public void update_WithUser_Incorrect_Id_Correct() {

    }

    @Test
    public void update_WithUser_Correct_Id_Correct() {

    }
    // wg olszewskiego powinno być tych testów ( gdzie test dla danego parametru liczy się jako pojedynczy test)
    // powinno być 4 * 4, wszystkie możliwe kombinacje dla złego id i złęgo usera

    @Test
    public void delete_WithId_Existing() {

    }

    @Test
    public void delete_WithId_Nonexistent() {

    }

    @Test
    public void delete_WithId_Negative() {

    }

    @Test
    public void delete_WithId_MAX_INTEGER_And_MIN_INTEGER() {

    }

}
