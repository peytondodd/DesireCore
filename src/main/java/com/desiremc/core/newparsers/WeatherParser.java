package com.desiremc.core.newparsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.WeatherType;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.newcommands.Parser;
import com.desiremc.core.session.Session;
import com.desiremc.core.utils.StringUtils;

public class WeatherParser implements Parser<WeatherType>
{

    private static final List<String> OPTIONS = Arrays.asList("sun", "sunny", "clear", "rain", "rainy", "raining", "storm", "stormy", "storming", "snow", "snowy", "snowing");
    
    @Override
    public WeatherType parseArgument(Session sender, String[] label, String rawArgument)
    {
        for (String str : getRecommendations(null, "s"))
        {
            System.out.println(str);
        }
        if (StringUtils.containsAny(rawArgument, "sun", "sunny", "clear"))
        {
            return WeatherType.CLEAR;
        }
        else if (StringUtils.containsAny(rawArgument, "rain", "rainy", "raining", "storm", "stormy", "storming", "snow", "snowy", "snowing"))
        {
            return WeatherType.DOWNFALL;
        }
        else
        {
            DesireCore.getLangHandler().sendRenderMessage(sender, "not_weather");
            return null;
        }

    }
    
    @Override
    public List<String> getRecommendations(Session sender, String lastWord)
    {
        lastWord = lastWord.toLowerCase();
        List<String> values = new ArrayList<>(OPTIONS);
        Iterator<String> it = values.iterator();
        while (it.hasNext())
        {
            if (!it.next().startsWith(lastWord))
            {
                it.remove();
            }
        }
        return values;
    }

}