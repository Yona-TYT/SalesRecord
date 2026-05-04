package com.example.salesrecord.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import io.reactivex.annotations.NonNull;

@Entity
public class Conf {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String config;
    public String version;
    public String hexid;
    public String date;
    public String time;
    public Integer curr;
    public Double dolar;
    public Integer moneda;
    public Integer mes;
    public Integer show;

    public Conf(@NonNull String config, String version, String hexid, String date, String time, Integer curr, Double dolar, Integer moneda, Integer mes, Integer show) {
        this.config = config;
        this.version = version;
        this.hexid = hexid;
        this.date = date;
        this.time = time;
        this.curr = curr;
        this.dolar = dolar;
        this.moneda = moneda;
        this.mes = mes;
        this.show = show;
    }
}
