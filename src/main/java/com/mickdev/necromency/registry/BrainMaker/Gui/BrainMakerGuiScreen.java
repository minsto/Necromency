package com.mickdev.necromency.registry.BrainMaker.Gui;


import com.mickdev.necromency.registry.BrainMaker.BrainMakerCraftPayload;
import com.mickdev.necromency.registry.init.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BrainMakerGuiScreen extends AbstractContainerScreen<BrainMakerGuiMenu> {

    private static final ResourceLocation TEX =
            ResourceLocation.parse("necromency:textures/gui/brain_maker_gui.png");

    private Button buttonAdd;

    public BrainMakerGuiScreen(BrainMakerGuiMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 246;
        this.imageHeight = 184;
        this.inventoryLabelY = 9999; // cache texte inventaire vanilla
        this.titleLabelY = 9999;     // cache titre vanilla si tu veux
    }

    @Override
    protected void init() {
        super.init();

        buttonAdd = Button.builder(Component.literal("ADD"), b -> {
            // Envoie au serveur: craft sur CE container
            PacketHandler.sendToServer(new BrainMakerCraftPayload(this.menu.containerId));
        }).bounds(this.leftPos + 97, this.topPos + 77, 40, 20).build();

        this.addRenderableWidget(buttonAdd);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
        super.render(gg, mouseX, mouseY, partialTicks);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTicks, int mouseX, int mouseY) {
        gg.blit(RenderPipelines.GUI_TEXTURED, TEX, this.leftPos, this.topPos,
                0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}
