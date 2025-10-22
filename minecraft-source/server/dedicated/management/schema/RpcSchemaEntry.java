/*
 * External method calls:
 *   Lnet/minecraft/server/dedicated/management/schema/RpcSchema;ofReference(Ljava/net/URI;)Lnet/minecraft/server/dedicated/management/schema/RpcSchema;
 *   Lnet/minecraft/server/dedicated/management/schema/RpcSchema;ofArray(Lnet/minecraft/server/dedicated/management/schema/RpcSchema;)Lnet/minecraft/server/dedicated/management/schema/RpcSchema;
 */
package net.minecraft.server.dedicated.management.schema;

import java.net.URI;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public record RpcSchemaEntry(String name, URI reference, RpcSchema schema) {
    public RpcSchema ref() {
        return RpcSchema.ofReference(this.reference);
    }

    public RpcSchema array() {
        return RpcSchema.ofArray(RpcSchema.ofReference(this.reference));
    }
}

