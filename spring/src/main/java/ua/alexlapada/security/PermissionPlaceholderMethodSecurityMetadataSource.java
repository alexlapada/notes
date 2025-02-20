package ua.alexlapada.security;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of {@link AbstractMethodSecurityMetadataSource} that replaces the permission placeholder in the
 * {@link PreAuthorize} annotation with the actual permission value.
 */
public class PermissionPlaceholderMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource {
    private final PrePostInvocationAttributeFactory attributeFactory;

    public PermissionPlaceholderMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }

    /**
     * To get the {@link ConfigAttribute} for the given {@link Method} invocation. {@link ConfigAttribute} is used to define authorize expressions
     * to use in the Spring Security framework. {@link org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice} evaluates
     * SpEL {<a href="https://docs.spring.io/spring-framework/docs/3.0.x/reference/expressions.html">Spring Expression Language (SpEL)</a>} to parse authorize expressions from the attribute.
     * If there is no annotation that contains the permission to be replaced, return the empty list to delegate
     * processing to the next. See {@link org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration#methodSecurityMetadataSource()}.
     *
     * @param method      - the method to be checked.
     * @param targetClass - the target class to be checked.
     * @return - the {@link ConfigAttribute}s for the given {@link Method} invocation.
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return Collections.emptyList();
        }
        ArrayList<ConfigAttribute> attrs = new ArrayList<>(1);
        PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
        if (preAuthorize == null) {
            return attrs;
        }
        Annotation replaceAnnotation = getWithReplaceAnnotation(method);
        if (replaceAnnotation == null) {
            return attrs;
        }
        String preAuthorizeValue = specifyValue(replaceAnnotation, preAuthorize);
        PreInvocationAttribute pre = attributeFactory.createPreInvocationAttribute(null, null, preAuthorizeValue);
        attrs.add(pre);
        return attrs;
    }

    /**
     * Replaces the permission placeholder in the {@link PreAuthorize} annotation with the actual permission value.
     *
     * @param replaceAnnotation - the annotation that contains the permission to be replaced.
     * @param preAuthorize      - the {@link PreAuthorize} annotation to be checked.
     * @return - the {@link PreAuthorize} value with the permission placeholder replaced.
     */
    private String specifyValue(Annotation replaceAnnotation, PreAuthorize preAuthorize) {
        try {
            Method permissionMethod = replaceAnnotation.getClass().getMethod("value");
            permissionMethod.setAccessible(true);
            Object value = permissionMethod.invoke(replaceAnnotation);
            if (value == null) {
                return preAuthorize.value();
            }
            String permissionToReplace;
            if (value instanceof Permission permission) {
                permissionToReplace = permission.name();
            } else {
                permissionToReplace = value.toString();
            }
            return preAuthorize.value().replaceAll(ReplacePermission.PLACEHOLDER, "'" + permissionToReplace + "'");
        } catch (Exception e) {
            throw new RuntimeException("Error while replacing permission placeholder in PreAuthorize annotation.", e);
        }
    }

    /**
     * Returns the annotation that contains the permission to be replaced.
     *
     * @param method - the method to be checked.
     * @return - the annotation that contains the permission to be replaced.
     */
    private Annotation getWithReplaceAnnotation(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(ReplacePermission.class)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        // nothing here
        return null;
    }
}
