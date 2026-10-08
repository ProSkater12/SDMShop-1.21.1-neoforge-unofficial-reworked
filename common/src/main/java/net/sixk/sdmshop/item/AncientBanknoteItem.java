package net.sixk.sdmshop.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sixk.sdmshop.shop.ShopEconomy;

public class AncientBanknoteItem extends Item {

    public AncientBanknoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int added = stack.getCount();
            ShopEconomy.addMoney(player, ShopEconomy.DEFAULT_CURRENCY_ID, added);
            long balance = ShopEconomy.getMoney(player, ShopEconomy.DEFAULT_CURRENCY_ID);
            player.sendSystemMessage(Component.translatable("sdm_shop.banknote.deposited", added, balance));
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, level.isClientSide());
    }
}
