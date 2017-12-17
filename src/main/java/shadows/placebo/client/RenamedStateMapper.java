package shadows.placebo.client;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

public class RenamedStateMapper implements IStateMapper {

	final String modid;
	final String path;
	String append = "";
	String variant = "";

	public RenamedStateMapper(String modid, String path) {
		this.modid = modid;
		this.path = path;
	}

	public RenamedStateMapper(String modid, String path, String append) {
		this(modid, path);
		this.append = append;
	}

	public RenamedStateMapper(String modid, String path, String append, String variant) {
		this(modid, path, append);
		this.variant = variant;
	}

	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
		Map<IBlockState, ModelResourceLocation> map = new DefaultStateMapper().putStateModelLocations(block);
		for (IBlockState state : map.keySet()) {
			ModelResourceLocation loc = map.get(state);
			String var = variant.length() == 0 ? loc.getVariant() : variant;

			map.put(state, new ModelResourceLocation(modid + ":" + path, var + append));
		}
		return map;
	}

}
