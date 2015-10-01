package de.robingrether.idisguise.management.impl.v1_8_R3;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.robingrether.idisguise.disguise.ColoredDisguise;
import de.robingrether.idisguise.disguise.CreeperDisguise;
import de.robingrether.idisguise.disguise.Disguise;
import de.robingrether.idisguise.disguise.DisguiseType;
import de.robingrether.idisguise.disguise.EndermanDisguise;
import de.robingrether.idisguise.disguise.GuardianDisguise;
import de.robingrether.idisguise.disguise.HorseDisguise;
import de.robingrether.idisguise.disguise.MobDisguise;
import de.robingrether.idisguise.disguise.OcelotDisguise;
import de.robingrether.idisguise.disguise.PigDisguise;
import de.robingrether.idisguise.disguise.PlayerDisguise;
import de.robingrether.idisguise.disguise.RabbitDisguise;
import de.robingrether.idisguise.disguise.SizedDisguise;
import de.robingrether.idisguise.disguise.SkeletonDisguise;
import de.robingrether.idisguise.disguise.VillagerDisguise;
import de.robingrether.idisguise.disguise.WolfDisguise;
import de.robingrether.idisguise.disguise.ZombieDisguise;
import de.robingrether.idisguise.management.PacketHelper;
import de.robingrether.idisguise.management.PlayerHelper;
import de.robingrether.idisguise.management.VersionHelper;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.EntityAgeable;
import net.minecraft.server.v1_8_R3.EntityBat;
import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.EntityEnderman;
import net.minecraft.server.v1_8_R3.EntityGuardian;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityOcelot;
import net.minecraft.server.v1_8_R3.EntityPig;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityRabbit;
import net.minecraft.server.v1_8_R3.EntitySheep;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.EntityWolf;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.EnumColor;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.World;

public class PacketHelperImpl extends PacketHelper {
	
	private Field fieldUUID;
	
	public PacketHelperImpl() {
		try {
			fieldUUID = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("b");
			fieldUUID.setAccessible(true);
		} catch(Exception e) {
		}
	}
	
