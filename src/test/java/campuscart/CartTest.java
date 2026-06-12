package campuscart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    void emptyCartState() {
        assertTrue(cart.nothingInside());
        assertEquals(0, cart.allPiecesCount());
        assertEquals(new BigDecimal("0.00"), cart.goodsSubtotal());
        assertTrue(cart.snapshotRows().isEmpty());
    }

    @Test
    void addPiecesNewLine() {
        int total = cart.addPieces("Binder", new BigDecimal("6.25"), 2);
        assertEquals(2, total);
        assertFalse(cart.nothingInside());
        assertEquals(new BigDecimal("12.50"), cart.goodsSubtotal());
    }

    @Test
    void addPiecesMergesExistingLine() {
        cart.addPieces("binder", new BigDecimal("6.25"), 1);
        int total = cart.addPieces("  BINDER ", new BigDecimal("6.25"), 3);
        assertEquals(4, total);
        assertEquals(1, cart.snapshotRows().size());
        assertEquals(4, cart.snapshotRows().get(0).getHowMany());
    }

    @Test
    void dropRowRemovesExisting() {
        cart.addPieces("pencil", new BigDecimal("0.50"), 2);
        assertTrue(cart.dropRow("PENCIL"));
        assertTrue(cart.nothingInside());
    }

    @Test
    void dropRowMissingReturnsFalse() {
        assertFalse(cart.dropRow("ghost"));
    }

    @Test
    void changeQtyUpdatesExisting() {
        cart.addPieces("hoodie", new BigDecimal("44.95"), 1);
        assertTrue(cart.changeQty("hoodie", 3));
        assertEquals(3, cart.lookup("hoodie").getHowMany());
    }

    @Test
    void changeQtyMissingReturnsFalse() {
        assertFalse(cart.changeQty("missing", 1));
    }

    @Test
    void lookupFindsCaseInsensitive() {
        cart.addPieces("Stapler", new BigDecimal("16.33"), 1);
        LineItem row = cart.lookup("  stapler ");
        assertNotNull(row);
        assertEquals("Stapler", row.getLabel());
    }

    @Test
    void lookupMissingReturnsNull() {
        assertNull(cart.lookup("nope"));
    }

    @Test
    void wipeClearsCart() {
        cart.addPieces("lanyard", new BigDecimal("3.10"), 1);
        cart.wipe();
        assertTrue(cart.nothingInside());
        assertEquals(0, cart.allPiecesCount());
    }

    @Test
    void snapshotRowsIsCopy() {
        cart.addPieces("earplugs", new BigDecimal("4.49"), 1);
        List<LineItem> snap = cart.snapshotRows();
        assertEquals(1, snap.size());
        cart.wipe();
        assertEquals(1, snap.size());
    }
}
