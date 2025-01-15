package com.tiger.apigateway.dtos.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaintenanceDto {
    Boolean isActive;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String message;
}
