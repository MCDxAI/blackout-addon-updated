/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/entity/EntityUtils.java
 */
package kassuk.addon.blackout.utils.meteor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class BOEntityUtils {
  public static boolean intersectsWithEntity(
      AABB box, Predicate<Entity> predicate, Map<AbstractClientPlayer, AABB> customBoxes) {
    for (Entity entity :
        mc.level.getEntities(
            (Entity) null,
            box,
            e -> {
              AABB entityBox =
                  e instanceof Player && customBoxes.containsKey(e)
                      ? customBoxes.get(e)
                      : e.getBoundingBox();
              return entityBox.intersects(box) && predicate.test(e);
            })) {
      return true;
    }
    return false;
  }
}
