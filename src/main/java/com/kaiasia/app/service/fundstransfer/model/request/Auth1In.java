package com.kaiasia.app.service.fundstransfer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


/**
 * Class này dùng để định nghĩa dữ liệu cần gửi tới Auth-1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth1In {
    private String authenType;
    private String sessionId;

}
