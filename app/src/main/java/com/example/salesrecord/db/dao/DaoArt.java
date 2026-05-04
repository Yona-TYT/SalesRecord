package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.salesrecord.db.Article;

import java.util.List;

@Dao
public interface DaoArt extends GenericDao<Article>{

    // Nuevo: @Insert simple con parámetro directo (para inferencia)
    @Insert
    void insert(Article article);  // Room infiere Deuda del parámetro

    //-------------------------------------------------------------------------------------


    @Query("SELECT * FROM Article")
    List<Article> getUsers();

    @Query("SELECT * FROM Article WHERE article= :user")
    Article getUsers(String user);

    //----------------------------------------------------------------------------------------------

    @Insert
    void insertUser(Article... articles);

    @Update
    void update(Article article);

    @Query("UPDATE Article SET nombre = :nombre, descr = :descr, precund = :precund, " +
            "margen = :margen, artipo = :artipo, ultfec = :ultfec " +
            "WHERE article = :articleId")
    void updateArticleBasicInfo(String articleId, String nombre, String descr,
                                Double precund, Double margen, Integer artipo, Long ultfec);

//    @Query("UPDATE Article SET nombre= :nombre, descr= :descr, monto= :monto, acctipo= :acctipo WHERE article= :user")
//    void updateAccount(String user, String nombre, String descr, String monto, Integer acctipo);
//
//    @Query("UPDATE Article SET fecselc= :fecselc,  accselc= :accselc, moneda= :moneda, dolar= :dolar, ultfec= :ultfec WHERE article= :user")
//    void updateData(String user, Integer fecselc, Integer accselc, Integer moneda, String dolar, String ultfec);

    //----------------------------------------------------------------------------------------------

    @Query("DELETE FROM Article WHERE  article= :user")
    void removerUser(String user);

    @Query("DELETE FROM Article WHERE  uid= :uid")
    void removerUser(long uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void  insertUser(Article user);
}
