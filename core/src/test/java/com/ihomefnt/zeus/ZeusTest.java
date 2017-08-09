package com.ihomefnt.zeus;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.ihomefnt.zeus.util.JsonUtils;
import org.junit.Test;

import com.ihomefnt.BaseTest;
import com.ihomefnt.common.api.ResponseVo;
import com.ihomefnt.zeus.finder.ServiceCaller;

/**
 * Created by onefish on 2017/4/15 0015.
 */
public class ZeusTest extends BaseTest {
    
    @Resource
    private ServiceCaller serviceCaller;
    
    @Test
    public void autoProxy() {
//        String param = "{\"receiverAddress\":[\"zhaoqi@ihomefnt.com\"],\"subject\":\"just a test\",\"mailContent\":\"send1\",\"carbonCopyAddress\":[\"\"],\"attachment\":[\"\"]}";
//    	JSONObject result = serviceCaller.post( "ihome-api.ding.user.getByDingUserId",param, JSONObject.class);
//        System.out.println(result.toJSONString());
       
        Map<String, Object> param = new HashMap<>();
        param.put("departmentIds", "1");
        ResponseVo responseVo = serviceCaller.get("ihome-api.ding.user.queryDepartmentById", param, ResponseVo.class);
        System.out.println(JsonUtils.obj2json(responseVo));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("");
        
//        JSONObject result = serviceCaller.post( "ihome-api.artProduct.query",35, JSONObject.class);
//        System.out.println(result.toJSONString());
//        JSONObject result2 = serviceCaller.post("ihome-api.queryInv",22, JSONObject.class);
//        System.out.println(result2.toJSONString());
    }
}
