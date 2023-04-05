package com.tranhuan.peopledb.repository;

import com.tranhuan.peopledb.annotation.SQL;
import com.tranhuan.peopledb.model.Address;
import com.tranhuan.peopledb.model.CrudOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressRepository extends CRUDRepository<Address>{

    public AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(operationType = CrudOperation.SAVE, value = "INSERT INTO ADDRESSES(STREET_ADDRESS, ADDRESS2, CITY, STATE, POSTCODE, COUNTY, REGION, COUNTRY) VALUES(?, ?, ?, ?, ?, ?, ?, ?)")
    void mapForSave(Address entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getStreetAddress());
        ps.setString(2, entity.getAddress2());
        ps.setString(3, entity.getCity());
        ps.setString(4, entity.getState());
        ps.setString(5, entity.getPostcode());
        ps.setString(6, entity.getCounty());
        ps.setString(7, entity.getRegion().toString());
        ps.setString(8, entity.getCountry());
    }

    @Override
    void mapForUpdate(Address entity, PreparedStatement ps) throws SQLException {

    }

    @Override
    Address extractEntityFromResultSets(ResultSet rs) throws SQLException {
        return null;
    }
}
