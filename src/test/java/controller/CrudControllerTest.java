package controller;

import lombok.SneakyThrows;
import lombok.val;
import model.User;
import org.assertj.core.api.Assertions;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.postgresql.util.PSQLException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrudControllerTest {

    private final static String TABLE_NAME = "users";
    private ITable userTable;
    private CRUD<User> controller;
    private IDatabaseConnection connection;

    private DatabaseTestConfigurator databaseTestConfigurator;
    private final static String USERNAME_WITH_32_CHARS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private final static String USER_THAT_ALREADY_EXISTS = "Krabelard";
    private final static String ALL_POLISH_SIGNS = "Zażółć gęślą jaźń";
    public static final String CORRECT_USERNAME = "TEST";
    public static final String SQL_INJECTION = "SELECT * FROM users";
    public static final String NULL_LITERAL = "null";
    public static final String EXPECTED_ARGUMENT_EXCEPTION_MSG = "Username must not be blank.";


    @BeforeAll
    void setup() {
        databaseTestConfigurator = new DatabaseTestConfigurator();
        userTable = databaseTestConfigurator.getUserTable();
        controller = databaseTestConfigurator.getUserCrud();
        connection = databaseTestConfigurator.getDatabaseConnection();
    }

    @BeforeEach
    void reloadDataSet() {
        databaseTestConfigurator.setUpDataSet();
    }

    @AfterEach
    void tearDownDataSet() {
        databaseTestConfigurator.tearDown();
    }

    @AfterAll
    void tearDown() throws Exception {
        connection.close();
    }

    @Nested
    class Create {
        @ParameterizedTest
        @SneakyThrows
        @MethodSource("correctUserGenerator")
        void create_WithUser_Correct(User user) throws Exception {
            // when given a user with correct name

            //when
            assertDoesNotThrow(() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount() + 1, resultingTable.getRowCount());
        }

        @ParameterizedTest
        @SneakyThrows
        @MethodSource("incorrectUserGeneratorPsqlManaged")
        void create_WithUser_Incorrect_PsqlManaged(User user) {
            //when given an incorrect user

            //when
            assertThrows(PSQLException.class,() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount(), resultingTable.getRowCount());

        }
        @ParameterizedTest
        @SneakyThrows
        @MethodSource("incorrectUserGenerator")
        void create_WithUser_Incorrect(User user) throws Exception {
            // when given a user with correct name

            //when
            Exception e = assertThrows(IllegalArgumentException.class,() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount() , resultingTable.getRowCount());
            assertEquals(EXPECTED_ARGUMENT_EXCEPTION_MSG,e.getMessage());
        }
        @Test
        @SneakyThrows
        void create_WithUser_Null()
        {
            // when given a user with correct name
            User user = null;


            assertThrows(NullPointerException.class,() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount(), resultingTable.getRowCount());
        }
        private static Stream<Arguments> incorrectUserGeneratorPsqlManaged() {

            return Stream.of(
                    Arguments.of(new User(USERNAME_WITH_32_CHARS)),
                    Arguments.of(new User("\0")),
                    Arguments.of(new User(null))
                    );
        }
        private static Stream<Arguments> correctUserGenerator() {

            return Stream.of(
                    Arguments.of(new User(CORRECT_USERNAME)),
                    Arguments.of(new User(SQL_INJECTION)),
                    Arguments.of(new User(NULL_LITERAL)),
                    Arguments.of(new User(USER_THAT_ALREADY_EXISTS)),
                    Arguments.of(new User(ALL_POLISH_SIGNS))

            );
        }
        private static Stream<Arguments> incorrectUserGenerator()
        {
            return Stream.of(
                    Arguments.of(new User(" ")),
                    Arguments.of(new User("\n")),
                    Arguments.of(new User("\t"))
            );
        }

    }

    @Nested
    class Read {
        @ParameterizedTest
        @SneakyThrows
        @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9})
        void read_shouldReturnNonEmptyOptional_whenIdValid(long id) {
            assertDoesNotThrow(() -> controller.read(id));
            val readResult = controller.read(id);
            assertTrue(readResult.isPresent());
            assertEquals(userTable.getValue((int) id - 1 ,"username"), readResult.get().username());
        }

        @SneakyThrows
        @ParameterizedTest
        @ValueSource(longs = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE})
        void read_shouldReturnEmptyOptional_whenIdInvalid(long id) {
            assertDoesNotThrow(() -> controller.read(id));
            val readResult = controller.read(id);
            assertTrue(readResult.isEmpty());
        }
    }

    @Nested
    class Update {


        @Test
        void update_WithId_Existing() {

        }

        @Test
        void update_WithId_Nonexistent() {

        }

        @Test
        void update_WithId_Negative() {

        }

        @Test
        void update_WithId_MAX_INTEGER_And_MIN_INTEGER() {

        }

        @Test
         void update_WithUser_Incorrect() {

        }

        @Test
         void update_WithUser_Name_Incorrect() {

        }

        @Test
         void update_WithUser_Incorrect_Id_Correct() {

        }

        @Test
         void update_WithUser_Correct_Id_Correct() {

        }
    }
    // wg olszewskiego powinno być tych testów ( gdzie test dla danego parametru liczy się jako pojedynczy test)
    // powinno być 4 * 4, wszystkie możliwe kombinacje dla złego id i złęgo usera

    // TODO#12: Parameterized tests with a few more test cases
    @Nested
    class Delete {

        @SneakyThrows
        @Test
         void delete_WithId_Existing() {
            // given an id that is in the database
            final long idOfExistingUser = 10;

            // when
            assertDoesNotThrow(() -> controller.delete(idOfExistingUser));

            // then
            final var resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME + " WHERE id = " + idOfExistingUser);

            Assertions.assertThat(resultingTable.getRowCount())
                    .isEqualTo(0);
        }

        @SneakyThrows
        @Test
         void delete_WithId_Nonexistent() {
            // given that a user of this id does not exist
            final long idOfNonexistentUser = 2147483647;

            // when
            assertDoesNotThrow(() -> controller.delete(idOfNonexistentUser));

            // then
            final var resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);

            Assertions.assertThat(resultingTable.getRowCount())
                    .isEqualTo(userTable.getRowCount());
        }

        @Test
         void delete_WithId_Negative() {
            // given that a user of this id does not exist
            final long idOfNonexistentUser = -1;

            // when, then
            assertThrows(IllegalArgumentException.class, () -> controller.delete(idOfNonexistentUser));
        }
    }

}
