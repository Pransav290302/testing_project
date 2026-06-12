package campuscart;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstructorCoverageTest {

    @Test
    void privateConstructorsExist() throws Exception {
        for (Class<?> type : new Class<?>[] {Main.class, Pricing.class, StoreCatalog.class}) {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            assertNotNull(ctor.newInstance());
        }
    }
}
