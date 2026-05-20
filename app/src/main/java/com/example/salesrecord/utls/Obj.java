package com.example.salesrecord.utls;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Obj implements Serializable, Parcelable {

    public String strId;
    public String name;
    public String desc;
    public String img;

    public int startDate = 0;
    public int endDate = 0;
    public int currDate = 0;
    public int click = 0;
    public int unit = 0;

    public float currCount = 0;
    public float maxCount = 0;
    public float minCount = 0;
    public float saleCount = 0;

    public double price = 0.0;

    public long id;

    public Obj(String strId, String name, String desc, String img, int click, int unit, float currCount, float maxCount, float saleCount, double price, Long id) {
        this.strId = strId;
        this.name = name;
        this.desc = desc;
        this.img = img;

        this.click = click;
        this.unit = unit;

        this.currCount = currCount;
        this.maxCount = maxCount;
        this.saleCount = saleCount;

        this.price = price;

        this.id = id;
    }

    protected Obj(Parcel in) {
        name = in.readString();
        desc = in.readString();
        img = in.readString();
        click = in.readInt();
        unit = in.readInt();
        startDate = in.readInt();
        endDate = in.readInt();
        currDate = in.readInt();
        currCount = in.readFloat();
        maxCount = in.readFloat();
        minCount = in.readFloat();
        saleCount = in.readFloat();
        price = in.readDouble();
        id = in.readLong();
    }

    public static final Creator<Obj> CREATOR = new Creator<Obj>() {
        @Override
        public Obj createFromParcel(Parcel in) {
            return new Obj(in);
        }

        @Override
        public Obj[] newArray(int size) {
            return new Obj[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(img);
        dest.writeInt(click);
        dest.writeInt(unit);
        dest.writeInt(startDate);
        dest.writeInt(endDate);
        dest.writeInt(currDate);
        dest.writeFloat(currCount);
        dest.writeFloat(maxCount);
        dest.writeFloat(minCount);
        dest.writeFloat(saleCount);
        dest.writeDouble(price);
        dest.writeLong(id);
    }
}