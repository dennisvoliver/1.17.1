package net.minecraft.world.event.listener;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import org.jetbrains.annotations.Nullable;

/**
 * A game event listener listens to game events from {@link GameEventDispatcher}s.
 */
public interface GameEventListener {
   /**
    * Returns the position source of this listener.
    */
   PositionSource getPositionSource();

   /**
    * Returns the range, in blocks, of the listener.
    */
   int getRange();

   /**
    * Listens to an incoming game event.
    * 
    * @return {@code true} if the game event has been accepted by this listener
    */
   boolean listen(World world, GameEvent event, @Nullable Entity entity, BlockPos pos);
}
