package com.titammods.aeronautics_curios_compat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterEntriesExtension;
import com.titammods.aeronautics_curios_compat.util.DummyLevel;
import com.titammods.aeronautics_curios_compat.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mixin(LinkedTypewriterBlockEntity.class)
public abstract class MixinLinkedTypewriterBlockEntity implements ILinkedTypewriterBlockEntityExtension {

    @Shadow private UUID currentUser;
    @Shadow private LinkedTypewriterEntries entryMap;
    @Shadow private String typedEntry;

    @Mutable
    @Shadow @Final
    private List<Integer> pressedKeys;

    @Unique public boolean acc$runOnServer = false;
    @Unique public boolean acc$clientCheck = false;

    @Override
    public void saveAdditional(ItemStack stack) {
        LinkedTypewriterBlockEntity self = (LinkedTypewriterBlockEntity)(Object) this;
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag compoundTag = customData != null ? customData.copyTag() : new CompoundTag();
        if (this.currentUser != null) {
            compoundTag.putUUID("currentUser", this.currentUser);
        } else {
            compoundTag.remove("currentUser");
        }
        compoundTag.putString("typedEntry", this.typedEntry);
        if (self.getLevel() != null) {
            compoundTag.put("Keys", this.entryMap.saveKeys(self.getLevel().registryAccess()));
            compoundTag.putIntArray("PressedKeys", this.pressedKeys);
        }
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(compoundTag));
    }

    @Override
    public void loadAdditional(ItemStack stack, Level level, BlockPos pos) {
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            this.currentUser = tag.contains("currentUser") ? tag.getUUID("currentUser") : null;
            this.typedEntry = tag.getString("typedEntry");
            this.entryMap = LinkedTypewriterEntries.readKeys(level.registryAccess(), tag.getList("Keys", 10), pos);
            List<Integer> t = Arrays.stream(tag.getIntArray("PressedKeys")).boxed().toList();
            this.pressedKeys = !t.isEmpty() ? new ArrayList<>(t) : new ArrayList<>();
        }
    }

    @Override
    public boolean isRunOnServer(DummyLevel dummyLevel) {
        boolean last = this.acc$runOnServer;
        this.acc$runOnServer = !dummyLevel.isClientSide();
        return !last && this.acc$runOnServer;
    }

    @Override
    public void setClientCheck(boolean b) {
        this.acc$clientCheck = b;
    }

    @ModifyArg(
            method = "checkAndStartUsing",
            at = @At(value = "INVOKE",
                    target = "Ldev/simulated_team/simulated/mixin_interface/PlayerTypewriterExtension;simulated$setCurrentTypewriter(Lnet/minecraft/core/BlockPos;)V"),
            require = 0)
    public BlockPos acc$setCurrentTypewriter(BlockPos pos, @Local(name = "player") PlayerTypewriterExtension player) {
        return pos != null ? pos : (player != null ? ((Player) player).blockPosition() : null);
    }

    @ModifyExpressionValue(
            method = {"tick", "checkAndStartUsing", "disconnectUser"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD))
    public boolean acc$isClientSide(boolean original) {
        BlockEntity self = (BlockEntity)(Object) this;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(self);
        return dummyLevel != null ? dummyLevel.isClientSide() : original;
    }

    @WrapWithCondition(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Ldev/simulated_team/simulated/content/blocks/redstone/linked_typewriter/LinkedTypewriterEntries;updateNetworks(Lnet/minecraft/world/level/Level;)V"))
    public boolean acc$updateNetworks(LinkedTypewriterEntries instance, Level level) {
        BlockEntity self = (BlockEntity)(Object) this;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(self);
        if (dummyLevel != null) {
            ((ILinkedTypewriterEntriesExtension) entryMap).acc$setIsClientSide(false);
            entryMap.updateNetworks(level);
        }
        return dummyLevel == null;
    }

    // Substituto do @Definition/@Expression (requer MixinExtras 0.5.3 que não temos)
    // Injeta no HEAD de checkAndStartUsing e intercepta o caso do DummyLevel
    @Inject(method = "checkAndStartUsing", at = @At("HEAD"), cancellable = true)
    public void acc$isCurrentUserNull(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        LinkedTypewriterBlockEntity lbe = (LinkedTypewriterBlockEntity)(Object) this;
        Level level = lbe.getLevel();
        if (level == null) return;
        Player player = level.getPlayerByUUID(uuid);
        if (player == null) return;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player, false);
        if (dummyLevel == null) return;
        boolean b = isRunOnServer(dummyLevel);
        if (b) {
            cir.setReturnValue(this.acc$clientCheck);
        }
    }

    @ModifyExpressionValue(
            method = "checkAndStartUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            require = 0)
    public BlockEntity acc$getBlockEntity(BlockEntity original, @Local(name = "player") PlayerTypewriterExtension player) {
        if (original != null) return original;
        if (player != null) {
            Player p = (Player) player;
            return LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(p.blockPosition(), p);
        }
        return null;
    }

    @Inject(method = "disconnectUser", at = @At("HEAD"))
    public void acc$disconnectUser(CallbackInfo ci) {
        LinkedTypewriterBlockEntity lbe = (LinkedTypewriterBlockEntity)(Object) this;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor((BlockEntity)(Object) this);
        if (dummyLevel != null) {
            Level level = dummyLevel.getLevel();
            if (level != null) {
                Player player = level.getPlayerByUUID(this.currentUser);
                if (player != null) {
                    dummyLevel.removeBlockEntity(player.blockPosition(), player);
                }
            }
        }
    }
}