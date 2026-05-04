package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Fecha {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String fecha;
    public String year;
    public String mes;
    public String dia;
    public String hora;
    public String date;

    public Fecha(@NonNull String fecha, String year, String mes, String dia, String hora, String date)
    {
            this.fecha = fecha;
            this.year = year;
            this.mes = mes;
            this.dia = dia;
            this.hora = hora;
            this.date = date;
    }

    // Getter requerido para el sorting (y otros accesos)
    public String getDate() {
        return date;
    }
}
