package com.revature.models;

import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.Table;

@Table(tableName = "Test")
public class Test {
    @Column(columnName = "Id")
    private int id;
    @Column(columnName = "Name")
    private String name;

}
