package dbedit.plugin;

public class PluginFactory {

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
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return plugin;
    }
}
