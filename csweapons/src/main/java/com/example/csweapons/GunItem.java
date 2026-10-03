package com.example.csweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Hitscan gun with a magazine. Right-click fires, R reloads (bullets come from the inventory).
 * The ammo bar under the item icon shows rounds left in the magazine.
 */
public class GunItem extends Item {
    private static final float HEADSHOT_MULTIPLIER = 2.5f;

    private final GunStats stats;

    public GunItem(Settings settings, GunStats stats) {
        super(settings);
        this.stats = stats;
    }

    public GunStats stats() {
        return stats;
    }

    public static int getAmmo(ItemStack stack) {
        return stack.getOrDefault(ModComponents.AMMO, 0);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        stack.set(ModComponents.AMMO, ammo);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity player)) {
            return ActionResult.SUCCESS; // client: just swing the arm
        }
        ItemStack gun = user.getStackInHand(hand);

        if (Reloader.isReloading(player)) return ActionResult.FAIL;

        int ammo = getAmmo(gun);
        if (ammo <= 0) {
            sw.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0f, 1.8f);
            Reloader.start(player); // auto-reload on empty
            return ActionResult.FAIL;
        }

        setAmmo(gun, ammo - 1);
        user.getItemCooldownManager().set(gun, stats.cooldown());
        SoundEvent sound = stats.loud() ? SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST
                                        : SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST;
        sw.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundCategory.PLAYERS,
                stats.volume(), stats.pitch());

        fire(sw, user);
        return ActionResult.SUCCESS;
    }

    private float effectiveSpread(PlayerEntity p) {
        float s = stats.spread();
        if (stats.scoped()) return p.isSneaking() ? 0f : s;   // snipers are only accurate when scoped
        if (p.isSneaking()) s *= 0.5f;
        else if (p.isSprinting()) s *= 2.5f;
        else if (!p.isOnGround()) s *= 2.0f;
        return s;
    }

    private void fire(ServerWorld sw, PlayerEntity user) {
        Vec3d start = user.getEyePos();
        Vec3d dir = user.getRotationVec(1.0f);

        float spread = effectiveSpread(user);
        if (spread > 0f) {
            Random r = sw.getRandom();
            dir = dir.add(r.nextGaussian() * spread, r.nextGaussian() * spread, r.nextGaussian() * spread).normalize();
        }
        Vec3d end = start.add(dir.multiply(stats.range()));

        BlockHitResult blockHit = sw.raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
        Vec3d stop = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getPos();
        double dist = start.distanceTo(stop);

        Box box = user.getBoundingBox().stretch(dir.multiply(dist)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(user, start, stop, box,
                e -> !e.isSpectator() && e.canHit(), dist * dist);

        for (double d = 1.5; d < dist; d += 1.5) {
            Vec3d p = start.add(dir.multiply(d));
            sw.spawnParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }

        if (hit != null) {
            Entity target = hit.getEntity();
            float dmg = stats.damage();
            if (target instanceof LivingEntity le) {
                if (hit.getPos().y > le.getEyeY() - 0.2) {
                    dmg *= HEADSHOT_MULTIPLIER;
                    user.sendMessage(Text.literal("HEADSHOT"), true);
                    sw.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                le.timeUntilRegen = 0; // let fast guns land every shot
            }
            target.damage(sw, sw.getDamageSources().playerAttack(user), dmg);
        }
    }

    // ammo bar under the icon = rounds in the magazine
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0f * getAmmo(stack) / stats.magSize());
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFB000;
    }
}
