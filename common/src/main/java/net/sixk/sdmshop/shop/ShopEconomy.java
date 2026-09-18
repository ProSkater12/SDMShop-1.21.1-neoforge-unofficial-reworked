package net.sixk.sdmshop.shop;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.sdmeconomy.api.CustomCurrencies;
import net.sixik.sdmeconomy.api.EconomyAPI;
import net.sixik.sdmeconomy.economy.Currency;
import net.sixik.sdmeconomy.economy.CurrencySymbol;
import net.sixik.sdmeconomy.economyData.CurrencyData;
import net.sixik.sdmeconomy.economyData.CurrencyPlayerData;
import net.sixik.sdmeconomy.utils.ErrorCodeStruct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class ShopEconomy {

    public static final String DEFAULT_CURRENCY_ID = "sdmcoin";
    public static final String DEFAULT_CURRENCY_SYMBOL = "◎";

    private ShopEconomy() {
    }

    public static void init() {
        CustomCurrencies.CURRENCIES.put(DEFAULT_CURRENCY_ID, ShopEconomy::defaultCurrency);
    }

    public static Currency defaultCurrency() {
        return new Currency(DEFAULT_CURRENCY_ID, new CurrencySymbol(DEFAULT_CURRENCY_SYMBOL), 0.0).canDelete(false);
    }

    public static String symbol(Currency currency) {
        if (currency == null || currency.symbol == null || currency.symbol.value == null || currency.symbol.value.isBlank()) {
            return DEFAULT_CURRENCY_SYMBOL;
        }
        return currency.symbol.value;
    }

    public static List<String> currencyIds() {
        CurrencyData data = CurrencyData.SERVER != null ? CurrencyData.SERVER : CurrencyData.CLIENT;
        if (data == null || data.currencies == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (Object entry : data.currencies) {
            ids.add(((Currency) entry).getName());
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    public static LinkedList<CurrencyPlayerData.PlayerCurrency> clientCurrencies() {
        if (CurrencyPlayerData.CLIENT == null || CurrencyPlayerData.CLIENT.currencies == null) {
            return new LinkedList<>();
        }
        return CurrencyPlayerData.CLIENT.currencies;
    }

    public static CurrencyPlayerData.PlayerCurrency clientCurrency(String id) {
        for (CurrencyPlayerData.PlayerCurrency currency : clientCurrencies()) {
            if (currency.currency.getName().equalsIgnoreCase(id)) {
                return currency;
            }
        }
        return null;
    }

    public static long getMoney(Player player, String id) {
        if (player.isLocalPlayer()) {
            return (long) CurrencyPlayerData.CLIENT.getBalance(id);
        }
        if (CurrencyPlayerData.SERVER == null) {
            return 0L;
        }
        ErrorCodeStruct result = CurrencyPlayerData.SERVER.getBalance(player, id);
        if (result == null || result.value == null) {
            return 0L;
        }
        return ((Number) result.value).longValue();
    }

    public static void setMoney(Player player, String id, long money) {
        if (!(player instanceof ServerPlayer serverPlayer) || CurrencyPlayerData.SERVER == null) {
            return;
        }
        CurrencyPlayerData.SERVER.setCurrencyValue(serverPlayer, id, money);
        EconomyAPI.syncPlayer(serverPlayer);
    }

    public static void addMoney(Player player, String id, long amount) {
        if (!(player instanceof ServerPlayer serverPlayer) || CurrencyPlayerData.SERVER == null) {
            return;
        }
        CurrencyPlayerData.SERVER.addCurrencyValue(serverPlayer, id, amount);
        EconomyAPI.syncPlayer(serverPlayer);
    }

    public static void createCurrency(String name, String symbol) {
        EconomyAPI.createCurrencyOnClient(new Currency(name, new CurrencySymbol(symbol), 0.0));
    }

    public static void deleteCurrency(Currency currency) {
        EconomyAPI.deleteCurrencyOnClient(currency);
    }

    public static void ensureDefaultCurrency(MinecraftServer server) {
        if (server == null || CurrencyData.SERVER == null || CurrencyData.SERVER.currencies == null) {
            return;
        }
        for (Object entry : CurrencyData.SERVER.currencies) {
            if (DEFAULT_CURRENCY_ID.equalsIgnoreCase(((Currency) entry).getName())) {
                return;
            }
        }
        EconomyAPI.createCurrencyOnServer(defaultCurrency());
        EconomyAPI.syncCurrencyData(server);
    }
}
