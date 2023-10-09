package com.wlh.seckill;


import com.wlh.seckill.mq.RocketMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import  org.junit.jupiter.api.Test;
import java.util.Date;

@SpringBootTest
public class MQTest {
    @Autowired
    RocketMQService rocketMQService;
    @Test
    public void sendMQTest() throws Exception {
        rocketMQService.sendMessage("test-wlh", "Hello World!" + new
                Date().toString());
    }
}