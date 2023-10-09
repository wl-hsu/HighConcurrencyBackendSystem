package com.wlh.seckill.mq;


import com.alibaba.fastjson.JSON;
import com.wlh.seckill.db.dao.SeckillActivityDao;
import lombok.extern.slf4j.Slf4j;
import com.wlh.seckill.db.po.Order;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Transactional
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Override
    public void onMessage(MessageExt messageExt) {
        //2.Parse the create order request message
        String message = new String(messageExt.getBody(),
                StandardCharsets.UTF_8);
        log.info("Receive order creation request：" + message);
        Order order = JSON.parseObject(message, Order.class);
        //2.deducted inventory
        seckillActivityDao.deductStock(order.getSeckillActivityId());
    }
}