package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Deuda {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String deuda;
    public String accid;
    public String cltid;
    public Double rent;
    public Integer total;
    public Integer porc;
    public String fecha;
    public Integer estat;
    public Integer pagado;
    public String ulfech;
    public Integer oper;
    public Double remnant;
    public String disabfec;

    public Deuda(@NonNull String deuda, String accid, String cltid, Double rent, Integer total, Integer porc,
                 String fecha, Integer estat, Integer pagado, String ulfech, Integer oper, Double remnant, String disabfec)
    {
        this.deuda = deuda;
        this.accid = accid;
        this.cltid = cltid;
        this.rent = rent;
        this.total = total;
        this.porc = porc;
        this.fecha = fecha;
        this.estat = estat;
        this.pagado = pagado;
        this.ulfech = ulfech;
        this.oper = oper;
        this.remnant = remnant;
        this.disabfec = disabfec;
    }
}
