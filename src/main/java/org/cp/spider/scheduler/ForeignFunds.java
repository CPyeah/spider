package org.cp.spider.scheduler;


import org.cp.spider.dao.ForeignFundsMapper;
import org.cp.spider.util.ToolsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


/**
 * 外资流向
 */
@Configuration
@EnableScheduling
public class ForeignFunds {

    private static final Logger logger = LoggerFactory.getLogger(ForeignFunds.class);

    @Autowired
    ForeignFundsMapper foreignFundsMapper;

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 6 * * ?")
    private void process() {
        RestTemplate t = ToolsUtils.getBaseRestTemplate();
        Date yesterday = ToolsUtils.rollDateTime(new Date(), false, 1, ChronoUnit.DAYS);
        String d = new SimpleDateFormat("yyyy-MM-dd").format(yesterday);//2020-07-30
//        String url = "http://dcfm.eastmoney.com/EM_MutiSvcExpandInterface/api/js/get?type=HSGT20_GGTJ_SUM&token=894050c76af8597a853f5b408b759f5d&st=ShareSZ_Chg_One&sr=-1&p=1&ps=3000&filter=(DateType='1'and HdDate='2020-07-30')&rt=53202488";
        String url = "http://dcfm.eastmoney.com/EM_MutiSvcExpandInterface/api/js/get?type=HSGT20_GGTJ_SUM&token=894050c76af8597a853f5b408b759f5d&st=ShareSZ_Chg_One&sr=-1&p=1&ps=3000&filter=(DateType='1'and HdDate='"+d+"')&rt=53202488";
        logger.info("接口链接： " + url);
        ArrayList<Map> data = t.getForObject(url, ArrayList.class);
        foreignFundsMapper.insertList(data);
        System.out.println(data);

    }

}
