package com.desiremc.core.parsers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.command.ArgumentParser;
import com.desiremc.core.session.HCFSession;
import com.desiremc.core.session.HCFSessionHandler;

public class PlayerHCFSessionParser implements ArgumentParser
{

    @Override
    public Object parseArgument(CommandSender sender, String label, String arg)
    {
        Player p = Bukkit.getPlayerExact(arg);
        HCFSession s;
        if (p == null)
        {
            s = HCFSessionHandler.findOfflinePlayerByName(arg);
        }
        else
        {
            s = HCFSessionHandler.getHCFSession(p.getUniqueId());
        }
        if (s == null)
        {
            DesireCore.getLangHandler().sendString(sender, "player-not-found");
            return null;
        }

        return s;
    }

}