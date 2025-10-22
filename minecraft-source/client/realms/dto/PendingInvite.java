package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Date;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Util;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PendingInvite
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String invitationId;
    public String worldName;
    public String worldOwnerName;
    public UUID worldOwnerUuid;
    public Date date;

    public static PendingInvite parse(JsonObject json) {
        PendingInvite lv = new PendingInvite();
        try {
            lv.invitationId = JsonUtils.getNullableStringOr("invitationId", json, "");
            lv.worldName = JsonUtils.getNullableStringOr("worldName", json, "");
            lv.worldOwnerName = JsonUtils.getNullableStringOr("worldOwnerName", json, "");
            lv.worldOwnerUuid = JsonUtils.getUuidOr("worldOwnerUuid", json, Util.NIL_UUID);
            lv.date = JsonUtils.getDateOr("date", json);
        } catch (Exception exception) {
            LOGGER.error("Could not parse PendingInvite: {}", (Object)exception.getMessage());
        }
        return lv;
    }
}

