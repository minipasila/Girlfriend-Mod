/*
 * External method calls:
 *   Lnet/minecraft/client/realms/CheckedGson;fromJson(Ljava/lang/String;Ljava/lang/Class;)Lnet/minecraft/client/realms/RealmsSerializable;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/realms/dto/RealmsRegionSelectionPreference;clone()Lnet/minecraft/client/realms/dto/RealmsRegionSelectionPreference;
 */
package net.minecraft.client.realms.dto;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.dto.RealmsRegion;
import net.minecraft.client.realms.dto.RegionSelectionMethod;
import net.minecraft.client.realms.dto.ValueObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsRegionSelectionPreference
extends ValueObject
implements RealmsSerializable {
    public static final RealmsRegionSelectionPreference DEFAULT = new RealmsRegionSelectionPreference(RegionSelectionMethod.AUTOMATIC_OWNER, null);
    private static final Logger LOGGER = LogUtils.getLogger();
    @SerializedName(value="regionSelectionPreference")
    @JsonAdapter(value=RegionSelectionMethod.SelectionMethodTypeAdapter.class)
    public RegionSelectionMethod selectionMethod;
    @SerializedName(value="preferredRegion")
    @JsonAdapter(value=RealmsRegion.RegionTypeAdapter.class)
    @Nullable
    public RealmsRegion preferredRegion;

    public RealmsRegionSelectionPreference(RegionSelectionMethod selectionMethod, @Nullable RealmsRegion preferredRegion) {
        this.selectionMethod = selectionMethod;
        this.preferredRegion = preferredRegion;
    }

    private RealmsRegionSelectionPreference() {
    }

    public static RealmsRegionSelectionPreference parse(CheckedGson gson, String json) {
        try {
            RealmsRegionSelectionPreference lv = gson.fromJson(json, RealmsRegionSelectionPreference.class);
            if (lv == null) {
                LOGGER.error("Could not parse RegionSelectionPreference: {}", (Object)json);
                return new RealmsRegionSelectionPreference();
            }
            return lv;
        } catch (Exception exception) {
            LOGGER.error("Could not parse RegionSelectionPreference: {}", (Object)exception.getMessage());
            return new RealmsRegionSelectionPreference();
        }
    }

    public RealmsRegionSelectionPreference clone() {
        return new RealmsRegionSelectionPreference(this.selectionMethod, this.preferredRegion);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }
}

