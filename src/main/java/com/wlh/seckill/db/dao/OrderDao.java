package com.wlh.seckill.db.dao;

import com.wlh.seckill.db.po.Order;

public interface OrderDao {
    void insertOrder(Order order);

    Order queryOrder(String orderNo);

    void updateOrder(Order order);
}
