package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Article {
    @PrimaryKey(autoGenerate = true)

    public long uid;

    // Identificadores y Textos
    public String article;    // ID único
    public String nombre;
    public String descr;
    public String iddesde;    // ID origen (paquete/caja)
    public String image;

    // Precios y Margen (Double)
    public Double precund;
    public Double precpq;
    public Double preccj;
    public Double margen;

    // Cantidades y Estados (Integer)
    public Float totalcount;
    public Float currcount;
    public Integer isopen;    // 0 = cerrado, 1 = abierto
    public Integer artipo;    // unidad, paquete o caja
    public Integer metrica;   // Kg, L, etc.
    public Integer caduca;    // Días para caducar
    public Integer staus;

    // Fechas (Long - Timestamps)
    public Long ultfec;       // Última reposición
    public Long fecha;        // Fecha creación

    // Constructor corregido y organizado
    public Article(@NonNull String article, String nombre, String descr, String iddesde, String image,
                   Double precund, Double precpq, Double preccj, Double margen,

                   Float totalcount, Float currcount, Integer isopen, Integer artipo,
                   Integer metrica, Integer caduca, Integer staus,

                   Long ultfec, Long fecha) {

        this.article = article;
        this.nombre = nombre;
        this.descr = descr;
        this.iddesde = iddesde;
        this.image = image;

        this.precund = precund;
        this.precpq = precpq;
        this.preccj = preccj;
        this.margen = margen;

        this.totalcount = totalcount;
        this.currcount = currcount;
        this.isopen = isopen;
        this.artipo = artipo;
        this.metrica = metrica;
        this.caduca = caduca;
        this.staus = staus;

        this.ultfec = ultfec;
        this.fecha = fecha;
    }
}
