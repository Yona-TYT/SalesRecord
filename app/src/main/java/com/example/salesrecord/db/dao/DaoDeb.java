package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.salesrecord.db.Deuda;

import java.util.List;

@Dao
public interface DaoDeb extends GenericDao<Deuda>{
    @Query("SELECT * FROM deuda")
    List<Deuda> getUsers();

    @Query("SELECT * FROM deuda WHERE deuda= :user")
    Deuda getUsers(String user);

    // Recuperar una lista específica por grupoId
    @Query("SELECT * FROM Deuda WHERE cltid = :cltid ORDER BY uid ASC")
    List<Deuda> getListByGroupId(String cltid);

    @Query("SELECT * FROM Deuda WHERE cltid = :cltId AND accid = :accId LIMIT 1")
    Deuda getUserByCltAndAcc(String cltId, String accId);

    // Se obtinen valores individuales de accselc, moneda, dolar------------------------------------

    @Insert
    void insertUser(Deuda...deudas);

    @Update
    void update(Deuda deuda);

    @Query("UPDATE deuda SET accid= :accid, cltid= :cltid, rent= :rent, total= :total, porc= :porc, fecha= :fecha, estat= :estat, pagado= :pagado, ulfech= :ulfech, oper= :oper, remnant= :remnant WHERE deuda= :user")
    void updateUser(String user, String accid, String cltid, Double rent, Integer total, Integer porc, String fecha, Integer estat, Integer pagado, String ulfech, Integer oper, Double remnant);

    @Query("UPDATE deuda SET  pagado= :pagado, ulfech= :ulfech, remnant= :remnant WHERE deuda= :user")
    void updateDebt(String user, Integer pagado, String ulfech, Double remnant);

    @Query("UPDATE deuda SET  rent= :rent, estat= :estat, ulfech= :ulfech, oper= :oper, disabfec= :disabfec WHERE deuda= :user")
    void updateFormCltWin(String user, Double rent, Integer estat, String ulfech, Integer oper, String disabfec);

    @Query("UPDATE deuda SET total= :total, porc= :porc, pagado= :pagado, ulfech= :ulfech, oper= :oper, remnant= :remnant  WHERE deuda= :user")
    void updateFormPayWin(String user, Integer total, Integer porc, Integer pagado, String ulfech, Integer oper, Double remnant);

    @Query("DELETE FROM deuda WHERE  deuda= :user")
    void removerUser(String user);

    @Query("DELETE FROM deuda WHERE  uid= :uid")
    void removerUser(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertUser(Deuda user);
}

