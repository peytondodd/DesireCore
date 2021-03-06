package com.desiremc.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Fancy JSON serialization mostly by evilmidget38.
 *
 * @author evilmidget38, gomeow
 */
public class Serialization
{

    public static Map<String, Object> toMap(JSONObject object)
    {
        Map<String, Object> map = new HashMap<>();

        // Weird case of bad meta causing null map to be passed here.
        if (object == null)
        {
            return map;
        }

        for (Object key : object.keySet())
        {
            map.put(key.toString(), fromJson(object.get(key)));
        }
        return map;
    }

    private static Object fromJson(Object json)
    {
        if (json == null)
        {
            return null;
        }
        else if (json instanceof JSONObject)
        {
            return toMap((JSONObject) json);
        }
        else if (json instanceof JSONArray)
        {
            return toList((JSONArray) json);
        }
        else
        {
            return json;
        }
    }

    public static List<Object> toList(JSONArray array)
    {
        List<Object> list = new ArrayList<>();
        for (Object value : array)
        {
            list.add(fromJson(value));
        }
        return list;
    }

    public static List<String> toStrings(List<ItemStack> items)
    {
        List<String> result = new ArrayList<>();
        for (ConfigurationSerializable cs : items)
        {
            if (cs == null)
            {
                result.add("null");
            }
            else
            {
                result.add(new JSONObject(serialize(cs)).toString());
            }
        }
        return result;
    }

    public static String toString(ItemStack item)
    {
        if (item == null)
        {
            return null;
        }
        return new JSONObject(serialize(item)).toString();
    }

    public static List<ItemStack> toItems(List<String> stringItems)
    {
        List<ItemStack> contents = new ArrayList<>();
        for (String piece : stringItems)
        {
            if (piece.equalsIgnoreCase("null"))
            {
                contents.add(null);
            }
            else
            {
                contents.add((ItemStack) deserialize(toMap((JSONObject) JSONValue.parse(piece))));
            }
        }
        return contents;
    }

    public static Map<String, Object> serialize(ConfigurationSerializable cs)
    {
        Map<String, Object> returnVal = handleSerialization(cs.serialize());
        returnVal.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(cs.getClass()));
        return returnVal;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> handleSerialization(Map<String, Object> map)
    {
        Map<String, Object> serialized = recreateMap(map);
        for (Entry<String, Object> entry : serialized.entrySet())
        {
            if (entry.getValue() instanceof ConfigurationSerializable)
            {
                entry.setValue(serialize((ConfigurationSerializable) entry.getValue()));
            }
            else if (entry.getValue() instanceof Iterable<?>)
            {
                List<Object> newList = new ArrayList<>();
                for (Object object : ((Iterable<?>) entry.getValue()))
                {
                    if (object instanceof ConfigurationSerializable)
                    {
                        object = serialize((ConfigurationSerializable) object);
                    }
                    newList.add(object);
                }
                entry.setValue(newList);
            }
            else if (entry.getValue() instanceof Map<?, ?>)
            {
                // unchecked cast here. If you're serializing to a non-standard
                // Map you deserve ClassCastExceptions
                entry.setValue(handleSerialization((Map<String, Object>) entry.getValue()));
            }
        }
        return serialized;
    }

    public static Map<String, Object> recreateMap(Map<String, Object> original)
    {
        Map<String, Object> map = new HashMap<>();
        map.putAll(original);
        return map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object deserialize(Map<String, Object> map)
    {
        for (Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof Map)
            {
                entry.setValue(deserialize((Map) entry.getValue()));
            }
            else if (entry.getValue() instanceof Iterable)
            {
                entry.setValue(convertIterable((Iterable) entry.getValue()));
            }
            else if (entry.getValue() instanceof Number)
            {
                entry.setValue(convertNumber((Number) entry.getValue()));
            }
        }
        return map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY) ? ConfigurationSerialization.deserializeObject(map) : map;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<?> convertIterable(Iterable<?> iterable)
    {
        List<Object> newList = new ArrayList<>();
        for (Object object : iterable)
        {
            if (object instanceof Map)
            {
                object = deserialize((Map<String, Object>) object);
            }
            else if (object instanceof List)
            {
                object = convertIterable((Iterable) object);
            }
            else if (object instanceof Number)
            {
                object = convertNumber((Number) object);
            }
            newList.add(object);
        }
        return newList;
    }

    private static Number convertNumber(Number number)
    {
        if (number instanceof Long)
        {
            Long longObj = (Long) number;
            if (longObj.longValue() == longObj.intValue())
            {
                return new Integer(longObj.intValue());
            }
        }
        return number;
    }

}