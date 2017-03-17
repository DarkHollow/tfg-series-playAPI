package json;

public class TvShowViews {
  public static class SearchTvShow {}
  public static class SearchTVDB extends SearchTvShow {}
  public static class FullTvShow extends SearchTvShow {}
  public static class InternalFullTvShow extends FullTvShow {}
}
