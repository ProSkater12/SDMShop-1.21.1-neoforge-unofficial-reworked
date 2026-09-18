package net.sixk.sdmshop.compat.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ISingleLongValueTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.sixk.sdmshop.shop.ShopEconomy;

/**
 * FTB Quests task: the player must pay the configured amount of SDM Economy currency.
 */
public class FTBMoneyTask extends Task implements ISingleLongValueTask {

    public static TaskType TYPE;

    private long value = 1L;
    private String currency = FTBIntegrationHelper.FALLBACK_CURRENCY;

    public FTBMoneyTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TYPE;
    }

    @Override
    public long getMaxProgress() {
        return value;
    }

    @Override
    public String formatMaxProgress() {
        return FTBIntegrationHelper.moneyString(currency, value);
    }

    @Override
    public String formatProgress(TeamData teamData, long progress) {
        return FTBIntegrationHelper.moneyString(currency, progress);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong("value", value);
        nbt.putString("currency", currency);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        value = nbt.getLong("value");
        String stored = nbt.getString("currency");
        currency = stored.isEmpty() ? FTBIntegrationHelper.FALLBACK_CURRENCY : stored;
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(value);
        buffer.writeUtf(currency);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = buffer.readVarLong();
        currency = buffer.readUtf();
    }

    @Override
    public void setValue(long v) {
        value = v;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE)
                .setNameKey("ftbquests.task.sdmshop.money");
        config.addString("currency", currency, v -> currency = v, FTBIntegrationHelper.FALLBACK_CURRENCY)
                .setNameKey("ftbquests.task.sdmshop.currency");
    }

    @Override
    public MutableComponent getAltTitle() {
        return Component.literal(FTBIntegrationHelper.moneyString(currency, value));
    }

    @Override
    public boolean consumesResources() {
        return true;
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 0;
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem) {
        if (!checkTaskSequence(teamData) || teamData.isCompleted(this)) {
            return;
        }

        long money = ShopEconomy.getMoney(player, currency);
        long add = Math.min(money, value - teamData.getProgress(this));

        if (add > 0L) {
            ShopEconomy.setMoney(player, currency, money - add);
            teamData.addProgress(this, add);
        }
    }
}
