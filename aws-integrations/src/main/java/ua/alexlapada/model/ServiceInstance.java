package ua.alexlapada.model;

import lombok.Data;

@Data
public class ServiceInstance {
    private String id;
    private String name;
    private String instanceType;
    private String awsState;
    private ServiceInstanceState state;

    public void setAwsState(String state) {
        this.awsState = state.toLowerCase();
    }
}
