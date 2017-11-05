package com.desiremc.core.commands.chat;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.LangHandler;
import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatClearCommand extends ValidCommand
{

    private static final LangHandler LANG = DesireCore.getLangHandler();

    public ChatClearCommand()
    {
        super("clear", "Clear all chat", Rank.MODERATOR, new String[] {});
    }

    public void validRun(CommandSender sender, String label, Object... args)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            Session session = SessionHandler.getSession(p.getUniqueId());

            if (session.getRank().isStaff())
            {
                if (!session.getName().equalsIgnoreCase(sender.getName()))
                {
                    LANG.sendRenderMessage(session, "staff.chat-cleared-all", "{player}", sender.getName());
                }
                continue;
            }

            for (int i = 0; i < 75; i++)
            {
                p.sendMessage("");
            }
            LANG.sendRenderMessage(session, "staff.chat-cleared-all", "{player}", sender.getName());
        }
        LANG.sendRenderMessage(sender, "staff.chat-cleared");
    }
}