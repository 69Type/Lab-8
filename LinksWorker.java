import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksWorker {
    String text = "";
    List<String> list = new ArrayList<>();

    final String htmlLinkRegExp = "<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1";
    final String linkProtocolRegExp = "^(?:(ht|f)tp(s?)\\:\\/\\/)?";
    final String linkHostRegExp = "(?<=:\\/\\/)[^\\/]+";

    private String protocol = "http://";
    private String host = "www.google.com";

    public LinksWorker(String text){
        this.text = text;
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(htmlLinkRegExp).matcher(text);
        while (m.find()) {
            allMatches.add(m.group(2));
        }
        list = allMatches;
    }

    public void removeRepeats(){
        Set<String> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);
    }

    public void makeLinksValid(String curLink){
        Matcher m = Pattern.compile(linkProtocolRegExp).matcher(curLink);
        if (m.find()) {
            protocol = m.group(0);
        }
        m = Pattern.compile(linkHostRegExp).matcher(curLink);
        if (m.find()) {
            host = m.group(0);
        }

        /* удаляем точки и обратные слеши у ссылок для дальнейшей обработки */
        for (int i=0; i<list.size(); i++){
            String curItem = list.get(i);
            list.set(i, curItem.replaceAll("^\\.?\\/?", ""));

            m = Pattern.compile(linkProtocolRegExp).matcher(curItem);
            if (m.find()) {
                if (m.group().equals("")){
                    list.set(i, protocol + host + "/" + curItem);
                }
            }
        }

    }

    public void filterCannotStartsWith(String str){
        list.removeIf(a -> a.startsWith(str));
    }

    public List<String> getList(){
        return list;
    }

}
