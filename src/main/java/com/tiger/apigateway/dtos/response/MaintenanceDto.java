package com.tiger.apigateway.dtos.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
