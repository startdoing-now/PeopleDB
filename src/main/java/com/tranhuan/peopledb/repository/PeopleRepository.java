package com.tranhuan.peopledb.repository;

import com.tranhuan.peopledb.annotation.SQL;
import com.tranhuan.peopledb.model.CrudOperation;
import com.tranhuan.peopledb.model.Person;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PeopleRepository extends CRUDRepository <Person>{
    public static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE(FIRST_NAME, LAST_NAME, DOB, SALARY) VALUES(?, ?, ?, ?)";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PEOPLE WHERE ID=?";
    public static final String FIND_ALL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PEOPLE";
    public static final String COUNT_SQL = "SELECT COUNT(*) FROM PEOPLE";
    public static final String UPDATE_SQL = "UPDATE PEOPLE SET  FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";
    public static final String DELETE_SQL = "DELETE FROM PEOPLE WHERE ID = ?";
    public static final String DELETE_IN_SQL = "DELETE FROM PEOPLE WHERE ID IN (:ids)";


    public PeopleRepository(Connection connection) {
        super(connection);
    }





    @Override
    @SQL(value = FIND_BY_ID_SQL, operationType = CrudOperation.FIND_BY_ID)
    @SQL(value = FIND_ALL, operationType = CrudOperation.FIND_ALL)
    @SQL(value = COUNT_SQL, operationType = CrudOperation.COUNT)
    @SQL(value = DELETE_SQL, operationType = CrudOperation.DELETE_ONE)
    @SQL(value = DELETE_IN_SQL, operationType = CrudOperation.DELETE_MANY)
    Person extractEntityFromResultSets(ResultSet rs) throws SQLException {
        Person person;
        Long personId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        person = new Person(personId, firstName, lastName, dob, salary);
        return person;
    }




    @Override
    @SQL(value = "INSERT INTO PEOPLE(FIRST_NAME, LAST_NAME, DOB, SALARY) VALUES(?, ?, ?, ?)", operationType = CrudOperation.SAVE)
    void mapForSave(Person person, PreparedStatement ps) throws SQLException {
        ps.setString(1, person.getFirstName());
        ps.setString(2, person.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(person.getDob()));
        ps.setBigDecimal(4, person.getSalary());
    }
    @Override
    @SQL(value = "UPDATE PEOPLE SET  FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?", operationType = CrudOperation.UPDATE )
    void mapForUpdate(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());

    }


    private static Timestamp convertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }






}
