package com.vavedin.euder.feature.item.domain;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class ItemLookupTest {
    @Test void findsStoredItemByIdAndReturnsEmptyForUnknownId() {
        var repository = new ItemRepository(new ItemDatabase());
        var item = new Item("Coffee","Good coffee",new BigDecimal("10.00"),5,"item-1");
        repository.addItem(item);
        assertThat(repository.findById("item-1")).contains(item);
        assertThat(repository.findById("missing")).isEmpty();
    }
}
