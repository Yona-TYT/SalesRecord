package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String cliente;
    public String nombre;
    public String iduser;
    public String defaulacc;
    public Integer priority;
    public Long fecha;
    public Float level;
    public Long ulfech;
    public Integer status;
    public Integer count;

    public Cliente(@NonNull String cliente, String nombre, String iduser, String defaulacc,
                   Integer priority, Long fecha, Float level, Long ulfech,
                   Integer status, Integer count
            )
    {
        this.cliente = cliente;
        this.nombre = nombre;
        this.iduser = iduser;
        this.defaulacc = defaulacc;
        this.priority = priority;
        this.fecha = fecha;
        this.level = level;
        this.ulfech = ulfech;
        this.status = status;
        this.count = count;
    }
}
