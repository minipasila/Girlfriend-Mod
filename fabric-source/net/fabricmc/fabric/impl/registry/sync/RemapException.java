package net.fabricmc.fabric.impl.registry.sync;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;

public class RemapException extends Exception {
	@Nullable
	private final Text text;

	public RemapException(String message) {
		super(message);
		this.text = null;
	}

	public RemapException(Text text) {
		super(text.getString());
		this.text = text;
	}

	@Nullable
	public Text getText() {
		return text;
	}
}
