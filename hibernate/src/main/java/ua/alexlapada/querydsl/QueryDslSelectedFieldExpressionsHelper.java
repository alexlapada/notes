package ua.alexlapada.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import graphql.schema.SelectedField;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryDslSelectedFieldExpressionsHelper {
  public static <T> Expression<?> mapFieldNameToEntityPath(EntityPath<T> qEntity,  SelectedField selectedField) {
    final Map<String, Expression<?>> columnsByName = Arrays.stream(qEntity.getClass().getDeclaredFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .map(field -> {
          try {
            return (Path<?>) field.get(qEntity);
          }
          catch (final Exception e) {
//           "should never happen"
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(expression -> expression.getMetadata().getName(), expression -> expression));
    return columnsByName.get(selectedField.getName());
  }


//    default Optional<BusinessProcessTemplateEntity> findById(
//            String id, List<SelectedField> selectionFields) {
//        List<? extends Expression<?>> pathList =
//                selectionFields.stream()
//                               .map(selectionField -> mapFieldNameToEntityPath(businessProcessTemplateEntity, selectionField))
//                               .toList();
//        Expression<?>[] selectedExpressions = pathList.toArray(new Expression[0]);
//        QBean<BusinessProcessTemplateEntity> selection =
//                Projections.fields(businessProcessTemplateEntity, selectedExpressions);
//        return Optional.ofNullable(
//                find(query ->
//                             query
//                                     .select(selection)
//                                     .from(businessProcessTemplateEntity)
//                                     .where(businessProcessTemplateEntity.id.eq(id)))
//                        .fetchOne());
//    }
}
