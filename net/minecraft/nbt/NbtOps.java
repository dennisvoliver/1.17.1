package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/**
 * Used to handle Minecraft NBTs within {@link com.mojang.serialization.Dynamic
 * dynamics} for DataFixerUpper, allowing generalized serialization logic
 * shared across different type of data structures. Use {@link NbtOps#INSTANCE}
 * for the ops singleton.
 * 
 * <p>For instance, dimension data may be stored as JSON in data packs, but
 * they will be transported in packets as NBT. DataFixerUpper allows
 * generalizing the dimension serialization logic to prevent duplicate code,
 * where the NBT ops allow the DataFixerUpper dimension serialization logic
 * to interact with Minecraft NBTs.
 * 
 * @see NbtOps#INSTANCE
 */
public class NbtOps implements DynamicOps<NbtElement> {
   /**
    * An singleton of the NBT dynamic ops.
    * 
    * <p>This ops does not compress maps (replace field name to value pairs
    * with an ordered list of values in serialization). In fact, since
    * Minecraft NBT lists can only contain elements of the same type, this op
    * cannot compress maps.
    */
   public static final NbtOps INSTANCE = new NbtOps();

   protected NbtOps() {
   }

   public NbtElement empty() {
      return NbtNull.INSTANCE;
   }

   public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement nbtElement) {
      switch(nbtElement.getType()) {
      case 0:
         return dynamicOps.empty();
      case 1:
         return dynamicOps.createByte(((AbstractNbtNumber)nbtElement).byteValue());
      case 2:
         return dynamicOps.createShort(((AbstractNbtNumber)nbtElement).shortValue());
      case 3:
         return dynamicOps.createInt(((AbstractNbtNumber)nbtElement).intValue());
      case 4:
         return dynamicOps.createLong(((AbstractNbtNumber)nbtElement).longValue());
      case 5:
         return dynamicOps.createFloat(((AbstractNbtNumber)nbtElement).floatValue());
      case 6:
         return dynamicOps.createDouble(((AbstractNbtNumber)nbtElement).doubleValue());
      case 7:
         return dynamicOps.createByteList(ByteBuffer.wrap(((NbtByteArray)nbtElement).getByteArray()));
      case 8:
         return dynamicOps.createString(nbtElement.asString());
      case 9:
         return this.convertList(dynamicOps, nbtElement);
      case 10:
         return this.convertMap(dynamicOps, nbtElement);
      case 11:
         return dynamicOps.createIntList(Arrays.stream(((NbtIntArray)nbtElement).getIntArray()));
      case 12:
         return dynamicOps.createLongList(Arrays.stream(((NbtLongArray)nbtElement).getLongArray()));
      default:
         throw new IllegalStateException("Unknown tag type: " + nbtElement);
      }
   }

   public DataResult<Number> getNumberValue(NbtElement nbtElement) {
      return nbtElement instanceof AbstractNbtNumber ? DataResult.success(((AbstractNbtNumber)nbtElement).numberValue()) : DataResult.error("Not a number");
   }

   public NbtElement createNumeric(Number number) {
      return NbtDouble.of(number.doubleValue());
   }

   public NbtElement createByte(byte b) {
      return NbtByte.of(b);
   }

   public NbtElement createShort(short s) {
      return NbtShort.of(s);
   }

   public NbtElement createInt(int i) {
      return NbtInt.of(i);
   }

   public NbtElement createLong(long l) {
      return NbtLong.of(l);
   }

   public NbtElement createFloat(float f) {
      return NbtFloat.of(f);
   }

   public NbtElement createDouble(double d) {
      return NbtDouble.of(d);
   }

   public NbtElement createBoolean(boolean bl) {
      return NbtByte.of(bl);
   }

   public DataResult<String> getStringValue(NbtElement nbtElement) {
      return nbtElement instanceof NbtString ? DataResult.success(nbtElement.asString()) : DataResult.error("Not a string");
   }

   public NbtElement createString(String string) {
      return NbtString.of(string);
   }

   private static AbstractNbtList<?> method_29144(byte b, byte c) {
      if (method_29145(b, c, (byte)4)) {
         return new NbtLongArray(new long[0]);
      } else if (method_29145(b, c, (byte)1)) {
         return new NbtByteArray(new byte[0]);
      } else {
         return (AbstractNbtList)(method_29145(b, c, (byte)3) ? new NbtIntArray(new int[0]) : new NbtList());
      }
   }

   private static boolean method_29145(byte b, byte c, byte d) {
      return b == d && (c == d || c == 0);
   }

   private static <T extends NbtElement> void method_29151(AbstractNbtList<T> abstractNbtList, NbtElement nbtElement, NbtElement nbtElement2) {
      if (nbtElement instanceof AbstractNbtList) {
         AbstractNbtList<?> abstractNbtList2 = (AbstractNbtList)nbtElement;
         abstractNbtList2.forEach((nbtElementx) -> {
            abstractNbtList.add(nbtElementx);
         });
      }

      abstractNbtList.add(nbtElement2);
   }

   private static <T extends NbtElement> void method_29150(AbstractNbtList<T> abstractNbtList, NbtElement nbtElement, List<NbtElement> list) {
      if (nbtElement instanceof AbstractNbtList) {
         AbstractNbtList<?> abstractNbtList2 = (AbstractNbtList)nbtElement;
         abstractNbtList2.forEach((nbtElementx) -> {
            abstractNbtList.add(nbtElementx);
         });
      }

      list.forEach((nbtElementx) -> {
         abstractNbtList.add(nbtElementx);
      });
   }

   public DataResult<NbtElement> mergeToList(NbtElement nbtElement, NbtElement nbtElement2) {
      if (!(nbtElement instanceof AbstractNbtList) && !(nbtElement instanceof NbtNull)) {
         return DataResult.error("mergeToList called with not a list: " + nbtElement, (Object)nbtElement);
      } else {
         AbstractNbtList<?> abstractNbtList = method_29144(nbtElement instanceof AbstractNbtList ? ((AbstractNbtList)nbtElement).getHeldType() : 0, nbtElement2.getType());
         method_29151(abstractNbtList, nbtElement, nbtElement2);
         return DataResult.success(abstractNbtList);
      }
   }

   public DataResult<NbtElement> mergeToList(NbtElement nbtElement, List<NbtElement> list) {
      if (!(nbtElement instanceof AbstractNbtList) && !(nbtElement instanceof NbtNull)) {
         return DataResult.error("mergeToList called with not a list: " + nbtElement, (Object)nbtElement);
      } else {
         AbstractNbtList<?> abstractNbtList = method_29144(nbtElement instanceof AbstractNbtList ? ((AbstractNbtList)nbtElement).getHeldType() : 0, (Byte)list.stream().findFirst().map(NbtElement::getType).orElse((byte)0));
         method_29150(abstractNbtList, nbtElement, list);
         return DataResult.success(abstractNbtList);
      }
   }

   public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, NbtElement nbtElement2, NbtElement nbtElement3) {
      if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtNull)) {
         return DataResult.error("mergeToMap called with not a map: " + nbtElement, (Object)nbtElement);
      } else if (!(nbtElement2 instanceof NbtString)) {
         return DataResult.error("key is not a string: " + nbtElement2, (Object)nbtElement);
      } else {
         NbtCompound nbtCompound = new NbtCompound();
         if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound2.getKeys().forEach((string) -> {
               nbtCompound.put(string, nbtCompound2.get(string));
            });
         }

         nbtCompound.put(nbtElement2.asString(), nbtElement3);
         return DataResult.success(nbtCompound);
      }
   }

   public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, MapLike<NbtElement> mapLike) {
      if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtNull)) {
         return DataResult.error("mergeToMap called with not a map: " + nbtElement, (Object)nbtElement);
      } else {
         NbtCompound nbtCompound = new NbtCompound();
         if (nbtElement instanceof NbtCompound) {
            NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
            nbtCompound2.getKeys().forEach((string) -> {
               nbtCompound.put(string, nbtCompound2.get(string));
            });
         }

         List<NbtElement> list = Lists.newArrayList();
         mapLike.entries().forEach((pair) -> {
            NbtElement nbtElement = (NbtElement)pair.getFirst();
            if (!(nbtElement instanceof NbtString)) {
               list.add(nbtElement);
            } else {
               nbtCompound.put(nbtElement.asString(), (NbtElement)pair.getSecond());
            }
         });
         return !list.isEmpty() ? DataResult.error("some keys are not strings: " + list, (Object)nbtCompound) : DataResult.success(nbtCompound);
      }
   }

   public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement nbtElement) {
      if (!(nbtElement instanceof NbtCompound)) {
         return DataResult.error("Not a map: " + nbtElement);
      } else {
         NbtCompound nbtCompound = (NbtCompound)nbtElement;
         return DataResult.success(nbtCompound.getKeys().stream().map((string) -> {
            return Pair.of(this.createString(string), nbtCompound.get(string));
         }));
      }
   }

   public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement nbtElement) {
      if (!(nbtElement instanceof NbtCompound)) {
         return DataResult.error("Not a map: " + nbtElement);
      } else {
         NbtCompound nbtCompound = (NbtCompound)nbtElement;
         return DataResult.success((biConsumer) -> {
            nbtCompound.getKeys().forEach((string) -> {
               biConsumer.accept(this.createString(string), nbtCompound.get(string));
            });
         });
      }
   }

   public DataResult<MapLike<NbtElement>> getMap(NbtElement nbtElement) {
      if (!(nbtElement instanceof NbtCompound)) {
         return DataResult.error("Not a map: " + nbtElement);
      } else {
         final NbtCompound nbtCompound = (NbtCompound)nbtElement;
         return DataResult.success(new MapLike<NbtElement>() {
            @Nullable
            public NbtElement get(NbtElement nbtElement) {
               return nbtCompound.get(nbtElement.asString());
            }

            @Nullable
            public NbtElement get(String string) {
               return nbtCompound.get(string);
            }

            public Stream<Pair<NbtElement, NbtElement>> entries() {
               return nbtCompound.getKeys().stream().map((string) -> {
                  return Pair.of(NbtOps.this.createString(string), nbtCompound.get(string));
               });
            }

            public String toString() {
               return "MapLike[" + nbtCompound + "]";
            }
         });
      }
   }

   public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
      NbtCompound nbtCompound = new NbtCompound();
      stream.forEach((pair) -> {
         nbtCompound.put(((NbtElement)pair.getFirst()).asString(), (NbtElement)pair.getSecond());
      });
      return nbtCompound;
   }

   public DataResult<Stream<NbtElement>> getStream(NbtElement nbtElement) {
      return nbtElement instanceof AbstractNbtList ? DataResult.success(((AbstractNbtList)nbtElement).stream().map((nbtElementx) -> {
         return nbtElementx;
      })) : DataResult.error("Not a list");
   }

   public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement nbtElement) {
      if (nbtElement instanceof AbstractNbtList) {
         AbstractNbtList<?> abstractNbtList = (AbstractNbtList)nbtElement;
         Objects.requireNonNull(abstractNbtList);
         return DataResult.success(abstractNbtList::forEach);
      } else {
         return DataResult.error("Not a list: " + nbtElement);
      }
   }

   public DataResult<ByteBuffer> getByteBuffer(NbtElement nbtElement) {
      return nbtElement instanceof NbtByteArray ? DataResult.success(ByteBuffer.wrap(((NbtByteArray)nbtElement).getByteArray())) : DynamicOps.super.getByteBuffer(nbtElement);
   }

   public NbtElement createByteList(ByteBuffer byteBuffer) {
      return new NbtByteArray(DataFixUtils.toArray(byteBuffer));
   }

   public DataResult<IntStream> getIntStream(NbtElement nbtElement) {
      return nbtElement instanceof NbtIntArray ? DataResult.success(Arrays.stream(((NbtIntArray)nbtElement).getIntArray())) : DynamicOps.super.getIntStream(nbtElement);
   }

   public NbtElement createIntList(IntStream intStream) {
      return new NbtIntArray(intStream.toArray());
   }

   public DataResult<LongStream> getLongStream(NbtElement nbtElement) {
      return nbtElement instanceof NbtLongArray ? DataResult.success(Arrays.stream(((NbtLongArray)nbtElement).getLongArray())) : DynamicOps.super.getLongStream(nbtElement);
   }

   public NbtElement createLongList(LongStream longStream) {
      return new NbtLongArray(longStream.toArray());
   }

   public NbtElement createList(Stream<NbtElement> stream) {
      PeekingIterator<NbtElement> peekingIterator = Iterators.peekingIterator(stream.iterator());
      if (!peekingIterator.hasNext()) {
         return new NbtList();
      } else {
         NbtElement nbtElement = (NbtElement)peekingIterator.peek();
         ArrayList list3;
         if (nbtElement instanceof NbtByte) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (nbtElementx) -> {
               return ((NbtByte)nbtElementx).byteValue();
            }));
            return new NbtByteArray(list3);
         } else if (nbtElement instanceof NbtInt) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (nbtElementx) -> {
               return ((NbtInt)nbtElementx).intValue();
            }));
            return new NbtIntArray(list3);
         } else if (nbtElement instanceof NbtLong) {
            list3 = Lists.newArrayList(Iterators.transform(peekingIterator, (nbtElementx) -> {
               return ((NbtLong)nbtElementx).longValue();
            }));
            return new NbtLongArray(list3);
         } else {
            NbtList nbtList = new NbtList();

            while(peekingIterator.hasNext()) {
               NbtElement nbtElement2 = (NbtElement)peekingIterator.next();
               if (!(nbtElement2 instanceof NbtNull)) {
                  nbtList.add(nbtElement2);
               }
            }

            return nbtList;
         }
      }
   }

   public NbtElement remove(NbtElement nbtElement, String string) {
      if (nbtElement instanceof NbtCompound) {
         NbtCompound nbtCompound = (NbtCompound)nbtElement;
         NbtCompound nbtCompound2 = new NbtCompound();
         nbtCompound.getKeys().stream().filter((k) -> {
            return !Objects.equals(k, string);
         }).forEach((k) -> {
            nbtCompound2.put(k, nbtCompound.get(k));
         });
         return nbtCompound2;
      } else {
         return nbtElement;
      }
   }

   public String toString() {
      return "NBT";
   }

   public RecordBuilder<NbtElement> mapBuilder() {
      return new NbtOps.MapBuilder();
   }

   private class MapBuilder extends AbstractStringBuilder<NbtElement, NbtCompound> {
      protected MapBuilder() {
         super(NbtOps.this);
      }

      protected NbtCompound initBuilder() {
         return new NbtCompound();
      }

      protected NbtCompound append(String string, NbtElement nbtElement, NbtCompound nbtCompound) {
         nbtCompound.put(string, nbtElement);
         return nbtCompound;
      }

      protected DataResult<NbtElement> build(NbtCompound nbtCompound, NbtElement nbtElement) {
         if (nbtElement != null && nbtElement != NbtNull.INSTANCE) {
            if (!(nbtElement instanceof NbtCompound)) {
               return DataResult.error("mergeToMap called with not a map: " + nbtElement, (Object)nbtElement);
            } else {
               NbtCompound nbtCompound2 = new NbtCompound(Maps.newHashMap(((NbtCompound)nbtElement).toMap()));
               Iterator var4 = nbtCompound.toMap().entrySet().iterator();

               while(var4.hasNext()) {
                  Entry<String, NbtElement> entry = (Entry)var4.next();
                  nbtCompound2.put((String)entry.getKey(), (NbtElement)entry.getValue());
               }

               return DataResult.success(nbtCompound2);
            }
         } else {
            return DataResult.success(nbtCompound);
         }
      }
   }
}
