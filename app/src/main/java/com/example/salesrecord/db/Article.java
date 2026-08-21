package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

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
    public Double totalcount;
    public Double currcount;
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

                   Double totalcount, Double currcount, Integer isopen, Integer artipo,
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

    public Article(Article other) {

        if (other != null) {
            this.uid = other.uid;
            this.article = other.article;
            this.nombre = other.nombre;
            this.descr = other.descr;
            this.iddesde = other.iddesde;
            this.image = other.image;

            this.precund = other.precund;
            this.precpq = other.precpq;
            this.preccj = other.preccj;
            this.margen = other.margen;

            this.totalcount = other.totalcount;
            this.currcount = other.currcount;
            this.isopen = other.isopen;
            this.artipo = other.artipo;
            this.metrica = other.metrica;
            this.caduca = other.caduca;
            this.staus = other.staus;

            this.ultfec = other.ultfec;
            this.fecha = other.fecha;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article artA = (Article) o;
        return uid == artA.uid &&
                Objects.equals(article, artA.article) &&
                Objects.equals(nombre, artA.nombre) &&
                Objects.equals(descr, artA.descr) &&
                Objects.equals(iddesde, artA.iddesde) &&
                Objects.equals(image, artA.image) &&
                Objects.equals(precund, artA.precund) &&
                Objects.equals(precpq, artA.precpq) &&
                Objects.equals(preccj, artA.preccj) &&
                Objects.equals(margen, artA.margen) &&
                Objects.equals(totalcount, artA.totalcount) &&
                Objects.equals(currcount, artA.currcount) &&
                Objects.equals(isopen, artA.isopen) &&
                Objects.equals(artipo, artA.artipo) &&
                Objects.equals(metrica, artA.metrica) &&
                Objects.equals(caduca, artA.caduca) &&
                Objects.equals(staus, artA.staus) &&
                Objects.equals(ultfec, artA.ultfec) &&
                Objects.equals(fecha, artA.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, article, nombre, descr, iddesde, image,
                precund, precpq, preccj, margen,
                totalcount, currcount, isopen, artipo,
                metrica, caduca, staus, ultfec, fecha);
    }
}
