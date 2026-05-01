package kassuk.addon.blackout.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record PlaceData(BlockPos pos, Direction dir, boolean valid) {}
