package ua.alexlapada.testframework.factory.core;

public interface AfterSave<ENTITY> {

    Object action(EntityAttrMapper<ENTITY> context);
}
