package com.revature.orm.mapper;

import com.revature.models.Tester;
import com.revature.orm.exceptions.FailedQueryException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresORMTest {

    private ObjectRelationalMapper orm = new PostgresORM();
    private Tester tester;
    private final int TESTER_ID = 5;
    private final String TESTER_NAME = "Numbah 5";

    @BeforeEach
    public void setup(){
        tester = new Tester();
        tester.setId(TESTER_ID);
        tester.setName(TESTER_NAME);
    }

    @Test
    @Order(1)
    public void insert() {
        assertTrue(orm.insert(tester));
    }

    @Test
    @Order(2)
    public void getAll() {
        try {
            List<Tester> testers = orm.getAll(Tester.class);
            assertFalse(testers.isEmpty());
        } catch (FailedQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(3)
    public void getById() {
        Tester newTester = (Tester) orm.getById(Tester.class, TESTER_ID).orElseThrow(RuntimeException::new);
        assertEquals(TESTER_ID, newTester.getId());
        assertEquals(TESTER_NAME, newTester.getName());
    }

    @Test
    @Order(4)
    public void update() {
        String updatedName = "New name";
        tester.setName(updatedName);
        assertTrue(orm.update(tester, TESTER_ID));
    }

    @Test
    @Order(5)
    public void delete() {
        assertTrue(orm.delete(Tester.class, TESTER_ID));
    }
}