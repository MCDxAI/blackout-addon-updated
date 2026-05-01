/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/entity/EntityUtils.java
 */
package kassuk.addon.blackout.utils.meteor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import meteordevelopment.meteorclient.mixin.EntityTrackingSectionAccessor;
import meteordevelopment.meteorclient.mixin.SectionedEntityCacheAccessor;
import meteordevelopment.meteorclient.mixin.SimpleEntityLookupAccessor;
import meteordevelopment.meteorclient.mixin.WorldAccessor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BOEntityUtils {
    public static boolean intersectsWithEntity(AABB box, Predicate<Entity> predicate, Map<AbstractClientPlayer, AABB> customBoxes) {
        LevelEntityGetter<Entity> entityLookup = ((WorldAccessor) mc.level).meteor$getEntityLookup();

        // Fast implementation using SimpleEntityLookup that returns on the first intersecting entity
        if (entityLookup instanceof LevelEntityGetterAdapter<Entity> simpleEntityLookup) {
            EntitySectionStorage<Entity> cache = ((SimpleEntityLookupAccessor) simpleEntityLookup).meteor$getCache();
            LongSortedSet trackedPositions = ((SectionedEntityCacheAccessor) cache).meteor$getTrackedPositions();
            Long2ObjectMap<EntitySection<Entity>> trackingSections = ((SectionedEntityCacheAccessor) cache).meteor$getTrackingSections();

            int i = SectionPos.posToSectionCoord(box.minX - 2);
            int j = SectionPos.posToSectionCoord(box.minY - 2);
            int k = SectionPos.posToSectionCoord(box.minZ - 2);
            int l = SectionPos.posToSectionCoord(box.maxX + 2);
            int m = SectionPos.posToSectionCoord(box.maxY + 2);
            int n = SectionPos.posToSectionCoord(box.maxZ + 2);

            for (int o = i; o <= l; o++) {
                long p = SectionPos.asLong(o, 0, 0);
                long q = SectionPos.asLong(o, -1, -1);
                LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1).iterator();

                while (longIterator.hasNext()) {
                    long r = longIterator.nextLong();
                    int s = SectionPos.y(r);
                    int t = SectionPos.z(r);

                    if (s >= j && s <= m && t >= k && t <= n) {
                        EntitySection<Entity> entityTrackingSection = trackingSections.get(r);

                        if (entityTrackingSection != null && entityTrackingSection.getStatus().isAccessible()) {
                            for (Entity entity : ((EntityTrackingSectionAccessor) entityTrackingSection).<Entity>meteor$getCollection()) {
                                if ((entity instanceof Player && customBoxes.containsKey(entity) ? customBoxes.get(entity) : entity.getBoundingBox()).intersects(box) && predicate.test(entity)) return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
        // Slow implementation that loops every entity if for some reason the EntityLookup implementation is changed
        AtomicBoolean found = new AtomicBoolean(false);

        entityLookup.get(box, entity -> {
            if (!found.get() && predicate.test(entity)) found.set(true);
        });

        return found.get();
    }
}
