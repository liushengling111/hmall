package com.hmall.client;

import com.hmall.client.fallback.ItemClientFallbackFactory;
import com.hmall.config.DefeatClientConfiguration;
import com.hmall.dto.ItemDTO;
import com.hmall.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service", configuration = DefeatClientConfiguration.class,fallbackFactory = ItemClientFallbackFactory.class)
public  interface ItemClient {
    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);
    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);
}
