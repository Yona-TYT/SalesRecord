package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Sale {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String sale;         //ID en string
    public String cliente;      //Nombre del cliente (opcional)
    public String artclist;     //Lista de producto ids en string
    public String countlist;     //Lista de cantidad de productos en string
    public String pricelist;
    public String marglist;

    public Double monto;        //Monto total de la venta
    public Double tasa;        //Tasa al momento de realizar la venta

    public Integer status;      // Pendiente, en proceso, completada
    public String imagen;
    public Long time;

    public String cltid;

    public Integer cltnr;
    public String more5;

    public Long fecha;

    public Sale(@NonNull String sale, String cliente, String artclist, String countlist, String pricelist,
                    String marglist, Double monto, Double tasa, Integer status, String imagen, Long time,
                    String cltid, Integer cltnr, String more5, Long fecha
                )
    {
        this.sale = sale;
        this.cliente = cliente;
        this.artclist = artclist;
        this.countlist = countlist;
        this.pricelist = pricelist;
        this.marglist = marglist;
        this.monto = monto;
        this.tasa = tasa;
        this.status = status;
        this.imagen = imagen;
        this.time = time;
        this.cltid = cltid;
        this.cltnr = cltnr;
        this.more5 = more5;
        this.fecha = fecha;

    }

    public Sale(Sale other) {
        if (other != null) {
            this.uid = other.uid;
            this.sale = other.sale;
            this.cliente = other.cliente;
            this.artclist = other.artclist;
            this.countlist = other.countlist;
            this.pricelist = other.pricelist;
            this.marglist = other.marglist;
            this.monto = other.monto;
            this.tasa = other.tasa;
            this.status = other.status;
            this.imagen = other.imagen;
            this.time = other.time;
            this.cltid = other.cltid;
            this.cltnr = other.cltnr;
            this.more5 = other.more5;
            this.fecha = other.fecha;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale1 = (Sale) o;
        return uid == sale1.uid &&
                Objects.equals(sale, sale1.sale) &&
                Objects.equals(cliente, sale1.cliente) &&
                Objects.equals(artclist, sale1.artclist) &&
                Objects.equals(countlist, sale1.countlist) &&
                Objects.equals(pricelist, sale1.pricelist) &&
                Objects.equals(marglist, sale1.marglist) &&
                Objects.equals(monto, sale1.monto) &&
                Objects.equals(tasa, sale1.tasa) &&
                Objects.equals(status, sale1.status) &&
                Objects.equals(imagen, sale1.imagen) &&
                Objects.equals(time, sale1.time) &&
                Objects.equals(cltid, sale1.cltid) &&
                Objects.equals(cltnr, sale1.cltnr) &&
                Objects.equals(more5, sale1.more5) &&
                Objects.equals(fecha, sale1.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, sale, cliente, artclist, countlist, pricelist, marglist,
                monto, tasa, status, imagen, time, cltid, cltnr, more5, fecha);
    }
}
