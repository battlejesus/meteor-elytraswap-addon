package com.elytraswap.modules;

import com.elytraswap.ElytraSwapAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import meteordevelopment.meteorclient.systems.modules.Categories;

public class ElytraSwap extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> closeInventory = sgGeneral.add(new BoolSetting.Builder()
        .name("close-inventory")
        .description("It automatically closes the inventory after the swap transaction.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> preferNetherite = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-netherite")
        .description("If possible, he prefers the Netherite Chestplate.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Keybind> swapKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("swap-key")
        .description("Key to bind for Elytra ↔ Chestplate swap.")
        .defaultValue(Keybind.fromKey(88)) // Default: X
        .build()
    );

    private boolean wasPressed = false;

    public ElytraSwap() {
        /*super(ElytraSwapAddon.CATEGORY, "elytra-swap", "Tuşa basıldığında Elytra ile Chestplate arasında hızlı swap yapar, Swap key kullanın bind kullanmayın.");*/
        super(Categories.Player, "elytra-swap", "Press the key to quickly swap between Elytra and Chestplate, use the swap key, not the bind.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean isPressed = swapKey.get().isPressed();

        if (isPressed && !wasPressed) {
            performSwap();
        }

        wasPressed = isPressed;
    }

    private void performSwap() {
        if (mc.player == null) return;

        ItemStack chestStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean isElytraEquipped = chestStack.contains(DataComponentTypes.GLIDER);

        if (isElytraEquipped) {
            equipBestChestplate();
        } else {
            equipElytra();
        }
    }

    private boolean equipBestChestplate() {
        int slot = findItem(Items.NETHERITE_CHESTPLATE);
        if (slot != -1 && preferNetherite.get()) {
            equip(slot);
            return true;
        }

        slot = findItem(Items.DIAMOND_CHESTPLATE);
        if (slot != -1) {
            equip(slot);
            return true;
        }

        slot = findAnyChestplate();
        if (slot != -1) {
            equip(slot);
            return true;
        }
        return false;
    }

    private void equipElytra() {
        int slot = findElytra();
        if (slot != -1) {
            equip(slot);
        }
    }

    private int findItem(Item item) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    private int findElytra() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.contains(DataComponentTypes.GLIDER)) {
                return i;
            }
        }
        return -1;
    }

    private int findAnyChestplate() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (isChestplate(stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isOf(Items.NETHERITE_CHESTPLATE) ||
               stack.isOf(Items.DIAMOND_CHESTPLATE) ||
               stack.isOf(Items.IRON_CHESTPLATE) ||
               stack.isOf(Items.GOLDEN_CHESTPLATE) ||
               stack.isOf(Items.CHAINMAIL_CHESTPLATE) ||
               stack.isOf(Items.LEATHER_CHESTPLATE);
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);

        if (closeInventory.get() && mc.currentScreen != null) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }
}