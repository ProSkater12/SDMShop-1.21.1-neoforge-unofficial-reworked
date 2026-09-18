package net.sixk.sdmshop.config;

import net.minecraft.server.MinecraftServer;
import net.sixk.sdmshop.shop.ShopEconomy;

public class ShopConfig {

    public static void init() {
        ShopEconomy.init();
    }

    public static String defaultCurrencyId() {
        return ShopEconomy.DEFAULT_CURRENCY_ID;
    }

    public static void ensureDefaultCurrency(MinecraftServer server) {
        ShopEconomy.ensureDefaultCurrency(server);
    }
}
