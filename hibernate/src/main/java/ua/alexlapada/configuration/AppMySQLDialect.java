package ua.alexlapada.configuration;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StandardBasicTypes;

// To register custom function to HIBERNATE
public class AppMySQLDialect extends MySQL5Dialect {

    public AppMySQLDialect() {
        super();
        registerFunction("distance", new SQLFunctionTemplate(DoubleType.INSTANCE, "ST_DISTANCE_SPHERE(POINT(?1, ?2),POINT(?3, ?4))"));
        registerFunction("datediff", new StandardSQLFunction("datediff", StandardBasicTypes.INTEGER));
        registerFunction("regexp", new SQLFunctionTemplate(IntegerType.INSTANCE, "?1 regexp ?2"));
    }

//    public class QueryDslExampleUsage {
//        private String attribute;
//        public Predicate toPredicate() {
//            final BooleanBuilder and = new BooleanBuilder();
//
//            Optional.ofNullable(this.attribute)
//                    .map(id -> "[[:<:]]" + id + "[[:>:]]")
//                    .map(regexp -> Expressions.booleanTemplate("function('regexp', {0}, {1})=1", "QEntity.argument", regexp))
//                    .ifPresent(and::and);
//
//            return and.getValue();
//        }
//
//        public SimpleExpression<Double> calculateDistance(Position position) {
//            return Expressions.template(Double.class, "function('distance', {0}, {1}, {2}, {3})", "QEntity.longitude",
//                    "QEntity.latitude", position.getLongitude(), position.getLatitude()).as("distance");
//        }
//
//        public BooleanExpression withinRadius(Position position) {
//            return Expressions.booleanTemplate("function('distance', {0}, {1}, {2}, {3})<{4}", "QEntity.longitude",
//                    "QEntity.latitude", position.getLongitude(), position.getLatitude(), position.getRadiusInUnit());
//        }
//
//        @Data
//        class Position {
//            private Double longitude;
//            private Double latitude;
//            private Double radius;
//            private LocationUnit unit;
//
//            public Double getRadiusInUnit() {
//                return radius * unit.rate();
//            }
//        }
//
//        @RequiredArgsConstructor
//        public enum LocationUnit {
//            MILES(1609.0),
//            KILOMETERS(1000.0),
//            METERS(1.0);
//
//            private final Double rate;
//
//            public Double rate() {
//                return this.rate;
//            }
//        }
//    }

//    @PersistenceContext
//    private EntityManager entityManager;
//
//    public class TypedQueryExample {
//        Integer userId = 1;
//        TypedQuery<EntityProjection> projectionQuery = entityManager
//                .createQuery("select new ua.alexlapada.configuration.EntityProjection(\n"
//                        + "       function('datediff', now(), t.createdAt) as age,\n"
//                        + "from Table t\n"
//                        + "where t.userId=:userId\n"
//                        + "order by t.createdAt desc\n", EntityProjection.class);
//        projectionQuery.setParameter("userId", userId);
//
//    }
//
//    class EntityProjection {
//
//    }
}
