package ua.alexlapada.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppStreamSession {
    private String id;
    private String userId;
    private String stackName;
    private String fleetName;
    private String state;
    private String connectionState;
    private String authenticationType;
}
