package net.sixk.sdmshop.compat.ftbquests;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;
import net.sixk.sdmshop.SDMShop;

public class FTBIntegrationHelper {

    public static final String FALLBACK_CURRENCY = "sdmcoin";

    public static boolean FTBQuestLoaded = false;

    public static void init() {
        if (!Platform.isModLoaded("ftbquests")) {
            return;
        }

        FTBQuestLoaded = true;

        try {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SDMShop.MODID, "money");
            FTBMoneyTask.TYPE = TaskTypes.register(id, FTBMoneyTask::new, () -> Icons.MONEY);
            FTBMoneyReward.TYPE = RewardTypes.register(id, FTBMoneyReward::new, () -> Icons.MONEY);
        } catch (Throwable error) {
            SDMShop.LOGGER.error("Failed to load FTB Quests integration", error);
            FTBQuestLoaded = false;
        }
    }

    public static String moneyString(String currency, long amount) {
        return currency + " " + String.format("%,d", amount);
    }
}
