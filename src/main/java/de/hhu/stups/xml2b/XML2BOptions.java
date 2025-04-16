package de.hhu.stups.xml2b;

import java.io.File;
import java.nio.file.Path;

public class XML2BOptions {
	public enum XML2BOption {
		ABSTRACT_CONSTANTS("ac","include useful abstract constants in generated machine",false),
		FAST_RW("frw","specify Prolog system for fast read-write: SICSTUS, SWI; NONE for standard output",true),
		OUTPUT("o", "output path for generated machine", true),
		VERSION("version", "prints the current version of XML2B", false),
		XSD("xsd", "use XSD file for schema validation and type extraction", true);

		private final String arg, desc;
		private final boolean hasArg;

		XML2BOption(String arg, String desc, boolean hasArg) {
			this.arg = arg;
			this.desc = desc;
			this.hasArg = hasArg;
		}

		public String arg() {
			return arg;
		}

		public String desc() {
			return desc;
		}

		public boolean hasArg() {
			return hasArg;
		}
	}

	public static final String SICSTUS_NAME = "sicstus", SWI_NAME = "swi", NONE_NAME = "none";

	private final String machineName;
	private final Path directory;
	private final String frwPrologSystem;
	private final boolean generateAbstractConstants;

	private XML2BOptions(String machineName, Path directory, String frwPrologSystem,
	                    boolean generateAbstractConstants) {
		this.machineName = machineName;
		this.directory = directory;
		this.frwPrologSystem = frwPrologSystem;
		this.generateAbstractConstants = generateAbstractConstants;
	}

	public static XML2BOptions defaultOptions(File xmlFile) {
		String[] splitName = xmlFile.getName().split("\\.");
		return new XML2BOptions(splitName[splitName.length > 1 ? splitName.length-2 : 0], xmlFile.getAbsoluteFile().getParentFile().toPath(),
				SICSTUS_NAME, false);
	}

	public XML2BOptions withMachineName(String machineName) {
		return new XML2BOptions(machineName, this.directory, this.frwPrologSystem, this.generateAbstractConstants);
	}

	public XML2BOptions withDirectory(Path directory) {
		return new XML2BOptions(this.machineName, directory, this.frwPrologSystem, this.generateAbstractConstants);
	}

	public XML2BOptions withPrologSystem(String prologSystem) {
		if (prologSystem.equalsIgnoreCase(NONE_NAME)) {
			return new XML2BOptions(this.machineName, this.directory, null, this.generateAbstractConstants);
		}
		if (!prologSystem.equalsIgnoreCase(SICSTUS_NAME) && !prologSystem.equalsIgnoreCase(SWI_NAME)) {
			throw new IllegalArgumentException("Unsupported Prolog system: " + prologSystem);
		}
		return new XML2BOptions(this.machineName, this.directory, prologSystem, this.generateAbstractConstants);
	}

	public XML2BOptions withGenerateAbstractConstants(boolean generateAbstractConstants) {
		return new XML2BOptions(this.machineName, this.directory, this.frwPrologSystem, generateAbstractConstants);
	}

	public String machineName() {
		return machineName;
	}

	public Path directory() {
		return directory;
	}

	public String frwPrologSystem() {
		return frwPrologSystem;
	}

	public boolean generateAbstractConstants() {
		return generateAbstractConstants;
	}
}
