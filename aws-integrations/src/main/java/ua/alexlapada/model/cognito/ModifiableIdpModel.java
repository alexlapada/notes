package ua.alexlapada.model.cognito;

import lombok.Data;

import java.util.Map;

@Data
public class ModifiableIdpModel {
    private String metadataUrl;
    private String emailAttribute;
    private Map<String, String> attributeMapping;
}
