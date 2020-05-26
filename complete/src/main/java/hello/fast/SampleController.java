package hello.fast;

import hello.fast.obj.Bucket;
import hello.fast.sampling.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.*;

@RestController
public class SampleController {
    @RequestMapping("/sample")
    public List<Map<String, Object>> dataPoints(
            @RequestParam(value="url", defaultValue = "jdbc:iotdb://127.0.0.1:6667/") String url,
            @RequestParam(value="username", defaultValue = "root") String username,
            @RequestParam(value="password", defaultValue = "root") String password,
            @RequestParam(value="database") String database,
            @RequestParam(value="timeseries") String timeseries,
            @RequestParam(value="columns") String columns,
            @RequestParam(value="starttime", required = false) String starttime,
            @RequestParam(value="endtime", required = false) String endtime,
            @RequestParam(value="conditions", required = false) String conditions,
            @RequestParam(value="query", required = false) String query,
            @RequestParam(value="format", defaultValue = "map") String format,
            @RequestParam(value="ip", required = false) String ip,
            @RequestParam(value="port", required = false) String port,
            @RequestParam(value="amount", required = false) Integer amount,
            @RequestParam(value="dbtype", defaultValue = "iotdb") String dbtype,
            @RequestParam(value="sample", defaultValue = "m4") String sample,
            @RequestParam(value="percent", required = false) Double percent,
            @RequestParam(value="alpha", required = false) Double alpha
    ) throws Exception {

        url = url.replace("\"", "");
        username = username.replace("\"", "");
        password = password.replace("\"", "");
        database = database.replace("\"", "");
        timeseries = timeseries.replace("\"", "");
        columns = columns.replace("\"", "");
        starttime = starttime == null ? null : starttime.replace("\"", "");
        endtime = endtime == null ? null :endtime.replace("\"", "");
        conditions = conditions == null ? null : conditions.replace("\"", "");
        format = format.replace("\"", "");
        dbtype = dbtype.replace("\"", "");
        sample = sample.replace("\"", "");
        ip = ip == null ? null : ip.replace("\"", "");
        port = port == null ? null : port.replace("\"", "");
        query = query == null ? null : query.replace("\"", "");

        return _samplePoints(url, username, password, database, timeseries, columns, starttime, endtime, conditions, query, format, sample, ip, port, amount, dbtype, percent, alpha);
    }

    static List<Map<String, Object>> _samplePoints(
            String url,
            String username,
            String password,
            String database,
            String timeseries,
            String columns,
            String starttime,
            String endtime,
            String conditions,
            String query,
            String format,
            String sample,
            String ip,
            String port,
            Integer amount,
            String dbtype,
            Double percent,
            Double alpha) throws SQLException {

        // 先根据采样算子分桶，"simpleXXX"为等间隔分桶，否则为自适应分桶
        List<Bucket> buckets =
            sample.contains("simple") ?
            BucketsController._intervals(url, username, password, database, timeseries, columns, starttime, endtime, conditions, query, format, ip, port, amount, dbtype) :
            BucketsController._buckets(url, username, password, database, timeseries, columns, starttime, endtime, conditions, query, format, ip, port, amount, dbtype, percent, alpha);

        SamplingOperator samplingOperator;

        if(sample.contains("agg")) samplingOperator = new Aggregation();
        else if(sample.contains("sample")) samplingOperator = new Sample();
        else if(sample.contains("outlier")) samplingOperator = new Outlier();
        else samplingOperator = new M4();

        String iotdbLabel = database + "." + timeseries + "." +columns;
        String label = dbtype.equals("iotdb") ? iotdbLabel : columns;
        String timelabel = "time";

        List<Map<String, Object>> res =  samplingOperator.sample(buckets, timelabel, label);

        if(format.equals("map")) return res;
        List<Map<String, Object>> result = new LinkedList<>();
        for(Map<String, Object> map : res){
            Object time = map.get("time");
            for(Map.Entry<String, Object> entry : map.entrySet()){
                String mapKey = entry.getKey();
                if(mapKey.equals("time")) continue;
                Map<String, Object> m = new HashMap<>();
                m.put("time", time);
                m.put("label", mapKey);
                m.put("value", entry.getValue());
                result.add(m);
            }
        }
        return result;
    }

    static List<Map<String, Object>> _samplePoints(List<Bucket> buckets, String timelabel, String label, String sample){
        SamplingOperator samplingOperator;

        if(sample.contains("agg")) samplingOperator = new Aggregation();
        else if(sample.contains("sample")) samplingOperator = new Sample();
        else if(sample.contains("outlier")) samplingOperator = new Outlier();
        else samplingOperator = new M4();

        return samplingOperator.sample(buckets, timelabel, label);
    }
}