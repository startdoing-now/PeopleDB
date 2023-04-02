package com.tranhuan.peopledb.repository;

import com.tranhuan.peopledb.annotation.Id;
import com.tranhuan.peopledb.annotation.MultiSQL;
import com.tranhuan.peopledb.annotation.SQL;
import com.tranhuan.peopledb.model.CrudOperation;
import com.tranhuan.peopledb.model.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

abstract class CRUDRepository<T extends Entity> {
    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }


    public T save(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.SAVE, this::getSaveSql), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                long id = rs.getLong(1);
                setIdByAnnotation(id, entity);
            }
            System.out.println(recordAffected);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    public Optional<T> findById(Long id) {
        T entity = null;
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.FIND_BY_ID, this::getFindByIdSql));
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entity = extractEntityFromResultSets(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(entity);
    }

    public List<T> findAll() {
        List<T> entities =new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.FIND_ALL, this::getFindAllSql));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entities.add(extractEntityFromResultSets(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }



    public long count() {
        long cnt = 0;

        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.COUNT,this::getCountSql));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cnt = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cnt;
    }

    public void delete(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.DELETE_ONE, this::getDeleteSql));
            ps.setLong(1, getIdByAnnotation(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(T...entity) {
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(entity).map(e -> getIdByAnnotation(e)).map(String::valueOf).collect(joining(","));
            int affected = stmt.executeUpdate(getSaveSqlByAnnotation(CrudOperation.DELETE_MANY, this::getDeleteInSql).replace(":ids", ids));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSqlByAnnotation(CrudOperation.UPDATE, this::getUpdateSql));
            mapForUpdate(entity, ps);
            ps.setLong(5, getIdByAnnotation(entity));
            int i = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String getSaveSqlByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter) {
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap((mtsql -> Arrays.stream(mtsql.value())));


        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));
        return Stream.concat(multiSqlStream, sqlStream)
                .filter(a -> a.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);
    }

    private void setIdByAnnotation(Long id, T entity) {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .forEach(
                        f -> {
                            f.setAccessible(true);
                            try {
                                f.set(entity, id);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Unable to set ID field value.");
                            }
                        }
                );
    }

    private Long getIdByAnnotation(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(f -> {
                    f.setAccessible(true);
                    Long id;
                    try {
                        id = (long) f.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return id;
                })
                .findFirst().orElseThrow(() -> new RuntimeException("No ID annotated field found"));
    }


    protected String getUpdateSql(){return "";}

    protected  String getDeleteInSql() {return "";}

    protected  String getDeleteSql() {return "";}

    protected  String getCountSql() {return "";}


    protected  String getFindByIdSql() {return "";}
    protected  String getFindAllSql() {return "";}
    private String getSaveSql(){
        return "";}


    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;
    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;
    abstract T extractEntityFromResultSets(ResultSet rs) throws SQLException;




}
