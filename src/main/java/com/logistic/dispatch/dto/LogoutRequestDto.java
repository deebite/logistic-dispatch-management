package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDto {

    /**
     * true  -> release all active batches
     * false -> keep batches assigned for later resume
     */
    @Builder.Default
    private Boolean releaseBatches = false;
}
