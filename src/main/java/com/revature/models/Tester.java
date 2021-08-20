package com.revature.models;

import com.revature.orm.annotations.Column;
import com.revature.orm.annotations.PrimaryKey;
import com.revature.orm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(tableName = "tester")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class Tester {
    @Column(columnName = "testerid")
    @PrimaryKey
    private int id;
    @Column(columnName = "testername")
    private String name;

    @Override
    public String toString() {
        return "Tester{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
