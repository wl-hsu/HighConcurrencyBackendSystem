package com.wlh.seckill.web;


import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.wlh.seckill.db.dao.OrderDao;
import com.wlh.seckill.db.dao.SeckillActivityDao;
import com.wlh.seckill.db.dao.SeckillCommodityDao;
import com.wlh.seckill.db.po.Order;
import com.wlh.seckill.db.po.SeckillActivity;
import com.wlh.seckill.db.po.SeckillCommodity;
import com.wlh.seckill.services.SeckillActivityService;
import com.wlh.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Autowired
    SeckillActivityService seckillActivityService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    RedisService redisService;


    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";
    }

    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException {
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        try (Entry entry = SphU.entry("seckills")) {
            List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
            resultMap.put("seckillActivities", seckillActivities);
            return "seckill_activity";
        } catch (BlockException ex) {
            log.error("Querying the list of flash sale activities is limited. "+ex.toString());
            return "wait";
        }
    }

    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {

//        try (Entry entry = SphU.entry("seckills")) {
//            SeckillActivity seckillActivity;
//            SeckillCommodity seckillCommodity;
//
//            String seckillActivityInfo = redisService.getValue("seckillActivity:" + seckillActivityId);
//            if (StringUtils.isNotEmpty(seckillActivityInfo)) {
//                log.info("redis cache data:" + seckillActivityInfo);
//                seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
//            } else {
//                seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
//            }
//
//            String seckillCommodityInfo = redisService.getValue("seckillCommodity:" + seckillActivity.getCommodityId());
//            if (StringUtils.isNotEmpty(seckillCommodityInfo)) {
//                log.info("redis cache data:" + seckillCommodityInfo);
//                seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
//            } else {
//                seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
//            }
//
//            resultMap.put("seckillActivity", seckillActivity);
//            resultMap.put("seckillCommodity", seckillCommodity);
//            resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
//            resultMap.put("oldPrice", seckillActivity.getOldPrice());
//            resultMap.put("commodityId", seckillActivity.getCommodityId());
//            resultMap.put("commodityName", seckillCommodity.getCommodityName());
//            resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
//
//            return "seckill_item";
//
//        } catch (BlockException ex) {
//            log.error("Querying the list of flash sale activities is limited. "+ex.toString());
//            return "wait";
//        }

        SeckillActivity seckillActivity;
        SeckillCommodity seckillCommodity;

        String seckillActivityInfo = redisService.getValue("seckillActivity:" + seckillActivityId);
        if (StringUtils.isNotEmpty(seckillActivityInfo)) {
            log.info("redis cache:" + seckillActivityInfo);
            seckillActivity = JSON.parseObject(seckillActivityInfo, SeckillActivity.class);
        } else {
            seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        }

        String seckillCommodityInfo = redisService.getValue("seckillCommodity:" + seckillActivity.getCommodityId());
        if (StringUtils.isNotEmpty(seckillCommodityInfo)) {
            log.info("redis cache:" + seckillCommodityInfo);
            seckillCommodity = JSON.parseObject(seckillActivityInfo, SeckillCommodity.class);
        } else {
            seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        }

        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());

        return "seckill_item";
    }

    /**
     * Handling purchase requests
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId, @PathVariable long seckillActivityId) {
        boolean stockValidateResult = false;

        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
             * 判斷用戶是否在已購名單中
             * check if the user is in the purchased list
             */
            // Commented out this section in order to use the Jmeter to test the QPS
//            if (redisService.isInLimitMember(seckillActivityId, userId)) {
//                //提示用戶已經在限購名單中，回傳結果
//                modelAndView.addObject("resultInfo", "Sorry, you are already on the purchase restriction list");
//                modelAndView.setViewName("seckill_result");
//                return modelAndView;
//            }
            /*
             * Confirm whether it can be purchased
             */
            stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                Order order = seckillActivityService.createOrder(seckillActivityId, userId);
                // The purchase is successful, the order is being created, the order ID:
                modelAndView.addObject("resultInfo","Flash sale is successful, order is being created, order ID：" + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                //Add user to purchased list
                redisService.addLimitMember(seckillActivityId, userId);
            } else {
                //Sorry, the item is out of stock
                modelAndView.addObject("resultInfo","Sorry, the product is out of stock");
            }
        } catch (Exception e) {
            //seckill system exception
            log.error("Flash sale system exception" + e.toString());
            modelAndView.addObject("resultInfo","Flash sale failed");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /**
     * Order Tracking
     * @param orderNo
     * @return
     */
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        //log.info("order inquiry, order number：" + orderNo);
        log.info("Order inquiry, order number：" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

    /**
     * pay
     * @return
     */
    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }

    /**
     * Get the current server-side time
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/getSystemTime")
    public String getSystemTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//Set date format set date format
        String date = df.format(new Date());// new Date() is to obtain the current system time To get the current system time
        return date;
    }

}