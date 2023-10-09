package com.wlh.seckill.services;

import com.alibaba.fastjson.JSON;
import com.wlh.seckill.db.dao.OrderDao;
import com.wlh.seckill.db.dao.SeckillActivityDao;
import com.wlh.seckill.db.dao.SeckillCommodityDao;
import com.wlh.seckill.db.po.Order;
import com.wlh.seckill.db.po.SeckillActivity;
import com.wlh.seckill.db.po.SeckillCommodity;
import com.wlh.seckill.mq.RocketMQService;
import com.wlh.seckill.util.RedisService;
import com.wlh.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Slf4j
@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private RocketMQService rocketMQService;

    @Autowired
    SeckillCommodityDao seckillCommodityDao;

    @Autowired
    OrderDao orderDao;

    /**
     * datacenterId;
     * machineId;
     */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * estimate inventory
     * @param activityId
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    /**
     * Create Order
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1. Create Order
         */
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        //Using Snowflake Algorithm to Generate Order ID
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
         *2.Send create order message
         */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        /*
         * 3.Send order payment status verification message
         * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
         */
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 5);


        return order;
    }

    /**
     * Order payment completed
     * @param orderNo
     */
    public void payOrderProcess(String orderNo) throws Exception {
        //log.info("Complete the payment order  Order No.：" + orderNo);
        log.info("Complete payment order, Order number:" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        /*
         * 1.Check if an order exists
         * 2.Determine whether the order status is unpaid
         */
        if (order == null) {
            //log.error("The order number corresponding to the order does not exist：" + orderNo);
            log.error("The order corresponding to the order number does not exist：" + orderNo);
            return;
        } else if(order.getOrderStatus() != 1 ) {

            //og.error("Invalid order status：" + orderNo);
            log.error("Order status is invalid：" + orderNo);
            return;
        }
        /*
         * 2. Order payment completed
         */
        order.setPayTime(new Date());
        //Order status 0: No stock available, invalid order
        //1: Created and waiting for payment , 2: Payment completed
        order.setOrderStatus(2);
        orderDao.updateOrder(order);
        /*
         *3.Send order payment success message
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }

    /**
     * Pour the information about the seckill details into redis
     * @param seckillActivityId
     */
    public void pushSeckillInfoToRedis(long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, JSON.toJSONString(seckillActivity));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), JSON.toJSONString(seckillCommodity));
    }

}

