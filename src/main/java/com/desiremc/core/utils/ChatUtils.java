package com.desiremc.core.utils;

import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.tickets.TicketHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ChatUtils
{

    private final static int CENTER_PX = 154;

    public static void sendCenteredMessage(Player player, String message)
    {
        sendCenteredMessageFinal(SessionHandler.getSession(player), message);
    }

    public static void sendCenteredMessage(CommandSender sender, String message)
    {
        sendCenteredMessageFinal(SessionHandler.getSession(sender), message);
    }

    private static void sendCenteredMessageFinal(Session session, String message)
    {
        session.sendMessage(renderCenteredMessage(message));
    }

    public static String renderCenteredMessage(String message)
    {
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray())
        {
            if (c == ChatColor.COLOR_CHAR)
            {
                previousCode = true;
            }
            else if (previousCode)
            {
                previousCode = false;
                isBold = (c == 'l' || c == 'L');
            }
            else
            {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate)
        {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString() + message;
    }

    public static String getNameWithRankColor(UUID uuid, boolean prefix)
    {
        Session session = SessionHandler.getGeneralSession(uuid);
        if (prefix)
        {
            return session.getRank().getMain() + session.getRank().getPrefix() + ChatColor.GRAY + session.getName() + ChatColor.RESET;
        }
        else
        {
            return session.getRank().getMain() + session.getName() + ChatColor.RESET;
        }
    }

    public static void sendStaffMessage(Exception ex, JavaPlugin plugin)
    {
        String error = processException(ex, plugin);
        for (Session s : SessionHandler.getOnlineStaff())
        {
            s.getPlayer().sendMessage(error);
        }
        TicketHandler.openTicket(SessionHandler.getConsoleSession(), error.split("\n")[1]);
        Bukkit.getLogger().severe("===========================================");
        Bukkit.getLogger().severe("CATASTROPHIC FAILURE IN " + plugin.getName());
        Bukkit.getLogger().severe("CHECK TICKET OR ERRORS FOLDER WITHIN PLUGIN");
        Bukkit.getLogger().severe("===========================================");
    }

    private static String processException(Exception ex, JavaPlugin plugin)
    {

        try
        {
            File errorFolder = new File(plugin.getDataFolder() + "/errors/");
            if (errorFolder != null)
            {
                errorFolder.mkdir();
            }
            File file = new File(plugin.getDataFolder() + "/errors/", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS")) + ".log");
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file);
            writer.print(ExceptionUtils.getStackTrace(ex));
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        sb.append("§4§lA catastrophic error just occurred in " + plugin.getName() + ". If no admin or developer is online, please contact them immediately.\n");
        sb.append("§4§lFor developers: The exception was thrown at line " + ex.getStackTrace()[0].getLineNumber() + " in class " + ex.getStackTrace()[0].getClassName() + ".");

        return sb.toString();
    }

    public static String renderString(String string, String... arguments)
    {
        if (arguments.length % 2 != 0)
        {
            throw new IllegalArgumentException("Must have an even number of arguments.");
        }
        for (int i = 0; i < arguments.length; i += 2)
        {
            string = string.replace(arguments[i], arguments[i + 1]);
        }
        return string;
    }

}
