package ua.alexlapada.configuration;

import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import ua.alexlapada.security.PermissionPlaceholderMethodSecurityMetadataSource;

@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        ExpressionBasedAnnotationAttributeFactory attributeFactory = new ExpressionBasedAnnotationAttributeFactory(getExpressionHandler());
        return new PermissionPlaceholderMethodSecurityMetadataSource(attributeFactory);
    }
}
