package dbedit;

import java.util.List;
import java.util.ArrayList;

public class History {

    private int offset = -1;
    private List<String> history = new ArrayList<String>();

    public boolean hasPrevious() {
        return offset > 0;
    }

    public boolean hasNext() {
        return offset < history.size() - 1;
    }

    public String previous() {
        return history.get(--offset);
    }

    public String next() {
        return history.get(++offset);
    }

    public void add(String text) {
        offset = history.size() - 1;
        // don't add empty text or text that just has been added
        if (text.trim().length() > 0 && (history.isEmpty() || !text.equals(history.get(offset)))) {
            history.add(text);
            offset++;
        }
    }
}
