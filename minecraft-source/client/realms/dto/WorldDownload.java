/*
 * External method calls:
 *   Lnet/minecraft/util/LenientJsonParser;parse(Ljava/lang/String;)Lcom/google/gson/JsonElement;
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldDownload
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String downloadLink;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static WorldDownload parse(String json) {
        JsonObject jsonObject = LenientJsonParser.parse(json).getAsJsonObject();
        WorldDownload lv = new WorldDownload();
        try {
            lv.downloadLink = JsonUtils.getNullableStringOr("downloadLink", jsonObject, "");
            lv.resourcePackUrl = JsonUtils.getNullableStringOr("resourcePackUrl", jsonObject, "");
            lv.resourcePackHash = JsonUtils.getNullableStringOr("resourcePackHash", jsonObject, "");
        } catch (Exception exception) {
            LOGGER.error("Could not parse WorldDownload: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

