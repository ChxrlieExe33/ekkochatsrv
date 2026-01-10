package com.cdcrane.ekkochatsrv.config.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionErrorResponse {

    private String message;
    private Integer errorCode;
    private Long timestamp;

}
