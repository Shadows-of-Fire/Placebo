package shadows.placebo.codec;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.network.FriendlyByteBuf;

public class PacketOps implements DynamicOps<FriendlyByteBuf> {

	protected final FriendlyByteBuf buf;

	public PacketOps(FriendlyByteBuf buf) {
		this.buf = buf;
	}

	@Override
	public FriendlyByteBuf empty() {
		return buf;
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, FriendlyByteBuf input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataResult<Number> getNumberValue(FriendlyByteBuf input) {
		return null;
	}

	@Override
	public FriendlyByteBuf createNumeric(Number i) {
		return null;
	}

	@Override
	public DataResult<String> getStringValue(FriendlyByteBuf input) {
		return DataResult.success(input.readUtf());
	}

	@Override
	public FriendlyByteBuf createString(String value) {
		return null;
	}

	@Override
	public DataResult<FriendlyByteBuf> mergeToList(FriendlyByteBuf list, FriendlyByteBuf value) {
		return null;
	}

	@Override
	public DataResult<FriendlyByteBuf> mergeToMap(FriendlyByteBuf map, FriendlyByteBuf key, FriendlyByteBuf value) {
		return null;
	}

	@Override
	public DataResult<Stream<Pair<FriendlyByteBuf, FriendlyByteBuf>>> getMapValues(FriendlyByteBuf input) {
		return null;
	}

	@Override
	public FriendlyByteBuf createMap(Stream<Pair<FriendlyByteBuf, FriendlyByteBuf>> map) {
		return null;
	}

	@Override
	public DataResult<Stream<FriendlyByteBuf>> getStream(FriendlyByteBuf input) {
		return null;
	}

	@Override
	public FriendlyByteBuf createList(Stream<FriendlyByteBuf> input) {
		return null;
	}

	@Override
	public FriendlyByteBuf remove(FriendlyByteBuf input, String key) {
		return null;
	}

}
