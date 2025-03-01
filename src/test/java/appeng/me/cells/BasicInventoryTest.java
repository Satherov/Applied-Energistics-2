package appeng.me.cells;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.BaseActionSource;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
public class BasicInventoryTest {
    private static final IActionSource SRC = new BaseActionSource();

    /**
     * Check that we can extract more than MAX_INT fluid at once from a cell. Regression test for
     * <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6794">#6794</a>
     */
    @Test
    void testFluidExtract() {
        var item = AEItems.FLUID_CELL_256K.asItem();
        var stack = new ItemStack(item);
        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);
        // Values are more than Integer.MAX_VALUE
        long testAmount = 40_000L * AEFluidKey.AMOUNT_BUCKET;
        assertThat(cell.insert(AEFluidKey.of(Fluids.LAVA), testAmount, Actionable.MODULATE, SRC))
                .isEqualTo(testAmount);
        assertThat(cell.extract(AEFluidKey.of(Fluids.LAVA), testAmount, Actionable.MODULATE, SRC))
                .isEqualTo(testAmount);
    }

    @Test
    void testTypeLimit() {
        var item = AEItems.ITEM_CELL_1K.get();
        var stack = new ItemStack(item);
        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        var maxTypes = item.getTotalTypes(stack);
        var keys = generateDifferentKeys(128);

        for (int i = 0; i < maxTypes; ++i) {
            assertThat(cell.insert(keys[i], 1, Actionable.MODULATE, SRC)).isEqualTo(1);
        }

        // Can't insert new types!
        assertThat(cell.insert(keys[maxTypes], 1, Actionable.MODULATE, SRC)).isEqualTo(0);

        for (int i = 0; i < maxTypes; ++i) {
            assertThat(cell.insert(keys[i], 1, Actionable.MODULATE, SRC)).isEqualTo(1);
        }
    }

    @Test
    void testSingleType() {
        var item = AEItems.ITEM_CELL_1K.get();
        var stack = new ItemStack(item);
        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        long maxItems = (long) (item.getBytes(stack) - item.getBytesPerType(stack))
                * AEKeyType.items().getAmountPerByte();

        assertThat(cell.insert(AEItemKey.of(Items.DIAMOND_PICKAXE), Long.MAX_VALUE, Actionable.MODULATE, SRC))
                .isEqualTo(maxItems);
    }

    @Test
    void testEvenDistribution() {
        var item = AEItems.ITEM_CELL_1K.get();
        var stack = new ItemStack(item);
        item.getUpgrades(stack).addItems(AEItems.EQUAL_DISTRIBUTION_CARD.stack());
        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        int totalTypes = item.getTotalTypes(stack);
        long maxTotalCount = (item.getBytes(stack) - (long) totalTypes * item.getBytesPerType(stack))
                * AEKeyType.items().getAmountPerByte();
        // Should be inserted for the first totalTypes-1 types
        long maxItemsPerType = (long) Math.ceil((double) maxTotalCount / item.getTotalTypes(stack));
        // Should be inserted for the very last type
        long lastTypeItems = maxTotalCount - maxItemsPerType * (totalTypes - 1);

        var keys = generateDifferentKeys(totalTypes);

        // Check that totalTypes-1 keys are correctly limited by the distribution card
        for (int i = 0; i < totalTypes - 1; ++i) {
            assertThat(cell.insert(keys[i], Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(maxItemsPerType);
        }
        // Check that the last one is limited to a lower number (since the distribution is not perfectly even)
        assertThat(cell.insert(keys[totalTypes - 1], Long.MAX_VALUE, Actionable.MODULATE, SRC))
                .isEqualTo(lastTypeItems);
        // Check that the cell is now full, as it should be.
        assertThat(cell.getStatus()).isEqualTo(CellState.FULL);
    }

    @Test
    void testVoidUpgrade() {
        var item = AEItems.ITEM_CELL_1K.get();
        var stack = new ItemStack(item);
        item.getUpgrades(stack).addItems(AEItems.VOID_CARD.stack());

        // Setup whitelist
        var allowed = AEItemKey.of(Items.DIAMOND);
        var allowed2 = AEItemKey.of(Items.DIAMOND_BLOCK);
        var rejected = AEItemKey.of(Items.GOLD_INGOT);
        item.getConfigInventory(stack).addFilter(allowed).addFilter(allowed2);

        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        // Ensure that the first insert voids.
        assertThat(cell.insert(allowed, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(Long.MAX_VALUE);
        // Ensure that subsequent inserts void too even if the cell is full.
        assertThat(cell.insert(allowed, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(Long.MAX_VALUE);
        assertThat(cell.insert(allowed2, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(Long.MAX_VALUE);

        // Ensure that items that don't match the filter don't get voided.
        assertThat(cell.insert(rejected, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isZero();
    }

    @Test
    void testVoidUpgradeUnformatted() {
        var item = AEItems.ITEM_CELL_1K.get();
        var stack = new ItemStack(item);
        item.getUpgrades(stack).addItems(AEItems.VOID_CARD.stack());

        var cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        // Ensure that the first insert of a single type voids only excess.
        var filler = AEItemKey.of(Items.DIAMOND);
        assertThat(cell.insert(filler, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(Long.MAX_VALUE);
        assertThat(cell.getAvailableStacks().get(filler)).isNotZero();
        // Ensure that new item types that the cell cannot store don't get voided.
        var rejected = AEItemKey.of(Items.STICK);
        assertThat(cell.insert(rejected, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isZero();

        // Part two, fill cell with 63 different types this time.
        cell.extract(filler, Long.MAX_VALUE, Actionable.MODULATE, SRC);
        item.getUpgrades(stack).removeItems(1, AEItems.VOID_CARD.stack(), null);
        item.getUpgrades(stack).addItems(AEItems.EQUAL_DISTRIBUTION_CARD.stack());
        cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        var maxTypes = item.getTotalTypes(stack);
        var keys = generateDifferentKeys(maxTypes);

        for (int i = 0; i < maxTypes; ++i) {
            cell.insert(keys[i], Long.MAX_VALUE, Actionable.MODULATE, SRC);
        }

        item.getUpgrades(stack).addItems(AEItems.VOID_CARD.stack());
        cell = StorageCells.getCellInventory(stack, null);
        Objects.requireNonNull(cell);

        // Ensure that inserting an already-stored item voids.
        assertThat(cell.insert(keys[0], Long.MAX_VALUE, Actionable.MODULATE, SRC)).isEqualTo(Long.MAX_VALUE);
        // Ensure that items that aren't on the cell don't get voided.
        assertThat(cell.insert(rejected, Long.MAX_VALUE, Actionable.MODULATE, SRC)).isZero();
    }

    private static AEItemKey[] generateDifferentKeys(int count) {
        var out = new AEItemKey[count];
        for (int i = 0; i < count; ++i) {
            var itemStack = new ItemStack(Items.DIAMOND);
            itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("number" + i));
            out[i] = AEItemKey.of(itemStack);
        }
        return out;
    }
}
