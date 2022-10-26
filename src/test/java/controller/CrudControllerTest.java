package controller;

import lombok.SneakyThrows;
import lombok.val;
import model.User;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.BeforeAll;
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

    @BeforeAll
    void setup() {
        final var configurator = new DatabaseTestConfigurator();
        userTable = configurator.getUserTable();
        controller = configurator.getUserCrud();
        connection = configurator.getDatabaseConnection();
    }

    @Nested
    class Create {

        @Test
        public void create_WithUser_Correct() throws Exception {
            User correctUser = new User("Test");
            controller.create(correctUser);

            ITable resultingTable = connection.createQueryTable(TABLE_NAME, "SELECT * FROM " + TABLE_NAME);

            assertEquals(resultingTable.getRowCount(), userTable.getRowCount() + 1);
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

    @Nested
    class Delete {

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

}
