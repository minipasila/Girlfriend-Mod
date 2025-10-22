package net.minecraft.client.realms.dto;

import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsSerializable;
import net.minecraft.client.realms.dto.ValueObject;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsDescriptionDto
extends ValueObject
implements RealmsSerializable {
    @SerializedName(value="name")
    @Nullable
    public String name;
    @SerializedName(value="description")
    public String description;

    public RealmsDescriptionDto(@Nullable String name, String description) {
        this.name = name;
        this.description = description;
    }
}

