package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.salesrecord.db.Deuda;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.Pagos;

import java.util.List;

@Dao
public interface DaoPay extends GenericDao<Pagos>{
    @Query("SELECT * FROM Pagos")
    List<Pagos> getUsers();

    // Insertar una lista completa (grupoId ya está en cada objeto Pgos)
    @Insert
    void insertAll(List<Pagos> pagos);

    // Recuperar una lista específica por grupoId
    @Query("SELECT * FROM Pagos WHERE accid = :accid ORDER BY uid ASC")
    List<Pagos> getListByGroupId(String accid);  // Cambiado a String

    @Query("SELECT * FROM Pagos WHERE cltid = :cltid AND accid = :accid ORDER BY uid ASC")
    List<Pagos> getListByCltAndAcc(String cltid, String accid);

    @Query("SELECT * FROM Pagos WHERE pago= :user")
    Pagos getUsers(String user);

    @Insert
    void insertUser(Pagos...registros);

    @Update
    void update(Pagos pago);

    @Query("UPDATE Pagos SET nombre= :nombre, concep= :concep, monto= :monto, oper= :oper, porc= :porc, imagen= :imagen, fecha= :fecha, time= :time, cltid= :cltid, accid= :accid, more4= :more4, more5= :more5 WHERE pago= :user")
    void updateUser(String user, String nombre, String concep, Double monto, Integer oper, Integer porc, String imagen, String fecha, String time , String cltid, String accid, Integer more4, String more5 );

    // Para actualizar valores individuales --------------------------------------------------------
    @Query("UPDATE Pagos SET concep= :concep, monto= :monto, oper= :oper, porc= :porc, imagen= :imagen  WHERE pago= :user")
    void updatePay(String user, String concep, Double monto, Integer oper, Integer porc, String imagen);

    //----------------------------------------------------------------------------------------------

    @Query("DELETE FROM Pagos WHERE  pago= :user")
    void removerUser(String user);

    @Query("DELETE FROM Pagos WHERE  uid= :uid")
    void removerUser(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertUser(Pagos user);
}

