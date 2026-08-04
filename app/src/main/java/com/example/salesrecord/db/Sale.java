package com.example.salesrecord.db;

import io.reactivex.annotations.NonNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    public Integer more4;
    public String more5;

    public Long fecha;

    public Sale(@NonNull String sale, String cliente, String artclist, String countlist, String pricelist,
                    String marglist, Double monto, Double tasa, Integer status, String imagen, Long time,
                    String cltid, Integer more4, String more5, Long fecha
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
        this.more4 = more4;
        this.more5 = more5;
        this.fecha = fecha;

    }
}
