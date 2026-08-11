/*
 *   This file is a part the best minecraft mod called Blackout Client (https://github.com/KassuK1/Blackout-Client)
 *   and licensed under the GNU GENERAL PUBLIC LICENSE (check LICENCE file or https://www.gnu.org/licenses/gpl-3.0.html)
 *   Copyright (C) 2024 KassuK and OLEPOSSU
 */

package kassuk.addon.blackout.mixins;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author OLEPOSSU
 */
@Mixin(CompoundTag.class)
public interface AccessorCompoundTag {
  @Accessor("tags")
  Map<String, Tag> blackout$getTags();
}
