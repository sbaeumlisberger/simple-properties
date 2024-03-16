package io.github.sbaeumlisberger.simpleproperties;

public interface PropertiesPlugin {

    String onPropertyRead(String key, String value);

    String onPropertyWrite(String key, String value);
}
