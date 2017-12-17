package shadows.placebo;

import net.minecraft.block.Block;

public class Proxy {

	@SuppressWarnings("deprecation")
	public String translate(String lang, Object... args) {
		return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(lang, args);
	}

	public void useRenamedMapper(Block b, String path) {

	}

	public void useRenamedMapper(Block b, String path, String append) {

	}

	public void useRenamedMapper(Block b, String path, String append, String variant) {

	}

}
