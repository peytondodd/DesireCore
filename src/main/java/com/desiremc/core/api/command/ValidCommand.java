package com.desiremc.core.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.desiremc.core.DesireCore;
import com.desiremc.core.api.command.arity.CommandArity;
import com.desiremc.core.api.command.arity.OptionalCommandArity;
import com.desiremc.core.api.command.arity.OptionalVariadicCommandArity;
import com.desiremc.core.api.command.arity.RequiredVariadicCommandArity;
import com.desiremc.core.api.command.arity.StrictCommandArity;
import com.desiremc.core.session.Rank;

/**
 * @author Ryan Radomski
 */
public abstract class ValidCommand
{

    public static final int ARITY_STRICT = 0;
    public static final int ARITY_OPTIONAL = 1;
    public static final int ARITY_OPTIONAL_VARIADIC = 2;
    public static final int ARITY_REQUIRED_VARIADIC = 3;

    protected List<ValidCommand> subCommands;

    protected String name;
    protected String description;

    protected Rank requiredRank;

    protected String[] aliases;
    protected String[] args;

    protected List<CommandValidator> validators;

    protected List<CommandValidator> optionalExistsValidators;
    protected List<CommandValidator> optionalNonexistantValidators;

    protected ArgumentParser[] parsers;

    protected Map<String, Integer> argsMap;

    protected CommandArity commandArity;

    /**
     * @param name
     * @param description
     * @param aliases
     */
    public ValidCommand(String name, String description, Rank requiredRank, int commandArity, String args[], String... aliases)
    {
        this.name = name;
        this.description = description;
        this.requiredRank = requiredRank;
        this.aliases = aliases;
        this.subCommands = new ArrayList<>();
        this.validators = new LinkedList<>();
        this.optionalExistsValidators = new LinkedList<>();
        this.optionalNonexistantValidators = new LinkedList<>();
        this.argsMap = new HashMap<>();
        this.commandArity = newCommandArity(commandArity);
        this.args = args;

        for (int i = 0; i < args.length; i++)
        {
            argsMap.put(args[i], i);
        }

        parsers = new ArgumentParser[args.length];
    }

    public ValidCommand(String name, String description, Rank requiredRank, String args[], String... aliases)
    {
        this(name, description, requiredRank, ARITY_STRICT, args, aliases);
    }

    /**
     * Execute the command with the proper parameter.
     * 
     * @param sender
     * @param label
     * @param args
     */
    public void run(CommandSender sender, String label, String[] args)
    {
        List<String> listArguments = new ArrayList<>(Arrays.asList(args));
        if (!commandArity.validateArity(listArguments, this.args.length))
        {
            DesireCore.getLangHandler().sendUsageMessage(sender, label, (Object[]) this.args);
            return;
        }
        args = listArguments.toArray(new String[0]);

        Object[] parsedArgs = parseArguments(sender, label, args);

        if (parsedArgs == null || !isArgsValid(sender, label, parsedArgs, this.validators))
        {
            return;
        }

        if (commandArity.hasOptional())
        {
            if (parsedArgs.length == this.args.length && !isArgsValid(sender, label, parsedArgs, optionalExistsValidators))
            {
                return;
            }
            else if (!isArgsValid(sender, label, parsedArgs, optionalNonexistantValidators))
            {
                return;
            }
        }

        this.validRun(sender, label, parsedArgs);
    }

    private boolean isArgsValid(CommandSender sender, String label, Object[] parsedArgs, Collection<CommandValidator> validators)
    {
        for (CommandValidator validator : validators)
        {
            if (!validator.validate(sender, label, parsedArgs))
            {
                return false;
            }
        }

        return true;
    }

    private Object[] parseArguments(CommandSender sender, String label, String[] args)
    {
        Object[] parsedArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++)
        {
            Object parsed = getParser(i).parseArgument(sender, label, args[i]);

            if (parsed == null)
            {
                return null;
            }
            parsedArgs[i] = parsed;
        }
        return parsedArgs;
    }

    public abstract void validRun(CommandSender sender, String label, Object... args);

    /**
     * @return the name of the command.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return a description of the command.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the permission required to use the command.
     */
    protected Rank getRequiredRank()
    {
        return requiredRank;
    }

    /**
     * @return the aliases of the command.
     */
    public String[] getAliases()
    {
        return aliases;
    }

    /**
     * Checks whether the passed in command string matches this particular custom command.
     * 
     * @param command
     * @return whether the parameter matches the command.
     */
    public boolean matches(String command)
    {
        if (command == null)
        {
            return false;
        }
        if (command.equalsIgnoreCase(name))
        {
            return true;
        }
        for (String alias : aliases)
        {
            if (command.equalsIgnoreCase(alias))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a new subcommand.
     * 
     * @param subCommand
     */
    public void addSubCommand(ValidCommand subCommand)
    {
        this.subCommands.add(subCommand);
    }

    /**
     * @param cmd
     * @return the subcommand of the given name. Null if it does not exist.
     */
    public ValidCommand getSubCommand(String cmd)
    {
        for (ValidCommand command : this.subCommands)
        {
            if (command.matches(cmd))
            {
                return command;
            }
        }
        return null;
    }

    protected String getSubCommandString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < subCommands.size(); i++)
        {
            sb.append(subCommands.get(i).getName());
            if (i != subCommands.size() - 1)
            {
                sb.append('/');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public void addValidator(CommandValidator validator, String... argsToValidate)
    {
        validator.setArgsToValidate(argsMap, argsToValidate);
        validators.add(validator);
    }

    public void addOptionalExistsValidator(CommandValidator validator, String... argsToValidate)
    {
        validator.setArgsToValidate(argsMap, argsToValidate);
        optionalExistsValidators.add(validator);
    }

    public void addOptionalNonexistantValidator(CommandValidator validator, String... argsToValidate)
    {
        validator.setArgsToValidate(argsMap, argsToValidate);
        optionalNonexistantValidators.add(validator);
    }

    public void addParser(ArgumentParser parser, String... argsToParse)
    {
        for (String arg : argsToParse)
        {
            Integer index = argsMap.get(arg);

            if (index == null)
            {
                throw new IllegalArgumentException("Argument " + arg + " not found.");
            }

            parsers[argsMap.get(arg)] = parser;
        }
    }

    public String[] getArgs()
    {
        return args;
    }

    private ArgumentParser getParser(int argIndex)
    {
        return argIndex >= args.length ? parsers[args.length - 1] : parsers[argIndex];
    }

    private CommandArity newCommandArity(int option)
    {
        switch (option)
        {
            case ARITY_STRICT:
                return new StrictCommandArity();
            case ARITY_OPTIONAL:
                return new OptionalCommandArity();
            case ARITY_REQUIRED_VARIADIC:
                return new RequiredVariadicCommandArity();
            case ARITY_OPTIONAL_VARIADIC:
                return new OptionalVariadicCommandArity();
            default:
                throw new IllegalArgumentException("Arity not found.");
        }
    }

}