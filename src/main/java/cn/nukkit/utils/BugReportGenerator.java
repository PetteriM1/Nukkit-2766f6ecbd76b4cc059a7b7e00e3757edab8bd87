package cn.nukkit.utils;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class BugReportGenerator extends Thread {

    private Throwable throwable;

    BugReportGenerator(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void run() {
        try {
            if (Server.getInstance().sentry != null) {
                sentry();
            }
        } catch (Exception ignored) {
            Server.getInstance().getLogger().error("[BugReport] Sending a bug report failed!");
        }
    }

    private void sentry() {
        Server.getInstance().getLogger().info("[BugReport] Sending a bug report ...");

        boolean pluginError = false;
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 0) {
            if (!throwable.getStackTrace()[0].getClassName().startsWith("cn.nukkit")) {
                pluginError = true;
            }
        }

        String cpuType = System.getenv("PROCESSOR_IDENTIFIER");
        OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Server.getInstance().sentry.getContext().clear();
        Server.getInstance().sentry.getContext().addExtra("Nukkit Version", Nukkit.VERSION);
        Server.getInstance().sentry.getContext().addExtra("Java Version", System.getProperty("java.vm.name") + " (" + System.getProperty("java.runtime.version") + ')');
        Server.getInstance().sentry.getContext().addExtra("Host OS", osMXBean.getName() + '-' + osMXBean.getArch() + " [" + osMXBean.getVersion() + ']');
        Server.getInstance().sentry.getContext().addExtra("Memory", getCount(osMXBean.getTotalPhysicalMemorySize(), true));
        Server.getInstance().sentry.getContext().addExtra("CPU Type", cpuType == null ? "UNKNOWN" : cpuType);
        Server.getInstance().sentry.getContext().addExtra("Available Cores", String.valueOf(osMXBean.getAvailableProcessors()));
        Server.getInstance().sentry.getContext().addExtra("Plugin Error", String.valueOf(pluginError));
        Server.getInstance().sentry.getContext().addTag("plugin_error", String.valueOf(pluginError));
        Server.getInstance().sentry.getContext().addTag("nukkit_version", Nukkit.VERSION);
        Server.getInstance().sentry.getContext().addTag("branch", Nukkit.getBranch());
        Server.getInstance().sentry.sendException(throwable);
    }

    public static String getCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
