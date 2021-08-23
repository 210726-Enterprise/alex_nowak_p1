package com.revature.models;

import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;

@Table(tableName = "tester")
public class Tester {
    @Column(columnName = "testerid")
    @PrimaryKey
    private int id;
    @Column(columnName = "testername")
    private String name;

    public Tester(){
        id = 0;
        name = "";
    }

    public Tester(int id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Tester{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
