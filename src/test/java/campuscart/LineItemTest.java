package campuscart;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void constructorTrimsLabelAndRoundsPrice() {
        LineItem item = new LineItem("  binder  ", new BigDecimal("6.251"), 2);
        assertEquals("binder", item.getLabel());
        assertEquals(new BigDecimal("6.25"), item.getUnitPrice());
        assertEquals(2, item.getHowMany());
    }

    @Test
    void lineSubtotalMultipliesAndRounds() {
        LineItem item = new LineItem("pencil", new BigDecimal("0.50"), 3);
        assertEquals(new BigDecimal("1.50"), item.lineSubtotal());
    }

    @Test
    void setHowManyUpdatesQuantity() {
        LineItem item = new LineItem("hoodie", new BigDecimal("44.95"), 1);
        item.setHowMany(5);
        assertEquals(5, item.getHowMany());
        assertEquals(new BigDecimal("224.75"), item.lineSubtotal());
    }
}
