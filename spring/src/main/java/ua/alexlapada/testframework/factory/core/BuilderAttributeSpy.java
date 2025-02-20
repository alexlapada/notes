package ua.alexlapada.testframework.factory.core;

import java.util.Map;

public interface BuilderAttributeSpy {

    Map<String, Object> getAttributes();

    Object getProxy();
}
