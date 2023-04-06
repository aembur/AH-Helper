package com.aembr.ahhelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(
        modid = AhHelper.MOD_ID,
        name = AhHelper.MOD_NAME,
        version = AhHelper.VERSION
)
@Mod.EventBusSubscriber
public class AhHelper {
    public static final String MOD_ID = "ah-helper";
    public static final String MOD_NAME = "AH-Helper";
    public static final String VERSION = "1.12.2-0.1.2";

    public static Pattern usesPattern = Pattern.compile("§.(\\d+) §fremaining uses");
    public static Pattern pricePattern = Pattern.compile("§5§o§9Price: §e\\$(.*)");

    public AhHelper() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) {
            return;
        }

        GuiContainer container = (GuiContainer) Minecraft.getMinecraft().currentScreen;

        if (!(container.inventorySlots instanceof ContainerChest)) {
            return;
        }

        String containerName = ((ContainerChest) container.inventorySlots).getLowerChestInventory().getName();

        if (!(containerName.equals("Auction House") || containerName.equals("Your Current Listings"))) {
            return;
        }

        List<String> tooltip = event.getToolTip();
        ItemStack itemStack = event.getItemStack();
        int itemCount = 1; // Default value is 1, will be changed to amount of uses if sellstick
        int totalCount = 0; // Stack size * uses (just stack size if sellstick)
        String priceString = null;
        int totalPrice = 0;
        int priceLineIndex = 0;

        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i);
            Matcher matcher = pricePattern.matcher(line);
            if (matcher.matches()) {
                totalPrice = Integer.parseInt(matcher.group(1).replaceAll(",", ""));
                priceLineIndex = i;
                priceString = matcher.group(0);
                break;
            }
        }

        if (totalPrice == 0) {
            return;
        }

        // Match SellSticks
        if (itemStack.getItem().getUnlocalizedNameInefficiently(itemStack).equals("item.stick")) {
            if (!(itemStack.hasTagCompound())) {
                return;
            }

            NBTTagList lore = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
            for (int j = 0; j < lore.tagCount(); j++) {
                String line = lore.getStringTagAt(j);
                Matcher matcher = usesPattern.matcher(line);
                if (matcher.matches()) {
                    itemCount = Integer.parseInt(matcher.group(1));
                }
            }
        }

        totalCount = itemStack.getCount() * itemCount;
        if (totalCount == 1) {
            return;
        }

        int pricePerItem = totalPrice / totalCount;
        tooltip.set(priceLineIndex, priceString + " §7($" + String.format("%,d", pricePerItem) + " each)");
    }
}
