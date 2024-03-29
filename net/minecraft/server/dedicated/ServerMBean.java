package net.minecraft.server.dedicated;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic management object for a Minecraft Server.
 * 
 * <p>It exposes the average tick time and the historical tick times of the
 * Minecraft Server.
 * 
 * @see javax.management.DynamicMBean
 */
public final class ServerMBean implements DynamicMBean {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer server;
   private final MBeanInfo mBeanInfo;
   private final Map<String, ServerMBean.Entry> entries;

   private ServerMBean(MinecraftServer server) {
      this.entries = (Map)Stream.of(new ServerMBean.Entry("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class), new ServerMBean.Entry("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)).collect(Collectors.toMap((entry) -> {
         return entry.name;
      }, Function.identity()));
      this.server = server;
      MBeanAttributeInfo[] mBeanAttributeInfos = (MBeanAttributeInfo[])this.entries.values().stream().map(ServerMBean.Entry::createInfo).toArray((i) -> {
         return new MBeanAttributeInfo[i];
      });
      this.mBeanInfo = new MBeanInfo(ServerMBean.class.getSimpleName(), "metrics for dedicated server", mBeanAttributeInfos, (MBeanConstructorInfo[])null, (MBeanOperationInfo[])null, new MBeanNotificationInfo[0]);
   }

   /**
    * Registers a dynamic MBean for a Minecraft Server.
    * 
    * @param server the server to have the MBean
    */
   public static void register(MinecraftServer server) {
      try {
         ManagementFactory.getPlatformMBeanServer().registerMBean(new ServerMBean(server), new ObjectName("net.minecraft.server:type=Server"));
      } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException var2) {
         LOGGER.warn((String)"Failed to initialise server as JMX bean", (Throwable)var2);
      }

   }

   /**
    * Returns the server's current average tick time, in milliseconds.
    */
   private float getAverageTickTime() {
      return this.server.getTickTime();
   }

   /**
    * Returns the server's historical tick times, in milliseconds.
    */
   private long[] getTickTimes() {
      return this.server.lastTickLengths;
   }

   @Nullable
   public Object getAttribute(String attribute) {
      ServerMBean.Entry entry = (ServerMBean.Entry)this.entries.get(attribute);
      return entry == null ? null : entry.getter.get();
   }

   public void setAttribute(Attribute attribute) {
   }

   public AttributeList getAttributes(String[] attributes) {
      Stream var10000 = Arrays.stream(attributes);
      Map var10001 = this.entries;
      Objects.requireNonNull(var10001);
      List<Attribute> list = (List)var10000.map(var10001::get).filter(Objects::nonNull).map((entry) -> {
         return new Attribute(entry.name, entry.getter.get());
      }).collect(Collectors.toList());
      return new AttributeList(list);
   }

   public AttributeList setAttributes(AttributeList attributes) {
      return new AttributeList();
   }

   @Nullable
   public Object invoke(String actionName, Object[] params, String[] signature) {
      return null;
   }

   public MBeanInfo getMBeanInfo() {
      return this.mBeanInfo;
   }

   /**
    * Represents a read-only attribute of the server MBean.
    */
   private static final class Entry {
      final String name;
      final Supplier<Object> getter;
      private final String description;
      private final Class<?> type;

      Entry(String name, Supplier<Object> getter, String description, Class<?> type) {
         this.name = name;
         this.getter = getter;
         this.description = description;
         this.type = type;
      }

      private MBeanAttributeInfo createInfo() {
         return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
      }
   }
}
