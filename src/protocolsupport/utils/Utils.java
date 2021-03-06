package protocolsupport.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;

import net.minecraft.server.v1_10_R1.MinecraftServer;

public class Utils {

	public static <K, V> V getOrCreateDefault(Map<K, V> map, K key, V defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			map.put(key, defaultValue);
			return defaultValue;
		}
	}

	public static String exceptionMessage(Object... strings) {
		StringBuilder msg = new StringBuilder();
		msg.append(strings[0]).append(System.lineSeparator());
		msg.append("Additional exception info:").append(System.lineSeparator());
		for (int i = 1; i < strings.length; i++) {
			msg.append("\t").append(strings[i]).append(System.lineSeparator());
		}
		msg.append("Stacktrace:");
		return msg.toString();
	}

	public static String clampString(String string, int limit) {
		return string.substring(0, string.length() > limit ? limit : string.length());
	}

	public static List<int[]> splitArray(int[] array, int limit) {
		List<int[]> list = new ArrayList<>();
		if (array.length <= limit) {
			list.add(array);
			return list;
		}
		int count = getSplitCount(array.length, limit);
		int copied = 0;
		for (int i = 0; i < count; i++) {
			list.add(Arrays.copyOfRange(array, copied, Math.min(array.length, copied + limit)));
			copied += limit;
		}
		return list;
	}

	public static int getSplitCount(int length, int maxlength) {
		int count = length / maxlength;
		if ((length % maxlength) != 0) {
			count++;
		}
		return count;
	}

	public static <T> T getJavaPropertyValue(String property, T defaultValue, Converter<String, T> converter) {
		try {
			String value = System.getProperty(property);
			if (value != null) {
				return converter.convert(value);
			}
		} catch (Throwable t) {
		}
		return defaultValue;
	}

	public static interface Converter<T, R> {
		public static final Converter<String, Integer> STRING_TO_INT = new Converter<String, Integer>() {
			@Override
			public Integer convert(String t) {
				return Integer.parseInt(t);
			}
		};
		public static final Converter<String, Long> STRING_TO_LONG = new Converter<String, Long>() {
			@Override
			public Long convert(String t){
				return Long.parseLong(t);
			}
		};
		public static final Converter<String, Boolean> STRING_TO_BOOLEAN = new Converter<String, Boolean>() {
			@Override
			public Boolean convert(String t) {
				return Boolean.parseBoolean(t);
			}
		};
		public static final Converter<String, String> NONE = new Converter<String, String>() {
			@Override
			public String convert(String t) {
				return t;
			}
		};
		public R convert(T t);
	}

	public static boolean isTrue(Boolean b) {
		return (b != null) && b;
	}

	public static MinecraftServer getServer() {
		return ((CraftServer) Bukkit.getServer()).getServer();
	}

}
