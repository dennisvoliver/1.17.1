package net.minecraft.server.dedicated;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.gui.DedicatedServerGui;
import net.minecraft.server.filter.TextFilterer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.rcon.QueryResponseHandler;
import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.server.rcon.RconListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.snooper.Snooper;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class MinecraftDedicatedServer extends MinecraftServer implements DedicatedServer {
   static final Logger LOGGER = LogManager.getLogger();
   private static final int field_29662 = 5000;
   private static final int field_29663 = 2;
   private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
   private final List<PendingServerCommand> commandQueue = Collections.synchronizedList(Lists.newArrayList());
   private QueryResponseHandler queryResponseHandler;
   private final RconCommandOutput rconCommandOutput;
   private RconListener rconServer;
   private final ServerPropertiesLoader propertiesLoader;
   @Nullable
   private DedicatedServerGui gui;
   @Nullable
   private final TextFilterer filterer;
   @Nullable
   private final Text resourcePackPrompt;

   public MinecraftDedicatedServer(Thread serverThread, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session, ResourcePackManager dataPackManager, ServerResourceManager serverResourceManager, SaveProperties saveProperties, ServerPropertiesLoader propertiesLoader, DataFixer dataFixer, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
      super(serverThread, registryManager, session, saveProperties, dataPackManager, Proxy.NO_PROXY, dataFixer, serverResourceManager, sessionService, gameProfileRepo, userCache, worldGenerationProgressListenerFactory);
      this.propertiesLoader = propertiesLoader;
      this.rconCommandOutput = new RconCommandOutput(this);
      this.filterer = TextFilterer.load(propertiesLoader.getPropertiesHandler().textFilteringConfig);
      this.resourcePackPrompt = parseResourcePackPrompt(propertiesLoader);
   }

   public boolean setupServer() throws IOException {
      Thread thread = new Thread("Server console handler") {
         public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            String string;
            try {
               while(!MinecraftDedicatedServer.this.isStopped() && MinecraftDedicatedServer.this.isRunning() && (string = bufferedReader.readLine()) != null) {
                  MinecraftDedicatedServer.this.enqueueCommand(string, MinecraftDedicatedServer.this.getCommandSource());
               }
            } catch (IOException var4) {
               MinecraftDedicatedServer.LOGGER.error((String)"Exception handling console input", (Throwable)var4);
            }

         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
      thread.start();
      LOGGER.info((String)"Starting minecraft server version {}", (Object)SharedConstants.getGameVersion().getName());
      if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
         LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
      }

      LOGGER.info("Loading properties");
      ServerPropertiesHandler serverPropertiesHandler = this.propertiesLoader.getPropertiesHandler();
      if (this.isSinglePlayer()) {
         this.setServerIp("127.0.0.1");
      } else {
         this.setOnlineMode(serverPropertiesHandler.onlineMode);
         this.setPreventProxyConnections(serverPropertiesHandler.preventProxyConnections);
         this.setServerIp(serverPropertiesHandler.serverIp);
      }

      this.setPvpEnabled(serverPropertiesHandler.pvp);
      this.setFlightEnabled(serverPropertiesHandler.allowFlight);
      this.setResourcePack(serverPropertiesHandler.resourcePack, this.createResourcePackHash());
      this.setMotd(serverPropertiesHandler.motd);
      super.setPlayerIdleTimeout((Integer)serverPropertiesHandler.playerIdleTimeout.get());
      this.setEnforceWhitelist(serverPropertiesHandler.enforceWhitelist);
      this.saveProperties.setGameMode(serverPropertiesHandler.gameMode);
      LOGGER.info((String)"Default game type: {}", (Object)serverPropertiesHandler.gameMode);
      InetAddress inetAddress = null;
      if (!this.getServerIp().isEmpty()) {
         inetAddress = InetAddress.getByName(this.getServerIp());
      }

      if (this.getServerPort() < 0) {
         this.setServerPort(serverPropertiesHandler.serverPort);
      }

      this.generateKeyPair();
      LOGGER.info((String)"Starting Minecraft server on {}:{}", (Object)(this.getServerIp().isEmpty() ? "*" : this.getServerIp()), (Object)this.getServerPort());

      try {
         this.getNetworkIo().bind(inetAddress, this.getServerPort());
      } catch (IOException var10) {
         LOGGER.warn("**** FAILED TO BIND TO PORT!");
         LOGGER.warn((String)"The exception was: {}", (Object)var10.toString());
         LOGGER.warn("Perhaps a server is already running on that port?");
         return false;
      }

      if (!this.isOnlineMode()) {
         LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
         LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
         LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
         LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
      }

      if (this.convertData()) {
         this.getUserCache().save();
      }

      if (!ServerConfigHandler.checkSuccess(this)) {
         return false;
      } else {
         this.setPlayerManager(new DedicatedPlayerManager(this, this.registryManager, this.saveHandler));
         long l = Util.getMeasuringTimeNano();
         SkullBlockEntity.setUserCache(this.getUserCache());
         SkullBlockEntity.setSessionService(this.getSessionService());
         SkullBlockEntity.setExecutor(this);
         UserCache.setUseRemote(this.isOnlineMode());
         LOGGER.info((String)"Preparing level \"{}\"", (Object)this.getLevelName());
         this.loadWorld();
         long m = Util.getMeasuringTimeNano() - l;
         String string = String.format(Locale.ROOT, "%.3fs", (double)m / 1.0E9D);
         LOGGER.info((String)"Done ({})! For help, type \"help\"", (Object)string);
         if (serverPropertiesHandler.announcePlayerAchievements != null) {
            ((GameRules.BooleanRule)this.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS)).set(serverPropertiesHandler.announcePlayerAchievements, this);
         }

         if (serverPropertiesHandler.enableQuery) {
            LOGGER.info("Starting GS4 status listener");
            this.queryResponseHandler = QueryResponseHandler.create(this);
         }

         if (serverPropertiesHandler.enableRcon) {
            LOGGER.info("Starting remote control listener");
            this.rconServer = RconListener.create(this);
         }

         if (this.getMaxTickTime() > 0L) {
            Thread thread2 = new Thread(new DedicatedServerWatchdog(this));
            thread2.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
            thread2.setName("Server Watchdog");
            thread2.setDaemon(true);
            thread2.start();
         }

         Items.AIR.appendStacks(ItemGroup.SEARCH, DefaultedList.of());
         if (serverPropertiesHandler.enableJmxMonitoring) {
            ServerMBean.register(this);
            LOGGER.info("JMX monitoring enabled");
         }

         return true;
      }
   }

   public boolean shouldSpawnAnimals() {
      return this.getProperties().spawnAnimals && super.shouldSpawnAnimals();
   }

   public boolean isMonsterSpawningEnabled() {
      return this.propertiesLoader.getPropertiesHandler().spawnMonsters && super.isMonsterSpawningEnabled();
   }

   public boolean shouldSpawnNpcs() {
      return this.propertiesLoader.getPropertiesHandler().spawnNpcs && super.shouldSpawnNpcs();
   }

   public String createResourcePackHash() {
      ServerPropertiesHandler serverPropertiesHandler = this.propertiesLoader.getPropertiesHandler();
      String string3;
      if (!serverPropertiesHandler.resourcePackSha1.isEmpty()) {
         string3 = serverPropertiesHandler.resourcePackSha1;
         if (!Strings.isNullOrEmpty(serverPropertiesHandler.resourcePackHash)) {
            LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
         }
      } else if (!Strings.isNullOrEmpty(serverPropertiesHandler.resourcePackHash)) {
         LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
         string3 = serverPropertiesHandler.resourcePackHash;
      } else {
         string3 = "";
      }

      if (!string3.isEmpty() && !SHA1_PATTERN.matcher(string3).matches()) {
         LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
      }

      if (!serverPropertiesHandler.resourcePack.isEmpty() && string3.isEmpty()) {
         LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
      }

      return string3;
   }

   public ServerPropertiesHandler getProperties() {
      return this.propertiesLoader.getPropertiesHandler();
   }

   public void updateDifficulty() {
      this.setDifficulty(this.getProperties().difficulty, true);
   }

   public boolean isHardcore() {
      return this.getProperties().hardcore;
   }

   public SystemDetails addExtraSystemDetails(SystemDetails details) {
      details.addSection("Is Modded", () -> {
         return (String)this.getModdedStatusMessage().orElse("Unknown (can't tell)");
      });
      details.addSection("Type", () -> {
         return "Dedicated Server (map_server.txt)";
      });
      return details;
   }

   public void dumpProperties(Path file) throws IOException {
      ServerPropertiesHandler serverPropertiesHandler = this.getProperties();
      BufferedWriter writer = Files.newBufferedWriter(file);

      try {
         writer.write(String.format("sync-chunk-writes=%s%n", serverPropertiesHandler.syncChunkWrites));
         writer.write(String.format("gamemode=%s%n", serverPropertiesHandler.gameMode));
         writer.write(String.format("spawn-monsters=%s%n", serverPropertiesHandler.spawnMonsters));
         writer.write(String.format("entity-broadcast-range-percentage=%d%n", serverPropertiesHandler.entityBroadcastRangePercentage));
         writer.write(String.format("max-world-size=%d%n", serverPropertiesHandler.maxWorldSize));
         writer.write(String.format("spawn-npcs=%s%n", serverPropertiesHandler.spawnNpcs));
         writer.write(String.format("view-distance=%d%n", serverPropertiesHandler.viewDistance));
         writer.write(String.format("spawn-animals=%s%n", serverPropertiesHandler.spawnAnimals));
         writer.write(String.format("generate-structures=%s%n", serverPropertiesHandler.method_37371(this.registryManager).shouldGenerateStructures()));
         writer.write(String.format("use-native=%s%n", serverPropertiesHandler.useNativeTransport));
         writer.write(String.format("rate-limit=%d%n", serverPropertiesHandler.rateLimit));
      } catch (Throwable var7) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (writer != null) {
         writer.close();
      }

   }

   public Optional<String> getModdedStatusMessage() {
      String string = this.getServerModName();
      return !"vanilla".equals(string) ? Optional.of("Definitely; Server brand changed to '" + string + "'") : Optional.empty();
   }

   public void exit() {
      if (this.filterer != null) {
         this.filterer.close();
      }

      if (this.gui != null) {
         this.gui.stop();
      }

      if (this.rconServer != null) {
         this.rconServer.stop();
      }

      if (this.queryResponseHandler != null) {
         this.queryResponseHandler.stop();
      }

   }

   public void tickWorlds(BooleanSupplier shouldKeepTicking) {
      super.tickWorlds(shouldKeepTicking);
      this.executeQueuedCommands();
   }

   public boolean isNetherAllowed() {
      return this.getProperties().allowNether;
   }

   public void addSnooperInfo(Snooper snooper) {
      snooper.addInfo("whitelist_enabled", this.getPlayerManager().isWhitelistEnabled());
      snooper.addInfo("whitelist_count", this.getPlayerManager().getWhitelistedNames().length);
      super.addSnooperInfo(snooper);
   }

   public boolean isSnooperEnabled() {
      return this.getProperties().snooperEnabled;
   }

   public void enqueueCommand(String command, ServerCommandSource commandSource) {
      this.commandQueue.add(new PendingServerCommand(command, commandSource));
   }

   public void executeQueuedCommands() {
      while(!this.commandQueue.isEmpty()) {
         PendingServerCommand pendingServerCommand = (PendingServerCommand)this.commandQueue.remove(0);
         this.getCommandManager().execute(pendingServerCommand.source, pendingServerCommand.command);
      }

   }

   public boolean isDedicated() {
      return true;
   }

   public int getRateLimit() {
      return this.getProperties().rateLimit;
   }

   public boolean isUsingNativeTransport() {
      return this.getProperties().useNativeTransport;
   }

   public DedicatedPlayerManager getPlayerManager() {
      return (DedicatedPlayerManager)super.getPlayerManager();
   }

   public boolean isRemote() {
      return true;
   }

   public String getHostname() {
      return this.getServerIp();
   }

   public int getPort() {
      return this.getServerPort();
   }

   public String getMotd() {
      return this.getServerMotd();
   }

   public void createGui() {
      if (this.gui == null) {
         this.gui = DedicatedServerGui.create(this);
      }

   }

   public boolean hasGui() {
      return this.gui != null;
   }

   public boolean areCommandBlocksEnabled() {
      return this.getProperties().enableCommandBlock;
   }

   public int getSpawnProtectionRadius() {
      return this.getProperties().spawnProtection;
   }

   public boolean isSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player) {
      if (world.getRegistryKey() != World.OVERWORLD) {
         return false;
      } else if (this.getPlayerManager().getOpList().isEmpty()) {
         return false;
      } else if (this.getPlayerManager().isOperator(player.getGameProfile())) {
         return false;
      } else if (this.getSpawnProtectionRadius() <= 0) {
         return false;
      } else {
         BlockPos blockPos = world.getSpawnPos();
         int i = MathHelper.abs(pos.getX() - blockPos.getX());
         int j = MathHelper.abs(pos.getZ() - blockPos.getZ());
         int k = Math.max(i, j);
         return k <= this.getSpawnProtectionRadius();
      }
   }

   public boolean acceptsStatusQuery() {
      return this.getProperties().enableStatus;
   }

   public int getOpPermissionLevel() {
      return this.getProperties().opPermissionLevel;
   }

   public int getFunctionPermissionLevel() {
      return this.getProperties().functionPermissionLevel;
   }

   public void setPlayerIdleTimeout(int playerIdleTimeout) {
      super.setPlayerIdleTimeout(playerIdleTimeout);
      this.propertiesLoader.apply((serverPropertiesHandler) -> {
         return (ServerPropertiesHandler)serverPropertiesHandler.playerIdleTimeout.set(this.getRegistryManager(), playerIdleTimeout);
      });
   }

   public boolean shouldBroadcastRconToOps() {
      return this.getProperties().broadcastRconToOps;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return this.getProperties().broadcastConsoleToOps;
   }

   public int getMaxWorldBorderRadius() {
      return this.getProperties().maxWorldSize;
   }

   public int getNetworkCompressionThreshold() {
      return this.getProperties().networkCompressionThreshold;
   }

   protected boolean convertData() {
      boolean bl = false;

      int i;
      for(i = 0; !bl && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         bl = ServerConfigHandler.convertBannedPlayers(this);
      }

      boolean bl2 = false;

      for(i = 0; !bl2 && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         bl2 = ServerConfigHandler.convertBannedIps(this);
      }

      boolean bl3 = false;

      for(i = 0; !bl3 && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         bl3 = ServerConfigHandler.convertOperators(this);
      }

      boolean bl4 = false;

      for(i = 0; !bl4 && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         bl4 = ServerConfigHandler.convertWhitelist(this);
      }

      boolean bl5 = false;

      for(i = 0; !bl5 && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         bl5 = ServerConfigHandler.convertPlayerFiles(this);
      }

      return bl || bl2 || bl3 || bl4 || bl5;
   }

   private void sleepFiveSeconds() {
      try {
         Thread.sleep(5000L);
      } catch (InterruptedException var2) {
      }
   }

   public long getMaxTickTime() {
      return this.getProperties().maxTickTime;
   }

   public String getPlugins() {
      return "";
   }

   public String executeRconCommand(String command) {
      this.rconCommandOutput.clear();
      this.submitAndJoin(() -> {
         this.getCommandManager().execute(this.rconCommandOutput.createRconCommandSource(), command);
      });
      return this.rconCommandOutput.asString();
   }

   public void setUseWhitelist(boolean useWhitelist) {
      this.propertiesLoader.apply((serverPropertiesHandler) -> {
         return (ServerPropertiesHandler)serverPropertiesHandler.whiteList.set(this.getRegistryManager(), useWhitelist);
      });
   }

   public void shutdown() {
      super.shutdown();
      Util.shutdownExecutors();
   }

   public boolean isHost(GameProfile profile) {
      return false;
   }

   public int adjustTrackingDistance(int initialDistance) {
      return this.getProperties().entityBroadcastRangePercentage * initialDistance / 100;
   }

   public String getLevelName() {
      return this.session.getDirectoryName();
   }

   public boolean syncChunkWrites() {
      return this.propertiesLoader.getPropertiesHandler().syncChunkWrites;
   }

   public TextStream createFilterer(ServerPlayerEntity player) {
      return this.filterer != null ? this.filterer.createFilterer(player.getGameProfile()) : TextStream.UNFILTERED;
   }

   public boolean requireResourcePack() {
      return this.propertiesLoader.getPropertiesHandler().requireResourcePack;
   }

   @Nullable
   public GameMode getForcedGameMode() {
      return this.propertiesLoader.getPropertiesHandler().forceGameMode ? this.saveProperties.getGameMode() : null;
   }

   @Nullable
   private static Text parseResourcePackPrompt(ServerPropertiesLoader propertiesLoader) {
      String string = propertiesLoader.getPropertiesHandler().resourcePackPrompt;
      if (!Strings.isNullOrEmpty(string)) {
         try {
            return Text.Serializer.fromJson(string);
         } catch (Exception var3) {
            LOGGER.warn((String)"Failed to parse resource pack prompt '{}'", (Object)string, (Object)var3);
         }
      }

      return null;
   }

   @Nullable
   public Text getResourcePackPrompt() {
      return this.resourcePackPrompt;
   }
}
