package shadows.placebo;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.model.ModelLoader;
import shadows.placebo.client.RenamedStateMapper;

public class ClientProxy extends Proxy {

	@Override
	public String translate(String lang, Object... args) {
		return I18n.format(lang, args);
	}

	@Override
	public void useRenamedMapper(Block b, String path) {
		ModelLoader.setCustomStateMapper(b, new RenamedStateMapper(b.getRegistryName().getResourceDomain(), path));
	}

	@Override
	public void useRenamedMapper(Block b, String path, String append) {
		ModelLoader.setCustomStateMapper(b, new RenamedStateMapper(b.getRegistryName().getResourceDomain(), path, append));
	}

	@Override
	public void useRenamedMapper(Block b, String path, String append, String variant) {
		ModelLoader.setCustomStateMapper(b, new RenamedStateMapper(b.getRegistryName().getResourceDomain(), path, append, variant));
	}

}
