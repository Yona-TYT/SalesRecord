package com.example.salesrecord.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoDat;
import com.example.salesrecord.db.dao.DaoDeb;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.db.dao.QueueItemDao;

@Database(
        entities = {Article.class, Conf.class, Cliente.class, Deuda.class, Fecha.class, Sale.class, QueueItem.class},
        version = 1,
        exportSchema = false  // Opcional: evita exportar el esquema en builds de debug
)
public abstract class AllDao extends RoomDatabase {
    public abstract DaoArt daoAtr();

    public abstract DaoCfg daoCfg();

    public abstract DaoClt daoClt();

    public abstract DaoDeb daoDeb();

    public abstract DaoDat daoDat();

    public abstract DaoSal daoSal();

    public abstract QueueItemDao daoQueue();

}
