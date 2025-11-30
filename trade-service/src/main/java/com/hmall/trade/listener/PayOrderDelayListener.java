package com.hmall.trade.listener;

import com.hmall.client.PayClient;
import com.hmall.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayOrderDelayListener {
    private final IOrderService orderService;
    private final PayClient payClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        Order order = orderService.getById(orderId);
        if(order == null || order.getStatus()!=1){
            //订单不存在或者已支付
            return;
        }
        //查询支付流水
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
        if(payOrder != null || payOrder.getPayType() == 3){
            //订单存在且已支付，更新订单状态
            orderService.markOrderPaySuccess(orderId);
        }else {
            // TODO未支付，取消订单，回复库存
            orderService.cancelOrder(orderId);
        }
    }
}
