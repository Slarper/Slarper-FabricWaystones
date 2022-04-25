package wraith.waystones.util;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import wraith.waystones.registry.BlockRegistry;

public class CustomItemGroup {

    public static final ItemGroup WAYSTONE_GROUP = FabricItemGroupBuilder.create(Utils.ID("waystones")).icon(() -> new ItemStack(BlockRegistry.WAYSTONE)).build();

}
