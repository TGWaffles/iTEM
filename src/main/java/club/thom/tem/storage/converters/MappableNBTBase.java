package club.thom.tem.storage.converters;

import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.dizitart.no2.Document;
import org.dizitart.no2.KeyValuePair;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Iterator;

public class MappableNBTBase implements Mappable {
    NBTBase base;

    public MappableNBTBase() {
        this.base = null;
    }

    public MappableNBTBase(NBTBase base) {
        this.base = base;
    }

    public Object convertBaseToValue(NBTBase entity, NitriteMapper nitriteMapper) {
        switch (entity.getId()) {
            case Constants.NBT.TAG_BYTE:
                return ((NBTTagByte) entity).getByte();
            case Constants.NBT.TAG_SHORT:
                return ((NBTTagShort) entity).getShort();
            case Constants.NBT.TAG_INT:
                return ((NBTTagInt) entity).getInt();
            case Constants.NBT.TAG_STRING:
                return ((NBTTagString) entity).getString();
            case Constants.NBT.TAG_LONG:
                return ((NBTTagLong) entity).getLong();
            case Constants.NBT.TAG_FLOAT:
                return ((NBTTagFloat) entity).getFloat();
            case Constants.NBT.TAG_DOUBLE:
                return ((NBTTagDouble) entity).getDouble();
            case Constants.NBT.TAG_BYTE_ARRAY:
                return ((NBTTagByteArray) entity).getByteArray();
            case Constants.NBT.TAG_INT_ARRAY:
                return ((NBTTagIntArray) entity).getIntArray();
            case Constants.NBT.TAG_COMPOUND:
                Document embeddedDocument = new Document();
                NBTTagCompound compound = (NBTTagCompound) entity;
                for (String key : compound.getKeySet()) {
                    embeddedDocument.put(key, new MappableNBTBase(compound.getTag(key)).write(nitriteMapper));
                }
                return embeddedDocument;
            case Constants.NBT.TAG_LIST:
                NBTTagList tagList = (NBTTagList) entity;
                byte tagType = (byte) tagList.getTagType();
                Document embeddedList = Document.createDocument("elementType", tagType);
                Object[] list = new Object[tagList.tagCount()];
                for (int i = 0; i < tagList.tagCount(); i++) {
                    list[i] = convertBaseToValue(tagList.get(i), nitriteMapper);
                }
                embeddedList.put("list", list);
                return embeddedList;
            default:
                return null;
        }
    }

    public NBTBase convertValueToBase(byte valueType, Object value, NitriteMapper nitriteMapper) {
        switch (valueType) {
            case Constants.NBT.TAG_BYTE:
                return new NBTTagByte((byte) value);
            case Constants.NBT.TAG_SHORT:
                return new NBTTagShort((short) value);
            case Constants.NBT.TAG_INT:
                return new NBTTagInt((int) value);
            case Constants.NBT.TAG_STRING:
                return new NBTTagString((String) value);
            case Constants.NBT.TAG_LONG:
                return new NBTTagLong((long) value);
            case Constants.NBT.TAG_FLOAT:
                return new NBTTagFloat((float) value);
            case Constants.NBT.TAG_DOUBLE:
                return new NBTTagDouble((double) value);
            case Constants.NBT.TAG_BYTE_ARRAY:
                return new NBTTagByteArray((byte[]) value);
            case Constants.NBT.TAG_INT_ARRAY:
                return new NBTTagIntArray((int[]) value);
            case Constants.NBT.TAG_COMPOUND:
                Document embeddedDocument = (Document) value;
                NBTTagCompound compound = new NBTTagCompound();
                for (KeyValuePair entry : embeddedDocument) {
                    MappableNBTBase element = new MappableNBTBase();
                    element.read(nitriteMapper, (Document) entry.getValue());
                    compound.setTag(entry.getKey(), element.getBase());
                }
                return compound;
            case Constants.NBT.TAG_LIST:
                Document embeddedList = (Document) value;
                byte tagType = embeddedList.get("elementType", Byte.class);
                Object[] list = embeddedList.get("list", Object[].class);
                NBTTagList tagList = new NBTTagList();
                for (Object element : list) {
                    tagList.appendTag(convertValueToBase(tagType, element, nitriteMapper));
                }
                return tagList;
            default:
                return null;
        }
    }

    public NBTBase getBase() {
        return base;
    }


    @Override
    public Document write(NitriteMapper mapper) {
        return Document.createDocument("tagId", base.getId())
                .put("value", convertBaseToValue(base, mapper));
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        byte tagId = document.get("tagId", Byte.class);
        Object value = document.get("value");
        this.base = convertValueToBase(tagId, value, mapper);
    }
}
