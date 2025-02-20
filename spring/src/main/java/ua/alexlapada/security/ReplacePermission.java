package ua.alexlapada.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used to define whether we need to replace the permission placeholder in the {@link PreAuthorize} annotation with the actual permission value.
 * during execution.
 * see {@link PermissionPlaceholderMethodSecurityMetadataSource}
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ReplacePermission
{
   String PLACEHOLDER = "#permission";
}