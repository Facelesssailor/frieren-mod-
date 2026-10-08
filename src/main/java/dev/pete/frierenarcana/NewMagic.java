package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;

@EventBusSubscriber(
    modid = "frieren_arcana"
)
public final class NewMagic {
    private static final List<NewMagic.Hole> HOLES = new ArrayList<>();
    static final int HOLE_TICKS = 100;

    private NewMagic() {
    }

    public static void init() {
        for (NewSpell.K var3 : NewSpell.K.values()) {
            FrierenArcana.SPELLS.register(var3.path, () -> new NewSpell(var3));
        }
    }

    private static boolean foe(ServerPlayer var0, LivingEntity var1) {
        return var1 != var0 && var1.isAlive() && !var1.isSpectator() && !(var1 instanceof ArmorStand) && !DamageSources.isFriendlyFireBetween(var0, var1);
    }

    static void blackHole(NewSpell var0, ServerPlayer var1, ServerLevel var2, Vec3 var3, int var4, float var5) {
        Vec3 var6 = var1.getEyePosition().add(var1.getLookAngle().scale(0.6)).add(0.0, -0.3, 0.0);
        ArcanaNetwork.magicBeam(var2, var6, var3, 7);
        ArcanaNetwork.effect(var2, var3, 24, var4);
        HOLES.add(new NewMagic.Hole(var1, var2, var3, var0, var4, var5, var2.getGameTime()));
        var2.playSound(null, BlockPos.containing(var3), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.4F);
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (var0.getLevel() instanceof ServerLevel var1 && !HOLES.isEmpty()) {
            long var18 = var1.getGameTime();
            Iterator var4 = HOLES.iterator();

            while (var4.hasNext()) {
                NewMagic.Hole var5 = (NewMagic.Hole)var4.next();
                if (var5.level == var1) {
                    long var6 = var18 - var5.start;
                    double var8 = 6.0 + 1.5 * (double)var5.lvl;
                    boolean var10 = var5.owner.isAlive();

                    for (LivingEntity var12 : var1.getEntitiesOfClass(
                        LivingEntity.class, new AABB(var5.c, var5.c).inflate(var8), var1x -> foe(var5.owner, var1x)
                    )) {
                        Vec3 var13 = var5.c.subtract(var12.getBoundingBox().getCenter());
                        double var14 = var13.length();
                        if (!(var14 > var8) && !(var14 < 0.001)) {
                            double var16 = 0.07 + 0.22 * (1.0 - var14 / var8);
                            var12.setDeltaMovement(var12.getDeltaMovement().scale(0.82).add(var13.scale(var16 / var14)));
                            var12.hurtMarked = true;
                            var12.fallDistance = 0.0F;
                            if (var14 < 2.0 && var6 % 10L == 0L && var10) {
                                NewSpell.hurt(var5.spell, var5.owner, var12, var5.power * 0.35F);
                            }
                        }
                    }

                    if (var6 >= 100L) {
                        for (LivingEntity var20 : var1.getEntitiesOfClass(
                            LivingEntity.class, new AABB(var5.c, var5.c).inflate(3.5), var1x -> foe(var5.owner, var1x)
                        )) {
                            Vec3 var21 = var20.getBoundingBox().getCenter().subtract(var5.c);
                            if (var10) {
                                NewSpell.hurt(var5.spell, var5.owner, var20, var5.power * 1.6F);
                            }

                            Vec3 var22 = var21.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : var21.normalize();
                            var20.setDeltaMovement(var22.scale(1.1).add(0.0, 0.35, 0.0));
                            var20.hurtMarked = true;
                        }

                        ArcanaNetwork.effect(var1, var5.c, 25, var5.lvl);
                        var1.playSound(null, BlockPos.containing(var5.c), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
                        var4.remove();
                    }
                }
            }

            return;
        }
    }

    static void height(NewSpell var0, ServerPlayer var1, ServerLevel var2, float var3) {
        Vec3 var4 = var1.getEyePosition();
        Vec3 var5 = var1.getLookAngle();
        double var6 = Math.cos(Math.toRadians(35.0));
        double var8 = 18.0;
        ArcanaNetwork.magicBeam(var2, var4, var4.add(var5.scale(14.0)), 8);

        for (LivingEntity var11 : var2.getEntitiesOfClass(LivingEntity.class, var1.getBoundingBox().inflate(var8), var1x -> foe(var1, var1x))) {
            Vec3 var12 = var11.getBoundingBox().getCenter().subtract(var4);
            double var13 = var12.length();
            if (!(var13 > var8) && !(var13 < 0.001) && !(var12.scale(1.0 / var13).dot(var5) < var6) && var1.hasLineOfSight(var11)) {
                double var15 = 2.6 - var13 * 0.08;
                var11.setDeltaMovement(var5.scale(var15).add(0.0, 0.45, 0.0));
                var11.hurtMarked = true;
                NewSpell.hurt(var0, var1, var11, var3 * 0.5F);
                ArcanaNetwork.effect(var2, var11.getBoundingBox().getCenter(), 27, 1);
            }
        }
    }

    static void golemFist(NewSpell var0, ServerPlayer var1, ServerLevel var2, Vec3 var3, int var4, float var5) {
        double var6 = 2.2 + 0.5 * (double)var4;
        ArcanaNetwork.effect(var2, var3, 28, var4);
        var2.playSound(null, BlockPos.containing(var3), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);

        for (LivingEntity var9 : var2.getEntitiesOfClass(LivingEntity.class, new AABB(var3, var3).inflate(var6, var6 + 2.0, var6), var1x -> foe(var1, var1x))) {
            Vec3 var10 = var9.position().subtract(var3);
            if (!(Math.sqrt(var10.x * var10.x + var10.z * var10.z) > var6)) {
                NewSpell.hurt(var0, var1, var9, var5 * 1.5F);
                Vec3 var11 = new Vec3(var10.x, 0.0, var10.z);
                var11 = var11.lengthSqr() < 1.0E-4 ? Vec3.ZERO : var11.normalize().scale(0.35);
                var9.setDeltaMovement(var11.add(0.0, 0.95 + 0.1 * (double)var4, 0.0));
                var9.hurtMarked = true;
            }
        }
    }

    private static final class Hole {
        final ServerPlayer owner;
        final ServerLevel level;
        final Vec3 c;
        final NewSpell spell;
        final int lvl;
        final float power;
        final long start;

        Hole(ServerPlayer var1, ServerLevel var2, Vec3 var3, NewSpell var4, int var5, float var6, long var7) {
            this.owner = var1;
            this.level = var2;
            this.c = var3;
            this.spell = var4;
            this.lvl = var5;
            this.power = var6;
            this.start = var7;
        }
    }
}
