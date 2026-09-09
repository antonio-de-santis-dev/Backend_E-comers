package com.it.orderservis.dto.dto;

import com.it.orderservis.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTOOutput {

    private UUID id;

    private UUID userId;

    private BigDecimal totale;

    private OrderStatus stato;

    private LocalDateTime dataCreazione;

    private List<OrderItemDTOOutput> items;

}
