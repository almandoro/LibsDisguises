package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ReflectionManager {
    private static String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static Class itemClass;
    private static Method soundMethod;
    static {
        for (Method method : getNmsClass("EntityLiving").getDeclaredMethods()) {
            try {
                if (method.getReturnType() == float.class && Modifier.isProtected(method.getModifiers())
                        && method.getParameterTypes().length == 0) {
                    Object entity = getEntityInstance("Pig");
                    method.setAccessible(true);
                    method.invoke(entity);
                    Field random = getNmsClass("Entity").getDeclaredField("random");
                    random.setAccessible(true);
                    random.set(entity, null);
                    method.setAccessible(true);
                    try {
                        method.invoke(entity);
                    } catch (Exception ex) {
                        soundMethod = method;
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            itemClass = getCraftClass("inventory.CraftItemStack");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getEnumArt(Art art) {
        try {
            Class craftArt = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".CraftArt");
            Object enumArt = craftArt.getMethod("BukkitToNotch", Art.class).invoke(null, art);
            for (Field field : enumArt.getClass().getFields()) {
                if (field.getType() == String.class) {
                    return (String) field.get(enumArt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getNmsEntity(Entity entity) {
        try {
            return getCraftClass("entity.CraftEntity").getMethod("getHandle").invoke(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getCraftSound(Sound sound) {
        try {
            Class c = getCraftClass("CraftSound");
            return (String) c.getMethod("getSound", Sound.class).invoke(null, sound);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getEntityInstance(String entityName) {
        try {
            Class entityClass = getNmsClass("Entity" + entityName);
            Object entityObject;
            Object world = getWorld();
            if (entityName.equals("Player")) {
                Object minecraftServer = getNmsClass("MinecraftServer").getMethod("getServer").invoke(null);
                Object playerinteractmanager = getNmsClass("PlayerInteractManager").getConstructor(getNmsClass("World"))
                        .newInstance(world);
                entityObject = entityClass.getConstructor(getNmsClass("MinecraftServer"), getNmsClass("World"), String.class,
                        playerinteractmanager.getClass()).newInstance(minecraftServer, world, "LibsDisguises",
                        playerinteractmanager);
            } else {
                entityObject = entityClass.getConstructor(getNmsClass("World")).newInstance(world);
            }
            return entityObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getNmsClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + bukkitVersion + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + bukkitVersion + "." + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Float getSoundModifier(Object entity) {
        try {
            // TODO Update this each update!
            soundMethod.setAccessible(true);
            return (Float) soundMethod.invoke(entity);
        } catch (Exception ex) {
        }
        return null;
    }

    private static Object getWorld() {
        return getWorld(Bukkit.getWorlds().get(0));
    }

    public static Object getWorld(World world) {
        try {
            return getCraftClass("CraftWorld").getMethod("getHandle").invoke(world);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getNmsItem(ItemStack itemstack) {
        try {
            return itemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack getBukkitItem(Object nmsItem) {
        try {
            return (ItemStack) itemClass.getMethod("asBukkitCopy", getNmsClass("ItemStack")).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
