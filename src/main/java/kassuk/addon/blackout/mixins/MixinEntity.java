package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.AntiCrawl;
import kassuk.addon.blackout.modules.ForceSneak;
import kassuk.addon.blackout.modules.StepPlus;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract boolean hasPose(Pose pose);

    @Shadow
    public abstract Component getName();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract float maxUpStep();

    @Shadow
    public abstract boolean onGround();

    @Shadow
    public abstract AABB getBoundingBox();

    @Inject(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), cancellable = true)
    private void inject(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        StepPlus step = Modules.get().get(StepPlus.class);

        Entity entity = (Entity)(Object)this;
        boolean active = step.isActive() && entity == mc.player;

        if (active && step.slow.get()) {
            step.slowStep(entity, movement, cir);
            return;
        }

        active = active && System.currentTimeMillis() - step.lastStep > step.cooldown.get() * 1000;

        AABB box = this.getBoundingBox();
        List<VoxelShape> list = this.level().getEntityCollisions(entity, box.expandTowards(movement));
        Vec3 vec3d = movement.lengthSqr() == 0.0 ? movement : Entity.collideBoundingBox(entity, movement, box, this.level(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.onGround() || (!active && bl2 && movement.y < 0.0);
        if ((active ? step.height.get() : this.maxUpStep()) > 0.0F && bl4 && (bl || bl3)) {
            Vec3 vec3d2 = Entity.collideBoundingBox(entity, new Vec3(movement.x, active ? step.height.get() : this.maxUpStep(), movement.z), box, this.level(), list);
            Vec3 vec3d3 = Entity.collideBoundingBox(entity, new Vec3(0.0, active ? step.height.get() : this.maxUpStep(), 0.0), box.expandTowards(movement.x, 0.0, movement.z), this.level(), list);
            if (vec3d3.y < (active ? step.height.get() : this.maxUpStep())) {
                Vec3 vec3d4 = Entity.collideBoundingBox(entity, new Vec3(movement.x, 0.0, movement.z), box.move(vec3d3), this.level(), list).add(vec3d3);
                if (vec3d4.horizontalDistanceSqr() > vec3d2.horizontalDistanceSqr()) {
                    vec3d2 = vec3d4;
                }
            }

            if (vec3d2.horizontalDistanceSqr() > vec3d.horizontalDistanceSqr()) {
                Vec3 v = vec3d2.add(Entity.collideBoundingBox(entity, new Vec3(0.0, -vec3d2.y + movement.y, 0.0), box.move(vec3d2), entity.level(), list));
                if (active) step.step(step.getOffsets(v.y));
                cir.setReturnValue(v);
                return;
            }
        }

        cir.setReturnValue(vec3d);
    }

    @Inject(method = "isCrouching", at = @At(value = "RETURN"), cancellable = true)
    private void isSneaking(CallbackInfoReturnable<Boolean> cir) {
        if (mc.player == null || this.getName() != mc.player.getName()) {
            cir.setReturnValue(Modules.get().get(ForceSneak.class).isActive() || this.hasPose(Pose.CROUCHING));
        }
    }

    @Inject(method = "isFree(Lnet/minecraft/world/phys/AABB;)Z", at = @At("RETURN"), cancellable = true)
    private void poseNotCollide(AABB box, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().isActive(AntiCrawl.class)) {
            cir.setReturnValue(true);
        }
    }
}
