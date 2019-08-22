package cn.nukkit.entity.passive;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.mob.EntityWalkingMob;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityShootBowEvent;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntitySnowGolem extends EntityWalkingMob {

    public static final int NETWORK_ID = 21;

    public EntitySnowGolem(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setFriendly(true);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        this.setMaxHealth(4);
    }

    @Override
    public boolean targetOption(EntityCreature creature, double distance) {
        return !(creature instanceof Player) && creature.isAlive() && distance <= 60;
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && Utils.rand(1, 32) < 4 && this.distanceSquared(player) <= 55) {
            this.attackDelay = 0;

            double f = 1.2;
            double yaw = this.yaw + Utils.rand(-120.0, 120.0) / 10;
            double pitch = this.pitch + Utils.rand(-70.0, 70.0) / 10;
            Location location = new Location(this.x + (-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5), this.y + this.getEyeHeight(),
                    this.z + (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5), yaw, pitch, this.level);
            Entity k = Entity.createEntity("Snowball", location, this);
            if (k == null) {
                return;
            }

            EntitySnowball snowball = (EntitySnowball) k;
            snowball.setMotion(new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f));

            Vector3 motion = new Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
                    Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f).multiply(f);
            snowball.setMotion(motion);

            EntityShootBowEvent ev = new EntityShootBowEvent(this, Item.get(Item.ARROW, 0, 1), snowball, f);
            this.server.getPluginManager().callEvent(ev);

            EntityProjectile projectile = ev.getProjectile();
            if (ev.isCancelled()) {
                projectile.kill();
            } else if (projectile != null) {
                ProjectileLaunchEvent launch = new ProjectileLaunchEvent(projectile);
                this.server.getPluginManager().callEvent(launch);
                if (launch.isCancelled()) {
                    projectile.kill();
                } else {
                    projectile.spawnToAll();
                }
            }
        }
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (this.lastDamageCause instanceof EntityDamageByEntityEvent && !this.isBaby()) {
            for (int i = 0; i < Utils.rand(0, 15); i++) {
                drops.add(Item.get(Item.SNOWBALL, 0, 1));
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return 0;
    }

    @Override
    public String getName() {
        return "Snow Golem";
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (this.age % 20 == 0 && (this.level.isNether || (this.level.isRaining() && this.level.canBlockSeeSky(this)))) {
            this.attack(new EntityDamageEvent(this, EntityDamageEvent.DamageCause.FIRE_TICK, 1));
        }

        return super.entityBaseTick(tickDiff);
    }
}
