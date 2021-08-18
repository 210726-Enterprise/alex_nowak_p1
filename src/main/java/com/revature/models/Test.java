package com.revature.models;

import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(tableName = "TEST")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class Test {
    @Column(columnName = "ID")
    @PrimaryKey
    private int id;
    @Column(columnName = "NAME")
    private String name;

}
