/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/entity/EntityUtils.java
 */
package kassuk.addon.blackout.utils.meteor;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BOEntityUtils {
    public static boolean intersectsWithEntity(AABB box, Predicate<Entity> predicate, Map<AbstractClientPlayer, AABB> customBoxes) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            AABB entityBox = entity instanceof Player && customBoxes.containsKey(entity)
                ? customBoxes.get(entity)
                : entity.getBoundingBox();
            if (entityBox.intersects(box) && predicate.test(entity)) {
                return true;
            }
        }
        return false;
    }
}
