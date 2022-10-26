package controller;

import lombok.SneakyThrows;
import lombok.val;
import model.User;
import org.assertj.core.api.Assertions;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @AfterAll
    void tearDown() throws Exception {
        connection.close();
    }

    @Nested
    class Create {

        @Test
        public void create_WithUser_Correct() throws Exception {
            User correctUser = new User("Test");
            controller.create(correctUser);

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);

            assertEquals(userTable.getRowCount() + 1, resultingTable.getRowCount());
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
    }
    // wg olszewskiego powinno być tych testów ( gdzie test dla danego parametru liczy się jako pojedynczy test)
    // powinno być 4 * 4, wszystkie możliwe kombinacje dla złego id i złęgo usera

    // TODO: Parameterized tests with a few more test cases
    @Nested
    class Delete {

        @SneakyThrows
        @Test
        public void delete_WithId_Existing() {
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
        public void delete_WithId_Nonexistent() {
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
        public void delete_WithId_Negative() {
            // given that a user of this id does not exist
            final long idOfNonexistentUser = -1;

            // when, then
            assertThrows(IllegalArgumentException.class, () -> controller.delete(idOfNonexistentUser));
        }
    }

}
