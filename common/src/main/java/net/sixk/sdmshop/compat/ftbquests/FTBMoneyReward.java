package net.sixk.sdmshop.compat.ftbquests;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.net.NotifyRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.sixk.sdmshop.shop.ShopEconomy;

/**
 * FTB Quests reward: credits SDM Economy currency to the claiming player.
 */
public class FTBMoneyReward extends Reward {

    public static RewardType TYPE;

    private long value = 1L;
    private int randomBonus = 0;
    private String currency = FTBIntegrationHelper.FALLBACK_CURRENCY;

    public FTBMoneyReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong("ftb_money", value);
        nbt.putString("currency", currency);
        if (randomBonus > 0) {
            nbt.putInt("random_bonus", randomBonus);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        value = nbt.getLong("ftb_money");
        randomBonus = nbt.getInt("random_bonus");
        String stored = nbt.getString("currency");
        currency = stored.isEmpty() ? FTBIntegrationHelper.FALLBACK_CURRENCY : stored;
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(value);
        buffer.writeVarInt(randomBonus);
        buffer.writeUtf(currency);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = buffer.readVarLong();
        randomBonus = buffer.readVarInt();
        currency = buffer.readUtf();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("value", value, v -> value = v, 1L, 1L, Long.MAX_VALUE)
                .setNameKey("ftbquests.reward.sdmshop.money");
        config.addInt("random_bonus", randomBonus, v -> randomBonus = v, 0, 0, Integer.MAX_VALUE)
                .setNameKey("ftbquests.reward.random_bonus");
        config.addString("currency", currency, v -> currency = v, FTBIntegrationHelper.FALLBACK_CURRENCY)
                .setNameKey("ftbquests.reward.sdmshop.currency");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        long added = value + player.serverLevel().random.nextInt(randomBonus + 1);
        ShopEconomy.addMoney(player, currency, added);

        if (notify) {
            NetworkManager.sendToPlayer(player, new NotifyRewardMessage(
                    id,
                    Component.literal(FTBIntegrationHelper.moneyString(currency, added)).withStyle(ChatFormatting.GOLD),
                    Icons.MONEY,
                    disableRewardScreenBlur
            ));
        }
    }

    @Override
    public MutableComponent getAltTitle() {
        if (randomBonus > 0) {
            return Component.literal(FTBIntegrationHelper.moneyString(currency, value)
                    + " - "
                    + FTBIntegrationHelper.moneyString(currency, value + randomBonus)).withStyle(ChatFormatting.GOLD);
        }
        return Component.literal(FTBIntegrationHelper.moneyString(currency, value)).withStyle(ChatFormatting.GOLD);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getButtonText() {
        if (randomBonus > 0) {
            return value + "-" + (value + randomBonus);
        }
        return Long.toUnsignedString(value);
    }
}
