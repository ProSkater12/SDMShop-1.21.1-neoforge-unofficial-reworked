package net.sixk.sdmshop.shop;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sixik.sdmeconomy.economyData.CurrencyPlayerData;
import net.sixk.sdmshop.SDMShop;

import java.util.Collection;
import java.util.Objects;

public class ShopComands {

    private static final SuggestionProvider<CommandSourceStack> CURRENCY_SUGGESTIONS = (context, builder) -> {
        for (String key : ShopEconomy.currencyIds()) {
            builder.suggest(key);
        }
        return builder.buildFuture();
    };

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sdmshop")

                .then(Commands.literal("edit_mode")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> editMode(context.getSource()))
                )
                .then(Commands.literal("balance")
                        .executes(context -> balance(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> balance(context.getSource(), EntityArgument.getPlayer(context, "player")))
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", LongArgumentType.longArg())
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("currency", StringArgumentType.string()).suggests(CURRENCY_SUGGESTIONS)
                                        .executes(context -> addPay(context, context.getSource().getPlayerOrException(), LongArgumentType.getLong(context, "amount")))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> addPay(context, EntityArgument.getPlayer(context, "target"), LongArgumentType.getLong(context, "amount")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("currency", StringArgumentType.string()).suggests(CURRENCY_SUGGESTIONS)
                                    .then(Commands.argument("money", LongArgumentType.longArg(1L))
                                            .executes(context -> pay(context, context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"), LongArgumentType.getLong(context, "money")))
                                    )
                            )
                        )
                )
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.players())
                                .then(Commands.argument("currency", StringArgumentType.string()).suggests(CURRENCY_SUGGESTIONS)
                                    .then(Commands.argument("money", LongArgumentType.longArg(0L))
                                            .executes(context -> set(context, EntityArgument.getPlayers(context, "player"), LongArgumentType.getLong(context, "money")))
                                    )
                                )
                        )
                )
        );
    }




    private static int editMode(CommandSourceStack source){
        if(source.getPlayer() != null) {
            SDMShop.setEditMode(source.getPlayer(), !SDMShop.isEditMode(source.getPlayer()));
            source.sendSuccess(() -> Component.literal("Edit mode is " + SDMShop.isEditMode(source.getPlayer())), false);
        }
        return 1;
    }

    private static int balance(CommandSourceStack source, ServerPlayer player) {
        if (CurrencyPlayerData.SERVER == null) {
            source.sendFailure(Component.literal("Economy data is not loaded"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(player.getScoreboardName() + " balance:"), false);
        int shown = 0;
        for (CurrencyPlayerData.PlayerCurrency currency : CurrencyPlayerData.SERVER.getPlayersCurrency(player)) {
            String id = currency.currency.getName();
            String symbol = ShopEconomy.symbol(currency.currency);
            long amount = getMoney(player, id);
            source.sendSuccess(() -> Component.literal(" - " + id + " " + symbol + " " + amount), false);
            shown++;
        }

        if (shown == 0) {
            source.sendFailure(Component.literal("No currencies found"));
            return 0;
        }
        return shown;
    }

    private static int addPay(CommandContext<CommandSourceStack> context, ServerPlayer player, long amount) {
        String requested = StringArgumentType.getString(context, "currency");
        CommandSourceStack source = context.getSource();
        String currency = null;
        for (String id : ShopEconomy.currencyIds()) {
            if (id.equalsIgnoreCase(requested)) {
                currency = id;
                break;
            }
        }
        if (currency == null) {
            source.sendFailure(Component.literal("Unknown currency: " + requested));
            return 0;
        }

        ShopEconomy.addMoney(player, currency, amount);
        String currencyId = currency;
        long balance = getMoney(player, currencyId);
        source.sendSuccess(() -> Component.literal(player.getScoreboardName() + ": " + currencyId + " " + balance), false);
        return 1;
    }

    private static int pay(CommandContext<CommandSourceStack> context, ServerPlayer from, ServerPlayer to, long money) {

        String currency = StringArgumentType.getString(context,"currency");
        CommandSourceStack source = context.getSource();
        if (from.getUUID().equals(to.getUUID())) {
            source.sendFailure(Component.literal("You can't send money to yourself"));
            return 1;
        }
        if(getMoney(from,currency) >= money){
            setMoney((ServerPlayer) from, currency ,getMoney(from,currency) - money);
            setMoney((ServerPlayer) to, currency,getMoney(to,currency) + money);
            source.sendSuccess(() -> Component.literal("Money sended !"), false);
            return 0;
        }
        source.sendFailure(Component.literal("Not enough money"));
        return 1;
    };

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, long money) {

        String currency = StringArgumentType.getString(context,"currency");
        CommandSourceStack source = context.getSource();
        String specialSing = " ";
        for (ServerPlayer player : players) {
            for (CurrencyPlayerData.PlayerCurrency c : CurrencyPlayerData.SERVER.getPlayersCurrency(player)) {
               if (Objects.equals(c.currency.getName(), currency)) {
                   specialSing = ShopEconomy.symbol(c.currency);
                   break;
               }
            }
            setMoney(player, currency, money);
            String finalSpecialSing = specialSing;
            source.sendSuccess(() -> Component.literal(player.getScoreboardName() + ": ").append(Component.literal(finalSpecialSing + " ").append(String.valueOf(getMoney(player,currency)))), false);
        }

        return players.size();
    }

    public static void setMoney(Player player, String currency, long money) {
        ShopEconomy.setMoney(player, currency, money);
    }

    public static long getMoney(Player player,String currency) {
        return ShopEconomy.getMoney(player, currency);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        registerCommands(commandSourceStackCommandDispatcher);
    }
}
