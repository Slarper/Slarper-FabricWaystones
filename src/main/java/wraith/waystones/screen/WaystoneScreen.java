package wraith.waystones.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.client.WaystonesClient;
import wraith.waystones.util.Utils;
import wraith.waystones.util.WaystonePacketHandler;

import java.util.UUID;

public class WaystoneScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/waystone.png");
    private static final Identifier CONFIG_TEXTURE = Utils.ID("textures/gui/waystone_config.png");

    private TextFieldWidget nameField;

    private Button configPage = new Button(156, -17, 18, 18, 207, 0) {
        @Override
        public void onClick() {
            if (!isVisible()) {
                return;
            }
            page = Page.CONFIG;
            backgroundHeight = 125;
            for (Button button : buttons) {
                button.setup();
            }
        }
        @Override
        public boolean isVisible() {
            return page == Page.WAYSTONES;
        }

        @Override
        public void setup() {
            this.tooltip = new TranslatableText("waystones.config.tooltip.config");
        }
    };

    private Page page = Page.WAYSTONES;

    private enum Page {
        WAYSTONES,
        CONFIG
    }

    public WaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);

        buttons.add(configPage);

        buttons.add(new Button(156, -17, 18, 18, 177, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                page = Page.WAYSTONES;
                backgroundHeight = 176;
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(inventory.player);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.back");
            }
        });

        //Reset name
        buttons.add(new Button(8, 103, 13, 13, 177, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setText("");
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.delete_name");
            }
        });

        //Set name
        buttons.add(new ToggleableButton(128, 103, 13, 13, 190, 54, 216, 54) {
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.set_name");
                boolean settable = !((WaystoneScreenHandler)handler).getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                rename();
                boolean settable = !((WaystoneScreenHandler)handler).getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }

        });

        //Randomize name
        buttons.add(new Button(143, 103, 13, 13, 203, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setText(Utils.generateWaystoneName(""));
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.randomize_name");
            }
        });

        //Global Toggle
        buttons.add(new ToggleableButton(8, 48, 17, 17, 212, 0, 195, 0) {

            @Override
            public void setup() {
                this.toggled = ((WaystoneScreenHandler)handler).isGlobal();
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_is_global");
            }
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((WaystoneScreenHandler)handler).toggleGlobal();
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }
        });

        //View discovered
        buttons.add(new ToggleableButton(8, 11, 13, 13, 177, 54, 190, 54) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess)inventory.player).shouldViewDiscoveredWaystones();
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_discovered_view");
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess)inventory.player).toggleViewDiscoveredWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(inventory.player);
                PacketByteBuf packet = PacketByteBufs.create();
                packet.writeNbt(((PlayerEntityMixinAccess)inventory.player).toTagW(new NbtCompound()));
                ClientPlayNetworking.send(WaystonePacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

        });

        //View global
        buttons.add(new ToggleableButton(8, 27, 13, 13, 177, 54, 190, 54) {
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_global_view");
                this.toggled = ((PlayerEntityMixinAccess)inventory.player).shouldViewGlobalWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess)inventory.player).toggleViewGlobalWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(inventory.player);
                PacketByteBuf packet = PacketByteBufs.create();
                packet.writeNbt(((PlayerEntityMixinAccess)inventory.player).toTagW(new NbtCompound()));
                ClientPlayNetworking.send(WaystonePacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }
        });

        //Revoke ownership
        buttons.add(new Button(30, 48, 17, 17, 229, 0) {
            @Override
            public void onClick() {
                super.onClick();
                PacketByteBuf packet = PacketByteBufs.create();
                NbtCompound tag = new NbtCompound();
                tag.putString("waystone_hash", ((WaystoneScreenHandler)handler).getWaystone());
                UUID owner = ((WaystoneScreenHandler)handler).getOwner();
                tag.putUuid("waystone_owner", owner);
                packet.writeNbt(tag);
                ClientPlayNetworking.send(WaystonePacketHandler.REMOVE_WAYSTONE_OWNER, packet);
                ((WaystoneScreenHandler)handler).removeOwner();
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG && ((WaystoneScreenHandler)handler).hasOwner();
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.revoke_ownership");
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new TextFieldWidget(this.textRenderer, this.x + 28, this.y + 106, 93, 10, new LiteralText("")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
                if (bl && button == 1) {
                    this.setText("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }
            @Override
            public boolean changeFocus(boolean lookForwards) {
                return isVisible() && super.changeFocus(lookForwards);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return isVisible() && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            }
        };
        this.nameField.setMaxLength(16);
        this.nameField.setEditableColor(0xFFFFFF);
        this.nameField.setDrawsBackground(false);
        this.nameField.setFocusUnlocked(true);
        String waystone = WaystonesClient.WAYSTONE_STORAGE.getName(((WaystoneScreenHandler)handler).getWaystone());
        this.nameField.setText(waystone == null ? "" : waystone);
        this.nameField.setChangedListener((s) -> {
            boolean settable = !((WaystoneScreenHandler)handler).getName().equals(s);
            ToggleableButton button = ((ToggleableButton)buttons.get(4));
            if (button.isToggled() == settable) {
                button.toggle();
            }
        });
        this.addDrawableChild(this.nameField);
    }

    @Override
    protected boolean searchVisible() {
        return page == Page.WAYSTONES;
    }

    private boolean canEdit() {
        return ((WaystoneScreenHandler)handler).isOwner(inventory.player) || inventory.player.hasPermissionLevel(2);
    }

    @Override
    public void handledScreenTick() {
        if (this.nameField != null && this.nameField.isVisible()) {
            this.nameField.tick();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if (page == Page.WAYSTONES) {
            super.resize(client, width, height);
        } else {
            String string = this.nameField.getText();
            this.init(client, width, height);
            this.nameField.setText(string);
        }
        super.superResize(client, width, height);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (page == Page.WAYSTONES) {
            return super.charTyped(chr, keyCode);
        } else {
            if (this.ignoreTypedCharacter) {
                return false;
            } else {
                return this.nameField.isVisible() && this.nameField.charTyped(chr, keyCode);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (page == Page.WAYSTONES) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            this.ignoreTypedCharacter = false;
            if (InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                if (this.nameField.isVisible() && this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                } else {
                    return this.nameField.isVisible() && this.nameField.isFocused() && this.nameField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            super.drawBackground(matrices, delta, mouseX, mouseY);
        } else {
            this.renderBackground(matrices);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, CONFIG_TEXTURE);
            this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            if (canEdit()) {
                this.drawTexture(matrices, x + 23, y + 103, 0, 125, 103, 13);
            }
            renderButtons(matrices, mouseX, mouseY);
            renderButtonText(matrices);
            String owner = ((WaystoneScreenHandler) handler).getOwnerName();
            if (owner == null || "".equals(owner)) {
                owner = new TranslatableText("waystones.config.no_owner").getString();
            }
            this.textRenderer.draw(matrices, new TranslatableText("waystones.config.owner", owner), this.x + 10, this.y + 75, 0x161616);
            if (this.nameField.isVisible()) {
                this.nameField.render(matrices, mouseX, mouseY, delta);
            }
            renderButtonTooltips(matrices, mouseX, mouseY);
        }
    }

    @Override
    protected void renderWaystoneBackground(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {
        for(int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.backgroundHeight;
            if (((WaystoneScreenHandler)handler).getWaystone().equals(getDiscoveredWaystones().get(n))) {
                s += 18;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            this.drawTexture(matrixStack, x, r - 1, 0, s, 101, 18);
        }
    }

    @Override
    protected boolean tryClick(double mouseX, double mouseY) {
        int i1 = this.x + 24;
        int j1 = this.y + 45;
        int i2 = this.x + 36;
        int j2 = this.y + 39;
        int k = this.scrollOffset + 5;

        int n = getDiscoveredCount();
        for(int l = this.scrollOffset; l < k && l < n; ++l) {
            int m = l - this.scrollOffset;
            double x1 = mouseX - (double)(i1);
            double y1 = mouseY - (double)(j1 + m * 18);

            double x2 = mouseX - (double)(i2);
            double y2 = mouseY - (double)(j2 + m * 18);
            if (m < n && x1 >= 0.0D && y1 >= 0.0D && x1 < 8 && y1 < 8 && (this.handler).onButtonClick(this.client.player, l * 2 + 1)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                this.scrollOffset = Math.max(0, this.scrollOffset - 1);

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", l * 2 + 1);
                PacketByteBuf packet = PacketByteBufs.create().writeNbt(tag);

                ClientPlayNetworking.send(WaystonePacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);

                return true;
            }
            if (((WaystoneScreenHandler)handler).getWaystone().equals(getDiscoveredWaystones().get(l))) {
                continue;
            }
            if (x2 >= 0.0D && y2 >= 0.0D && x2 < 101.0D && y2 < 18.0D && (this.handler).onButtonClick(this.client.player, l * 2)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", l * 2);
                PacketByteBuf packet = PacketByteBufs.create().writeNbt(tag);

                ClientPlayNetworking.send(WaystonePacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);
                return true;
            }
        }

        int i3 = this.x + 141;
        int j3 = this.y + 40;
        if (mouseX >= (double)i3 && mouseX < (double)(i3 + 11) && mouseY >= (double)j3 && mouseY < (double)(j3 + 90)) {
            this.mouseClicked = true;
        }
        return false;
    }

    private void renderButtonText(MatrixStack matrices) {
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_discovered"), this.x + 25, this.y + 14, 0x161616);
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_global"), this.x + 25, this.y + 30, 0x161616);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            this.textRenderer.draw(matrices, ((WaystoneScreenHandler)handler).getName(), this.titleX, this.titleY, 4210752);
        }
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (page == Page.WAYSTONES) {
            super.onMouseClick(slot, invSlot, clickData, actionType);
        } else {
            super.superOnMouseClick(slot, invSlot, clickData, actionType);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (page == Page.WAYSTONES && configPage.isVisible() && configPage.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            configPage.onClick();
            return super.superMouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean canClickWaystones() {
        return page == Page.WAYSTONES;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (page == Page.WAYSTONES) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        } else {
            return super.superMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (page == Page.WAYSTONES) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        } else {
            return super.superMouseScrolled(mouseX, mouseY, amount);
        }
    }

    private void rename() {
        if (!canEdit()) {
            return;
        }
        String name = this.nameField.getText();
        String hash = ((WaystoneScreenHandler)handler).getWaystone();
        UUID owner = ((WaystoneScreenHandler)handler).getOwner();

        if (name == null) {
            name = "";
        }
        ((WaystoneScreenHandler)handler).setName(name);

        PacketByteBuf data = PacketByteBufs.create();

        NbtCompound tag = new NbtCompound();
        tag.putString("waystone_name", name);
        tag.putString("waystone_hash", hash);
        if (owner != null) {
            tag.putUuid("waystone_owner", owner);
        }
        data.writeNbt(tag);

        ClientPlayNetworking.send(WaystonePacketHandler.RENAME_WAYSTONE, data);
    }

}