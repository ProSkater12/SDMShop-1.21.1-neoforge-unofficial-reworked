package net.sixk.sdmshop.item;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sixk.sdmshop.SDMShop;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SDMShop.MODID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(SDMShop.MODID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<Item> ANCIENT_BANKNOTE = ITEMS.register("ancient_banknote", () -> new AncientBanknoteItem(new Item.Properties()));

    public static final RegistrySupplier<CreativeModeTab> SDM_SHOP_TAB = TABS.register("sdmshop", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.sdmshop"))
            .icon(() -> new ItemStack(ANCIENT_BANKNOTE.get()))
            .displayItems((parameters, output) -> output.accept(ANCIENT_BANKNOTE.get()))
            .build());

    private ModItems() {
    }

    public static void register() {
        ITEMS.register();
        TABS.register();
    }
}
