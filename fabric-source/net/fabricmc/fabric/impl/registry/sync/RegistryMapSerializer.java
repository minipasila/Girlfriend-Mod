package net.fabricmc.fabric.impl.registry.sync;

import java.util.LinkedHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class RegistryMapSerializer {
	public static final int VERSION = 1;

	public static Map<Identifier, Object2IntMap<Identifier>> fromNbt(NbtCompound nbt) {
		NbtCompound mainNbt = nbt.getCompound("registries").orElseThrow();
		Map<Identifier, Object2IntMap<Identifier>> map = new LinkedHashMap<>();

		for (String registryId : mainNbt.getKeys()) {
			Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
			NbtCompound idNbt = mainNbt.getCompound(registryId).orElseThrow();

			for (String id : idNbt.getKeys()) {
				idMap.put(Identifier.of(id), idNbt.getInt(id, 0));
			}

			map.put(Identifier.of(registryId), idMap);
		}

		return map;
	}

	public static NbtCompound toNbt(Map<Identifier, Object2IntMap<Identifier>> map) {
		NbtCompound mainNbt = new NbtCompound();

		map.forEach((registryId, idMap) -> {
			NbtCompound registryNbt = new NbtCompound();

			for (Object2IntMap.Entry<Identifier> idPair : idMap.object2IntEntrySet()) {
				registryNbt.putInt(idPair.getKey().toString(), idPair.getIntValue());
			}

			mainNbt.put(registryId.toString(), registryNbt);
		});

		NbtCompound nbt = new NbtCompound();
		nbt.putInt("version", VERSION);
		nbt.put("registries", mainNbt);
		return nbt;
	}
}
