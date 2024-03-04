package dev.shadowsoffire.placebo.network;

import java.util.ArrayList;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class MessageHelper {

    private static final Map<String, List<PayloadProvider<?, ?>>> ALL_PROVIDERS = new HashMap<>();
    private static boolean locked = false;

    /**
     * Registers a message using {@link PayloadProvider}.
     *
     * @param channel Channel to register for.
     * @param id      Message id.
     * @param prov    An instance of the message provider. Note that this object will be kept around, so try to keep all fields uninitialized if possible.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload, C extends IPayloadContext> void registerMessage(PayloadProvider<T, C> prov) {
        synchronized (ALL_PROVIDERS) {
            if (locked) throw new UnsupportedOperationException("Attempted to register a payload provider after registration has finished.");
            ALL_PROVIDERS.computeIfAbsent(prov.getMsgId().getNamespace(), k -> new ArrayList<>()).add(prov);
        }
    }

    /**
     * Handles a payload. Enqueues the runnable to run on the main thread.
     *
     * @param r   Code to run to handle the payload. Will be executed on the main thread.
     * @param ctx Context object. Available from {@link PayloadProvider#handle(CustomPacketPayload, IPayloadContext)}
     */
    public static void handle(Runnable r, IPayloadContext ctx) {
        ctx.workHandler().execute(r);
    }

    @SubscribeEvent
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void registerProviders(RegisterPayloadHandlerEvent event) {
        synchronized (ALL_PROVIDERS) {
            for (Map.Entry<String, List<PayloadProvider<?, ?>>> entry : ALL_PROVIDERS.entrySet()) {
                for (PayloadProvider prov : entry.getValue()) {
                    IPayloadRegistrar reg = event.registrar(entry.getKey());

                    if (prov.isOptional()) {
                        reg = reg.optional();
                    }

                    if (prov.getVersion().isPresent()) {
                        reg = reg.versioned(prov.getVersion().get().toString()); // Using a rawtype also rawtypes the Optional
                    }
                    reg.common(prov.getMsgId(), prov::read, new PayloadHandler(prov));
                }
            }
            locked = true;
        }
    }

    private static class PayloadHandler<T extends CustomPacketPayload, C extends IPayloadContext> implements IPayloadHandler<T> {

        private PayloadProvider<T, C> provider;
        private Optional<PacketFlow> flow;
        private List<ConnectionProtocol> protocols;

        private PayloadHandler(PayloadProvider<T, C> provider) {
            this.provider = provider;
            this.flow = provider.getFlow();
            this.protocols = provider.getSupportedProtocols();
            Preconditions.checkArgument(!this.protocols.isEmpty(), "The payload registration for " + provider.getMsgId() + " must specify at least one valid protocol.");
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handle(T payload, IPayloadContext context) {
            if (flow.isPresent() && flow.get() != context.flow()) {
                Placebo.LOGGER.error("Received a payload {} on the incorrect side.", payload.id());
                return;
            }

            if (!protocols.contains(context.protocol())) {
                Placebo.LOGGER.error("Received a payload {} on the incorrect protocol.", payload.id());
                return;
            }

            provider.handle(payload, (C) context);
        }
    }

}
