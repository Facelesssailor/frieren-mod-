package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class NewSpell extends AbstractSpell {
    public final NewSpell.K kind;

    public NewSpell(NewSpell.K var1) {
        this.kind = var1;
        this.baseManaCost = var1.cost;
        this.manaCostPerLevel = var1.levels > 1 ? 15 : 0;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return FrierenArcana.id(this.kind.path);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
            .setMinRarity(this.kind.rarity)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(this.kind.levels)
            .setCooldownSeconds((double)this.kind.cooldown)
            .build();
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int var1, LivingEntity var2) {
        ArrayList var3 = new ArrayList();
        var3.add(Component.translatable("spell.frieren_arcana." + this.kind.path + ".summary"));
        return var3;
    }

    @Override
    public void onCast(Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
        if (var3 instanceof ServerPlayer var6) {
            ServerLevel var7 = var6.serverLevel();
            float var8 = this.getSpellPower(var2, var6);
            switch (this.kind) {
                case BLACK_HOLE:
                    NewMagic.blackHole(this, var6, var7, aim(var6, 26.0, true), var2, var8);
                    break;
                case HEIGHT:
                    NewMagic.height(this, var6, var7, var8);
                    break;
                case GOLEM_FIST:
                    NewMagic.golemFist(this, var6, var7, aim(var6, 24.0, false), var2, var8);
            }
        }

        super.onCast(var1, var2, var3, var4, var5);
    }

    static Vec3 aim(ServerPlayer var0, double var1, boolean var3) {
        Vec3 var4 = var0.getEyePosition();
        Vec3 var5 = var0.getLookAngle();
        Vec3 var6 = var4.add(var5.scale(var1));
        BlockHitResult var7 = var0.level().clip(new ClipContext(var4, var6, Block.COLLIDER, Fluid.NONE, var0));
        Vec3 var8 = var7.getType() == Type.MISS ? var6 : var7.getLocation().subtract(var5.scale(0.4));
        AABB var9 = var0.getBoundingBox().expandTowards(var8.subtract(var4)).inflate(1.5);
        double var10 = Double.POSITIVE_INFINITY;

        for (LivingEntity var13 : var0.level()
            .getEntitiesOfClass(LivingEntity.class, var9, var1x -> var1x != var0 && var1x.isAlive() && !var1x.isSpectator() && !(var1x instanceof ArmorStand))) {
            Optional var14 = var13.getBoundingBox().inflate(0.4).clip(var4, var8);
            if (var14.isPresent() && ((Vec3)var14.get()).distanceToSqr(var4) < var10) {
                var10 = ((Vec3)var14.get()).distanceToSqr(var4);
                var8 = var3 ? var13.getBoundingBox().getCenter() : var13.position();
            }
        }

        if (!var3) {
            BlockHitResult var15 = var0.level().clip(new ClipContext(var8.add(0.0, 1.0, 0.0), var8.add(0.0, -12.0, 0.0), Block.COLLIDER, Fluid.NONE, var0));
            if (var15.getType() != Type.MISS) {
                var8 = var15.getLocation();
            }
        }

        return var8;
    }

    static void hurt(NewSpell var0, ServerPlayer var1, LivingEntity var2, float var3) {
        DamageSources.applyDamage(var2, var3, SpellDamageSource.source(var1, var0));
    }

    public static enum K {
        BLACK_HOLE("black_hole", 110, 3, 22, SpellRarity.EPIC),
        HEIGHT("height_of_magic", 70, 1, 12, SpellRarity.LEGENDARY),
        GOLEM_FIST("golem_fist", 55, 3, 9, SpellRarity.RARE);

        public final String path;
        public final int cost;
        public final int levels;
        public final int cooldown;
        public final SpellRarity rarity;

        private K(String nullxx, int nullxxx, int nullxxxx, int nullxxxxx, SpellRarity nullxxxxxx) {
            this.path = nullxx;
            this.cost = nullxxx;
            this.levels = nullxxxx;
            this.cooldown = nullxxxxx;
            this.rarity = nullxxxxxx;
        }
    }
}