	public Packet<?> getPacket(Player player, Disguise disguise) {
		if(disguise == null) {
			return null;
		}
		EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
		DisguiseType type = disguise.getType();
		Packet<?> packet = null;
		if(disguise instanceof MobDisguise) {
			MobDisguise mobDisguise = (MobDisguise)disguise;
			EntityInsentient entity;
			try {
				entity = (EntityInsentient)type.getClass(VersionHelper.getNMSPackage()).getConstructor(World.class).newInstance(entityPlayer.getWorld());
			} catch(Exception e) {
				entity = null;
			}
			if(mobDisguise.getCustomName() != null && !mobDisguise.getCustomName().isEmpty()) {
				entity.setCustomName(mobDisguise.getCustomName());
			}
			if(entity instanceof EntityAgeable && !mobDisguise.isAdult()) {
				((EntityAgeable)entity).setAge(-24000);
			}
			Location location = player.getLocation();
			entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			entity.d(entityPlayer.getId());
			if(mobDisguise instanceof ColoredDisguise) {
				if(entity instanceof EntitySheep) {
					((EntitySheep)entity).setColor(EnumColor.fromColorIndex(((ColoredDisguise)mobDisguise).getColor().getData()));
				}
				if(mobDisguise instanceof WolfDisguise) {
					if(entity instanceof EntityWolf) {
						WolfDisguise wolfDisguise = (WolfDisguise)mobDisguise;
						EntityWolf wolf = (EntityWolf)entity;
						wolf.setCollarColor(EnumColor.fromColorIndex(wolfDisguise.getColor().getData()));
						wolf.setTamed(wolfDisguise.isTamed());
						wolf.setAngry(wolfDisguise.isAngry());
					}
				}
			} else if(mobDisguise instanceof CreeperDisguise) {
				if(entity instanceof EntityCreeper) {
					((EntityCreeper)entity).setPowered(((CreeperDisguise)mobDisguise).isPowered());
				}
			} else if(mobDisguise instanceof EndermanDisguise) {
				if(entity instanceof EntityEnderman) {
					EndermanDisguise endermanDisguise = (EndermanDisguise)mobDisguise;
					((EntityEnderman)entity).setCarried(Block.getById(endermanDisguise.getBlockInHand().getId()).fromLegacyData(endermanDisguise.getBlockInHandData()));
				}
			} else if(mobDisguise instanceof GuardianDisguise) {
				if(entity instanceof EntityGuardian) {
					((EntityGuardian)entity).setElder(((GuardianDisguise)mobDisguise).isElder());
				}
			} else if(mobDisguise instanceof HorseDisguise) {
				if(entity instanceof EntityHorse) {
					HorseDisguise horseDisguise = (HorseDisguise)mobDisguise;
					EntityHorse horse = (EntityHorse)entity;
					horse.setType(horseDisguise.getVariant().ordinal());
					horse.setVariant(horseDisguise.getColor().ordinal() & 0xFF | horseDisguise.getStyle().ordinal() << 8);
					horse.inventoryChest.setItem(0, horseDisguise.isSaddled() ? CraftItemStack.asNMSCopy(new ItemStack(Material.SADDLE)) : null);
					horse.inventoryChest.setItem(1, CraftItemStack.asNMSCopy(horseDisguise.getArmor().getItem()));
					horse.setHasChest(horseDisguise.hasChest());
				}
			} else if(mobDisguise instanceof OcelotDisguise) {
				if(entity instanceof EntityOcelot) {
					((EntityOcelot)entity).setCatType(((OcelotDisguise)mobDisguise).getCatType().getId());
				}
			} else if(mobDisguise instanceof PigDisguise) {
				if(entity instanceof EntityPig) {
					((EntityPig)entity).setSaddle(((PigDisguise)mobDisguise).isSaddled());
				}
			} else if(mobDisguise instanceof RabbitDisguise) {
				if(entity instanceof EntityRabbit) {
					((EntityRabbit)entity).setRabbitType(((RabbitDisguise)mobDisguise).getRabbitType().getId());
				}
			} else if(mobDisguise instanceof SizedDisguise) {
				if(entity instanceof EntitySlime) {
					((EntitySlime)entity).setSize(((SizedDisguise)mobDisguise).getSize());
				}
			} else if(mobDisguise instanceof SkeletonDisguise) {
				if(entity instanceof EntitySkeleton) {
					((EntitySkeleton)entity).setSkeletonType(((SkeletonDisguise)mobDisguise).getSkeletonType().getId());
				}
			} else if(mobDisguise instanceof VillagerDisguise) {
				if(entity instanceof EntityVillager) {
					((EntityVillager)entity).setProfession(((VillagerDisguise)mobDisguise).getProfession().getId());
				}
			} else if(mobDisguise instanceof ZombieDisguise) {
				if(entity instanceof EntityZombie) {
					ZombieDisguise zombieDisguise = (ZombieDisguise)mobDisguise;
					EntityZombie zombie = (EntityZombie)entity;
					zombie.setBaby(!zombieDisguise.isAdult());
					zombie.setVillager(zombieDisguise.isVillager());
				}
			}
			if(entity instanceof EntityBat) {
				((EntityBat)entity).setAsleep(false);
			}
			if(attributes[0]) {
				entity.setCustomName(player.getName());
			}
			packet = new PacketPlayOutSpawnEntityLiving(entity);
		} else if(disguise instanceof PlayerDisguise) {
			packet = new PacketPlayOutNamedEntitySpawn(((CraftPlayer)player).getHandle());
			try {
				fieldUUID.set(packet, PlayerHelper.instance.getUniqueId(((PlayerDisguise)disguise).getName()));
			} catch(Exception e) {
			}
		}
		return packet;
	}
	
}