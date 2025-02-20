package ua.alexlapada.testframework.factory.core;

public interface BeforeSave<ENTITY> {

    Object action(EntityAttrMapper<ENTITY> context);
}
