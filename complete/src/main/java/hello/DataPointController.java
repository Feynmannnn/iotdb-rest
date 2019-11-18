package hello;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import org.apache.iotdb.jdbc.IoTDBSQLException;

@RestController
public class DataPointController {

    @RequestMapping("/data")
    public List<Map<String, Object>> dataPoints(
            @RequestParam(value="database") String database,
            @RequestParam(value="timeseries") String timeseries,
            @RequestParam(value="columns") String columns,
            @RequestParam(value="starttime", required = false) Long starttime,
            @RequestParam(value="endtime", required = false) Long endtime,
            @RequestParam(value="conditions", required = false) String conditions
        ) throws SQLException {
        Connection connection = IoTDBConnection.getConnection();
        if (connection == null) {
            System.out.println("get connection defeat");
            return null;
        }
        long stime = System.currentTimeMillis();
        Statement statement = connection.createStatement();
        String sql = "SELECT " + columns.replace("\"", "") +
                     " FROM " + database.replace("\"", "") + "." + timeseries.replace("\"", "") +
                     (starttime == null ? "" : " WHERE time >= " + starttime) +
                     (endtime   == null ? "" : " AND time <= " + endtime) +
                     (conditions    == null ? "" : conditions.replace("\"", ""));
        ResultSet resultSet = statement.executeQuery(sql);
        System.out.println("exec used time: " + (System.currentTimeMillis() - stime) + "ms");
        List<Map<String, Object>> res = new LinkedList<>();
        if (resultSet != null) {
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                for(int i = 1; i <= columnCount; i++){
                    int type = metaData.getColumnType(i);
                    String label = metaData.getColumnLabel(i);
                    if(Types.INTEGER == type) map.put(label, resultSet.getInt(i));
                    else if(Types.BIGINT == type) map.put(label, resultSet.getLong(i));
                    else if(Types.BOOLEAN == type) map.put(label, resultSet.getString(i));
                    else if(Types.FLOAT == type) map.put(label, resultSet.getFloat(i));
                    else if(Types.DOUBLE == type) map.put(label, resultSet.getDouble(i));
                    else if(Types.DATE == type) map.put(label, resultSet.getDate(i));
                    else if(Types.TIME == type) map.put(label, resultSet.getTime(i));
                    else if(Types.TIMESTAMP == type) map.put(label, resultSet.getTimestamp(i));
                    else map.put(label, resultSet.getString(i));
                }
                res.add(map);
            }
        }
        statement.close();
        connection.close();
        System.out.println("used time: " + (System.currentTimeMillis() - stime) + "ms");
        return res;
    }
}
