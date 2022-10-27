package controller;

import lombok.SneakyThrows;
import lombok.val;
import model.User;
import org.assertj.core.api.Assertions;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
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

import java.sql.SQLException;
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
        void create_shouldAddNewEntry_whenUserValid(User user) throws Exception {
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
        void create_shouldThrowException_whenUserInvalidBySqlStandard(User user) {
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
        void create_shouldThrowException_whenUserInvalidByAppLogicStandard(User user) throws Exception {
            // when given a user with correct name

            //when
            Exception e = assertThrows(IllegalArgumentException.class,() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount() , resultingTable.getRowCount());
            assertEquals("Username must not be blank.",e.getMessage());
        }
        @Test
        @SneakyThrows
        void create_shouldThrowException_whenUserNull()
        {
            // when given a user with correct name
            User user = null;

            //when
            assertThrows(NullPointerException.class,() -> controller.create(user));

            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount(), resultingTable.getRowCount());
        }
        private static Stream<Arguments> incorrectUserGeneratorPsqlManaged() {

            return Stream.of(
                    Arguments.of(new User("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")),
                    Arguments.of(new User("\0"))
                    );
        }
        private static Stream<Arguments> correctUserGenerator() {

            return Stream.of(
                    Arguments.of(new User("Krabelard")),
                    Arguments.of(new User("Zażółć gęślą jaźń")),
                    Arguments.of(new User("TEST")),
                    Arguments.of(new User("SELECT * FROM users")),
                    Arguments.of(new User("null"))

            );
        }
        private static Stream<Arguments> incorrectUserGenerator()
        {
            return Stream.of(
                    Arguments.of(new User(" ")),
                    Arguments.of(new User("")),
                    Arguments.of(new User("\n")),
                    Arguments.of(new User("\t")),
                    Arguments.of(new User("\r")),
                    Arguments.of(new User("\f"))
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

        @ParameterizedTest
        @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9})
        public void update_ShouldUpdateUser_WhenIdIsInDatabase_AndUserIsCorrect(long id) throws DataSetException, SQLException {
            //given
            User userUpdate = new User("UpdatedUser");
            //when
            assertDoesNotThrow(() -> controller.update(id, userUpdate));
            //then
            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME +" WHERE id="+ id);
            assertEquals("UpdatedUser", resultingTable.getValue(0, "username"));

        }

        @ParameterizedTest
        @ValueSource(longs = {100, Integer.MAX_VALUE})
        public void update_ShouldThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsCorrect(long id){
            //given
            User userUpdate = new User("UpdatedUser");
            //when
            assertDoesNotThrow(() -> controller.update(id, userUpdate));
        }

        @ParameterizedTest
        @ValueSource(longs = {Integer.MIN_VALUE, -1})
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsCorrect(long id){
            //given
            User userUpdate = new User("UpdatedUser");
            //when, then
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }
        @ParameterizedTest
        @ValueSource(longs = {1})
        public void update_ShouldThrowException_WhenIdIsCorrect_AndUserIsNull(long id){
            User userUpdate = null;
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "\t", "\n"})
        public void update_ShouldThrowException_WhenIdIsCorrect_AndUserIsWhitespace(String username){
            long id = 1;
            User userUpdate = new User(username);
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @ParameterizedTest
        @ValueSource(longs = {Integer.MIN_VALUE, -1})
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsNull(long id){
            //given
            User userUpdate = null;
            //when, then
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @ParameterizedTest
        @ValueSource(longs = {100, Integer.MAX_VALUE})
        public void update_ShouldThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsNull(long id){
            //given
            User userUpdate = null;
            //when, then
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }
        
        @ParameterizedTest
        @MethodSource("provideParametersForIdNegative")
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsWhitespace(String username, long id){
            User userUpdate = new User(username);
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }
        
        @ParameterizedTest
        @MethodSource("provideParametersForIdCorrectButNotInDatabase")
        public void update_ShouldThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsWhitespace(String username, long id){
            User userUpdate = new User(username);
            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }
        
        private static Stream<Arguments> provideParametersForIdNegative() {
            return Stream.of(
                    Arguments.of(" ", Integer.MIN_VALUE),
                    Arguments.of("\t", Integer.MIN_VALUE),
                    Arguments.of("\n", Integer.MIN_VALUE),
                    Arguments.of(" ", -1),
                    Arguments.of("\t", -1),
                    Arguments.of("\n", -1)
            );
        }

        private static Stream<Arguments> provideParametersForIdCorrectButNotInDatabase() {
            return Stream.of(
                    Arguments.of(" ", Integer.MAX_VALUE),
                    Arguments.of("\t", Integer.MAX_VALUE),
                    Arguments.of("\n", Integer.MAX_VALUE),
                    Arguments.of(" ", 100),
                    Arguments.of("\t", 100),
                    Arguments.of("\n", 100)
            );
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
