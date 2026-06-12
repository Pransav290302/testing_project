package campuscart;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingTest {

    @Test
    void roundMoneyHalfUp() {
        assertEquals(new BigDecimal("2.56"), Pricing.roundMoney(new BigDecimal("2.555")));
        assertEquals(new BigDecimal("2.55"), Pricing.roundMoney(new BigDecimal("2.554")));
        assertEquals(new BigDecimal("1.00"), Pricing.roundMoney(new BigDecimal("1")));
    }

    @Test
    void okPurchaseSizeBoundaries() {
        assertFalse(Pricing.okPurchaseSize(new BigDecimal("0.99")));
        assertTrue(Pricing.okPurchaseSize(new BigDecimal("1.00")));
        assertTrue(Pricing.okPurchaseSize(new BigDecimal("99999.99")));
        assertFalse(Pricing.okPurchaseSize(new BigDecimal("100000.00")));
    }

    @Test
    void salesTaxNullInputs() {
        assertEquals(new BigDecimal("0.00"), Pricing.salesTax(null, new BigDecimal("10")));
        assertEquals(new BigDecimal("0.00"), Pricing.salesTax("IL", null));
    }

    @Test
    void salesTaxTaxedStates() {
        BigDecimal goods = new BigDecimal("100.00");
        assertEquals(new BigDecimal("6.00"), Pricing.salesTax("IL", goods));
        assertEquals(new BigDecimal("6.00"), Pricing.salesTax("CA", goods));
        assertEquals(new BigDecimal("6.00"), Pricing.salesTax("NY", goods));
    }

    @Test
    void salesTaxAbbreviationsUseTaxedList() {
        assertEquals(new BigDecimal("0.60"), Pricing.salesTax("il", new BigDecimal("10.00")));
    }

    @Test
    void salesTaxFullStateNames() {
        BigDecimal goods = new BigDecimal("50.00");
        assertEquals(new BigDecimal("3.00"), Pricing.salesTax("Illinois", goods));
        assertEquals(new BigDecimal("3.00"), Pricing.salesTax("CALIFORNIA", goods));
        assertEquals(new BigDecimal("3.00"), Pricing.salesTax("New York", goods));
        assertEquals(new BigDecimal("3.00"), Pricing.salesTax("NEWYORK", goods));
    }

    @Test
    void salesTaxUntaxedState() {
        assertEquals(new BigDecimal("0.00"), Pricing.salesTax("TX", new BigDecimal("100")));
        assertEquals(new BigDecimal("0.00"), Pricing.salesTax("  tx  ", new BigDecimal("100")));
    }

    @Test
    void shipFeeNullInputs() {
        assertEquals(new BigDecimal("0.00"), Pricing.shipFee(null, new BigDecimal("10")));
        assertEquals(new BigDecimal("0.00"), Pricing.shipFee(Pricing.ShipSpeed.STANDARD, null));
    }

    @Test
    void shipFeeNextDay() {
        assertEquals(new BigDecimal("25.00"), Pricing.shipFee(Pricing.ShipSpeed.NEXT_DAY, new BigDecimal("10")));
    }

    @Test
    void shipFeeStandardThreshold() {
        assertEquals(new BigDecimal("10.00"), Pricing.shipFee(Pricing.ShipSpeed.STANDARD, new BigDecimal("50.00")));
        assertEquals(new BigDecimal("0.00"), Pricing.shipFee(Pricing.ShipSpeed.STANDARD, new BigDecimal("50.01")));
        assertEquals(new BigDecimal("10.00"), Pricing.shipFee(Pricing.ShipSpeed.STANDARD, new BigDecimal("30.00")));
    }

    @Test
    void orderTotalCombinesComponents() {
        BigDecimal goods = new BigDecimal("100.00");
        BigDecimal total = Pricing.orderTotal("IL", Pricing.ShipSpeed.STANDARD, goods);
        assertEquals(new BigDecimal("106.00"), total);
    }

    @Test
    void orderTotalWithPaidStandardShipping() {
        BigDecimal goods = new BigDecimal("40.00");
        BigDecimal total = Pricing.orderTotal("IL", Pricing.ShipSpeed.STANDARD, goods);
        assertEquals(new BigDecimal("52.40"), total);
    }

    @Test
    void orderTotalNoTaxFreeShipping() {
        BigDecimal goods = new BigDecimal("60.00");
        BigDecimal total = Pricing.orderTotal("TX", Pricing.ShipSpeed.STANDARD, goods);
        assertEquals(new BigDecimal("60.00"), total);
    }
}
