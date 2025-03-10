/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2019 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.toobeetooteebot.util.handler;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.daporkchop.lib.logging.Logger;
import net.daporkchop.lib.primitive.function.bifunction.ObjectObjectBooleanBiFunction;
import net.daporkchop.toobeetooteebot.util.Constants;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HandlerRegistry<S extends Session> implements Constants {
    protected static final boolean DEBUG_INBOUND_PACKETS = CONFIG.getBoolean("debug.packet.received");
    protected static final boolean DEBUG_OUTBOUND_PACKETS = CONFIG.getBoolean("debug.packet.preSent");
    protected static final boolean DEBUG_POSTOUTBOUND_PACKETS = CONFIG.getBoolean("debug.packet.postSent");

    @NonNull
    protected final Map<Class<? extends Packet>, ObjectObjectBooleanBiFunction<? extends Packet, S>> inboundHandlers;

    @NonNull
    protected final Map<Class<? extends Packet>, BiFunction<? extends Packet, S, ? extends Packet>> outboundHandlers;

    @NonNull
    protected final Map<Class<? extends Packet>, BiConsumer<? extends Packet, S>> postOutboundHandlers;

    @NonNull
    protected final Logger logger;

    @SuppressWarnings("unchecked")
    public <P extends Packet> boolean handleInbound(@NonNull P packet, @NonNull S session) {
        if (DEBUG_INBOUND_PACKETS)  {
            this.logger.debug("Received packet: %s", packet.getClass());
        }
        ObjectObjectBooleanBiFunction<P, S> handler = (ObjectObjectBooleanBiFunction<P, S>) this.inboundHandlers.get(packet.getClass());
        return handler == null || handler.apply(packet, session);
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> P handleOutgoing(@NonNull P packet, @NonNull S session) {
        if (DEBUG_OUTBOUND_PACKETS)  {
            this.logger.debug("About to send packet: %s", packet.getClass());
        }
        BiFunction<P, S, P> handler = (BiFunction<P, S, P>) this.outboundHandlers.get(packet.getClass());
        return handler == null ? packet : handler.apply(packet, session);
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> void handlePostOutgoing(@NonNull P packet, @NonNull S session) {
        if (DEBUG_POSTOUTBOUND_PACKETS)  {
            this.logger.debug("Sent packet: %s", packet.getClass());
        }
        PostOutgoingHandler<P, S> handler = (PostOutgoingHandler<P, S>) this.postOutboundHandlers.get(packet.getClass());
        if (handler != null) {
            handler.accept(packet, session);
        }
    }

    public interface IncomingHandler<P extends Packet, S extends Session> extends ObjectObjectBooleanBiFunction<P, S>, Constants {
        /**
         * Handle a packet
         *
         * @param packet  the packet to handle
         * @param session the session the packet was received on
         * @return whether or not the packet should be forwarded
         */
        @Override
        boolean apply(P packet, S session);

        Class<P> getPacketClass();
    }

    public interface OutgoingHandler<P extends Packet, S extends Session> extends BiFunction<P, S, P>, Constants {
        @Override
        P apply(P packet, S session);

        Class<P> getPacketClass();
    }

    public interface PostOutgoingHandler<P extends Packet, S extends Session> extends BiConsumer<P, S>, Constants {
        @Override
        void accept(P packet, S session);

        Class<P> getPacketClass();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Builder<S extends Session> {
        protected final Map<Class<? extends Packet>, ObjectObjectBooleanBiFunction<? extends Packet, S>> inboundHandlers = new IdentityHashMap<>();

        protected final Map<Class<? extends Packet>, BiFunction<? extends Packet, S, ? extends Packet>> outboundHandlers = new IdentityHashMap<>();

        protected final Map<Class<? extends Packet>, BiConsumer<? extends Packet, S>> postOutboundHandlers = new IdentityHashMap<>();

        @NonNull
        protected Logger logger;

        public <P extends Packet> Builder<S> registerInbound(@NonNull Class<P> clazz, @NonNull BiConsumer<P, S> handler) {
            return this.registerInbound(clazz, (packet, session) -> {
                handler.accept(packet, session);
                return true;
            });
        }

        public <P extends Packet> Builder<S> registerInbound(@NonNull Class<P> clazz, @NonNull ObjectObjectBooleanBiFunction<P, S> handler) {
            this.inboundHandlers.put(clazz, handler);
            return this;
        }

        public Builder<S> registerInbound(@NonNull IncomingHandler<? extends Packet, S> handler) {
            this.inboundHandlers.put(handler.getPacketClass(), handler);
            return this;
        }

        public <P extends Packet> Builder<S> registerOutbound(@NonNull Class<P> clazz, @NonNull BiFunction<P, S, P> handler) {
            this.outboundHandlers.put(clazz, handler);
            return this;
        }

        public Builder<S> registerOutbound(@NonNull OutgoingHandler<? extends Packet, S> handler) {
            this.outboundHandlers.put(handler.getPacketClass(), handler);
            return this;
        }

        public <P extends Packet> Builder<S> registerPostOutbound(@NonNull Class<P> clazz, @NonNull BiConsumer<P, S> handler) {
            this.postOutboundHandlers.put(clazz, handler);
            return this;
        }

        public Builder<S> registerPostOutbound(@NonNull PostOutgoingHandler<? extends Packet, S> handler) {
            this.postOutboundHandlers.put(handler.getPacketClass(), handler);
            return this;
        }

        public HandlerRegistry<S> build() {
            return new HandlerRegistry<>(this.inboundHandlers, this.outboundHandlers, this.postOutboundHandlers, this.logger);
        }
    }
}
