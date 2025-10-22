/*
 * External method calls:
 *   Lnet/minecraft/util/LenientJsonParser;parse(Ljava/lang/String;)Lcom/google/gson/JsonElement;
 */
package net.minecraft.client.realms.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.util.LenientJsonParser;

@Environment(value=EnvType.CLIENT)
public class Ops
extends ValueObject {
    public Set<String> ops = Sets.newHashSet();

    public static Ops parse(String json) {
        Ops lv = new Ops();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(json).getAsJsonObject();
            JsonElement jsonElement = jsonObject.get("ops");
            if (jsonElement.isJsonArray()) {
                for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                    lv.ops.add(jsonElement2.getAsString());
                }
            }
        } catch (Exception exception) {
            // empty catch block
        }
        return lv;
    }
}

