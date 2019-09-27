package shadows.placebo.statemap;

import net.minecraft.client.renderer.model.ModelResourceLocation;

public interface IMapper<T> {

	ModelResourceLocation map(T t);

}
