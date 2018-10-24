package dbedit.plugin;

import dbedit.ExceptionDialog;

public final class PluginFactory {

    private static Plugin plugin;


    private PluginFactory() {
    }

    public static Plugin getPlugin() {
        if (plugin == null) {
            try {
                plugin = (Plugin) Class.forName("dbedit.plugin.DuPontPlugin").newInstance();
            } catch (ClassNotFoundException e) {
                plugin = new DefaultPlugin();
            } catch (IllegalAccessException e) {
                ExceptionDialog.hideException(e);
            } catch (InstantiationException e) {
                ExceptionDialog.hideException(e);
            }
        }
        return plugin;
    }
}
