package com.tranhuan.peopledb.repository;

import com.tranhuan.peopledb.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {


    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:~/peopletest".replace("~", System.getProperty("user.home")));
        repo = new PeopleRepository(connection);
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        Person john = new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 12, 12, 12, ZoneId.of("+0")));
        Person savedPerson = repo.save(john);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }
    @Test
    public void canSaveTwoPeople() {
        Person p1 = new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 12, 12, 12, ZoneId.of("+0")));
        Person p2 = new Person("save", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 12, 12, 12, ZoneId.of("+0")));
        Person savedPerson1 = repo.save(p1);
        System.out.println(savedPerson1.getId());
        Person savedPerson2 = repo.save(p2);
        System.out.println(savedPerson2.getId());
        assertThat(savedPerson2.getId()).isNotEqualTo(savedPerson1.getId());
    }

    @Test
    public void canFindById() {
        Person savedPerson = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        Person foundperson = repo.findById(savedPerson.getId()).get();
        assertThat(foundperson).isEqualTo(savedPerson);
    }

    @Test
    public void testPersonIdNotFound() {
        Person savedPerson = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        Optional<Person> person = repo.findById(-1L);
        assertThat(person).isEmpty();
    }

    @Test
    public void canDelete() {
        Person savedPerson = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        long before = repo.count();
        repo.delete(savedPerson);
        long after = repo.count();
        assertThat(after).isEqualTo(before - 1);
    }
    @Test
    public void canDeleteTwoPeople() {
        Person savedPerson1 = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        Person savedPerson2 = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        long before = repo.count();
        repo.delete(savedPerson1, savedPerson2);
        long after = repo.count();
        assertThat(after).isEqualTo(before - 2);
    }

    @Test
    public void canUpdate() {
        Person savedPerson = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0, ZoneId.of("+0"))));
        Person p1 = repo.findById(savedPerson.getId()).get();
        System.out.println(p1.getSalary());
        savedPerson.setSalary(new BigDecimal("72000.99"));
        repo.update(savedPerson);
        System.out.println(savedPerson.getSalary());
        assertThat(savedPerson.getSalary()).isNotEqualTo(p1.getSalary());

    }

}
