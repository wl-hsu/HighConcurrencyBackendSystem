package com.wlh.seckill.mq;


import com.alibaba.fastjson.JSON;
import com.wlh.seckill.db.dao.OrderDao;
import com.wlh.seckill.db.dao.SeckillActivityDao;
import com.wlh.seckill.db.po.Order;
import com.wlh.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        // log.info("Receive order payment status verification message:" + message);
        log.info("Received order payment status verification message:" + message);
        Order order = JSON.parseObject(message, Order.class);
        //1.checking order
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());
        //2.Determine whether the order has been paid for
        if (orderInfo.getOrderStatus() != 2) {
            //3.Incomplete payment close order
            log.info("Close the order without completing the payment, order number:" + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);
            //4.Restoring database inventory
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // restore redis inventory
            redisService.revertStock("stock:" + order.getSeckillActivityId());
            //5.Remove user from purchased list
            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());

        }
    }
}
