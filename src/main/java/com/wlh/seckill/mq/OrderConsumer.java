package com.wlh.seckill.mq;


import com.alibaba.fastjson.JSON;
import com.wlh.seckill.db.dao.OrderDao;
import com.wlh.seckill.db.dao.SeckillActivityDao;
import com.wlh.seckill.db.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup =
        "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Override
    @Transactional
    public void onMessage (MessageExt messageExt) {
        //1.Parse the create order request message.
        String message = new String(messageExt.getBody(),
                StandardCharsets.UTF_8);
        log.info("Request to create order received:" + message); // log.info("Create order request received：" + message);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());
        //2.deducted inventory
        boolean lockStockResult =
                seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            //Order Status 0: No stock available, invalid order 1: Created pending payment
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        //3.Insert order
        orderDao.insertOrder(order);
    }
}
