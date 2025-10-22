package net.fabricmc.fabric.impl.gametest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.minecraft.test.XmlReportingTestCompletionListener;

/**
 * An extension of {@link XmlReportingTestCompletionListener} which creates the destination directory before saving
 * the report.
 */
final class SavingXmlReportingTestCompletionListener extends XmlReportingTestCompletionListener {
	SavingXmlReportingTestCompletionListener(File file) throws ParserConfigurationException {
		super(file);
	}

	@Override
	public void saveReport(File file) throws TransformerException {
		try {
			Files.createDirectories(file.toPath().getParent());
		} catch (IOException e) {
			throw new TransformerException("Failed to create parent directory", e);
		}

		super.saveReport(file);
	}
}
