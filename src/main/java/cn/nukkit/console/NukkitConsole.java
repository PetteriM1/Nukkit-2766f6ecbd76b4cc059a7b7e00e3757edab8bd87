package cn.nukkit.console;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import cn.nukkit.event.server.ServerCommandEvent;
import co.aikar.timings.Timings;
import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class NukkitConsole extends SimpleTerminalConsole {

    private final BlockingQueue<String> consoleQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean executingCommands = new AtomicBoolean(false);

    @Override
    protected boolean isRunning() {
        return Server.getInstance().isRunning();
    }

    @Override
    protected void runCommand(String command) {
        if (executingCommands.get()) {
            if (Timings.serverCommandTimer != null) Timings.serverCommandTimer.startTiming();
            ServerCommandEvent event = new ServerCommandEvent(Server.getInstance().getConsoleSender(), command);
            if (Server.getInstance().getPluginManager() != null) {
                Server.getInstance().getPluginManager().callEvent(event);
            }
            if (!event.isCancelled()) {
                Server.getInstance().getScheduler().scheduleTask(() -> Server.getInstance().dispatchCommand(event.getSender(), event.getCommand()));
            }
            if (Timings.serverCommandTimer != null) Timings.serverCommandTimer.stopTiming();
        } else {
            consoleQueue.add(command);
        }
    }

    public String readLine() {
        try {
            return consoleQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutdown() {
        Server.getInstance().shutdown();
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        builder.completer(new NukkitConsoleCompleter());
        builder.appName(Nukkit.NUKKIT);
        builder.option(LineReader.Option.HISTORY_BEEP, false);
        builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true);
        builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true);
        return super.buildReader(builder);
    }

    public boolean isExecutingCommands() {
        return executingCommands.get();
    }

    public void setExecutingCommands(boolean executingCommands) {
        if (this.executingCommands.compareAndSet(!executingCommands, executingCommands) && executingCommands) {
            consoleQueue.clear();
        }
    }
}
