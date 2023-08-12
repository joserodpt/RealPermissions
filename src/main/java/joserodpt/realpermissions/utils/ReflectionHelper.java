package joserodpt.realpermissions.utils;

import joserodpt.realpermissions.RealPermissions;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ReflectionHelper
{
    public static Class<?> getNMSClass(final String str) {
        return getClass("net.minecraft.server." + getServerVersion() + "." + str);
    }
    
    public static Class<?> getCraftBukkitClass(final String str) {
        return getClass("org.bukkit.craftbukkit." + getServerVersion() + "." + str);
    }
    
    public static Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        }
        catch (Exception ex) {
            RealPermissions.getPlugin().getLogger().severe("Error while executing reflection (getClass) nms.");
            RealPermissions.getPlugin().getLogger().severe(ex.toString());
            return null;
        }
    }

    public static void setField(Class<?> clazz, Object instance, Object var, String varname) {
        try {
            Field f = clazz.getDeclaredField(varname);
            f.setAccessible(true);
            f.set(instance, var);
        } catch (Exception ignored) {
        }
    }

    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }
}