package wraith.waystones.mixin;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wraith.waystones.util.Utils;

import java.util.concurrent.Executor;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin {

    @ModifyVariable(method = "populateNoise", at = @At("HEAD"), argsOnly = true)
    public StructureAccessor populateNoise(StructureAccessor sa, Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return Utils.populateNoise(structureAccessor, chunk);
    }

}
