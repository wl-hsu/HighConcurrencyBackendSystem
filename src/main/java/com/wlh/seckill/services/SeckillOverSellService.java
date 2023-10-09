package com.wlh.seckill.services;


import com.wlh.seckill.db.dao.SeckillActivityDao;
import com.wlh.seckill.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;
    public String processSeckill(long activityId) {
        SeckillActivity seckillActivity =
                seckillActivityDao.querySeckillActivityById(activityId);
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        // result = Congratulations, the purchase was successful
        if (availableStock > 0) {result = "Congratulations, the purchase was successful";
            System.out.println(result);
            availableStock = availableStock - 1;
            seckillActivity.setAvailableStock(new Integer("" + availableStock));
            seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
            // result = Sorry, the rush purchase failed, the goods were sold out
            result = "Sorry, the rush to buy failed and the product was sold out.";
            System.out.println(result);
        }
        return result;
    }
}
