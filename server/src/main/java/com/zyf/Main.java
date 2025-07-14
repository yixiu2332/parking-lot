package com.zyf;

import com.zyf.dao.ParkingDB;


import java.sql.Connection;
import java.sql.SQLException;



//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws SQLException {
        Connection connection = ParkingDB.getConnection();
        System.out.println(connection);
    }
}