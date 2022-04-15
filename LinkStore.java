import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LinkStore {
    private final List<LinkDict> finalList = new ArrayList<>();

    public LinkStore(){}

    /* Добавляет в конечный лист новый объект ссылки */
    public void store(LinkDict cell){
        finalList.add(cell);
    }

    public List<LinkDict> storage(){
        return finalList;
    }

    /* Проверка на ссылку в списке */
    public boolean isLinkAlreadyInList(String URL){
        for (LinkDict cell : finalList){
            if (cell.getLink().equals(URL)) return true;
        }
        return false;
    }

    /* Проверка хоста в списке */
    public boolean isHostAlreadyInList(String link){
        try {
            URL newURL = new URL(link);
            for (LinkDict cell : finalList){
                URL existsURL = new URL(cell.getLink());
                if (existsURL.getHost().equals(newURL.getHost())) return true;
            }
        } catch (MalformedURLException ignored) {}
        return false;
    }
}