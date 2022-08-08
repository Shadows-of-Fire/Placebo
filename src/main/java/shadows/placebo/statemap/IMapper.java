package shadows.placebo.statemap;

import net.minecraft.client.resources.model.ModelResourceLocation;

@Deprecated
public interface IMapper<T> {

	ModelResourceLocation map(T t);

}
