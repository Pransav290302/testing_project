package campuscart;

import java.math.BigDecimal;

public class LineItem {

    private final String label;
    private final BigDecimal unitPrice;
    private int howMany;

    public LineItem(String label, BigDecimal unitPrice, int howMany) {
        this.label = label.trim();
        this.unitPrice = Pricing.roundMoney(unitPrice);
        this.howMany = howMany;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getHowMany() {
        return howMany;
    }

    public void setHowMany(int howMany) {
        this.howMany = howMany;
    }

    public BigDecimal lineSubtotal() {
        return Pricing.roundMoney(unitPrice.multiply(BigDecimal.valueOf(howMany)));
    }
}
