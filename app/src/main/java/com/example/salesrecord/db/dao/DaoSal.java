package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.salesrecord.db.Sale;

import java.util.List;

@Dao
public interface DaoSal extends GenericDao<Sale>{
    @Query("SELECT * FROM Article WHERE uid = :uid LIMIT 1")
    Sale getUser(long uid);

    @Query("SELECT * FROM Sale")
    List<Sale> getUsers();

    // Insertar una lista completa (grupoId ya está en cada objeto Pgos)
    @Insert
    void insertAll(List<Sale> sales);


    @Query("SELECT * FROM Sale WHERE sale= :user")
    Sale getUsers(String user);

    @Insert
    void insertUser(Sale...sale);

    @Update
    void update(Sale pago);

    @Query("UPDATE Sale SET cliente= :nombre, artclist= :concep, monto= :monto, tasa= :oper, status= :porc, imagen= :imagen, fecha= :fecha, time= :time, cltid= :cltid, more4= :more4, more5= :more5 WHERE sale= :user")
    void updateUser(String user, String nombre, String concep, Double monto, Integer oper, Integer porc, String imagen, String fecha, String time , String cltid, Integer more4, String more5 );

    // Para actualizar valores individuales --------------------------------------------------------
    @Query("UPDATE Sale SET artclist= :concep, monto= :monto, tasa= :oper, status= :porc, imagen= :imagen  WHERE sale= :user")
    void updatePay(String user, String concep, Double monto, Integer oper, Integer porc, String imagen);

    //----------------------------------------------------------------------------------------------

    @Query("DELETE FROM Sale WHERE  sale= :user")
    void removerUser(String user);

    @Query("DELETE FROM Sale WHERE  uid= :uid")
    void removerUser(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertUser(Sale user);
}

