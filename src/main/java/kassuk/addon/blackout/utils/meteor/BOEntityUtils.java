/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/entity/EntityUtils.java
 */
package kassuk.addon.blackout.utils.meteor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.mixin.EntitySectionAccessor;
import meteordevelopment.meteorclient.mixin.EntitySectionStorageAccessor;
import meteordevelopment.meteorclient.mixin.LevelAccessor;
import meteordevelopment.meteorclient.mixin.LevelEntityGetterAdapterAccessor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.phys.AABB;

public class BOEntityUtils {

  /**
   * Section-scoped, allocation-free entity-intersection test. It returns on the first hit.
   *
   * <p>The intersection check uses the custom or extrapolated box when one is present. Otherwise it
   * uses the entity's real bounding box. The check iterates the raw entity sections, so it can
   * still see entities that only have an extrapolated box.
   *
   * <p>Commits {@code f67c37c} and {@code 1a53cd9} removed this fast path. A Meteor accessor rename
   * looked like a removal, but it was not. The "behavior preserved exactly" claim in {@code
   * 1a53cd9} was false. {@code Level#getEntities} pre-filters by each entity's real bounding box,
   * so it silently drops players whose extrapolated box intersects but whose real box does not.
   * This regressed AutoCrystal and HoleFill extrapolation accuracy, and it allocated a List per
   * call.
   *
   * <p>The code below restores the fast path with Meteor's renamed accessors ({@link
   * LevelAccessor}, {@link LevelEntityGetterAdapterAccessor}, {@link EntitySectionStorageAccessor},
   * {@link EntitySectionAccessor}). Do not replace it with {@code Level#getEntities}.
   *
   * @param box the query volume
   * @param predicate an extra per-entity filter
   * @param customBoxes extrapolated boxes indexed by player; the check prefers these to the real
   *     box
   * @return {@code true} on the first entity that intersects and matches the predicate
   */
  public static boolean intersectsWithEntity(
      AABB box, Predicate<Entity> predicate, Map<AbstractClientPlayer, AABB> customBoxes) {
    LevelEntityGetter<Entity> entityLookup = ((LevelAccessor) mc.level).meteor$getEntityLookup();

    // Fast path: iterate only the nearby sections. Return on the first entity that intersects.
    if (entityLookup instanceof LevelEntityGetterAdapter adapter) {
      EntitySectionStorage<Entity> sectionStorage =
          ((LevelEntityGetterAdapterAccessor) adapter).<Entity>meteor$getSectionStorage();
      LongSortedSet sectionIds =
          ((EntitySectionStorageAccessor) sectionStorage).meteor$getSectionIds();
      Long2ObjectMap<EntitySection<Entity>> sections =
          ((EntitySectionStorageAccessor) sectionStorage).<Entity>meteor$getSections();

      int i = SectionPos.posToSectionCoord(box.minX - 2);
      int j = SectionPos.posToSectionCoord(box.minY - 2);
      int k = SectionPos.posToSectionCoord(box.minZ - 2);
      int l = SectionPos.posToSectionCoord(box.maxX + 2);
      int m = SectionPos.posToSectionCoord(box.maxY + 2);
      int n = SectionPos.posToSectionCoord(box.maxZ + 2);

      for (int o = i; o <= l; o++) {
        long p = SectionPos.asLong(o, 0, 0);
        long q = SectionPos.asLong(o, -1, -1);
        LongBidirectionalIterator longIterator = sectionIds.subSet(p, q + 1).iterator();

        while (longIterator.hasNext()) {
          long r = longIterator.nextLong();
          int s = SectionPos.y(r);
          int t = SectionPos.z(r);

          if (s >= j && s <= m && t >= k && t <= n) {
            EntitySection<Entity> section = sections.get(r);

            if (section != null && section.getStatus().isAccessible()) {
              for (Entity entity : ((EntitySectionAccessor) section).<Entity>meteor$getStorage()) {
                AABB entityBox =
                    entity instanceof Player && customBoxes.containsKey(entity)
                        ? customBoxes.get(entity)
                        : entity.getBoundingBox();
                if (entityBox.intersects(box) && predicate.test(entity)) {
                  return true;
                }
              }
            }
          }
        }
      }

      return false;
    }

    // Slow fallback for non-adapter lookups. It does not apply custom boxes. Keep it for
    // correctness.
    AtomicBoolean found = new AtomicBoolean(false);
    entityLookup.get(
        box,
        entity -> {
          if (!found.get() && predicate.test(entity)) {
            found.set(true);
          }
        });
    return found.get();
  }
}
