package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;

import java.util.List;

@Dao
public interface DaoClt extends GenericDao<Cliente>{
    @Query("SELECT * FROM cliente")
    List<Cliente> getUsers();

    @Query("SELECT * FROM cliente WHERE cliente= :user")
    Cliente getUsers(String user);

    // Se obtinen valores individuales de nombre, alias, bits------------------------------------
    @Query("SELECT nombre FROM cliente WHERE cliente= :user ")
    String getSaveName(String user);

    @Query("SELECT iduser FROM cliente WHERE cliente= :user ")
    String getSaveAlias(String user);

    @Insert
    void insertUser(Cliente...clientes);

    @Update
    void update(Cliente cliente);

    @Query("UPDATE cliente SET nombre= :nombre, iduser= :alias, defaulacc= :defaulacc, priority= :priority, fecha= :fecha, level= :level, ulfech= :ulfech WHERE cliente= :user")
    void updateUser(String user, String nombre, String alias, String defaulacc, Integer priority, Long fecha, Float level, Long ulfech);

    @Query("UPDATE cliente SET  ulfech= :ulfech WHERE cliente= :user")
    void updateUltfech(String user, String ulfech);

    @Query("UPDATE cliente SET  nombre= :nombre, iduser= :alias WHERE cliente= :user")
    void updateNames(String user, String nombre, String alias);

    @Query("DELETE FROM cliente WHERE  cliente= :user")
    void removerUser(String user);

    @Query("DELETE FROM cliente WHERE  uid= :uid")
    void removerUser(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertUser(Cliente user);
}

