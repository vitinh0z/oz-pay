package com.ozpay.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Gateway {

    private UUID id;
    private UUID tenentId;
    private String gatewayName;
    private Map<String, String> credential;
    private boolean isActive;

}
