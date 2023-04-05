package com.tranhuan.peopledb.repository;

import com.tranhuan.peopledb.model.Address;
import com.tranhuan.peopledb.model.Person;
import com.tranhuan.peopledb.model.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {


    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:~/peopletest".replace("~", System.getProperty("user.home")));
        repo = new PeopleRepository(connection);
        connection.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
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
    public void canSavePersonWithAddress() {
        Person john = repo.save(new Person("John", "Smith", ZonedDateTime.of(1920, 12, 12, 12, 0, 0, 0,  ZoneId.of("+0")), Optional.ofNullable(new Address( "123", "Apt. 1A", "wala wala", "WA", "9240", "Viet Nam",  Region.WEST, "county"))));

        assertThat(john.getHomeAddress().get().getId()).isGreaterThan(0);
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

    @Test
    @Disabled
    public void canLoadData() throws IOException {
        Files.lines(Path.of("C:\\Users\\admin\\Downloads\\Hr5m.csv"))
                .skip(1)
                .limit(10)
                .map(s -> s.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime ldtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(ldtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zdtob);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;
                })
                .forEach(repo::save);
    }

}
