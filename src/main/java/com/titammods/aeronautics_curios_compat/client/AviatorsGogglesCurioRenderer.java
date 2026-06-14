package com.titammods.aeronautics_curios_compat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class AviatorsGogglesCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation ARMOR_TEXTURE = ResourceLocation.fromNamespaceAndPath("aeronautics", "textures/models/armor/aviators_goggles_layer_1.png");

    private HumanoidModel<LivingEntity> defaultOuterArmorModel;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack matrixStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        LivingEntity entity = slotContext.entity();
        EntityModel<T> baseModel = renderLayerParent.getModel();

        if (!(baseModel instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }

        if (this.defaultOuterArmorModel == null) {
            this.defaultOuterArmorModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        }

        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> armorModel = (HumanoidModel<LivingEntity>) IClientItemExtensions.of(stack)
                .getHumanoidArmorModel(entity, stack, EquipmentSlot.HEAD, this.defaultOuterArmorModel);

        matrixStack.pushPose();

        @SuppressWarnings("unchecked")
        EntityModel<LivingEntity> entityModel = (EntityModel<LivingEntity>) humanoidModel;
        entityModel.copyPropertiesTo(armorModel);

        armorModel.crouching = humanoidModel.crouching;
        armorModel.riding = humanoidModel.riding;
        armorModel.young = humanoidModel.young;
        armorModel.rightArmPose = humanoidModel.rightArmPose;
        armorModel.leftArmPose = humanoidModel.leftArmPose;

        armorModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        armorModel.setAllVisible(false);
        armorModel.head.visible = true;
        armorModel.hat.visible = true;

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                renderTypeBuffer,
                RenderType.armorCutoutNoCull(ARMOR_TEXTURE),
                stack.hasFoil()
        );

        armorModel.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        matrixStack.popPose();
    }
}