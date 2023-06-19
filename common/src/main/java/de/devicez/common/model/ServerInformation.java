package de.devicez.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServerInformation {
    private String organisationName;
    private String organisationUrl;
    private String frontendUrl;
    private boolean registration;
}
