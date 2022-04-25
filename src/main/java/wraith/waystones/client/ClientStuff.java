package wraith.waystones.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public final class ClientStuff {

    public static void playSound(SoundEvent sound, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, pitch));
    }

    public static int getWaystoneCount() {
        if (WaystonesClient.WAYSTONE_STORAGE != null) {
            return WaystonesClient.WAYSTONE_STORAGE.getHashCount();
        }
        return -1;
    }

    @Nullable
    public static HashSet<String> getWaystoneHashes() {
        if (WaystonesClient.WAYSTONE_STORAGE != null) {
            return WaystonesClient.WAYSTONE_STORAGE.getAllHashes();
        }
        return null;
    }

}
