import io.github.sbaeumlisberger.simpleproperties.SimpleProperties;
import org.junit.jupiter.api.Test;

import static io.github.sbaeumlisberger.simpleproperties.PropertyMappers.asBoolean;
import static io.github.sbaeumlisberger.simpleproperties.PropertyMappers.asEnum;


public class SimplePropertiesTest {
    enum TestEnum {
        TEST
    }

    @Test
    public void test() {
        SimpleProperties properties = new SimpleProperties();

        var x = properties.getProperty("true", asBoolean());

        var y = properties.getProperty("test", asEnum(TestEnum.class, true));
    }

}
