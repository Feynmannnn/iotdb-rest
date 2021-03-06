package hello.fast.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * IoTDB数据库连接操作类
 */
public class IoTDBConnection {
    public static Connection getConnection(String url, String username, String password) {
        // JDBC driver name and database URL
        String driver = "org.apache.iotdb.jdbc.IoTDBDriver";

        Connection connection = null;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
