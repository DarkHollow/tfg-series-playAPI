package utils.json;

public class TvShowViews {
  public static class SearchTvShow {}
  public static class SearchTvShowTvdbId extends SearchTvShow {}
  public static class SearchTVDB extends SearchTvShowTvdbId {}
  public static class FullTvShow extends SearchTvShowTvdbId {}
  public static class InternalFullTvShow extends FullTvShow {}
}
