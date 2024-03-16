package io.github.sbaeumlisberger.simpleproperties;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

// TODO use dots for hierarchy
public class PropertiesConverter extends Object {

    public static <T> T readObject(SimpleProperties properties, Class<T> clazz) throws PropertiesSerializationException {
        var parameterlessConstructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.getParameterCount() == 0 && (c.getModifiers() & Modifier.PUBLIC) != 0).findAny();

        if (parameterlessConstructor.isPresent()) {
            T object;
            try {
                object = (T) parameterlessConstructor.get().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new PropertiesSerializationException("Failed to create instance of " + clazz.getName(), e);
            }
            for (Method method : clazz.getMethods()) {
                if (isSetter(method)) {
                    String propertyName = determinePropertyName(method);
                    String propertyValue = properties.getProperty(propertyName);
                    if (propertyValue != null) // TODO try other formats
                    {
                        try {
                            method.invoke(object, propertyValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new PropertiesSerializationException("Failed to invoke method " + method.getName(), e);
                        }
                    }
                    if (!method.getReturnType().equals(Optional.class) /*&& !method.isAnnotationPresent(OptionalAnnotation.class)*/) {
                        // TODO fail if required, optional annotation
                    }
                }
            }
            return object;
        } else {
            return null; // TODO
        }
    }

    public static <T> void saveObject(SimpleProperties properties, T object) throws PropertiesSerializationException {
        for (Method method : object.getClass().getMethods()) {
            if (isGetter(method)) {
                try {
                    String propertyName = determinePropertyName(method); // TODO format options
                    String propertyValue = method.invoke(object).toString();
                    properties.setProperty(propertyName, propertyValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new PropertiesSerializationException("Failed to invoke method " + method.getName(), e);
                }
            }
        }
    }

    private static boolean isGetter(Method method) {
        return (method.getName().startsWith("get") || method.getName().startsWith("is"))
                && !method.getName().equals("getClass")
                && method.getParameterCount() == 0 && method.getReturnType() != Void.class;
    }

    private static boolean isSetter(Method method) {
        return (method.getName().startsWith("set")) && method.getParameterCount() == 1;
    }

    private static String determinePropertyName(Method method) {
        String propertyName = method.getName();
        propertyName = StringUtils.stripStart(propertyName, "get");
        propertyName = StringUtils.stripStart(propertyName, "is");
        propertyName = StringUtils.stripStart(propertyName, "set");
        return propertyName;

    }

}
