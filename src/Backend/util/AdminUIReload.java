package Backend.util;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AdminUIReload extends JPanel {

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    private AdminUIReload() {}

    public static void Register(Runnable listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void Unregister(Runnable listener) {
        LISTENERS.remove(listener);
    }

    public static void ReloadAll() {
        for (Runnable listener : LISTENERS) {
            SwingUtilities.invokeLater(listener);
        }
    }
}
