package net.sixk.sdmshop;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.sixk.sdmshop.compat.ftbquests.FTBIntegrationHelper;
import net.sixk.sdmshop.config.ShopConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.sixik.sdmcore.impl.utils.serializer.DataIO;
import net.sixik.sdmcore.impl.utils.serializer.data.IData;
import net.sixk.sdmshop.shop.ShopComands;
import net.sixk.sdmshop.shop.Tab.TovarTab;
import net.sixk.sdmshop.shop.Tovar.TovarList;
import net.sixk.sdmshop.shop.Tovar.TovarType.TovarCommand;
import net.sixk.sdmshop.shop.Tovar.TovarType.TovarItem;
import net.sixk.sdmshop.shop.Tovar.TovarType.TovarTypeRegister;
import net.sixk.sdmshop.shop.Tovar.TovarType.TovarXP;
import net.sixk.sdmshop.shop.network.ModNetwork;
import net.sixk.sdmshop.shop.network.server.SendEditModeS2C;
import net.sixk.sdmshop.shop.network.server.SendShopDataS2C;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SDMShop {

    public static final String MODID = "sdmshop";
    public static final Logger LOGGER = LoggerFactory.getLogger("SDMShop");

    private static boolean isSerialize;
    private static final Set<UUID> SERVER_EDIT_MODE = ConcurrentHashMap.newKeySet();
    private static volatile boolean CLIENT_EDIT_MODE = false;

    public static void init(){

        ModNetwork.init();

        ShopConfig.init();

        if (Platform.isModLoaded("ftbquests")) {
            FTBIntegrationHelper.init();
        }

        event();

        CommandRegistrationEvent.EVENT.register(ShopComands::registerCommands);

        EnvExecutor.runInEnv(Env.CLIENT,() ->SDMShopClient::init);

        TovarTypeRegister.registerTovar(new TovarItem.Constructor());
        TovarTypeRegister.registerTovar(new TovarXP.Constructor());
        TovarTypeRegister.registerTovar(new TovarCommand.Constructor());
    }

    public static void event(){

        LifecycleEvent.SERVER_STARTED.register((server) -> {
            TovarTab.SERVER = new TovarTab();
            TovarList.SERVER = new TovarList();
            isSerialize = false;

            if(!server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarTab.sdm").toFile().exists()) saveData(server);

            IData w1 = DataIO.read(server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarTab.sdm").toString());

            if (w1 != null) TovarTab.SERVER.deserialize(w1.asKeyMap());

            ShopConfig.ensureDefaultCurrency(server);
        });

        LifecycleEvent.SERVER_STOPPED.register((server) -> {
            if( !server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").toFile().exists()) {

                server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").toFile().mkdir();
            }
            if(TovarTab.SERVER != null) {
                DataIO.write(TovarTab.SERVER.serialize(),server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarTab.sdm").toString());
            }
            if(TovarList.SERVER != null) {
                DataIO.write(TovarList.SERVER.serialize(server.registryAccess()),server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarList.sdm").toString());
            }
        });

        PlayerEvent.PLAYER_JOIN.register((serverPlayer) -> {
            ShopConfig.ensureDefaultCurrency(serverPlayer.getServer());

            IData w2 = DataIO.read(serverPlayer.getServer().getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarList.sdm").toString());
            if (w2 != null && !isSerialize) {
                isSerialize = true;
                TovarList.SERVER.deserialize(w2.asKeyMap(), serverPlayer.getServer().registryAccess());
            }



            NetworkManager.sendToPlayer((ServerPlayer) serverPlayer,new SendShopDataS2C(TovarList.SERVER.serialize(serverPlayer.registryAccess()).asNBT(),TovarTab.SERVER.serialize().asNBT()));
            NetworkManager.sendToPlayer((ServerPlayer) serverPlayer, new SendEditModeS2C(isEditMode(serverPlayer)));
        });

    }

    public static void saveData(MinecraftServer server){

        if( !server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").toFile().exists()) {

            server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").toFile().mkdir();
        }
        if(TovarTab.SERVER != null) {
            DataIO.write(TovarTab.SERVER.serialize(),server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarTab.sdm").toString());
        }

        if(TovarList.SERVER != null) {
            DataIO.write(TovarList.SERVER.serialize(server.registryAccess()),server.getWorldPath(LevelResource.ROOT).resolve("SDMShopData").resolve("SDMTovarList.sdm").toString());
        }
    }

    public static boolean isEditMode(Player player){
        return player != null && SERVER_EDIT_MODE.contains(player.getUUID());
    }

    public static boolean isEditMode(){
        return CLIENT_EDIT_MODE;
    }

    public static void setClientEditMode(boolean value) {
        CLIENT_EDIT_MODE = value;
    }

    public static void setEditMode(ServerPlayer player, boolean value){
        if (value) {
            SERVER_EDIT_MODE.add(player.getUUID());
        } else {
            SERVER_EDIT_MODE.remove(player.getUUID());
        }

        NetworkManager.sendToPlayer(player, new SendEditModeS2C(value));
    }
}
