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
    public String alias;
    public String defaulacc;
    public Integer priority;
    public String fecha;
    public Float level;
    public String ulfech;
    public Integer oper;
    public String debe;
    public String bits;

    public Cliente(@NonNull String cliente, String nombre, String alias, String defaulacc,
                   Integer priority, String fecha, Float level, String ulfech,
                   Integer oper, String bits
            )
    {
        this.cliente = cliente;
        this.nombre = nombre;
        this.alias = alias;
        this.defaulacc = defaulacc;
        this.priority = priority;
        this.fecha = fecha;
        this.level = level;
        this.ulfech = ulfech;
        this.oper = oper;
        this.bits = bits;
    }
}
