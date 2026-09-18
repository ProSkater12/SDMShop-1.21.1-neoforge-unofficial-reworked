package net.sixk.sdmshop.compat.ftbquests;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sixk.sdmshop.shop.ShopEconomy;

public class FTBMoneySubmitScreen extends BaseScreen {

    private final FTBMoneyTask task;
    private final boolean canClick;
    private final Component title;
    private final Component info;
    private final SimpleTextButton backButton;
    private final SimpleTextButton submitButton;

    public FTBMoneySubmitScreen(FTBMoneyTask task, boolean canClick) {
        this.task = task;
        this.canClick = canClick;

        long remaining = Math.max(0L, task.getMaxProgress() - ClientQuestFile.INSTANCE.selfTeamData.getProgress(task));
        long have = Minecraft.getInstance().player == null ? 0L : ShopEconomy.getMoney(Minecraft.getInstance().player, task.getCurrency());
        long willTake = Math.min(have, remaining);

        title = Component.translatable("sdm_shop.ftbquests.money.submit_title", task.getCurrency());
        info = Component.translatable("sdm_shop.ftbquests.money.submit_info", willTake, remaining, have);

        backButton = new SimpleTextButton(this, Component.translatable("gui.back"), Color4I.empty()) {
            @Override
            public void onClicked(MouseButton button) {
                playClickSound();
                onBack();
            }

            @Override
            public boolean renderTitleInCenter() {
                return true;
            }
        };

        submitButton = new SimpleTextButton(this, Component.translatable("ftbquests.gui.submit"), Color4I.empty()) {
            @Override
            public void onClicked(MouseButton button) {
                playClickSound();
                NetworkManager.sendToServer(new SubmitTaskMessage(task.id));
                onBack();
            }

            @Override
            public WidgetType getWidgetType() {
                return canClick && willTake > 0L ? super.getWidgetType() : WidgetType.DISABLED;
            }

            @Override
            public boolean renderTitleInCenter() {
                return true;
            }
        };
    }

    @Override
    public void addWidgets() {
        setWidth(Math.max(180, getTheme().getStringWidth(title) + 16));
        setHeight(72);
        add(backButton);
        add(submitButton);
        backButton.setPosAndSize(8, 44, 70, 20);
        submitButton.setPosAndSize(width - 78, 44, 70, 20);
    }

    @Override
    public Theme getTheme() {
        return FTBQuestsTheme.INSTANCE;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);
        theme.drawString(graphics, title, x + w / 2, y + 8, Color4I.WHITE, Theme.CENTERED);
        theme.drawString(graphics, info, x + w / 2, y + 24, Color4I.WHITE, Theme.CENTERED);
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) {
            return true;
        }
        if (key.esc()) {
            onBack();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClientQuestFile.exists() && ClientQuestFile.INSTANCE.isPauseGame();
    }

    @Override
    public boolean onClosedByKey(Key key) {
        if (super.onClosedByKey(key)) {
            onBack();
        }
        return false;
    }
}
