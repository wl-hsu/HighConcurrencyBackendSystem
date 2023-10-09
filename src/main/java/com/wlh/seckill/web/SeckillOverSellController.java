package com.wlh.seckill.web;


import com.wlh.seckill.services.SeckillActivityService;
import com.wlh.seckill.services.SeckillOverSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillOverSellController {

    @Autowired
    private SeckillOverSellService seckillOverSellService;


    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * Handling purchase requests
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
//    @RequestMapping("/seckill/{seckillActivityId}")
    public String  seckil(@PathVariable long seckillActivityId){
        return seckillOverSellService.processSeckill(seckillActivityId);
    }

    /**
     * Handle purchase requests with lua scripts
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId) {
        boolean stockValidateResult = seckillActivityService.seckillStockValidator(seckillActivityId);
        // "Congratulations on your success" : "Items are sold out, come back next time."
        return stockValidateResult ? "Congratulations on your successful flash sale purchase" : "The product has been sold out, please come again next time";
    }

}


