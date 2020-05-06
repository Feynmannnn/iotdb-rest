package hello.refactor.sampling;

import hello.refactor.obj.Bucket;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class M4 implements Operator  {
    @Override
    public List<Map<String, Object>> sample(List<Bucket> buckets, String timelabel, String label, String format) {
        List<Map<String, Object>> res = new LinkedList<>();

        long st = System.currentTimeMillis();
        System.out.println("m4sample started");

        for(Bucket bucket : buckets){
            List<Map<String, Object>> datapoints = bucket.getDataPoints();
            if(datapoints.size() <= 4){
                res.addAll(datapoints);
                continue;
            }

            res.add(datapoints.get(0));
            Map<String, Object> maxi = datapoints.get(0);
            Map<String, Object> mini = datapoints.get(0);
            for(int i = 1; i < datapoints.size()-1; i++){
                Map<String, Object> candi = datapoints.get(i);
                Object value = candi.get(label);
                if(value instanceof Double) maxi = (Double) value >= (Double)maxi.get(label) ? candi : maxi;
                else if(value instanceof Integer) maxi = (Integer) value >= (Integer)maxi.get(label) ? candi : maxi;
                else if(value instanceof Long) maxi = (Long) value >= (Long)maxi.get(label) ? candi : maxi;
                else maxi = (Double) value >= (Double)maxi.get(label) ? candi : maxi;

                if(value instanceof Double) mini = (Double) value <= (Double)mini.get(label) ? candi : mini;
                else if(value instanceof Integer) mini = (Integer) value <= (Integer)mini.get(label) ? candi : mini;
                else if(value instanceof Long) mini = (Long) value <= (Long)mini.get(label) ? candi : mini;
                else mini = (Double) value <= (Double)mini.get(label) ? candi : mini;
            }
            res.add(maxi);
            res.add(mini);
            res.add(datapoints.get(datapoints.size()-1));
        }
        System.out.println("m4sample used time: " + (System.currentTimeMillis() - st) + "ms");

        return res;
    }
}
