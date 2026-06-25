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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

        ItemStack chestStack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean isElytraEquipped = chestStack.has(DataComponents.GLIDER);

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

    private int findItem(net.minecraft.world.item.Item item) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            if (inv.getNonEquipmentItems().get(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private int findElytra() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            ItemStack stack = inv.getNonEquipmentItems().get(i);
            if (stack.has(DataComponents.GLIDER)) {
                return i;
            }
        }
        return -1;
    }

    private int findAnyChestplate() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            ItemStack stack = inv.getNonEquipmentItems().get(i);
            if (isChestplate(stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.NETHERITE_CHESTPLATE) ||
               stack.is(Items.DIAMOND_CHESTPLATE) ||
               stack.is(Items.IRON_CHESTPLATE) ||
               stack.is(Items.GOLDEN_CHESTPLATE) ||
               stack.is(Items.CHAINMAIL_CHESTPLATE) ||
               stack.is(Items.LEATHER_CHESTPLATE);
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);

        if (closeInventory.get() && mc.screen != null) {
            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
        }
    }
}