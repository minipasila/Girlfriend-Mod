/*
 * External method calls:
 *   Lnet/minecraft/util/LenientJsonParser;parse(Ljava/lang/String;)Lcom/google/gson/JsonElement;
 *   Lnet/minecraft/client/realms/dto/Subscription$SubscriptionType;name()Ljava/lang/String;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/realms/dto/Subscription;typeFrom(Ljava/lang/String;)Lnet/minecraft/client/realms/dto/Subscription$SubscriptionType;
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
public class Subscription
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public long startDate;
    public int daysLeft;
    public SubscriptionType type = SubscriptionType.NORMAL;

    public static Subscription parse(String json) {
        Subscription lv = new Subscription();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(json).getAsJsonObject();
            lv.startDate = JsonUtils.getLongOr("startDate", jsonObject, 0L);
            lv.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
            lv.type = Subscription.typeFrom(JsonUtils.getNullableStringOr("subscriptionType", jsonObject, SubscriptionType.NORMAL.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse Subscription: {}", (Object)exception.getMessage());
        }
        return lv;
    }

    private static SubscriptionType typeFrom(String subscriptionType) {
        try {
            return SubscriptionType.valueOf(subscriptionType);
        } catch (Exception exception) {
            return SubscriptionType.NORMAL;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;

    }
}

