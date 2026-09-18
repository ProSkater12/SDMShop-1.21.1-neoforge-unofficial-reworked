package net.sixk.sdmshop.compat.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.CustomIconItem;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Icon picker used by FTB Quests (and by our task/reward configs).
 * Left click opens a context menu: pick an item, or pick a texture that is stored
 * on FTB Quests' Custom Icon item so the quest UI can render it.
 */
public class ConfigIconItemStack extends ItemStackConfig {

    public ConfigIconItemStack() {
        super(false, true);
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        if (!getCanEdit()) {
            return;
        }

        if (button.isRight()) {
            openImageSelector(callback);
            return;
        }

        clickedWidget.getGui().openContextMenu(List.of(
                new ContextMenuItem(
                        Component.translatable("ftbquests.gui.icon_menu.item"),
                        ItemIcon.getIcon(Items.DIAMOND.arch$registryName()),
                        b -> new SelectItemStackScreen(this, callback).openGui()
                ),
                new ContextMenuItem(
                        Component.translatable("ftbquests.gui.icon_menu.image"),
                        Icons.ART,
                        b -> openImageSelector(callback)
                )
        ));
    }

    private void openImageSelector(ConfigCallback callback) {
        ImageResourceConfig imageConfig = new ImageResourceConfig();

        new SelectImageResourceScreen(imageConfig, accepted -> {
            if (accepted) {
                if (!imageConfig.getValue().equals(ImageResourceConfig.NONE)) {
                    setCurrentValue(Util.make(new ItemStack(ModItems.CUSTOM_ICON.get()),
                            //stack -> CustomIconItem.setIcon(stack, imageConfig.getValue())));
                            stack -> setIcon(ItemIcon.getItemIcon(Items.GOLD_INGOT))));
                } else {
                    setCurrentValue(ItemStack.EMPTY);
                }
            }
            callback.save(accepted);
        }).openGui();
    }
}
