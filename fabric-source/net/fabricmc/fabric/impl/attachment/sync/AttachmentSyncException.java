package net.fabricmc.fabric.impl.attachment.sync;

import net.minecraft.text.Text;

public class AttachmentSyncException extends Exception {
	private final Text text;

	public AttachmentSyncException(Text text) {
		super(text.getString());
		this.text = text;
	}

	public Text getText() {
		return text;
	}
}
