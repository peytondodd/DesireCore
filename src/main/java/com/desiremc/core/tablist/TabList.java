package com.desiremc.core.tablist;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import com.desiremc.core.DesireCore;
import com.desiremc.core.tablist.events.TabDeleteEvent;

public class TabList implements Listener
{

    private static TabList instance;
    private TabListOptions options;

    public TabList()
    {
        this(TabListOptions.getDefaultOptions());
    }

    public TabList(TabListOptions options)
    {
        if (Bukkit.getMaxPlayers() < 60)
        {
            throw new NumberFormatException("Player limit must be at least 60!");
        }
        else
        {
            TabList.instance = this;
            this.options = options;
            (new BukkitRunnable()
            {
                public void run()
                {
                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        TabList.this.checkPlayer(p);
                    }
                }
            }).runTaskLaterAsynchronously(DesireCore.getInstance(), 4L);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        
        System.out.println(Bukkit.getOnlinePlayers().contains(event.getPlayer()));

        Bukkit.getScheduler().runTaskLaterAsynchronously(DesireCore.getInstance(), new Runnable()
        {
            public void run()
            {
                TabList.this.checkPlayer(player);
                System.out.println(Bukkit.getOnlinePlayers().contains(event.getPlayer()));
            }
        }, 4l);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Tab playerTab = Tab.getByPlayer(player);

        if (playerTab != null)
        {
            Iterator<Team> iterator = (new HashSet<Team>(playerTab.getScoreboard().getTeams())).iterator();

            while (iterator.hasNext())
            {
                Team team = (Team) iterator.next();

                team.unregister();
            }

            Tab.getPlayerTabs().remove(playerTab);
            Bukkit.getPluginManager().callEvent(new TabDeleteEvent(playerTab));
        }

    }

    private void checkPlayer(Player player)
    {
        Tab playerTab = Tab.getByPlayer(player);

        if (playerTab == null)
        {
            long time = System.currentTimeMillis();

            new Tab(player);

            if (this.options.sendCreationMessage())
            {
                player.sendMessage(ChatColor.GRAY + "We created your tab list in a time of " + (System.currentTimeMillis() - time) + " ms.");
            }
        }
        else
        {
            playerTab.clear();
        }

    }

    public static TabList getInstance()
    {
        return TabList.instance;
    }

    public TabListOptions getOptions()
    {
        return this.options;
    }
}
