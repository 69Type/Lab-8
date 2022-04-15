public class LinkDict implements Comparable<LinkDict> {
    private final String link;
    private final int depth;

    public LinkDict(String link, int depth) {
        this.link = link;
        this.depth = depth;
    }

    public String getLink() {
        return link;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public int compareTo(LinkDict o) {
        return 0;
    }
}
