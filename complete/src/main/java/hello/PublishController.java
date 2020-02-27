package hello;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.DigestUtils;

import java.sql.*;
import java.util.Map;

import org.apache.iotdb.jdbc.IoTDBSQLException;

@RestController
public class PublishController {

    private static final String salt = "&%12345***&&%%$$#@1";

    @RequestMapping("/publish")
    public List<Map<String, Object>> publish(
            @RequestParam(value="url", defaultValue = "jdbc:iotdb://127.0.0.1:6667/") String url,
            @RequestParam(value="username", defaultValue = "root") String username,
            @RequestParam(value="password", defaultValue = "root") String password,
            @RequestParam(value="database") String database,
            @RequestParam(value="timeseries") String timeseries,
            @RequestParam(value="columns") String columns,
            @RequestParam(value="starttime", required = false) String starttime,
            @RequestParam(value="endtime", required = false) String endtime,
            @RequestParam(value="amount", required = false) Long amount,
            @RequestParam(value="format", defaultValue = "map") String format
    ) throws SQLException {
        url = url.replace("\"", "");
        username = username.replace("\"", "");
        password = password.replace("\"", "");
        database = database.replace("\"", "");
        timeseries = timeseries.replace("\"", "");
        columns = columns.replace("\"", "");
        starttime = starttime == null ? null : starttime.replace("\"", "");
        endtime = endtime == null ? null : endtime.replace("\"", "");
        format = format.replace("\"", "");

        String innerUrl = "jdbc:postgresql://192.168.10.172:5432/";
        String innerUserName = "postgres";
        String innerPassword = "1111aaaa";

        // iotdb is . tsdb is _
        String L0tableName = "L0" + "_M" + DigestUtils.md5DigestAsHex(String.format("%s,%s,%s,%s,%s", url, database, timeseries, columns, salt).getBytes()).substring(0,8);;
        String L1tableName = "L1" + "_M" + DigestUtils.md5DigestAsHex(String.format("%s,%s,%s,%s,%s", url, database, L0tableName, columns, salt).getBytes()).substring(0,8);
        String L2tableName = "L2" + "_M" + DigestUtils.md5DigestAsHex(String.format("%s,%s,%s,%s,%s", url, database, L1tableName, columns, salt).getBytes()).substring(0,8);

        String[] tables = new String[3];
        tables[0] = L2tableName;
        tables[1] = L1tableName;
        tables[2] = L0tableName;

        List<Map<String, Object>> res = null;
        for(String tableName : tables){
            System.out.println(tableName);
            res = new DataPointController().dataPoints(
                    innerUrl, innerUserName, innerPassword, database.replace(".", "_"), tableName, columns, starttime, endtime, null, null, "map", null, null, "pg");
            System.out.println(tableName);
            if(res.size() >= amount) {
                break;
            }
        }
        return res;
//        if(format.equals("map")) return res;
//        List<Map<String, Object>> result = new LinkedList<>();
//        for(Map<String, Object> map : res){
//            Object time = map.get("Time");
//            for(Map.Entry<String, Object> entry : map.entrySet()){
//                String mapKey = entry.getKey();
//                if(mapKey.equals("Time")) continue;
//                Map<String, Object> m = new HashMap<>();
//                m.put("time", time);
//                m.put("label", mapKey);
//                m.put("value", entry.getValue());
//                result.add(m);
//            }
//        }
//        return result;
    }
}