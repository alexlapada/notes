package ua.alexlapada.testframework.factory.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleAttributeSpy implements BuilderAttributeSpy {

    @Getter
    private final Object proxy;

    private final BuilderMethodInterceptor handler;

    public SimpleAttributeSpy(Object builder) {
        handler = new BuilderMethodInterceptor(builder);

        ProxyFactory proxyFactory = new ProxyFactory(builder);
        proxyFactory.addAdvice(handler);
        proxy = proxyFactory.getProxy();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return handler.getAttrs();
    }

    @RequiredArgsConstructor
    static class BuilderMethodInterceptor implements MethodInterceptor {

        private final Object builder;

        @Getter
        private final Map<String, Object> attrs = new LinkedHashMap<>();

        @Override
        public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();

            attrs.put(method.getName(), arguments[0]);

            return method.invoke(builder, arguments);
        }
    }
}
