package com.example.salesrecord.utls;

import java.io.Serializable;

public class Obj implements Serializable {

    public String name;
    public String desc;
    public String img;

    public Integer startDate = 0;
    public Integer endDate = 0;
    public Integer currDate = 0;

    public Integer currCount = 0;
    public Integer maxCount = 0;
    public Integer minCount = 0;
    public Long id;

    public Obj(String name, String desc, String img, Long id) {
        this.name = name;
        this.desc = desc;
        this.img = img;

        this.id = id;
    }
}