package wraith.waystones.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import wraith.waystones.item.PocketWormholeItem;
import wraith.waystones.registry.CustomScreenHandlerRegistry;

public class PocketWormholeScreenHandler extends UniversalWaystoneScreenHandler {

    public PocketWormholeScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, syncId, inventory.player);
    }

    @Override
    public void onForget(String waystone) {

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        for (var hand : Hand.values()) {
            if (player.getStackInHand(hand).getItem() instanceof PocketWormholeItem) {
                return true;
            }
        }
        return false;
    }

}
