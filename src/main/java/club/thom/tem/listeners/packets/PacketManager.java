package club.thom.tem.listeners.packets;

import club.thom.tem.listeners.packets.events.PacketEvent;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketManager {
    private static final Logger logger = LogManager.getLogger(PacketManager.class);
    private final ClientPacketListener clientListener = new ClientPacketListener(this);
    private final ServerPacketListener serverListener = new ServerPacketListener(this);
    private final List<IPacketEventListener> listeners = new ArrayList<>();
    private final Map<Class<? extends PacketEvent>, MethodHandle> packetEventMethodHandles = new HashMap<>();
    private final Map<Class<? extends Packet<?>>, Class<? extends PacketEvent>> packetToEvent = new HashMap<>();

    private static final ExecutorService listenerRunner = Executors.newCachedThreadPool();

    public PacketManager() {
        buildHandles();
    }

    private void buildHandles() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Class<IPacketEventListener> clazz = IPacketEventListener.class;
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameter = method.getParameterTypes()[0];
            if (!PacketEvent.class.isAssignableFrom(parameter)) {
                continue;
            }

            //noinspection unchecked
            Class<? extends PacketEvent> eventClass = (Class<? extends PacketEvent>) parameter;

            for (Constructor<?> eventConstructor : eventClass.getConstructors()) {
                // Find all packet constructors for this PacketEvent
                if (eventConstructor.getParameterCount() != 1 || !Packet.class.isAssignableFrom(eventConstructor.getParameterTypes()[0])) {
                    // Constructor either takes >1 parameter or the parameter is not a Packet
                    continue;
                }
                //noinspection unchecked
                packetToEvent.put((Class<? extends Packet<?>>) eventConstructor.getParameterTypes()[0], eventClass);
            }

            try {
                MethodHandle handle = lookup.unreflect(method);
                //noinspection unchecked
                packetEventMethodHandles.put((Class<? extends PacketEvent>) parameter, handle);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", clientListener.getClass().getName(), clientListener);
        pipeline.addBefore("packet_handler", serverListener.getClass().getName(), serverListener);
    }

    public void registerListener(IPacketEventListener listener) {
        listeners.add(listener);
    }

    /**
     * @param packet The packet to process.
     * @return Whether the packet should continue processing.
     */
    public boolean processPacket(Packet<?> packet) {
        Class<?> clazz = packet.getClass();
        Class<? extends PacketEvent> eventClass = packetToEvent.get(clazz);
        if (eventClass == null) {
            return true;
        }
        PacketEvent event;
        try {
            event = eventClass.getConstructor(clazz).newInstance(packet);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            logger.error("Error creating packet event", e);
            return true;
        }
        for (IPacketEventListener listener : listeners) {
            MethodHandle handle = packetEventMethodHandles.get(eventClass);
            if (handle == null) {
                continue;
            }
            if (listener instanceof PacketEventListener) {
                // Run the event in a separate thread.
                listenerRunner.execute(() -> invokeHandle(handle, listener, event));
                continue;
            } else if (!(listener instanceof CancellablePacketEventListener)) {
                throw new IllegalStateException("Unsupported listener type: " + listener.getClass().getName());
            }

            invokeHandle(handle, listener, event);
            if (event.isCancelled()) {
                // Event's been cancelled, let's not forward the packet to the next listener.
                return false;
            }
        }
        return true;
    }

    private void invokeHandle(MethodHandle handle, IPacketEventListener listener, PacketEvent event) {
        try {
            handle.invoke(listener, event);
        } catch (Throwable throwable) {
            logger.error("Error invoking packet event listener", throwable);
        }
    }



}
