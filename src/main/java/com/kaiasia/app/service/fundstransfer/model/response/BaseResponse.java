package com.kaiasia.app.service.fundstransfer.model.response;

import com.kaiasia.app.service.fundstransfer.model.validation.FailureGroup;
import com.kaiasia.app.service.fundstransfer.model.validation.SuccessGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse {
    @NotBlank(message = "Response code is required", groups = SuccessGroup.class)
    private String responseCode;

    @NotBlank(message = "Response code is required", groups = FailureGroup.class)
    private String code;

    private String desc;
}
