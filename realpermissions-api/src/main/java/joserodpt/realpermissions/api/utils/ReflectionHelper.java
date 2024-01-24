package joserodpt.realpermissions.api.utils;

import joserodpt.realpermissions.api.RealPermissionsAPI;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ReflectionHelper
{
    public static Class<?> getCraftBukkitClass(final String str) {
        return getClass("org.bukkit.craftbukkit." + getServerVersion() + "." + str);
    }
    
    public static Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        }
        catch (Exception ex) {
            RealPermissionsAPI.getInstance().getLogger().severe("Error while executing reflection (getClass) nms.");
            RealPermissionsAPI.getInstance().getLogger().severe(ex.toString());
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