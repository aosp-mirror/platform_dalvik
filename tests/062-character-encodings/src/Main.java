import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.Set;

public class Main {
    static public void main(String[] args) throws Exception {
        SortedMap<String, Charset> all = Charset.availableCharsets();

        for (Map.Entry<String, Charset> e : all.entrySet()) {
            String canonicalName = e.getKey();
            System.out.println(canonicalName);
            Set<String> aliases = e.getValue().aliases();
            if ((aliases != null) && (aliases.size() != 0)) {
                ArrayList<String> list = new ArrayList<String>(aliases.size());
                list.addAll(aliases);
                Collections.sort(list);
                for (String s : list) {
                    if (! s.equals(canonicalName)) {
                        System.out.println("  " + s);
                    }
                }
            } else {
                System.out.println("  (no aliases)");
            }
        }
    }
}
