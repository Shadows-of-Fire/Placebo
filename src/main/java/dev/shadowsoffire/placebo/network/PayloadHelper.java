package dev.shadowsoffire.placebo.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public class PayloadHelper {

    private static final Map<CustomPacketPayload.Type<?>, PayloadProvider<?>> ALL_PROVIDERS = new HashMap<>();
    private static boolean locked = false;

    /**
     * Registers a payload using {@link PayloadProvider}.
     *
     * @param channel Channel to register for.
     * @param id      The ID of the payload being registered.
     * @param prov    An instance of the payload provider.
     */
    public static <T extends CustomPacketPayload> void registerPayload(PayloadProvider<T> prov) {
        Preconditions.checkNotNull(prov);
        synchronized (ALL_PROVIDERS) {
            if (locked) {
                throw new UnsupportedOperationException("Attempted to register a payload provider after registration has finished.");
            }
            if (ALL_PROVIDERS.containsKey(prov.getType())) {
                throw new UnsupportedOperationException("Attempted to register payload provider with duplicate ID: " + prov.getType().id());
            }
            ALL_PROVIDERS.put(prov.getType(), prov);
        }
    }

    @SubscribeEvent
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerProviders(RegisterPayloadHandlersEvent event) {
        synchronized (ALL_PROVIDERS) {
            for (PayloadProvider prov : ALL_PROVIDERS.values()) {
                NetworkRegistry.register(prov.getType(), prov.getCodec(), new PayloadHandler(prov), new PayloadHandler(prov), prov.getSupportedProtocols(), prov.getFlow(), prov.getVersion(), prov.isOptional());
            }
            locked = true;
        }
    }

    private static class PayloadHandler<T extends CustomPacketPayload> implements IPayloadHandler<T> {

        private PayloadProvider<T> provider;
        private Optional<PacketFlow> flow;
        private List<ConnectionProtocol> protocols;

        private PayloadHandler(PayloadProvider<T> provider) {
            this.provider = provider;
            this.flow = provider.getFlow();
            this.protocols = provider.getSupportedProtocols();
            Preconditions.checkArgument(!this.protocols.isEmpty(), "The payload registration for " + provider.getType().id() + " must specify at least one valid protocol.");
        }

        @Override
        public void handle(T payload, IPayloadContext context) {
            if (this.flow.isPresent() && this.flow.get() != context.flow()) {
                Placebo.LOGGER.error("Received a payload {} on the incorrect side.", payload.type().id());
                return;
            }

            if (!this.protocols.contains(context.protocol())) {
                Placebo.LOGGER.error("Received a payload {} on the incorrect protocol.", payload.type().id());
                return;
            }

            if (context.flow() == PacketFlow.CLIENTBOUND) {
                switch (provider.getHandlerThread()) {
                    case MAIN -> context.enqueueWork(() -> this.provider.handleClient(payload, context));
                    case NETWORK -> this.provider.handleClient(payload, context);
                }
            }
            else {
                switch (provider.getHandlerThread()) {
                    case MAIN -> context.enqueueWork(() -> this.provider.handleServer(payload, context));
                    case NETWORK -> this.provider.handleServer(payload, context);
                }
            }
        }
    }

}
