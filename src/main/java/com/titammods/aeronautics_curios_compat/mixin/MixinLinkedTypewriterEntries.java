package com.titammods.aeronautics_curios_compat.mixin;

import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterEntriesExtension;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LinkedTypewriterEntries.class)
public class MixinLinkedTypewriterEntries implements ILinkedTypewriterEntriesExtension {

    @Unique
    private boolean acc$isClientSide = true;

    @Override
    public LinkedTypewriterEntries acc$setIsClientSide(boolean b) {
        this.acc$isClientSide = b;
        return (LinkedTypewriterEntries)(Object) this;
    }
}