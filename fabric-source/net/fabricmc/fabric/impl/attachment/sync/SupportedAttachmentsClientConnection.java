package net.fabricmc.fabric.impl.attachment.sync;

import java.util.Set;

import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

/**
 * Implemented on {@link ClientConnection} to store which attachments the client supports.
 */
public interface SupportedAttachmentsClientConnection {
	void fabric_setSupportedAttachments(Set<Identifier> supportedAttachments);

	Set<Identifier> fabric_getSupportedAttachments();
}
