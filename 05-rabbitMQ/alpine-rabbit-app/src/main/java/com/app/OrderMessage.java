package com.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderMessage {
    private String teamName;
    private String orderID;
    private String productName;

    @JsonCreator
    public OrderMessage(
            @JsonProperty("teamName") String teamName,
            @JsonProperty("orderID") String orderID,
            @JsonProperty("productName") String productName) {
        this.teamName = teamName;
        this.orderID = orderID;
        this.productName = productName;
    }


}