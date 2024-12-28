package com.kaiasia.app.service.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth1Out {
    private String responseCode;
    private String sessionId;
    private String username;
}
