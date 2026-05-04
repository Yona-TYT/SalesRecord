package com.example.salesrecord.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.salesrecord.db.Deuda;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    void insert(Deuda usuario);

    @Query("SELECT * FROM deuda")
    List<Deuda> getAllUsuarios();
}
