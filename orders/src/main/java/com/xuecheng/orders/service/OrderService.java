package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.entity.MqMessage;
import com.xuecheng.orders.dto.AddOrderDto;
import com.xuecheng.orders.dto.PayRecordDto;
import com.xuecheng.orders.dto.PayStatusDto;
import com.xuecheng.orders.entity.XcPayRecord;

/**
 * @className: OrderService
 * @author: 朱江
 * @description:
 * @date: 2023/6/23
 **/
public interface OrderService {

    /**
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @description: 创建商品订单
     * @author Mr.M
     * @date: 2022/10/4 11:02
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    XcPayRecord getPayRecordByPayNo(String payNo);

    // 主动查询支付结果
    PayRecordDto queryPayResult(String payNo);

    /**
     * @param payStatusDto 支付结果信息
     * @return void
     * @description 保存支付宝支付结果
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

    void notifyPayResult(MqMessage message);
}
