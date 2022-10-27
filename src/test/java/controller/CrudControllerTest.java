package controller;

import lombok.SneakyThrows;
import lombok.val;
import model.User;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.postgresql.util.PSQLException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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

        @SneakyThrows
        @DisplayName("CrudController#create - should add new database entry for valid User object")
        @ParameterizedTest(name = "{index} -> user={0}")
        @MethodSource("correctUserGenerator")
        void create_shouldAddNewEntry_whenUserValid(User user) {
            assertDoesNotThrow(() -> controller.create(user));

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount() + 1, resultingTable.getRowCount());
        }

        @SneakyThrows
        @DisplayName("CrudController#create - should throw SQLException for User object invalid by SQL standard")
        @ParameterizedTest(name = "{index} -> user={0}")
        @MethodSource("incorrectUserGeneratorPsqlManaged")
        void create_shouldThrowException_whenUserInvalidBySqlStandard(User user) {
            assertThrows(PSQLException.class,() -> controller.create(user));

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount(), resultingTable.getRowCount());

        }

        @SneakyThrows
        @DisplayName("CrudController#create - should throw IllegalArgumentException for User object invalid by business logic")
        @ParameterizedTest(name = "{index} -> user={0}")
        @MethodSource("incorrectUserGenerator")
        void create_shouldThrowException_whenUserInvalidByAppLogicStandard(User user) {
            Exception e = assertThrows(IllegalArgumentException.class,() -> controller.create(user));

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);
            assertEquals(userTable.getRowCount() , resultingTable.getRowCount());
            assertEquals("Username must not be blank.",e.getMessage());
        }

        @SneakyThrows
        @DisplayName("CrudController#create - should throw NullPointerException for null User object")
        @ParameterizedTest(name = "{index} -> user={0}")
        @NullSource
        void create_shouldThrowException_whenUserNull(User user) {
            assertThrows(NullPointerException.class,() -> controller.create(user));

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

        private static Stream<Arguments> incorrectUserGenerator() {
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

        @DisplayName("CrudController#read - should throw IllegalArgumentException for id <= 0")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {Integer.MIN_VALUE, -1, 0})
        void read_shouldThrow_whenIdInvalid(long id) {
            val expectedMessage = "id must be greater than 0";

            Exception e = assertThrows(IllegalArgumentException.class, () -> controller.read(id));
            assertEquals(expectedMessage, e.getMessage());
        }

        @SneakyThrows
        @DisplayName("CrudController#read - should return Optional of given User for existing id")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {1, 9})
        void read_shouldReturnNonEmptyOptional_whenIdValid(long id) {
            val expectedUser = new User((String) userTable.getValue((int) id - 1 ,"username"));

            assertDoesNotThrow(() -> controller.read(id));
            val readResult = controller.read(id);

            assertTrue(readResult.isPresent());
            assertEquals(expectedUser, readResult.get());
        }

        @SneakyThrows
        @DisplayName("CrudController#read - should return Optional.empty() for id >= 10")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {10, Integer.MAX_VALUE})
        void read_shouldReturnEmptyOptional_whenIdNotInDatabase(long id) {
            assertDoesNotThrow(() -> controller.read(id));

            val readResult = controller.read(id);

            assertTrue(readResult.isEmpty());
        }

    }

    @Nested
    class Update {

        @SneakyThrows
        @DisplayName("CrudController#update - should alter record in database for existing id and valid User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {1, 9})
        public void update_ShouldUpdateUser_WhenIdIsInDatabase_AndUserIsCorrect(long id) {
            User userUpdate = new User("UpdatedUser");

            assertDoesNotThrow(() -> controller.update(id, userUpdate));

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME +" WHERE id="+ id);
            assertEquals("UpdatedUser", resultingTable.getValue(0, "username"));

        }

        @DisplayName("CrudController#update - should do nothing for non-existent id and valid User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {10, Integer.MAX_VALUE})
        public void update_ShouldNotThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsCorrect(long id){
            User userUpdate = new User("UpdatedUser");

            assertDoesNotThrow(() -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for id <= 0 and valid User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {Integer.MIN_VALUE, -1, 0})
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsCorrect(long id) {
            User userUpdate = new User("UpdatedUser");

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for existing id and null User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {1, 9})
        public void update_ShouldThrowException_WhenIdIsCorrect_AndUserIsNull(long id){
            User userUpdate = null;

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for existing id and User object with blank username")
        @ParameterizedTest(name = "{index} -> username={0}")
        @ValueSource(strings = {"", " ", "\t", "\n", "\r"})
        public void update_ShouldThrowException_WhenIdIsCorrect_AndUserIsWhitespace(String username) {
            long id = 1;
            User userUpdate = new User(username);

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for id <= 0 and null User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {Integer.MIN_VALUE, -1, 0})
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsNull(long id){
            User userUpdate = null;

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw for non-existent id and null User object")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {10, Integer.MAX_VALUE})
        public void update_ShouldThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsNull(long id) {
            User userUpdate = null;

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for id <= 0 and User object with blank username")
        @ParameterizedTest(name = "{index} -> id={1}, username={0}")
        @MethodSource("provideParametersForIdNegative")
        public void update_ShouldThrowException_WhenIdIsNegative_AndUserIsWhitespace(String username, long id) {
            User userUpdate = new User(username);

            assertThrows(IllegalArgumentException.class, () -> controller.update(id, userUpdate));
        }

        @DisplayName("CrudController#update - should throw IllegalArgumentException for non-existent id and User object with blank username")
        @ParameterizedTest(name = "{index} -> id={1}, username={0}")
        @MethodSource("provideParametersForIdCorrectButNotInDatabase")
        public void update_ShouldThrowException_WhenIdIsCorrectButNotInDatabase_AndUserIsWhitespace(String username, long id) {
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

    @Nested
    class Delete {

        @SneakyThrows
        @DisplayName("CrudController#delete - should remove record from database for existing id")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {1, 9})
         void delete_ShouldDeleteUserSuccessfully_WhenUserWithIdExists(long idOfExistingUser) {
            assertDoesNotThrow(() -> controller.delete(idOfExistingUser));

            val resultingTable = connection.createQueryTable(TABLE_NAME,
                    "SELECT * FROM " + TABLE_NAME + " WHERE id = " + idOfExistingUser);

            assertThat(resultingTable.getRowCount()).isEqualTo(0);
        }

        @SneakyThrows
        @DisplayName("CrudController#delete - should do nothing for non-existent id")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {10, Integer.MAX_VALUE})
         void delete_ShouldNotDeleteAnyUsers_WhenUserWithIdDoesNotExist(long idOfNonexistentUser) {
            assertDoesNotThrow(() -> controller.delete(idOfNonexistentUser));

            val resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);

            assertThat(resultingTable.getRowCount()).isEqualTo(userTable.getRowCount());
        }

        @SneakyThrows
        @DisplayName("CrudController#delete - should throw IllegalArgumentException for id <= 0")
        @ParameterizedTest(name = "{index} -> id={0}")
        @ValueSource(longs = {Integer.MIN_VALUE, -1, 0})
         void delete_ShouldThrowException_AndNotDeleteUsers_WhenUserIdIsSmallerThanZero(long idOfNonexistentUser) {
            assertThrows(IllegalArgumentException.class, () -> controller.delete(idOfNonexistentUser));

            val resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);

            assertThat(resultingTable.getRowCount()).isEqualTo(userTable.getRowCount());
        }
    }

}
