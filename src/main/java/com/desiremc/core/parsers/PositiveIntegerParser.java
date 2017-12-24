package com.desiremc.core.parsers;

import java.util.List;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.newcommands.Parser;
import com.desiremc.core.session.Session;

public class PositiveIntegerParser implements Parser<Integer>
{

    @Override
    public Integer parseArgument(Session sender, String[] label, String rawArgument)
    {
        int val;
        try
        {
            val = Integer.parseInt(rawArgument);
        }
        catch (NumberFormatException ex)
        {
            DesireCore.getLangHandler().sendRenderMessage(sender, "arg_not_number");
            return null;
        }
        if (val < 0)
        {
            DesireCore.getLangHandler().sendRenderMessage(sender, "number.too_large");
            return null;
        }
        return val;
    }

    @Override
    public List<String> getRecommendations(Session sender, String lastWord)
    {
        return null;
    }

}