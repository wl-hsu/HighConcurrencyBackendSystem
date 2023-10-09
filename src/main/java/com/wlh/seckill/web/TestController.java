package com.wlh.seckill.web;


import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class TestController {

    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        String result;
        try (Entry entry = SphU.entry("HelloResource")){
            // Protected business logic
            result  = "Hello Sentinel";
            return result;
        }catch (BlockException ex) {
            // Resource access blocked, data limiting or downgraded
            // Do the appropriate processing here
            log.error(ex.toString());
            // System is busy and try again later
            result = "The system is busy and try again later.";
            return  result;
        }
    }

    /**
     *  Define data currency limiting rules
     *  1.Create a collection to store data rules
     *  2.Create data currency limiting rules
     *  3.Put data currency limiting rules into collections
     *  4.Load rules
     *  @PostConstruct  Executed after the constructor of the current class is executed
     */
    @PostConstruct
    public void seckillsFlow(){
        //1.1.Create a collection to store data rules
        List<FlowRule> rules = new ArrayList<>();
        //2.Create data currency limiting rules
        FlowRule rule = new FlowRule();
        //Define a resource, indicating that sentinel will take effect on that resource
        rule.setResource("seckills");
        //Define current limiting rule type, QPS type
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //Defines the number of requests passed by QPS per second
        rule.setCount(1);

        FlowRule rule2 = new FlowRule();
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(2);
        rule2.setResource("HelloResource");
        //3.Put data currency limiting rules into collections
        rules.add(rule);
        rules.add(rule2);
        //4.Load rules
        FlowRuleManager.loadRules(rules);
    }
}

