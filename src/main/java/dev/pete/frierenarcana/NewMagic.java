package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = "frieren_arcana"
)
public final class NewMagic {
    public static final double FORM = 1.0;
    public static final double SPEED = 12.0;
    public static final double HOLD = 2.4;
    public static final double COLLAPSE = 0.8;
    private static final List<NewMagic.Hole> HOLES = new ArrayList<>();

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

    public static double travel(double var0) {
        return Math.max(0.4, Math.min(2.5, var0 / 12.0));
    }

    static void blackHole(NewSpell var0, ServerPlayer var1, ServerLevel var2, Vec3 var3, int var4, float var5) {
        Vec3 var6 = var1.getLookAngle();
        Vec3 var7 = var1.getEyePosition().add(var6.scale(0.7)).add(0.0, -0.15, 0.0);
        ArcanaNetwork.magicBeam(var2, var7, var3, 7);
        HOLES.add(new NewMagic.Hole(var1, var2, var7, var3, var0, var4, var5, var2.getGameTime()));
        CompoundTag var8 = new CompoundTag();
        var8.putString("kind", "blackhole");
        var8.putDouble("ax", var7.x);
        var8.putDouble("ay", var7.y);
        var8.putDouble("az", var7.z);
        var8.putDouble("bx", var3.x);
        var8.putDouble("by", var3.y);
        var8.putDouble("bz", var3.z);
        PacketDistributor.sendToPlayer(var1, new ArcanaNetwork.Payload(var8));
        var2.playSound(null, BlockPos.containing(var7), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.4F);
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        if (var0.getLevel() instanceof ServerLevel var1 && !HOLES.isEmpty()) {
            long var19 = var1.getGameTime();
            Iterator var4 = HOLES.iterator();

            while (var4.hasNext()) {
                NewMagic.Hole var5 = (NewMagic.Hole)var4.next();
                if (var5.level == var1) {
                    double var6 = (double)(var19 - var5.start) / 20.0;
                    Vec3 var8 = var5.at(var6);
                    double var9 = (var6 < 1.0 ? 2.0 : 6.0) + 1.5 * (double)var5.lvl;
                    boolean var11 = var5.owner.isAlive();

                    for (LivingEntity var13 : var1.getEntitiesOfClass(LivingEntity.class, new AABB(var8, var8).inflate(var9), var1x -> foe(var5.owner, var1x))) {
                        Vec3 var14 = var8.subtract(var13.getBoundingBox().getCenter());
                        double var15 = var14.length();
                        if (!(var15 > var9) && !(var15 < 0.001)) {
                            double var17 = 0.07 + 0.24 * (1.0 - var15 / var9);
                            var13.setDeltaMovement(var13.getDeltaMovement().scale(0.82).add(var14.scale(var17 / var15)));
                            var13.hurtMarked = true;
                            var13.fallDistance = 0.0F;
                            if (var15 < 2.0 && (var19 - var5.start) % 10L == 0L && var11) {
                                NewSpell.hurt(var5.spell, var5.owner, var13, var5.power * 0.35F);
                            }
                        }
                    }

                    if (var6 >= var5.end()) {
                        for (LivingEntity var21 : var1.getEntitiesOfClass(
                            LivingEntity.class, new AABB(var5.b, var5.b).inflate(3.5), var1x -> foe(var5.owner, var1x)
                        )) {
                            Vec3 var22 = var21.getBoundingBox().getCenter().subtract(var5.b);
                            if (var11) {
                                NewSpell.hurt(var5.spell, var5.owner, var21, var5.power * 1.6F);
                            }

                            Vec3 var23 = var22.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : var22.normalize();
                            var21.setDeltaMovement(var23.scale(1.1).add(0.0, 0.35, 0.0));
                            var21.hurtMarked = true;
                        }

                        ArcanaNetwork.effect(var1, var5.b, 25, var5.lvl);
                        var1.playSound(null, BlockPos.containing(var5.b), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
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
        final Vec3 a;
        final Vec3 b;
        final NewSpell spell;
        final int lvl;
        final float power;
        final long start;
        final double travel;

        Hole(ServerPlayer var1, ServerLevel var2, Vec3 var3, Vec3 var4, NewSpell var5, int var6, float var7, long var8) {
            this.owner = var1;
            this.level = var2;
            this.a = var3;
            this.b = var4;
            this.spell = var5;
            this.lvl = var6;
            this.power = var7;
            this.start = var8;
            this.travel = NewMagic.travel(var3.distanceTo(var4));
        }

        Vec3 at(double var1) {
            return var1 <= 1.0 ? this.a : this.a.lerp(this.b, Math.min(1.0, (var1 - 1.0) / this.travel));
        }

        double end() {
            return 1.0 + this.travel + 2.4;
        }
    }
}
